(ns qoback.closspad.core
  (:require [replicant.dom :as r-dom]
            [qoback.closspad.router :as router]
            [qoback.closspad.state.db :refer [!state !dispatcher]]
            [qoback.closspad.state.events :refer [event-handler]]))

(defn match-view
  [match]
  [:div "Match"
   [:p match]])

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
