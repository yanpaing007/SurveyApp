//Function to Copy GeneratedId text for Application Details

function copyText(myValue, text) {
    const value = document.getElementById(myValue);
    console.log('value', myValue);
    const tooltip = document.getElementById(text);
    console.log(tooltip);


    navigator.clipboard.writeText(myValue).then(() => {
        tooltip.classList.add("opacity-100");
        tooltip.classList.remove("opacity-0");
    })

    setTimeout(() => {
        tooltip.classList.remove("opacity-100");
        tooltip.classList.add("opacity-0");
    }, 1000)
}


