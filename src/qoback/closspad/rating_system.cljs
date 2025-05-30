(ns qoback.closspad.rating-system
  (:require [cljs.spec.alpha :as s]
            [clojure.string :as str]))

;; Type definitions
(s/def ::player-id string?)
(s/def ::couple (s/tuple ::player-id ::player-id))
(s/def ::game-set (s/tuple number? number?))
(s/def ::game-result (s/or :two-sets (s/tuple ::game-set ::game-set)
                           :three-sets (s/tuple ::game-set ::game-set ::game-set)))

(s/def ::player (s/keys :req-un [::player-id ::name ::points]
                        :opt-un [::volatility]))
(s/def ::name string?)
(s/def ::points number?)
(s/def ::volatility number?)

(s/def ::options (s/keys :req-un [::default-rating ::max-rating ::min-rating
                                  ::base-k ::scale-factor ::players]))
(s/def ::default-rating number?)
(s/def ::max-rating number?)
(s/def ::min-rating number?)
(s/def ::base-k number?)
(s/def ::scale-factor number?)
(s/def ::players (s/map-of ::player-id ::player))

(s/def ::match (s/keys :req-un [::couple_a ::couple_b ::played_at ::result]
                       :opt-un [::importance]))
(s/def ::couple_a ::couple)
(s/def ::couple_b ::couple)
(s/def ::played_at inst?)
(s/def ::result ::game-result)
(s/def ::importance number?)

;; Default configuration
(def default-options
  {:default-rating 50
   :max-rating 100
   :min-rating 0
   :base-k 20
   :scale-factor 100
   :players {}
   :date nil})

;; Core functions
(defn create-system
  ([] (create-system {}))
  ([options]
   (merge default-options options)))

(defn clamp-rating [system rating]
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

(defn get-ratings [system player-ids]
  (reduce (fn [acc id]
            (assoc acc id (get-in system [:players id :points] 0)))
          {}
          player-ids))

;; Helper functions
(defn expected-win [system team-a-rating team-b-rating]
  (let [exponent (/ (- team-b-rating team-a-rating) (:scale-factor system))]
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

(defn team-rating [system couple]
  (reduce (fn [sum id] (+ sum (get-in system [:players id :points] 0)))
          0
          couple))

(defn adjusted-points-change [system player teammate opponent-team-rating is-winner expected-win importance]
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
                     (* importance partner-factor opponent-factor performance-factor)
                     (* importance (/ (* (- 2 partner-factor) (- 2 opponent-factor) (- 2 performance-factor)) 4)))

        k (k-factor system player)
        proximity (proximity-factor system player)
        base-change (* k (if is-winner (- 1 expected-win) (- expected-win)))]

    (* base-change proximity (max 0.5 (min 1.5 adjustment)))))

(defn update-couple [system couple-id expected-win winner couple opponent-team-rating importance]
  (let [[player1-id player2-id] couple
        is-winner (= winner couple-id)

        player1 (get-in system [:players player1-id])
        player2 (get-in system [:players player2-id])

        change1 (adjusted-points-change system player1 player2 opponent-team-rating is-winner expected-win importance)
        change2 (adjusted-points-change system player2 player1 opponent-team-rating is-winner expected-win importance)]

    (-> system
        (assoc-in [:players player1-id :points] (clamp-rating system (+ (:points player1) change1)))
        (assoc-in [:players player1-id :volatility] (max 0.8 (* (or (:volatility player1) 1) 0.99)))
        (assoc-in [:players player2-id :points] (clamp-rating system (+ (:points player2) change2)))
        (assoc-in [:players player2-id :volatility] (max 0.8 (* (or (:volatility player2) 1) 0.99))))))

(defn apply-decay [system active-players]
  (let [decay-factor 0.98]
    (update system :players
            (fn [players]
              (reduce-kv (fn [m id player]
                           (if (some #{id} active-players)
                             (assoc m id player)
                             (assoc m id (update player :points #(clamp-rating system (* % decay-factor))))))
                         {}
                         players)))))

(defn update-system [system match]
  (let [{:keys [couple_a couple_b result importance]} match
        importance (or importance 1)
        all-players (concat couple_a couple_b)
        winner (determine-winner result)

        ;; Pre-calculate all ratings
        team-a-rating (team-rating system couple_a)
        team-b-rating (team-rating system couple_b)
        expected-win-a (expected-win system team-a-rating team-b-rating)

        ;; Add missing players
        system (reduce (fn [sys player-id]
                         (if (get-in sys [:players player-id])
                           sys
                           (add-player sys player-id)))
                       system
                       all-players)

        ;; Update both teams
        system (update-couple system "A" expected-win-a winner couple_a team-b-rating importance)
        system (update-couple system "B" (- 1 expected-win-a) winner couple_b team-a-rating importance)]

    ;; Apply decay to inactive players
    (-> system
        (apply-decay all-players)
        (assoc :date (:played_at match)))
    ))

(defn process-matches [matches]
  (let [initial-state (create-system)
        states (reduce (fn [acc match]
                         (cons (update-system (first acc) match) acc))
                       [initial-state]
                       matches)]
    (map (fn [state]
           [(:date state)
            (->> (:players state)
                 (map (fn [[id player]] [id (/ (Math/round (* 2 (:points player))) 2)]))
                 #_(sort-by second >)
                 (sort-by first >))])
         states)))
