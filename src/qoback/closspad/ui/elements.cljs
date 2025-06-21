(ns qoback.closspad.ui.elements
  (:require [replicant.alias :refer [defalias]]))

(def w->h
  {:w-4 :h-4
   :w-6 :h-6
   :w-8 :h-8
   :w-12 :h-12
   :w-16 :h-16})

(defalias card-title
  [attrs body]
  [:h2.text-base.font-bold.flex.gap-2.items-start attrs body])

(defalias card-details
  [attrs body]
  [:div.text-base attrs body])

(defalias link-item
  [{:keys [route action] :as attr} body]
  [:a.text-gray-700.hover:text-gray-900.font-medium.transition-all.duration-200.ease-in-out
   {:href (str "/#/" route)
    :class ["hover:scale-105" "hover:underline" "hover:underline-offset-4"]
    :on {:click [action]}}
   body])

(defalias icon
  [attrs path]
  (let [h (w->h (:width attrs))]
    [:svg {:xmlns "http://www.w3.org/2000/svg"
           :class [(:width attrs) h]
           :fill "none"
           :viewBox "0 0 24 24"
           :stroke "currentColor"}
     [:path {:stroke-linecap "round"
             :stroke-linejoin "round"
             :stroke-width "2"
             :d (first path)}]]))

(defalias right-arrow-icon
  [attrs]
  [icon attrs "M7 5 l7 7 -7 7 M16 5 l7 7 -7 7"])

(defalias left-arrow-icon
  [attrs]
  [icon attrs "M19 5 l-7 7 7 7 M10 5 l-7 7 7 7"])

