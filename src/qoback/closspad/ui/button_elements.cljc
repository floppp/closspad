(ns qoback.closspad.ui.button-elements
  (:require [replicant.alias :refer [defalias]]))

(defalias icon-button [attrs body]
  [:span
   {:on {:click (:actions attrs)}}
   body])

(defalias refresh-button [state]
  [:button.bg-blue-500.hover:bg-blue-600.text-white.px-4.py-2.rounded.disabled:opacity-50
   {:disabled (-> state :is-loading?)
    :on {:click [[:data/refresh]]}}
   (if (-> state :is-loading?)
     "Cargando..."
     "Actualizar")])
