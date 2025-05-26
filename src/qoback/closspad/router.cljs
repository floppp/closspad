(ns qoback.closspad.router
  (:require [reitit.frontend :as rfr]
            [reitit.frontend.easy :as rfe]))

#_(defn redirect-middleware [handler]
  (fn [match]
    (if match
      (handler match)
      (rfe/push-state :route/home)))) ; or your 404 page

(def routes
  [
   ["/match/:day" {:name :route/match
                   :path [:day string?]}] ;; format: YYYY-MM-DD
   ["/classification/:day" {:name :route/classification
                            :path [:day string?]}]
   ["/login" {:name :route/login
              :controllers
              [{:startm
                (fn [_]
                  (let [is-logged? true]
                    (.log js/console "is logged? " is-logged?)))
                :stop
                (fn [& _]
                  (.log js/console "Leaving login page"))}]}]
   ["/" {:name :route/home}]])

(defn- get-route-actions
  [{:keys [data path-params]}]
  (case (:name data)
    :route/home [[:route/home]]
    :route/match (let [date ^js (js/Date. (:day path-params))]
                   [[:route/match {:date date}]])
    :route/classification (let [day (keyword (:day path-params))]
                            [[:route/classification {:day day}]])
    :route/login [[:route/login]]
    [[:route/not-found]]))

(defn start! [routes dispatch!]
  (rfe/start!
   (rfr/router routes)
   (fn do-routing [m]
     (dispatch! nil (get-route-actions m)))
   {:use-fragment true}))
