(ns qoback.closspad.helpers)

(def month-order
  {"jan" 1 "feb" 2 "mar" 3 "abr" 4 "may" 5 "jun" 6
   "jul" 7 "ago" 8 "sep" 9 "oct" 10 "nov" 11 "dic" 12})

(defn days-between [date1 date2]
  (let [d1 (.getTime (js/Date. date1))
        d2 (.getTime (js/Date. date2))
        diff (- d1 d2)]
    (/ diff (* 1000 60 60 24))))



(defn rotate-months [months]
  (let [current-month (-> (js/Date.) .getMonth inc) ; 1-12
        months-vec (vec (sort-by month-order months))
        rotate-point (- 13 current-month)]
    (concat (drop rotate-point months-vec)
            (take rotate-point months-vec))))

(defn first-day-next-month-prev-year
  ([] (first-day-next-month-prev-year (js/Date.)))
  ([date]
   (let [now (js/Date.)
         prev-year (doto date
                     (.setFullYear (-> now .getFullYear dec)))
         next-month (doto (js/Date. prev-year)
                      (.setMonth (-> prev-year .getMonth inc)))
         first-day (doto (js/Date. next-month)
                     (.setDate 1))]
     (-> first-day .toISOString (.split "T") first))))

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
