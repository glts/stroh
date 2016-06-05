(ns stroh.config
  "Configuration."
  (:require [com.stuartsierra.component :as component]
            [ring.component.jetty :refer [jetty-server]]
            [stroh.db :as db]
            [stroh.handler :as handler]
            [stroh.render :as render]))

(defrecord StrohApp [database renderer]
  component/Lifecycle
  (start [this]
    this)

  (stop [this]
    this))

(defn app
  "Returns an app component that represents the running Stroh application."
  [options]
  (map->StrohApp options))

(defn system
  "Constructs a new component system representing the app."
  [options]
  (-> (component/system-map
        :server (jetty-server (:server options))
        :handler (handler/handler {})
        :app (app {})
        :database (db/database (:database options))
        :renderer (render/renderer {}))
      (component/system-using
        {:server {:app :handler}
         :handler [:app]
         :app [:database :renderer]})))
