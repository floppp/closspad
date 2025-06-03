;; Definición opciones para los distintos gráficos de echarts
(ns qoback.closspad.components.stats.echarts
  #_(:require ["echarts" :as echarts]))

(defn mount-stats
  [^js node]
  (.init js/echarts node))

(defn update-chart
  [^js chart option]
  (.setOption chart option))


(def mock
  {:title {:text "Echarts Getting Started Exapmle"}
   :tooltip {}
   :legend {:data ["sales"]}
   :xAxis {:data ["Shirts" "Cardingas" "Chiffons" "Pants" "Heels" "Socks"]}
   :yAxis {}
   :series {:name "sales" :type "bar" :data [5 20 36 10 10 20]}})
