(ns qoback.closspad.pages.forecast.view
  (:require [qoback.closspad.ui.layout-elements :as lui]
            [qoback.closspad.ui.elements :as ui]
            [qoback.closspad.ui.button-elements :as bui]
            [qoback.closspad.ui.card-elements :as cui]))

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
       :class ["bg-blue-100" "shadow-md"
               (when-not selectable? "bg-red-200")]
       :style {:min-height "80px"}}
      (map
       (fn [p]
         [cui/card
          {:element p
           :class ["bg-blue-400" "text-white" "font-bold"
                   (if selectable?
                     "cursor-pointer"
                     "bg-red-200")]}
          [cui/card-details
           [:p.flex.justify-between
            [:span p]
            [bui/icon-button
             {:actions (when selectable?
                         [[:drag :start p]
                          [:drag :drop :selected]])}
             [ui/right-arrow-icon]]]]])
       non-selected)]]))

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
   [:div.flex.gap-4
    (non-selected-players forecast)
    (selected-players forecast)]])
