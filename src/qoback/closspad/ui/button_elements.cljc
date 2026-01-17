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

(defn- toggle-text [active off-text on-text]
  (if active
    on-text
    off-text))

(defalias toggle-button [{:keys [active on-text off-text]}]
  [:button.flex.items-center.gap-2.bg-gray-200.hover:bg-gray-300.text-gray-700.px-4.py-2.rounded
   {:on {:click [[:ui/toggle]]}}
   [:div.w-4.h-4.rounded-full.border-2.border-gray-400.relative
    {:class (if active
              "bg-blue-500.translate-x-full"
              "bg-gray-400")}]
   [:span (toggle-text active on-text off-text)]])
