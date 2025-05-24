(ns qoback.closspad.pages.widgets
  (:require [qoback.closspad.helpers :as h]))

(defn header
  [_state]
  [:div.flex
   [:div.flex-grow.p-4
    [:h1.text-3xl.font-bold.text-center.mb-2 "Clasificación Kurdistán"]]])

(defn date-only [^js/Date js-date]
  (doto (js/Date. (.getTime js-date))
    (.setHours 0 0 0 0)))

(defn add-day [current-date match-dates]
  (let [current-day (date-only current-date)]
    (some #(when (> (.getTime %) (.getTime current-day)) %)
          (->> match-dates
               (map date-only)
               (sort-by #(.getTime %)))
          #_(sort-by #(.getTime %)
                     (map date-only match-dates)))))

(defn- arrow-button
  [path cb]
  (let [date (cb)]
    [:a.btn.btn-circle
     {:href (when date (str "#/match/" date))
      :class (when-not date "cursor-not-allowed")}
     [:svg {:xmlns "http://www.w3.org/2000/svg" :class ["h-6" "w-6"] :fill "none" :viewBox "0 0 24 24" :stroke "currentColor"}
      [:path {:stroke-linecap "round" :stroke-linejoin "round" :stroke-width "2" :d path}]]]))

(defn arrow-left
  [date match-dates]
  (letfn [(substract-day []
            (let [prev-date-with-match (last (filter #(< % date) match-dates))]
              (when prev-date-with-match
                (h/format-iso-date prev-date-with-match))))]
    (arrow-button "M15 19l-7-7 7-7" substract-day)))

(defn arrow-right
  [date match-dates]
  (letfn [(add-day-fn [] (h/format-iso-date (add-day date match-dates)))]
    (arrow-button "M9 5l7 7-7 7" add-day-fn)))

(defn arrow-selector
  [date match-dates]
  [:div.flex.justify-center.items-center.gap-4
   (arrow-left date match-dates)

   [:div.text-xl.font-semibold.min-w-120.text-center
    (h/datetime->date->str date)]

   (arrow-right date match-dates)])

