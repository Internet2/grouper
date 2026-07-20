//let daemonPerformanceData = {}; // returned from UiV2Admin.viewPerformanceChartResults

function drawDaemonPerformanceChartD3() {
    const millisValues = daemonPerformanceData['millis'] || [];
    const maxMillis = millisValues.length > 0 ? Math.max(...millisValues) : 0;

    // pick a single unit for the whole graph based on the largest value
    let divisor = 1;
    let unitLabel = 'millis';
    if (maxMillis >= 60000) {
        divisor = 60000;
        unitLabel = 'minutes';
    } else if (maxMillis > 10000) {
        divisor = 1000;
        unitLabel = 'seconds';
    }

    const convertedValues = millisValues.map(v => v / divisor);

    const c3_daemonPerformanceData = {
        x: 'date',
        columns: [
            ['date', ...daemonPerformanceData['date'].map(dt => new Date(dt*1000))],
            [unitLabel, ...convertedValues]
        ]
    }

    const chartPerformance = c3.generate({
        bindto: '#daemonPerformanceChart',
        data: c3_daemonPerformanceData,
        axis: {
            x: {
                type: 'timeseries',
                tick: {
                    format: '%Y-%m-%d %H:%M',
                    rotate: 90,
                    multiline: false
                }
            },
            y: {
                label: {
                    text: unitLabel,
                    position: 'outer-middle'
                }
            }
        },
        tooltip: {
            format: {
                value: function (value) {
                    const rounded = divisor === 1 ? value : Math.round(value * 100) / 100;
                    return rounded + ' ' + unitLabel;
                }
            }
        }
    });

}
