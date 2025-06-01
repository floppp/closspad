(ns qoback.closspad.components.stats.charts
  (:require ["echarts" :as echarts]
            [qoback.closspad.components.stats.service :as stats]
            [qoback.closspad.helpers :as h]))

(defn render-player-stats-chart
  "Renders an ECharts visualization of player stats with:
  1. Stacked bar chart of monthly wins/losses
  2. Pie chart of win/loss ratio
  3. Performance trend line
  Takes a DOM element and stats map from compute-player-stats"
  [element stats]
  (let [chart (echarts/init element)
        months (->> (:by-month stats)
                    (keys)
                    (sort-by h/month-to-num))
        win-data (map #(get-in stats [:by-month % :wins] 0) months)
        loss-data (map #(get-in stats [:by-month % :losses] 0) months)
        total-wins (:wins stats)
        total-losses (:losses stats)
        win-rate (js/parseFloat (:win-percentage stats))]
    (.setOption chart
                (clj->js {:backgroundColor "#f8f9fa"
                          :title  {:text (str (:player stats) " - " total-wins "W / " total-losses "L")
                                   :subtext (str "Overall Win Rate: " win-rate "% (" total-wins "/" (+ total-wins total-losses) ")")
                                   :subtext (str "Win Rate: " (:win-percentage stats) "%")
                                   :left "center"
                                   :top 20
                                   :textStyle  {:fontSize 18
                                                :fontWeight "bold"
                                                :color "#333"}}
                          :tooltip  {:trigger "axis"
                                     :axisPointer  {:type "shadow"}}
                          :legend  {:data  ["Wins" "Losses" "Win Rate Trend"]
                                    :top 50}
                          :grid  {:left "3%"
                                  :right "4%"
                                  :bottom "3%"
                                  :containLabel true}
                          :xAxis  {:type "category"
                                   :data (clj->js months)
                                   :axisLabel  {:rotate 30
                                                :interval 0}}
                          :yAxis  {:type "value"
                                   :name "Matches"
                                   :axisLine  {:show true}
                                   :axisLabel  {:formatter "{value}"}}
                          :series  [{:name "Wins"
                                     :type "bar"
                                     :stack "total"
                                     :emphasis  {:focus "series"}
                                     :itemStyle  {:color "#4CAF50"}
                                     :data (clj->js win-data)}
                                    {:name "Losses"
                                     :type "bar"
                                     :stack "total"
                                     :emphasis #js {:focus "series"}
                                     :itemStyle #js {:color "#F44336"}
                                     :data (clj->js loss-data)}
                                    {:name "Win Rate Trend"
                                     :type "line"
                                     :yAxisIndex 0
                                     :symbolSize 8
                                     :symbol "circle"
                                     :lineStyle  {:width 3
                                                  :color "#2196F3"}
                                     :itemStyle  {:color "#2196F3"}
                                     :data (clj->js
                                            (map (fn [w l]
                                                   (if (zero? (+ w l))
                                                     0
                                                     (* 100 (/ w (+ w l))))
                                                   win-data loss-data)))}]}))
    chart))

(defn player-stats-dashboard
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

(defn render-win-loss-pie
  "Renders a donut chart of win/loss ratio"
  [element stats]
  (let [chart (echarts/init element)]
    (.setOption chart
                #js {:title #js {:text "Win/Loss Ratio"
                                 :left "center"
                                 :top 20
                                 :textStyle #js {:fontSize 14}}
                     :tooltip #js {:trigger "item"}
                     :legend #js {:orient "vertical"
                                  :left "left"
                                  :top "middle"}
                     :series #js [#js {:name "Matches"
                                       :type "pie"
                                       :radius ["40%", "70%"]
                                       :avoidLabelOverlap false
                                       :itemStyle #js {:borderRadius 10
                                                       :borderColor "#fff"
                                                       :borderWidth 2}
                                       :label #js {:show true
                                                   :formatter "{b}: {c} ({d}%)"}
                                       :emphasis #js {:label #js {:show true
                                                                  :fontSize 18
                                                                  :fontWeight "bold"}}
                                       :labelLine #js {:show true}
                                       :data #js [#js {:value (:wins stats)
                                                       :name "Wins"
                                                       :itemStyle #js {:color "#4CAF50"}}
                                                  #js {:value (:losses stats)
                                                       :name "Losses"
                                                       :itemStyle #js {:color "#F44336"}}]}]
                     :responsive true})
    chart))
