(ns qoback.closspad.components.widgets
  (:require [qoback.closspad.helpers :as h]))

(defn header
  [_state]
  [:div.bg-white.rounded-lg.shadow-md.p-4.mb-6
   [:div.flex.flex-grow.gap-8.items-center
    [:a.text-gray-700.hover:text-gray-900.font-medium.transition-all.duration-200.ease-in-out
     {:href "/#"
      :class "hover:scale-105 hover:underline hover:underline-offset-4"}
     "Inicio"]
    [:a.text-gray-700.hover:text-gray-900.font-medium.transition-all.duration-200.ease-in-out
     {:href "/#/explanation"
      :title "para llorones"
      :class "hover:scale-105 hover:underline hover:underline-offset-4"}
     "Explicación"]
    [:a.text-gray-700.hover:text-gray-900.font-medium.transition-all.duration-200.ease-in-out
     {:href "/#/login"
      :class "hover:scale-105 hover:underline hover:underline-offset-4"}
     "Login"]]])

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

(defn add-day [current-date match-dates]
  (let [current-day (date-only current-date)]
    (some #(when (> (.getTime %) (.getTime current-day)) %)
          (->> match-dates
               (map date-only)
               (sort-by #(.getTime %))))))

(defn arrow-right
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
