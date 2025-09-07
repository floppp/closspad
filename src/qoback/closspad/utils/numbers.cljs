(ns qoback.closspad.utils.numbers)

(defn half-round-number
  [n]
  (/ (Math/round (* 2 n)) 2))

<<<<<<< HEAD
(defn to-fixed-num
  "Number to Number, using the number of decimals
  specified. "
  [n decimals]
  (js/parseFloat (.toFixed n decimals)))
=======
(defn to-fixed-num [n decimals]
  (when n
    (js/parseFloat (.toFixed n decimals))))
>>>>>>> e4368b573f183a0676772f7d1a2005a3df3538fc

