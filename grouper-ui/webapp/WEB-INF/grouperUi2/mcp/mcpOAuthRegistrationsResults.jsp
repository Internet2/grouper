<%@ include file="../assetsJsp/commonTaglib.jsp"%>

                <p>${textContainer.text['mcpInfoOAuthRegistrationsDescription']}</p>

                <c:choose>
                  <c:when test="${empty grouperRequestContainer.mcpContainer.guiOAuthClients}">
                    <p><span class="text-warning">${textContainer.text['mcpInfoOAuthRegistrationsEmpty']}</span></p>
                  </c:when>
                  <c:otherwise>
                    <table class="table table-hover table-bordered table-striped table-condensed data-table">
                      <thead>
                        <tr>
                          <th>${textContainer.text['mcpInfoOAuthClientNameHeader']}</th>
                          <th>${textContainer.text['mcpInfoOAuthClientIdHeader']}</th>
                          <th>${textContainer.text['mcpInfoOAuthRedirectUrisHeader']}</th>
                          <th>${textContainer.text['mcpInfoOAuthRegisteredHeader']}</th>
                          <th>${textContainer.text['mcpInfoOAuthActionsHeader']}</th>
                        </tr>
                      </thead>
                      <tbody>
                        <c:forEach var="guiOAuthClient" items="${grouperRequestContainer.mcpContainer.guiOAuthClients}">
                          <tr>
                            <td>${grouper:escapeHtml(guiOAuthClient.grouperOAuthClient.clientName)}</td>
                            <td><code>${grouper:escapeHtml(guiOAuthClient.grouperOAuthClient.clientId)}</code></td>
                            <td>${grouper:escapeHtml(guiOAuthClient.grouperOAuthClient.redirectUrisDb)}</td>
                            <td style="white-space: nowrap">${grouper:escapeHtml(guiOAuthClient.registeredTimeFormatted)}</td>
                            <td>
                              <a href="#" class="btn btn-mini btn-danger" onclick="if (confirm('${textContainer.textEscapeSingleDouble['mcpInfoOAuthDeleteConfirm']}')) { ajax('../app/UiV2Mcp.mcpDeleteOAuthRegistration?oauthClientInternalId=${guiOAuthClient.grouperOAuthClient.internalId}'); } return false;">${textContainer.text['mcpInfoOAuthDeleteButton']}</a>
                            </td>
                          </tr>
                        </c:forEach>
                      </tbody>
                    </table>
                  </c:otherwise>
                </c:choose>
