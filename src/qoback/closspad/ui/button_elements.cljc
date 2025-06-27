(ns qoback.closspad.ui.button-elements
  (:require [replicant.alias :refer [defalias]]))

(defalias icon-button [attrs body]
  [:span
   {:on {:click (:actions attrs)}}
   body])
