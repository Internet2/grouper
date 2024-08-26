<%@ include file="../assetsJsp/commonTaglib.jsp"%>
                 <grouper:browserPage jspName="adminDaemonJobsContents" />
                 <input type="hidden" id="daemonJobsRefreshed" value="1" />
                 <table id="daemontable" class="table table-hover table-bordered table-striped table-condensed data-table table-bulk-update table-privileges footable">
                    <thead>
                      <tr>
                        <th data-hide="phone" style="white-space: nowrap; text-align: left;">
                          <span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                            data-original-title="${textContainer.textEscapeDouble['adminDaemonJobsColumnTooltipJobName']}">${textContainer.text['adminDaemonJobsColumnHeaderJobName'] }</span>
                        </th>
                        <th data-hide="phone" style="white-space: nowrap; text-align: left;">
                          <span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                            data-original-title="${textContainer.textEscapeDouble['adminDaemonJobsColumnTooltipState']}">${textContainer.text['adminDaemonJobsColumnHeaderState'] }</span>
                        </th>
                        <th data-hide="phone" style="white-space: nowrap; text-align: left;">
                          <span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                            data-original-title="${textContainer.textEscapeDouble['adminDaemonJobsColumnTooltipOverallStatus']}">${textContainer.text['adminDaemonJobsColumnHeaderOverallStatus'] }</span>
                        </th>
                        <th data-hide="phone" style="white-space: nowrap; text-align: left;">
                          <span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                            data-original-title="${textContainer.textEscapeDouble['adminDaemonJobsColumnTooltipLastRunStatus']}">${textContainer.text['adminDaemonJobsColumnHeaderLastRunStatus'] }</span>
                        </th>
                        <th data-hide="phone" style="white-space: nowrap; text-align: left;">
                          <span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                            data-original-title="${textContainer.textEscapeDouble['adminDaemonJobsColumnTooltipMoreActions']}">${textContainer.text['adminDaemonJobsColumnHeaderMoreActions'] }</span>
                        </th>
                        <th data-hide="phone" style="white-space: nowrap; text-align: left;">
                          <span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                            data-original-title="${textContainer.textEscapeDouble['adminDaemonJobsColumnTooltipSchedule']}">${textContainer.text['adminDaemonJobsColumnHeaderSchedule'] }</span>
                        </th>
                        <c:if test="${grouperRequestContainer.adminContainer.daemonJobsShowExtendedResults}" >
                          <th data-hide="phone" style="white-space: nowrap; text-align: left;">
                            <span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                              data-original-title="${textContainer.textEscapeDouble['adminDaemonJobsColumnTooltipNextFireTime']}">${textContainer.text['adminDaemonJobsColumnHeaderNextFireTime'] }</span>
                          </th>
                          <th data-hide="phone" style="white-space: nowrap; text-align: left;">
                            <span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                              data-original-title="${textContainer.textEscapeDouble['adminDaemonJobsColumnTooltipLastRunHost']}">${textContainer.text['adminDaemonJobsColumnHeaderLastRunHost'] }</span>
                          </th>
                          <th data-hide="phone" style="white-space: nowrap; text-align: left;">
                            <span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                              data-original-title="${textContainer.textEscapeDouble['adminDaemonJobsColumnTooltipLastRunStartTime']}">${textContainer.text['adminDaemonJobsColumnHeaderLastRunStartTime'] }</span>
                          </th>
                          <th data-hide="phone" style="white-space: nowrap; text-align: left;">
                            <span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                              data-original-title="${textContainer.textEscapeDouble['adminDaemonJobsColumnTooltipLastRunTotalTime']}">${textContainer.text['adminDaemonJobsColumnHeaderLastRunTotalTime'] }</span>
                          </th>
                          <th data-hide="phone" style="white-space: nowrap; text-align: left;">
                            <span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                              data-original-title="${textContainer.textEscapeDouble['adminDaemonJobsColumnTooltipLastRunSummary']}">${textContainer.text['adminDaemonJobsColumnHeaderLastRunSummary'] }</span>
                          </th>
                          <th data-hide="phone" style="white-space: nowrap; text-align: left;">
                            <span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                              data-original-title="${textContainer.textEscapeDouble['adminDaemonJobsColumnTooltipChangeLogInfo']}">${textContainer.text['adminDaemonJobsColumnHeaderChangeLogInfo'] }</span>
                          </th>
                        </c:if>
                      </tr>
                    </thead>
                    <tbody>
                      <c:forEach items="${grouperRequestContainer.adminContainer.guiDaemonJobs}" var="guiDaemonJob">
                        <tr class="adminDaemonJobEntry">
                          <td class="expand foo-clicker" style="white-space: nowrap;">
                            <a class="adminDaemonJobNameLink" href="?operation=UiV2Admin.viewLogs&jobName=${grouper:escapeUrl(guiDaemonJob.jobName)}"
                              onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Admin.viewLogs&jobName=${grouper:escapeUrl(guiDaemonJob.jobName)}');">${guiDaemonJob.jobName}</a>
                          </td>
                          <td class="expand foo-clicker"><span style='white-space: nowrap'>${guiDaemonJob.stateDescription}</span></td>
                          <td class="expand foo-clicker"
                            style="color: White;
                            background-color: ${guiDaemonJob.overallStatus == 'SUCCESS' ? 'green' : (guiDaemonJob.overallStatus == 'ERROR' ? 'red' : 'gray')};
                            font-weight: bold;">
                            <span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" class="grouperTooltip" style="border-bottom-color: white;"
                                data-original-title="${grouper:escapeHtml(guiDaemonJob.overallStatusDescription)}">${guiDaemonJob.overallStatus}</span>
                          </td>
                          <td class="expand foo-clicker"><span style='white-space: nowrap'>${guiDaemonJob.lastRunStatus}</span></td>
                          <td class="expand foo-clicker">
                            <div class="btn-group btn-block">
                              <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreDaemonJobActions']}" id="more-action-button" class="btn btn-medium btn-block dropdown-toggle" aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#daemon-jobs-more-actions').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#daemon-jobs-more-actions li').first().focus();return true;});">
                                ${textContainer.text['adminDaemonJobsMoreActionsDefaultText'] } <span class="caret"></span>
                              </a>
                              <ul class="dropdown-menu dropdown-menu-right" id="daemon-jobs-more-actions">
                                <c:if test="${guiDaemonJob.loader == false && guiDaemonJob.editable }">
	                                <li><a href="#" onclick="return guiV2link('operation=UiV2Admin.editDaemon&jobName=${guiDaemonJob.jobName}'); return false;">${textContainer.text['grouperDaemonConfigEditJob'] }</a></li>
                                </c:if>
                                <c:if test="${guiDaemonJob.loader == true}">
	                                <li><a href="#" onclick="return guiV2link('operation=UiV2GrouperLoader.editGrouperLoader&${guiDaemonJob.editQueryParam}'); return false;">${textContainer.text['grouperDaemonConfigEditJob'] }</a></li>
                                </c:if>
                                <c:if test="${guiDaemonJob.failsafeNeedsApproval}" >
                                  <li><a href="#" onclick="ajax('../app/UiV2Admin.daemonJobsSubmit?action=failsafeApprove&jobName=${grouper:escapeUrl(guiDaemonJob.jobName)}', {formIds: 'daemonJobsFilterFormId, daemonJobsPagingFormId, daemonJobsPagingFormPageNumberId'}); return false;" >${textContainer.text['adminDaemonJobsMoreActionsFailsafeApprove'] }</a></li>
                                </c:if>
                                <c:if test="${guiDaemonJob.showMoreActionsRunNow}" >
                                  <li><a href="#" onclick="ajax('../app/UiV2Admin.daemonJobsSubmit?action=runNow&jobName=${grouper:escapeUrl(guiDaemonJob.jobName)}', {formIds: 'daemonJobsFilterFormId, daemonJobsPagingFormId, daemonJobsPagingFormPageNumberId'}); return false;" >${textContainer.text['adminDaemonJobsMoreActionsRunNow'] }</a></li>
                                </c:if>
                                <li><a href="#" onclick="return guiV2link('operation=UiV2Admin.viewLogs&jobName=${guiDaemonJob.jobName}'); return false;">${textContainer.text['adminDaemonJobsMoreActionsLogs'] }</a></li>
                                <br />
                                <li class="dropdown-item disabled grouper-menu-subheader">${textContainer.text['adminDaemonJobsMoreActionsDelete']}</li>
                                <c:if test="${guiDaemonJob.multiple && guiDaemonJob.editable}">                        
                                  <li><a href="#" onclick="if (confirm('${textContainer.textEscapeSingleDouble['grouperDaemonConfirmDeleteConfig']}')) { return guiV2link('operation=UiV2Admin.deleteDaemon&jobName=${guiDaemonJob.jobName}');}">${textContainer.text['grouperDaemonConfigDeleteJob'] }</a></li>
                                </c:if>
                                <c:if test="${guiDaemonJob.showMoreActionsDisable}" >
                                  <li><a href="#" onclick="ajax('../app/UiV2Admin.daemonJobsSubmit?action=disable&jobName=${grouper:escapeUrl(guiDaemonJob.jobName)}', {formIds: 'daemonJobsFilterFormId, daemonJobsPagingFormId, daemonJobsPagingFormPageNumberId'}); return false;" >${textContainer.text['adminDaemonJobsMoreActionsDisable'] }</a></li>
                                </c:if>
                                <c:if test="${guiDaemonJob.showMoreActionsEnable}" >
                                  <li><a href="#" onclick="ajax('../app/UiV2Admin.daemonJobsSubmit?action=enable&jobName=${grouper:escapeUrl(guiDaemonJob.jobName)}', {formIds: 'daemonJobsFilterFormId, daemonJobsPagingFormId, daemonJobsPagingFormPageNumberId'}); return false;" >${textContainer.text['adminDaemonJobsMoreActionsEnable'] }</a></li>
                                </c:if>
                              </ul>
                            </div>
                          </td>
                          <td class="expand foo-clicker"><span style='white-space: nowrap'>${guiDaemonJob.schedule}</span></td>
                          <c:if test="${grouperRequestContainer.adminContainer.daemonJobsShowExtendedResults}" >
                            <td class="expand foo-clicker"><span style='white-space: nowrap'>${guiDaemonJob.nextFireTime}</span></td>
                            <td class="expand foo-clicker"><span style='white-space: nowrap'>${guiDaemonJob.lastRunHost}</span></td>
                            <td class="expand foo-clicker"><span style='white-space: nowrap'>${guiDaemonJob.lastRunStartTime}</span></td>
                            <td class="expand foo-clicker"><span style='white-space: nowrap'>${guiDaemonJob.lastRunTotalTime}</span></td>
                            <td class="expand foo-clicker"><span style='white-space: nowrap'>${guiDaemonJob.lastRunSummary}</span></td>
                            <td class="expand foo-clicker"><span style='white-space: nowrap'>${guiDaemonJob.changeLogInfo}</span></td>
                          </c:if>
                        </tr>
                      </c:forEach>
                    </tbody>
                  </table>
                  <div class="data-table-bottom gradient-background">
                    <grouper:paging2 guiPaging="${grouperRequestContainer.adminContainer.daemonJobsGuiPaging}" formName="daemonJobsPagingForm" ajaxFormIds="daemonJobsFilterFormId"
                      refreshOperation="../app/UiV2Admin.daemonJobsSubmit" />
                  </div>
            <script language="javascript">
              daemonJobsNextRefreshSeconds = daemonJobsRefreshSeconds;
              if (!daemonJobsRefreshInterval) { 
                daemonJobsRefreshInterval = setInterval(theFunction, 1000);
              }
            </script>