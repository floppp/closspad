(ns qoback.closspad.ui.elements
  (:require [replicant.alias :refer [defalias]]))

(defalias card-details [attrs body]
  [:div.text-base attrs body])
