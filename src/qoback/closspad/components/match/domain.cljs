(ns qoback.closspad.components.match.domain
  (:require [cljs.spec.alpha :as s]
            [clojure.string :as str]
            [qoback.closspad.network.domain :refer [organization]]
            [qoback.closspad.core-domain :as core]))

(def importances
  {:major 1.0
   :p1 0.7
   :p2 0.5
   :promises 0.3
   :regular 0.1})

(defn importance->type
  [importance]
  (case importance
    1.0 :major
    0.7 :p1
    0.5 :p2
    0.3 :promises
    0.1 :regular
    :undefined))

(defn importance->color
  [importance]
  ())
(defn importance->name
  [importance]
  (str/capitalize (name (importance->type importance))))

(def importance-keys
  (set
   (->> importances keys (map (comp str/capitalize name)))))

(s/def ::player-id ::core/non-empty-string)
(s/def ::organization string?)
(s/def ::importance importance-keys)
(s/def ::couple (s/tuple ::player-id ::player-id))
(s/def ::game-set (s/tuple number? number?))
(s/def ::game-result (s/or
                      :one-set (s/tuple ::game-set)
                      :two-sets (s/tuple ::game-set ::game-set)
                      :three-sets (s/tuple ::game-set ::game-set ::game-set)))

(s/def ::couple_a   ::couple)
(s/def ::couple_b   ::couple)
(s/def ::result     ::game-result)
(s/def ::played_at  inst?)
(s/def ::match      (s/keys
                     :req-un [::couple_a ::couple_b ::played_at ::result ::organization]
                     :opt-un [::importance]))

(s/def ::matches (s/coll-of ::match))

(defn valid-set?
  [s]
  (let [[s1 s2] s
        bigger (max s1 s2)
        diff (Math/abs (- s1 s2))]
    (when (s/valid? ::game-set [s1 s2])
      (cond
        (and (= bigger 6) (> diff 1)) true
        (and (= bigger 7) (<= diff 2) (not= diff 0)) true
        :else false))))

(defn is-there-a-winner?
  [result]
  (let [sa (count (filter (fn [[a b]] (> a b)) result))
        sb (count (filter (fn [[a b]] (< a b)) result))]
    (not= sa sb)))

(defn valid-result?
  [result]
  (and
   (> 4 (count result))
   (every? valid-set? result)
   (is-there-a-winner? result)))

(defn valid-couples?
  [[a b] [c d]]
  (= 4 (count (into #{} [a b c d]))))

(defn new-match->match
  [match]
  (let [result (:result match)
        tr-result (mapv (fn [a b] [(js/parseInt a) (js/parseInt b)]) (:a result) (:b result))]
    (assoc match
           :result tr-result
           :organization organization
           :played_at (js/Date. (:played_at match)))))

(defn valid-match?
  [{:keys [result n-sets] :as match}]
  (let [match (new-match->match match)]
    (println (s/explain-data ::match match))
    (when (s/valid? ::match match)
      (and (= n-sets (count (:a result)) (count (:b result)) (count (:result match))) ;; this to check when new set with no fields yet
           (valid-result? (:result match))
           (valid-couples? (:couple_a match) (:couple_b match))))))

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
  (def match? {:couple_a ["Carlos" "Juan"]
               :couple_b ["Fober" "Pod"]
               :n-sets	2
               :organization	"fik"
               :played_at (js/Date.)
               :result	[[6 2] [1 6]]})

  (s/valid? ::match match?)

  (valid-match? right-match)
  (s/explain-data ::match right-match)
  (s/explain-data ::match wrong-match))
