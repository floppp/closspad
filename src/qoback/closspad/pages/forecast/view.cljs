(ns qoback.closspad.pages.forecast.view
  (:require [qoback.closspad.ui.layout-elements :as lui]
            [qoback.closspad.ui.elements :as ui]
            [qoback.closspad.ui.button-elements :as bui]
            [qoback.closspad.ui.card-elements :as cui]
            [qoback.closspad.rating.system :as s]
            ;; [qoback.closspad.components.match-analysis :as ma]
            [qoback.closspad.components.match.ui :as mui]
            [qoback.closspad.pages.forecast.services :as fs]))

(defn- selected-player-card
  [color]
  (fn [p]
    [cui/card
     {:element p
      :class [color "cursor-pointer"]}
     [cui/card-details
      [:p.flex.justify-between
       [bui/icon-button
        {:actions [[:drag :start p]
                   [:drag :drop :not-selected]]}
        [ui/left-arrow-icon]]
       [:span p]]]]))

(defn- non-selected-player-card
  [selectable?]
  (fn [p]
    [cui/card
     {:element p
      :class (concat
              ["font-bold" "shadow-md" "transition-all" "duration-300" "ease-in-out"]
              (if selectable?
                ["cursor-pointer" "bg-blue-400" "text-white"]
                ["bg-base-100" "text-gray-400" ]))}
     [cui/card-details
      [:p.flex.justify-between
       [:span p]
       [bui/icon-button
        {:actions (when selectable?
                    [[:drag :start p]
                     [:drag :drop :selected]])}
        [ui/right-arrow-icon]]]]]))

(defn selected-players
  [{:players/keys [selected]}]
  [lui/column
   {:class ["flex" "flex-col" "gap-2" "rounded-lg" "w-full"]}
   [lui/column-body
    {:on {:drop (when (> 4 (count selected)) [[:drag :drop :selected]])}
     :class ["bg-blue-400"  "shadow-md"]
     :style {:min-height "80px"}}
    (map
     (selected-player-card "bg-blue-100")
     selected)]])

(defn non-selected-players
  [{:players/keys [non-selected selected]}]
  (let [selectable? (< (count selected) 4)]
    [lui/column
     {:class ["flex" "flex-col" "gap-2" "rounded-lg" "w-full"]}
     [lui/column-body
      {:on {:drop [[:drag :drop :non-selected]]}
       :class ["shadow-md"
               "transition-all"
               "duration-300"
               "ease-in-out"
               (when-not selectable? ["bg-base-100"])]
       :style {:min-height "80px"}}
      (map
       (non-selected-player-card selectable?)
       non-selected)]]))

(defn probability
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
    [:div.flex.flex-col
     [:div
      [:div.grid.grid-cols-2.gap-4
       [:div.bg-blue-50.p-4
        (map
         (fn [[k v]]
           [:p.flex.justify-between
            [:span (name k)]
            [:span v]])
         ca-ratings)]
       [:div.bg-blue-50.p-4

        (map
         (fn [[k v]]
           [:p.flex.justify-between
            [:span (name k)]
            [:span v]])
         cb-ratings)]]]
     [mui/match-probability
      {:expected-win-a (.toFixed expected-a-win 2)
       :winner winner}]]))

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
            (probability state [[a b] [c d]])
]]]))]))

(defn view
  [{:keys [forecast] :as state}]
  [:div.bg-white.rounded-lg.shadow-md.p-6.max-w-4xl.mx-auto.w-full.pb-10
   [:h1.text-3xl.font-bold.text-gray-800.mb-6 "Simulador Partida"]
   [:button.btn.w-full.mb-4.rounded-lg.text-white.font-bold.shadow-md.bg-blue-400
    {:on {:click [[:event/prevent-default]
                  [:db/dissoc :forecast]
                  [:db/assoc-in
                   [:forecast :players/non-selected]
                   (-> state :stats :players)]]}}
    "Limpiar"]
   [:div.flex.flex-col.gap-4
    [:div.flex.gap-4
     (non-selected-players forecast)
     (selected-players forecast)]
    (when (= 4 (-> state :forecast :players/selected count))
      (analysis state))]])
