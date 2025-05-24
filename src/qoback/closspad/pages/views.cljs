(ns qoback.closspad.pages.views
  (:require [qoback.closspad.pages.widgets :as w]
            [qoback.closspad.helpers :as h]
            [qoback.closspad.pages.match.component :as match]))

(defn- not-found-view
  []
  [:div "Not Found"])

(defn- home-view
  []
  [:div [:div "Home"
         [:ul
          [:li "foo"]
          [:li "bar"]]]])

(defn classification-view
  [state]
  (let [day (:date (:page/navigated state))
        day-str (h/datetime->date->str day)
        ratings (:ratings (:classification state))
        day-ratings (first (filter (fn [[d _]]
                                     (= day-str (h/datetime->date->str (js/Date. d))))
                                   ratings))
        players (second day-ratings)]
    [:div
     [:h2.text-2xl.font-bold.text-center.mt-4.mb-2 "Clasificación"]
     [:div.flex.flex-col
      (for [[name, points] players]
        (let [cl (cond
                   (>= points 60) "bg-green-200"
                   (< points 40) "bg-red-200"
                   (< points 50) "bg-orange-200")]
          [:p.flex.justify-between.pt-2.pb-2.p
           {:class cl}
           [:span.font-bold name]
           [:span points]]))]]))

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
    [:div.flex.flex-col.gap-4
     {:style {:min-width "400px"}}
     (w/arrow-selector match-date (->> all-matches
                                     (map (comp #(js/Date. %) :played_at))
                                     sort))
     [:div.matches
      (for [match day-matches]
        (match/component match))]]))

(defn view [state]
  [:div.flex.h-screen
   [:div.flex-grow.p-4
    [:div.flex.flex-col.items-center.min-h-screen.mt-10
     (w/header state)
     (case (:page (:page/navigated state))
       :not-found (not-found-view)
       :home (home-view)
       :match [:div
               (match-view state)
               (classification-view state)])]]])
