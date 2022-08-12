                <%@ include file="../assetsJsp/commonTaglib.jsp"%>
                
                <div class="row-fluid">
                  <div class="lead span9 pull-left"><h1>${textContainer.text['daemonJobsViewLogsTitle'] }</h1></div>
                  <div class="span2 pull-right">
                  
                    <div class="btn-group btn-block">
                      <a data-toggle="dropdown" href="#" aria-label="${textContainer.text['ariaLabelGuiMoreDaemonJobActions']}" id="more-action-button" class="btn btn-medium btn-block dropdown-toggle" aria-haspopup="true" aria-expanded="false" role="menu" onclick="$('#daemon-jobs-more-actions').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#daemon-jobs-more-actions li').first().focus();return true;});">
                        ${textContainer.text['adminDaemonJobsMoreActionsDefaultText'] } <span class="caret"></span>
                      </a>
                      <ul class="dropdown-menu dropdown-menu-right" id="daemon-jobs-more-actions">
                        <c:if test="${grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).loader == false}">
                          <li><a href="#" onclick="return guiV2link('operation=UiV2Admin.editDaemon&jobName=${grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).jobName}'); return false;">${textContainer.text['grouperDaemonConfigEditJob'] }</a></li>
                        </c:if>
                        <c:if test="${grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).loader == true}">
                          <li><a href="#" onclick="return guiV2link('operation=UiV2GrouperLoader.editGrouperLoader&${grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).editQueryParam}'); return false;">${textContainer.text['grouperDaemonConfigEditJob'] }</a></li>
                        </c:if>
                        <c:if test="${grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).failsafeNeedsApproval}" >
                          <li><a href="#" onclick="ajax('../app/UiV2Admin.daemonJobsSubmit?action=failsafeApprove&source=logs&jobName=${grouper:escapeUrl(grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).jobName)}'); return false;" >${textContainer.text['adminDaemonJobsMoreActionsFailsafeApprove'] }</a></li>
                        </c:if>
                        <c:if test="${grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).showMoreActionsRunNow}" >
                          <li><a href="#" onclick="ajax('../app/UiV2Admin.daemonJobsSubmit?action=runNow&source=logs&jobName=${grouper:escapeUrl(grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).jobName)}'); return false;" >${textContainer.text['adminDaemonJobsMoreActionsRunNow'] }</a></li>
                        </c:if>
                        <br />
                        <li class="dropdown-item disabled grouper-menu-subheader">${textContainer.text['adminDaemonJobsMoreActionsDelete']}</li>
                        <c:if test="${grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).multiple}">                        
                          <li><a href="#" onclick="if (confirm('${textContainer.textEscapeSingleDouble['grouperDaemonConfirmDeleteConfig']}')) { return guiV2link('operation=UiV2Admin.deleteDaemon&jobName=${grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).jobName}');}">${textContainer.text['grouperDaemonConfigDeleteJob'] }</a></li>
                        </c:if>
                        <c:if test="${grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).showMoreActionsDisable}" >
                          <li><a href="#" onclick="ajax('../app/UiV2Admin.daemonJobsSubmit?action=disable&source=logs&jobName=${grouper:escapeUrl(grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).jobName)}'); return false;" >${textContainer.text['adminDaemonJobsMoreActionsDisable'] }</a></li>
                        </c:if>
                        <c:if test="${grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).showMoreActionsEnable}" >
                          <li><a href="#" onclick="ajax('../app/UiV2Admin.daemonJobsSubmit?action=enable&source=logs&jobName=${grouper:escapeUrl(grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).jobName)}'); return false;" >${textContainer.text['adminDaemonJobsMoreActionsEnable'] }</a></li>
                        </c:if>
                      </ul>
                    </div>
                  
                  </div>
                </div>
                