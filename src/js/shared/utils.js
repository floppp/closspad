// Shared utility functions for both rating systems

/**
 * Determine winner from match result
 * @param {Array<Array<number>>} result - Match result e.g. [[6,4], [6,3]]
 * @returns {string} 'A' if team A won, 'B' if team B won
 */
function determineWinner(result) {
  const points = result.map(([a, b]) => a > b ? 1 : -1).reduce((acc, e) => acc + e, 0);
  return points > 0 ? "A" : "B";
}

/**
 * Calculate days between two dates
 * @param {Date|string} date1 - First date
 * @param {Date|string} date2 - Second date
 * @returns {number} Number of days between dates
 */
function daysBetween(date1, date2) {
  const d1 = new Date(date1);
  const d2 = new Date(date2);
  const diffTime = Math.abs(d2 - d1);
  return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
}

/**
 * Add weeks to a date
 * @param {Date|string} date - Starting date
 * @param {number} weeks - Number of weeks to add
 * @returns {Date} New date with weeks added
 */
function addWeeks(date, weeks) {
  const result = new Date(date);
  result.setDate(result.getDate() + (weeks * 7));
  return result;
}

/**
 * Check if a date is within a rolling window
 * @param {Date|string} date - Date to check
 * @param {Date|string} referenceDate - Reference date (usually now)
 * @param {number} weeks - Window size in weeks
 * @returns {boolean} True if date is within window
 */
function isWithinRollingWindow(date, referenceDate, weeks) {
  const cutoff = addWeeks(referenceDate, -weeks);
  return new Date(date) >= cutoff;
}

/**
 * Sort matches by played_at date (chronological)
 * @param {Array<Object>} matches - Array of match objects
 * @returns {Array<Object>} Sorted matches
 */
function sortMatchesChronologically(matches) {
  return [...matches].sort((a, b) => 
    new Date(a.played_at) - new Date(b.played_at)
  );
}

/**
 * Get all unique player IDs from matches
 * @param {Array<Object>} matches - Array of match objects
 * @returns {Array<string>} Array of unique player IDs
 */
function getAllPlayerIds(matches) {
  const playerIds = new Set();
  matches.forEach(match => {
    match.couple_a.forEach(id => playerIds.add(id));
    match.couple_b.forEach(id => playerIds.add(id));
  });
  return Array.from(playerIds);
}

module.exports = {
  determineWinner,
  daysBetween,
  addWeeks,
  isWithinRollingWindow,
  sortMatchesChronologically,
  getAllPlayerIds
};