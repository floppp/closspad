(ns qoback.closspad.pages.forecast.services
  (:require [clojure.string]
            [qoback.closspad.rating.system :as s]
            [qoback.closspad.components.match.ui :as mui]
            [qoback.closspad.components.match.services :as m]))

(defn couple-matches?
  [couple matches]
  (->> matches
       (map m/match-with-couples-as-set)
       (filter #(m/couple-in-match? couple %))))

(defn same-match?
  [couples matches]
  (->> matches
       (map m/match-with-couples-as-set)
       (filter #(m/same-match? couples %))))

(defn get-couple-matches
  [couple state]
  (let [matches (-> state :match :results)]
    (couple-matches? couple matches)))

(defn ui-couple-ratings
  [ratings]
  [:div.bg-blue-50.p-4
   (map
    (fn [[k v]]
      [:p.flex.justify-between
       [:span (name k)]
       [:span v]])
    ratings)])

(defn couple->str
  [couple]
  (str "r: " (first couple) ", " "d: " (second couple)))

(defn ui-probability
  [state number-fn [ca cb]]
  (let [ca (map keyword ca)
        cb (map keyword cb)
        system (-> state :system :history first)
        ca-ratings (s/get-ratings system ca)
        cb-ratings (s/get-ratings system cb)
        ca-rating (s/get-team-rating system ca)
        cb-rating (s/get-team-rating system cb)
        expected-a-win (s/expected-a-win system ca-rating cb-rating)
        winner (if (> expected-a-win 0.5) "A" "B")]
    [:div.flex.flex-col.gap-4
     [mui/match-probability
      {:expected-win-a (number-fn expected-a-win)
       :winner winner}]
     [:div.grid.grid-cols-2.gap-4
       (ui-couple-ratings ca-ratings)
       (ui-couple-ratings cb-ratings)]]))

(defn ui-match
  ([match date-fn] (ui-match match date-fn nil))
  ([match date-fn couple]
   (let [{:keys [couple_a couple_b played_at result]} match
         winner (s/determine-winner result)
         team-letter (if (= (set couple_a) (set couple)) "A" "B")
         rivals (if (= (set couple_a) (set couple))
                  couple_b couple_a)]
     [:div
      (if (seq couple)
        [:p.flex.justify-between
         [:span "Rivales"]
         [:span
          {:class ["px-2"
                   (if (= winner team-letter)
                     "bg-red-200"
                     "bg-green-200")]}
          (couple->str rivals)]]
        [:p.flex.justify-between
         [:span "Ganadores"]
         [:span  (if (= winner "A")
                   (couple->str couple_a)
                   (couple->str couple_b))]])
      [:p.flex.justify-between
       [:span "Resultado"]
       [:span (->> result
                   (map #(str (first %) "-" (second %)))
                   (clojure.string/join " / "))]]
      [:p.flex.justify-between
       [:span "Fecha"]
       [:span (date-fn played_at)]]])))
