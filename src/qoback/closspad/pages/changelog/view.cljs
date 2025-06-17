(ns qoback.closspad.pages.changelog.view)

(defn view
  [state]
  [:div
   [:p "17/06/2025"
    [:h4 "FEATURES"]
    [:ul
     [:li "Click en partido abre características del mismo y variación de puntos debido a él"]]
    [:h4 "BUGS"]
    [:ul
     [:li "Corregido BUG que daba 0 puntos a un nuevo jugador al calcular puntos de pareja, por lo que"
      [:ol
       [:li "la probabilidad de victoria de la pareja con un nuevo jugador era muy baja"]
       [:li "distorsionaba muchísimo los puntos ganados/perdidos"]
       [:li "si se ganaba a una pareja con nuevo jugador se obtenías pocos puntos"]
       [:li "si se perdía, ser perdían pocos."]]]
     ]]])
