(ns qoback.closspad.pages.forecast.services
  (:require [clojure.string]
            [qoback.closspad.components.match.services :as m]))

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

(defn couple->str
  [couple]
  (str "r: " (first couple) ", " "d: " (second couple)))


