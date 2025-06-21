(ns closspad.ui.card-scenes
  (:require [qoback.closspad.ui.elements :as e]
            [portfolio.replicant :refer-macros [defscene]]))

(defscene card-title
  [e/card-title "Add keyboard shortcuts for board navigation"])

(defscene link-item
  [e/link-item
   {:route "foo" :action [:go/to :bar]}
   "Classification"]
  )

(defscene arrow-icons
  [e/right-arrow-icon {:width "w-6"}])
