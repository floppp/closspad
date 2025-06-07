(ns qoback.closspad.utils.numbers)

(defn half-round-number
  [n]
  (/ (Math/round (* 2 n)) 2))
