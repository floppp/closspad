(ns qoback.closspad.components.system-explanation)

(def inactivity
  [:div
   [:h3.text-xl.font-medium.text-gray-700.mt-6.mb-3
    "Decaimiento por Inactividad"]
   [:p.text-gray-600.mb-2 "Fórmula implementada:"]
   [:pre.bg-gray-100.p-4.rounded.mb-4.overflow-x-auto
    [:code.text-sm.font-mono
     "// Sin decaimiento si ≤ 1 mes inactivo\n"
     "if (monthsDiff <= 1) return 0;\n\n"
     "// Decaimiento logarítmico después\n"
     "decayPercentage =\n"
     "   monthsDiff < 5  ~> 0.04 * (log10(1 + (9 * monthsDiff / 4))\n"
     "   monthsDiff >= 5 ~> 0.04"]]
      [:ul.list-disc.list-inside.pl-4.mb-4.space-y-1
    [:li.text-gray-700 "2 meses inactivo: ~1.6% de puntos perdidos"]
    [:li.text-gray-700 "4 meses inactivo: 4% de puntos perdidos"]
    [:li.text-gray-700 "6 meses inactivo: 4% (se mantiene el tope)"]]])

(def victory-options
  [:div
   [:h3.text-xl.font-medium.text-gray-700.mt-6.mb-3
    "1. Probabilidad de Victoria (P)"]
   [:p.text-gray-600.mb-2 "Fórmula:"]
   [:pre.bg-gray-100.p-4.rounded.mb-4.overflow-x-auto
    [:code.text-sm.font-mono "P = 1 / (1 + 10^((PuntosEquipoB - PuntosEquipoA)/FactorEscala)"]]
   [:p.text-gray-600.mb-2 "Donde FactorEscala = 100 (valor configurable)"]
   [:p.text-gray-600.mb-2 "Ejemplo:" [:br] "Equipo A (120 puntos) vs Equipo B (100 puntos)"]
   [:pre.bg-gray-100.p-4.rounded.mb-4.overflow-x-auto
    [:code.text-sm.font-mono "P = 1 / (1 + 10^((100-120)/100)) = 1 / (1 + 10^-0.2) ≈ 60%"]]])

(def change-probability
  [:div
   [:h3.text-xl.font-medium.text-gray-700.mt-6.mb-3
    "2. Cálculo de Cambio de Puntos"]
   [:p "Fórmula base:"]
   [:pre.bg-gray-100.p-4.rounded.mb-4.overflow-x-auto
    [:code.text-sm.font-mono "Cambio = K * (Resultado - P)"]]
   [:p.text-gray-600.mb-2 "Donde:"]
   [:ul.list-disc.list-inside.pl-4.mb-4.space-y-1
    [:li.text-gray-700 "K = BaseK * Volatilidad (25 * 1.1 inicialmente)"]
    [:li.text-gray-700 "Resultado = 1 (ganó) o 0 (perdió)"]
    [:li.text-gray-700 "P = Probabilidad calculada"]]])

