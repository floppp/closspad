(ns qoback.closspad.components.match-analysis
  (:require [qoback.closspad.utils.numbers :as numbers]
            ["../../../js/ratingSystem" :as rating]))

(defn couple->str
  [[a b]]
  (str a " & " b))

(defn couples->str
  [a b]
  (str (couple->str a) " vs " (couple->str b)))

(defn- get-team-rating [system players]
  (rating/getTeamRating (clj->js system) (clj->js players)))

(defn- player-variation
  [variations player]
  (let [variation (get variations (keyword player))]
    [:div.bg-gray-50.p-4.rounded-lg.mb-3
     [:h4.text-lg.font-medium.text-gray-700 player]
     [:div.grid.grid-cols-2.gap-2
      [:div "Base Variation:"] [:div (str (:base variation))]
      [:div "Weighted:"] [:div (str (:weighted variation))]
      [:div "Surprise Bonus:"] [:div (str (:surprise variation))]
      [:div "Weak Bonus:"] [:div (str (:weak variation))]
      [:div "Underdog Bonus:"] [:div (str (:underdog variation))]
      [:div.font-bold "Total Change:"] [:div.font-bold (str (:total variation))]]]))

(defn- calculate-variations
  [system couple rating-a rating-b expected-win is-winner importance]
  (js->clj (rating/computeVariationPerPlayer
            (clj->js system)
            (clj->js couple)
            rating-a
            rating-b
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
        variations-a (calculate-variations
                      system
                      couple_a
                      team-a-rating
                      team-b-rating
                      expected-win
                      (= winner "A")
                      importance)
        variations-b (calculate-variations
                      system
                      couple_b
                      team-b-rating
                      team-a-rating
                      (- 1 expected-win)
                      (= winner "B")
                      importance)]
    (.log js/console variations-a)
    [:div
     [:div.grid.grid-cols-2.gap-4.mb-6
      [:div.bg-gray-50.p-4.rounded-lg
       [:h3.text-lg.font-semibold.text-gray-700 "Team A"]
       [:p.text-gray-600 (str "Rating: " team-a-rating)]
       [:ul.list-disc.list-inside.pl-4.space-y-1
        (for [player couple_a]
          [:li.text-gray-700 (str (get-in system [:players (keyword player) :name] player) ": "
                                  (get-in system [:players (keyword player) :points] 50))])]]

      [:div.bg-gray-50.p-4.rounded-lg
       [:h3.text-lg.font-semibold.text-gray-700 "Team B"]
       [:p.text-gray-600 (str "Rating: " team-b-rating)]
       [:ul.list-disc.list-inside.pl-4.space-y-1
        (for [player couple_b]
          [:li.text-gray-700 (str (get-in system [:players (keyword player) :name] player) ": "
                                  (get-in system [:players (keyword player) :points] 50))])]]]

     [:div.bg-blue-50.p-4.rounded-lg.mb-6
      [:p.text-blue-600.flex.justify-between
       [:span "Probability: "]
       [:span (.toFixed expected-win 2)]]
      [:p.text-blue-600.flex.justify-between
       [:span "Winner: "]
       [:span (str " Team " winner)]]]

     [:h4.font-semibold.text-gray-700.mb-3 "Variación"]
     [:div.grid.gap-4
      {:style {:grid-template-columns "1fr 1fr"}}
      (for [player couple_a]
        (player-variation variations-a player))]
     [:div.grid.gap-4
      {:style {:grid-template-columns "1fr 1fr"}}
      (for [player couple_b]
        (player-variation variations-b player))]]))
