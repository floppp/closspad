(ns qoback.closspad.utils.numbers)

(defn half-round-number
  [n]
  (/ (Math/round (* 2 n)) 2))

(defn to-fixed-num [n decimals]
  (when n
    (js/parseFloat (.toFixed n decimals))))

