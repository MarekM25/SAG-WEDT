window.onload = function() {
    startTimeline();
}

var timeline = [
    {id: 'rect_1', 			add: 'animate'},
    {id: 'right_border', 	add: 'animate'},
    {wait: 500},
    {id: 'txt_1_a', 		add: 'animate'},

    {wait: 3000},
    {id: 'right_border', 	remove: 'animate'},
    {wait: 50},
    {id: 'right_border', 	add: 'animate'},
    {id: 'rect_2', 			add: 'animate'},
    {wait: 250},
    {id: 'fond_2_img', 		add: 'animate'},
    {id: 'txt_2_a', 		add: 'animate'},
    {wait: 500},
    {id: 'logo', 			add: 'animate'},

    {wait: 3000},
    {id: 'right_border', 	remove: 'animate'},
    {wait: 50},
    {id: 'right_border', 	add: 'animate'},
    {id: 'rect_3', 			add: 'animate'},
    {wait: 200},
    {id: 'fond_3', 			add: 'animate'},
    {id: 'txt_3_a', 		add: 'animate'},
    {wait: 150},
    {id: 'btn', 			add: 'animate'},
    {wait: 1000},
    {id: 'txt_3_b', 		add: 'animate'},
];

// timeline v. 1.09
function startTimeline(e,n){var i,t,d;i="undefined"!=typeof e?e:timeline;for(t in i)if("undefined"!=typeof i[t].id){var f=document.getElementById(i[t].id);f.style.removeProperty("-webkit-transition"),f.style.removeProperty("transition")}d="undefined"!=typeof n?n:0,setTimeout(function(){runTimeline(i,d)},15)}function runTimeline(e,n){if(e.length!=n)if("undefined"!=typeof e[n].wait)timeline_timeout="undefined"==typeof e[n].id&&"undefined"==typeof e[n].fn?setTimeout(function(){n++,runTimeline(e,n)},e[n].wait):setTimeout(function(){"undefined"!=typeof e[n].fn&&e[n].fn(),"undefined"!=typeof e[n].id&&("undefined"!=typeof e[n].add&&document.getElementById(e[n].id).classList.add(e[n].add),"undefined"!=typeof e[n].remove&&document.getElementById(e[n].id).classList.remove(e[n].remove)),n++,runTimeline(timeline,n)},e[n].wait);else if("undefined"==typeof e[n].wait)return"undefined"!=typeof e[n].fn&&e[n].fn(),"undefined"!=typeof e[n].id&&("undefined"!=typeof e[n].add&&document.getElementById(e[n].id).classList.add(e[n].add),"undefined"!=typeof e[n].remove&&document.getElementById(e[n].id).classList.remove(e[n].remove)),n++,void runTimeline(e,n)}var timeline_timeout;
function resetTimeline(e){var t,i,n;t="undefined"!=typeof e?e:timeline,clearTimeout(timeline_timeout);for(i in t)"undefined"!=typeof t[i].id&&(n=document.getElementById(t[i].id),n.style.setProperty("-webkit-transition","none","important"),n.style.setProperty("transition","none","important"),n.classList.remove(t[i].add))}
