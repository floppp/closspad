// Elo Rating System (bounded 0-100)
// Extracted from original ratingSystem.js

const { ELO_DEFAULTS } = require('../shared/constants');
const { determineWinner } = require('../shared/utils');

const defaultOptions = ELO_DEFAULTS;

function createSystem(options = {}) {
    return { ...defaultOptions, ...options, players: {}, auditLog: {} };
}

function calculatePointDecay(player, matchDate) {
    const monthsDiff = (matchDate - new Date(player.lastMatchDate)) / (1000 * 3600 * 24 * 30);

    if (monthsDiff <= 1) {
        return 0;
    }

    const decayPercentage = monthsDiff <= 6
        ? 0.03 * (Math.log10(1 + (9 * monthsDiff / 6)) / Math.log10(10))
        : 0.03;

    return player.points * decayPercentage;
}

function applyDecay(system, inactivePlayers, matchDate) {
    const date = new Date(matchDate);
    const updatedPlayers = Object.values(system.players)
        .filter(pl => inactivePlayers.includes(pl.id))
        .reduce((acc, pl) => {
            const decayPoints = calculatePointDecay(pl, date);
            acc[pl.id] = {
                ...pl,
                points: clampRating(system, pl.points - decayPoints)
            };

            return acc;
        }, { ...system.players });

    return {
        ...system,
        players: updatedPlayers
    };
}

function addPlayer(
    system,
    playerId,
    matchDate,
    initialRating = system.defaultRating
) {
    const rating = clampRating(system, initialRating);
    return {
        ...system,
        players: {
            ...system.players,
            [playerId]: {
                id: playerId,
                name: playerId,
                points: rating,
                volatility: 1.1,
                lastMatchDate: matchDate
            },
        },
    };
}

function updateSystem(system, match) {
    const { couple_a, couple_b, result, played_at } = match;
    const importance = computeImportance(system, match);
    const matchPlayers = [...couple_a, ...couple_b];
    const winner = determineWinner(result);

    const teamARating = getTeamRating(system, couple_a);
    const teamBRating = getTeamRating(system, couple_b);
    const expectedWinA = calculateExpectedWin(system, teamARating, teamBRating);

    let newSystem = system;
    const initialPlayerRatings = {};

    for (const playerId of matchPlayers) {
        if (!newSystem.players[playerId]) {
            newSystem = addPlayer(newSystem, playerId, played_at);
        }
        initialPlayerRatings[playerId] = newSystem.players[playerId].points;
    }

    const matchDate = new Date(played_at);

    const { variations: playerVariationsA, auditDetails: auditDetailsA } =
        computeVariationPerPlayer(newSystem, couple_a, teamARating, teamBRating, winner === 'A', expectedWinA, importance);
    const { variations: playerVariationsB, auditDetails: auditDetailsB } =
        computeVariationPerPlayer(newSystem, couple_b, teamBRating, teamARating, winner === 'B', 1 - expectedWinA, importance);

    newSystem = updateSystemCouple(newSystem, couple_a, playerVariationsA, played_at);
    newSystem = updateSystemCouple(newSystem, couple_b, playerVariationsB, played_at);

    const inactivePlayersForDecay = Object.keys(newSystem.players).filter((id) => !matchPlayers.includes(id));
    const systemAfterDecay = applyDecay(newSystem, inactivePlayersForDecay, matchDate);

    // --- AUDIT LOGGING ---
    const auditEntry = {
        matchId: match.id || `match-${played_at}-${couple_a.join('-')}-vs-${couple_b.join('-')}`,
        playedAt: played_at,
        teamA: couple_a,
        teamB: couple_b,
        result: result,
        winner: winner,
        importance: importance,
        expectedWinA: expectedWinA.toFixed(4),
        teamARatingBefore: teamARating,
        teamBRatingBefore: teamBRating,
        playersAudit: {},
        decayDetails: [],
    };

    // Collect change details for active players
    for (const playerId of matchPlayers) {
        const initialPoints = initialPlayerRatings[playerId];
        const finalPoints = systemAfterDecay.players[playerId].points;
        const totalDelta = finalPoints - initialPoints;

        const auditDetailForPlayer = {};
        if (couple_a.includes(playerId)) {
            Object.assign(auditDetailForPlayer, auditDetailsA[playerId]);
        } else if (couple_b.includes(playerId)) {
            Object.assign(auditDetailForPlayer, auditDetailsB[playerId]);
        }

        auditEntry.playersAudit[playerId] = {
            initialPoints,
            finalPoints,
            totalDelta,
            breakdown: auditDetailForPlayer // This contains the breakdown (baseDelta, bonuses, etc.)
        };
    }

    // Collect decay details for inactive players
    for (const playerId of inactivePlayersForDecay) {
        const playerBeforeDecay = newSystem.players[playerId]; // Points before applying decay
        const playerAfterDecay = systemAfterDecay.players[playerId]; // Points after applying decay
        const pointsDecayed = playerBeforeDecay.points - playerAfterDecay.points;

        if (pointsDecayed > 0) { // Only if there was decay
            auditEntry.decayDetails.push({
                playerId: playerId,
                initialPoints: playerBeforeDecay.points,
                finalPoints: playerAfterDecay.points,
                decayAmount: pointsDecayed,
            });
        }
    }

    return {
        ...systemAfterDecay,
        date: matchDate,
        auditLog: auditEntry,
    };
}

