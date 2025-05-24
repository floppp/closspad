(ns qoback.closspad.pages.views
  (:require [qoback.closspad.pages.widgets :as w]
            [qoback.closspad.helpers :as h]
            [qoback.closspad.pages.match.component :as match]))

(defn- not-found-view
  []
  [:div "Not Found"])

(defn- home-view
  []
  [:div
   [:div "Home"
    [:ul
     [:li "foo"]
     [:li "bar"]]]])

(defn player-color?
  [points]
  (cond
    (>= points 60) ["bg-green-100" "border-l-4" "border-green-500"]
    (< points 40) ["bg-red-100" "border-l-4" "border-red-500"]
    (< points 50) ["bg-orange-100" "border-l-4" "border-orange-500"]
    :else ["bg-gray-50" "border-l-4" "border-gray-300"]))

(defn- player
  [[name, points]]
  (let [cl (player-color? points)]
    [:div.flex.justify-between.items-center.p-4.rounded-lg.shadow-sm
     {:class (concat cl ["transition-all" "duration-300" "ease-in-out"])}
     [:span.font-medium.text-gray-800 name]
     [:span.font-bold.text-lg (str points)]]))

(defn classification-view
  [state]
  (let [day (:date (:page/navigated state))
        day-str (h/datetime->date->str day)
        ratings (:ratings (:classification state))
        day-ratings (first
                     (filter
                      (fn [[d _]]
                        (= day-str (h/datetime->date->str (js/Date. d))))
                      ratings))
        players (second day-ratings)]
    [:div.bg-white.rounded-b-lg.shadow-md.p-8
     [:h2.text-3xl.font-bold.text-center.mb-6.text-gray-800 "Clasificación"]
     [:div.space-y-3
      (map player players)]]))

(defn match-view
  [state]
  (let [match-date (:date (:page/navigated state))
        match-date-str (h/datetime->date->str match-date)
        all-matches (:results (:match state))
        day-matches (filter
                     #(= match-date-str
                         (-> %
                             :played_at
                             js/Date.
                             h/datetime->date->str))
                     all-matches)]
    [:div.bg-white.rounded-t-lg.shadow-md.p-8
     [:div.mb-6
      (w/arrow-selector match-date (->> all-matches
                                        (map (comp #(js/Date. %) :played_at))
                                        sort))]
     [:div.space-y-4
      (for [match day-matches]
        [:div.border.border-gray-200.rounded-lg.p-4.shadow-sm
         (match/component match)])]]))

(defn view [state]
  [:div.flex.h-screen
   [:div.flex-grow.p-4
    [:div.flex.flex-col.items-center.min-h-screen.mt-10
     (w/header state)
     (case (:page (:page/navigated state))
       :not-found (not-found-view)
       :home (home-view)
       :match [:div {:class ["w-1/2" "min-w-[500px]"]}
               (match-view state)
               (classification-view state)])]]])
