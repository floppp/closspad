# Sistema de Puntuación de Pádel - Explicación Completa

## Fundamentos Matemáticos

### 1. Probabilidad de Victoria (P)
Fórmula:
```
P = 1 / (1 + 10^((PuntosEquipoB - PuntosEquipoA)/200)
```
Ejemplo:  
Equipo A (120 puntos) vs Equipo B (100 puntos)  
```
P = 1 / (1 + 10^((100-120)/200)) = 1 / (1 + 10^-0.1) ≈ 56%
```


### 2. Cálculo de Cambio de Puntos
Fórmula base:
```
Cambio = K * (Resultado - P)
```
Donde:
- K = 10 (valor base)
- Resultado = 1 (ganó) o 0 (perdió)
- P = Probabilidad calculada

### 3. Factores de Ajuste

#### Normalización de Puntos
Todos los puntos se normalizan al rango 0-1 usando:
```
PuntosNormalizados = (PuntosActuales - PuntosMínimos) / (PuntosMáximos - PuntosMínimos)
```
Donde:
- PuntosMínimos = 0
- PuntosMáximos = 100

#### a) Factor Compañero
```
0.7 + (0.5 - PuntosNormalizadosCompañero) * 0.6
```
- Si compañero tiene 50 puntos → 0.7
- Si compañero tiene 75 puntos → 0.55
- Si compañero tiene 25 puntos → 0.85

#### b) Factor Rival
``` 
0.6 + PuntosNormalizadosRival * 0.8
``` 
- Si rival tiene 50 puntos → 1.0
- Si rival tiene 75 puntos → 1.2
- Si rival tiene 25 puntos → 0.8

#### c) Factor Rendimiento
```
1.1 - (PuntosNormalizadosJugador - 0.5) * 0.4
```
- Si jugador tiene 50 puntos → 1.1
- Si jugador tiene 75 puntos → 1.0
- Si jugador tiene 25 puntos → 1.2

#### d) Factor Proximidad
```
2 * min(
  (PuntosMáximos - PuntosJugador)/PuntosMáximos,
  PuntosJugador/PuntosMáximos
)
```
- Si jugador tiene 50 puntos → 1.0
- Si jugador tiene 90 puntos → 0.2
- Si jugador tiene 10 puntos → 0.2

## Ejemplos Detallados

### Ejemplo 1: Partido Equilibrado
- **Jugadores**: Todos con 60 puntos
- **Probabilidad**: 50% para cada equipo
- **Resultado**: Equipo A gana 6-4, 6-4
- **Cálculo**:
  Cambio = 10 * (1 - 0.5) = 5 puntos
- **Ajuste Final**: 
  - Ganadores: 60 → 65 (+5)
  - Perdedores: 60 → 55 (-5)

### Ejemplo 2: Sorpresa
- **Equipo A**: 40+40 = 80 puntos
- **Equipo B**: 60+60 = 120 puntos  
- **Probabilidad**: 15% para Equipo A
- **Resultado**: Equipo A gana
- **Cálculo**:
  Cambio base = 10 * (1 - 0.15) = 8.5
  Factores aplicados → +12 puntos
- **Ajuste Final**:
  - Equipo A: 40 → 52 (+12)
  - Equipo B: 60 → 48 (-12)

### Ejemplo 3: Jugador Nuevo
- **Equipo A**: 70 (exp) + 50 (nuevo)
- **Equipo B**: 60 + 60
- **Probabilidad**: 45% Equipo A
- **Resultado**: Equipo B gana
- **Cálculo**:
  - Jugador nuevo (K=11): Cambio = 11 * (0 - 0.45) = -4.95
  - Factores aplicados → -10 puntos
- **Ajuste Final**:
  - Jugador nuevo: 50 → 40 (-10)
  - Experto: 70 → 62 (-8)

### Ejemplo 4: Torneo Importante
- **Importancia**: 1.5x
- **Equipo A**: 80 + 75 = 155
- **Equipo B**: 70 + 70 = 140
- **Probabilidad**: 65% Equipo A
- **Resultado**: Equipo A gana
- **Cálculo**:
  Cambio base = 10 * (1 - 0.65) = 3.5
  Con importancia → 3.5 * 1.5 ≈ 5.25
  Factores aplicados → +6/+7 puntos
- **Ajuste Final**:
  - 80 → 86 (+6)
  - 75 → 82 (+7)
  - Rivales: 70 → 61 (-9)

### Ejemplo 5: Jugadores Top
- **Equipo A**: 95 + 90 = 185
- **Equipo B**: 50 + 50 = 100
- **Probabilidad**: 92% Equipo A
- **Resultado**: Equipo A gana
- **Cálculo**:
  Cambio base = 10 * (1 - 0.92) = 0.8
  Factor proximidad → 0.8 * 0.5 = 0.4
  Redondeo → +1 punto
- **Ajuste Final**:
  - 95 → 96 (+1)
  - 90 → 92 (+2)
  - Rivales: 50 → 47 (-3)

## Reglas Especiales
1. **Límites**: Puntos entre 0-100
2. **Volatilidad**:
   - Nuevos: 1.1
   - Decrece 1% por partido
   - Mínimo: 0.8
3. **Inactividad**: -2% por periodo inactivo
4. **Importancia**: Multiplicador 1-2x
