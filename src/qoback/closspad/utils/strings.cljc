(ns qoback.closspad.utils.strings
  (:require [clojure.string :as s]))

(defn camel->capitalize [s]
  (let [words (-> s
                  (s/replace #"([A-Z])" " $1") ;; insert a space before every capital letter
                  s/trim
                  (s/split #" "))]
    (->> words
         (map s/capitalize)
         (s/join " "))))
