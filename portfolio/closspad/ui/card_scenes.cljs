(ns closspad.ui.card-scenes
  (:require [qoback.closspad.ui.elements :as e]
            [portfolio.replicant :refer-macros [defscene]]))

(defscene card-title
  [e/card-title "Add keyboard shortcuts for board navigation"])

(defscene link-item
  [e/link-item
   {:route "foo" :action [:go/to :bar]}
   "Classification"])

(defscene arrow-icons
  [:div.flex.justify-between
   [e/left-double-arrow-icon {:width "w-6"}]
   [e/right-double-arrow-icon {:width "w-6"}]])

(defscene icon-buttons
  (let [date (js/Date.)]
    [:div.flex.justify-between
     [e/button-icon
      {:href (when date (str "#/match/" date))
       :class (when-not date "cursor-not-allowed")}
      [e/left-double-arrow-icon {:width "w-6"}]]
     [e/button-icon
      {:class "cursor-not-allowed"}
      [e/right-double-arrow-icon {:width "w-6"}]]]))


