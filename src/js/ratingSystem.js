const defaultOptions = {
    defaultRating: 50,
    maxRating: 100,
    minRating: 0,
    baseK: 10,
    scaleFactor: 200,
    players: {},
};

function calculatePointDecay(lastMatchDate, currentDate) {
    // Ensure lastMatchDate is a Date object
    if (!(lastMatchDate instanceof Date)) {
        lastMatchDate = new Date(lastMatchDate);
    }

    const timeDiff = currentDate.getTime() - lastMatchDate.getTime();
    const monthsDiff = timeDiff / (1000 * 3600 * 24 * 30); // Approximate months

    if (monthsDiff > 1) {
        // Logarithmic decay. Returns a negative value for point reduction.
        // Adjust the multiplier (e.g., 2) to control the decay rate.
        return -Math.log(monthsDiff) * 2;
    }
    return 0; // No decay for matches within the last month or less
}

function createSystem(options = {}) {
    return { ...defaultOptions, ...options };
}

function addPlayer(
    system,
    playerId,
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
                lastMatchDate: new Date(), // Initialize lastMatchDate for new players
            },
        },
    };
}

function updateSystem    (system, match) {
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
            newSystem = addPlayer(newSystem, playerId);
        }
    }

    const matchDate = new Date(played_at);
    // const currentDate = new Date(); // Get current date for updating lastMatchDate

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
        date: matchDate,
        ...applyDecay(newSystem, inactivePlayersForDecay),
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
                volatility: Math.max(0.8, (player1.volatility ?? 1) * 0.99),
                lastMatchDate: currentDate, // Update last match date
            },
            [player2Id]: {
                ...player2,
                points: clampRating(system, player2.points + change2),
                volatility: Math.max(0.8, (player2.volatility ?? 1) * 0.99),
                lastMatchDate: currentDate, // Update last match date
            },
        },
    };
}

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

function applyDecay(system, playersToDecay) {
    // Renamed parameter for clarity: these are the players who will decay
    const updatedPlayers = { ...system.players };
    const currentDate = new Date(); // The date when this decay calculation is being made

    for (const id of playersToDecay) {
        // Iterate only through the specified players to decay
        const player = updatedPlayers[id];

        // Ensure player exists and has a lastMatchDate
        if (player && player.lastMatchDate) {
            const lastMatchDateObj = new Date(player.lastMatchDate);

            // Apply decay only if the player's last activity was on a day PRIOR to currentDate
            // This prevents applying decay to players whose lastMatchDate is "today"
            // (even if they weren't in the *current* match, they might have played *another* match today).
            if (lastMatchDateObj.toDateString() !== currentDate.toDateString()) {
                const decayPoints = calculatePointDecay(
                    player.lastMatchDate,
                    currentDate,
                );
                if (decayPoints !== 0) {
                    // Only update if there's actual decay
                    updatedPlayers[id] = {
                        ...player,
                        points: clampRating(system, player.points + decayPoints),
                        // Optional: consider increasing volatility slightly for decayed players
                        // volatility: Math.min(player.volatility * 1.01, 1.5)
                    };
                }
            }
        }
    }

    return {
        ...system,
        players: updatedPlayers,
    };
}

const processMatches = (matches) => {
    const states = matches.reduce(
        (acc, match) => {
            const lastState = acc[0];
            const newState = updateSystem(lastState, match);

            return [newState, ...acc];
        },
        [createSystem()],
    );

    console.log(states)

    const formattedStates = states.map((state) =>
        [state.date, Object.entries(state.players)
         .map(([id, player]) => [id, Math.round(2 * player.points) / 2])
         .sort((a, b) => b[1] - a[1])],
    );

    return formattedStates;
};

// processMatches(matches);

module.exports = { processMatches };


