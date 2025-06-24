(ns qoback.closspad.components.match.domain
  (:require [cljs.spec.alpha :as s]
            [qoback.closspad.core-domain :as core]))

(s/def ::player-id ::core/non-empty-string)
(s/def ::organization string?)
(s/def ::couple (s/tuple ::player-id ::player-id))
(s/def ::game-set (s/tuple number? number?))
(s/def ::game-result (s/or
                      :one-set ::game-set
                      :two-sets (s/tuple ::game-set ::game-set)
                      :three-sets (s/tuple ::game-set ::game-set ::game-set)))

(s/def ::couple_a   ::couple)
(s/def ::couple_b   ::couple)
(s/def ::result     ::game-result)
(s/def ::played_at  inst?)
(s/def ::importance number?)
(s/def ::match      (s/keys
                     :req-un [::couple_a ::couple_b ::played_at ::result ::organization]
                     :opt-un [::importance]))

(s/def ::matches (s/coll-of ::match))

(defn valid-set?
  [s]
  (let [[s1 s2] s
        bigger (max s1 s2)
        minor  (min s1 s2)
        diff (Math/abs (- s1 s2))]
    (when (s/valid? ::game-set s)
      (cond
        (and (>= bigger 6) (>= diff 2)) true
        (and  (= bigger 7) (= minor 6)) true
        :else false))))

(defn valid-result?
  [result]
  (and
   (> 4 (count result))
   (every? valid-set? result)))

(defn valid-match?
  [{:keys [result] :as match}]
  (when (s/valid? ::match match)
    (valid-result? result)))

(comment
  (valid-set? [6 4])
  (valid-set? [6 6])
  (valid-result? [[7 5]])
  (valid-result? [[6 4] [3 6] [6 4]])
  (valid-result? [[6 4] [3 5] [6 4]])
  (valid-result? [[6 4] [6 5] [6 4]])
  (valid-result? [[6 4] [6 3] [6 4]])
  (valid-result? [[6 4] [6 3] [6 4] [5 7]])
  (s/valid? ::played_at (js/Date.))
  (s/valid? ::player-id "foo")
  (s/valid? ::couple ["bar" "foo"])
  (def right-match {:result [[6 4]]
                    :couple_a ["foo" "bar"]
                    :couple_b ["foo" "bar"]
                    :played_at (js/Date.)
                    :organization "fik"})
  (def wrong-match {:couple_a ["foo" "bar"]
                    :couple_b ["foo" "bar"]
                    :played_at (js/Date.)
                    :organization "fik"})
  (valid-match? right-match)
  (s/explain-data ::match right-match)
  (s/explain-data ::match wrong-match)
  )



