(ns qoback.closspad.pages.match.view
  (:require [qoback.closspad.components.match.component :as match]
            [qoback.closspad.components.classification.component :as classification]
            [qoback.closspad.ui.elements :as ui]))

(defn view
  [state]
  (let [is-loading? (-> state :is-loading?)]
    (if is-loading?
      [ui/spinner]
      [:div
       (match/day-matches state)
       (classification/component state)])))
