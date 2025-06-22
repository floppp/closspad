(ns qoback.closspad.pages.full-stats.view
  (:require [qoback.closspad.utils.datetime :as dt]
            [qoback.closspad.components.stats.echarts :as ech]
            [qoback.closspad.components.stats.charts :as charts]
            [qoback.closspad.ui.elements :as ui]))

(defn transform-history-data
  [history all-players]
  (reduce
   (fn [acc [date ratings]]
     (let [date-str (dt/datetime->date->str (js/Date. date))
           rating-map (into {} ratings)]
       (reduce (fn [acc player]
                 (update acc
                         player
                         conj {:date date-str
                               :points (get rating-map player)}))
               acc
               all-players)))
   (zipmap all-players (repeat []))
   history))

(defn view
  [state]
  (let [all-players (-> state :stats :players)
        history (-> state :classification :ratings reverse)
        points-history (transform-history-data history all-players)
        is-loading? (nil? (:stats state))]
    (if is-loading?
      [ui/spinner]
      [:div
       [:div.mb-4
        {:style {:height "400px" :width "100%"}

         :replicant/key :stats2

         :replicant/on-mount
         (fn [{:replicant/keys [node remember]}]
           (let [el (ech/mount-stats node)]
             (remember el)
             (.addEventListener js/window "resize" (fn [] (.resize el)))
             (ech/update-chart
              el
              (charts/points-evolution-chart points-history))))

         :replicant/on-update
         (fn [data]
           (ech/update-chart
            (:replicant/memory data)
            (charts/points-evolution-chart points-history)))

         :replicant/on-unmount
         (fn [{:replicant/keys [memory]}]
             ;; TODO: Añadir `removeEventListener`
           #_(.removeEventListener js/window "resize" (fn [] (.resize el))))}]])))
