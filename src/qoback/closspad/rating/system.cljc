(ns qoback.closspad.rating.system
  (:require [qoback.closspad.utils.numbers :as n]
            [qoback.closspad.rating.core :as core]))

(defn- constant-decay
  [system active-players]
  (let [decay-factor 0.98]
    (update system :players
            (fn [players]
              (reduce-kv
               (fn [m id player]
                 (if (some #{id} active-players)
                   (assoc m id player)
                   (assoc m id (update player :points #(* % decay-factor)))))
               {}
               players)))))

(defn create-system
  ([] (create-system {}))
  ([options]
   (merge core/default-options options)))

(defn clamp-rating
  [system rating]
  (max (:min-rating system) (min (:max-rating system) rating)))

(defn add-player
  ([system player-id] (add-player system player-id (:default-rating system)))
  ([system player-id initial-rating]
   (let [rating (clamp-rating system initial-rating)]
     (update system :players assoc player-id
             {:id player-id
              :name player-id
              :points rating
              :volatility 1.1}))))

(defn get-ratings
  [system player-ids]
  (reduce
   (fn [acc id]
     (assoc acc id (get-in system [:players id :points] 50)))
   {}
   player-ids))

(defn get-team-rating [system couple]
  (reduce
   (fn [sum id]
     (+ sum (get-in system [:players id :points] 0)))
   0
   couple))

(defn expected-a-win [system team-a-rating team-b-rating]
  (let [exponent (/ (- team-b-rating team-a-rating) (:scaleFactor system))]
    (/ 1 (+ 1 (Math/pow 10 exponent)))))

(defn proximity-factor [system player]
  (let [rating (or (:points player) (:default-rating system))
        from-top (/ (- (:max-rating system) rating) (:max-rating system))
        from-bottom (/ rating (:max-rating system))]
    (* (min from-top from-bottom 1) 2)))

(defn k-factor [system player]
  (* (:base-k system) (or (:volatility player) 1)))

(defn determine-winner [result]
  (let [sets-won-by-a (count (filter (fn [[a b]] (> a b)) result))
        sets-won-by-b (count (filter (fn [[a b]] (< a b)) result))]
    (if (> sets-won-by-a sets-won-by-b)
      "A"
      "B")))



#_(defn adjusted-points-change
  [system player teammate opponent-team-rating is-winner expected-win]
  (let [normalize (fn [points]
                    (max 0 (min 1 (/ (- points (:min-rating system))
                                     (- (:max-rating system) (:min-rating system))))))

        player-norm (normalize (:points player))
        teammate-norm (normalize (:points teammate))
        opponent-norm (normalize (/ opponent-team-rating 2))

        partner-factor (+ 0.7 (* (- 0.5 teammate-norm) 0.6))
        opponent-factor (+ 0.6 (* opponent-norm 0.8))
        performance-factor (- 1.1 (* (- player-norm 0.5) 0.4))

        adjustment (if is-winner
                     (* partner-factor opponent-factor performance-factor)
                     (* (/ (* (- 2 partner-factor) (- 2 opponent-factor) (- 2 performance-factor)) 4)))

        k (k-factor system player)
        proximity (proximity-factor system player)
        base-change (* k (if is-winner (- 1 expected-win) (- expected-win)))]

    (* base-change proximity (max 0.5 (min 1.5 adjustment)))))

#_(defn compute-couple-match-points
  [system couple-id expected-win winner couple opponent-team-rating]
  (let [[p1-id p2-id] couple
        winner? (= winner couple-id)

        p1 (get-in system [:players p1-id])
        p2 (get-in system [:players p2-id])

        change1 (adjusted-points-change
                 system
                 p1
                 p2
                 opponent-team-rating
                 winner?
                 expected-win)
        change2 (adjusted-points-change
                 system
                 p2
                 p1
                 opponent-team-rating
                 winner?
                 expected-win)]
    (-> system
        (assoc-in
         [:players p1-id :points]
         (clamp-rating system (+ (:points p1) change1)))
        (assoc-in
         [:players p2-id :points]
         (clamp-rating system (+ (:points p2) change2))))))

#_(defn compute-match-points
  [system match]
  (let [{:keys [couple_a couple_b result]} match
        all-players (concat couple_a couple_b)
        winner (determine-winner result)
        team-a-rating (team-rating system couple_a)
        team-b-rating (team-rating system couple_b)
        expected-win-a (expected-win system team-a-rating team-b-rating)

        ;; Add missing players
        system (reduce
                (fn [sys player-id]
                  (if (get-in sys [:players player-id])
                    sys
                    (add-player sys player-id)))
                system
                all-players)

        system (compute-couple-match-points
                system
                "A"
                expected-win-a
                winner
                couple_a
                team-b-rating)
        system (compute-couple-match-points
                system
                "B"
                (- 1 expected-win-a)
                winner
                couple_b
                team-a-rating)]

    (assoc system :date (:played_at match))))

#_(defn debut-aware-decay
  "Applies decay only after player's first appearance
   Parameters:
   - player-data: Input array
   - inactive-threshold: 5
   - decay-rate: 0.95"
  [player-data & {:keys [inactive-threshold decay-rate]
                  :or {inactive-threshold 5 decay-rate 0.95}}]
  (let [sorted-data (sort-by first player-data)]
    (loop [remaining sorted-data
           streaks {} ; {:player {:debut? Boolean :last-score X :streak Y}}
           result []]
      (if (empty? remaining)
        (reverse result)
        (let [[date players] (first remaining)
              new-result
              (conj
               result
               [date
                (map
                 (fn [[name score]]
                   (let [prev (get streaks name)
                         is-debut? (nil? prev)
                         inactive? (and (:debut? prev)
                                        (= score (:last-score prev)))
                         new-streak (cond
                                      is-debut? 0
                                      inactive? (inc (:streak prev))
                                      :else 0)
                         decayed-score (if (>= new-streak inactive-threshold)
                                         (* score (Math/pow decay-rate
                                                            (- new-streak (dec inactive-threshold))))
                                         score)]
                     [name (n/half-round-number decayed-score)]))
                 players)])
              new-streaks
              (reduce
               (fn [s [name score]]
                 (let [prev (get s name)
                       is-debut? (nil? prev)
                       inactive? (and (:debut? prev)
                                      (= score (:last-score prev)))]
                   (assoc s name
                          {:debut? true
                           :last-score score
                           :streak (cond
                                     is-debut? 0
                                     inactive? (inc (:streak prev))
                                     :else 0)})))
               streaks
               players)]
          (recur (rest remaining) new-streaks new-result))))))

#_(defn activity-decay
  "Applies score decay based on player activity frequency
   Parameters:
   - player-data: The input array of [date players-list]
   - min-activity: Minimum matches to avoid penalty (default 5)
   - max-decay: Maximum score reduction (default 0.7 = 30% of score remains)
   - curve: Controls decay curve steepness (default 2.0)"
  ([player-data] (activity-decay player-data 5 0.7 2.0))
  ([player-data min-activity max-decay curve]
   (let [; Calculate total appearances per player
         player-counts (->> player-data
                            (map second)
                            (apply concat)
                            (group-by first)
                            (map (fn [[k v]] [k (count v)]))
                            (into {}))

                                        ; Find most active player's count
         max-count (apply max (vals player-counts))

                                        ; Normalization function with minimum activity threshold
         normalize (fn [count]
                     (if (< count min-activity)
                       0.0       ; Full penalty below minimum activity
                       (Math/pow (/ (- count min-activity)
                                    (- max-count min-activity))
                                 curve)))]

     (map (fn [[date players]]
            [date
             (map (fn [[name score]]
                    (let [activity (get player-counts name 0)
                          scale (+ max-decay
                                   (* (- 1.0 max-decay)
                                      (normalize activity)))]
                      [name (n/half-round-number (* score scale))]))
                  players)])
          player-data))))