(def volatility
  [:div
   [:h3.text-xl.font-medium.text-gray-700.mt-6.mb-3
    "Sistema de Volatilidad"]
   [:p.text-gray-600.mb-2 "Comportamiento actual:"]
   [:pre.bg-gray-100.p-4.rounded.mb-4.overflow-x-auto
    [:code.text-sm.font-mono
     "// Volatilidad decae con cada partido jugado\n"
     "newVolatility = volatility * (1 - 0.02)^matchCount\n"
     "// Mínimo permitido: 0.85\n\n"
     "// Jugadores inactivos 3+ meses reciben boost\n"
     "if (monthsInactive >= 3) {\n"
     "  newVolatility = min(volatility * 1.15, 1.3)\n"
     "}"]]
   [:p.text-gray-600.mb-2 "Rango efectivo de K:"]
   [:ul.list-disc.list-inside.pl-4.mb-4.space-y-1
    [:li.text-gray-700 "Mínimo: 25 * 0.85 = 21.25 (jugadores estables)"]
    [:li.text-gray-700 "Máximo: 25 * 1.3 = 32.5 (jugadores inactivos)"]]

   [:p.text-gray-600.mb-2 "Impacto en el sistema:"]
   [:ul.list-disc.list-inside.pl-4.mb-4.space-y-1
    [:li.text-gray-700 "Jugadores nuevos/inactivos tienen mayor volatilidad (K más alto)"]
    [:li.text-gray-700 "Jugadores estables ven cambios más pequeños en su puntuación"]
    [:li.text-gray-700 "Cambios grandes de puntos aumentan temporalmente la volatilidad"]
    [:li.text-gray-700 "Rango efectivo de K: 21.25 (estable) a 50 (máxima volatilidad)"]]])

(def examples
  [:div
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

   [:h3.text-xl.font-medium.text-gray-700.mt-6.mb-3 "Ejemplo 4: Jugadores Top con Volatilidad"]
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
      [:li.text-gray-700 "Jugador 1 (95 pts, volatilidad 0.9): 95 → 96 (+1)"]
      [:li.text-gray-700 "Jugador 2 (90 pts, volatilidad 1.3): 90 → 93 (+3)"]
      [:li.text-gray-700 "Rivales (50 pts): 50 → 47 (-3)"]]]
    [:li.text-gray-700 [:strong.font-medium "Notas"] ":"
     [:ul.list-disc.list-inside.pl-4.mb-6.space-y-2.bg-gray-50.p-4.rounded-lg
      [:li "El jugador con mayor volatilidad (1.3) recibe mayor ajuste"]
      [:li "El K efectivo fue 25 * 1.3 = 32.5 para el segundo jugador"]
      [:li "Se aplicó factor de proximidad (0.2) por estar cerca del máximo"]]]]

   [:div
    [:h3.text-xl.font-medium.text-gray-700.mt-6.mb-3
     "Equipo Mixto (experto + nuevo)"]
    [:ul.list-disc.list-inside.pl-4.mb-6.space-y-2.bg-gray-50.p-4.rounded-lg
     [:li.text-gray-700 [:strong.font-medium "Equipo A"] ": 70 (exp, volat=0.9) + 50 (nuevo, volat=1.1)"]
     [:li.text-gray-700 [:strong.font-medium "Equipo B"] ": 60 + 60 (ambos volat=1.0)"]
     [:li.text-gray-700 [:strong.font-medium "Probabilidad"] ": 45% Equipo A"]
     [:li.text-gray-700 [:strong.font-medium "Resultado"] ": Equipo A gana"]
     [:li.text-gray-700 [:strong.font-medium "Cálculos"] ":"
      [:ul.list-disc.list-inside.pl-4.mb-6.space-y-2.bg-gray-50.p-4.rounded-lg
       [:li.text-gray-700 "Base: 25 * (1 - 0.45) = 13.75"]
       [:li.text-gray-700 "Surprise: (0.5 - 0.45) * 25 * 0.5 = 0.625"]
       [:li.text-gray-700 "Pesos: √70/(√70+√50) ≈ 0.54, √50/(√70+√50) ≈ 0.46"]]
      [:li.text-gray-700 [:strong.font-medium "Ajuste Final"] ":"
       [:ul.list-disc.list-inside.pl-4.mb-6.space-y-2.bg-gray-50.p-4.rounded-lg
        [:li.text-gray-700 "Experto: (13.75*0.54 + 0.625*0.54) + (1-0.54)*2 ≈ +9"]
        [:li.text-gray-700 "Nuevo: (13.75*0.46 + 0.625*0.46) + (1-0.46)*2 ≈ +8"]
        [:li.text-gray-700 "Rivales: -25 * (0 - 0.55) ≈ -14 (repartido)"]]]]]]])

