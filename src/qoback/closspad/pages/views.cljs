(ns qoback.closspad.pages.views
  (:require [qoback.closspad.pages.widgets :as w]
            [qoback.closspad.pages.login.view :as login]
            [qoback.closspad.pages.match.view :as match]
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

(defn view
  [state]
  [:div.flex.h-screen
   [:div.flex-grow.p-4
    [:div.flex.flex-col.items-center.min-h-screen.mt-10
     (w/header state)
     (case (:page (:page/navigated state))
       :not-found (not-found-view)
       :login (login/view state)
       :home (home-view)
       :match [:div {:class ["w-full"
                             "md:w-3/4"
                             "lg:w-2/3"
                             "xl:w-1/2"
                             "2xl:w-1/3"
]}
               (match/view state)
               (classification/view state)])]]])
