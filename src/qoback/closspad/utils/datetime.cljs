(ns qoback.closspad.utils.datetime)

(defn date->minus-one-year
  ([] (date->minus-one-year (js/Date.)))
  ([^js date]
   (let [d (doto date
             (.setFullYear (-> date .getFullYear dec))
             (.setHours 0 0 0))]
     (-> d .toISOString (.split "T") first))))


(defn get-month-name
  [^js date]
  (.toLocaleString date "default" #js {:month "short"}))

(defn get-month-name-with-year
  [^js date]
  (let [m (get-month-name date)]
    (str m "/" (.getFullYear date))))


(comment
  (date->minus-one-year)

  (get-month-name (js/Date.))

  (get-month-name-with-year (js/Date.))
  )
