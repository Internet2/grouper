var jobHistoryTasks = [];  // returned from UiV2Visualization.buildGraph

// mapping from task status to css class
var jobHistoryTaskStatus = {
    "STARTED": "status-started",
    "RUNNING": "status-running",
    "CONFIG_ERROR": "status-config_error",
    "SUBJECT_PROBLEMS": "status-subject_problems",
    "SUCCESS": "status-success",
    "WARNING": "status-warning",
    "ERROR": "status-error"
};

var jobHistoryTaskNames = [];

var jobHistoryChartSettings = {};

function jobHistoryChartInit() {
  if (jobHistoryTasks.length < 1) {
    $('#jobHistoryChartPaneDiv').hide();
    $('#jobHistoryNoData').show();
    return;
  }
  $('#jobHistoryChartPaneDiv').show();
  $('#jobHistoryNoData').hide();

    jobHistoryTasks.forEach(function(element){
        element.startDate = new Date(element.startDateString);
        element.endDate = new Date(element.endDateString);
    });

    var gantt = d3.gantt()
    gantt.taskTypes(jobHistoryTaskNames).taskStatus(jobHistoryTaskStatus);
    gantt.timeDomainMode("fixed");
    gantt.tickFormat(jobHistoryChartSettings.dateFormat);
    gantt.selector("#jobHistoryChartPaneDiv");
    //built-in width defective, looks at body, not selector
    gantt.width(document.querySelector("#jobHistoryChartPaneDiv").clientWidth - gantt.margin().right - gantt.margin().left - 5);
    gantt.timeDomain([new Date(jobHistoryChartSettings.startDateString), new Date(jobHistoryChartSettings.endDateString)]);

    gantt(jobHistoryTasks);

}


//$(document).ready(function() {
//    jobHistoryChartInit();
//});
