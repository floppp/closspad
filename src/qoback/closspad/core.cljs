(ns qoback.closspad.core
  (:require [replicant.dom :as r]))

(defn- render! [state]
  (r/render
   (js/document.getElementById "app")
   [:div "foo"]))

(defn ^{:dev/after-load true :export true} start! []
  (render! {}))

(defn ^:export init! []

  (start!))
