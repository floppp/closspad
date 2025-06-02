(ns qoback.closspad.helpers)

(defn datetime->date->str [date & [{:keys [tz] :or {tz "es-ES"}}]]
  (str (.toLocaleDateString date tz)))

(defn format-iso-date [date]
  (let [d (js/Date. date)
        year (.getFullYear d)
        month (inc (.getMonth d))
        day (.getDate d)]
    (str year "-"
         (when (< month 10) "0") month "-"
         (when (< day 10) "0") day)))

(defn add-days
  [date & {:keys [n-days] :or {n-days 1}}]
  (let [js-date (js/Date. date)
        new-time (+ (.getTime js-date) (* n-days 24 60 60 1000))]
    (js/Date. new-time)))

(defn substract-days
  [date & {:keys [n-days] :or {n-days 1}}]
  (let [js-date (js/Date. date)
        new-time (- (.getTime js-date) (* n-days 24 60 60 1000))]
    (js/Date. new-time)))

(defn get-month-name [date]
  (.toLocaleString date "default" #js {:month "short"}))

(defn get-month-num
  [date]
  (.getMonth (js/Date. date)))
