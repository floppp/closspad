(ns qoback.closspad.components.system-explanation)

(defn component
  []
  [:div.bg-white.rounded-lg.shadow-md.p-6.max-w-4xl.mx-auto.w-full
   [:h1.text-3xl.font-bold.text-gray-800.mb-6
    "Sistema de Puntuación de Pádel"]

   [:h3.text-xl.font-medium.text-gray-700.mt-6.mb-3
    "1. Probabilidad de Victoria (P)"]
   [:p.text-gray-600.mb-2 "Fórmula:"]
   [:pre.bg-gray-100.p-4.rounded.mb-4.overflow-x-auto
    [:code.text-sm.font-mono "P = 1 / (1 + 10^((PuntosEquipoB - PuntosEquipoA)/FactorEscala)"]]
   [:p.text-gray-600.mb-2 "Donde FactorEscala = 100 (valor configurable)"]
   [:p.text-gray-600.mb-2 "Ejemplo:" [:br] "Equipo A (120 puntos) vs Equipo B (100 puntos)"]
   [:pre.bg-gray-100.p-4.rounded.mb-4.overflow-x-auto
    [:code.text-sm.font-mono "P = 1 / (1 + 10^((100-120)/100)) = 1 / (1 + 10^-0.2) ≈ 60%"]]

   [:h3.text-xl.font-medium.text-gray-700.mt-6.mb-3
    "2. Cálculo de Cambio de Puntos"]
   [:p "Fórmula base:"]
   [:pre.bg-gray-100.p-4.rounded.mb-4.overflow-x-auto
    [:code.text-sm.font-mono "Cambio = K * (Resultado - P)"]]
   [:p.text-gray-600.mb-2 "Donde:"]
   [:ul.list-disc.list-inside.pl-4.mb-4.space-y-1
    [:li.text-gray-700 "K = BaseK (20) * Volatilidad (1.1 inicial)"]
    [:li.text-gray-700 "Resultado = 1 (ganó) o 0 (perdió)"]
    [:li.text-gray-700 "P = Probabilidad calculada"]]

   [:h3 "3. Factores de Ajuste"]

   [:h4.text-lg.font-medium.text-gray-700.mt-4.mb-2
    "Normalización de Puntos"]
   [:p.text-gray-600.mb-2 "Todos los puntos se normalizan al rango 0-1 usando:"]
   [:pre.bg-gray-100.p-4.rounded.mb-4.overflow-x-auto
    [:code.text-sm.font-mono "Numerador = PuntosActuales - PuntosMínimos"]
    [:br]
    [:code.text-sm.font-mono "Denominador = PuntosMáximos - PuntosMínimos"]
    [:br]
    [:code.text-sm.font-mono "PuntosNormalizados = Numerador / Denominador"]]
   [:p.text-gray-600.mb-2 "Donde:"]
   [:ul.list-disc.list-inside.pl-4.mb-4.space-y-1
    [:li.text-gray-700 "PuntosMínimos = 0"]
    [:li.text-gray-700 "PuntosMáximos = 100"]]

   [:h4.text-lg.font-medium.text-gray-700.mt-4.mb-2
    "a) Factor Compañero"]
   [:pre.bg-gray-100.p-4.rounded.mb-4.overflow-x-auto
    [:code.text-sm.font-mono "0.7 + (0.5 - PuntosNormalizadosCompañero) * 0.6"]]
   [:ul.list-disc.list-inside.pl-4.mb-4.space-y-1
    [:li.text-gray-700 "Si compañero tiene 75 puntos → 0.55"]
    [:li.text-gray-700 "Si compañero tiene 50 puntos → 0.7"]
    [:li.text-gray-700 "Si compañero tiene 25 puntos → 0.85"]]

   [:h4.text-lg.font-medium.text-gray-700.mt-4.mb-2
    "b) Factor Rival"]
   [:pre.bg-gray-100.p-4.rounded.mb-4.overflow-x-auto
    [:code.text-sm.font-mono "0.6 + PuntosNormalizadosRival * 0.8"]]
   [:ul.list-disc.list-inside.pl-4.mb-4.space-y-1
    [:li.text-gray-700 "Si rival tiene 75 puntos → 1.2"]
    [:li.text-gray-700 "Si rival tiene 50 puntos → 1.0"]
    [:li.text-gray-700 "Si rival tiene 25 puntos → 0.8"]]

   [:h4.text-lg.font-medium.text-gray-700.mt-4.mb-2
    "c) Factor Rendimiento"]
   [:pre.bg-gray-100.p-4.rounded.mb-4.overflow-x-auto
    [:code.text-sm.font-mono "1.1 - (PuntosNormalizadosJugador - 0.5) * 0.4"]]
   [:ul.list-disc.list-inside.pl-4.mb-4.space-y-1
    [:li.text-gray-700 "Si jugador tiene 75 puntos → 1.0"]
    [:li.text-gray-700 "Si jugador tiene 50 puntos → 1.1"]
    [:li.text-gray-700 "Si jugador tiene 25 puntos → 1.2"]]

   [:h4.text-lg.font-medium.text-gray-700.mt-4.mb-2
    "d) Factor Proximidad"]
   [:pre.bg-gray-100.p-4.rounded.mb-4.overflow-x-auto
    [:code.text-sm.font-mono "2 * min(\n  (PuntosMáximos - PuntosJugador)/PuntosMáximos,\n  PuntosJugador/PuntosMáximos\n)"]]
   [:ul.list-disc.list-inside.pl-4.mb-4.space-y-1
    [:li.text-gray-700 "Si jugador tiene 90 puntos → 0.2"]
    [:li.text-gray-700 "Si jugador tiene 50 puntos → 1.0"]
    [:li.text-gray-700 "Si jugador tiene 10 puntos → 0.2"]]

   [:h2.text-2xl.font-semibold.text-gray-700.mt-8.mb-4
    "Ejemplos Detallados"]

   [:h3.text-xl.font-medium.text-gray-700.mt-6.mb-3
    "Ejemplo 1: Partido Equilibrado"]
   [:ul.list-disc.list-inside.pl-4.mb-6.space-y-2.bg-gray-50.p-4.rounded-lg
    [:li.text-gray-700 [:strong.font-medium "Jugadores:"] " Todos con 60 puntos"]
    [:li.text-gray-700 [:strong.font-medium "Probabilidad:"] " 50% para cada equipo"]
    [:li.text-gray-700 [:strong.font-medium "Resultado:"] " Equipo A gana 6-4, 6-4"]
    [:li.text-gray-700
     [:strong.font-medium "Cálculo:"]
     [:pre.bg-gray-100.p-2.rounded.inline-block.ml-2
      "Cambio = 10 * (1 - 0.5) = 5 puntos"]]
    [:li.text-gray-700 [:strong.font-medium "Ajuste Final:"] " "
     [:ul.list-disc.list-inside.pl-4.space-y-1
      [:li.text-gray-700 "Ganadores: " [:span.font-medium "60 → 65 (+5)"]]
      [:li.text-gray-700 "Perdedores: " [:span.font-medium "60 → 55 (-5)"]]]]]

   [:h3.text-xl.font-medium.text-gray-700.mt-6.mb-3 "Ejemplo 2: Sorpresa"]
   [:ul.list-disc.list-inside.pl-4.mb-6.space-y-2.bg-gray-50.p-4.rounded-lg
    [:li.text-gray-700 [:strong.font-medium "Equipo A"] ": 40+40 = 80 puntos"]
    [:li.text-gray-700 [:strong.font-medium "Equipo B"] ": 60+60 = 120 puntos"]
    [:li.text-gray-700 [:strong.font-medium "Probabilidad"] ": 15% para Equipo A"]
    [:li.text-gray-700 [:strong.font-medium "Resultado"] ": Equipo A gana"]
    [:li.text-gray-700 [:strong.font-medium "Cálculo:"]
     [:br]
     [:pre.bg-gray-100.p-2.rounded.inline-block.ml-2 "Cambio base = BaseK * (1 - 0.15) = 8.5"]
     [:br]
     [:pre.bg-gray-100.p-2.rounded.inline-block.ml-2 "Factores aplicados → +12 puntos"]]
    [:li.text-gray-700 [:strong.font-medium "Ajuste Final"] ":"
     [:ul.list-disc.list-inside.pl-4.mb-6.space-y-2.bg-gray-50.p-4.rounded-lg
      [:li.text-gray-700 "Equipo A: 40 → 52 (+12)"]
      [:li.text-gray-700 "Equipo B: 60 → 48 (-12)"]]]]

   [:h3.text-xl.font-medium.text-gray-700.mt-6.mb-3 "Ejemplo 3: Jugador Nuevo"]
   [:ul.list-disc.list-inside.pl-4.mb-6.space-y-2.bg-gray-50.p-4.rounded-lg
    [:li.text-gray-700 [:strong.font-medium "Equipo A"] ": 70 (exp) + 50 (nuevo)"]
    [:li.text-gray-700 [:strong.font-medium "Equipo B"] ": 60 + 60"]
    [:li.text-gray-700 [:strong.font-medium "Probabilidad"] ": 45% Equipo A"]
    [:li.text-gray-700 [:strong.font-medium "Resultado"] ": Equipo B gana"]
    [:li.text-gray-700 [:strong.font-medium "Cálculo"] ":"
     [:ul.list-disc.list-inside.pl-4.mb-6.space-y-2.bg-gray-50.p-4.rounded-lg
      [:li.text-gray-700 "Jugador nuevo (K=11):"
       [:pre.bg-gray-100.p-2.rounded.inline-block.ml-2
        "Cambio = 11 * (0 - 0.45) = -4.95"]]
      [:li.text-gray-700 "Factores aplicados → -10 puntos"]]
     [:li.text-gray-700 [:strong.font-medium "Ajuste Final"] ":"
      [:ul.list-disc.list-inside.pl-4.mb-6.space-y-2.bg-gray-50.p-4.rounded-lg
       [:li.text-gray-700 "Jugador nuevo: 50 → 40 (-10)"]
       [:li.text-gray-700 "Experto: 70 → 62 (-8)"]]]]]

   [:h3.text-xl.font-medium.text-gray-700.mt-6.mb-3 "Ejemplo 4: Jugadores Top"]
   [:ul.list-disc.list-inside.pl-4.mb-6.space-y-2.bg-gray-50.p-4.rounded-lg
    [:li.text-gray-700 [:strong.font-medium "Equipo A"] ": 95 + 90 = 185"]
    [:li.text-gray-700 [:strong.font-medium "Equipo B"] ": 50 + 50 = 100"]
    [:li.text-gray-700 [:strong.font-medium "Probabilidad"] ": 92% Equipo A"]
    [:li.text-gray-700 [:strong.font-medium "Resultado"] ": Equipo A gana"]
    [:li.text-gray-700 [:strong.font-medium "Cálculo:"]
     [:br]
     [:pre.bg-gray-100.p-2.rounded.inline-block.ml-2
      "Cambio base = 10 * (1 - 0.92) = 0.8"]
     [:br]
     [:pre.bg-gray-100.p-2.rounded.inline-block.ml-2
      "Factor proximidad → 0.8 * 0.5 = 0.4"]
     [:br]
     [:pre.bg-gray-100.p-2.rounded.inline-block.ml-2
      "Redondeo → +1 punto"]]
    [:li.text-gray-700 [:strong.font-medium "Ajuste Final"] ":"
     [:ul.list-disc.list-inside.pl-4.mb-6.space-y-2.bg-gray-50.p-4.rounded-lg
      [:li.text-gray-700 "95 → 96 (+1)"]
      [:li.text-gray-700 "90 → 92 (+2)"]
      [:li.text-gray-700 "Rivales: 50 → 47 (-3)"]]]]

   [:h2.text-xl.font-medium.text-gray-700.mt-6.mb-3
    "Reglas Especiales"]
   [:ol.list-inside.pl-4.mb-6.space-y-2.bg-gray-50.p-4.rounded-lg
    [:li [:strong "Límites"] ": Puntos entre 0-100"]
    [:li [:strong "Partido de 1 Set"] ": Se minoran con un 0.8"]
    [:li [:strong "Volatilidad"] ":"
     [:ul.list-disc.list-inside.pl-4.mb-6.space-y-2.bg-gray-50.p-4.rounded-lg
      [:li "Nuevos: 1.1"]
      [:li "Decrece 1% por partido (multiplicado por 0.99)"]
      [:li "Mínimo: 0.8"]
      [:li "Afecta directamente al factor K (K = BaseK * volatilidad)"]]]
    [:li [:strong "Decaimiento Temporal"] ":"
     [:ul.list-disc.list-inside.pl-4.mb-6.space-y-2.bg-gray-50.p-4.rounded-lg
      [:li "El valor de cada partido decae con el tiempo"]
      [:li "Fórmula: ((días desde el que se jugó el partido hasta el último) / 300) * valor del partido"]
      [:li "Ejemplo: Si un partido fue hace 150 días, su valor se reduce a la mitad"]]]
    [:li [:strong "Importancia"] ": Multiplicador 1-2x"]]])
