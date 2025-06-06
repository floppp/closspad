(ns qoback.closspad.pages.match.view
  (:require [qoback.closspad.components.widgets :as w]
            [qoback.closspad.utils.datetime :as dt]
            [qoback.closspad.pages.match.component :as match]))

(defn view
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
         (match/component match)])]]))
