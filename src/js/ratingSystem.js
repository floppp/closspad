const defaultOptions = {
    defaultRating: 50,
    maxRating: 100,
    minRating: 0,
    baseK: 10,
    scaleFactor: 100,
    players: {},
};

function calculatePointDecay(lastMatchDate, matchDate, playerScore) {
    const timeDiff = matchDate - lastMatchDate;
    const monthsDiff = timeDiff / (1000 * 3600 * 24 * 30);

    if (monthsDiff <= 0) return 0;

    const decayPercentage = monthsDiff <= 2
          ? 0.02 * (Math.log10(1 + (9 * monthsDiff/2)) / Math.log10(10))
          : 0.02;

    return -playerScore * decayPercentage;
}

function createSystem(options = {}) {
    return { ...defaultOptions, ...options };
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
                // volatility: 1.1,
                lastMatchDate: matchDate
            },
        },
    };
}

function updateSystem (system, match) {
    const { couple_a, couple_b, result, played_at, importance = 1 } = match;
    const matchPlayers = [...couple_a, ...couple_b];
    const winner = determineWinner(result);

    // Pre-calculate all ratings
    const teamARating = getTeamRating(system, couple_a);
    const teamBRating = getTeamRating(system, couple_b);
    const expectedWinA = calculateExpectedWin(system, teamARating, teamBRating);

    // Add missing players
    let newSystem = system;
    for (const playerId of matchPlayers) {
        if (!newSystem.players[playerId]) {
            newSystem = addPlayer(newSystem, playerId, played_at);
        }
    }

    const matchDate = new Date(played_at);

    // Update both teams with proper opponent ratings
    newSystem = updateCouple(
        newSystem,
        "A",
        expectedWinA,
        winner,
        couple_a,
        teamBRating,
        importance,
        matchDate,
    );
    newSystem = updateCouple(
        newSystem,
        "B",
        1 - expectedWinA,
        winner,
        couple_b,
        teamARating,
        importance,
        matchDate,
    );

    const inactivePlayersForDecay = Object.keys(newSystem.players).filter(
        (id) => !matchPlayers.includes(id),
    );
    return {
        ...applyDecay(newSystem, inactivePlayersForDecay, matchDate),
        date: matchDate,
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
    return system.baseK * (player?.volatility ?? 1);
}

function clampRating(system, rating) {
    return Math.max(system.minRating, Math.min(system.maxRating, rating));
}

function determineWinner(result) {
    const setsWon = result.map(([a, b]) => a > b).filter(Boolean).length;
    return setsWon > 1 ? "A" : "B";
}

function getTeamRating(system, couple) {
    return couple.reduce((sum, id) => sum + (system.players[id]?.points ?? 0), 0);
}

function updateCouple(
    system,
    coupleId,
    expectedWin,
    winner,
    couple,
    opponentTeamRating,
    importance,
    currentDate, // Accept currentDate parameter
) {
    const [player1Id, player2Id] = couple;
    const isWinner = winner === coupleId;

    const player1 = system.players[player1Id];
    const player2 = system.players[player2Id];

    const change1 = getAdjustedPointsChange(
        system,
        player1,
        player2,
        opponentTeamRating,
        isWinner,
        expectedWin,
        importance,
    );

    const change2 = getAdjustedPointsChange(
        system,
        player2,
        player1,
        opponentTeamRating,
        isWinner,
        expectedWin,
        importance,
    );

    return {
        ...system,
        players: {
            ...system.players,
            [player1Id]: {
                ...player1,
                points: clampRating(system, player1.points + change1),
                // volatility: Math.max(0.8, (player1.volatility ?? 1) * 0.99),
                lastMatchDate: currentDate, // Update last match date
            },
            [player2Id]: {
                ...player2,
                points: clampRating(system, player2.points + change2),
                // volatility: Math.max(0.8, (player2.volatility ?? 1) * 0.99),
                lastMatchDate: currentDate, // Update last match date
            },
        },
    };
}

// function calculateTeamPoints(system, teamRating, opponentRating, isWinner) {
//     const ratingDiff = opponentRating - teamRating;
//     const expected = 1 / (1 + Math.pow(10, ratingDiff/system.scaleFactor));

//     // Base points based on match outcome and expectation
//     const basePoints = isWinner
//         ? system.baseK * (1 - expected)  // Always positive for winners
//         : -system.baseK * expected;      // Always negative for losers

//     // Apply non-linear scaling for more balanced results
//     const scaledPoints = basePoints * (1 - Math.pow(expected, 2));

//     return Math.max(
//         system.minPointChange,
//         Math.min(system.maxPointChange, scaledPoints)
//     );
// }

// function updateCouple(system, couple, pointsChange) {
//     const [p1, p2] = couple;
//     const changePerPlayer = pointsChange / 2; // Equal split

//     return {
//         ...system,
//         players: {
//             ...system.players,
//             [p1]: {
//                 ...system.players[p1],
//                 points: clampRating(system, system.players[p1].points + changePerPlayer)
//             },
//             [p2]: {
//                 ...system.players[p2],
//                 points: clampRating(system, system.players[p2].points + changePerPlayer)
//             }
//         }
//     };
// }

function getAdjustedPointsChange(
    system,
    player,
    teammate,
    opponentTeamRating,
    isWinner,
    expectedWin,
    importance,
) {
    const normalize = (points) =>
          Math.max(
              0,
              Math.min(
                  1,
                  (points - system.minRating) / (system.maxRating - system.minRating),
              ),
          );

    const playerNorm = normalize(player.points);
    const teammateNorm = normalize(teammate.points);
    const opponentNorm = normalize(opponentTeamRating / 2);

    const partnerFactor = 0.7 + (0.5 - teammateNorm) * 0.6;
    const opponentFactor = 0.6 + opponentNorm * 0.8;
    const performanceFactor = 1.1 - (playerNorm - 0.5) * 0.4;

    let adjustment = importance;
    if (isWinner) {
        adjustment *= partnerFactor * opponentFactor * performanceFactor;
    } else {
        adjustment *=
            ((2 - partnerFactor) * (2 - opponentFactor) * (2 - performanceFactor)) /
            4;
    }

    const k = getKFactor(system, player);
    const proximity = proximityFactor(system, player);
    const baseChange = k * (isWinner ? 1 - expectedWin : -expectedWin);

    return baseChange * proximity * Math.max(0.5, Math.min(1.5, adjustment));
}

function applyDecay(system, currentMatchPlayers, matchDate) {
    const updatedPlayers = { ...system.players };
    const matchDateObj = new Date(matchDate);

    // Only decay players NOT in current match
    const playersToDecay = Object.keys(updatedPlayers)
        .filter(id => !currentMatchPlayers.includes(id));

    for (const id of playersToDecay) {
        const player = updatedPlayers[id];
        if (!player || !player.lastMatchDate) continue;

        const lastMatchDateObj = new Date(player.lastMatchDate);
        const timeDiff = matchDateObj - lastMatchDateObj;

        if (timeDiff > 0) {
            const decayPoints = calculatePointDecay(
                lastMatchDateObj,
                matchDateObj,
                player.points
            );

            if (decayPoints !== 0) {
                updatedPlayers[id] = {
                    ...player,
                    points: clampRating(system, player.points + decayPoints)
                };
            }
        }
    }

    return {
        ...system,
        players: updatedPlayers
    };
}


const processMatches = (matches) => {
    const initialState = createSystem();

    const states = matches.reduce(
        (acc, match) => {
            const [lastState] = acc;
            const newState = updateSystem(lastState, match);
            return [newState, ...acc];
        },
        [initialState]
    );

    const formattedStates = states.map(state => {
        return [state.date,
                Object.entries(state.players)
                .map(([id, player]) => [id, Math.round(2 * player.points) / 2])
                .sort((a, b) => b[1] - a[1])];
    });

    return formattedStates;
};

// processMatches(matches);

// module.exports = { processMatches };
export { processMatches };
