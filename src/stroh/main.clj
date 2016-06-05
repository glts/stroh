(ns stroh.main
  "Main entry point for the stand-alone artefact of the Stroh app."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [stroh.config :as config])
  (:gen-class))

(defn- read-config []
  (edn/read-string (slurp (io/resource "config.edn"))))

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
  (let [options (read-config)
        system (component/start (config/system options))]
    (run-on-shutdown! #(component/stop system))
    (log/info "stroh system started")))
