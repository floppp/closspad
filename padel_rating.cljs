(ns padel-rating.core
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

(s/def ::options (s/keys :req-un [::defaultRating ::maxRating ::minRating
                                  ::baseK ::scaleFactor ::players]))
(s/def ::defaultRating number?)
(s/def ::maxRating number?)
(s/def ::minRating number?)
(s/def ::baseK number?)
(s/def ::scaleFactor number?)
(s/def ::players (s/map-of ::player-id ::player))

(s/def ::match (s/keys :req-un [::coupleA ::coupleB ::date ::result]
                       :opt-un [::importance]))
(s/def ::coupleA ::couple)
(s/def ::coupleB ::couple)
(s/def ::date inst?)
(s/def ::result ::game-result)
(s/def ::importance number?)

;; Default configuration
(def default-options
  {:defaultRating 50
   :maxRating 100
   :minRating 0
   :baseK 10
   :scaleFactor 200
   :players {}})

;; Core functions
(defn create-system
  ([] (create-system {}))
  ([options]
   (merge default-options options)))

(defn add-player
  ([system player-id] (add-player system player-id (:defaultRating system)))
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

(defn update-system [system match]
  (let [{:keys [coupleA coupleB result importance]} match
        importance (or importance 1)
        all-players (concat coupleA coupleB)
        winner (determine-winner result)

        ;; Pre-calculate all ratings
        team-a-rating (team-rating system coupleA)
        team-b-rating (team-rating system coupleB)
        expected-win-a (expected-win system team-a-rating team-b-rating)

        ;; Add missing players
        system (reduce (fn [sys player-id]
                         (if (get-in sys [:players player-id])
                           sys
                           (add-player sys player-id)))
                       system
                       all-players)

        ;; Update both teams
        system (update-couple system "A" expected-win-a winner coupleA team-b-rating importance)
        system (update-couple system "B" (- 1 expected-win-a) winner coupleB team-a-rating importance)]

    ;; Apply decay to inactive players
    (apply-decay system all-players)))

;; Helper functions
(defn expected-win [system team-a-rating team-b-rating]
  (let [exponent (/ (- team-b-rating team-a-rating) (:scaleFactor system))]
    (/ 1 (+ 1 (Math/pow 10 exponent)))))

(defn proximity-factor [system player]
  (let [rating (or (:points player) (:defaultRating system))
        from-top (/ (- (:maxRating system) rating) (:maxRating system))
        from-bottom (/ rating (:maxRating system))]
    (* (min from-top from-bottom 1) 2)))

(defn k-factor [system player]
  (* (:baseK system) (or (:volatility player) 1)))

(defn clamp-rating [system rating]
  (max (:minRating system) (min (:maxRating system) rating)))

(defn determine-winner [result]
  (let [sets-won (count (filter (fn [[a b]] (> a b)) result))]
    (if (> sets-won 1) "A" "B")))

(defn team-rating [system couple]
  (reduce (fn [sum id] (+ sum (get-in system [:players id :points] 0)))
          0
          couple))

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
        (assoc-in [:players player2-id :volatility] (max 0.8 (* (or (:volatility player2) 1) 0.99)))))

(defn adjusted-points-change [system player teammate opponent-team-rating is-winner expected-win importance]
  (let [normalize (fn [points]
                    (max 0 (min 1 (/ (- points (:minRating system))
                                    (- (:maxRating system) (:minRating system))))))

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

;; Example usage
(def matches
  [{:coupleA ["Álex" "Dirk"]
    :coupleB ["Guapo" "Fernando"]
    :date #inst "2025-02-10"
    :result [[6 4] [3 6] [6 2]]}
   ;; ... other matches ...
   ])

(defn process-matches [matches]
  (let [initial-state (create-system)
        states (reduce (fn [acc match]
                         (cons (update-system (first acc) match) acc))
                       [initial-state]
                       matches)]
    (map (fn [state]
           (->> (:players state)
                (map (fn [[id player]] [id (/ (Math/round (* 2 (:points player))) 2)]))
                (sort-by second >)))
         states)))

(def formatted-states (process-matches matches))
