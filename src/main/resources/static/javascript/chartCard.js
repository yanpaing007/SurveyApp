document.addEventListener("DOMContentLoaded", function () {
    const surveyStatusCounts = [45, 35, 100];
    const monthlySurveyLabels = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul"];
    const monthlySurveyData = [12, 19, 3, 5, 2, 100];
    const applicationStatusCounts = [30, 25, 80, 15];
    const monthlyApplicationLabels = ["Jan", "Feb", "Mar", "Apr", "May", "Jun"];
    const monthlyApplicationData = [8, 15, 7, 12, 5, 20];

    const ctxSurveyStatus = document.getElementById('surveyStatusChart');
    if (ctxSurveyStatus) {
        new Chart(ctxSurveyStatus, {
            type: 'doughnut',
            data: {
                labels: ['Pending', 'Succeeded', 'Failed'],
                datasets: [{
                    label: 'Survey Status',
                    data: surveyStatusCounts,
                    backgroundColor: ['#facc15', '#22c55e', '#ef4444'],
                    borderWidth: 1
                }]
            },

            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'left',
                        labels: {
                            usePointStyle: true,
                            pointStyle: 'circle',
                        }

                    }
                }
            }
        });
    }

    const ctxMonthlySurvey = document.getElementById('monthlySurveyChart');
    if (ctxMonthlySurvey) {
        new Chart(ctxMonthlySurvey, {
            type: 'line',
            data: {
                labels: monthlySurveyLabels,
                datasets: [{
                    label: 'Surveys',
                    data: monthlySurveyData,
                    fill: false,
                    borderColor: '#6366f1',
                    tension: 0.3
                },
                    {
                        label: 'Applications',
                        data: monthlyApplicationData,
                        fill: false,
                        borderColor: '#d10b23',
                        tension: 0.3
                    }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: {
                        beginAtZero: true
                    }
                }
            }
        });
    }

    const ctxApplicationStatus = document.getElementById('applicationStatusChart');
    if (ctxApplicationStatus) {
        new Chart(ctxApplicationStatus, {
            type: 'doughnut',
            data: {
                labels: ['Pending', 'Processing', 'Completed', 'Cancelled'],
                datasets: [{
                    label: 'Application Status',
                    data: applicationStatusCounts,
                    backgroundColor: ['#facc15', '#f97316', '#22c55e', '#ef4444'],
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'left',
                        labels: {
                            usePointStyle: true,
                            pointStyle: 'circle',
                        }
                    }
                }
            }
        });
    }

    const ctxMonthlyApplication = document.getElementById('monthlyApplicationChart');
    if (ctxMonthlyApplication) {
        new Chart(ctxMonthlyApplication, {
            type: 'line',
            data: {
                labels: monthlyApplicationLabels,
                datasets: [{
                    label: 'Applications',
                    data: monthlyApplicationData,
                    backgroundColor: '#818cf8',
                    borderColor: '#6366f1',
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: {
                        beginAtZero: true
                    }
                }
            }
        });
    }
});

function cleanAndSubmit(form) {
    const inputs = form.querySelectorAll("input, select");
    inputs.forEach(input => {
        if (!input.value.trim()) {
            input.removeAttribute("name");
        }
    });
    form.submit();
}

document.addEventListener("DOMContentLoaded", function () {
    const buttons = document.querySelectorAll(".createApplicationBtn");
    buttons.forEach(button => {
        button.addEventListener("click", function () {
            document.querySelector("#newApplication #surveyId").value = this.getAttribute("data-survey-id");
            document.querySelector("#newApplication #customerName").value = this.getAttribute("data-customer-name");
            document.querySelector("#newApplication #phoneNumber").value = this.getAttribute("data-phone-number");
            document.querySelector("#newApplication #address").value = this.getAttribute("data-address")
            document.querySelector("#newApplication #longitude").value = this.getAttribute("data-longitude");
            document.querySelector("#newApplication #latitude").value = this.getAttribute("data-latitude");
        })
    })
})
