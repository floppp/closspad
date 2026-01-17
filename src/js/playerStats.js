/**
 * Update player stats for a single match
 * @param {Object} stats - Current player statistics
 * @param {string} playerId - ID of the player to update
 * @param {Array<string>} opponents - Array of opponent IDs
 * @param {boolean} isWinner - Whether the player won the match
 * @returns {Object} Updated player statistics
 */
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

/**
 * Update opponent statistics for a match
 * @param {Object} opponents - Current opponent statistics
 * @param {Array<string>} opponentIds - Array of opponent IDs
 * @param {boolean} isWinner - Whether the player won against these opponents
 * @returns {Object} Updated opponent statistics
 */
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

/**
 * Get opponent statistics for a specific player
 * @param {Array<Object>} matches - Array of match objects
 * @param {string} playerId - ID of the player to analyze
 * @returns {Object} Player's match history and opponent statistics
 */
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

  // Calculate aggregated stats against each opponent efficiently using Map
  const opponentStatsMap = new Map();

  for (const match of allMatches) {
    for (const opponentId of match.opponents) {
      let stats = opponentStatsMap.get(opponentId);
      if (!stats) {
        stats = {
          matches: 0,
          wins: 0,
          losses: 0,
          lastPlayed: null,
          results: []
        };
        opponentStatsMap.set(opponentId, stats);
      }

      stats.matches++;
      if (match.won) stats.wins++;
      else stats.losses++;

      // Update lastPlayed (most recent date)
      if (!stats.lastPlayed || new Date(match.date) > new Date(stats.lastPlayed)) {
        stats.lastPlayed = match.date;
      }

      stats.results.push({
        date: match.date,
        won: match.won,
        score: match.result
      });
    }
  }

  const opponentStats = Array.from(opponentStatsMap.entries());

  return {
    allMatches,
    opponentStats
  };
}

/**
 * Get opponent statistics for multiple players
 * @param {Array<string>} playerIds - Array of player IDs
 * @param {Array<Object>} matches - Array of match objects
 * @returns {Array<Array>} Array of [playerId, opponentStats] pairs
 */
function getAllPlayersOpponentStats(playerIds, matches) {
  return playerIds.map(playerId => [
    playerId,
    getPlayerOpponentStats(matches, playerId)
  ]);
}

module.exports = {
  getPlayerOpponentStats,
  getAllPlayersOpponentStats
};
