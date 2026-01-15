(ns qoback.closspad.utils.datetime
  (:require [clojure.string :as str]))

(defn sp-date->js-date
  [s]
  (let [[d m y] (str/split s #"/")]
    (js/Date. y (dec m) d)))

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

(defn datetime->date->str
  [date & [{:keys [tz] :or {tz "es-ES"}}]]
  (if (instance? js/Date date)
    (str (.toLocaleDateString date tz))
    "Fecha no disponible"))

(defn invalid-date? [s]
  (let [d (js/Date. s)]
    (js/Number.isNaN (.getTime d))))

(comment
  (date->minus-one-year)

  (get-month-name (js/Date.))

  (get-month-name-with-year (js/Date.))
  )

