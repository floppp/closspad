(ns qoback.closspad.state.effects
  (:require [qoback.closspad.state.db :refer [get-dispatcher]]
            [qoback.closspad.network.query :as network]
            [reitit.frontend.easy :as rfe]
            [qoback.closspad.helpers :as h]))

(defn navigated-not-found-page []
  (let [dispatcher (get-dispatcher)]
    (dispatcher nil [[:db/assoc :page/navigated {:page :not-found}]])))

(defn navigated-home-page []
  (let [dispatcher (get-dispatcher)]
    (dispatcher nil [[:db/assoc :page/navigated {:page :home}]])))

(defn navigated-match-page
  [{:keys [date]}]
  (.log js/console "date " date)
  (let [dispatcher (get-dispatcher)]
    (dispatcher
     nil
     [[:db/assoc :page/navigated {:page :match :date date}]])))


(defn perform-effect!
  [{:replicant/keys [^js js-event] :as replicant-data} [effect & args]]
  (case effect
    :dom/prevent-default (.preventDefaul js-event)
    :route/not-found
    (let [date (-> args
                   first
                   :match
                   :results
                   last
                   :played_at)]
      (when date
        (rfe/push-state :route/match {:day (h/format-iso-date date)})))
    :route/home (navigated-home-page)
    :route/match (navigated-match-page (-> args first first))
    :data/query (network/query-async {:query/kind :query/matches :query/data args})
    (js/console.log "Unknown effect" effect)))