(def implementation-notes
  [:div.mt-8.p-4.bg-blue-50.rounded-lg
   [:h3.text-xl.font-medium.text-blue-800 "Notas de Implementación"]
   [:p.text-blue-700 "Este sistema prioriza:"]
   [:ul.list-disc.list-inside.pl-4.space-y-1.text-blue-700
    [:li "Mayor movilidad para jugadores nuevos/inactivos"]
    [:li "Estabilidad para jugadores establecidos"]
    [:li "Bonus por victorias improbables"]
    [:li "Protección contra la inflación de puntos"]
    [:li "Mantenimiento automático del ecosistema"]]])

(defn view
  []
  [:div.bg-white.rounded-lg.shadow-md.p-6.mx-auto.w-full

    [:h1.text-3xl.font-bold.text-gray-800.mb-6
     "Sistema de Puntuación de Pádel"]

    victory-options
    change-probability

    [:h3.text-xl.font-medium.text-gray-700.mt-6.mb-3
     "3. Factores de Ajuste"]
    [:h4.text-lg.font-medium.text-gray-700.mt-4.mb-2
     "Normalización de Puntos"]
    [:p.text-gray-600.mb-2 "El sistema usa diferentes métodos de normalización según el contexto:"]
    [:ul.list-disc.list-inside.pl-4.mb-4.space-y-1
     [:li.text-gray-700
      "Escala lineal (para factores de ajuste):"
      [:pre.bg-gray-100.p-2.rounded.inline-block.ml-2
       "(PuntosActuales - 0) / (100 - 0)"]]
     [:li.text-gray-700
      "Peso por raíz cuadrada (para distribución de puntos):"
      [:pre.bg-gray-100.p-2.rounded.inline-block.ml-2
       "√puntosJugador / Σ√puntosEquipo"]]
     [:li.text-gray-700
      "Proximidad a extremos (para jugadores top/nuevos):"
      [:pre.bg-gray-100.p-2.rounded.inline-block.ml-2
       "min((100-Puntos)/100, Puntos/100) * 2"]]]
    [:p.text-gray-600.mb-2 "Ejemplos:"]
    [:ul.list-disc.list-inside.pl-4.mb-4.space-y-1
     [:li.text-gray-700 "Jugador con 75 puntos → 0.75 (lineal), 0.87 (√), 0.5 (proximidad)"]
     [:li.text-gray-700 "Jugador con 50 puntos → 0.5 (lineal), 0.71 (√), 1.0 (proximidad)"]
     [:li.text-gray-700 "Jugador con 25 puntos → 0.25 (lineal), 0.5 (√), 0.5 (proximidad)"]]

    [:h4.text-lg.font-medium.text-gray-700.mt-4.mb-2
     "Cálculo Real de Variación"]
    [:p.text-gray-600.mb-2 "El sistema calcula la variación de puntos usando:"]
    [:pre.bg-gray-100.p-4.rounded.mb-4.overflow-x-auto
     [:code.text-sm.font-mono
      "// 1. Peso por jugador (√puntos)\n"
      "playerWeight = √puntosJugador / Σ√puntosEquipo\n\n"
      "// 2. Cálculo base\n"
      "baseDelta = importancia * baseK * (resultado - probabilidad)\n\n"
      "// 3. Bonuses (solo si gana)\n"
      "surpriseBonus = max(0, (0.5 - probabilidad)) * baseK * 0.5\n"
      "weakBonus = (1 - playerWeight) * 2\n"
      "underdogBonus = max(0, (puntosRivalPromedio - puntosJugador)) * 0.015\n\n"
      "// 4. Variación final\n"
      "variación = round(baseDelta * playerWeight + surpriseShare) + weakBonus + underdogBonus"]]
    [:p.text-gray-600.mb-2 "Donde:"]
    [:ul.list-disc.list-inside.pl-4.mb-4.space-y-1
     [:li.text-gray-700 "playerWeight: Da más peso a jugadores con más puntos (usando √)"]
     [:li.text-gray-700 "surpriseBonus: Bonus por victorias improbables"]
     [:li.text-gray-700 "weakBonus: Ayuda al jugador más débil del equipo"]
     [:li.text-gray-700 "underdogBonus: Compensa desventaja contra rivales superiores"]]

    [:h4.text-lg.font-medium.text-gray-700.mt-4.mb-2
     "Factor Proximidad"]
    [:pre.bg-gray-100.p-4.rounded.mb-4.overflow-x-auto
     [:code.text-sm.font-mono "2 * min(\n  (PuntosMáximos - PuntosJugador)/PuntosMáximos,\n  PuntosJugador/PuntosMáximos\n)"]]
    [:ul.list-disc.list-inside.pl-4.mb-4.space-y-1
     [:li.text-gray-700 "Si jugador tiene 90 puntos → 0.2"]
     [:li.text-gray-700 "Si jugador tiene 50 puntos → 1.0"]
     [:li.text-gray-700 "Si jugador tiene 10 puntos → 0.2"]]

    [:h3.text-xl.font-medium.text-gray-700.mt-6.mb-3
     "Configuración Base del Sistema"]
    [:ul.list-disc.list-inside.pl-4.mb-4.space-y-1
     [:li.text-gray-700 "Puntuación inicial: 50 puntos"]
     [:li.text-gray-700 "Rango de puntos: 0-100"]
     [:li.text-gray-700 "BaseK: 25 (factor de variabilidad)"]
     [:li.text-gray-700 "FactorEscala: 120"]
     [:li.text-gray-700 "Importancia partido 1 set: 0.6"]]

    [:h4.text-lg.font-medium.text-gray-700.mt-4.mb-2
     "Distribución No Equitativa"]
    [:p.text-gray-600.mb-2 "Fórmula completa de distribución:"]
    [:pre.bg-gray-100.p-4.rounded.mb-4.overflow-x-auto
     [:code.text-sm.font-mono
      "baseDelta = importancia * baseK * (resultado - probabilidad)\n"
      "playerWeight = √puntosJugador / Σ√puntosEquipo\n"
      "surpriseBonus = max(0, (0.5 - probabilidad)) * baseK * 0.5\n"
      "weakBonus = (1 - playerWeight) * 2\n"
      "underdogBonus = max(0, (puntosRivalPromedio - puntosJugador)) * 0.015\n"
      "variaciónFinal = round(baseDelta * playerWeight + surpriseShare) + weakBonus + underdogBonus"]]
    [:p.text-gray-600.mb-2 "Donde:"]
    [:ul.list-disc.list-inside.pl-4.mb-4.space-y-1
     [:li.text-gray-700 "Peso por jugador (√puntos) - Ajusta la distribución según nivel"]
     [:li.text-gray-700 "Bonus por sorpresa - Hasta +12 puntos cuando gana equipo con <50% probabilidad"]
     [:li.text-gray-700 "Bonus para jugador más débil - Hasta +2 puntos adicionales"]
     [:li.text-gray-700 "Bonus underdog - 0.015 puntos por cada punto de diferencia con rival promedio"]
     [:li.text-gray-700 "Todos los bonuses solo aplican al equipo ganador"]]

    inactivity

    volatility

    [:div
     [:h2.text-xl.font-medium.text-gray-700.mt-6.mb-3
      "Reglas Especiales"]
     [:ol.list-inside.pl-4.mb-6.space-y-2.bg-gray-50.p-4.rounded-lg
      [:li [:strong "Límites"] ": Puntos entre 0-100"]
      [:li [:strong "Partido de 1 Set"] ": Se minoran con un 0.6"]
      [:li [:strong "Decaimiento Temporal"] ":"]]]
    examples

    implementation-notes])
