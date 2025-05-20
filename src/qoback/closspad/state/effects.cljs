(ns qoback.closspad.state.effects
  (:require [qoback.closspad.state.db :refer [get-dispatcher]]))

(defn navigated-not-found-page []
  (let [dispatcher (get-dispatcher)]
    (dispatcher nil [[:db/assoc :page/navigated {:page :not-found}]])))

(defn navigated-home-page []
  (let [dispatcher (get-dispatcher)]
    (dispatcher nil [[:db/assoc :page/navigated {:page :home}]])))

(defn navigated-match-page
  [{:keys [id]}]
  (let [dispatcher (get-dispatcher)]
    (dispatcher
     nil
     [[:db/assoc :page/navigated {:page :match :match id}]])))


(defn perform-effect!
  [{:keys [^js replicant/js-event]} [effect & args]]
  (case effect
    :route/not-found (navigated-not-found-page)
    :route/home (navigated-home-page)
    :route/match (navigated-match-page (-> args first first))
    :else (js/console.error "Unknown effect" effect)))


