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
            displayEmptyChart(ctxSurveyStatus,"No Survey data Available","Create your new survey to view data");
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
            displayEmptyChart(ctxApplicationStatus,"No Application Data Available","Create your new application to view data");
        }
    }




    let chart;

    function getChartConfig(type, range) {
        const isSurvey = type === 'survey';

        let labels = [];
        let datasets = [];

        if (range === '7') {
            labels = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
        } else if (range === '14') {
            labels = Array.from({ length: 14 }, (_, i) => `Day ${i + 1}`);
        } else if (range === '30') {
            labels = Array.from({ length: 30 }, (_, i) => `Day ${i + 1}`);
        } else if (range === 'monthly') {
            labels = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul"];
        }

        if (isSurvey) {

            datasets = [
                {
                    label: 'Total Surveys',
                    data: getRandomData(labels.length),
                    borderColor: 'rgba(75,192,192,1)',
                    backgroundColor: 'rgba(75,192,192,0.1)',
                    borderWidth: 1
                },
                {
                    label: 'Pending',
                    data: getRandomData(labels.length),
                    borderColor: 'orange',
                    backgroundColor: 'rgba(255,165,0,0.1)',
                    borderWidth: 1
                },
                {
                    label: 'Succeeded',
                    data: getRandomData(labels.length),
                    borderColor: 'green',
                    backgroundColor: 'rgba(0,128,0,0.1)',
                    borderWidth: 1
                },
                {
                    label: 'Failed',
                    data: getRandomData(labels.length),
                    borderColor: 'red',
                    backgroundColor: 'rgba(255,0,0,0.1)',
                    borderWidth: 1
                }
            ];
        } else {

            datasets = [
                {
                    label: 'Total Applications',
                    data: getRandomData(labels.length),
                    borderColor: 'rgba(54,162,235,1)',
                    backgroundColor: 'rgba(54,162,235,0.1)',
                    borderWidth: 1
                },
                {
                    label: 'Pending',
                    data: getRandomData(labels.length),
                    borderColor: 'orange',
                    backgroundColor: 'rgba(255,165,0,0.1)',
                    borderWidth: 1
                },
                {
                    label: 'Processing',
                    data: getRandomData(labels.length),
                    borderColor: 'purple',
                    backgroundColor: 'rgba(128,0,128,0.1)',
                    borderWidth: 1
                },
                {
                    label: 'Completed',
                    data: getRandomData(labels.length),
                    borderColor: 'green',
                    backgroundColor: 'rgba(0,128,0,0.1)',
                    borderWidth: 1
                },
                {
                    label: 'Cancelled',
                    data: getRandomData(labels.length),
                    borderColor: 'red',
                    backgroundColor: 'rgba(255,0,0,0.1)',
                    borderWidth: 1
                }
            ];
        }

        return {
            type: 'bar',
            data: {
                labels: labels,
                datasets: datasets
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
                        text: `${isSurvey ? 'Survey' : 'Application'} Chart (${range})`
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true
                    }
                }
            }
        };
    }

    function renderChart() {
        const type = $('#chartTypeSelect').val();
        const range = $('#rangeSelect').val();
        const ctx = document.getElementById('monthlySurveyChart').getContext('2d');

        if (chart) {
            chart.destroy();
        }

        chart = new Chart(ctx, getChartConfig(type, range));
    }

    function getRandomData(length) {
        return Array.from({ length }, () => Math.floor(Math.random() * 100));
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
