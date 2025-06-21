(ns qoback.closspad.components.stats.service
  (:require [clojure.string :as str]
            [qoback.closspad.rating.rating-system :refer [determine-winner]]
            [qoback.closspad.helpers :as h]
            [qoback.closspad.utils.datetime :as dt]))

(defn get-all-players
  "Returns a set of all unique player names from matches.
  Takes a sequence of matches where each match has :couple_a and :couple_b vectors."
  [matches]
  (->> matches
       (mapcat (fn [{:keys [couple_a couple_b]}]
                 (concat couple_a couple_b)))
       (into #{})))

(defn normalize-couple
  "Normalizes a couple by sorting names alphabetically and joining with ' & '.
  Takes a vector of player names and returns a normalized string representation."
  [couple]
  (->> couple
       (sort)
       (str/join " & ")))


(defn compute-player-couples-stats
  "Calculates a player's win/loss statistics against each couple they've played.
  Returns a map where keys are normalized couple strings and values contain:
  - :wins - wins against this couple
  - :losses - losses against this couple
  - :total - total matches against this couple
  - :win-percentage - win percentage (string)
  - :loss-percentage - loss percentage (string)"
  [player matches]
  (let [stats (reduce
               (fn [acc {:keys [couple_a couple_b result]}]
                 (let [in-p1 (contains? (set couple_a) player)
                       in-p2 (contains? (set couple_b) player)]
                   (if (or in-p1 in-p2)
                     (let [opponent-couple (if in-p1 couple_b couple_a)
                           opponent-key (normalize-couple opponent-couple)
                           ;; winner (determine-winner result)
                           winner (determine-winner result)
                           won (= winner (if in-p1 "A" "B"))
                           current (get acc opponent-key {:wins 0 :losses 0 :total 0})]
                       (assoc acc opponent-key
                              (-> current
                                  (update (if won :wins :losses) inc)
                                  (update :total inc))))
                     acc)))
               {}
               matches)]
    (reduce-kv
     (fn [m k v]
       (assoc m k
              (assoc v
                     :win-percentage
                     (if (pos? (:total v))
                       (str (* 100 (/ (:wins v) (:total v))))
                       "0.00")
                     :loss-percentage
                     (if (pos? (:total v))
                       (str (* 100 (/ (:losses v) (:total v))))
                       "0.00"))))
     {}
     stats)))

(defn couple-stats-to-player-stats
  "Converts couple-based stats to individual player stats.
  Takes a map of couple stats (from compute-player-couples-stats).
  Returns a map where keys are player names and values are:
  {:wins total-wins :losses total-losses}"
  [couple-stats]
  (reduce
   (fn [acc [couple {:keys [wins losses]}]]
     (let [players (str/split couple #" & ")]
       (reduce
        (fn [acc player]
          (-> acc
              (update-in [player :wins] (fnil + 0) wins)
              (update-in [player :losses] (fnil + 0) losses)))
        acc
        players)))
   {}
   couple-stats))

(defn compute-player-stats
  "Calculates match statistics for a specific player.
  Returns a map containing:
  - :player - player name
  - :wins - total wins count
  - :losses - total losses count
  - :win-percentage - win percentage (string)
  - :loss-percentage - loss percentage (string)
  - :by-month - map of monthly stats {:wins x :losses y}
  - :against-couples - map from compute-player-couples-stats"
  [player matches]
  (let [stats (reduce
               (fn [acc {:keys [couple_a couple_b result played_at]}]
                 (let [date (js/Date. played_at)
                       month (dt/get-month-name-with-year date)
                       in-p1 (contains? (set couple_a) player)
                       in-p2 (contains? (set couple_b) player)]
                   (if (or in-p1 in-p2)
                     (let [player-team (if in-p1 "A" "B")
                           winner (determine-winner result)
                           won (= winner player-team)
                           month-stats (get (:by-month acc) month {:wins 0 :losses 0})]
                       (-> acc
                           (update (if won :wins :losses) inc)
                           (assoc :by-month
                                  (assoc (:by-month acc)
                                         month
                                         (if won
                                           (update month-stats :wins inc)
                                           (update month-stats :losses inc))))))
                     acc)))
               {:player player :wins 0 :losses 0 :by-month {}}
               matches)
        total (+ (:wins stats) (:losses stats))
        couple-stats (compute-player-couples-stats player matches)]
    (assoc stats
           :win-percentage
           (if (pos? total)
             (str (* 100 (/ (:wins stats) total)))
             "0.00")
           :loss-percentage
           (if (pos? total)
             (str (* 100 (/ (:losses stats) total)))
             "0.00")
           :against-couples couple-stats
           :against-player (couple-stats-to-player-stats couple-stats))))

(defn compute-opponent-stats
  "Calculates statistics against different opponents.
  Returns a map where keys are normalized opponent couples and values are:
  - :wins - wins against this opponent
  - :losses - losses against this opponent
  - :total - total matches against this opponent
  - :win-percentage - win percentage (string)
  - :loss-percentage - loss percentage (string)"
  [matches]
  (.log js/console matches)
  (let [stats (reduce
               (fn [acc {:keys [couple_a couple_b result]}]
                 (let [winner (determine-winner result)
                       p1-key (normalize-couple couple_a)
                       p2-key (normalize-couple couple_b)]
                   (-> acc
                       (update p1-key
                               (fn [s]
                                 (let [won (= winner "A")
                                       s (or s {:wins 0 :losses 0 :total 0})]
                                   (-> s
                                       (update (if won :wins :losses) inc)
                                       (update :total inc)))))
                       (update p2-key
                               (fn [s]
                                 (let [won (= winner "B")
                                       s (or s {:wins 0 :losses 0 :total 0})]
                                   (-> s
                                       (update (if won :wins :losses) inc)
                                       (update :total inc))))))))
               {}
               matches)]
    (reduce-kv
     (fn [m k v]
       (assoc
        m k
        (assoc v
               :win-percentage
               (if (pos? (:total v))
                 (str (* 100 (/ (:wins v) (:total v))))
                 "0.00")
               :loss-percentage
               (if (pos? (:total v))
                 (str (* 100 (/ (:losses v) (:total v))))
                 "0.00"))))
     {}
     stats)))

(defn compute-couple-stats
  "Calculates statistics for each couple combination.
  Returns a map where keys are normalized couple strings and values contain:
  - :wins - total wins
  - :losses - total losses
  - :total - total matches
  - :win-percentage - win percentage (string)
  - :loss-percentage - loss percentage (string)"
  [matches]
  (let [stats (reduce
               (fn [acc {:keys [couple_a couple_b result]}]
                 (let [p1-key (normalize-couple couple_a)
                       p2-key (normalize-couple couple_b)
                       winner (determine-winner result)]
                   (-> acc
                       (update p1-key
                               (fn [s]
                                 (let [s (or s {:wins 0 :losses 0 :total 0})]
                                   (-> s
                                       (update (if (= winner "A") :wins :losses) inc)
                                       (update :total inc)))))
                       (update p2-key
                               (fn [s]
                                 (let [s (or s {:wins 0 :losses 0 :total 0})]
                                   (-> s
                                       (update (if (= winner "B") :wins :losses) inc)
                                       (update :total inc))))))))
               {}
               matches)]
    (reduce-kv
     (fn [m k v]
       (assoc m k
              (assoc
               v
               :win-percentage
               (if (pos? (:total v))
                 (str (* 100 (/ (:wins v) (:total v))))
                 "0.00")
               :loss-percentage
               (if (pos? (:total v))
                 (str (* 100 (/ (:losses v) (:total v))))
                 "0.00"))))
     {}
     stats)))



(defn compute-all-players-stats
  [players matches]
  (mapv #(compute-player-stats % matches) players))
