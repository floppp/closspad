(ns qoback.closspad.core
  (:require [replicant.dom :as r-dom]
            [clojure.string :as str]
            [qoback.closspad.helpers :as h]
            [qoback.closspad.router :as router]
            [qoback.closspad.state.db :refer [!state !dispatcher get-dispatcher]]
            [qoback.closspad.state.events :refer [event-handler]]))


(defn- arrow-button
  [path cb]
  (let [date (cb)]
    [:a.btn.btn-circle
     {;;:on {:click #(cb)} ;; podemos con dispatcher pero es bastante más lioso y obliga a caminos extra. Al hacerlo por url entra el router que llama a su vez al dispatcher con camino ya conocido.
      :href (str "#/match/" date)}
     [:svg {:xmlns "http://www.w3.org/2000/svg" :class ["h-6" "w-6"] :fill "none" :viewBox "0 0 24 24" :stroke "currentColor"}
      [:path {:stroke-linecap "round" :stroke-linejoin "round" :stroke-width "2" :d path}]]]))

(defn arrow-left
  [date]
  (letfn [(substract-day []
            (let [new-date (h/substract-days date)]
              (h/format-iso-date new-date)))]
    (arrow-button "M15 19l-7-7 7-7" substract-day)))

(defn arrow-right
  [date]
  (letfn [(add-day []
            (let [new-date (h/format-iso-date (h/add-days date))]
              (h/format-iso-date new-date)))]
    (arrow-button "M9 5l7 7-7 7" add-day)))

(defn arrow-selector
  [date match-dates]
  (.log js/console match-dates)
  [:div.flex.justify-center.items-center.gap-4
   (arrow-left date)

   [:div.text-xl.font-semibold.min-w-120.text-center
    (h/datetime->date->str date)]

   (arrow-right date)])

(defn match-component [{:keys [couple_a couple_b result]}]
  (let [f (first result)
        s (second result)
        t (get result 2)]
    [:table.table-auto.w-full.border-collapse.mb-6
     [:thead
      [:tr.border-b.border-gray-200 {:style {:display "grid" :grid-template-columns "3fr 1fr 1fr 1fr"}}
       [:th.text-left.pb-1 "Team"]
       [:th.text-center.pb-1 (if t "Set 1" "")]
       [:th.text-center.pb-1 (if t "Set 2" "Set 1")]
       [:th.text-center.pb-1 (if t "Set 3" "Set 2")]]]
     [:tbody
      [:tr {:style {:display "grid" :grid-template-columns "3fr 1fr 1fr 1fr"}}
       [:td.py-2 (str/join ", " couple_a)]
       [:td.text-center.py-2 (if t (first f) "")]
       [:td.text-center.py-2 (if t (first s) (first f))]
       [:td.text-center.py-2 (if t (first t) (first s))]]
      [:tr {:style {:display "grid" :grid-template-columns "3fr 1fr 1fr 1fr"}}
       [:td.py-2 (str/join ", " couple_b)]
       [:td.text-center.py-2 (if t (second f) "")]
       [:td.text-center.py-2 (if t (second s) (second f))]
       [:td.text-center.py-2 (if t (second t) (second s))]]]]))


(defn match-view
  [state]
  (let [match-date (:date (:page/navigated state))
        match-date-str (h/datetime->date->str match-date)
        all-matches (:results (:match state))
        day-matches (filter #(= match-date-str (h/datetime->date->str (js/Date. (:played_at %)))) all-matches)]
    [:div.flex.flex-col.gap-4
     {:style {:min-width "400px"}}
     (arrow-selector match-date (map :played_at all-matches))
     [:button.btn.btn-success
      {:on {:click [[:data/query [1 2]]]}}
      "Success"]
     [:div.matches
      (for [match day-matches]
        (match-component match))]]))

(defn not-found-view
  []
  [:div "Not Found"])

(defn home-view
  []
  [:div [:div "Home"
         [:ul
          [:li "foo"]
          [:li "bar"]]]])

(defn- header-view
  [_state]
  [:div.flex
   [:div.flex-grow.p-4
    [:h1.text-3xl.font-bold.text-center.mt-5 "Clasificación Pádel"]
    [:h2.text-xl.font-bold.text-center.mt-10 "Elige día para ver partidos"]]])

(defn view [state]
  [:div.flex.h-screen
   [:div.flex-grow.p-4
    [:div.flex.flex-col.items-center.min-h-screen.mt-10
     (header-view state)
     (case (:page (:page/navigated state))
       :not-found (not-found-view)
       :home (home-view)
       :match (match-view state #_(:match (:page/navigated state))))]]])

(defn- render! [state]
  (r-dom/render
   (js/document.getElementById "app")
   (view state)))

(defn ^{:dev/after-load true :export true} start! []
  (render! @!state))

(defn watch! [render!]
  (add-watch !state
             ::render (fn [_ _ old-state new-state]
                        (when (not= old-state new-state)
                          (render! new-state)))))

(defn ^:export init! []
  (r-dom/set-dispatch! event-handler)
  (swap! !dispatcher assoc :dispatcher event-handler)
  (router/start! router/routes event-handler)
  (watch! render!)
  (start!))
