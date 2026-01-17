// Factory for creating rating systems
// Supports Elo (bounded 0-100), Unbounded Elo (no upper limit), and ATP systems

const { createEloSystem } = require('./ratingSystems/eloSystem');
const { createATPSystem } = require('./ratingSystems/atpSystem');
const { createEloUnboundedSystem } = require('./ratingSystems/eloUnboundedSystem');

// Factory function
function createRatingSystem(type = 'elo', options = {}) {
    switch (type.toLowerCase()) {
        case 'elo':
            return createEloSystem(options);
        case 'elo-unbounded':
            return createEloUnboundedSystem(options);
        case 'atp':
            return createATPSystem(options);
        default:
            throw new Error(`Unknown rating system type: ${type}. Supported types: 'elo', 'elo-unbounded', 'atp'`);
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
    createEloUnboundedSystem,
    createATPSystem,
    getSystemInfo
};