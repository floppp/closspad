(ns qoback.closspad.pages.forecast.view
  (:require [qoback.closspad.ui.layout-elements :as ui]
            [qoback.closspad.ui.card-elements :as cui]))

(defn selected-players
  [{:players/keys [selected]}]
  [ui/column
   {:class ["flex" "flex-col" "gap-2" "bg-white" "rounded-lg" "shadow-md" "w-full"]}
   [ui/column-body
    {:on {:drop [[:event/prevent-default]
                 [:drag :drop :selected]]}}
    (map
     (fn [p]
       [cui/card
        {:element p}
        [cui/card-details p]])
     selected)]])

(defn non-selected-players
  [{:players/keys [non-selected]}]
  [ui/column
   {:class ["flex" "flex-col" "gap-2" "bg-white" "rounded-lg" "shadow-md" "w-full"]}
   [ui/column-body
    {:on {:drop (fn [_]
                  [[:event/prevent-default]
                    [:drag :drop :non-selected]])
          }}
    (map
     (fn [p]
       [cui/card
        {:element p}
        [cui/card-details p]])
     non-selected)]])

(defn view
  [{:keys [forecast]}]
  [:div
   [:h3 "forecast"]
   [:div.flex.gap-4
    (non-selected-players forecast)
    (selected-players forecast)]])

