(ns qoback.closspad.pages.views
  (:require [qoback.closspad.components.header :refer [header]]
            [qoback.closspad.ui.elements :as ui]
            [qoback.closspad.components.dialog :as dialog]
            [qoback.closspad.pages.login.view :as login]
            [qoback.closspad.pages.match.view :as match]
            [qoback.closspad.pages.forecast.view :as forecast]
            [qoback.closspad.pages.stats.view :as stats]
            [qoback.closspad.pages.add.view :as add]
            [qoback.closspad.pages.changelog.view :as changelog]
            [qoback.closspad.pages.full-stats.view :as full-stats]
            [qoback.closspad.components.system-explanation :as system-explanation]))

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
  (case (:page (:page/navigated state))
    :login (login/view state)
    [:div
     [:div.flex-grow
      [:div.flex.flex-col.items-center.min-h-screen.mt-5
       (header state)
       [ui/error-toast state]
       [:div {:class ["w-full"
                      "z-5"
                      "md:w-3/4"
                      "xl:w-2/3"
                      "2xl:w-2/5"]}
        (case (:page   (:page/navigated state))
          :not-found   (not-found-view)
          :home        (home-view)
          :match       (match/view state)
          :explanation (system-explanation/view)
          :full-stats  (full-stats/view state)
          :forecast    (forecast/view state)
          :changelog   (changelog/view state)
          :add-match   (add/view state)
          :stats       (stats/view state))]]]
     (dialog/component (:ui/dialog state) (:dialog state))]))
