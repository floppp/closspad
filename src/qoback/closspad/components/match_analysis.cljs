(ns qoback.closspad.components.match-analysis
  (:require [qoback.closspad.utils.numbers :as numbers])
  (:require ["../../../js/ratingSystem" :as rating]))

(defn- get-team-rating [system players]
  (rating/getTeamRating (clj->js system) (clj->js players)))

(defn- calculate-variations [system team-a team-b expected-win is-winner importance]
  (js->clj (rating/computeVariationPerPlayer
            (clj->js system)
            (clj->js team-a)
            (clj->js team-b)
            expected-win
            is-winner
            importance)
           :keywordize-keys true))

(defn component [system match]
  (let [{:keys [couple_a couple_b result importance]} match
        winner (rating/determineWinner (clj->js result))
        team-a-rating (get-team-rating system couple_a)
        team-b-rating (get-team-rating system couple_b)
        expected-win (rating/calculateExpectedWin
                      (clj->js system) team-a-rating team-b-rating)
        importance (or importance (rating/computeImportance (clj->js system) (clj->js match)))
        variations (calculate-variations system couple_a couple_b expected-win (= winner "A") importance)]

    [:div.bg-white.rounded-lg.shadow-md.p-6.max-w-4xl.mx-auto.w-full
     [:h2.text-2xl.font-bold.text-gray-800.mb-4 "Match Analysis"]

     [:div.grid.grid-cols-2.gap-4.mb-6
      [:div.bg-gray-50.p-4.rounded-lg
       [:h3.text-lg.font-semibold.text-gray-700 "Team A"]
       [:p.text-gray-600 (str "Rating: " team-a-rating)]
       [:ul.list-disc.list-inside.pl-4.space-y-1
        (for [player couple_a]
          [:li.text-gray-700 (str (get-in system [:players player :name] player) ": "
                                  (get-in system [:players player :points] 50))])]]

      [:div.bg-gray-50.p-4.rounded-lg
       [:h3.text-lg.font-semibold.text-gray-700 "Team B"]
       [:p.text-gray-600 (str "Rating: " team-b-rating)]
       [:ul.list-disc.list-inside.pl-4.space-y-1
        (for [player couple_b]
          [:li.text-gray-700 (str (get-in system [:players player :name] player) ": "
                                  (get-in system [:players player :points] 50))])]]]

     [:div.bg-blue-50.p-4.rounded-lg.mb-6
      [:h3.text-lg.font-semibold.text-blue-700 "Match Outcome"]
      [:p.text-blue-600 (str "Probability: " expected-win)]
      [:p.text-blue-600 (str "Winner: Team " winner)]]

     [:h3.text-xl.font-semibold.text-gray-700.mb-3 "Point Variations"]
     (for [player couple_a]
       (let [variation (get variations (keyword player))]
         [:div.bg-gray-50.p-4.rounded-lg.mb-3
          [:h4.text-lg.font-medium.text-gray-700 (get-in system [:players player :name] player)]
          [:div.grid.grid-cols-2.gap-2
           [:div "Base Variation:"] [:div (str (:base variation))]
           [:div "Weighted:"] [:div (str (:weighted variation))]
           [:div "Surprise Bonus:"] [:div (str (:surprise variation))]
           [:div "Weak Bonus:"] [:div (str (:weak variation))]
           [:div "Underdog Bonus:"] [:div (str (:underdog variation))]
           [:div.font-bold "Total Change:"] [:div.font-bold (str (:total variation))]]]))]))
