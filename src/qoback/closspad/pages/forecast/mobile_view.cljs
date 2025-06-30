(ns qoback.closspad.pages.forecast.mobile-view
  (:require [clojure.string]
            [qoback.closspad.ui.layout-elements :as lui]
            [qoback.closspad.ui.elements :as ui]
            [qoback.closspad.ui.button-elements :as bui]
            [qoback.closspad.ui.card-elements :as cui]))

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

(defn component
  [ps forecast]
  [:div.flex.flex-col.gap-4
   (ui-players ps forecast)])
