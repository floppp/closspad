// Factory for creating rating systems
// Supports Elo (bounded 0-100) and ATP (unlimited points) systems

const { createEloSystem } = require('./ratingSystems/eloSystem');
const { createATPSystem } = require('./ratingSystems/atpSystem');

// Factory function
function createRatingSystem(type = 'elo', options = {}) {
    switch (type.toLowerCase()) {
        case 'elo':
            return createEloSystem(options);
        case 'atp':
            return createATPSystem(options);
        default:
            throw new Error(`Unknown rating system type: ${type}. Supported types: 'elo', 'atp'`);
    }
}

// Utility function to get system info
function getSystemInfo(system) {
    return {
        type: system.type,
        playerCount: Object.keys(system.players).length,
        config: system.config
    };
}

module.exports = {
    createRatingSystem,
    createEloSystem,
    createATPSystem,
    getSystemInfo
};