#_(defn process-matches [matches]
  (let [initial-state (create-system)
        ;; last-match-date (:played_at (last (sort-by :played_at matches)))
        states (reduce
                (fn [acc match]
                  (cons (compute-match-points
                         (first acc)
                         match)
                        acc))
                [initial-state]
                matches)]
    (map
     (fn [state]
       [(:date state)
        (->> (:players state)
             (map
              (fn [[id player]]
                [id (n/half-round-number (:points player))]))
             (sort-by first >))])
     states)))

#_(defn logarithmic-decay
  "Applies logarithmic score decay with configurable parameters
   Parameters:
   - player-data: Input array of matches
   - options: {:threshold 5 :base-decay 0.9 :max-effect 0.5 :scale 10}
     - threshold: Matches before decay starts (default 5)
     - base-decay: Initial decay strength (default 0.9 = 10% reduction)
     - max-effect: Maximum decay effect (default 0.5 = 50% max reduction)
     - scale: Controls curve steepness (default 10)"
  [player-data
   & {:keys [threshold base-decay max-effect scale]
      :or {threshold 5
           base-decay 0.8
           max-effect 0.5
           scale 10}}]
  (let [sorted-data (sort-by first player-data)]
    (loop [remaining sorted-data
           streaks {} ; {:player {:last-score X :streak Y :active? Boolean}}
           result []]
      (if (empty? remaining)
        (reverse result)
        (let [[date players] (first remaining)
              new-result
              (conj
               result
               [date
                (map
                 (fn [[name score]]
                   (let [prev (get streaks name)
                         inactive? (and prev (= score (:last-score prev)))
                         new-streak (if inactive? (inc (:streak prev)) 0)
                         decay-factor (if (>= new-streak threshold)
                                        (let [over-threshold (- new-streak threshold)
                                              progress (Math/log (inc (/ over-threshold scale)))
                                              decay-amount (min max-effect
                                                                (* (- 1 base-decay) progress))
                                              final-factor (- 1 decay-amount)]
                                          (max 0.1 final-factor)) ; Never go below 10%
                                        1.0)]
                     [name (n/half-round-number (* score decay-factor))]))
                 players)])
              new-streaks
              (reduce
               (fn [s [name score]]
                 (let [prev (get s name)
                       inactive? (and prev (= score (:last-score prev)))]
                   (assoc s name
                          {:last-score score
                           :streak (if inactive? (inc (:streak prev)) 0)
                           :active? true})))
               streaks
               players)]
          (recur (rest remaining) new-streaks new-result))))))
