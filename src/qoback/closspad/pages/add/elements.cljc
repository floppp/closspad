(ns qoback.closspad.pages.add.elements
    (:require [replicant.alias :refer [defalias]]))

(defalias player-option [{:keys [selection]} [p]]
  [:option {:value p :selected (= p selection)} p])

(defalias player-options [{:keys [classes selection actions]} players]
  [:select.w-full {:class classes :on {:change actions}}
   (map (fn [p] [player-option {:selection selection} p]) players)])
