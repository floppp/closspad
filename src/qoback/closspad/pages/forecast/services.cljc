(ns qoback.closspad.pages.forecast.services
  (:require [qoback.closspad.components.match.services :as m]))

(defn couple-matches?
  [couple matches]
  (->> matches
       (map m/match-with-couples-as-set)
       (filter #(m/couple-in-match? couple %))))

(defn same-match?
  [couples matches]
  (->> matches
       (map m/match-with-couples-as-set)
       (filter #(m/same-match? couples %))))

(defn get-couple-matches
  [couple state]
  (let [matches (-> state :match :results)]
    (couple-matches? couple matches)))

