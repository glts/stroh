(ns stroh.ui
  "Web UI rendering."
  (:require [net.cgrand.enlive-html :as html :refer [defsnippet deftemplate]]
            [net.cgrand.reload :as reload]
            [stroh.render :as render]
            [stroh.tasks :as tasks]
            [stroh.util.i18n :refer [msg]]))

; auto-reload resources
(reload/auto-reload *ns*)

(defn- cp [codepoint]
  (String/valueOf (Character/toChars codepoint)))

(def ^:private emoji
  {:task.type/book (cp 0x1F4DA)
   :task.type/film (cp 0x1F4FD)
   :task.type/video (cp 0x1F4FA)
   :task.type/event (cp 0x1F39F)
   :task.type/article (cp 0x1F4D1)
   :task.type/audio (cp 0x1F4FB)})

(defsnippet task-item "templates/activities.html" [:#open :> :p]
  [task]
  [:p] (html/content (emoji (:task/type task) "NONE")
                     " "
                     (html/html [:a
                                 {:href (str "/task/" (:task/id task))}
                                 (:task/title task)])))

(defn substitute-tasks-list [tasks]
  (if (seq tasks)
    (html/substitute
      (html/transform (html/html [:ol [:li]])
        [:li] (html/clone-for [t tasks]
                (html/content (task-item t)))))
    identity))

(defsnippet create-task-form "templates/activities.html" [:#create]
  []
  [:#create] identity)

(defsnippet activities "templates/activities.html" [:#activities]
  [open in-progress done]
  [:#open :> :p] (substitute-tasks-list open)
  [:#in-progress :> :p] (substitute-tasks-list in-progress)
  [:#done :> :p] (substitute-tasks-list done))

(defsnippet task-title "templates/task.html" [:#description]
  [task]
  [[:p html/first-of-type]] (html/content (:task/title task)))

(defsnippet history "templates/task.html" [:#history]
  [history]
  [:#creation :> html/first-child] (html/after
                                     (html/html [:p (str (:created-at history))]))
  [:#status [:li html/first-child]] (html/clone-for [h (:status-history history)]
                                      (html/content (str h))))

(defn task-description [task]
  (concat (task-title task)
          (history (:history task))))

(deftemplate base-view "templates/application.html"
  [title body]
  [:head :> :title] (html/content title)
  [:header :> :h1] (html/html-content title)
  [:#body] (html/content body))

(defn not-found-view
  "Renders a 404 view."
  [app reason]
  (base-view reason reason))

(defn tasks-index
  "Renders the tasks index view."
  [app]
  (let [{:keys [open in-progress done]} (tasks/find-all-tasks app)]
    (base-view (msg ::tasks)
               (concat
                 (create-task-form)
                 (activities open in-progress done)))))

(defn task-detail
  "Renders a detail view of a single task."
  [app id]
  (if-let [task (tasks/find-task-by-id app id)]
    (base-view (render/asciidoc (:renderer app) (:task/title task))
               (task-description task))
    (not-found-view app (str "No task with id " id))))
