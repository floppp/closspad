(ns qoback.closspad.rating.match
  (:require [qoback.closspad.rating.core :as core]
            [qoback.closspad.helpers :as h]))

(defn calculate-decay-factor
  [current-date last-date]
  (let [day-distance (h/days-between last-date current-date)]
    (min 1 (/ day-distance 300))))

(defn importance_
  "If match was only one set, less importante"
  [{:keys [result importance]}]
  (or importance
      (if (= 1 (count result))
        (:one-set-importance core/DEFAULTS)
        (:regular-importance core/DEFAULTS))))

(defn importance
  "Applies time decay to match importance"
  [{:keys [result importance played_at]} last-match-date]
  (let [base-importance (or importance
                            (if (= 1 (count result))
                              (:one-set-importance core/DEFAULTS)
                              (:regular-importance core/DEFAULTS)))
        decay-factor (calculate-decay-factor played_at last-match-date)]
    (* base-importance (- 1 decay-factor))))



