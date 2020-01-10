<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.groupContainer.guiGroup.group.parentUuid}" />

            <%-- show the add member button for privileges --%>
            <c:set target="${grouperRequestContainer.groupContainer}" property="showAddMember" value="false" />
            <%@ include file="groupHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12">
                <ul class="nav nav-tabs">
                  <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Group.viewGroup&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {dontScrollTop: true});" >${textContainer.text['groupMembersTab'] }</a></li>
                  <c:if test="${grouperRequestContainer.groupContainer.canAdmin}">
                    <li><a role="tab" href="#" onclick="return guiV2link('operation=UiV2Group.groupPrivileges&groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {dontScrollTop: true});" >${textContainer.text['groupPrivilegesTab'] }</a></li>
                  </c:if>
                  <%@ include file="groupMoreTab.jsp" %>
                </ul>
                <div class="row-fluid">
                  <div class="lead span10">${textContainer.text['grouperLoaderLogsTitle'] }</div>
                  <div class="span2" id="grouperLoaderMoreActionsButtonContentsDivId">
                    <%@ include file="grouperLoaderMoreActionsButtonContents.jsp"%>
                  </div>
                </div>
                
                <form class="form-inline form-small form-filter" id="logFilterFormId">
                
                  <div class="row-fluid">
                    <div class="span1">
                      <label for="people-filter">${textContainer.text['grouperLoaderLogsFilterFor'] }</label>
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
                  
                  <%-- only some jobs have subjobs --%>
                  <c:if test="${grouperRequestContainer.grouperLoaderContainer.hasSubjobs}">
                    
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
                  
                  </c:if>

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
                        value="${grouperRequestContainer.grouperLoaderContainer.numberOfRows}" />
                    </div>
                  </div>

                  <div class="row-fluid" style="margin-top: 1em">

                    <div class="span3"></div>
                    <div class="span6" style="white-space: nowrap"><input type="submit" class="btn" aria-controls="groupFilterResultsId" id="filterSubmitId" 
                      value="${textContainer.text['grouperLoaderButtonApplyFilter'] }" 
                      onclick="ajax('../app/UiV2GrouperLoader.viewLogsFilter?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}', {formIds: 'logFilterFormId'}); return false;"> 
                      &nbsp; 
                      <a class="btn" role="button" 
                        onclick="ajax('../app/UiV2GrouperLoader.viewLogs?groupId=${grouperRequestContainer.groupContainer.guiGroup.group.id}'); return false;"
                        >${textContainer.text['grouperLoaderButtonReset'] }</a>                                                                          
                    </div>
                  </div>
                </form>
                <br />
                <div id="grouperLoaderLogsResultsId"></div>
                
              </div>
            </div>
