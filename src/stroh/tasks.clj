(ns stroh.tasks
  "Tasks service layer."
  (:require [clojure.set :as set]
            [clojure.spec :as s]
            [clojure.string :as string]
            [medley.core :refer [map-keys random-uuid]]
            [stroh.db :as db]))

(s/def :task/id uuid?)
(s/def :task/title (s/and string? (complement string/blank?)))
(s/def :task/status #{:task.status/open :task.status/in-progress :task.status/done})
(s/def :task/type
  #{:task.type/book
    :task.type/film
    :task.type/video
    :task.type/event
    :task.type/article
    :task.type/audio})

(s/def ::task
  (s/keys :req [:task/id :task/title :task/status :task/type]))

(def descending #(compare %2 %1))

(defn find-all-tasks
  "Find all tasks preparing them for display."
  [app]
  (let [tasks (db/find-all-tasks (:database app))]
    (-> (group-by :task/status tasks)
        (update :task.status/open #(sort-by :status-updated %))
        (update :task.status/in-progress #(sort-by :status-updated %))
        (update :task.status/done #(sort-by :status-updated descending %))
        (->> (map-keys (comp keyword name))))))

(defn find-task-by-id
  "Find a task by its id. Returns nil if not found."
  [app id]
  (db/find-task (:database app) id))

; TODO Improve ugly key renaming.
(defn coerce-to-task [task-data]
  (-> task-data
      (update :status #(keyword "task.status" %))
      (update :type #(keyword "task.type" %))
      (update :title string/trim)
      (set/rename-keys
        {:status :task/status, :type :task/type, :title :task/title})))

(defn create-task
  "Creates a new task based on the given task data. Throws if task
  data does not conform."
  [app task-data]
  (let [id (random-uuid)
        task (assoc (coerce-to-task task-data) :task/id id)]
    (if (s/valid? ::task task)
      (do (db/create-task (:database app) task)
          task)
      (throw (ex-info "bad things" (s/explain-data ::task task-data))))))

(defn update-task-status
  "Updates a task with a new status."
  [app id new-status]
  (let [new-status (keyword "task.status" new-status)]
    (if (s/valid? :task/status new-status)
      (db/update-status (:database app) id new-status)
      (throw (ex-info "invalid status" (s/explain-data :task/status new-status))))))
