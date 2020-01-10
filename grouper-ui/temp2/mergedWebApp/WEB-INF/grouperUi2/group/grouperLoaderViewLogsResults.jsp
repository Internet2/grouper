<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                <p>${textContainer.text['grouperLoaderLogsDescription'] }</p>
                
                <%--
                  <a href="../app/UiV2GrouperLoader.grouperLoaderLogExportSubmit/groupId%3d${grouperRequestContainer.groupContainer.guiGroup.group.id}/${grouperRequestContainer.groupImportContainer.exportAll ? 'all' : 'ids'}/${grouperRequestContainer.groupImportContainer.exportFileName}" 
                    >${textContainer.text['grouperLoaderButtonExport'] }</a>
                --%>            
                <table class="table table-hover table-bordered table-striped table-condensed data-table table-bulk-update footable">
                  <thead>
                    <tr>
                    
                      <th><span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                        data-original-title="${textContainer.textEscapeDouble['grouperLoaderLogsStatusHeaderTooltip']}">${textContainer.text['grouperLoaderLogsStatusHeader'] }</span></th>
                      <th><span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                        data-original-title="${textContainer.textEscapeDouble['grouperLoaderLogsLoadedGroupHeaderTooltip']}">${textContainer.text['grouperLoaderLogsLoadedGroupHeader'] }</span></th>
                      <th><span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                        data-original-title="${textContainer.textEscapeDouble['grouperLoaderLogsJobTypeHeaderTooltip']}">${textContainer.text['grouperLoaderLogsJobTypeHeader'] }</span></th>
                      <th><span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                        data-original-title="${textContainer.textEscapeDouble['grouperLoaderLogsStartedHeaderTooltip']}">${textContainer.text['grouperLoaderLogsStartedHeader'] }</span></th>
                      <th><span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                        data-original-title="${textContainer.textEscapeDouble['grouperLoaderLogsEndedHeaderTooltip']}">${textContainer.text['grouperLoaderLogsEndedHeader'] }</span></th>
                      <th><span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                        data-original-title="${textContainer.textEscapeDouble['grouperLoaderLogsMillisHeaderTooltip']}">${textContainer.text['grouperLoaderLogsMillisHeader'] }</span></th>
                      <th><span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                        data-original-title="${textContainer.textEscapeDouble['grouperLoaderLogsMillisGetDataHeaderTooltip']}">${textContainer.text['grouperLoaderLogsMillisGetDataHeader'] }</span></th>
                      <th><span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                        data-original-title="${textContainer.textEscapeDouble['grouperLoaderLogsMillisLoadDataHeaderTooltip']}">${textContainer.text['grouperLoaderLogsMillisLoadDataHeader'] }</span></th>
                      <th><span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                        data-original-title="${textContainer.textEscapeDouble['grouperLoaderLogsTotalCountHeaderTooltip']}">${textContainer.text['grouperLoaderLogsTotalCountHeader'] }</span></th>
                      <th><span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                        data-original-title="${textContainer.textEscapeDouble['grouperLoaderLogsInsertCountHeaderTooltip']}">${textContainer.text['grouperLoaderLogsInsertCountHeader'] }</span></th>
                      <th><span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                        data-original-title="${textContainer.textEscapeDouble['grouperLoaderLogsUpdateCountHeaderTooltip']}">${textContainer.text['grouperLoaderLogsUpdateCountHeader'] }</span></th>
                      <th><span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                        data-original-title="${textContainer.textEscapeDouble['grouperLoaderLogsDeleteCountHeaderTooltip']}">${textContainer.text['grouperLoaderLogsDeleteCountHeader'] }</span></th>
                      <th><span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                        data-original-title="${textContainer.textEscapeDouble['grouperLoaderLogsUnresolvableCountHeaderTooltip']}">${textContainer.text['grouperLoaderLogsUnresolvableCountHeader'] }</span></th>
                      <th><span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                        data-original-title="${textContainer.textEscapeDouble['grouperLoaderLogsIdHeaderTooltip']}">${textContainer.text['grouperLoaderLogsIdHeader'] }</span></th>
                      <th><span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                        data-original-title="${textContainer.textEscapeDouble['grouperLoaderLogsLoadedLastUpdatedHeaderTooltip']}">${textContainer.text['grouperLoaderLogsLoadedLastUpdatedHeader'] }</span></th>
                      <th><span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                        data-original-title="${textContainer.textEscapeDouble['grouperLoaderLogsLoadedHostHeaderTooltip']}">${textContainer.text['grouperLoaderLogsLoadedHostHeader'] }</span></th>
                      <th><span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                        data-original-title="${textContainer.textEscapeDouble['grouperLoaderLogsLoadedJobMessageHeaderTooltip']}">${textContainer.text['grouperLoaderLogsLoadedJobMessageHeader'] }</span></th>
                      <%-- only some jobs have subjobs --%>
                      <c:if test="${grouperRequestContainer.grouperLoaderContainer.hasSubjobs}">
                        <th><span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                          data-original-title="${textContainer.textEscapeDouble['grouperLoaderLogsParentJobIdHeaderTooltip']}">${textContainer.text['grouperLoaderLogsParentJobIdHeader'] }</span></th>
                      </c:if>
                    </tr>
                  </thead>
                  <tbody>

                    <c:forEach var="guiHib3GrouperLoaderLog" items="${grouperRequestContainer.grouperLoaderContainer.guiHib3GrouperLoaderLogs}" >
                      
                      <tr>
                      
                        <td style="color: ${grouper:capitalizeFully(guiHib3GrouperLoaderLog.statusTextColor)}; background-color: ${grouper:capitalizeFully(guiHib3GrouperLoaderLog.statusBackgroundColor)}; font-weight: bold;" 
                          >${grouper:capitalizeFully(guiHib3GrouperLoaderLog.hib3GrouperLoaderLog.status)}</td>
                      
                        <td>
                          <c:choose>
                            <c:when test="${guiHib3GrouperLoaderLog.loadedGuiGroup == null && guiHib3GrouperLoaderLog.hib3GrouperLoaderLog.parentJobId == null}">
                            
                              ${textContainer.text['grouperLoaderLogsLoadedGroupMultiple']}
                            </c:when>
                            
                            <c:when test="${guiHib3GrouperLoaderLog.loadedGuiGroup == null && guiHib3GrouperLoaderLog.hib3GrouperLoaderLog.parentJobId != null}">
                            
                              ${textContainer.text['grouperLoaderLogsLoadedGroupNotFound']}
                            </c:when>

                            <c:otherwise>
                              ${guiHib3GrouperLoaderLog.loadedGuiGroup.shortLinkWithIcon}
                            </c:otherwise>
                          
                          </c:choose>
                        
                        </td>
                        
                        <td>
                          <c:choose>

                            <c:when test="${guiHib3GrouperLoaderLog.hib3GrouperLoaderLog.parentJobId != null}">
                              ${textContainer.text['grouperLoaderLogsJobTypeSubjob']}
                            </c:when>
                          
                            <c:when test="${guiHib3GrouperLoaderLog.loadedGuiGroup == null}">
                              ${textContainer.text['grouperLoaderLogsJobTypeOverall']}
                            </c:when>
                            
                            <c:otherwise>
                             ${textContainer.text['grouperLoaderLogsJobTypeSimple']}
                            </c:otherwise>
                          
                          </c:choose>
                        
                        </td>
                        
                        <td style="white-space: nowrap">${guiHib3GrouperLoaderLog.hib3GrouperLoaderLog.startedTime}</td>
                        <td style="white-space: nowrap">${guiHib3GrouperLoaderLog.hib3GrouperLoaderLog.endedTime}</td>
                        <td>${guiHib3GrouperLoaderLog.hib3GrouperLoaderLog.millis}</td>
                        <td>${guiHib3GrouperLoaderLog.hib3GrouperLoaderLog.millisGetData}</td>
                        <td>${guiHib3GrouperLoaderLog.hib3GrouperLoaderLog.millisLoadData}</td>
                        <td>${guiHib3GrouperLoaderLog.hib3GrouperLoaderLog.totalCount}</td>
                        <td>${guiHib3GrouperLoaderLog.hib3GrouperLoaderLog.insertCount}</td>
                        <td>${guiHib3GrouperLoaderLog.hib3GrouperLoaderLog.updateCount}</td>
                        <td>${guiHib3GrouperLoaderLog.hib3GrouperLoaderLog.deleteCount}</td>
                        <td>${guiHib3GrouperLoaderLog.hib3GrouperLoaderLog.unresolvableSubjectCount}</td>
                        <td>${guiHib3GrouperLoaderLog.hib3GrouperLoaderLog.id}</td>
                        <td style="white-space: nowrap">${guiHib3GrouperLoaderLog.hib3GrouperLoaderLog.lastUpdated}</td>
                        <td style="white-space: nowrap">${guiHib3GrouperLoaderLog.hib3GrouperLoaderLog.host}</td>
                        <td style="white-space: nowrap">
                          <c:choose>
                            <c:when test="${fn:length(guiHib3GrouperLoaderLog.hib3GrouperLoaderLog.jobMessage) > 0}">
                              <span id="jobMessageSpan__${guiHib3GrouperLoaderLog.hib3GrouperLoaderLog.id}"
                                >${grouper:abbreviate(guiHib3GrouperLoaderLog.hib3GrouperLoaderLog.jobMessage, 30, false, true)}
                                <a href="#" onclick="$('#jobMessageTextarea__${guiHib3GrouperLoaderLog.hib3GrouperLoaderLog.id}').show('slow'); 
                                $('#jobMessageSpan__${guiHib3GrouperLoaderLog.hib3GrouperLoaderLog.id}').hide('slow');
                                return false">${textContainer.text['grouperLoaderLogsLoadedJobMessageShow']}</a>
                              </span>
                            
                              <textarea cols="20" rows="3" style="display: none" 
                               id="jobMessageTextarea__${guiHib3GrouperLoaderLog.hib3GrouperLoaderLog.id}"
                               >${grouper:escapeHtml(guiHib3GrouperLoaderLog.hib3GrouperLoaderLog.jobMessage)}</textarea>
                            </c:when>
                          </c:choose>
                        </td>
                        <%-- only some jobs have subjobs --%>
                        <c:if test="${grouperRequestContainer.grouperLoaderContainer.hasSubjobs}">
                          <td style="white-space: nowrap">${guiHib3GrouperLoaderLog.hib3GrouperLoaderLog.parentJobId}</td>
                        </c:if>
                          
                      </tr>
                    </c:forEach>
                  </tbody>
                </table>
