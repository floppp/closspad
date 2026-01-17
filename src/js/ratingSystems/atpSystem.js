// ATP-like Rating System (unlimited points, 52-week rolling window)
// Simple additive system: 10 base points × importance multiplier

const { ATP_DEFAULTS } = require('../shared/constants');
const { determineWinner } = require('../shared/utils');

const defaultOptions = ATP_DEFAULTS;

function createSystem(options = {}) {
    return { ...defaultOptions, ...options, players: {}, auditLog: {} };
}

function clampRating(rating) {
    return Math.max(0, rating); // Only minimum bound of 0, no upper bound
}

function getTeamRating(system, couple) {
    return couple.reduce((sum, id) => sum + (system.players[id]?.points ?? 0), 0);
}

function computeImportance(match) {
    const { importance = 1 } = match;
    return importance; // ATP uses importance directly as multiplier
}

function addPlayer(
    system,
    playerId,
    matchDate,
    initialRating = 0
) {
    const rating = clampRating(initialRating);
    return {
        ...system,
        players: {
            ...system.players,
            [playerId]: {
                id: playerId,
                name: playerId,
                points: rating,
                lastMatchDate: matchDate,
                matchHistory: [] // Store match history for 52-week rolling window
            },
        },
    };
}

function removeOldMatches(player, currentDate) {
    const oneYearAgo = new Date(currentDate);
    oneYearAgo.setFullYear(oneYearAgo.getFullYear() - 1);
    
    return player.matchHistory.filter(match => 
        new Date(match.date) >= oneYearAgo
    );
}

function updatePointsForPlayer(system, playerId, pointsToAdd, matchDate, matchId) {
    const player = system.players[playerId];
    if (!player) return system;
    
    // Remove matches older than 52 weeks
    const currentDate = new Date(matchDate);
    const updatedMatchHistory = removeOldMatches(player, currentDate);
    
    // Add new match to history
    updatedMatchHistory.push({
        id: matchId,
        date: matchDate,
        points: pointsToAdd
    });
    
    // Recalculate total points from last 52 weeks
    const totalPoints = updatedMatchHistory.reduce((sum, match) => sum + match.points, 0);
    
    // Update player
    const updatedPlayers = {
        ...system.players,
        [playerId]: {
            ...player,
            points: clampRating(totalPoints),
            lastMatchDate: matchDate,
            matchHistory: updatedMatchHistory
        }
    };
    
    return {
        ...system,
        players: updatedPlayers
    };
}

function updateSystem(system, match) {
    const { couple_a, couple_b, result, played_at, id } = match;
    const importance = computeImportance(match);
    const winner = determineWinner(result);
    
    // Base points: 10 points per win × importance multiplier
    const basePoints = 10 * importance;
    
    // Distribute points to winning team
    const winningCouple = winner === 'A' ? couple_a : couple_b;
    const pointsPerPlayer = basePoints / 2; // Equal split between partners
    
    // Update points for all players
    const matchPlayers = [...couple_a, ...couple_b];
    let newSystem = system;
    
    for (const playerId of matchPlayers) {
        if (!newSystem.players[playerId]) {
            newSystem = addPlayer(newSystem, playerId, played_at, 0);
        }
        
        // Winners get points, losers get 0
        const pointsToAdd = winningCouple.includes(playerId) ? pointsPerPlayer : 0;
        newSystem = updatePointsForPlayer(newSystem, playerId, pointsToAdd, played_at, id);
    }
    
    // Create audit log entry
    const auditEntry = {
        matchId: id || `match-${played_at}-${couple_a.join('-')}-vs-${couple_b.join('-')}`,
        playedAt: played_at,
        teamA: couple_a,
        teamB: couple_b,
        result: result,
        winner: winner,
        importance: importance,
        basePoints: basePoints,
        pointsPerWinner: pointsPerPlayer,
        playersAudit: {}
    };
    
    // Add player audit details
    for (const playerId of matchPlayers) {
        const player = newSystem.players[playerId];
        auditEntry.playersAudit[playerId] = {
            points: player.points,
            pointsAdded: winningCouple.includes(playerId) ? pointsPerPlayer : 0,
            totalMatches: player.matchHistory.length
        };
    }
    
    return {
        ...newSystem,
        date: new Date(played_at),
        auditLog: auditEntry
    };
}

const processMatches = (matches) => {
    const systemHistory = matches.reduce(
        (acc, match) => {
            const [lastState] = acc;
            const newState = updateSystem(lastState, match);
            return [newState, ...acc];
        },
        [createSystem()]
    );

    const classificationHistory = systemHistory.map(state =>
        [
            state.date,
            Object.entries(state.players)
                .map(([id, player]) => [id, player.points])
                .sort((a, b) => b[1] - a[1])
        ]
    );

    return [classificationHistory, systemHistory];
};

// Factory function for ATP system
function createATPSystem(options = {}) {
    const system = createSystem(options);
    
    return {
        type: 'atp',
        config: system,
        players: system.players,
        auditLog: system.auditLog,
        
        // Core functions
        addPlayer: (playerId, matchDate, initialRating) => {
            const newSystem = addPlayer(system, playerId, matchDate, initialRating);
            Object.assign(system, newSystem);
            return this;
        },
        
        processMatch: (match) => {
            const newSystem = updateSystem(system, match);
            Object.assign(system, newSystem);
            return this;
        },
        
        processMatches: (matches) => {
            const [classification, history] = processMatches(matches);
            // Update internal state with last state
            if (history.length > 0) {
                const lastState = history[0];
                Object.assign(system, lastState);
            }
            return [classification, system];
        },
        
        // Utility functions
        getPlayer: (playerId) => system.players[playerId],
        getAllPlayers: () => Object.values(system.players),
        getClassification: () => {
            return Object.entries(system.players)
                .map(([id, player]) => [id, player.points])
                .sort((a, b) => b[1] - a[1]);
        },
        
        // ATP-specific functions
        getPlayerMatchHistory: (playerId) => system.players[playerId]?.matchHistory || [],
        getPointsInLast52Weeks: (playerId) => {
            const player = system.players[playerId];
            if (!player) return 0;
            return player.points; // Points already represent last 52 weeks
        }
    };
}

module.exports = {
    createATPSystem,
    processMatches,
    determineWinner,
    getTeamRating,
    computeImportance,
    clampRating,
    createSystem,
    updateSystem,
    addPlayer
};