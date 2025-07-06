(ns qoback.closspad.pages.changelog.view)

(defn view
  [state]
  [:div.bg-white.rounded-lg.shadow-md.p-6.mx-auto.w-full
   [:div.mb-10
    [:h3.text-xl.font-medium.text-gray-700.mb-3.text-right
     "06/07/2025"]
    [:h4.text-2xl.font-bold.text-blue-400.mb-3.mt-6 "FEATURES"]
    [:ul.list-disc.list-inside.space-y-2.ml-4
     [:li "Distintos tipos de partidos, los puntos que se ponen en juego son más a mayor importancia: promesas, regular, p2, p1, major"
]]]

   [:hr]
   [:div.mt-5.mb-10
    [:h3.text-xl.font-medium.text-gray-700.mb-3.text-right
     "25/06/2025"]
    [:h4.text-2xl.font-bold.text-blue-400.mb-3.mt-6 "FEATURES"]
    [:ul.list-disc.list-inside.space-y-2.ml-4
     [:li "Simulador, por ahora muy básico, de partidos. Incluye probabilidad de victoria por pareja e histórico de partidos jugados por la pareja y enfrentamientos previos"]]]

   [:hr]
   [:div.mt-5.mb-10
    [:h3.text-xl.font-medium.text-gray-700.mb-3.text-right
     "17/06/2025"]
    [:h4.text-2xl.font-bold.text-blue-400.mb-3.mt-6 "FEATURES"]
    [:ul.list-disc.list-inside.space-y-2.ml-4
     [:li "Click en partido abre características del mismo y variación de puntos debido a él"
      [:p.pl-10 "El estilo está WIP"]]]

    [:h4.text-2xl.font-bold.text-red-400.mb-3.mt-6 "BUGS"]
    [:ul.list-disc.list-inside.space-y-2.ml-4
     [:li "Corregido BUG que daba 0 puntos a un nuevo jugador al calcular el rating de la pareja, por lo que"
      [:ol.list-decimal.list-inside.ml-6.mt-2.space-y-1
       [:li "la probabilidad de victoria de la pareja con un nuevo jugador era muy baja"]
       [:li "distorsionaba muchísimo los puntos ganados/perdidos"]
       [:li "si se ganaba a una pareja con nuevo jugador se obtenían pocos puntos"]
       [:li "si se perdía, se perdían pocos."]
       [:li "por contra, si se ganaba con alguien nuevo la subida era enorme"]]]]]])
