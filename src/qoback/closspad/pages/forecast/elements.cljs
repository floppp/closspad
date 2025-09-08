(ns qoback.closspad.pages.forecast.elements
  (:require [clojure.string :as str]
            [qoback.closspad.state.db :refer [get-dispatcher]]
            [qoback.closspad.rating.system :as s]
            [qoback.closspad.ui.layout-elements :as lui]
            [qoback.closspad.widgets.select :refer [select default-select-style]]
            [qoback.closspad.components.match.ui :as mui]
            [qoback.closspad.components.match.domain :as m]
            [qoback.closspad.pages.forecast.services :as fs]))

(defn ui-couple-ratings
  [ratings]
  [:div.bg-blue-50.p-4
   (map
    (fn [[k v]]
      [:p.flex.justify-between
       [:span (name k)]
       [:span (.toFixed v 2)]])
    ratings)])

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
      [:p.flex.justify-between
       [:span "Rivales"]
       [:span
        {:class ["px-2"
                 (if (= winner team-letter)
                   "bg-red-200"
                   "bg-green-200")]
         :title (if (= winner team-letter)
                  "Red, the analyzed couple won"
                  "Green, the oponents won")}
        (fs/couple->str rivals)]]
      ;; Extraigo arriba el `if` branch, porque el `else` creo que no se gasta nunca.
      #_(if (seq couple)
          [:p.flex.justify-between
           [:span "Rivales"]
           [:span
            {:class ["px-2"
                     (if (= winner team-letter)
                       "bg-red-200"
                       "bg-green-200")]
             :title (if (= winner team-letter)
                      "Red, the analyzed couple won"
                      "Green, the oponents won")}
            (fs/couple->str rivals)]]
          [:p.flex.justify-between
           [:span "Ganadores"]
           [:span  (if (= winner "A")
                     (fs/couple->str couple_a)
                     (fs/couple->str couple_b))]])
      [:p.flex.justify-between
       [:span "Resultado"]
       [:span (->> result
                   (map #(str (first %) "-" (second %)))
                   (clojure.string/join " / "))]]
      [:p.flex.justify-between
       [:span "Fecha"]
       [:span (date-fn played_at)]]])))

(defn ui-matches
  [matches ca cb is-mobile?]
  (let [prev-matches (fs/same-match? [ca cb] matches)
        prev-matches-id (set (map :id prev-matches))
        rest-matches (filter #(not (contains? prev-matches-id (:id %)))
                             matches)
        ca-matches (fs/couple-matches? ca rest-matches)
        cb-matches (fs/couple-matches? cb rest-matches)
        date-fn (fn [d] (.toLocaleString (js/Date. d)))]
    [:div
     (when (seq prev-matches)
       [:div
        [:h2.p-4.text-bold.text-lg.mt-4 "Histórico Enfrentamiento Parejas"]
        [:hr.mx-4.mb-4
         {:style {:border "1px solid gray"}}]
        [:div.px-4.flex.flex-col.gap-4
         (map #(ui-match % date-fn) prev-matches)]])

     (when (or (seq ca-matches) (seq cb-matches))
       [:div
        [:h2.p-4.text-bold.text-lg.mt-4 "Histórico por Pareja"]
        [:hr.mx-4.mb-4
         {:style {:border "1px solid gray"}}]
        [:div
         {:class (if is-mobile?
                   ["grid" "grid-cols-2"]
                   ["flex" "flex-col" "gap-8"])}
         [:div.px-4.flex.flex-col.gap-4
          [:h2.text-bold
           {:class (if is-mobile? ["text-right"] ["text-left"])}
           (str (first ca) " &  " (second ca))]
          (map #(ui-match % date-fn ca) ca-matches)]
         [:div.px-4.flex.flex-col.gap-4
          [:h2.text-bold
           {:class (if is-mobile? ["text-right"] ["text-left"])}
           (str (first cb) " &  " (second cb))]
          (map #(ui-match % date-fn cb) cb-matches)]]])]))

(defn- match-simulation
  [state is-mobile? [a b] [c d]]
  [lui/accordion-item
   [lui/accordion-item-title
    {:class ["grid" "grid-cols-3" "items-center"]}
    [:span (str a " &  " b)]
    [:span.text-center "vs"]
    [:span.text-right (str c " &  " d)]]
   [lui/accordion-item-body
    [:div
     (ui-probability state (fn [n] (.toFixed n 2)) [[a b] [c d]])
     (ui-matches (-> state :match :results)
                 [a b]
                 [c d]
                 is-mobile?)]]])

(defn forecast-match-simulation
  [forecast combinations]
  (let [dispatcher (get-dispatcher)
        importances-str (->> m/importances keys (map (comp str/capitalize name)))]
    [:div.flex.flex-col.gap-1.mt-4
     [:span [:strong "Winner Simulation"]]

     [select
      {:classes (concat default-select-style
                        ["bg-white" "border-1" "border-gray-300" "border-solid"])
       :selection (:importance forecast)
       :actions [[:event/prevent-default]
                 [:db/assoc-in [:forecast :importance] :event/target.value]]
       :replicant/key :forecast-importance
       :replicant/on-mount
       (fn []
         (dispatcher
          nil
          [[:db/assoc-in [:forecast :importance] (first importances-str)]]))}
      importances-str]

     [select
      {:classes (concat
                 default-select-style
                 ["bg-white" "border-1" "border-gray-300" "border-solid"])
       :selection (:winners forecast)
       :actions [[:event/prevent-default]
                 ;; [:db/assoc-in [:forecast :winners] :event/target.value]
                 [:forecast/winners :event/target.value (first combinations)]]
       :render-fn (fn [c] (str (first c) " & " (second c)))
       :replicant/key :forecast-winner
       :replicant/on-mount
       (fn []
         (dispatcher
          nil
          ;; [[:db/assoc-in [:forecast :winner] (-> combinations first first)]]
          [[:forecast/winners (-> combinations first first) combinations]]))}
      (apply concat combinations)]]))

(defn analysis
  [{:keys [forecast] :as state} is-mobile?]
  (let [ps (:players/selected forecast)
        cs (for [i (range (count ps))
                 j (range (inc i) (count ps))]
             [i j])
        matches [[(nth cs 0) (nth cs 5)]
                 [(nth cs 1) (nth cs 4)]
                 [(nth cs 2) (nth cs 3)]]
        combinations (map (fn [[[a b] [c d]]]
                            [[(nth ps a) (nth ps b)]
                             [(nth ps c) (nth ps d)]])
                          matches)]
    [lui/column
     (map
      (fn [[ca cb]] (match-simulation state is-mobile? ca cb))
      combinations)

     (forecast-match-simulation forecast combinations)]))
