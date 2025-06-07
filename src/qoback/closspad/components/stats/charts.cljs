(ns qoback.closspad.components.stats.charts
  (:require [qoback.closspad.helpers :as h]))

(defn player-stats-chart
  "Renders an ECharts visualization of player stats with:
  1. Stacked bar chart of monthly wins/losses
  2. Pie chart of win/loss ratio
  3. Performance trend line
  Takes a DOM element and stats map from compute-player-stats"
  [stats]
  (.log js/console stats)
  (let [months (->> (:by-month stats)
                    (keys)
                    (h/rotate-months))
        win-data (map #(get-in stats [:by-month % :wins] 0) months)
        loss-data (map #(get-in stats [:by-month % :losses] 0) months)
        total-wins (:wins stats)
        total-losses (:losses stats)
        win-rate (js/parseFloat (:win-percentage stats))]
    (clj->js
     {:backgroundColor "#ffffff"
      :title  {:text (str (:player stats)
                          "  " total-wins "W / " total-losses "L     "
                          (.toFixed win-rate 2)
                          "% ")
               :left "center"
               :top 0
               :textStyle  {:fontSize 16 :fontWeight "bold" :color "#333"}}
      :tooltip  {:trigger "axis"
                 :axisPointer  {:type "shadow"}}
      :legend  {:data  ["Victorias" "Derrotas" "Ratio Victorias"]
                :top 30
                :itemGap 20}
      :grid  {:left "3%" :right "4%" :bottom "3%" :top "20%"
              :containLabel true}
      :xAxis  {:type "category"
               :data months
               :axisLabel  {:rotate 30
                            :interval 0}}
      :yAxis  [{:type "value"
                :name "Partidos"
                :axisLine {:show true}
                :axisLabel {:formatter "{value}"}}
               {:type "value"
                :name "Ratio Victorias %"
                :min 0
                :max 100
                :axisLine {:show true}
                :axisLabel {:formatter "{value}%"}
                :splitLine {:show false}}]
      :series  [{:name "Victorias"
                 :type "bar"
                 :stack "total"
                 :emphasis  {:focus "series"}
                 :itemStyle  {:color "#4CAF50"}
                 :data win-data}
                {:name "Derrotas"
                 :type "bar"
                 :stack "total"
                 :emphasis #js {:focus "series"}
                 :itemStyle #js {:color "#F44336"}
                 :data loss-data}
                {:name "Ratio Victorias"
                 :type "line"
                 :yAxisIndex 1
                 :symbolSize 8
                 :symbol "circle"
                 :lineStyle  {:width 3
                              :color "#2196F3"}
                 :itemStyle  {:color "#2196F3"}
                 :data (map
                        (fn [w l]
                          (if (zero? (+ w l))
                            0
                            (* 100 (/ w (+ w l)))))
                        win-data
                        loss-data)}]})))

#_(defn player-stats-dashboard
    "Renders a complete stats dashboard with:
  1. Main performance chart (top)
  2. Win/loss pie chart (bottom)
  Takes a player name and matches collection"
    [player matches]
    (let [stats (stats/compute-player-stats player matches)]
      [:div {:style {:display "flex"
                     :flexDirection "column"
                     :gap "2rem"
                     :padding "1rem"
                     :maxWidth "1200px"
                     :margin "0 auto"}}
       [:div {:style {:height "400px"
                      :width "100%"
                      :border "1px solid #e0e0e0"
                      :borderRadius "8px"
                      :boxShadow "0 2px 8px rgba(0,0,0,0.1)"}
              :ref #(when % (render-player-stats-chart % stats))}]
       [:div {:style {:display "flex"
                      :justifyContent "center"
                      :gap "2rem"}}
        [:div {:style {:height "300px"
                       :width "300px"
                       :border "1px solid #e0e0e0"
                       :borderRadius "8px"
                       :boxShadow "0 2px 8px rgba(0,0,0,0.1)"}
               :ref #(when % (render-win-loss-pie % stats))}]]]))

(defn radial-chart-against-players
  [stats]
  (let [opponents (:against-player stats)
        categories (keys opponents)
        win-data (map #(get-in opponents [% :wins] 0) categories)
        loss-data (map #(get-in opponents [% :losses] 0) categories)]
    (clj->js
     {:title {:text "Resultados Individuales"
              :left "center"}
      :tooltip {:trigger "item"}
      :legend {:data (map name categories)
               :orient "vertical"
               :left "left"}
      :radar {:indicator
              (map
               (fn [opp]
                 {:name opp :max (apply max (map + win-data loss-data))})
               categories)
              :radius "65%"
              :splitNumber 4
              :axisName {:color "#333"}
              :splitArea {:show true
                          :areaStyle {:color ["rgba(255, 255, 255, 0.5)"]}}
              :axisLine {:show true
                         :lineStyle {:color "rgba(0, 0, 0, 0.1)"}}
              :splitLine {:show true
                          :lineStyle {:color "rgba(0, 0, 0, 0.1)"}}}
      :series [{:type "radar"
                :data [{:value win-data
                        :name "Victorias"
                        :areaStyle {:color "rgba(76, 175, 80, 0.4)"}
                        :itemStyle {:color "#4CAF50"}
                        :lineStyle {:width 2
                                    :color "#4CAF50"}}
                       {:value loss-data
                        :name "Derrotas"
                        :areaStyle {:color "rgba(244, 67, 54, 0.4)"}
                        :itemStyle {:color "#F44336"}
                        :lineStyle {:width 2
                                    :color "#F44336"}}]}]})))

(defn radial-chart-against-couples
  [stats]
  (let [opponents (:against-couples stats)
        categories (keys opponents)
        win-data (map #(get-in opponents [% :wins] 0) categories)
        loss-data (map #(get-in opponents [% :losses] 0) categories)]
    (clj->js
     {:title {:text "Resultados Contra Parejas"
              :left "center"}
      :tooltip {:trigger "item"}
      :legend {:data (map name categories)
               :orient "vertical"
               :left "left"}
      :radar {:indicator (map (fn [opp] {:name opp :max (apply max (map + win-data loss-data))})
                              categories)
              :radius "65%"
              :splitNumber 4
              :axisName {:color "#333"}
              :splitArea {:show true
                          :areaStyle {:color ["rgba(255, 255, 255, 0.5)"]}}
              :axisLine {:show true
                         :lineStyle {:color "rgba(0, 0, 0, 0.1)"}}
              :splitLine {:show true
                          :lineStyle {:color "rgba(0, 0, 0, 0.1)"}}}
      :series [{:type "radar"
                :data [{:value win-data
                        :name "Victorias"
                        :areaStyle {:color "rgba(76, 175, 80, 0.4)"}
                        :itemStyle {:color "#4CAF50"}
                        :lineStyle {:width 2
                                    :color "#4CAF50"}}
                       {:value loss-data
                        :name "Derrotas"
                        :areaStyle {:color "rgba(244, 67, 54, 0.4)"}
                        :itemStyle {:color "#F44336"}
                        :lineStyle {:width 2
                                    :color "#F44336"}}]}]})))

(defn points-evolution-chart
  "Renders a line chart showing players' points evolution over time.
  Accepts a map of {player-id [{:date date-str :points points}]}"
  [points-history]
  (let [all-dates (->> points-history
                       vals
                       (mapcat (partial map :date)))
        unique-dates (distinct all-dates)
        players (keys points-history)]
    (clj->js
     {:backgroundColor "#ffffff"
      :tooltip {:trigger "axis"}
      :legend {:data (map name players)}
      :xAxis {:type "category"
              :data unique-dates
              :axisLabel {:rotate 30}}
      :yAxis {:type "value"
              :name "Puntos"}
      :series (map (fn [[player data]]
                     {:name player
                      :type "line"
                      :data (map :points data)
                      :symbolSize 8
                      :lineStyle {:width 3}
                      :itemStyle
                      {:color
                       (rand-nth ["#2196F3" "#4CAF50" "#F44336" "#FFC107" "#9C27B0"])}})
                   points-history)})))
