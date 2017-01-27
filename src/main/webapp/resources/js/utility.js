/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 * 
 * j_idt36:indirizzo
 */


function geocode() {
    PF('geoMap').geocode(document.getElementById('twitter_search_buttons:indirizzo').value);
}

function geocodeDB() {
    PF('geoMapDB').geocode(document.getElementById('searchbox:indirizzo').value);
}

function reverseGeocode() {
    var lat = document.getElementById('lat').value,
        lng = document.getElementById('lng').value;

    PF('revGeoMap').reverseGeocode(lat, lng);
}
