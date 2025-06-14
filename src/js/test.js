import { processMatches } from './ratingSystem.js';
import { data } from './data.js';

processMatches(
  data.map(d => ({...d, played_at: new Date(d.played_at)}))
    .filter(d => d.played_at > new Date('2025-05-20')))[0];


// [{"id":2,"played_at":"2025-05-21T17:30:00+00:00","couple_a":["Carlos","Fernando"],"couple_b":["Raúl","Alberto"],"result":[[6,2],[6,3]],"organization":"fik"}, 
//  {"id":1,"played_at":"2025-05-21T18:15:00+00:00","couple_a":["Raúl","Fernando"],"couple_b":["Carlos","Alberto"],"result":[[6,2],[6,3]],"organization":"fik"}, 
//  {"id":18,"played_at":"2025-05-28T17:00:00+00:00","couple_a":["Pablo Cortes","Álex"],"couple_b":["Amador","Fernando"],"result":[[6,4],[6,4]],"organization":"fik"}, 
//  {"id":19,"played_at":"2025-05-29T17:00:05+00:00","couple_a":["Raúl","Carlos"],"couple_b":["Alberto","Mario"],"result":[[6,0],[6,4]],"organization":"fik"}, 
//  {"id":20,"played_at":"2025-05-29T18:00:00+00:00","couple_a":["Raúl","Mario"],"couple_b":["Alberto","Carlos"],"result":[[6,3]],"organization":"fik"}]