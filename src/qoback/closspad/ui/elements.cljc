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
  (let [attrs (merge {:width "w-6"} attrs)
        h (w->h (:width attrs))]
    [:svg {:xmlns "http://www.w3.org/2000/svg"
           :class [(:width attrs) h]
           :fill "none"
           :viewBox "0 0 24 24"
           :stroke "currentColor"}
     [:path {:stroke-linecap "round"
             :stroke-linejoin "round"
             :stroke-width "2"
             :d (first path)}]]))

(defalias right-double-arrow-icon
  [attrs]
  [icon attrs "M7 5 l7 7 -7 7 M16 5 l7 7 -7 7"])

(defalias right-arrow-icon
  [attrs]
  [icon attrs "M9 5l7 7-7 7"])

(defalias minus-icon
  [attrs]
  [icon attrs "M5 12h14"])

(defalias plus-icon
  [attrs]
  [icon attrs "M12 5v14M5 12h14"])


(defalias left-double-arrow-icon
  [attrs]
  [icon attrs "M19 5 l-7 7 7 7 M10 5 l-7 7 7 7"])

(defalias left-arrow-icon
  [attrs]
  [icon attrs "M15 19l-7-7 7-7"])

(defalias link-icon
  [attrs body]
  [:a
   (cond-> attrs
     (:href attrs)
     (update :class
             #(into
               (or % [])
               ["hover:text-gray-500" "hover:scale-110" "transition-all" "duration-200" "easy-in-out"])))
   body])

(defalias spinner
  [{:keys [style] :or {style {:height "100px" :width "100px"}}}]
  [:div.w-full.flex.justify-center.mt-5.py-5
   {:style {:background "white"}}
   [:span.loading.loading-ring.loading-xl
    {:style style}]])

(defalias error-toast
  [{:keys [error]}]
  (if error
    [:div.toast
     [:div.alert.alert-error
      {:on {:click [[:db/dissoc :error]]}}
      [:span error]]]
    [:span.hidden]))
