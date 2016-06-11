(ns stroh.util.edn
  "Utilities for EDN configuration."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]))

(defn read-resource
  "Reads EDN data from a config resource in the classpath."
  ; TODO Remove duplication.
  ([r]
   (edn/read-string (slurp (io/resource r))))
  ([opts r]
   (edn/read-string opts (slurp (io/resource r)))))
