(ns qoback.closspad.pages.forecast.desktop-view
  (:require [clojure.string]
            [qoback.closspad.ui.layout-elements :as lui]
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

(defn- non-selected-player-card
  [selectable?]
  (fn [p]
    [cui/card
     {:element p
      :class (concat
              ["font-bold" "shadow-md" "transition-all" "duration-300" "ease-in-out"]
              (if selectable?
                ["cursor-pointer" "bg-blue-400" "text-white"]
                ["bg-base-100" "text-gray-400"]))}
     [cui/card-details
      [:p.flex.justify-between
       [:span p]
       [bui/icon-button
        {:actions (when selectable?
                    [[:drag :start p]
                     [:drag :drop :selected]])}
        [ui/right-arrow-icon]]]]]))

(defn selected-players
  [all-players {:players/keys [selected]}]
  (let [ss (set selected)]
    [lui/column
     {:class ["flex" "flex-col" "gap-2" "rounded-lg" "w-full"]}
     [lui/column-body
      {:on {:drop (when (> 4 (count selected)) [[:drag :drop :selected]])}
       :class ["bg-blue-400"  "shadow-md"]
       :style {:min-height "80px"}}
      (map
       (selected-player-card "bg-blue-100")
       (filter #(contains? ss %) all-players))]]))

(defn non-selected-players
  [all-players {:players/keys [selected]}]
  (let [selectable? (< (count selected) 4)
        ss (set selected)]
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
       (filter #(not (contains? ss %)) all-players))]]))

(defn component
  [ps forecast]
  [:div.flex.gap-4
   (non-selected-players ps forecast)
   (selected-players ps forecast)])
