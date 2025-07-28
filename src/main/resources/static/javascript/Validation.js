function resetErrors() {
    $('input, select').removeClass('border-red-500');
    $('#stateDiv,#townShipDiv').removeClass('border border-red-500');
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

function showError(fieldId, message) {
    $(`#${fieldId}`).addClass('border border-red-500 rounded-md');
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
