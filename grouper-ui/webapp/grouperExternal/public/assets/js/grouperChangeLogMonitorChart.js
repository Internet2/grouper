// GRP-7510: change log monitor graphs
// changeLogMonitorChartData is assigned by UiV2ChangeLogMonitor.changeLogMonitor:
//   dates: bucket start times (epoch millis)
//   tempName, tempValues: temp change log rows moved to the change log in each bucket
//   consumerSeries: [{name, values}] change log entries each consumer processed in each bucket
//   yLabel: label of the y axis

function changeLogMonitorChartDateColumn(data) {
  var dateColumn = ['date'];
  for (var i = 0; i < data.dates.length; i++) {
    dateColumn.push(new Date(data.dates[i]));
  }
  return dateColumn;
}

function changeLogMonitorChartAxis(data) {
  return {
    x: {
      type: 'timeseries',
      tick: {
        format: '%Y-%m-%d %H:%M',
        rotate: 45,
        multiline: false
      }
    },
    y: {
      min: 0,
      padding: { bottom: 0 },
      label: {
        text: data.yLabel,
        position: 'outer-middle'
      }
    }
  };
}

function drawChangeLogMonitorCharts() {
  if (typeof changeLogMonitorChartData === 'undefined' || changeLogMonitorChartData == null) {
    return;
  }
  var data = changeLogMonitorChartData;

  if (document.getElementById('changeLogMonitorTempChart') != null) {
    var tempColumn = [data.tempName];
    for (var i = 0; i < data.tempValues.length; i++) {
      tempColumn.push(data.tempValues[i]);
    }
    c3.generate({
      bindto: '#changeLogMonitorTempChart',
      data: {
        x: 'date',
        columns: [changeLogMonitorChartDateColumn(data), tempColumn],
        type: 'area'
      },
      point: { show: data.dates.length <= 100 },
      axis: changeLogMonitorChartAxis(data)
    });
  }

  if (document.getElementById('changeLogMonitorConsumersChart') != null && data.consumerSeries.length > 0) {
    var columns = [changeLogMonitorChartDateColumn(data)];
    for (var j = 0; j < data.consumerSeries.length; j++) {
      var series = data.consumerSeries[j];
      var column = [series.name];
      for (var k = 0; k < series.values.length; k++) {
        column.push(series.values[k]);
      }
      columns.push(column);
    }
    c3.generate({
      bindto: '#changeLogMonitorConsumersChart',
      data: {
        x: 'date',
        columns: columns
      },
      point: { show: data.dates.length <= 100 },
      axis: changeLogMonitorChartAxis(data)
    });
  }
}
