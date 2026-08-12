<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                <p>${textContainer.text['mcpInfoToolLogsDescription']}</p>

                <c:choose>
                  <c:when test="${empty grouperRequestContainer.mcpContainer.guiMcpToolLogs}">
                    <p><span class="text-warning">${textContainer.text['mcpInfoToolLogsEmpty']}</span></p>
                  </c:when>
                  <c:otherwise>
                    <table class="table table-hover table-bordered table-striped table-condensed data-table">
                      <thead>
                        <tr>
                          <th>${textContainer.text['mcpInfoToolLogsTimeHeader']}</th>
                          <th>${textContainer.text['mcpInfoToolLogsToolNameHeader']}</th>
                          <th>${textContainer.text['mcpInfoToolLogsCategoryHeader']}</th>
                          <th>${textContainer.text['mcpInfoToolLogsErrorHeader']}</th>
                          <th>${textContainer.text['mcpInfoToolLogsDurationHeader']}</th>
                          <th>${textContainer.text['mcpInfoToolLogsRequestHeader']}</th>
                          <th>${textContainer.text['mcpInfoToolLogsResponseHeader']}</th>
                        </tr>
                      </thead>
                      <tbody>
                        <c:forEach var="guiMcpToolLog" items="${grouperRequestContainer.mcpContainer.guiMcpToolLogs}">
                          <tr>
                            <td style="white-space: nowrap">${grouper:escapeHtml(guiMcpToolLog.startedTimeFormatted)}</td>
                            <td>${grouper:escapeHtml(guiMcpToolLog.grouperMcpToolLog.toolName)}</td>
                            <td>${grouper:escapeHtml(guiMcpToolLog.grouperMcpToolLog.toolCategory)}</td>
                            <td>
                              <c:choose>
                                <c:when test="${guiMcpToolLog.grouperMcpToolLog.isError == 'T'}">
                                  <i class="fa fa-exclamation-circle" style="color: red;" title="Error"></i>
                                </c:when>
                                <c:otherwise>
                                  <i class="fa fa-check-circle" style="color: green;" title="OK"></i>
                                </c:otherwise>
                              </c:choose>
                            </td>
                            <td style="white-space: nowrap">${grouper:escapeHtml(guiMcpToolLog.durationMs)}</td>
                            <td style="white-space: nowrap"><span class="jobMessageContainer"><grouper:abbreviateTextarea text="${guiMcpToolLog.grouperMcpToolLog.request}" showCharCount="30" cols="20" rows="3"/></span><c:if test="${not empty guiMcpToolLog.grouperMcpToolLog.request}"> <button type="button" class="grouper-copy-btn" onclick="grouperCopyJobMessage(this);" aria-label="${textContainer.textEscapeXml['copyToClipboardTooltip']}" title="${textContainer.text['copyToClipboardTooltip']}" style="margin-left:4px;"><i class="fa fa-clone" aria-hidden="true"></i></button></c:if></td>
                            <td style="white-space: nowrap"><span class="jobMessageContainer"><grouper:abbreviateTextarea text="${guiMcpToolLog.grouperMcpToolLog.responseOrError}" showCharCount="30" cols="20" rows="3"/></span><c:if test="${not empty guiMcpToolLog.grouperMcpToolLog.responseOrError}"> <button type="button" class="grouper-copy-btn" onclick="grouperCopyJobMessage(this);" aria-label="${textContainer.textEscapeXml['copyToClipboardTooltip']}" title="${textContainer.text['copyToClipboardTooltip']}" style="margin-left:4px;"><i class="fa fa-clone" aria-hidden="true"></i></button></c:if></td>
                          </tr>
                        </c:forEach>
                      </tbody>
                    </table>
                  </c:otherwise>
                </c:choose>
