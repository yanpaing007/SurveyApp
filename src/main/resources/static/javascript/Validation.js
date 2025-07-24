
function resetErrors() {
    $('input, select').removeClass('border-red-500');
    $('.error-message').addClass('hidden').text('');
}

async function checkEmailUniqueness(email) {
    try {
        const csrfToken = $("meta[name='_csrf']").attr("content");
        const csrfHeader = $("meta[name='_csrf_header']").attr("content");

        const response = await $.ajax({
            url: '/admin/api/check-email',
            method: 'POST',
            headers: {
                [csrfHeader]: csrfToken,
                'Content-Type': 'application/json'
            },
            data: JSON.stringify({ email: email }),
            dataType: 'json'
        });

        return response.isUnique;
    } catch (error) {
        console.error("Email check failed:", error);
        throw error;
    }
}



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
        showError('state', "*Please select a state");
        hasError = true;
    }
    if (!fields.township) {
        showError('townShip', "*Please enter township");
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


async function validateUser() {
    const fields = {
        fullName: $('#fullName').val().trim(),
        email: $('#email').val().trim(),
        phoneNumber: $('#phoneNumber').val().trim(),
        password: $('#password').val().trim(),
        role: $('#role').val()
    };

    let hasError = false;
    resetErrors();

    if (!fields.fullName) {
        showError('fullName', "*Please enter user's full name");
        hasError = true;
    } else if (fields.fullName.length < 6) {
        showError('fullName', "*Full Name must have at least 6 characters");
        hasError = true;
    }
    if (!fields.phoneNumber) {
        showError('phoneNumber', "*Please enter phone number");
        hasError = true;
    }
    if (!fields.email) {
        showError('email', "*Please enter email address");
        hasError = true;
    } else if (!isValidEmail(fields.email)) {
        showError('email', "*Please enter a valid email address");
        hasError = true;
    }else {
        try {
            const isUnique = await checkEmailUniqueness(fields.email);
            if (!isUnique) {
                showError('email', "*This email is already registered");
                hasError = true;
            }
        } catch (error) {
            console.error("Email check error:", error);
            showError('email', "*Error checking email availability");
            hasError = true;
        }
    }
    if (!fields.password) {
        showError('password', "*Please enter password");
        hasError = true;
    } else if (fields.password.length < 8) {
        showError('password', "*Password must be at least 8 characters");
        hasError = true;
    }
    if (!fields.role) {
        showError('role', "*Please select a role");
        hasError = true;
    }

    if (hasError) {
        showValidationToast();
        return false;
    }
    return true;
}

function showError(fieldId, message) {
    $(`#${fieldId}`).addClass('border-red-500');
    $(`#${fieldId}Error`).text(message).removeClass('hidden');
}

function isValidEmail(email) {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(email);
}

function showValidationToast() {
    Swal.fire({
        icon: 'warning',
        title: 'Please correct all the required fields correctly.',
        toast: true,
        position: 'top-end',
        showConfirmButton: false,
        timer: 3000
    });
}



$(document).ready(function() {
    $('#userForm').on('submit', async function(e) {
        e.preventDefault();
        const isValid = await validateUser();
        if (isValid) {
            this.submit();
        }
    });
});