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

$(document).ready(function() {
    $('#userForm').on('submit', async function(e) {
        e.preventDefault();
        const isValid = await validateUser();
        if (isValid) {
            this.submit();
        }
    });
});