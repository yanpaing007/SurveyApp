document.addEventListener('DOMContentLoaded', e => {
    let mapInitialized = false;


        if (mapInitialized) return;

        const mapDiv = document.getElementById('ShowMap');
        const longitude = parseFloat(mapDiv.getAttribute('data-longitude')) || 96.193;
        const latitude = parseFloat(mapDiv.getAttribute('data-latitude')) || 16.863;
        const customerLocation = { lat: latitude, lng: longitude };

        map = new google.maps.Map(mapDiv, {
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

    document.addEventListener('alpine:init', () => {
    Alpine.data('statusChangeHandler', (initialStatus) => ({
        newStatus: initialStatus,
        prevStatus: initialStatus,
        showModal: false,
        

        confirmStatusChange(event) {
        this.newStatus = event.target.value;

        if (this.newStatus !== this.prevStatus) {
            this.showModal = true;
        }
    },

        cancelChange() {
            this.newStatus = this.prevStatus;
            this.$refs.statusSelect.value = this.prevStatus;
            this.showModal = false;
        },

        submitChange() {
            this.$refs.realSubmit.click();
            this.showModal = false;
        }
    }))
})

