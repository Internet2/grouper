<%@ include file="../assetsJsp/commonTaglib.jsp"%>

            <%-- for the new group or new stem button --%>
            <input type="hidden" name="objectStemId" value="${grouperRequestContainer.groupContainer.guiGroup.group.parentUuid}" />

            <%-- show the add member button for privileges --%>
            <c:set target="${grouperRequestContainer.groupContainer}" property="showAddMember" value="false" />
            <%@ include file="groupHeader.jsp" %>

            <div class="row-fluid">
              <div class="span12">
                <c:set var="grouperCurrentTab" value="none" />
                <%@ include file="../group/groupTabs.jsp" %>
                <div class="row-fluid">
                  <div class="lead span10">${textContainer.text['grouperLoaderLogsTitle'] }</div>
                  <div class="span2" id="grouperLoaderMoreActionsButtonContentsDivId">
                    <%@ include file="grouperLoaderMoreActionsButtonContents.jsp"%>
                  </div>
                </div>
                
                <form class="form-inline form-filter" id="logFilterFormId">
                
                  <div class="row-fluid">
                    <div class="span1">
                      <label for="people-filter">${textContainer.text['grouperLoaderLogsFilterFor'] }</label>
                    </div>
                  </div>

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
                      <span class="control-label" style="white-space: nowrap; font-weight: bold;">${textContainer.text['grouperLoaderLogsFilterZeroCount'] }</span>
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
