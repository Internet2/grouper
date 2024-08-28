<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                <p>${grouper:escapeHtml(textContainer.text['daemonJobsViewLogsDescription']) }</p>

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
                      <th><span rel="tooltip" data-html="true" data-delay-show="200" data-placement="right" 
                        data-original-title="${textContainer.textEscapeDouble['grouperLoaderLogsParentJobIdHeaderTooltip']}">${textContainer.text['grouperLoaderLogsParentJobIdHeader'] }</span></th>
                    </tr>
                  </thead>
                  <tbody>

                    <c:forEach var="guiHib3GrouperLoaderLog" items="${grouperRequestContainer.adminContainer.guiHib3GrouperLoaderLogs}" >
                      
                      <tr>
                      
                        <td style="color: ${grouper:capitalizeFully(guiHib3GrouperLoaderLog.statusTextColor)}; background-color: ${grouper:capitalizeFully(guiHib3GrouperLoaderLog.statusBackgroundColor)}; font-weight: bold;" 
                          >${grouper:capitalizeFully(guiHib3GrouperLoaderLog.hib3GrouperLoaderLog.status)}</td>
                      
                        <td>
                          <c:choose>
                            <c:when test="${guiHib3GrouperLoaderLog.loadedGuiGroup == null && guiHib3GrouperLoaderLog.isLoadedGroupJob() == false}">
                            
                              ${textContainer.text['grouperLoaderLogsLoadedGroupNotAGroup']}
                            </c:when>

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
                        <td>${guiHib3GrouperLoaderLog.totalElapsedFormatted}</td>
                        <td>${guiHib3GrouperLoaderLog.getDataElapsedFormatted}</td>
                        <td>${guiHib3GrouperLoaderLog.loadDataElapsedFormatted}</td>
                        <td>${guiHib3GrouperLoaderLog.hib3GrouperLoaderLog.totalCount}</td>
                        <td>${guiHib3GrouperLoaderLog.hib3GrouperLoaderLog.insertCount}</td>
                        <td>${guiHib3GrouperLoaderLog.hib3GrouperLoaderLog.updateCount}</td>
                        <td>${guiHib3GrouperLoaderLog.hib3GrouperLoaderLog.deleteCount}</td>
                        <td>${guiHib3GrouperLoaderLog.hib3GrouperLoaderLog.unresolvableSubjectCount}</td>
                        <td>${guiHib3GrouperLoaderLog.hib3GrouperLoaderLog.id}</td>
                        <td style="white-space: nowrap">${guiHib3GrouperLoaderLog.hib3GrouperLoaderLog.lastUpdated}</td>
                        <td style="white-space: nowrap">${guiHib3GrouperLoaderLog.hib3GrouperLoaderLog.host}</td>
                        <td style="white-space: nowrap"><grouper:abbreviateTextarea text="${guiHib3GrouperLoaderLog.hib3GrouperLoaderLog.jobMessage}" showCharCount="30" cols="20" rows="3"/></td>
                        <td style="white-space: nowrap">${guiHib3GrouperLoaderLog.hib3GrouperLoaderLog.parentJobId}</td>
                          
                      </tr>
                    </c:forEach>
                  </tbody>
                </table>
