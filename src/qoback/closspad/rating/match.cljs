(ns qoback.closspad.rating.match
  (:require [qoback.closspad.rating.core :as core]
            [qoback.closspad.helpers :as h]))

(defn calculate-decay-factor
  [match-date last-date]
  (let [day-distance (h/days-between last-date match-date)]
    (min (:max-decay core/DEFAULTS)
         (/ day-distance (:time-distance-factor core/DEFAULTS)))))

(defn one-set-importance
  "If match was only one set, less importante"
  [{:keys [result importance]}]
  (or importance
      (if (= 1 (count result))
        (:one-set-importance core/DEFAULTS)
        (:regular-importance core/DEFAULTS))))

(defn time-decay-importance
  "Applies time decay to match importance"
  [{:keys [played_at] :as data} last-match-date]
  (let [base-importance (one-set-importance data)
        decay-factor (calculate-decay-factor played_at last-match-date)
        factor (- 1 decay-factor)]
    (* base-importance factor)))


