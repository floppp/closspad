(ns qoback.closspad.components.match-analysis)

(defn couple->str
  [[a b]]
  (str a " & " b))

(defn couples->str
  [a b]
  (str (couple->str a) " vs " (couple->str b)))

(defn probability
  [expected-win-a winner]
  [:div
   [:div.bg-blue-50.p-4.my-6
    [:p.text-blue-600.flex.justify-between
     [:span "Probability: "]
     [:span expected-win-a]]
    [:p.text-blue-600.flex.justify-between
     [:span "Winner: "]
     [:span (str " Team " winner)]]]])

(defn- teams-rating
  [rating-a rating-b]
  [:div.flex.flex-col.bg-gray-50.g-8.p-4
   [:div.flex.gap-4.items-end.justify-between
    [:h3.text-lg.font-semibold.text-gray-700 "Team A"]
    [:p.text-gray-600 (str "Rating: " rating-a)]]
   [:div.flex.gap-4.items-end.justify-between
    [:h3.text-lg.font-semibold.text-gray-700.text-right "Team B"]
    [:p.text-gray-600.text-right (str "Rating: " rating-b)]]])

(def audit-fields
  {:baseDelta "Valor Base"
   :surpriseBonus "Bonus Sorpresa"
   :weakBonus "Bonus por Paquete"
   :underdogBonus "Bonus por underdog"
   :totalChange "Cambio"
   :playerWeight "Importancia Jugador"
   :originalPlayerPoints "Puntos Anteriores"})

(defn- couple-points
  [couple log is-winner]
  [:div.flex.flex-col.gap-2.p-4
   {:class (when is-winner "bg-green-100")}
   [:div.grid
    {:style {:grid-template-columns "2fr 1fr 1fr"}}
    [:span ""]
    (for [n couple]
      [:span.text-right.bold n])]
   (map
    (fn [[k v]]
      [:p.grid
       {:style {:grid-template-columns "2fr 1fr 1fr"}}
       [:span v]
       (for [n couple]
         (let [data (-> n keyword log :breakdown)]
           [:span.text-right (-> k keyword data)]))])
    audit-fields)])

(defn component [log match]
  (let [{:keys [couple_a couple_b]} match]
    (let [{:keys [expectedWinA
                  playersAudit
                  teamARatingBefore
                  teamBRatingBefore
                  winner]} log]
      [:div
       (teams-rating teamARatingBefore teamBRatingBefore)
       (probability expectedWinA winner)
       [:div.flex.flex-col.flex-gap-8
        (couple-points couple_a playersAudit (= winner "A"))
        (couple-points couple_b playersAudit (= winner "B"))]])))
