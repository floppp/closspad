(ns qoback.closspad.components.header)

(defn- header-item
  ([is-open? title route] (header-item is-open? title route nil))
  ([is-open? title route actions]
   [:a.text-gray-700.hover:text-gray-900.font-medium.transition-all.duration-200.ease-in-out
    {:href (str "/#/" route)
     :class ["hover:scale-105" "hover:underline" "hover:underline-offset-4"]
     :style (when is-open? {:text-align "center"})
     :on {:click (filter js/Boolean (conj [[:ui/header]] actions))}}
    title]))

(defn header
  [{:ui/keys [header] :as state}]
  (let [user-email (get-in state [:auth :user :email])
        is-logged? (not (nil? user-email))]
    [:div.bg-white.rounded-lg.shadow-md.p-4.sticky.top-0.mb-5
     {:class ["w-full"
              "z-10"
              "md:w-3/4"
              "xl:w-2/3"
              "2xl:w-2/5"]}
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
      (header-item header "Simulador" "forecast")
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
         (header-item header "Simulador" "forecast")
         (header-item header "Cambios" "changelog")
         (if is-logged?
           (header-item header "Añadir Partido" "add-match")
           (header-item header "Login" "login"))]])]))
