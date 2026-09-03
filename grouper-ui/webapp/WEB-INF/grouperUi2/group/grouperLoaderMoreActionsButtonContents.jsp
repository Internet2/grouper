<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                    <!-- start group/grouperLoaderMoreActionsButtonContents.jsp -->
                    <%-- NOTE, THIS IS DUPLICATED IN grouperLoaderOverall.jsp --%>

                    <div class="btn-group btn-block">
                    
                      <button type="button" data-toggle="dropdown" aria-label="${textContainer.text['ariaLabelGuiMoreGrouperLoaderActions']}" id="grouper-loader-more-action-button" aria-controls="grouper-loader-more-options" class="btn btn-medium btn-block dropdown-toggle" 
                        aria-haspopup="true" aria-expanded="false" onclick="$('#grouper-loader-more-options').is(':visible') === true ? $(this).attr('aria-expanded','false') : $(this).attr('aria-expanded',function(index, currentValue) { $('#grouper-loader-more-options li').first().focus();return true;});">
                          ${textContainer.text['grouperLoaderViewMoreActionsButton'] } <span class="caret"></span></button>
  
                      <ul class="dropdown-menu dropdown-menu-right" id="grouper-loader-more-options">

                        <c:if test="${grouperRequestContainer.grouperLoaderContainer.canEditLoader}" >
                          <c:if test="${grouperRequestContainer.grouperLoaderContainer.guiDaemonJob != null  && !grouperRequestContainer.grouperLoaderContainer.grouperRecentMembershipsLoader}" >
                            <c:if test="${grouperRequestContainer.grouperLoaderContainer.guiDaemonJob.showMoreActionsDisable}" >
                              <li><a href="?operation=UiV2GrouperLoader.disableJob&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2GrouperLoader.disableJob&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                                  >${textContainer.text['adminDaemonJobsMoreActionsDisable'] }</a></li>
                            </c:if>
                          </c:if>
                        </c:if>
                        
                        
                        <c:if test="${grouperRequestContainer.grouperLoaderContainer.canEditLoader or grouperRequestContainer.grouperLoaderContainer.canEditAbacOrRecentMembershipsLoader}" >
                          <li><a href="?operation=UiV2GrouperLoader.editGrouperLoader?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2GrouperLoader.editGrouperLoader?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                              >${textContainer.text['grouperLoaderEditConfiguration'] }</a></li>
                        </c:if>
                        
                         <c:if test="${grouperRequestContainer.grouperLoaderContainer.loaderGroup}" >
                          <li><a href="../app/UiV2GrouperLoader.exportLoaderConfig?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}">${textContainer.text['exportLoaderConfig'] }</a></li>
                        </c:if>
                        
                        <c:if test="${grouperRequestContainer.grouperLoaderContainer.canEditLoader && grouperRequestContainer.grouperLoaderContainer.failsafeIssue}" >
                          <li><a href="?operation=UiV2GrouperLoader.failsafeApprove?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2GrouperLoader.failsafeApprove?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                              >${textContainer.text['grouperLoaderFailsafeApprove'] }</a></li>
                        </c:if>

                        <c:if test="${grouperRequestContainer.grouperLoaderContainer.canEditLoader}" >
                          <c:if test="${grouperRequestContainer.grouperLoaderContainer.guiDaemonJob != null  && !grouperRequestContainer.grouperLoaderContainer.grouperRecentMembershipsLoader}" >
                            <c:if test="${grouperRequestContainer.grouperLoaderContainer.guiDaemonJob.showMoreActionsEnable}" >
                              <li><a href="?operation=UiV2GrouperLoader.enableJob&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2GrouperLoader.enableJob&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                                  >${textContainer.text['adminDaemonJobsMoreActionsEnable'] }</a></li>
                            </c:if>
                          </c:if>
                        </c:if>
                        
                         <c:if test="${!grouperRequestContainer.grouperLoaderContainer.loaderGroup}" >
                          <li><a href="?operation=UiV2GrouperLoader.importLoaderConfig?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2GrouperLoader.importLoaderConfig?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                            >${textContainer.text['importLoaderConfig'] }</a></li>
                        </c:if>

                        <c:if test="${grouperRequestContainer.grouperLoaderContainer.loaderGroup && !grouperRequestContainer.grouperLoaderContainer.grouperRecentMembershipsLoader}" >
                          <c:if test="${grouperRequestContainer.grouperLoaderContainer.canEditLoader}" >
                            <li><a href="?operation=UiV2GrouperLoader.loaderDiagnostics&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2GrouperLoader.loaderDiagnostics&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                              >${textContainer.text['grouperLoaderDiagnosticsButton'] }</a></li>
                          </c:if>
                        </c:if>

                        <c:if test="${grouperRequestContainer.grouperLoaderContainer.loaderGroup && !grouperRequestContainer.grouperLoaderContainer.grouperRecentMembershipsLoader && !grouperRequestContainer.grouperLoaderContainer.grouperJexlScriptLoader}" >
                          <c:if test="${grouperRequestContainer.grouperLoaderContainer.canEditLoader || mediaMap['uiV2.group.allowGroupAdminsToRefreshLoaderJobs']=='true'}" >
                            <li><button type="button" class="grouper-menuitem" onclick="ajax('../app/UiV2Group.updateLoaderGroup?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                              >${textContainer.text['groupRunLoaderProcessButton'] }</button></li>
                          </c:if>
                        </c:if>

                        <c:if test="${grouperRequestContainer.grouperLoaderContainer.canEditLoader}" >
                          <c:if test="${grouperRequestContainer.grouperLoaderContainer.loaderGroup && !grouperRequestContainer.grouperLoaderContainer.grouperRecentMembershipsLoader && !grouperRequestContainer.grouperLoaderContainer.grouperJexlScriptLoader}" >
                            <li><button type="button" class="grouper-menuitem" onclick="ajax('../app/UiV2Group.scheduleLoaderGroup?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                              >${textContainer.text['groupScheduleLoaderProcessButton'] }</button></li>
                          </c:if>
                        </c:if>
                              
                        <c:if test="${grouperRequestContainer.grouperLoaderContainer.loaderGroup && !grouperRequestContainer.grouperLoaderContainer.grouperRecentMembershipsLoader}" >
                          <li><a href="?operation=UiV2Group.viewAllLoaderManagedGroups?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Group.viewAllLoaderManagedGroups?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                            >${textContainer.text['groupViewAllLoaderManagedGroups'] }</a></li>
                        </c:if>

                        <c:if test="${grouperRequestContainer.grouperLoaderContainer.loaderGroup && !grouperRequestContainer.grouperLoaderContainer.grouperRecentMembershipsLoader}" >
                          <li><a href="?operation=UiV2GrouperLoader.viewLogs&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2GrouperLoader.viewLogs&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                              >${textContainer.text['grouperLoaderMoreActionsViewLoaderLogs'] }</a></li>
                        </c:if>

                        <li><a href="?operation=UiV2GrouperLoader.loader&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2GrouperLoader.loader&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                            >${textContainer.text['grouperLoaderMoreActionsViewLoader'] }</a></li>
                      </ul>
                    </div>

                    <!-- end group/grouperLoaderMoreActionsButtonContents.jsp -->
