(ns stroh.render
  "Asciidoctor text rendering facilities."
  (:require [com.stuartsierra.component :as component]
            [net.cgrand.enlive-html :as html])
  (:import [org.asciidoctor Asciidoctor Asciidoctor$Factory]))

(defrecord AsciidoctorRenderer [^Asciidoctor asciidoctor]
  component/Lifecycle
  (start [this]
    (if asciidoctor
      this
      (assoc this :asciidoctor (Asciidoctor$Factory/create))))

  (stop [this]
    (if-not asciidoctor
      this
      (do
        (.shutdown asciidoctor)
        (assoc this :asciidoctor nil)))))

(defn renderer
  "Returns a new renderer component."
  [options]
  (map->AsciidoctorRenderer options))

(def ^:private wrapper-selector
  [[:div.paragraph html/root] :> [:p html/only-child]])

(defn- unwrap-paragraph [rendered]
  (if-let [nodes (-> rendered
                     html/html-snippet
                     (html/select wrapper-selector)
                     (html/transform [[:p html/root]] html/unwrap)
                     html/emit*)]
    (apply str nodes)))

; TODO This is intended for use only with one-line strings.
(defn asciidoc
  "Render to an AsciiDoc string using an Asciidoctor renderer component."
  [renderer ^String s]
  (let [^Asciidoctor asciidoctor (:asciidoctor renderer)]
    ; The type-hinted array map stands in for {}, which cannot
    ; be hinted and triggers a reflection warning. See CLJ-1929.
    (unwrap-paragraph (.convert asciidoctor s ^java.util.Map (array-map)))))
