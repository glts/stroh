(ns stroh.db-test
  (:require [clojure.edn :as edn]
            [clojure.test :refer :all]
            [datomic.api :as d]
            [stroh.db :as db]))

(def uri "datomic:mem://strohtest")

(def ^:dynamic *conn*)

(defn- transact-schema [conn]
  (d/transact conn
    (edn/read-string {:readers *data-readers*} (slurp "resources/schema.edn"))))

(def task-fixture
  [{:db/id #db/id[:db.part/user]
    :task/id #uuid "00000000-0000-0000-0000-000000000000"
    :task/title "Manifest der Kommunistischen Partei"
    :task/type :task.type/book
    :task/status :task.status/done}])

(defn- setup-db-connection [test]
  (d/create-database uri)
  (binding [*conn* (d/connect uri)]
    (transact-schema *conn*)
    (test))
  (d/delete-database uri))

(use-fixtures :each setup-db-connection)

(deftest find-all-tasks-empty
  (let [database {:connection *conn*}]
    (is (empty? (db/find-all-tasks database)))))

(deftest find-all-tasks
  (d/transact *conn* task-fixture)
  (let [database {:connection *conn*}]
    (is (= 1 (count (db/find-all-tasks database))))))
