(ns qoback.closspad.rating.match
  (:require [qoback.closspad.rating.core :as core]
            [qoback.closspad.helpers :as h]
            [qoback.closspad.components.stats.service :as stats]
            ["../../../js/ratingSystem" :as rating]
            ["../../../js/playerStats" :as plStats]))

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

(defn full-matches-process
  [ms & {:keys [system-type] :or {system-type "elo"}}]
  (let [[classification system] (js->clj
                                 (rating/processMatches (clj->js ms) system-type)
                                 {:keywordize-keys true})
        all-players (-> ms stats/get-all-players vec sort)
        js-stats (js->clj
                  (plStats/getAllPlayersOpponentStats
                   (clj->js all-players)
                   (clj->js ms))
                  {:keywordize-keys true})]
    {:ratings (filter (comp some? first) classification)
     :history (filter :date system)
     :players all-players
     :stats-by-player (stats/compute-all-players-stats all-players ms)
     :oponent-stats js-stats
     :matches ms}))
