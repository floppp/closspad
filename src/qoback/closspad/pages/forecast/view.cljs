(ns qoback.closspad.pages.forecast.view
  (:require [qoback.closspad.ui.layout-elements :as ui]
            [qoback.closspad.ui.card-elements :as cui]))

(defn selected-players
  [{:players/keys [selected]}]
  [ui/column
   {:class ["flex" "flex-col" "gap-2" "rounded-lg" "w-full"]}
   [ui/column-body
    {:on {:drop [[:drag :drop :selected]]}
     :class ["bg-green-100"  "shadow-md"]
     :style {:min-height "80px"}}
    (map
     (fn [p]
       [cui/card
        {:element p
         :class ["bg-green-400" "text-white" "font-bold"]}
        [cui/card-details p]])
     selected)]])

(defn non-selected-players
  [{:players/keys [non-selected]}]
  [ui/column
   {:class ["flex" "flex-col" "gap-2" "bg-white" "rounded-lg" "shadow-md" "w-full"]}
   [ui/column-body
    {:on {:drop [[:drag :drop :non-selected]]}
     :class ["bg-blue-100"]
     :style {:min-height "80px"}}
    (map
     (fn [p]
       [cui/card
        {:element p
         :class ["bg-blue-400" "text-white" "font-bold"]}
        [cui/card-details p]])
     non-selected)]])

(defn view
  [{:keys [forecast]}]
  [:div.bg-white.rounded-lg.shadow-md.p-6.max-w-4xl.mx-auto.w-full.pb-10
   [:h1.text-3xl.font-bold.text-gray-800.mb-6 "Simulador Partida"]
   [:div.flex.gap-4
    (non-selected-players forecast)
    (selected-players forecast)]])
