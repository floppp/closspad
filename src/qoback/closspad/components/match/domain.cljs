(ns qoback.closspad.components.match.domain
  (:require [cljs.spec.alpha :as s]))

(s/def ::player-id string?)
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

(s/valid? ::matches [])

