(ns qoback.closspad.state.route-effects
  (:require [reitit.frontend.easy :as rfe]
            [qoback.closspad.state.db :refer [get-dispatcher]]
            [qoback.closspad.helpers :as h]))

(defn navigated-match-page
  [{:keys [date]}]
  (let [dispatcher (get-dispatcher)]
    (dispatcher
     nil
     [[:db/assoc :page/navigated {:page :match :date date}]])))

(defn goto->stats
  [player]
  (let [dispatcher (get-dispatcher)]
    (dispatcher
     nil
     [[:db/assoc :page/navigated {:page :stats :player player}]])))

(defn goto->page
  [page]
  (let [dispatcher (get-dispatcher)]
    (dispatcher
     nil
     [[:db/assoc :page/navigated {:page page}]])))

(defn perform!
  [[action & args]]
  (case action
    :not-found (let [date (-> args
                              first
                              :match
                              :results
                              last
                              :played_at)]
                 (rfe/push-state
                  :route/match
                  {:day (h/format-iso-date (if date date (js/Date.)))}))
    :home        (goto->page :home)
    :changelog   (goto->page :changelog)
    :forecast    (goto->page :forecast)
    :explanation (goto->page :explanation)
    :match       (navigated-match-page (first args))
    :add-match   (goto->page :add-match)
    :stats       (goto->stats (first args))
    :full-stats  (goto->page :full-stats)
    :login (let [session (-> args :session :access_token)]
             (if (nil? session)
               (goto->page :login)
               (rfe/push-state :route/home)))
    (when goog.DEBUG
      (.log js/console "Unknown Route Effect " action " with arguments" args))))
