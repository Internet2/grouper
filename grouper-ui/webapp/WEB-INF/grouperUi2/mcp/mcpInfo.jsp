<%@ include file="../assetsJsp/commonTaglib.jsp"%>
  ${grouper:title('mcpInfoTitle')}
            <grouper:browserPage jspName="mcpInfo" />
            <div class="bread-header-container">
              <ul class="breadcrumb">
                <li><a href="?operation=UiV2Main.indexMain" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.indexMain');">${textContainer.text['myServicesHomeBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li><a href="?operation=UiV2Main.miscellaneous" onclick="return handleGuiV2LinkClick(event, 'operation=UiV2Main.miscellaneous');">${textContainer.text['miscellaneousBreadcrumb'] }</a><span class="divider"><i class='fa fa-angle-right'></i></span></li>
                <li class="active">${textContainer.text['mcpInfoBreadcrumb'] }</li>
              </ul>
              <div class="page-header blue-gradient">
                <h1>${textContainer.text['mcpInfoTitle'] }</h1>
                <p style="margin-top: -1em; margin-bottom: 1em">${textContainer.text['mcpInfoSubtitle']}</p>
              </div>
            </div>
            <div class="row-fluid" style="margin-top: -10px">
              <div class="span12">

                <p>${textContainer.text['mcpInfoDescription']}</p>

                <c:if test="${!grouperRequestContainer.mcpContainer.mcpEnabled}">
                  <div class="alert alert-warning">
                    <i class="fa fa-exclamation-triangle"></i> ${textContainer.text['mcpInfoNotEnabled']}
                  </div>
                </c:if>

                <table class="table table-condensed table-striped">
                  <tbody>
                    <c:if test="${not empty grouperRequestContainer.mcpContainer.mcpServerUrl}">
                      <tr>
                        <td style="white-space: nowrap; font-weight: bold; width: 200px;">${textContainer.text['mcpInfoServerUrlLabel']}</td>
                        <td>
                          <code id="mcpServerUrl" style="color: #8b0000;">${grouper:escapeHtml(grouperRequestContainer.mcpContainer.mcpServerUrl)}/mcp</code>
                          <button type="button" class="grouper-copy-btn" onclick="grouperCopyToClipboard('mcpServerUrl');" aria-label="${textContainer.textEscapeXml['copyToClipboardTooltip']}" title="${textContainer.text['copyToClipboardTooltip']}" style="margin-left: 6px;"><i class="fa fa-clone" aria-hidden="true"></i></button>
                        </td>
                      </tr>
                    </c:if>
                    <c:if test="${empty grouperRequestContainer.mcpContainer.mcpServerUrl}">
                      <tr>
                        <td style="white-space: nowrap; font-weight: bold; width: 200px;">${textContainer.text['mcpInfoServerUrlLabel']}</td>
                        <td><span class="text-warning">${textContainer.text['mcpInfoServerUrlNotConfigured']}</span></td>
                      </tr>
                    </c:if>

                  </tbody>
                </table>

                <p style="margin-top: 4px; color: #666;"><i class="fa fa-info-circle"></i> ${textContainer.text['mcpInfoSessionNotePre']} ${grouperRequestContainer.mcpContainer.sessionDurationHours} ${textContainer.text['mcpInfoSessionNotePost']}</p>

                <h4 style="margin-top: 20px; margin-bottom: 8px;">${textContainer.text['mcpInfoAccessHeader']}</h4>
                <c:choose>
                  <c:when test="${grouperRequestContainer.mcpContainer.allowedReadonly || grouperRequestContainer.mcpContainer.allowedReadwrite || grouperRequestContainer.mcpContainer.allowedSqlReadonly || grouperRequestContainer.mcpContainer.allowedAdminReadonly || grouperRequestContainer.mcpContainer.allowedAdminReadwrite}">
                    <ul style="list-style: none; padding-left: 20px;">
                      <c:if test="${grouperRequestContainer.mcpContainer.allowedReadonly}">
                        <li><i class="fa fa-check" style="color: green;"></i> ${textContainer.text['mcpInfoAccessReadonly']}</li>
                      </c:if>
                      <c:if test="${grouperRequestContainer.mcpContainer.allowedReadwrite}">
                        <li><i class="fa fa-check" style="color: green;"></i> ${textContainer.text['mcpInfoAccessReadwrite']}</li>
                      </c:if>
                      <c:if test="${grouperRequestContainer.mcpContainer.allowedSqlReadonly}">
                        <li><i class="fa fa-check" style="color: green;"></i> ${textContainer.text['mcpInfoAccessSqlReadonly']}</li>
                      </c:if>
                      <c:if test="${grouperRequestContainer.mcpContainer.allowedAdminReadonly}">
                        <li><i class="fa fa-check" style="color: green;"></i> ${textContainer.text['mcpInfoAccessAdminReadonly']}</li>
                      </c:if>
                      <c:if test="${grouperRequestContainer.mcpContainer.allowedAdminReadwrite}">
                        <li><i class="fa fa-check" style="color: green;"></i> ${textContainer.text['mcpInfoAccessAdminReadwrite']}</li>
                      </c:if>
                    </ul>
                  </c:when>
                  <c:otherwise>
                    <p><span class="text-warning"><i class="fa fa-exclamation-triangle"></i> ${textContainer.text['mcpInfoAccessDenied']}</span></p>
                  </c:otherwise>
                </c:choose>

                <p>${textContainer.text['mcpInfoWikiLink']}</p>

                <h4 style="margin-top: 20px; margin-bottom: 8px;">${textContainer.text['mcpInfoToolLogsHeader']}</h4>
                <p>
                  <a href="#" class="btn btn-small" onclick="ajax('../app/UiV2Mcp.mcpToolLogs'); return false;">
                    <i class="fa fa-refresh"></i> ${textContainer.text['mcpInfoToolLogsLoadButton']}
                  </a>
                </p>
                <div id="mcpToolLogsResultsId"></div>

                <c:if test="${grouperRequestContainer.mcpContainer.allowedConfidentialClientRegistration}">
                  <h4 style="margin-top: 20px; margin-bottom: 8px;">${textContainer.text['mcpInfoConfidentialClientHeader']}</h4>
                  <p>${textContainer.text['mcpInfoConfidentialClientDescription']}</p>
                  <form class="form-inline" style="margin-bottom: 10px;" onsubmit="ajax('../app/UiV2Mcp.mcpRegisterConfidentialClient', {formIds: 'mcpConfidentialClientFormId'}); return false;" id="mcpConfidentialClientFormId">
                    <label for="confidentialClientName" style="margin-right: 6px;">${textContainer.text['mcpInfoConfidentialClientNameLabel']}</label>
                    <input type="text" name="confidentialClientName" id="confidentialClientName" placeholder="${textContainer.text['mcpInfoConfidentialClientNamePlaceholder']}" style="margin-right: 10px;" />
                    <label for="confidentialClientRedirectUri" style="margin-right: 6px;">${textContainer.text['mcpInfoConfidentialClientRedirectUriLabel']}</label>
                    <input type="text" name="confidentialClientRedirectUri" id="confidentialClientRedirectUri" placeholder="${textContainer.text['mcpInfoConfidentialClientRedirectUriPlaceholder']}" style="margin-right: 10px; width: 350px;" />
                    <button type="submit" class="btn btn-primary btn-small">
                      <i class="fa fa-key"></i> ${textContainer.text['mcpInfoConfidentialClientRegisterButton']}
                    </button>
                  </form>
                  <div id="mcpConfidentialClientResultId"></div>
                </c:if>

                <h4 style="margin-top: 20px; margin-bottom: 8px;">${textContainer.text['mcpInfoOAuthRegistrationsHeader']}</h4>
                <p>
                  <a href="#" class="btn btn-small" onclick="ajax('../app/UiV2Mcp.mcpOAuthRegistrations'); return false;">
                    <i class="fa fa-refresh"></i> ${textContainer.text['mcpInfoOAuthRegistrationsLoadButton']}
                  </a>
                </p>
                <div id="mcpOAuthRegistrationsResultsId"></div>

              </div>
            </div>
