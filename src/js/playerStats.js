function calculatePlayerStats(matches) {
  return matches.reduce((stats, match) => {
    const { couple_a, couple_b, result } = match;
    const winner = determineWinner(result);
    const allPlayers = [...couple_a, ...couple_b];

    const initializedStats = allPlayers.reduce((acc, playerId) => {
      if (acc[playerId]) return acc;

      acc[playerId] = {
        totalMatches: 0,
        wins: 0,
        losses: 0,
        opponents: {}
      };

      return acc;
    }, stats);

    const afterTeamA = couple_a.reduce(
      (acc, playerId) => updatePlayerStats(acc, playerId, couple_b, winner === 'A'),
      initializedStats,
    );

    return couple_b.reduce(
      (acc, playerId) => updatePlayerStats(acc, playerId, couple_a, winner === 'B'),
      afterTeamA,
    );
  }, {});
}

function updatePlayerStats(stats, playerId, opponents, isWinner) {
  const currentPlayer = stats[playerId] || {
    totalMatches: 0,
    wins: 0,
    losses: 0,
    opponents: {}
  };

  const updatedPlayer = Object.assign({}, currentPlayer, {
    totalMatches: currentPlayer.totalMatches + 1,
    wins: currentPlayer.wins + (isWinner ? 1 : 0),
    losses: currentPlayer.losses + (isWinner ? 0 : 1),
    opponents: updateOpponents(currentPlayer.opponents, opponents, isWinner)
  });

  const newStats = Object.assign({}, stats);
  newStats[playerId] = updatedPlayer;

  return newStats;
}

function updateOpponents(opponents, opponentIds, isWinner) {
  return opponentIds.reduce((acc, opponentId) => {
    const current = acc[opponentId] || { matches: 0, wins: 0, losses: 0 };
    acc[opponentId] = {
      matches: current.matches + 1,
      wins: current.wins + (isWinner ? 1 : 0),
      losses: current.losses + (isWinner ? 0 : 1)
    };
    return acc;
  }, opponents);
}

function getPlayerOpponentStats(matches, playerId) {
  const allMatches = matches
    .filter(match => [...match.couple_a, ...match.couple_b].includes(playerId))
    .map(match => ({
      date: match.played_at,
      won: (match.couple_a.includes(playerId) && match.winner === 'A') ||
        (match.couple_b.includes(playerId) && match.winner === 'B'),
      opponents: match.couple_a.includes(playerId) ? match.couple_b : match.couple_a,
      result: match.result,
      team: match.couple_a.includes(playerId) ? 'A' : 'B'
    }))
    .sort((a, b) => new Date(b.date) - new Date(a.date));

  // Calculate aggregated stats against each opponent using flatMap and map
  const opponentEntries = allMatches.flatMap(match =>
    match.opponents.map(opponentId => ({
      opponentId,
      match
    }))
  );

  const opponentStats = opponentEntries
    .map(entry => entry.opponentId)
    .map(opponentId => {
      const opponentMatches = opponentEntries
        .filter(e => e.opponentId === opponentId)
        .map(e => e.match);

      return [
        opponentId,
        {
          matches: opponentMatches.length,
          wins: opponentMatches.filter(m => m.won).length,
          losses: opponentMatches.filter(m => !m.won).length,
          lastPlayed: opponentMatches[0]?.date || null,
          results: opponentMatches.map(m => ({
            date: m.date,
            won: m.won,
            score: m.result
          }))
        }
      ];
    });

  return {
    allMatches,
    opponentStats
  };
}

function getAllPlayersOpponentStats(playerIds, matches) {
  return playerIds.map(playerId => [
    playerId,
    getPlayerOpponentStats(matches, playerId)
  ]);
}

module.exports = {
  calculatePlayerStats,
  getPlayerOpponentStats,
  getAllPlayersOpponentStats
};
