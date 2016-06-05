(ns stroh.render-test
  (:require [clojure.test :refer :all]
            [com.stuartsierra.component :as component]
            [stroh.render :as render]))

(def renderer
  (component/start (render/renderer {})))

(deftest render-simple-asciidoc
  (is (= "make <strong>me</strong> bold"
         (render/asciidoc renderer "make *me* bold"))))

(deftest escape-html-tags
  (is (= "&lt;script&gt;alert(\"pwn\");&lt;/script&gt;"
         (render/asciidoc renderer "<script>alert(\"pwn\");</script>"))))
