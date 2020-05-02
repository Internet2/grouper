<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<script src="../../grouperExternal/public/assets/js/gantt-chart-d3.js" charset="utf-8"></script>
<script src="../../grouperExternal/public/assets/js/grouperJobChart.js" type="text/javascript"></script>


            <div class="bread-header-container">
              <ul class="breadcrumb">
                <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['guiBreadcrumbsHomeLabel'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li><a href="#" onclick="return guiV2link('operation=UiV2Admin.daemonJobs');">${textContainer.text['adminDaemonJobsLink'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li class="active">${textContainer.text['adminJobHistoryChart'] }</li>
              </ul>
              <div class="page-header blue-gradient">
                <div class="row-fluid">
              
                <div class="span2 pull-right" id="daemonMoreActionsButtonContentsDivId">
                  <%@ include file="adminDaemonJobsMoreActionsButtonContents.jsp"%>
                </div>
                <div class="span10 pull-left">
                  <h1>${textContainer.text['adminJobHistoryChart'] }</h1>
                </div>
              </div>
              
            </div>
            <div class="row-fluid">
              <div class="lead span9">${textContainer.text['adminJobHistoryFilterLabel']}</div>


              <form class="form-inline form-small form-filter" id="jobHistoryFilterFormId">

                <table class="table table-condensed table-striped">
                  <tbody>

                    <tr>
                      <td style="vertical-align: top; white-space: nowrap;"><strong><label for="date-from">${textContainer.text['adminJobFilterFrom']}</label></strong></td>
                      <td>

                        <input type="text" class="span2" name="dateFrom" id="date-from" placeholder="${textContainer.text['adminJobHistoryDatePlaceholder']}" 
                            value="${grouperRequestContainer.adminContainer.guiJobHistoryDateFrom}"
                            required="required" />
                        <input type="text" class="span2" name="timeFrom" id="time-from" placeholder="${textContainer.text['adminJobHistoryTimePlaceholder']}" 
                            value="${grouperRequestContainer.adminContainer.guiJobHistoryTimeFrom}"
                            required="required" />
                        <br />                      
                        <span class="description">${textContainer.text['adminJobFilterFromHint']}</span>
                      </td>
                    </tr>
                    <tr>
                      <td style="vertical-align: top; white-space: nowrap;"><strong><label for="date-to">${textContainer.text['adminJobFilterTo']}</label></strong></td>
                      <td>

                        <input type="text" class="span2" name="dateTo" id="date-to" placeholder="${textContainer.text['adminJobHistoryDatePlaceholder']}" 
                            value="${grouperRequestContainer.adminContainer.guiJobHistoryDateTo}"
                            required="required" />
                        <input type="text" class="span2" name="timeTo" id="time-to" placeholder="${textContainer.text['adminJobHistoryTimePlaceholder']}" 
                            value="${grouperRequestContainer.adminContainer.guiJobHistoryTimeTo}"
                            required="required" />
                        <br />                      
                        <span class="description">${textContainer.text['adminJobFilterToHint']}</span>
                      </td>
                    </tr>
                    <tr>
                      <td style="vertical-align: top; white-space: nowrap;"><strong><label for="min-elapsed-seconds">${textContainer.text['adminJobHistoryMinExecutionFilter']}</label></strong></td>
                      <td>

                        <input type="text" class="span2" name="minElapsedSeconds" id="min-elapsed-seconds" placeholder="${textContainer.text['adminJobFilterPlaceholderSeconds']}" 
                          value="${grouperRequestContainer.adminContainer.guiJobHistoryMinimumElapsedSeconds}"
                          required="required" />
                        <br />                      
                        <span class="description">${textContainer.text['adminJobHistoryMinExecutionFilterHint']}</span>
                      </td>
                    </tr>
                    <tr>
                      <td style="vertical-align: top; white-space: nowrap;"><strong><label for="min-elapsed-seconds">${textContainer.text['adminJobHistoryNamesLikeFilter']}</label></strong></td>
                      <td>

                        <input type="text" class="span8" name="namesLikeFilter" id="names-like-filter" value="${grouperRequestContainer.adminContainer.guiJobHistoryNamesLikeFilter}"/>
                        <br />                      
                        <span class="description">${textContainer.text['adminJobHistoryNamesLikeFilterHint']}</span>
                      </td>
                    </tr>

                    <tr>
                      <td></td>
                      <td style="white-space: nowrap; padding-top: 1em; padding-bottom: 1em;">
                        <input type="submit" class="btn btn-primary"
                        aria-controls="jobHistoryFilterFormId" id="jobHistoryChartSubmitButtonId"
                        value="${textContainer.text['adminJobFilterRefreshButton'] }"
                        onclick="ajax('../app/UiV2Admin.jobHistoryChart', {formIds: 'jobHistoryFilterFormId'}); return false;">
                        
                      </td>
                    </tr>

                  </tbody>
                </table>
                
              </form>

            </div>
            <br />
            <div id="jobHistoryNoData" style="display: none;">${textContainer.text['adminJobHistoryNoData']}</div>
            <div id="jobHistoryChartPaneDiv"></div>
            <br />
            <br />