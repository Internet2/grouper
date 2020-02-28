<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<script src="../../grouperExternal/public/assets/js/gantt-chart-d3.js" charset="utf-8"></script>
<script src="../../grouperExternal/public/assets/js/grouperJobChart.js" type="text/javascript"></script>


            <div class="bread-header-container">
              <ul class="breadcrumb">
                <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['guiBreadcrumbsHomeLabel'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li class="active">${textContainer.text['adminJobHistoryChart'] }</li>
              </ul>
              <div class="page-header blue-gradient">
                <h1>${textContainer.text['adminJobHistoryChart'] }</h1>
              </div>

            </div>
            <div class="row-fluid">
              <form class="form-inline form-small form-filter" id="jobHistoryFilterFormId">
                <div class="span12">
                  <label>${textContainer.text['myActivityFilterFor']}</label>
                  <label>${textContainer.text['myActivitySearchRangeFromPlaceholder']}</label>
                  <input type="text" class="span2" name="dateFrom" id="date-from" placeholder="yyyy/mm/dd" value="${grouperRequestContainer.adminContainer.guiJobHistoryDateFrom}"
                      required="true" />
                  <input type="text" class="span2" name="timeFrom" id="time-from" placeholder="hh:mm:ss" value="${grouperRequestContainer.adminContainer.guiJobHistoryTimeFrom}"
                      required="true" />

                  <label for="date-to">${textContainer.text['myActivitySearchRangeToPlaceholder']}</label>
                  <input type="text" class="span2" name="dateTo" id="date-to" placeholder="yyyy/mm/dd" value="${grouperRequestContainer.adminContainer.guiJobHistoryDateTo}"
                      required="true" />
                  <input type="text" class="span2" name="timeTo" id="time-to" placeholder="hh:mm:ss" value="${grouperRequestContainer.adminContainer.guiJobHistoryTimeTo}"
                    required="true" />
                </div>
                <div class="span12">
                  <label>${textContainer.text['adminJobHistoryMinExecutionFilter']}</label>
                  <input type="text" class="span2" name="minElapsedSeconds" id="min-elapsed-seconds" placeholder="seconds" value="${grouperRequestContainer.adminContainer.guiJobHistoryMinimumElapsedSeconds}"
                      required="true" />
                </div>
                <div class="span12">
                  <label>${textContainer.text['adminJobHistoryNamesLikeFilter']}</label>
                  <input type="text" class="span3" name="namesLikeFilter" id="names-like-filter" value="${grouperRequestContainer.adminContainer.guiJobHistoryNamesLikeFilter}"/>
                </div>
                <div class="span12">
                  <button type="submit" class="btn" id="jobHistoryChartSubmitButtonId"
                  onclick="ajax('../app/UiV2Admin.jobHistoryChart', {formIds: 'jobHistoryFilterFormId'}); return false;"
                  >Refresh view</button>

              </form>
            </div>
            <div id="jobHistoryChartPaneDiv"></div>
