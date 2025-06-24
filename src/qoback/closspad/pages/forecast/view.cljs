(ns qoback.closspad.pages.forecast.view
  (:require [qoback.closspad.pages.add.elements :as ui]))

(defn view
  [state]
  [:div
   [:h3 "forecast"]
   [:div.flex.gap-4
    [ui/column
     {:class ["flex" "flex-col" "gap-2" "bg-white" "rounded-lg" "shadow-md" "w-full"]}
     [ui/column-body
      {:on {:drop [[:actions/prevent-default]]}}
      (map (fn [p]
             [:p.bg-red p])
           (-> state :stats :players))]]
    [ui/column
     {:class ["flex" "flex-col" "gap-2" "bg-white" "rounded-lg" "shadow-md" "w-full"]}
     [ui/column-body
      {:on {:drop [[:actions/prevent-default]]}}
      ]]]])
