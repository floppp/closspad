(ns qoback.closspad.rating.domain
  (:require [cljs.spec.alpha :as s]))


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
