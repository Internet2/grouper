<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <div class="bread-header-container">
              <ul class="breadcrumb">
                <li><a href="#" onclick="return guiV2link('operation=UiV2Main.indexMain');">${textContainer.text['adminDaemonJobsHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li><a href="#" onclick="return guiV2link('operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li><a href="#" onclick="return guiV2link('operation=UiV2Admin.daemonJobs');">${textContainer.text['adminDaemonJobsBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li class="active">${textContainer.text['adminDaemonLogsBreadcrumb'] }</li>
              </ul>
              <div class="page-header blue-gradient">
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
                          <li><a href="#" onclick="return guiV2link('operation=UiV2GrouperLoader.editGrouperLoader&${grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).jobName.editQueryParam}'); return false;">${textContainer.text['grouperDaemonConfigEditJob'] }</a></li>
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
                
              </div>

            </div>

            <div class="row-fluid">
              <div class="span12">
                
                <form class="form-inline form-small form-filter" id="logFilterFormId">
                
                  <div class="row-fluid">
                    <div class="span2">
                      <label for="people-filter">${textContainer.text['grouperLoaderLogsFilterFor'] }</label>
                    </div>
                    <div class="span9" style="white-space: nowrap;">
                      ${grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).jobName}
                    </div>
                  </div>

                  <div class="row-fluid">
                    <div class="span2">
                      <span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                        data-original-title="${textContainer.textEscapeDouble['grouperLoaderLogsStartedTooltip']}">
                        <label for="startTimeFromId" class="control-label" style="white-space: nowrap">${textContainer.text['grouperLoaderLogsStartedTime'] }</label>
                      </span>
                    </div>
                    <div class="span9" style="white-space: nowrap;">
                      <input type="text" placeholder="${textContainer.text['grouperLoaderLogsTimePlaceholder'] }" name="startTimeFromName" id="startTimeFromId" style="width: 12em;" />
                      &nbsp;
                      <input type="text" placeholder="${textContainer.text['grouperLoaderLogsTimePlaceholder'] }" name="startTimeToName" id="startTimeToId" style="width: 12em;" />
                    </div>
                  </div>

                  <div class="row-fluid">
                    <div class="span2">
                      <span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                        data-original-title="${textContainer.textEscapeDouble['grouperLoaderLogsEndedTooltip']}">
                        <label for="endTimeFromId" class="control-label" style="white-space: nowrap">${textContainer.text['grouperLoaderLogsEndedTime'] }</label>
                      </span>
                    </div>
                    <div class="span9" style="white-space: nowrap;">
                      <input type="text" placeholder="${textContainer.text['grouperLoaderLogsTimePlaceholder'] }" name="endTimeFromName" id="endTimeFromId" style="width: 12em;" />
                      &nbsp;
                      <input type="text" placeholder="${textContainer.text['grouperLoaderLogsTimePlaceholder'] }" name="endTimeToName" id="endTimeToId" style="width: 12em;" />
                    </div>
                  </div>

                  <div class="row-fluid">
                    <div class="span2">
                      <span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                        data-original-title="${textContainer.textEscapeDouble['grouperLoaderLogsLastUpdatedTooltip']}">
                        <label for="lastUpdateTimeFromId" class="control-label" style="white-space: nowrap">${textContainer.text['grouperLoaderLogsLastUpdatedTime'] }</label>
                      </span>
                    </div>
                    <div class="span9" style="white-space: nowrap;">
                      <input type="text" placeholder="${textContainer.text['grouperLoaderLogsTimePlaceholder'] }" name="lastUpdateTimeFromName" id="lastUpdateTimeFromId" style="width: 12em;" />
                      &nbsp;
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
                      <input type="checkbox" name="showSubjobsName" id="showSubjobsId" value="true" /> ${textContainer.text['grouperLoaderLogsShowSubjobsLabel'] }
                    </div>
                  </div>

                  <div class="row-fluid">
                    <div class="span2">
                      <label for="statusSuccessId" class="control-label" style="white-space: nowrap">${textContainer.text['grouperLoaderLogsShowStatus'] }</label>
                    </div>
                    <div class="span9" style="white-space: nowrap;">
                    
                      <span style="white-space: nowrap;"><input type="checkbox" name="statusSuccessName" id="statusSuccessId" value="true" /> 
                        ${textContainer.text['grouperLoaderStatus_SUCCESS'] }</span> &nbsp;
                      <span style="white-space: nowrap;"><input type="checkbox" name="statusErrorName" id="statusErrorId" value="true" /> 
                        ${textContainer.text['grouperLoaderStatus_ERROR'] }</span> &nbsp;
                      <span style="white-space: nowrap;"><input type="checkbox" name="statusStartedName" id="statusStartedId" value="true" /> 
                        ${textContainer.text['grouperLoaderStatus_STARTED'] }</span> &nbsp;
                      <span style="white-space: nowrap;"><input type="checkbox" name="statusRunningName" id="statusRunningId" value="true" /> 
                        ${textContainer.text['grouperLoaderStatus_RUNNING'] }</span> &nbsp;
                      <span style="white-space: nowrap;"><input type="checkbox" name="statusWarningName" id="statusWarningId" value="true" /> 
                        ${textContainer.text['grouperLoaderStatus_WARNING'] }</span> &nbsp;
                      <span style="white-space: nowrap;"><input type="checkbox" name="statusConfigErrorName" id="statusConfigErrorId" value="true" /> 
                        ${textContainer.text['grouperLoaderStatus_CONFIG_ERROR'] }</span> &nbsp;
                      <span style="white-space: nowrap;"><input type="checkbox" name="statusSubjectProblemsName" id="statusSubjectProblemsId" value="true" /> 
                        ${textContainer.text['grouperLoaderStatus_SUBJECT_PROBLEMS'] }</span> &nbsp;
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
                      onclick="ajax('../app/UiV2Admin.viewLogsFilter?jobName=${grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).jobName}', {formIds: 'logFilterFormId'}); return false;"> 
                      &nbsp; 
                      <a class="btn" role="button" 
                        onclick="ajax('../app/UiV2Admin.viewLogs?jobName=${grouperRequestContainer.adminContainer.guiDaemonJobs.get(0).jobName}'); return false;"
                        >${textContainer.text['grouperLoaderButtonReset'] }</a>                                                                          
                    </div>
                  </div>
                </form>
                <br />
                <div id="grouperLoaderLogsResultsId"></div>
                
              </div>
            </div>
