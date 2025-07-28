function validateSurvey() {
    const fields = {
        customerName: $('#customerName').val().trim(),
        phoneNumber: $('#phoneNumber').val().trim(),
        state: $('#state').val(),
        township: $('#townShip').val().trim(),
        longitude: $('#longitude').val().trim(),
        latitude: $('#latitude').val().trim()
    };

    let hasError = false;
    resetErrors();

    if (!fields.customerName) {
        showError('customerName', "*Please enter customer name");
        hasError = true;
    }
    if (!fields.phoneNumber) {
        showError('phoneNumber', "*Please enter phone number");
        hasError = true;
    }
    if (!fields.state) {
        showError('stateDiv', "*Please select a state");
        hasError = true;
    }
    if (!fields.township) {
        showError('townShipDiv', "*Please enter township");
        hasError = true;
    }
    if (!fields.longitude || isNaN(fields.longitude)) {
        showError('longitude', "*Please enter valid longitude (numeric value)");
        hasError = true;
    }
    if (!fields.latitude || isNaN(fields.latitude)) {
        showError('latitude', "*Please enter valid latitude (numeric value)");
        hasError = true;
    }

    if (hasError) {
        showValidationToast();
        return false;
    }
    return true;
}