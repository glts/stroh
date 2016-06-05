(ns stroh.util.i18n
  "Utilities for internationalisation.

  Note that it is recommended that the VM is started with en_US locale,
  to make sure a fallback resource bundle (ending in _en) is in place."
  (:require [clojure.tools.logging :as log])
  (:import [java.util Locale MissingResourceException ResourceBundle]))

(defn- ^ResourceBundle get-bundle [^String basename ^Locale locale]
  (ResourceBundle/getBundle basename locale))

(defn msg
  "Returns an i18n message for the given message key in locale. The namespace
  of the message key identifies the resource bundle, and the name of the
  message key is the key for the message in the resource bundle."
  (; TODO Perhaps don't use default, use user preference in session instead.
   [message]
   (msg Locale/ENGLISH message))
  ([locale message]
   (let [key (name message)
         bundle (get-bundle (namespace message) locale)]
     (try
       (.getString bundle key)
       (catch MissingResourceException e
         (log/warn (.getMessage e))
         key)))))
