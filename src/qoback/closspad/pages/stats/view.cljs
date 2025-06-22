(ns qoback.closspad.pages.stats.view
  (:require [qoback.closspad.components.stats.echarts :as ech]
            [qoback.closspad.components.stats.charts :as charts]
            [qoback.closspad.components.widgets :as w]
            [qoback.closspad.ui.elements :as ui]))

(defn view
  [state]
  (let [player (get-in state [:page/navigated :player])
        players (-> state :stats :players)
        stats-by-player (first
                         (filter
                          #(= (keyword (:player %)) player)
                          (-> state :stats :by-player)))
        is-loading? (nil? stats-by-player)]
    (if is-loading?
        [ui/spinner]
        [:div
         [:select.w-full.p-2.mb-4.text-gray-700.bg-white.rounded-md.focus:outline-none.focus:ring-2.focus:border-gray-300
          {:on {:change [[:route/push :event/target.value :route/stats]]}}
          (map (fn [p]
                 [:option {:value p
                           :selected (= (keyword p) player)} p]) players)]
         [:div.mb-4
          {:style {:height "400px" :width "100%"}

           :replicant/key :stats1

           :replicant/on-mount
           (fn [{:replicant/keys [node remember]}]
             (let [el (ech/mount-stats node)]
               (remember el)
               (.addEventListener js/window "resize" (fn [] (.resize el)))
               (ech/update-chart
                el
                (charts/player-stats-chart stats-by-player))))

           :replicant/on-update
           (fn [data]
             (ech/update-chart
              (:replicant/memory data)
              (charts/player-stats-chart stats-by-player)))

           :replicant/on-unmount
           (fn [{:replicant/keys [memory]}]
             ;; TODO: Añadir `removeEventListener`
             #_(.removeEventListener js/window "resize" (fn [] (.resize el))))}]

         [:div.grid.grid-cols-1.grid-rows-2.md:grid-rows-1.md:grid-cols-2.p-4
          {:style {:background "white"}}
          [:div.mb-4
           {:style {:min-height "400px" :width "100%"}

            :replicant/key :stats2

            :replicant/on-mount
            (fn [{:replicant/keys [node remember]}]
              (let [el (ech/mount-stats node)]
                (remember el)
                (.addEventListener js/window "resize" (fn [] (.resize el)))
                (ech/update-chart
                 el
                 (charts/radial-chart-against-players stats-by-player))))

            :replicant/on-update
            (fn [data]
              (ech/update-chart
               (:replicant/memory data)
               (charts/radial-chart-against-players stats-by-player)))}]

          [:div.mb-4
           {:style {:min-height "400px" :width "100%"}

            :replicant/key :stats3

            :replicant/on-mount
            (fn [{:replicant/keys [node remember]}]
              (let [el (ech/mount-stats node)]
                (remember el)
                (.addEventListener js/window "resize" (fn [] (.resize el)))
                (ech/update-chart
                 el
                 (charts/radial-chart-against-couples stats-by-player))))

            :replicant/on-update
            (fn [data]
              (ech/update-chart
               (:replicant/memory data)
               (charts/radial-chart-against-couples stats-by-player)))}]]])))
