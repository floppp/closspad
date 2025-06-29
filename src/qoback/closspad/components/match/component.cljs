(ns qoback.closspad.components.match.component
  (:require [clojure.string :as str]
            [qoback.closspad.components.widgets :as w]
            [qoback.closspad.utils.datetime :as dt]))

(defn determine-winner [result]
  (let [sets-won-by-a (count (filter (fn [[a b]] (> a b)) result))
        sets-won-by-b (count (filter (fn [[a b]] (< a b)) result))]
    (if (> sets-won-by-a sets-won-by-b) 0 1)))


(defn- component [{:keys [couple_a couple_b result] :as match} ]
  (let [f (first result)
        s (second result)
        t (get result 2)
        winner (determine-winner result)]
    [:table.table-auto.w-full.border-collapse
     {:on {:click [[:event/prevent-default]
                   [:ui/dialog :match-info match]]}}
     [:thead
      [:tr.border-b.border-gray-200
       {:style {:grid-template-columns "3fr 1fr 1fr 1fr"}
        :class ["grid" "pl-2"]}
       [:th.text-left.pb-1 "Team"]
       [:th.text-center.pb-1 (if t "Set 1" " ")]
       [:th.text-center.pb-1 (if t "Set 2" (if s "Set 1" " "))]
       [:th.text-center.pb-1 (if t "Set 3" (if s "Set 2" "Set 1"))]]]
     [:tbody
      [:tr {:style {:grid-template-columns "3fr 1fr 1fr 1fr"}
            :class ["grid" "pl-2" (when (= 0 winner) "bg-blue-100")]}
       [:td.py-2 (str/join ", " couple_a)]
       [:td.text-center.py-2 (if t (first f) "")]
       [:td.text-center.py-2 (if t (first s) (if s (first f) ""))]
       [:td.text-center.py-2 (if t (first t) (if s (first s) (first f)))]]
      [:tr {:style {:grid-template-columns "3fr 1fr 1fr 1fr"}
            :class ["grid" "pl-2" (when (= 1 winner) "bg-blue-100")]}
       [:td.py-2 (str/join ", " couple_b)]
       [:td.text-center.py-2 (if t (second f) "")]
       [:td.text-center.py-2 (if t (second s) (if s (second f) ""))]
       [:td.text-center.py-2 (if t (second t) (if s (second s) (second f)))]]]]))


(defn day-matches
  [state]
  (let [match-date (:date (:page/navigated state))
        match-date-str (dt/datetime->date->str match-date)
        all-matches (:results (:match state))
        day-matches (filter
                     #(= match-date-str
                         (-> %
                             :played_at
                             js/Date.
                             dt/datetime->date->str))
                     all-matches)]
    [:div.bg-white.rounded-t-lg.shadow-md.p-8
     [:div.mb-6.flex.justify-center
      (w/arrow-selector match-date (->> all-matches
                                        (map (comp #(js/Date. %) :played_at))
                                        sort))]
     [:div.space-y-4
      (for [match day-matches]
        [:div.border.border-gray-200.rounded-lg.p-4.shadow-sm
         (component match)])]]))

