(ns stroh.handler
  "Routing."
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [compojure.coercions :refer [as-uuid]]
            [compojure.core :refer [GET POST routes]]
            [compojure.route :refer [not-found]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.util.response :refer [redirect]]
            [stroh.tasks :as tasks]
            [stroh.ui :as ui]))

(defn make-routes [app]
  (routes
    (GET "/" []
      (ui/tasks-index app))
    (GET "/task/:id" [id :<< as-uuid]
      (ui/task-detail app id))
    (POST "/task/:id" [id :<< as-uuid, status]
      (tasks/update-task-status app id status)
      (redirect (str "/task/" id) :see-other))
    (POST "/task/new" {params :params}
      (let [task (tasks/create-task app (select-keys params [:title :status :type]))]
        (redirect (str "/task/" (:task/id task)) :see-other)))
    (not-found
      (ui/not-found-view app "Unknown route"))))

(defn app-routes [app]
  (-> (make-routes app)
      (wrap-keyword-params)
      (wrap-params)
      (wrap-resource "public")))

(defrecord StrohHandler [handler app]
  component/Lifecycle
  (start [this]
    (if handler
      this
      (assoc this :handler (app-routes app))))

  (stop [this]
    (if-not handler
      this
      (assoc this :handler nil))))

(defn handler
  "Returns a Ring handler for the app."
  [options]
  (map->StrohHandler options))
