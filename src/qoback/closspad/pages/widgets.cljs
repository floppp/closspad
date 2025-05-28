(ns qoback.closspad.pages.widgets
  (:require [qoback.closspad.helpers :as h]))

(defn header
  [_state]
  [:div.flex
   [:div.flex-grow.p-4
    #_[:h1.text-3xl.font-bold.text-center.mb-2 "FIK"]]])

(defn- arrow-button
  [path cb]
  (let [date (cb)]
    [:a
     {:href (when date (str "#/match/" date))
      :class (when-not date "cursor-not-allowed")}
     [:svg {:xmlns "http://www.w3.org/2000/svg" :class ["h-6" "w-6"] :fill "none" :viewBox "0 0 24 24" :stroke "currentColor"}
      [:path {:stroke-linecap "round" :stroke-linejoin "round" :stroke-width "2" :d path}]]]))

(defn- date-only [^js/Date js-date]
  (doto (js/Date. (.getTime js-date))
    (.setHours 0 0 0 0)))

(defn- add-day [current-date match-dates]
  (let [current-day (date-only current-date)]
    (some #(when (> (.getTime %) (.getTime current-day)) %)
          (->> match-dates
               (map date-only)
               (sort-by #(.getTime %))))))

(defn- arrow-right
  [date match-dates]
  (letfn [(add-day-fn []
            (when-let [day (add-day date match-dates)]
              (h/format-iso-date day)))]
    (arrow-button "M9 5l7 7-7 7" add-day-fn)))

(defn- arrow-left
  [date match-dates]
  (letfn [(substract-day []
            (when-let [prev-date-with-match (last (filter #(< % date) match-dates))]
              (h/format-iso-date prev-date-with-match)))]
    (arrow-button "M15 19l-7-7 7-7" substract-day)))

(defn- double-arrow-right
  [match-dates]
  (letfn [(add-day-fn [] (h/format-iso-date (last match-dates)))]
    (arrow-button "M7 5 l7 7 -7 7 M16 5 l7 7 -7 7" add-day-fn)))

(defn- double-arrow-left
  [match-dates]
  (letfn [(first-day-fn [] (h/format-iso-date (first match-dates)))]
    (arrow-button "M19 5 l-7 7 7 7 M10 5 l-7 7 7 7" first-day-fn)))

(defn arrow-selector
  [date match-dates]
  [:div.flex.justify-center.gap-3.items-center.w-full
   {:class ["max-w-[400px]""sm:items-start"]}
   (double-arrow-left match-dates)

   (arrow-left date match-dates)

   [:div
    {:class
     ["text-lg" "font-semibold" "min-w-120" "text-center" "sm:text-xl"]}
    (h/datetime->date->str date)]

   (arrow-right date match-dates)

   (double-arrow-right match-dates)])
