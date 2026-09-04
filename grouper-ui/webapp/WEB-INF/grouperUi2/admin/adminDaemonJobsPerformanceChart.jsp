<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<script src="../../grouperExternal/public/assets/js/grouperDaemonPerformanceChart.js" type="text/javascript"></script>

${grouper:titleFromKeyAndText('adminDaemonJobPageTitle', grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).jobName)}

            <div class="bread-header-container">
              <ul class="breadcrumb">
                <li><a href="?operation=UiV2Main.indexMain" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.indexMain');">${textContainer.text['adminDaemonJobsHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li><a href="?operation=UiV2Main.miscellaneous" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li><a href="?operation=UiV2Admin.daemonJobs" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Admin.daemonJobs');">${textContainer.text['adminDaemonJobsBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li class="active">${textContainer.text['daemonPerformanceChartBreadcrumb'] }</li>
              </ul>
              <div class="page-header blue-gradient" id="adminDaemonJobsMoreActionsId">

              </div>

            </div>

            <div class="row-fluid">
              <div class="span12">
                <div id="messages"></div>

                <p class="lead">${textContainer.text['daemonPerformanceChartDescription'] }</p>

                <div class="row-fluid">
                  <div class="span2" style="white-space: nowrap;">
                    <label>${textContainer.text['grouperLoaderLogsFilterFor'] }</label>
                  </div>
                  <div class="span9" style="white-space: nowrap;">
                    ${grouper:escapeHtml(grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).jobName)}
                  </div>
                </div>

                <div class="row-fluid">
                  <div class="span12">
                    <form class="form-inline form-small form-filter" id="daemonPerformanceChartFormId" method="post"
                          action="../app/UiV2Admin.viewPerformanceChartResults?jobName=${grouper:escapeUrl(grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).jobName)}">

                      <!-- time period type absolute/relative -->
                      <div class="row-fluid">
                        <div class="span1">
                          <label for="dateFromAbsoluteOptionId" style="white-space: nowrap;">${textContainer.text['groupHistoryChartRange'] }</label>
                        </div>
                        <div class="span4" style="white-space: nowrap;">
                          <button type="button" class="btn chartRangeOption" id="dateFromRelativeOptionId" aria-controls="dateFromRelativeOptionId" onclick="return showHideActivityChartFormDates(this)" data-html="true" data-delay-show="200" data-placement="right" rel="tooltip" data-original-title="${textContainer.text['groupHistoryChartBtnDateFromRelativeOptionTooltip'] }">${textContainer.text['groupHistoryChartBtnDateFromRelativeOption'] }</button>
                          <button type="button" class="btn chartRangeOption" id="dateFromAbsoluteOptionId" aria-controls="dateFromAbsoluteOptionId" onclick="return showHideActivityChartFormDates(this)" data-html="true" data-delay-show="200" data-placement="right" rel="tooltip" data-original-title="${textContainer.text['groupHistoryChartBtnDateFromAbsoluteOptionTooltip'] }">${textContainer.text['groupHistoryChartBtnDateFromAbsoluteOption'] }</button>
                          <input type="hidden" name="dateRangeType" id="dateRangeTypeId" />
                        </div>
                      </div>

                      <!-- relative span -->
                      <div id="date-range-relative-block-container" class="hide">
                        <div class="row-fluid">
                          <div class="span1">
                            <label for="dateFromRelativeId" style="white-space: nowrap;">${textContainer.text['groupHistoryChartTimePeriodFrom'] }</label>
                          </div>
                          <div class="span4" style="white-space: nowrap;">

                            <input type="text" name="dateFromRelative" id="dateFromRelativeId" class="span3" placeholder="${textContainer.text['groupHistoryChartRelativeScalePlaceholder'] }">

                            <select name="dateFromRelativeScale" id="dateFromRelativeScale" aria-label="${textContainer.textEscapeXml['groupHistoryChartRelativeScalePlaceholder']}" class="span4">
                              <option value="years">${textContainer.text['groupHistoryChartRelativeScaleYears'] }</option>
                              <option value="months">${textContainer.text['groupHistoryChartRelativeScaleMonths'] }</option>
                              <option value="days" selected>${textContainer.text['groupHistoryChartRelativeScaleDays'] }</option>
                              <option value="hours">${textContainer.text['groupHistoryChartRelativeScaleHours'] }</option>
                            </select>
                          </div>
                        </div>
                      </div>

                      <div id="date-range-absolute-block-container" class="hide">
                        <!-- absolute span from -->
                          <div class="row-fluid">
                            <div class="span1">
                              <label for="dateFromAbsoluteId" style="white-space: nowrap;">${textContainer.text['groupHistoryChartTimePeriodFrom'] }</label>
                            </div>
                            <div class="span5" style="white-space: nowrap;">
                              <input type="datetime-local" step="1" class="span8" name="dateFromAbsolute"  placeholder="${textContainer.text['membershipEditDatePlaceholder'] }" id="dateFromAbsoluteId">
                            </div>
                          </div>

                          <!-- absolute span to -->
                          <div class="row-fluid">
                            <div class="span1">
                              <label for="dateToAbsoluteId" style="white-space: nowrap;">${textContainer.text['groupHistoryChartTimePeriodTo'] }</label>
                            </div>
                            <div class="span5" style="white-space: nowrap;">
                              <input type="datetime-local" step="1" class="span8" name="dateToAbsolute"  placeholder="${textContainer.text['membershipEditDatePlaceholder'] }" id="dateToAbsoluteId">
                            </div>
                          </div>
                      </div>

                      <input type="hidden" id="btnActionId" name="action" value="graph" />
                      <input type="hidden" name="<csrf:token-name/>" value="<csrf:token-value />"/><!-- needed for the export -->
                      <button type="submit" class="btn"
                              onclick="document.getElementById('btnActionId').value='graph'; ajax('../app/UiV2Admin.viewPerformanceChartResults?jobName=${grouper:escapeUrl(grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).jobName)}', {formIds: 'daemonPerformanceChartFormId'}); return false;"
                      >${textContainer.text['groupHistoryChartActionShowChart']}</button>
                      <button type="submit" class="btn"
                              onclick="document.getElementById('btnActionId').value='export'; return true"
                      >${textContainer.text['groupHistoryChartActionExportData']}</button>
                    </form>

                  </div>
                </div>
                <script type="text/javascript">
                  $(document).ready(showHideActivityChartFormDates(document.getElementById('dateFromRelativeOptionId')))
                </script>

                <div id="grouperDaemonPerformanceChartDivId" role="region" aria-live="polite">
                </div>
              </div>
            </div>
