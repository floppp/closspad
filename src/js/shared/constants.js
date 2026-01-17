// Shared constants for both rating systems
module.exports = {
  // Importance values from existing ClojureScript app
  IMPORTANCE_VALUES: {
    major: 2.0,
    p1: 1.0,
    p2: 0.6,
    regular: 0.3,
    promises: 0.1
  },
  
  // Current Elo system defaults for backward compatibility
  ELO_DEFAULTS: {
    importance: 1.0,
    defaultRating: 50,
    maxRating: 100,
    minRating: 0,
    baseK: 25,
    scaleFactor: 120,
    oneSetImportance: 0.6
  },
  
  // ATP system defaults
  ATP_DEFAULTS: {
    rollingWindowWeeks: 52,
    basePointsPerWin: 10,
    startingPoints: 0
  },
  
  // System types
  SYSTEM_TYPES: {
    ELO: 'elo',
    ATP: 'atp'
  }
};