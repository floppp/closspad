(ns qoback.closspad.pages.full-stats.view
  (:require [qoback.closspad.utils.datetime :as dt]
            [qoback.closspad.utils.numbers :as n]
            [qoback.closspad.components.stats.echarts :as ech]
            [qoback.closspad.components.stats.charts :as charts]
            [qoback.closspad.ui.elements :as ui]))

(defn transform-history-data
  [history all-players]
  (reduce
   (fn [acc [date ratings]]
     (let [date-str (dt/datetime->date->str (js/Date. date) {:tz "en-US"})
           rating-map (into {} ratings)]
       (reduce (fn [acc player]
                 (update acc
                         player
                         conj {:date date-str
                               :original-date date
                               :points (when-let [v (get rating-map player)]
                                         (n/to-fixed-num v 2))}))
               acc
               all-players)))
   (zipmap all-players (repeat []))
   history))

(defn keep-last-by-date
  [coll]
  (->> coll
       (reduce (fn [m x] (assoc m (:date x) x)) {})
       vals
       (sort-by :original-date)))


(defn view
  [state]
  (let [all-players (-> state :stats :players)
        history (-> state :classification :ratings reverse)
        points-history (transform-history-data history all-players)
        is-loading? (nil? (:stats state))
        points-history (update-vals points-history keep-last-by-date)]
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
