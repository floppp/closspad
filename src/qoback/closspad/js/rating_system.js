/**
 * Calculates Elo ratings for players based on match data.
 *
/**
 * Calculates Elo ratings for players based on match data.
 *
 * @param {Array<Object>} matches An array of match objects.
 */
const CONSTANTS = {
  K: 8, // Elo K-factor, adjust for sensitivity to new results
  INITIAL_RATING: 50,
  MIN_RATING: 0,
  MAX_RATING: 100,
};

/**
 * Rounds the rating to the nearest 0.5.
 *
 * @param {number} rating The rating to round.
 * @returns {number} The rounded rating.
 */
function roundToNearestHalf(rating) {
  return Math.round(rating * 2) / 2;
}

/**
 * Calculates the actual score based on the difference in points.
 *
 * @param {number} scoreA The score of team A.
 * @param {number} scoreB The score of team B.
 * @returns {number} The actual score for team A.
 */
function calculateActualScore(scoreA, scoreB) {
  const scoreDifference = scoreA - scoreB;
  return 0.5 + (scoreDifference / (Math.abs(scoreDifference) + 10));
}

/**
 * Calculates the expected score for player A against player B.
 *
 * @param {number} ratingA The Elo rating of player A.
 * @param {number} ratingB The Elo rating of player B.
 * @returns {number} The expected score for player A.
 */
function expectedScore(ratingA, ratingB) {
  return 1 / (1 + Math.pow(10, (ratingB - ratingA) / 400));
}

function calculateEloRatings(matches) {
  let playerRatings = {};
  let classificationHistory = [];
  let lastProcessedDate = null;

  /**
   * Limits the rating to the range of 0 to 100.
   *
   * @param {number} rating The rating to limit.
   * @returns {number} The limited rating.
   */
  function limitRating(rating) {
    return Math.max(CONSTANTS.MIN_RATING, Math.min(CONSTANTS.MAX_RATING, rating));
  }

  /**
   * Gets or initializes the Elo rating for a player.
   *
   * @param {string} playerName The name of the player.
   * @returns {object} The player object.
   */
  function getPlayer(playerName) {
    if (!playerRatings[playerName]) {
      playerRatings[playerName] = {
        name: playerName,
        points: CONSTANTS.INITIAL_RATING,
        lastMatch: null,
      };
    }
    return playerRatings[playerName];
  }

  matches.forEach(match => {
    const [playerA1, playerA2] = match.couple_a;
    const [playerB1, playerB2] = match.couple_b;
    const scoreA = match.result.reduce((acc, set) => acc + set[0], 0);
    const scoreB = match.result.reduce((acc, set) => acc + set[1], 0);
    const teamA = playerA1 + ' & ' + playerA2;
    const teamB = playerB1 + ' & ' + playerB2;

    const matchDate = new Date(match.played_at).toISOString().split('T')[0];

    const playerA1Obj = getPlayer(playerA1);
    const playerA2Obj = getPlayer(playerA2);
    const playerB1Obj = getPlayer(playerB1);
    const playerB2Obj = getPlayer(playerB2);

    const ratingA = (playerA1Obj.points + playerA2Obj.points) / 2;
    const ratingB = (playerB1Obj.points + playerB2Obj.points) / 2;

    const expectedA = expectedScore(ratingA, ratingB);
    const expectedB = expectedScore(ratingB, ratingA);

    const actualScoreA = calculateActualScore(scoreA, scoreB);
    const actualScoreB = 1 - actualScoreA;

    const newRatingA = ratingA + CONSTANTS.K * (actualScoreA - expectedA);
    const newRatingB = ratingB + CONSTANTS.K * (actualScoreB - expectedB);

    playerA1Obj.points = roundToNearestHalf(limitRating(playerA1Obj.points + (newRatingA - ratingA)));
    playerA2Obj.points = roundToNearestHalf(limitRating(playerA2Obj.points + (newRatingA - ratingA)));
    playerB1Obj.points = roundToNearestHalf(limitRating(playerB1Obj.points + (newRatingB - ratingB)));
    playerB2Obj.points = roundToNearestHalf(limitRating(playerB2Obj.points + (newRatingB - ratingB)));

    playerA1Obj.lastMatch = match.played_at;
    playerA2Obj.lastMatch = match.played_at;
    playerB1Obj.lastMatch = match.played_at;
    playerB2Obj.lastMatch = match.played_at;

    if (matchDate !== lastProcessedDate) {
      // Save classification after processing all matches for the day
      const classificationForDay = Object.keys(playerRatings).map(playerName => ({
        name: playerName,
        points: playerRatings[playerName].points,
        lastMatch: playerRatings[playerName].lastMatch
      })).sort((a, b) => b.points - a.points);

      classificationHistory.push({
        date: matchDate,
        classification: classificationForDay
      });
      lastProcessedDate = matchDate;
    }
  });

  return classificationHistory;
}

export { calculateEloRatings };
