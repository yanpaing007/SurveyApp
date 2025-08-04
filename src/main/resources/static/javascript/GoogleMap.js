document.addEventListener('DOMContentLoaded', e => {
    let mapInitialized = false;


    if (mapInitialized) return;

    const showMap = document.getElementById('ShowMap');
    const longitude = parseFloat(showMap.getAttribute('data-longitude')) || 96.193;
    const latitude = parseFloat(showMap.getAttribute('data-latitude')) || 16.863;
    const customerLocation = { lat: latitude, lng: longitude };

    map = new google.maps.Map(showMap, {
        center: customerLocation,
        zoom: 14,
        gestureHandling: "auto",
        clickableIcons: false,
        disableDefaultUI: false,
        draggable: true,
    });

    marker = new google.maps.Marker({
        position: customerLocation,
        map,
        draggable: false,
    });

});

// Google Map Api Implementation
let map,marker;

function initMap(lat, lng) {
    lat = lat ?? parseFloat($('#latitude').val());
    lng = lng ?? parseFloat($('#longitude').val());

    lat = lat || 16.8661;
    lng = lng || 96.1951;
    const defaultLocation = { lat, lng };
    if (!map) {
        map = new google.maps.Map(document.getElementById('map'), {
            center: defaultLocation,
            zoom: 14,
        });

        marker = new google.maps.Marker({
            position: defaultLocation,
            map,
            draggable: true,
        });

        marker.addListener('dragend', function () {
            const position = marker.getPosition();
            updateLatLngInputs(position.lat(), position.lng());
        });

        map.addListener('click', function (event) {
            const lat = event.latLng.lat();
            const lng = event.latLng.lng();
            marker.setPosition({ lat, lng });
            updateLatLngInputs(lat, lng);
        });
    } else {
        // reuse map instance, just re-center and move marker
        const newPosition = { lat, lng };
        map.setCenter(newPosition);
        marker.setPosition(newPosition);
        updateLatLngInputs(lat, lng);
    }
}

function updateLatLngInputs(lat, lng){
    document.getElementById('latitude').value = lat.toFixed(6);
    document.getElementById('longitude').value = lng.toFixed(6);
}


// Get real-time location of user(Feature currently disabled)
// if (navigator.geolocation) {
//     navigator.geolocation.getCurrentPosition(
//         (position) => {
//             const userLocation = {
//                 lat: position.coords.latitude,
//                 lng: position.coords.longitude,
//             };
//             map.setCenter(userLocation);
//             marker.setPosition(userLocation);
//             updateLatLngInputs(userLocation.lat, userLocation.lng);
//         },
//         (error) => {
//             console.warn("Geolocation failed with error: ", error.message);
//         }
//     );
// }