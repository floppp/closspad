(ns qoback.closspad.core
  (:require [replicant.dom :as r-dom]
            [qoback.closspad.helpers :as h]
            [qoback.closspad.router :as router]
            [qoback.closspad.state.db :refer [!state !dispatcher get-dispatcher]]
            [qoback.closspad.state.events :refer [event-handler]]))

(def options ["03/03/2025" "06/03/2025" "11/04/2025" "21/04/2025" "03/05/2025"])

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
  [date]
  [:div.flex.items-center.gap-4
   (arrow-left date)

   [:div.text-xl.font-semibold.min-w-120.text-center
    (h/datetime->date->str date)]

   (arrow-right date)])

(defn match-view
  [state]
  (let [match-date (:date (:page/navigated state))]
    [:div.flex.flex-col.gap-4
     (arrow-selector match-date)
     [:button.btn.btn-success "Success"]]))

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
       :not-found [:div (not-found-view)]
       :home [:div (home-view)]
       :match [:div (match-view state #_(:match (:page/navigated state)))])]]])

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