function updateSystemCouple(system, couple, playerVariations, matchDate) {
    const updatedPlayers = { ...system.players };

    for (const playerId of couple) {
        const player = updatedPlayers[playerId];
        const matchCount = (player.matchCount ?? 0) + 1;
        const delta = playerVariations[playerId] ?? 0;
        let volatility = calculateVolatilityDecay(player.volatility, 1);
        volatility = reactivateVolatilityIfInactive(volatility, player.lastMatchDate, new Date(matchDate));

        updatedPlayers[playerId] = {
            ...player,
            points: clampRating(system, player.points + delta),
            lastMatchDate: matchDate,
            volatility,
        };
    }

    return {
        ...system,
        players: updatedPlayers,
    };
}

function updateSystemCoupleEqualyDistributed(system, couple, pointsChange) {
    const [p1, p2] = couple;
    const changePerPlayer = pointsChange / 2; // Equal split

    return {
        ...system,
        players: {
            ...system.players,
            [p1]: {
                ...system.players[p1],
                points: clampRating(system, system.players[p1].points + changePerPlayer)
            },
            [p2]: {
                ...system.players[p2],
                points: clampRating(system, system.players[p2].points + changePerPlayer)
            }
        }
    };
}

// --- Helper Functions ---
function calculateExpectedWin(system, teamARating, teamBRating) {
    const exponent = (teamBRating - teamARating) / system.scaleFactor;

    return 1 / (1 + Math.pow(10, exponent));
}

function proximityFactor(system, player) {
    const rating = player?.points ?? system.defaultRating;
    const fromTop = (system.maxRating - rating) / system.maxRating;
    const fromBottom = rating / system.maxRating;

    return Math.min(fromTop, fromBottom, 1) * 2;
}

function getKFactor(system, player) {
    return system.baseK * player.volatility;
}

function clampRating(system, rating) {
    return Math.max(system.minRating, Math.min(system.maxRating, rating));
}

function getTeamRating(system, couple) {
    return couple.reduce((sum, id) => sum + (system.players[id]?.points ?? system.defaultRating), 0);
}

function computeVariationPerPlayer(system, couple, coupleRating, otherCoupleRating, isWinner, expectedWin, importance) {
    const actualResult = isWinner ? 1 : 0;

    const totalWeightedPoints = couple.reduce(
        (sum, id) => sum + Math.pow(system.players[id].points, 0.5),
        0
    );

    const baseDelta = importance * system.baseK * (actualResult - expectedWin);

    // 🎲 SURPRISE BONUS
    const surpriseBonus = isWinner ? Math.max(0, (0.5 - expectedWin)) * system.baseK * 0.5 : 0;

    const variations = {};
    // Add a structure to store audit details per player
    const auditDetails = {};

    for (const playerId of couple) {
        const player = system.players[playerId];
        const playerWeight = Math.pow(player.points, 0.5) / totalWeightedPoints;

        // 💥 WEAK PLAYER REWARD
        const reverseWeight = 1 - playerWeight;
        const weakBonus = isWinner ? reverseWeight * 2 : 0;

        // 🧗 BONUS UNDERDOG
        const opponentAvg = otherCoupleRating / 2;
        const underdogGap = Math.max(0, opponentAvg - player.points);
        const underdogBonus = isWinner ? Math.round(underdogGap * 0.015) : 0;

        // SURPRISE BONUS DISTRIBUTION
        const surpriseShare = isWinner ? surpriseBonus * playerWeight : 0;
        const adjustedVariation = baseDelta * playerWeight + surpriseShare + weakBonus + underdogBonus;

        variations[playerId] = adjustedVariation;

        // Capture details for audit
        auditDetails[playerId] = {
            baseDelta: Math.round(baseDelta * playerWeight), // Base delta proportional to weight
            surpriseBonus: surpriseShare,
            weakBonus: weakBonus,
            underdogBonus: underdogBonus,
            totalChange: adjustedVariation,
            playerWeight: playerWeight,
            originalPlayerPoints: player.points // For reference
        };
    }

    // Return both variations and audit details
    return { variations, auditDetails };
}

const computeImportance = (system, match) => {
    const { importance = 1 } = match;

    return importance * (match.result.length === 1 ? system.oneSetImportance : system.importance);
}

function calculateVolatilityDecay(volatility, matchCount) {
    const MIN_VOLATILITY = 0.85;
    const decayRate = 0.02;
    const newVolatility = volatility * Math.pow(1 - decayRate, matchCount);

    return Math.max(MIN_VOLATILITY, newVolatility);
}

function reactivateVolatilityIfInactive(volatility, lastMatchDate, currentMatchDate) {
    const MAX_VOLATILITY = 1.3;
    const INACTIVITY_MONTHS_THRESHOLD = 3;

    const diffTime = currentMatchDate - new Date(lastMatchDate);
    const monthsInactive = diffTime / (1000 * 3600 * 24 * 30);

    if (monthsInactive >= INACTIVITY_MONTHS_THRESHOLD) {
        return Math.min(volatility * 1.15, MAX_VOLATILITY);
    }

    return volatility;
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

// Factory function for Elo system
function createEloSystem(options = {}) {
    const system = createSystem(options);
    
    return {
        type: 'elo',
        config: system,
        players: system.players,
        auditLog: system.auditLog,
        
        // Core functions
        addPlayer: (playerId, matchDate, initialRating) => {
            const newSystem = addPlayer(system, playerId, matchDate, initialRating);
            system.players = newSystem.players;
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
        }
    };
}

module.exports = {
    createEloSystem,
    processMatches,
    determineWinner,
    getTeamRating,
    calculateExpectedWin,
    computeImportance,
    computeVariationPerPlayer,
    clampRating,
    getKFactor,
    proximityFactor,
    createSystem
};