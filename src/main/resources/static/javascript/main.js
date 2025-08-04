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

