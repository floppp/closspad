(ns qoback.closspad.pages.views
  (:require [qoback.closspad.components.widgets :as w]
            [qoback.closspad.pages.login.view :as login]
            [qoback.closspad.pages.match.view :as match]
            [qoback.closspad.pages.stats.view :as stats]
            [qoback.closspad.pages.add.view :as add]
            [qoback.closspad.components.system-explanation :as system-explanation]
            [qoback.closspad.pages.classification.view :as classification]))

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

(defn explanation-view
  []
  (system-explanation/component))


(defn view
  [state]
  (case (:page (:page/navigated state))
    :login (login/view state)
    [:div.flex.h-screen
     [:div.flex-grow
      [:div.flex.flex-col.items-center.min-h-screen.mt-5
       (w/header state)
       [:div {:class ["w-full"
                      "md:w-3/4"
                      "lg:w-2/3"
                      "xl:w-1/2"
                      "2xl:w-1/3"]}
        (case (:page (:page/navigated state))
          :not-found (not-found-view)
          :home (home-view)
          :add-match (add/view state)
          :explanation (explanation-view)
          :match [:div
                  (match/view state)
                  (classification/view state)]
          :stats (stats/view state))]]]]))
