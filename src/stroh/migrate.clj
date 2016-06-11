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

(defn- migration-installed? [db migration-key]
  (boolean (d/entity db [migration-key-attr migration-key])))

(defn- with-migration-key [k tx-data]
  (conj tx-data [:db/add (d/tempid :db.part/tx) migration-key-attr k]))

(defn- apply-migration [conn [migration-key tx-data]]
  (when-not (migration-installed? (d/db conn) migration-key)
    (log/info "apply migration" migration-key)
    @(d/transact conn (with-migration-key migration-key tx-data))))

(defn- apply-migrations [conn migrations]
  (doseq [m (partition 2 migrations)]
    (apply-migration conn m)))

(defn ensure-migrations
  "Ensures that all migrations are installed in the database, applying
  outstanding migrations if necessary."
  [database]
  (let [conn (:connection database)]
    (install-migration-key-attr conn)
    (apply-migrations conn
                      (edn/read-resource {:readers *data-readers*} "migrations.edn"))))
