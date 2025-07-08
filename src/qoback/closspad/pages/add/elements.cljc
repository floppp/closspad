(ns qoback.closspad.pages.add.elements
  (:require [replicant.alias :refer [defalias]]))

(defalias option
  [{:keys [selection]} [o]]
  [:option {:value o :selected (= o selection)} o])

(defalias select
  [{:keys [classes selection actions]} options]
  [:select.w-full {:class classes :on {:change actions}}
   (map
    (fn [o] [option {:selection selection} o])
    options)])


