//let daemonPerformanceData = {}; // returned from UiV2Admin.viewPerformanceChartResults

function drawDaemonPerformanceChartD3() {
    const c3_daemonPerformanceData = {
        x: 'date',
        columns: [
            ['date', ...daemonPerformanceData['date'].map(dt => new Date(dt*1000))]
        ]
    }

    for (const [key, value] of Object.entries(daemonPerformanceData)) {
        if (key != 'date') {
            c3_daemonPerformanceData.columns.push([key, ...value],)
        }
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
                    text: 'millis',
                    position: 'outer-middle'
                }
            }
        }
    });

}
