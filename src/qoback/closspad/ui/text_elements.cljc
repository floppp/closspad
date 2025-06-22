(ns qoback.closspad.ui.text-elements
  (:require [replicant.alias :refer [defalias]]))

(def w->h
  {:w-4 :h-4
   :w-6 :h-6
   :w-8 :h-8
   :w-12 :h-12
   :w-16 :h-16})

(defalias text-lg-bold [attrs body]
  [:span.font-bold.text-lg.text-right
   attrs
   body])

(defalias text-gray [attrs body]
  [:span.text-gray-500.text-right
   attrs
   body])

(defalias text-dark-gray [attrs body]
  [:span.font-medium.text-gray-800
   attrs
   body])

(defalias title [attrs body]
  [:h2
   {:class (merge ["text-3xl" "font-bold" "text-center" "text-gray-800"]
                  (:class attrs))}
   body])
