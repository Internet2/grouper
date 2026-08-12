<%@ include file="../assetsJsp/commonTaglib.jsp"%>
                <div class="alert alert-warning" style="margin-top: 10px;">
                  <i class="fa fa-exclamation-triangle"></i> <strong>${textContainer.text['mcpInfoConfidentialClientSecretWarning']}</strong>
                </div>
                <table class="table table-condensed table-striped" style="margin-top: 10px;">
                  <tbody>
                    <tr>
                      <td style="white-space: nowrap; font-weight: bold; width: 200px;">${textContainer.text['mcpInfoConfidentialClientIdLabel']}</td>
                      <td>
                        <code id="confClientId">${grouper:escapeHtml(grouperRequestContainer.mcpContainer.registeredClientId)}</code>
                        <button type="button" class="grouper-copy-btn" onclick="grouperCopyToClipboard('confClientId');" aria-label="${textContainer.textEscapeXml['copyToClipboardTooltip']}" title="${textContainer.text['copyToClipboardTooltip']}" style="margin-left: 6px;"><i class="fa fa-clone" aria-hidden="true"></i></button>
                      </td>
                    </tr>
                    <tr>
                      <td style="white-space: nowrap; font-weight: bold; width: 200px;">${textContainer.text['mcpInfoConfidentialClientSecretLabel']}</td>
                      <td>
                        <code id="confClientSecret">${grouper:escapeHtml(grouperRequestContainer.mcpContainer.registeredClientSecret)}</code>
                        <button type="button" class="grouper-copy-btn" onclick="grouperCopyToClipboard('confClientSecret');" aria-label="${textContainer.textEscapeXml['copyToClipboardTooltip']}" title="${textContainer.text['copyToClipboardTooltip']}" style="margin-left: 6px;"><i class="fa fa-clone" aria-hidden="true"></i></button>
                      </td>
                    </tr>
                    <tr>
                      <td style="white-space: nowrap; font-weight: bold; width: 200px;">${textContainer.text['mcpInfoConfidentialAuthorizationUrlLabel']}</td>
                      <td>
                        <code id="confAuthUrl">${grouper:escapeHtml(grouperRequestContainer.mcpContainer.registeredAuthorizationUrl)}</code>
                        <button type="button" class="grouper-copy-btn" onclick="grouperCopyToClipboard('confAuthUrl');" aria-label="${textContainer.textEscapeXml['copyToClipboardTooltip']}" title="${textContainer.text['copyToClipboardTooltip']}" style="margin-left: 6px;"><i class="fa fa-clone" aria-hidden="true"></i></button>
                      </td>
                    </tr>
                    <tr>
                      <td style="white-space: nowrap; font-weight: bold; width: 200px;">${textContainer.text['mcpInfoConfidentialTokenUrlLabel']}</td>
                      <td>
                        <code id="confTokenUrl">${grouper:escapeHtml(grouperRequestContainer.mcpContainer.registeredTokenUrl)}</code>
                        <button type="button" class="grouper-copy-btn" onclick="grouperCopyToClipboard('confTokenUrl');" aria-label="${textContainer.textEscapeXml['copyToClipboardTooltip']}" title="${textContainer.text['copyToClipboardTooltip']}" style="margin-left: 6px;"><i class="fa fa-clone" aria-hidden="true"></i></button>
                      </td>
                    </tr>
                    <tr>
                      <td style="white-space: nowrap; font-weight: bold; width: 200px;">${textContainer.text['mcpInfoConfidentialScopeLabel']}</td>
                      <td>
                        <code id="confScope">openid</code>
                        <button type="button" class="grouper-copy-btn" onclick="grouperCopyToClipboard('confScope');" aria-label="${textContainer.textEscapeXml['copyToClipboardTooltip']}" title="${textContainer.text['copyToClipboardTooltip']}" style="margin-left: 6px;"><i class="fa fa-clone" aria-hidden="true"></i></button>
                      </td>
                    </tr>
                  </tbody>
                </table>
