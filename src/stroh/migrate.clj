(ns stroh.migrate
  "Database migration."
  (:require [clojure.tools.logging :as log]
            [datomic.api :as d]
            [stroh.util.edn :as edn]))

(def ^:private migration-key-attr :migration/key)

(defn- has-migration-key-attr? [db]
  (-> (d/entity db migration-key-attr)
      :db.install/_attribute
      boolean))

(defn- install-migration-key-attr [conn]
  (when-not (has-migration-key-attr? (d/db conn))
    @(d/transact conn [{:db/id (d/tempid :db.part/db)
                        :db/ident migration-key-attr
                        :db/valueType :db.type/keyword
                        :db/cardinality :db.cardinality/one
                        :db/unique :db.unique/identity
                        :db.install/_attribute :db.part/db}])))

(defn- migration-installed? [db key]
  (boolean (d/entity db [migration-key-attr key])))

(defn- with-migration-key [key tx-data]
  (conj tx-data [:db/add (d/tempid :db.part/tx) migration-key-attr key]))

(defn- apply-migrations [conn migrations]
  (doseq [[key tx-data] (partition 2 migrations)]
    (when-not (migration-installed? (d/db conn) key)
      (log/info "apply migration" key)
      @(d/transact conn (with-migration-key key tx-data)))))

(defn ensure-migrations
  "Ensures that all migrations are installed in the database, applying
  outstanding migrations if necessary."
  [database]
  (let [conn (:connection database)]
    (install-migration-key-attr conn)
    (apply-migrations conn
                      (edn/read-resource {:readers *data-readers*} "migrations.edn"))))
