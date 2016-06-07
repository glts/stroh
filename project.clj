(defproject stroh "0.1.0-SNAPSHOT"
  :description "Personal task tracking"
  :dependencies [[org.clojure/clojure "1.9.0-alpha5"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.slf4j/slf4j-api "1.7.21"]
                 [ch.qos.logback/logback-classic "1.1.7"]
                 [com.datomic/datomic-pro "0.9.5372"
                  :exclusions [commons-codec
                               org.slf4j/slf4j-log4j12
                               org.slf4j/slf4j-nop]]
                 [couchbase/couchbase-client "1.0.3"
                  :exclusions [commons-codec
                               org.jboss.netty/netty
                               spy/spymemcached]]
                 [com.stuartsierra/component "0.3.1"]
                 [ring/ring-core "1.4.0"]
                 [ring/ring-jetty-adapter "1.4.0"]
                 [ring-jetty-component "0.3.1"]
                 [compojure "1.5.0"]
                 [medley "0.8.2"]
                 [enlive "1.1.6"]
                 [org.asciidoctor/asciidoctorj "1.5.4"]]
  :main ^:skip-aot stroh.main
  :jvm-opts ["-Duser.language=en" "-Duser.country=US"]
  :target-path "target/%s/"
  :profiles
  {:dev [:project/dev :profiles/dev]
   :profiles/dev {}
   :project/dev {:source-paths ["dev"]
                 :resource-paths ["local-resources"]
                 :dependencies [[org.clojure/tools.namespace "0.2.11"]]
                 :repl-options {:init-ns user}}
   :uberjar {:aot :all}})
