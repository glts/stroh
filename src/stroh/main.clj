(ns stroh.main
  "Main entry point for the stand-alone artefact of the Stroh app."
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [stroh.config :as config]
            [stroh.migrate :refer [ensure-migrations]]
            [stroh.util.edn :refer [read-resource]])
  (:gen-class))

(defn- log-uncaught-exceptions! []
  (Thread/setDefaultUncaughtExceptionHandler
    (reify Thread$UncaughtExceptionHandler
      (uncaughtException [_ thread e]
        (log/error e "uncaught exception on thread" (.getName thread))))))

(defn- run-on-shutdown! [^Runnable proc]
  (.addShutdownHook (Runtime/getRuntime) (Thread. proc)))

(defn -main
  "Starts the application when run as a uberjar."
  [& args]
  (log-uncaught-exceptions!)
  (let [options (read-resource "config.edn")
        system (component/start (config/system options))]
    (run-on-shutdown! #(component/stop system))
    (future
      (ensure-migrations (:database system)))
    (log/info "stroh system started")))
