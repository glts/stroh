(ns stroh.db
  "Persistent storage service."
  (:require [com.stuartsierra.component :as component]
            [datomic.api :as d]))

(defrecord DatomicDatabase [host port connection uri]
  component/Lifecycle
  (start [this]
    (if connection
      this
      (assoc this :connection (d/connect uri))))

  (stop [this]
    (if-not connection
      this
      (do
        (d/release connection)
        (assoc this :connection nil)))))

(defn database
  "Returns a new Datomic database component with options."
  [options]
  (map->DatomicDatabase options))

(defn- last-status-update [db task]
  (d/q '[:find ?txInstant .
         :in $ ?task
         :where [?task :task/status _ ?tx]
                [?tx :db/txInstant ?txInstant]]
       db task))

(defn find-all-tasks [database]
  (let [db (d/db (:connection database))
        tasks (d/q '[:find [?task ...] :where [?task :task/id]] db)]
    (map (fn [task]
           (let [t (into {} (d/entity db task))]
             (assoc t :status-updated (last-status-update db task))))
         tasks)))

(defn- task-created-at [db task]
  (d/q '[:find ?created-at .
         :in $ ?task
         :where [?task :task/id _ ?tx]
                [?tx :db/txInstant ?created-at]]
       db task))

; returns:
; {:status-history
;  [[inst :task.status/open]
;   [inst :task.status/in-progress]
;   [inst :task.status/in-progress]]}
(defn- find-status-changes [db id]
  (vec (sort-by first
                #(compare %2 %1)
                (d/q '[:find ?inst ?status-ident
                       :in $ ?task
                       :where [?task :task/status ?status ?tx true]
                              [?status :db/ident ?status-ident]
                              [?tx :db/txInstant ?inst]]
                     (d/history db) [:task/id id]))))

(defn find-task [database id]
  (let [db (d/db (:connection database))]
    (when-let [e (d/entity db [:task/id id])]
      (-> (into {} e)
          (assoc :history {:created-at (task-created-at db [:task/id id])
                           :status-history (find-status-changes db id)})))))

(defn- create-task-tx [task]
  [(assoc task :db/id (d/tempid :db.part/user))])

(defn create-task
  "Stores the specified task in the database."
  [database task]
  (let [tx (create-task-tx task)]
    @(d/transact (:connection database) tx)))

(defn update-status
  "Updates status of task identified by id, throwing if doesn't exist."
  [database id status]
  (let [conn (:connection database)]
    @(d/transact conn [{:db/id [:task/id id]
                       :task/status status}])))
