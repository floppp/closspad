
const matches = [
    {
        date: '2025-01-15',
        couple_a: ['Juan', 'Carlos'],
        couple_b: ['Luis', 'Pedro'],
        result: [[6, 3], [4, 6], [7, 5]]
    },
    {
        date: '2025-02-10',
        couple_a: ['Juan', 'Carlos'],
        couple_b: ['Luis', 'Pedro'],
        result: [[6, 2], [6, 4]]
    },
    {
        date: '2025-03-05',
        couple_a: ['Juan', 'Luis'],
        couple_b: ['Carlos', 'Pedro'],
        result: [[3, 6], [6, 4], [5, 7]]
    },
    {
        date: '2025-04-20',
        couple_a: ['Juan', 'Pedro'],
        couple_b: ['Carlos', 'Luis'],
        result: [[6, 1], [6, 2]]
    },
    {
        date: '2025-05-18',
        couple_a: ['Carlos', 'Pedro'],
        couple_b: ['Juan', 'Luis'],
        result: [[4, 6], [7, 5], [6, 3]]
    }
];

// Get all unique players from matches
function getAllPlayers(matches) {
    const playersSet = new Set();
    matches.forEach(m => {
        m.couple_a.forEach(j => playersSet.add(j));
        m.couple_b.forEach(j => playersSet.add(j));
    });
    return playersSet;
}


// Funciones para calcular estadísticas
function calculateWinner(result) {
    let setsP1 = 0, setsP2 = 0;
    for (const [p1, p2] of result) {
        if (p1 > p2) setsP1++;
        else if (p2 > p1) setsP2++;
    }
    return setsP1 > setsP2 ? 1 : 2;
}

function normalizeCouple(couple) {
    return couple.slice().sort().join(' & ');
}

function calculateStats(player, matches) {
    let wins = 0, losses = 0;
    let byMonth = {};

    for (const match of matches) {
        const { couple_a, couple_b, result, date } = match;
        const month = date.slice(0, 7);
        const inP1 = couple_a.includes(player);
        const inP2 = couple_b.includes(player);
        if (!inP1 && !inP2) continue;

        const playerTeam = inP1 ? 1 : 2;
        const won = calculateWinner(result) === playerTeam;

        if (won) wins++;
        else losses++;

        if (!byMonth[month]) byMonth[month] = { wins: 0, losses: 0 };
        if (won) byMonth[month].wins++;
        else byMonth[month].losses++;
    }

    const total = wins + losses;
    return {
        player,
        wins,
        losses,
        winPercentage: total ? (wins / total * 100).toFixed(2) : '0.00',
        lossPercentage: total ? (losses / total * 100).toFixed(2) : '0.00',
        byMonth
    };
}

function calculateOpponentStats(player, matches) {
    const opponents = {};
    
    for (const match of matches) {
        const { couple_a, couple_b, result } = match;
        const inP1 = couple_a.includes(player);
        if (!inP1 && !couple_b.includes(player)) continue;

        const matchOpponents = inP1 ? couple_b : couple_a;
        const won = calculateWinner(result) === (inP1 ? 1 : 2);

        for (const opponent of matchOpponents) {
            if (!opponents[opponent]) {
                opponents[opponent] = { wins: 0, losses: 0 };
            }
            if (won) opponents[opponent].wins++;
            else opponents[opponent].losses++;
        }
    }

    // Calculate percentages
    for (const opponent in opponents) {
        const o = opponents[opponent];
        o.total = o.wins + o.losses;
        o.winPercentage = (o.wins / o.total * 100).toFixed(2);
    }

    return opponents;
}

function calculateCoupleStats(matches) {
    const couples = {};

    for (const { couple_a, couple_b, result } of matches) {
        const winner = calculateWinner(result);
        const keys = [
            { key: normalizeCouple(couple_a), won: winner === 1 },
            { key: normalizeCouple(couple_b), won: winner === 2 }
        ];

        for (const { key, won } of keys) {
            if (!couples[key]) couples[key] = { wins: 0, losses: 0, total: 0 };
            if (won) couples[key].wins++;
            else couples[key].losses++;
            couples[key].total++;
        }
    }

    for (const key in couples) {
        const p = couples[key];
        p.winPercentage = p.total ? (p.wins / p.total * 100).toFixed(2) : '0.00';
        p.lossPercentage = p.total ? (p.losses / p.total * 100).toFixed(2) : '0.00';
    }

    return couples;
}

// Inicialización de gráficos
const lineChart = echarts.init(document.getElementById('lineChart'));
const barChart = echarts.init(document.getElementById('barChart'));

function updateCharts(player) {
    const stats = calculateStats(player, matches);
    const vsOpponents = calculateOpponentStats(player, matches);
    const months = Object.keys(stats.byMonth).sort();
    const wins = months.map(m => stats.byMonth[m].wins);
    const losses = months.map(m => stats.byMonth[m].losses);

    const lineOption = {
        title: {
            text: `Wins and Losses for ${player} by Month`
        },
        tooltip: {
            trigger: 'axis'
        },
        legend: {
            data: ['Wins', 'Losses']
        },
        xAxis: {
            type: 'category',
            data: months
        },
        yAxis: {
            type: 'value'
        },
        series: [
            {
                name: 'Wins',
                type: 'line',
                data: wins
            },
            {
                name: 'Losses',
                type: 'line',
                data: losses
            }
        ]
    };

    lineChart.setOption(lineOption);

    const couplesStats = calculateCoupleStats(matches);
    const couples = Object.keys(couplesStats);
    const percentages = couples.map(p => parseFloat(couplesStats[p].winPercentage));

    // Convert opponent stats to chart data
    const opponents = Object.keys(vsOpponents);
    const opponentsPercentages = opponents.map(o => 
        parseFloat(vsOpponents[o].winPercentage));

    const barOption = {
        title: {
            text: 'Win Percentage (Couples vs Opponents)'
        },
        tooltip: {
            trigger: 'axis',
            axisPointer: {
                type: 'shadow'
            }
        },
        xAxis: {
            type: 'category',
            data: [...couples, ...opponents.map(o => `vs ${o}`)],
            axisLabel: {
                interval: 0,
                rotate: 30
            }
        },
        yAxis: {
            type: 'value',
            max: 100
        },
        series: [
            {
                name: 'Wins with Couple',
                type: 'bar',
                data: [...percentages, ...Array(opponents.length).fill(null)]
            },
            {
                name: 'Wins vs Opponent',
                type: 'bar',
                data: [...Array(couples.length).fill(null), ...opponentsPercentages]
            }
        ]
    };

    barChart.setOption(barOption);
}


// Initialize player selector
const playersSet = getAllPlayers(matches);
const players = Array.from(playersSet);
const playerSelect = document.getElementById('playerSelect');
players.forEach(j => {
    const option = document.createElement('option');
    option.value = j;
    option.textContent = j;
    playerSelect.appendChild(option);
});

playerSelect.addEventListener('change', () => {
    updateCharts(playerSelect.value);
});

// Initialize with first player
playerSelect.value = players[0];
updateCharts(players[0]);
