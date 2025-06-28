(ns qoback.closspad.pages.forecast.mobile-view
  (:require [clojure.string]
            [qoback.closspad.ui.layout-elements :as lui]
            [qoback.closspad.ui.elements :as ui]
            [qoback.closspad.ui.button-elements :as bui]
            [qoback.closspad.ui.card-elements :as cui]
            [qoback.closspad.rating.system :as s]
            [qoback.closspad.components.match.ui :as mui]
            [qoback.closspad.pages.forecast.services :as fs]))

(defn- player-card
  [selectable? p is-selected?]
  [cui/card
   {:element p
    :class (if is-selected? "bg-green-200" "bg-blue-200")}
   [cui/card-details
    [:p.flex.justify-between
     [:span p]
     [bui/icon-button
      {:actions (if is-selected?
                  [[:drag :start p]
                   [:drag :drop :not-selected]]
                  (when selectable?
                    [[:drag :start p]
                     [:drag :drop :selected]]))}
      (if is-selected?
        [ui/minus-icon]
        (when selectable?
          [ui/plus-icon]))]]]])

(defn ui-players
  [all-players {:players/keys [selected]}]
  (let [ss (set selected)
        selectable? (< (count selected) 4)]
    [lui/column
     {:class ["flex" "flex-col" "gap-2" "rounded-lg" "w-full"]}
     [lui/column-body
      {:on {:drop (when (> 4 (count selected)) [[:drag :drop :selected]])}
       :class ["bg-blue-400"  "shadow-md"]
       :style {:min-height "80px"}}
      (map
       #(player-card selectable? % (contains? ss %))
       all-players)]]))

(defn- couple-ratings
  [ratings]
  [:div.bg-blue-50.p-4
   (map
    (fn [[k v]]
      [:p.flex.justify-between
       [:span (name k)]
       [:span v]])
    ratings)])

(defn ui-probability
  [state [ca cb]]
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
      {:expected-win-a (.toFixed expected-a-win 2)
       :winner winner}]
     [:div.grid.grid-rows-2.gap-4
      (couple-ratings ca-ratings)
      (couple-ratings cb-ratings)]]))

(defn couple->str
  [couple]
  (str "r: " (first couple) ", " "d: " (second couple)))

(defn- ui-match
  ([match] (ui-match match nil))
  ([match couple]
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
       [:span (.toLocaleString (js/Date. played_at))]]])))

(defn ui-matches
  [matches ca cb]
  (let [prev-matches (fs/same-match? [ca cb] matches)
        prev-matches-id (set (map :id prev-matches))
        rest-matches (filter #(not (contains? prev-matches-id (:id %)))
                             matches)
        ca-matches (fs/couple-matches? ca rest-matches)
        cb-matches (fs/couple-matches? cb rest-matches)]
    [:div
     (when (seq prev-matches)
       [:div
        [:h2.p-4.text-bold.text-lg.mt-4 "Histórico Enfrentamiento Parejas"]
        [:hr.mx-4.mb-4
         {:style {:border "1px solid gray"}}]
        [:div.px-4.flex.flex-col.gap-4
         (map ui-match prev-matches)]])

     (when (or (seq ca-matches) (seq cb-matches))
       [:div
        [:h2.p-4.text-bold.text-lg.mt-4 "Histórico por Pareja"]
        [:hr.mx-4.mb-4
         {:style {:border "1px solid gray"}}]
        [:div.flex.flex-col.gap-8
         [:div.px-4.flex.flex-col.gap-4
          [:h2.text-right.text-bold (str (first ca) " &  " (second ca))]
          (map #(ui-match % ca) ca-matches)]
         [:div.px-4.flex.flex-col.gap-4
          [:h2.text-right.text-bold (str (first cb) " &  " (second cb))]
          (map #(ui-match % cb) cb-matches)]]])]))

(defn- analysis
  [{:keys [forecast] :as state}]
  (let [ps (:players/selected forecast)
        cs (for [i (range (count ps))
                 j (range (inc i) (count ps))]
             [i j])
        matches [[(nth cs 0) (nth cs 5)]
                 [(nth cs 1) (nth cs 4)]
                 [(nth cs 2) (nth cs 3)]]]
    [lui/column
     (for [[[a b] [c d]] matches]
       (let [a (nth ps a)
             b (nth ps b)
             c (nth ps c)
             d (nth ps d)]
         [lui/accordion-item
          [lui/accordion-item-title
           {:class ["grid" "grid-cols-3" "items-center"]}
           [:span (str a " &  " b)]
           [:span.text-center "vs"]
           [:span.text-right (str c " &  " d)]]
          [lui/accordion-item-body
           [:div
            (ui-probability state [[a b] [c d]])
            (ui-matches (-> state :match :results)
                        [a b]
                        [c d])]]]))]))

(defn component
  [{:keys [forecast] :as state}]
  [:div.flex.flex-col.gap-4
   [:div.flex.flex-col.gap-4
    (ui-players (-> state :stats :players) forecast)]
   (when (= 4 (-> state :forecast :players/selected count))
     (analysis state))])
