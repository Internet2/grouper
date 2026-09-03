                <%@ include file="../assetsJsp/commonTaglib.jsp"%>
                
                <div class="row-fluid">
                  <div class="lead span9 pull-left"><h1>${textContainer.text['daemonJobsViewLogsTitle'] }</h1></div>
                  <div class="span2 pull-right">
                  
                    <div class="btn-group btn-block">
                      <button type="button" data-toggle="dropdown" aria-label="${textContainer.text['ariaLabelGuiMoreDaemonJobActions']}" id="more-action-button" class="btn btn-medium btn-block dropdown-toggle" aria-haspopup="true" aria-expanded="false" onclick="$('#daemon-jobs-more-actions').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#daemon-jobs-more-actions li').first().focus();return true;});">
                        ${textContainer.text['adminDaemonJobsMoreActionsDefaultText'] } <span class="caret"></span>
                      </button>
                      <ul class="dropdown-menu dropdown-menu-right" id="daemon-jobs-more-actions">
                        <c:if test="${grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).loader == false && grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).editable}">
                          <li><a href="?operation=UiV2Admin.editDaemon&jobName=${grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).jobName}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Admin.editDaemon&jobName=${grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).jobName}'); return false;">${textContainer.text['grouperDaemonConfigEditJob'] }</a></li>
                        </c:if>
                        <c:if test="${grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).loader == true}">
                          <li><a href="?operation=UiV2GrouperLoader.editGrouperLoader&${grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).editQueryParam}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2GrouperLoader.editGrouperLoader&${grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).editQueryParam}'); return false;">${textContainer.text['grouperDaemonConfigEditJob'] }</a></li>
                        </c:if>
                        <c:if test="${grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).failsafeNeedsApproval}" >
                          <li><button type="button" class="grouper-menuitem" onclick="ajax('../app/UiV2Admin.daemonJobsSubmit?action=failsafeApprove&source=logs&jobName=${grouper:escapeUrl(grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).jobName)}'); return false;" >${textContainer.text['adminDaemonJobsMoreActionsFailsafeApprove'] }</button></li>
                        </c:if>
                        <c:if test="${grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).showMoreActionsRunNow}" >
                          <li><button type="button" class="grouper-menuitem" onclick="ajax('../app/UiV2Admin.daemonJobsSubmit?action=runNow&source=logs&jobName=${grouper:escapeUrl(grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).jobName)}'); return false;" >${textContainer.text['adminDaemonJobsMoreActionsRunNow'] }</button></li>
                        </c:if>
                        <li><a href="?operation=UiV2Admin.viewLogs&jobName=${grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).jobName}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Admin.viewLogs&jobName=${grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).jobName}'); return false;">${textContainer.text['adminDaemonJobsMoreActionsLogs'] }</a></li>
                        <li><a href="?operation=UiV2Admin.viewPerformanceChart&jobName=${grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).jobName}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Admin.viewPerformanceChart&jobName=${grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).jobName}'); return false;">${textContainer.text['adminDaemonJobsMoreActionsPerformance'] }</a></li>
                        <c:if test="${grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).reportJob && not empty grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).reportOwnerId}">
                          <c:set var="daemonReportOwnerId" value="${grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).reportOwnerId}" />
                          <c:set var="daemonReportMarkerId" value="${grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).reportAttributeAssignmentMarkerId}" />
                          <c:choose>
                            <c:when test="${grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).reportOwnerStem}">
                              <li><a href="?operation=UiV2Stem.viewStem&stemId=${daemonReportOwnerId}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Stem.viewStem&stemId=${daemonReportOwnerId}'); return false;">${textContainer.text['adminDaemonJobsMoreActionsReportViewFolder'] }</a></li>
                              <li><a href="?operation=UiV2GrouperReport.viewReportConfigsOnFolder&stemId=${daemonReportOwnerId}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2GrouperReport.viewReportConfigsOnFolder&stemId=${daemonReportOwnerId}'); return false;">${textContainer.text['adminDaemonJobsMoreActionsReportActions'] }</a></li>
                              <c:if test="${not empty daemonReportMarkerId}">
                                <li><a href="?operation=UiV2GrouperReport.viewAllReportInstancesForFolder&attributeAssignmentMarkerId=${daemonReportMarkerId}&stemId=${daemonReportOwnerId}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2GrouperReport.viewAllReportInstancesForFolder&attributeAssignmentMarkerId=${daemonReportMarkerId}&stemId=${daemonReportOwnerId}'); return false;">${textContainer.text['adminDaemonJobsMoreActionsReportInstances'] }</a></li>
                              </c:if>
                            </c:when>
                            <c:otherwise>
                              <li><a href="?operation=UiV2Group.viewGroup&groupId=${daemonReportOwnerId}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Group.viewGroup&groupId=${daemonReportOwnerId}'); return false;">${textContainer.text['adminDaemonJobsMoreActionsReportViewGroup'] }</a></li>
                              <li><a href="?operation=UiV2GrouperReport.viewReportConfigsOnGroup&groupId=${daemonReportOwnerId}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2GrouperReport.viewReportConfigsOnGroup&groupId=${daemonReportOwnerId}'); return false;">${textContainer.text['adminDaemonJobsMoreActionsReportActions'] }</a></li>
                              <c:if test="${not empty daemonReportMarkerId}">
                                <li><a href="?operation=UiV2GrouperReport.viewAllReportInstancesForGroup&attributeAssignmentMarkerId=${daemonReportMarkerId}&groupId=${daemonReportOwnerId}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2GrouperReport.viewAllReportInstancesForGroup&attributeAssignmentMarkerId=${daemonReportMarkerId}&groupId=${daemonReportOwnerId}'); return false;">${textContainer.text['adminDaemonJobsMoreActionsReportInstances'] }</a></li>
                              </c:if>
                            </c:otherwise>
                          </c:choose>
                        </c:if>
                        <br />
                        <li class="dropdown-item disabled grouper-menu-subheader">${textContainer.text['adminDaemonJobsMoreActionsDelete']}</li>
                        <c:if test="${grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).multiple && grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).editable}">                        
                          <li><a href="#" onclick="if (confirm('${textContainer.textEscapeSingleDouble['grouperDaemonConfirmDeleteConfig']}')) { return guiV2link('operation=UiV2Admin.deleteDaemon&jobName=${grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).jobName}');}">${textContainer.text['grouperDaemonConfigDeleteJob'] }</a></li>
                        </c:if>
                        <c:if test="${grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).showMoreActionsDisable}" >
                          <li><button type="button" class="grouper-menuitem" onclick="ajax('../app/UiV2Admin.daemonJobsSubmit?action=disable&source=logs&jobName=${grouper:escapeUrl(grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).jobName)}'); return false;" >${textContainer.text['adminDaemonJobsMoreActionsDisable'] }</button></li>
                        </c:if>
                        <c:if test="${grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).showMoreActionsEnable}" >
                          <li><button type="button" class="grouper-menuitem" onclick="ajax('../app/UiV2Admin.daemonJobsSubmit?action=enable&source=logs&jobName=${grouper:escapeUrl(grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).jobName)}'); return false;" >${textContainer.text['adminDaemonJobsMoreActionsEnable'] }</button></li>
                        </c:if>
                      </ul>
                    </div>
                  
                  </div>
                </div>
                