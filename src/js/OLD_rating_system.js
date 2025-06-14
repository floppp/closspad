/**
 * Calculates Elo ratings for players based on match data.
 */
const CONSTANTS = {
  K: 8, // elo k-factor for sensitivity
  INITIAL_RATING: 50,
  MIN_RATING: 0,
  MAX_RATING: 100,
  ELO_DIVISOR: 400,
};

function roundToNearestHalf(rating) {
  return Math.round(rating * 2) / 2;
}

/**
 * Calculates the actual score based on the difference in points and the Elo ratings.
 * @param {number} scoreA The score of team A.
 * @param {number} scoreB The score of team B.
 * @param {number} ratingA The Elo rating of team A.
 * @param {number} ratingB The Elo rating of team B.
 * @returns {number} The actual score for team A.
 */
function calculateActualScore(scoreA, scoreB, ratingA, ratingB) {
  const scoreDifference = scoreA - scoreB;
  const ratingDifference = ratingA - ratingB;
  return 1 / (1 + Math.exp(-(scoreDifference / 5 + ratingDifference / CONSTANTS.ELO_DIVISOR)));
}

/**
 * Calculates the expected score for couple A against couple B.
 * This represents the probability that couple A will win against couple B.
 * @param {number} ratingA The Elo rating of couple A.
 * @param {number} ratingB The Elo rating of couple B.
 * @returns {number} The expected score for couple A.
 */
/**
 * Calculates the point decay for a player based on the time difference since their last match.
 * @param {Date} lastMatch The date of the player's last match.
 * @param {Date} currentDate The current date.
 * @returns {number} The point decay for the player.
 */
function calculatePointDecay(lastMatch, currentDate) {
  const timeDiff = currentDate.getTime() - lastMatch.getTime();
  const monthsDiff = timeDiff / (1000 * 3600 * 24 * 30); // Approximate months
  if (monthsDiff > 1) {
    return -Math.log(monthsDiff) * 2; // Logarithmic decay, adjust 2 for decay rate
  }
  return 0;
}

function expectedScore(ratingA, ratingB) {
  return 1 / (1 + Math.pow(10, (ratingB - ratingA) / CONSTANTS.ELO_DIVISOR));
}

function calculateEloRatings(matches) {
  let playerRatings = {};
  let classificationHistory = [];
  let lastProcessedDate = null;

  function limitRating(rating) {
    return Math.max(CONSTANTS.MIN_RATING, Math.min(CONSTANTS.MAX_RATING, rating));
  }

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

  // Apply point decay to all players before processing matches
  const currentDate = new Date();
  for (const playerName in playerRatings) {
    const player = playerRatings[playerName];
    if (player.lastMatch) {
      const decay = calculatePointDecay(new Date(player.lastMatch), currentDate);
      player.points = roundToNearestHalf(limitRating(player.points + decay));
    }
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

    const actualScoreA = calculateActualScore(scoreA, scoreB, ratingA, ratingB);
    const actualScoreB = 1 - actualScoreA;

    console.log("ratingA", ratingA, "ratingB", ratingB, "expectedA", expectedA, "actualScoreA", actualScoreA);

    playerA1Obj.points = roundToNearestHalf(limitRating(playerA1Obj.points + CONSTANTS.K * (actualScoreA - expectedA)));
    playerA2Obj.points = roundToNearestHalf(limitRating(playerA2Obj.points + CONSTANTS.K * (actualScoreA - expectedA)));
    playerB1Obj.points = roundToNearestHalf(limitRating(playerB1Obj.points + CONSTANTS.K * (actualScoreB - expectedB)));
    playerB2Obj.points = roundToNearestHalf(limitRating(playerB2Obj.points + CONSTANTS.K * (actualScoreB - expectedB)));

    playerA1Obj.lastMatch = match.played_at;
    playerA2Obj.lastMatch = match.played_at;
    playerB1Obj.lastMatch = match.played_at;
    playerB2Obj.lastMatch = match.played_at;

    if (matchDate !== lastProcessedDate) {
      const classificationForDay = Object.keys(playerRatings).map(playerName => {
        const player = playerRatings[playerName];
        const previousPoints = classificationHistory.length > 0
          ? classificationHistory[classificationHistory.length - 1].classification.find(p => p.name === playerName)?.points || CONSTANTS.INITIAL_RATING
          : CONSTANTS.INITIAL_RATING;
        const pointDifference = player.points - previousPoints;

        return {
          name: playerName,
          points: player.points,
          lastMatch: player.lastMatch,
          pointDifference: pointDifference
        };
      }).sort((a, b) => b.points - a.points);

      classificationHistory.push({
        date: matchDate,
        classification: classificationForDay
      });
      lastProcessedDate = matchDate;
    }
  });

  return classificationHistory;
}

import { data } from './data.js';

const classificationHistory = calculateEloRatings(data);
console.log(classificationHistory);

export { calculateEloRatings };
