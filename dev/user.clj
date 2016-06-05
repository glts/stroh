(ns user
  "Development utilities for Stuart Sierra's reloaded workflow."
  (:require [clojure.tools.namespace.repl :refer [refresh]]
            [com.stuartsierra.component :as component]
            [stroh.config :as config]))

(def dev-config
  {:server {:port 3000}
   :database {:uri "datomic:couchbase://localhost:4334/strohtest/strohtest"}})

(def system nil)

(defn start []
  (alter-var-root #'system
    (constantly (component/start (config/system dev-config)))))

(defn stop []
  (alter-var-root #'system component/stop))

(defn reset []
  (stop)
  (refresh :after `start))

#_(start)
#_(reset)
