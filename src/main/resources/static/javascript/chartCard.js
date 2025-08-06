document.addEventListener("DOMContentLoaded", function () {
    const surveyData = document.getElementById("survey-data");
    const pendingSurvey = parseInt(surveyData.dataset.pending || 0);
    const successSurvey = parseInt(surveyData.dataset.success || 0);
    const failSurvey = parseInt(surveyData.dataset.failed || 0);

    const applicationData = document.getElementById("application-data");
    const pendingApplication = parseInt(applicationData.dataset.pending);
    const processingApplication = parseInt(applicationData.dataset.processing);
    const completeApplication = parseInt(applicationData.dataset.completed);
    const cancelledApplication = parseInt(applicationData.dataset.cancelled);

    const hasSurveyData = (pendingSurvey + successSurvey + failSurvey) > 0;
    const hasApplicationData = (pendingApplication+processingApplication+completeApplication+cancelledApplication) > 0;


    const ctxSurveyStatus = document.getElementById('surveyStatusChart');
    if (ctxSurveyStatus) {
        if(hasSurveyData) {
            new Chart(ctxSurveyStatus, {
                type: 'doughnut',
                data: {
                    labels: ['Pending', 'Succeeded', 'Failed'],
                    datasets: [{
                        label: 'Survey Status',
                        data: [pendingSurvey, successSurvey, failSurvey],
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
        else{
            displayEmptyChart(ctxSurveyStatus,"No Survey data Available","Create new survey to view data");
        }
    }


    const ctxApplicationStatus = document.getElementById('applicationStatusChart');
    if (ctxApplicationStatus) {
        if(hasApplicationData) {
            new Chart(ctxApplicationStatus, {
                type: 'doughnut',
                data: {
                    labels: ['Pending', 'Processing', 'Completed', 'Cancelled'],
                    datasets: [{
                        label: 'Application Status',
                        data: [pendingApplication,processingApplication,completeApplication,cancelledApplication],
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
        else{
            displayEmptyChart(ctxApplicationStatus,"No Application Data Available","Create new application to view data");
        }
    }


    let chart;
    function renderChart() {
        const type = $('#chartTypeSelect').val();
        const range = $('#rangeSelect').val();

        const ctx = document.getElementById('monthlySurveyChart').getContext('2d');

        if (chart) {
            chart.destroy();
        }

        $.get(`/admin/api/chart-data?type=${type}&range=${range}`, function (response) {

            const allValues = response.datasets.flatMap(ds => ds.data);
            const allZero = allValues.every(val => val === 0);

            if (allZero) {
                $('#monthlySurveyChart').hide();
                $('#chartMessage').text("No data available for the selected range.").show();
                return;
            } else {
                $('#chartMessage').hide();
                $('#monthlySurveyChart').show();
            }
            const config = {
                type: 'bar',
                data: {
                    labels: response.labels,
                    datasets: response.datasets.map(ds => ({
                        label: ds.label,
                        data: ds.data,
                        borderColor: getBorderColor(ds.label),
                        backgroundColor: getBackgroundColor(ds.label),
                        borderWidth: 2
                    }))
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    interaction: {
                        mode: 'index',
                        intersect: false
                    },
                    stacked: false,
                    plugins: {
                        title: {
                            display: true,
                            text: `${type === 'survey' ? 'Survey' : 'Application'} Chart (${range})`
                        }
                    },
                    scales: {
                        y: { beginAtZero: true }
                    }
                }
            };

            chart = new Chart(ctx, config);
        }).fail(() => {
            console.error("Failed to load chart data.");
        });
    }

    function getBorderColor(label) {
        const map = {
            'Total Surveys': 'rgba(75,192,192,1)',
            'PENDING': 'orange',
            'SUCCEEDED': 'green',
            'FAILED': 'red',
            'Total Applications': 'rgba(54,162,235,1)',
            'PROCESSING': 'purple',
            'COMPLETED': 'green',
            'CANCELLED': 'red'
        };
        return map[label] || 'black';
    }

    function getBackgroundColor(label) {
        const map = {
            'Total Surveys': 'rgba(75,192,192,0.1)',
            'PENDING': '#facc15',
            'SUCCEEDED': '#22c55e',
            'FAILED': '#ef4444',
            'Total Applications': 'rgba(54,162,235,0.1)',
            'PROCESSING': 'rgba(128,0,128,0.1)',
            'COMPLETED': 'rgba(0,128,0,0.1)',
            'CANCELLED': 'rgba(255,0,0,0.1)'
        };
        return map[label] || 'rgba(0,0,0,0.1)';
    }


    $(document).ready(function () {
        renderChart();

        $('#chartTypeSelect, #rangeSelect').on('change', function () {
            renderChart();
        });
    });


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


function displayEmptyChart(ctx, title, subtitle) {
    const parentElement = ctx.parentElement;

    ctx.style.display = 'none';

    const emptyContainer = document.createElement('div');
    emptyContainer.className = 'flex flex-col items-center justify-center h-full py-8 text-center';
    emptyContainer.innerHTML = `
            <div class="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mb-4">
                <i class="fa-solid fa-chart-pie text-2xl text-gray-400"></i>
            </div>
            <h3 class="text-sm font-medium text-gray-900 mb-1">${title}</h3>
            <p class="text-xs text-gray-500">${subtitle}</p>
        `;

    parentElement.appendChild(emptyContainer);
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
