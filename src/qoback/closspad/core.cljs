(ns qoback.closspad.core
  (:require [replicant.dom :as r-dom]
            [qoback.closspad.router :as router]
            [qoback.closspad.state.db :refer [!state !dispatcher]]
            [qoback.closspad.state.events :refer [event-handler]]))

(def options ["03/03/2025" "06/03/2025" "11/04/2025" "21/04/2025" "03/05/2025"])

(defn arrow-selector []
  [:div.flex.items-center.gap-4
   [:button.btn.btn-circle.btn-outline
    {:on {:click (fn [ev] (.log js/console ev))}}
    [:svg {:xmlns "http://www.w3.org/2000/svg" :class ["h-6" "w-6"] :fill "none" :viewBox "0 0 24 24" :stroke "currentColor"}
     [:path {:stroke-linecap "round" :stroke-linejoin "round" :stroke-width "2" :d "M15 19l-7-7 7-7"}]]]

       ;; Current Label
   [:div.text-xl.font-semibold.min-w-120.text-center
    (nth options 2)]

       ;; Right Arrow Button
   [:button.btn.btn-circle.btn-outline
    {:on {:click (fn [ev] (.log js/console ev))}}
    [:svg {:xmlns "http://www.w3.org/2000/svg" :class ["h-6" "w-6"] :fill "none" :viewBox "0 0 24 24" :stroke "currentColor"}
     [:path {:stroke-linecap "round" :stroke-linejoin "round" :stroke-width "2" :d "M9 5l7 7-7 7"}]]]])

(defn match-view
  [match]
  [:div
   (arrow-selector)
   [:p match]
   [:button.btn.btn-success "Success"]])

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
    [:h1.text-3xl.font-bold.text-center.mt-5 "WEB STORE with REPLICANT"]
    [:h2.text-xl.font-bold.text-center.mt-10 "Choose product group:"]]])

(defn view [state]
  [:div.flex.h-screen
   [:div.flex-grow.p-4
    [:div.flex.flex-col.items-center.min-h-screen.mt-10
     (header-view state)
     (case (:page (:page/navigated state))
       :not-found [:div (not-found-view)]
       :home [:div (home-view)]
       :match [:div (match-view (:match (:page/navigated state)))])]]])

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
