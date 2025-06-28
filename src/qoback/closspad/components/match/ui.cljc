(ns qoback.closspad.components.match.ui
  (:require [replicant.alias :refer [defalias]]))

(defalias match-probability
  [attrs body]
  [:div
   [:div.bg-blue-50.p-4
    [:p.text-blue-600.flex.justify-between
     [:span "Probability: "]
     [:span (:expected-win-a attrs)]]
    [:p.text-blue-600.flex.justify-between
     [:span "Winner: "]
     [:span (str " Team " (:winner attrs))]]]])
