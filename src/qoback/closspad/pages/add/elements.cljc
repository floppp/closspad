(ns qoback.closspad.pages.add.elements
    (:require [replicant.alias :refer [defalias]]))

(defalias player-option [{:keys [selected]} [p]]
  [:option {:value p :selected (= (keyword p) selected)} p])

(defalias player-options [{:keys [classes selected actions]} players]
  [:select.w-full {:class classes :on {:change actions}}
   (map (fn [p] [player-option {:selected selected} p]) players)])
