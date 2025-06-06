(ns qoback.closspad.router
  (:require [reitit.frontend :as rfr]
            [reitit.frontend.easy :as rfe]
            [reitit.frontend.controllers :as rfc]
            [qoback.closspad.state.db :refer [get-dispatcher !state]]))

(defn auth-guard
  [success]
  (let [st @!state]
    (if (:auth st)
      [[success]]
      (do
        (rfe/push-state :route/home)
        nil))))

(def routes
  [["/login"               {:name :route/login}]
   ["/add-match"           {:name :route/add-match}]
   ["/explanation"         {:name :route/explanation}]
   ["/stats"               {:name :route/full-stats}]
   ["/stats/:player"       {:name :route/stats          :path [:player string?]}]
   ["/match/:day"          {:name :route/match          :path [:day string?]}] ;; format: YYYY-MM-DD
   ["/classification/:day" {:name :route/classification :path [:day string?]}]])

(defn- get-route-actions
  [{:keys [data path-params]}]
  (case (:name data)
    ;; :route/home [[:route/home]]
    :route/match (let [date ^js (js/Date. (:day path-params))]
                   [[:route/match {:date date}]])
    :route/classification (let [day (keyword (:day path-params))]
                            [[:route/classification {:day day}]])
    :route/stats (let [player (keyword (:player path-params))]
                   [[:route/stats player]])
    :route/full-stats  [[:route/full-stats]]
    :route/explanation [[:route/explanation]]
    :route/add-match   (auth-guard :route/add-match)
    :route/login       [[:route/login]]
    [[:route/not-found]]))

(defn start! [routes dispatch!]
  (rfe/start!
   (rfr/router routes)
   (fn [{:keys [data] :as m}]
     (if m
       (do
         (rfc/apply-controllers nil m)
         (when-let [action (get-route-actions m)]
           (dispatch! nil action)))
       (dispatch! nil [[:route/not-found]])))
   {:use-fragment true}))
