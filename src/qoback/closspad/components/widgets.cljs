(ns qoback.closspad.components.widgets
  (:require [qoback.closspad.helpers :as h]
            [qoback.closspad.utils.datetime :as dt]
            [qoback.closspad.ui.elements :as ui]))

(defn- arrow-button
  [date icon]
  [ui/link-icon
   {:class (when-not date "cursor-not-allowed")
    :href (when date (str "#/match/" date))}
   icon])

(defn- date-only [^js/Date js-date]
  (doto (js/Date. (.getTime js-date))
    (.setHours 0 0 0 0)))

(defn add-day [current-date match-dates]
  (let [current-day (date-only current-date)]
    (some #(when (> (.getTime %) (.getTime current-day)) %)
          (->> match-dates
               (map date-only)
               (sort-by #(.getTime %))))))

(defn arrow-right
  [date match-dates]
  (let [date (when-let [day (add-day date match-dates)]
               (h/format-iso-date day))]
    (arrow-button date [ui/right-arrow-icon])))

(defn- arrow-left
  [date match-dates]
  (let [date (when-let [prev-date-with-match (last (filter #(< % date) match-dates))]
               (h/format-iso-date prev-date-with-match))]
    (arrow-button date [ui/left-arrow-icon])))

(defn- double-arrow-right
  [match-dates]
  (let [date (h/format-iso-date (last match-dates))]
    (arrow-button date [ui/right-double-arrow-icon])))

(defn- double-arrow-left
  [match-dates]
  (let [date (h/format-iso-date (first match-dates))]
    (arrow-button date [ui/left-double-arrow-icon])))

(defn arrow-selector
  [date match-dates]
  [:div.flex.justify-center.gap-3.items-center.w-full
   {:class ["max-w-[400px]" "sm:items-start"]}
   (double-arrow-left match-dates)
   (arrow-left date match-dates)

   [:div
    {:class
     ["text-lg" "font-semibold" "min-w-120" "text-center" "sm:text-xl"]}
    (dt/datetime->date->str date)]

   (arrow-right date match-dates)

   (double-arrow-right match-dates)])



