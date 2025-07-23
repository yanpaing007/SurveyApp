
//Function to Copy GeneratedId text for Application Details
function copyText(myValue, text) {
    const value = document.getElementById(myValue);
    const tooltip = document.getElementById(text);

    navigator.clipboard.writeText(value.value).then(() => {
        tooltip.classList.add("opacity-100");
        tooltip.classList.remove("opacity-0");
    })

    setTimeout(() => {
        tooltip.classList.remove("opacity-100");
        tooltip.classList.add("opacity-0");
    }, 1000)
}



// Google Map Api Implementation
let map,marker;

function initMap(lat = 16.8661, lng = 96.1951) {
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

        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(
                (position) => {
                    const userLocation = {
                        lat: position.coords.latitude,
                        lng: position.coords.longitude,
                    };
                    map.setCenter(userLocation);
                    marker.setPosition(userLocation);
                    updateLatLngInputs(userLocation.lat, userLocation.lng);
                },
                (error) => {
                    console.warn("Geolocation failed with error: ", error.message);
                }
            );
        }
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

// function initMap(lat, lng) {
//   if(map){
//       map.setView([lat, lng],13);
//       marker.setLatLng([lat, lng]);
//       return;
//   }
//
//   map = L.map('map').setView([lat, lng],13);
//   L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
//   }).addTo(map);
//
//   marker = L.marker([lat, lng], {draggable: true,clickable:true}).addTo(map);
//
//   marker.on('dragend', function() {
//       const position = marker.getLatLng();
//       $('#latitude').val(position.lat.toFixed(6));
//       $('#longitude').val(position.lng.toFixed(6));
//   });
//
//   map.on('click', function(e) {
//       const {lat, lng} = e.latlng;
//       marker.setLatLng([lat, lng]);
//       $('#latitude').val(lat.toFixed(6));
//       $('#longitude').val(lat.toFixed(6));
//   });
//
// }
//
// function initMapWithGeolocation() {
//     if (navigator.geolocation) {
//         navigator.geolocation.getCurrentPosition(
//             (position) => {
//                 const lat = position.coords.latitude;
//                 const lng = position.coords.longitude;
//                 $('#latitude').val(lat.toFixed(6));
//                 $('#longitude').val(lng.toFixed(6));
//                 initMap(lat, lng);
//             },
//             (error) => {
//                 console.warn("Geolocation failed or denied. Using default center.");
//                 initMap(21.9162, 95.9560); // Default to Myanmar center
//             }
//         );
//     } else {
//         console.warn("Geolocation not supported. Using default center.");
//         initMap(21.9162, 95.9560);
//     }
// }


// Select2 implementation for Survey Add Modal Form
$(document).ready(() => {
    initMap();
    $('#state').select2({width:'100%'});
    $('#townShip').select2({width:'100%'});



    const newSurveyModal = document.getElementById('newSurvey');

    newSurveyModal.addEventListener('shown.bs.modal', () => {
        setTimeout(() =>{
            google.maps.event.trigger(map, 'resize');
        },1000);
    });

    // initMapWithGeolocation();
    let stateData = [];
    $.getJSON('/data/db.json', (data) => {
        stateData = data.states;

        stateData.forEach((state) => {
            $('#state').append(new Option(`${state.mm}(${state.eng})`,state.eng));
        });
    });

    $('#state').on('change', function() {
        const selectedState = $(this).val();
        $('#townShip').empty().append('<option value="">Select Township</option>');
        const state = stateData.find(state => state.eng === selectedState);
        if(state) {
            const townShipSet = new Set();
            state.districts.forEach((district) => {
                district.townships.forEach(townShip => {
                    if(!townShipSet.has(townShip.eng)) {
                        townShipSet.add(townShip.eng);
                        $('#townShip').append(new Option(`${townShip.mm}(${townShip.eng})`,townShip.eng));
                    }
                });
            });
            const lat = state.lat || 20 ;
            const lng = state.lng || 90 ;
            $('#longitude').val(lng);
            $('#latitude').val(lat);
            initMap(lat,lng);

            $('#townShip').trigger('change.select2');
        }
    })
})


// Add Survey Modal Form Validation
function validateSurvey(form) {
    const customerName = $('#customerName').val().trim();
    const phoneNumber = $('#phoneNumber').val().trim();
    const state = $('#state').val();
    const township = $('#townShip').val();
    const longitude = $('#longitude').val().trim();
    const latitude = $('#latitude').val().trim();

    let hasError = false;

    $('input, select').removeClass('border-red-500');

    if (!customerName) {
        $('#customerName').addClass('border-red-500');
        hasError = true;
    }
    if (!phoneNumber) {
        $('#phoneNumber').addClass('border-red-500');
        hasError = true;
    }
    if (!state) {
        $('#state').next('.select2').find('.select2-selection').addClass('border-red-500');
        hasError = true;
    }
    if (!township) {
        $('#townShip').next('.select2').find('.select2-selection').addClass('border-red-500');
        hasError = true;
    }
    if (!longitude || isNaN(longitude)) {
        $('#longitude').addClass('border-red-500');
        hasError = true;
    }
    if (!latitude || isNaN(latitude)) {
        $('#latitude').addClass('border-red-500');
        hasError = true;
    }

    if (hasError) {
        Swal.fire({
            icon: 'warning',
            title: 'Please fill in all required fields correctly.',
            toast: true,
            position: 'top-end',
            showConfirmButton: false,
            timer: 3000
        });
        return false;
    }
    return true;
}

document.body.addEventListener("htmx:afterSwap", function(evt) {
    if (evt.detail.target.id === "myForm") {
        const modalEl = document.getElementById("newUser");
        if (modalEl) {
            modalEl.classList.remove("hidden"); // Tailwind
        }
    }
});