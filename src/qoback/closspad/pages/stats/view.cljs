(ns qoback.closspad.pages.stats.view
  (:require [qoback.closspad.components.stats.echarts :as ech]
            [qoback.closspad.components.stats.charts :as charts]))

(defn view
  [state]
  (let [player (get-in state [:page/navigated :player])
        stats-by-player (first
                         (filter
                          #(= (keyword (:player %)) player)
                          (-> state :stats :by-player)))]
    [:div.mb-4
     {:style {:min-height "400px" :min-width "400px"}

      :replicant/key :stats

      :replicant/on-mount
      (fn [{:replicant/keys [node remember]}]
        (let [el (ech/mount-stats node)]
          (remember el)
          (ech/update-chart
           el
           (charts/player-stats-chart stats-by-player))))

      :replicant/on-update
      (fn [data]
        (ech/update-chart
         (:replicant/memory data)
         (charts/player-stats-chart stats-by-player)))}]))
