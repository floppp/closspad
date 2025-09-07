(ns qoback.closspad.utils.numbers)

(defn half-round-number
  [n]
  (/ (Math/round (* 2 n)) 2))

(defn to-fixed-num
  "Number to Number, using the number of decimals
  specified. "
  [n decimals]
  (js/parseFloat (.toFixed n decimals)))

