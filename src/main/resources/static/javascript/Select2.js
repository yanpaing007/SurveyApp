// Select2 implementation for Survey Add Modal Form
$(document).ready(() => {
    initMap();
    $('#state').select2({width:'100%'});
    $('#townShip').select2({width:'100%'});


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