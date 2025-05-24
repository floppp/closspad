(ns qoback.closspad.pages.match.component
  (:require [clojure.string :as str]))

(defn component [{:keys [couple_a couple_b result]}]
  (let [f (first result)
        s (second result)
        t (get result 2)]
    [:table.table-auto.w-full.border-collapse
     [:thead
      [:tr.border-b.border-gray-200
       {:style {:display "grid" :grid-template-columns "3fr 1fr 1fr 1fr"}}
       [:th.text-left.pb-1 "Team"]
       [:th.text-center.pb-1 (if t "Set 1" "")]
       [:th.text-center.pb-1 (if t "Set 2" "Set 1")]
       [:th.text-center.pb-1 (if t "Set 3" "Set 2")]]]
     [:tbody
      [:tr {:style {:display "grid" :grid-template-columns "3fr 1fr 1fr 1fr"}}
       [:td.py-2 (str/join ", " couple_a)]
       [:td.text-center.py-2 (if t (first f) "")]
       [:td.text-center.py-2 (if t (first s) (first f))]
       [:td.text-center.py-2 (if t (first t) (first s))]]
      [:tr {:style {:display "grid" :grid-template-columns "3fr 1fr 1fr 1fr"}}
       [:td.py-2 (str/join ", " couple_b)]
       [:td.text-center.py-2 (if t (second f) "")]
       [:td.text-center.py-2 (if t (second s) (second f))]
       [:td.text-center.py-2 (if t (second t) (second s))]]]]))
