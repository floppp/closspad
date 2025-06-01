(ns qoback.closspad.pages.stats.view
  (:require [qoback.closspad.components.stats.echarts :as ech]))

(defn view
  [{:keys [stats] :as state}]
  (let [player (get-in state [:page/navigated :player])]
    [:div.mb-4
     {:style {:min-height "400px" :min-width "400px"}
      :replicant/on-mount
      (fn [{:replicant/keys [node remember]}]
        (.log js/console  "on mount map")
        (remember (ech/mount-stats node)))
      :replicant/on-update
      (fn [data]
        (ech/update-chart
         (:replicant/memory data)
         (clj->js ech/mock)))}]))
