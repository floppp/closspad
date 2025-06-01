(ns qoback.closspad.router
  (:require [reitit.frontend :as rfr]
            [reitit.frontend.easy :as rfe]
            [reitit.frontend.controllers :as rfc]
            [qoback.closspad.helpers :as h]
            [qoback.closspad.state.db :refer [get-dispatcher]]))

;; (def dispatcher (get-dispatcher))
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
   ["/add-match" {:name :route/add-match}]
   ["/explanation" {:name :route/explanation}]
   ["/stats/:player" {:name :route/stats :path [:player string?]}]
   ["/login" {:name :route/login
              :controllers
              [{:start
                (fn [_]
                  (let [dispatcher (get-dispatcher)]
                    (dispatcher nil [[:route/home]])))
                :stop
                (fn [& _]
                  (.log js/console "Leaving login page"))}]}]
   #_["/" {:name :route/home}]])

(defn- get-route-actions
  [{:keys [data path-params]}]
  (case (:name data)
    ;; :route/home [[:route/home]]
    :route/match (let [date ^js (js/Date. (:day path-params))]
                   [[:route/match {:date date}]])
    :route/classification (let [day (keyword (:day path-params))]
                            [[:route/classification {:day day}]])
    :route/stats (let [player (keyword (:player path-params))]
                   [[:route/stats {:player player}]])
    :route/explanation [[:route/explanation]]
    :route/login [[:route/login]]
    [[:route/not-found]]))

(defn start! [routes dispatch!]
  (rfe/start!
   (rfr/router routes)
   (fn do-routing [{:keys [data] :as m}]
     (if m
       (do
         (rfc/apply-controllers nil m)
         (dispatch! nil (get-route-actions m)))
       (dispatch! nil [[:route/not-found]])))
   {:use-fragment true}))
