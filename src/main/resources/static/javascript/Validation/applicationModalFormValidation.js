function validateApplication() {

    const fields = {
        customerName: $('#newApplication #customerName').val().trim(),
        address: $('#newApplication #address').val().trim(),
        phoneNumber: $('#newApplication #phoneNumber').val().trim(),
        contactEmail: $('#newApplication #contactEmail').val().trim(),
        longitude: $('#newApplication #longitude').val().trim(),
        latitude: $('#newApplication #latitude').val().trim()

    };

    let hasError = false;
    resetErrors();

    if (!fields.customerName) {
        showError('customerName', "*Please enter customer's full name");
        hasError = true;
    }
    if (!fields.address) {
        showError('address', "*Please enter customer address");
        hasError = true;
    }
    if (!fields.phoneNumber) {
        showError('phoneNumber', "*Please enter phone number");
        hasError = true;
    }
    if (!fields.contactEmail) {
        showError('contactEmail', "*Please enter contact email");
        hasError = true;
    } else if (!isValidEmail(fields.contactEmail)) {
        showError('contactEmail', "*Invalid email address");
        hasError = true;
    }
    if (!fields.longitude) {
        showError('longitude', "*Longitude is required");
        hasError = true;
    }
    if (!fields.latitude) {
        showError('latitude', "*Latitude is required");
        hasError = true;
    }
    if (hasError) {
        showValidationToast();
    }

    console.log(fields);

    return !hasError;
}