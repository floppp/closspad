(ns qoback.closspad.components.match-analysis
  (:require [qoback.closspad.utils.numbers :as numbers]
            ["../../../js/ratingSystem" :as rating]))

(defn couple->str
  [[a b]]
  (str a " & " b))

(defn couples->str
  [a b]
  (str (couple->str a) " vs " (couple->str b)))



(defn component [log match]
  (let [{:keys [couple_a couple_b result importance]} match]
    (.log js/console log)
    (let [{:keys [expectedWinA
                  playersAudit
                  teamARatingBefore

                  teamBRatingBefore
                  winner]} log]
      [:div
       [:div.flex.bg-gray-50.p-4.rounded-lg.justify-between
        [:div
         [:h3.text-lg.font-semibold.text-gray-700 "Team A"]
         [:p.text-gray-600 (str "Rating: " teamARatingBefore)]]
        [:div
         [:h3.text-lg.font-semibold.text-gray-700.text-right "Team B"]
         [:p.text-gray-600.text-right (str "Rating: " teamBRatingBefore)]]]
       [:div
        [:div.bg-blue-50.p-4.rounded-lg.my-6
         [:p.text-blue-600.flex.justify-between
          [:span "Probability: "]
          [:span expectedWinA]]
         [:p.text-blue-600.flex.justify-between
          [:span "Winner: "]
          [:span (str " Team " winner)]]]]

       [:div.grid.gap-4
        {:style {:grid-template-rows "1fr 1fr"
                 :grid-template-columns "1fr 1fr"}}
        (for [[k v] playersAudit]
          [:div.mb-4
           [:p (name k)]
           (map
            (fn [[k v]]
              [:p.flex.justify-between.gap-8.mb-2
               [:span (name k)]
               [:span v]])
            (:breakdown v))])]
       ])))
