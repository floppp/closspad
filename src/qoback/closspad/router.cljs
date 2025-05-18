(ns qoback.closspad.router
  (:require [reitit.frontend :as rf]
            [reitit.frontend.easy :as rfe]))

(def routes
  [["/" {:name :route/home}]
   ["/match/:id" {:name :route/match
                  :path [:id number?]}]
   ["/classification/:day" {:name :route/classification
                            :path [:day string?]}]
   ["/login" {:name :route/login
              :controllers
              [{:start
                (fn [_]
                  (let [is-logged? true]
                    (.log js/console "is logged? " is-logged?)))
                :stop
                (fn [& _]
                  (.log js/console "Leaving login page"))}]}]])

(defn- get-route-actions [{:keys [data path-params]}]
  (case (:name data)
    :route/home [[:route/home]]
    :route/match (let [id (int (:id path-params))]
                   [[:route/match {:id id}]])
    :route/classification (let [day (keyword (:day path-params))]
                            [[:route/classification {:day day}]])
    :route/login [[:route/login]]))

(defn start! [routes dispatch!]
  (rfe/start! (rf/router routes)
              (fn [m]
                (dispatch! nil (get-route-actions m)))
              {:use-fragment true}))
