(ns qoback.closspad.components.widgets
  (:require [qoback.closspad.helpers :as h]
            [qoback.closspad.utils.datetime :as dt]
            [qoback.closspad.ui.elements :as ui]))

(defn header-item
  [is-open? title route]
  [:a.text-gray-700.hover:text-gray-900.font-medium.transition-all.duration-200.ease-in-out
   {:href (str "/#/" route)
    :class ["hover:scale-105" "hover:underline" "hover:underline-offset-4"]
    :style (when is-open? {:text-align "center"})
    :on {:click [[:ui/header]]}}
   title])

(defn header
  [{:ui/keys [header] :as state}]
  (let [user-email (get-in state [:auth :user :email])
        is-logged? (not (nil? user-email))]
    [:div.bg-white.rounded-lg.shadow-md.p-4.sticky.top-0.mb-5
     {:class ["w-full"
              "md:w-3/4"
              "lg:w-2/3"
              "xl:w-1/2"
              "2xl:w-1/3"]}
     [:div.flex.items-center.md:hidden
      {:class (when header "justify-center")}
      [:button.text-gray-700.hover:text-gray-900
       {:on {:click [[:ui/header]]} :aria-label "Toggle menu"}
       [:svg.w-6.h-6
        {:fill "none" :viewBox "0 0 24 24" :stroke "currentColor"}
        [:path {:stroke-linecap "round" :stroke-linejoin "round" :stroke-width "2"
                :d (if header
                     "M6 18L18 6M6 6l12 12"
                     "M4 6h16M4 12h16M4 18h16")}]]]]

   ;; Hago así en vez de `if` para evitar montar/desmontar el header normal.
   ;; Que no es gran ganancia porque nadie estará abre/cierra, pero me
   ;; parece bueno tenerlo en cuenta para acordarme en otras situaciones.
     [:div.hidden.md:flex.flex-grow.gap-8.items-center.justify-center
      (header-item header "Inicio" "")
      (header-item header "Explicación" "explanation")
      (header-item header "Estadísticas" "stats")
      (header-item header "Cambios" "changelog")
      (if is-logged?
        (header-item header "Añadir Partido" "add-match")
        (header-item header "Login" "login"))]

     (when header
       [:div.overflow-hidden.transition-all.duration-300.ease-in-out
        {:class (if header "max-h-96" "max-h-0")}
        [:div.flex.flex-col.gap-4.p-4.items-center.md:hidden
         (header-item header "Inicio" "")
         (header-item header "Explicación" "explanation")
         (header-item header "Estadísticas" "stats")
         (header-item header "Cambios" "changelog")
         (if is-logged?
           (header-item header "Añadir Partido" "add")
           (header-item header "Login" "login"))]])]))

(defn- arrow-button
  [path date]
  #_(let [date (cb)])
  [:a
   {:href (when date (str "#/match/" date))
    :class (when-not date "cursor-not-allowed")}
   [:svg {:xmlns "http://www.w3.org/2000/svg" :class ["h-6" "w-6"] :fill "none" :viewBox "0 0 24 24" :stroke "currentColor"}
    [:path {:stroke-linecap "round" :stroke-linejoin "round" :stroke-width "2" :d path}]]])

(defn- date-only [^js/Date js-date]
  (doto (js/Date. (.getTime js-date))
    (.setHours 0 0 0 0)))

(defn add-day [current-date match-dates]
  (let [current-day (date-only current-date)]
    (some #(when (> (.getTime %) (.getTime current-day)) %)
          (->> match-dates
               (map date-only)
               (sort-by #(.getTime %))))))

(defn arrow-right
  [date match-dates]
  (let [date (when-let [day (add-day date match-dates)]
               (h/format-iso-date day))]
    [ui/button-icon
     {:class (when-not date "cursor-not-allowed")
      :href (when date (str "#/match/" date))}
     [ui/right-arrow-icon {:width "w-6"}]]))

(defn- arrow-left
  [date match-dates]
  (let [date (when-let [prev-date-with-match (last (filter #(< % date) match-dates))]
               (h/format-iso-date prev-date-with-match))]
    [ui/button-icon
     {:class (when-not date "cursor-not-allowed")
      :href (when date (str "#/match/" date))}
     [ui/left-arrow-icon {:width "w-6"}]]))

(defn- double-arrow-right
  [match-dates]
  (let [date (h/format-iso-date (last match-dates))]
    [ui/button-icon
     {:class (when-not date "cursor-not-allowed")
      :href (when date (str "#/match/" date))}
     [ui/right-double-arrow-icon {:width "w-6"}]]))

(defn- double-arrow-left
  [match-dates]
  (let [date (h/format-iso-date (first match-dates))]
    [ui/button-icon
     {:class (when-not date "cursor-not-allowed")
      :href (when date (str "#/match/" date))}
     [ui/left-double-arrow-icon {:width "w-6"}]]))

(defn arrow-selector
  [date match-dates]
  [:div.flex.justify-center.gap-3.items-center.w-full
   {:class ["max-w-[400px]" "sm:items-start"]}
   (double-arrow-left match-dates)
   (arrow-left date match-dates)

   [:div
    {:class
     ["text-lg" "font-semibold" "min-w-120" "text-center" "sm:text-xl"]}
    (dt/datetime->date->str date)]

   (arrow-right date match-dates)

   (double-arrow-right match-dates)])



