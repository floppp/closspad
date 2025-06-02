(ns qoback.closspad.pages.stats.view
  (:require [qoback.closspad.components.stats.echarts :as ech]
            [qoback.closspad.components.stats.charts :as charts]))

(defn view
  [state]
  (let [player (get-in state [:page/navigated :player])
        players (-> state :stats :players)
        stats-by-player (first
                         (filter
                          #(= (keyword (:player %)) player)
                          (-> state :stats :by-player)))]
    [:div
     [:select.w-full.p-2.mb-4.text-gray-700.bg-white.rounded-md.focus:outline-none.focus:ring-2.focus:border-gray-300
      {:on {:change [[:route/push :event/target.value :route/stats]]}}
      (map (fn [p]
             [:option {:value p
                       :selected (= (keyword p) player)} p]) players)]
     [:div.mb-4
      {:style {:min-height "400px" :min-width "400px"}

       :replicant/key :stats1

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
          (charts/player-stats-chart stats-by-player)))}]

     [:div.flex.gap-10
      [:div.mb-4
       {:style {:min-height "400px" :min-width "400px"}

        :replicant/key :stats2

        :replicant/on-mount
        (fn [{:replicant/keys [node remember]}]
          (let [el (ech/mount-stats node)]
            (remember el)
            (ech/update-chart
             el
             (charts/radial-chart-against-players stats-by-player))))

        :replicant/on-update
        (fn [data]
          (ech/update-chart
           (:replicant/memory data)
           (charts/radial-chart-against-players stats-by-player)))}]

      [:div.mb-4
       {:style {:min-height "400px" :min-width "400px"}

        :replicant/key :stats3

        :replicant/on-mount
        (fn [{:replicant/keys [node remember]}]
          (let [el (ech/mount-stats node)]
            (remember el)
            (ech/update-chart
             el
             (charts/radial-chart-against-couples stats-by-player))))

        :replicant/on-update
        (fn [data]
          (ech/update-chart
           (:replicant/memory data)
           (charts/radial-chart-against-couples stats-by-player)))}]]]))
