(ns qoback.closspad.components.match.services)

(defn couple-in-match?
  [couple match]
  (let [cs (set couple)
        ca (set (:couple_a match))
        cb (set (:couple_b match))]
    (or (= cs ca) (= cs cb))))

(defn match-couples-as-set
  [match]
  [(set (:couple_a match)) (set (:couple_b match))])

(defn match-with-couples-as-set
  [match]
  (let [[ca cb] (match-couples-as-set match)]
    (assoc match :couple_a ca :couple_b cb)))

(defn couple-matches?
  [couple matches]
  (->> matches
       (map match-with-couples-as-set)
       (filter #(couple-in-match? couple %))))


(defn get-couple-matches
  [couple state]
  (let [matches (-> state :stats :match)]
    (couple-matches? couple matches)))
