// Backward compatibility wrapper for rating systems
// Uses factory pattern to support both Elo and ATP systems
// Defaults to Elo system for backward compatibility

const { createRatingSystem, createEloSystem } = require('./ratingSystemFactory');
const { ELO_DEFAULTS } = require('./shared/constants');

// Create default Elo system for backward compatibility
let currentSystem = createEloSystem();

function createSystem(options = {}) {
    currentSystem = createEloSystem(options);
    return currentSystem.config;
}

// Backward compatibility functions that delegate to current system

function addPlayer(
    system,
    playerId,
    matchDate,
    initialRating = system.defaultRating
) {
    currentSystem.addPlayer(playerId, matchDate, initialRating);
    return currentSystem.config;
}

function updateSystem(system, match) {
    currentSystem.processMatch(match);
    return currentSystem.config;
}

function updateSystemCouple(system, couple, playerVariations, matchDate) {
    // This is an internal function used by Elo system
    // For backward compatibility, we'll use the Elo system implementation
    const { updateSystemCouple: eloUpdateSystemCouple } = require('./ratingSystems/eloSystem');
    return eloUpdateSystemCouple(system, couple, playerVariations, matchDate);
}

function updateSystemCoupleEqualyDistributed(system, couple, pointsChange) {
    // This is an internal function used by Elo system
    // For backward compatibility, we'll use the Elo system implementation
    const { updateSystemCoupleEqualyDistributed: eloUpdateSystemCoupleEqualyDistributed } = require('./ratingSystems/eloSystem');
    return eloUpdateSystemCoupleEqualyDistributed(system, couple, pointsChange);
}


// --- Helper Functions ---
// These functions delegate to the appropriate system implementation

function calculateExpectedWin(system, teamARating, teamBRating) {
    // Only relevant for Elo system
    const { calculateExpectedWin: eloCalculateExpectedWin } = require('./ratingSystems/eloSystem');
    return eloCalculateExpectedWin(system, teamARating, teamBRating);
}

function proximityFactor(system, player) {
    // Only relevant for Elo system
    const { proximityFactor: eloProximityFactor } = require('./ratingSystems/eloSystem');
    return eloProximityFactor(system, player);
}

function getKFactor(system, player) {
    // Only relevant for Elo system
    const { getKFactor: eloGetKFactor } = require('./ratingSystems/eloSystem');
    return eloGetKFactor(system, player);
}

function clampRating(system, rating) {
    // Only relevant for Elo system
    const { clampRating: eloClampRating } = require('./ratingSystems/eloSystem');
    return eloClampRating(system, rating);
}

function determineWinner(result) {
    const { determineWinner: sharedDetermineWinner } = require('./shared/utils');
    return sharedDetermineWinner(result);
}

function getTeamRating(system, couple) {
    // Delegate to appropriate system
    if (currentSystem.type === 'elo') {
        const { getTeamRating: eloGetTeamRating } = require('./ratingSystems/eloSystem');
        return eloGetTeamRating(system, couple);
    } else {
        // ATP system implementation
        return couple.reduce((sum, id) => sum + (system.players[id]?.points ?? 0), 0);
    }
}

function computeVariationPerPlayer(system, couple, coupleRating, otherCoupleRating, isWinner, expectedWin, importance) {
    // Only relevant for Elo system
    const { computeVariationPerPlayer: eloComputeVariationPerPlayer } = require('./ratingSystems/eloSystem');
    return eloComputeVariationPerPlayer(system, couple, coupleRating, otherCoupleRating, isWinner, expectedWin, importance);
}

const computeImportance = (system, match) => {
    // Delegate to appropriate system
    if (currentSystem.type === 'elo') {
        const { computeImportance: eloComputeImportance } = require('./ratingSystems/eloSystem');
        return eloComputeImportance(system, match);
    } else {
        // ATP uses importance directly
        const { importance = 1 } = match;
        return importance;
    }
}

function calculateVolatilityDecay(volatility, matchCount) {
    // Only relevant for Elo system
    const { calculateVolatilityDecay: eloCalculateVolatilityDecay } = require('./ratingSystems/eloSystem');
    return eloCalculateVolatilityDecay(volatility, matchCount);
}

function reactivateVolatilityIfInactive(volatility, lastMatchDate, currentMatchDate) {
    // Only relevant for Elo system
    const { reactivateVolatilityIfInactive: eloReactivateVolatilityIfInactive } = require('./ratingSystems/eloSystem');
    return eloReactivateVolatilityIfInactive(volatility, lastMatchDate, currentMatchDate);
}



const processMatches = (matches, systemType = 'elo') => {
    // If systemType is provided and different from current system, switch
    if (systemType && systemType !== currentSystem.type) {
        switchSystem(systemType);
    }
    return currentSystem.processMatches(matches);
};

// System switching function (for advanced use)
function switchSystem(type, options = {}) {
    currentSystem = createRatingSystem(type, options);
    return currentSystem.config;
}

// Get current system type
function getCurrentSystemType() {
    return currentSystem.type;
}

module.exports = {
    processMatches,
    determineWinner,
    getTeamRating,
    calculateExpectedWin,
    computeImportance,
    computeVariationPerPlayer,
    clampRating,
    getKFactor,
    proximityFactor,
    createSystem,
    // New functions for system management
    switchSystem,
    getCurrentSystemType,
    // Factory functions for advanced use
    createRatingSystem: (type, options) => {
        const { createRatingSystem: factoryCreateRatingSystem } = require('./ratingSystemFactory');
        return factoryCreateRatingSystem(type, options);
    },
    createEloSystem: (options) => {
        const { createEloSystem: factoryCreateEloSystem } = require('./ratingSystemFactory');
        return factoryCreateEloSystem(options);
    },
    createATPSystem: (options) => {
        const { createATPSystem: factoryCreateATPSystem } = require('./ratingSystemFactory');
        return factoryCreateATPSystem(options);
    },
    createEloUnboundedSystem: (options) => {
        const { createEloUnboundedSystem: factoryCreateEloUnboundedSystem } = require('./ratingSystemFactory');
        return factoryCreateEloUnboundedSystem(options);
    }
};
