<%@ include file="../assetsJsp/commonTaglib.jsp"%>

${grouper:titleFromKeyAndText('adminDaemonJobPageTitle', grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).jobName)}

            <div class="bread-header-container">
              <ul class="breadcrumb">
                <li><a href="?operation=UiV2Main.indexMain" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.indexMain');">${textContainer.text['adminDaemonJobsHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li><a href="?operation=UiV2Main.miscellaneous" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li><a href="?operation=UiV2Admin.daemonJobs" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Admin.daemonJobs');">${textContainer.text['adminDaemonJobsBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li class="active">${textContainer.text['adminDaemonLogsBreadcrumb'] }</li>
              </ul>
              <div class="page-header blue-gradient" id="adminDaemonJobsMoreActionsId">

              </div>

            </div>

            <div class="row-fluid">
              <div class="span12">
                
                <form class="form-inline form-filter" id="logFilterFormId">
                
                  <%-- which job these logs are for (restored after GRP-7310 removed it by accident).  a span, not a
                       label: there is no input to label --%>
                  <div class="row-fluid">
                    <div class="span2">
                      <span class="control-label" style="white-space: nowrap; font-weight: bold;">${textContainer.text['grouperLoaderLogsFilterFor'] }</span>
                    </div>
                    <div class="span9" style="white-space: nowrap;">
                      ${grouper:escapeHtml(grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).jobName)}
                    </div>
                  </div>

                  <%-- GRP-6905: for a report job, the report and the group or folder it runs on --%>
                  <c:if test="${grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).reportJob && not empty grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).reportOwnerId}">
                    <c:set var="daemonReportOwnerId" value="${grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).reportOwnerId}" />
                    <c:set var="daemonReportMarkerId" value="${grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).reportAttributeAssignmentMarkerId}" />
                    <div class="row-fluid">
                      <div class="span2">
                        <span class="control-label" style="white-space: nowrap; font-weight: bold;">${textContainer.text['daemonJobsViewLogsReportLabel'] }</span>
                      </div>
                      <div class="span9">
                        <c:if test="${not empty grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).reportConfigName}">
                          ${grouper:escapeHtml(grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).reportConfigName)}
                          ${textContainer.text['daemonJobsViewLogsReportOnLabel'] }
                        </c:if>
                        <c:choose>
                          <c:when test="${grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).reportOwnerStem}">
                            <a href="?operation=UiV2Stem.viewStem&stemId=${daemonReportOwnerId}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Stem.viewStem&stemId=${daemonReportOwnerId}'); return false;">${grouper:escapeHtml(grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).reportOwnerDisplayName)}</a>
                            &nbsp;
                            <c:choose>
                              <c:when test="${not empty daemonReportMarkerId}">
                                <a href="?operation=UiV2GrouperReport.viewAllReportInstancesForFolder&attributeAssignmentMarkerId=${daemonReportMarkerId}&stemId=${daemonReportOwnerId}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2GrouperReport.viewAllReportInstancesForFolder&attributeAssignmentMarkerId=${daemonReportMarkerId}&stemId=${daemonReportOwnerId}'); return false;">(${textContainer.text['adminDaemonJobsMoreActionsGoToReport'] })</a>
                              </c:when>
                              <c:otherwise>
                                <a href="?operation=UiV2GrouperReport.viewReportConfigsOnFolder&stemId=${daemonReportOwnerId}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2GrouperReport.viewReportConfigsOnFolder&stemId=${daemonReportOwnerId}'); return false;">(${textContainer.text['adminDaemonJobsMoreActionsGoToReport'] })</a>
                              </c:otherwise>
                            </c:choose>
                          </c:when>
                          <c:otherwise>
                            <a href="?operation=UiV2Group.viewGroup&groupId=${daemonReportOwnerId}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Group.viewGroup&groupId=${daemonReportOwnerId}'); return false;">${grouper:escapeHtml(grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).reportOwnerDisplayName)}</a>
                            &nbsp;
                            <c:choose>
                              <c:when test="${not empty daemonReportMarkerId}">
                                <a href="?operation=UiV2GrouperReport.viewAllReportInstancesForGroup&attributeAssignmentMarkerId=${daemonReportMarkerId}&groupId=${daemonReportOwnerId}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2GrouperReport.viewAllReportInstancesForGroup&attributeAssignmentMarkerId=${daemonReportMarkerId}&groupId=${daemonReportOwnerId}'); return false;">(${textContainer.text['adminDaemonJobsMoreActionsGoToReport'] })</a>
                              </c:when>
                              <c:otherwise>
                                <a href="?operation=UiV2GrouperReport.viewReportConfigsOnGroup&groupId=${daemonReportOwnerId}" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2GrouperReport.viewReportConfigsOnGroup&groupId=${daemonReportOwnerId}'); return false;">(${textContainer.text['adminDaemonJobsMoreActionsGoToReport'] })</a>
                              </c:otherwise>
                            </c:choose>
                          </c:otherwise>
                        </c:choose>
                      </div>
                    </div>
                  </c:if>

                                    <div class="row-fluid" role="group" aria-labelledby="startTimeGroupLabel">
                    <div class="span2">
                      <span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" data-original-title="${textContainer.textEscapeDouble['grouperLoaderLogsStartedTooltip']}">
                        <span class="control-label" style="white-space: nowrap; font-weight: bold;" id="startTimeGroupLabel">${textContainer.text['grouperLoaderLogsStartedTime'] }</span>
                      </span>
                    </div>
                    <div class="span9" style="white-space: nowrap;">
                      <label for="startTimeFromId" class="visually-hidden">${textContainer.text['guiFrom']}</label>
                      <input type="text" placeholder="${textContainer.text['grouperLoaderLogsTimePlaceholder'] }" name="startTimeFromName" id="startTimeFromId" style="width: 12em;" />
                      &nbsp;
                      <label for="startTimeToId" class="visually-hidden">${textContainer.text['guiTo']}</label>
                      <input type="text" placeholder="${textContainer.text['grouperLoaderLogsTimePlaceholder'] }" name="startTimeToName" id="startTimeToId" style="width: 12em;" />
                    </div>
                  </div>

                                    <div class="row-fluid" role="group" aria-labelledby="endTimeGroupLabel">
                    <div class="span2">
                      <span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" data-original-title="${textContainer.textEscapeDouble['grouperLoaderLogsEndedTooltip']}">
                        <span class="control-label" style="white-space: nowrap; font-weight: bold;" id="endTimeGroupLabel">${textContainer.text['grouperLoaderLogsEndedTime'] }</span>
                      </span>
                    </div>
                    <div class="span9" style="white-space: nowrap;">
                      <label for="endTimeFromId" class="visually-hidden">${textContainer.text['guiFrom']}</label>
                      <input type="text" placeholder="${textContainer.text['grouperLoaderLogsTimePlaceholder'] }" name="endTimeFromName" id="endTimeFromId" style="width: 12em;" />
                      &nbsp;
                      <label for="endTimeToId" class="visually-hidden">${textContainer.text['guiTo']}</label>
                      <input type="text" placeholder="${textContainer.text['grouperLoaderLogsTimePlaceholder'] }" name="endTimeToName" id="endTimeToId" style="width: 12em;" />
                    </div>
                  </div>

                                    <div class="row-fluid" role="group" aria-labelledby="lastUpdateTimeGroupLabel">
                    <div class="span2">
                      <span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" data-original-title="${textContainer.textEscapeDouble['grouperLoaderLogsLastUpdatedTooltip']}">
                        <span class="control-label" style="white-space: nowrap; font-weight: bold;" id="lastUpdateTimeGroupLabel">${textContainer.text['grouperLoaderLogsLastUpdatedTime'] }</span>
                      </span>
                    </div>
                    <div class="span9" style="white-space: nowrap;">
                      <label for="lastUpdateTimeFromId" class="visually-hidden">${textContainer.text['guiFrom']}</label>
                      <input type="text" placeholder="${textContainer.text['grouperLoaderLogsTimePlaceholder'] }" name="lastUpdateTimeFromName" id="lastUpdateTimeFromId" style="width: 12em;" />
                      &nbsp;
                      <label for="lastUpdateTimeToId" class="visually-hidden">${textContainer.text['guiTo']}</label>
                      <input type="text" placeholder="${textContainer.text['grouperLoaderLogsTimePlaceholder'] }" name="lastUpdateTimeToName" id="lastUpdateTimeToId" style="width: 12em;" />
                    </div>
                  </div>
                  
                  <div class="row-fluid">
                    <div class="span2">
                      <span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                        data-original-title="${textContainer.textEscapeDouble['grouperLoaderLogsShowSubjobsTooltip']}">
                        <label for="showSubjobsId" class="control-label" style="white-space: nowrap">${textContainer.text['grouperLoaderLogsShowSubjobs'] }</label>
                      </span>
                    </div>
                    <div class="span9" style="white-space: nowrap;">
                      <input type="checkbox" name="showSubjobsName" id="showSubjobsId" value="true" ${grouperRequestContainer.adminContainer.daemonLogsShowSubJobs? 'checked="checked"' : ''} /> ${textContainer.text['grouperLoaderLogsShowSubjobsLabel'] }
                    </div>
                  </div>

                  <div class="row-fluid">
                    <div class="span2">
                      <label for="daemonLogsStatusFilterId" class="control-label" style="white-space: nowrap">${textContainer.text['daemonJobsStatusSearchNamePlaceholder'] }</label>
                    </div>
                    <div class="span4" style="white-space: nowrap;">
                      <select name="daemonLogsStatusFilter" id="daemonLogsStatusFilterId">
                        <option value="" style="color:#aaaaaa !important">${textContainer.textEscapeXml['daemonJobsStatusSearchNamePlaceholder'] }</option>
                        <c:forEach items="${grouperRequestContainer.adminContainer.daemonLogStatusFilters}" var="daemonLogsStatusFilter" >
                          <option value="${grouper:escapeHtml(daemonLogsStatusFilter.value)}">
                              ${grouper:escapeHtml(daemonLogsStatusFilter.name) }
                          </option>
                        </c:forEach>
                      </select>
                    </div>
                  </div>

                  <div class="row-fluid">
                    <div class="span2">
                      <span class="control-label" style="white-space: nowrap; font-weight: bold;">${textContainer.text['grouperLoaderLogsFilterZeroCount'] }:</span>
                    </div>
                    <div class="span9" style="white-space: nowrap;">

                      <label style="white-space: nowrap;"><input type="checkbox" name="filterZeroCountTotal" id="filterZeroCountTotalId" value="true" />
                        ${textContainer.text['grouperLoaderZeroFilter_Total'] }</label> &nbsp;
                      <label style="white-space: nowrap;"><input type="checkbox" name="filterZeroCountCrud" id="filterZeroCountCrudId" value="true" />
                        ${textContainer.text['grouperLoaderZeroFilter_CRUD'] }</label> &nbsp;
                    </div>
                  </div>

                  <div class="row-fluid">
                    <div class="span2">
                      <label for="numberOfRowsId" class="control-label" style="white-space: nowrap">${textContainer.text['grouperLoaderLogsNumberOfRows'] }</label>
                    </div>
                    <div class="span9" style="white-space: nowrap;">
                      <input type="text" name="numberOfRowsName" id="numberOfRowsId" style="width: 5em;" 
                        value="${grouperRequestContainer.adminContainer.daemonJobsViewLogsNumberOfRows}" />
                    </div>
                  </div>

                  <div class="row-fluid" style="margin-top: 1em">

                    <div class="span3"></div>
                    <div class="span6" style="white-space: nowrap"><input type="submit" class="btn" aria-controls="groupFilterResultsId" id="filterSubmitId" 
                      value="${textContainer.text['grouperLoaderButtonApplyFilter'] }" 
                      onclick="ajax('../app/UiV2Admin.viewLogsFilter?jobName=${grouper:escapeUrl(grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).jobName)}', {formIds: 'logFilterFormId'}); return false;"> 
                      &nbsp; 
                      <a class="btn" role="button" 
                        onclick="ajax('../app/UiV2Admin.viewLogs?jobName=${grouper:escapeUrl(grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).jobName)}'); return false;"
                        >${textContainer.text['grouperLoaderButtonReset'] }</a>                                                                          
                    </div>
                  </div>
                </form>
                <br />
                <div id="grouperLoaderLogsResultsId"></div>
                
              </div>
            </div>
