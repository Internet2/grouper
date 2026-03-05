<%@ include file="../assetsJsp/commonTaglib.jsp"%>
<%@ page import="edu.internet2.middleware.grouper.ui.GrouperUiFilter" %>
<c:set var="lang" value="${(empty GrouperUiFilter.retrieveLocale()) ? 'en' : GrouperUiFilter.retrieveLocale().getLanguage()}" />

<!DOCTYPE html>
<html lang="${lang}">
<!-- start grouperUi2/oauth/oauthAuthorize.jsp -->
  <head>
    <title>${textContainer.text['oauthConsentTitle']}</title>
    <%@ include file="../assetsJsp/commonHead.jsp"%>
    <style>
      .oauth-consent-container {
        max-width: 500px;
        margin: 60px auto;
        padding: 30px;
        background: #fff;
        border: 1px solid #ddd;
        border-radius: 4px;
        box-shadow: 0 2px 4px rgba(0,0,0,0.1);
      }
      .oauth-consent-container h2 {
        margin-top: 0;
        margin-bottom: 20px;
        color: #333;
      }
      .oauth-consent-container .consent-info {
        margin-bottom: 20px;
        padding: 15px;
        background: #f5f5f5;
        border-radius: 4px;
      }
      .oauth-consent-container .consent-info dt {
        font-weight: bold;
        color: #555;
      }
      .oauth-consent-container .consent-info dd {
        margin-bottom: 10px;
        margin-left: 0;
      }
      .oauth-consent-container .consent-actions {
        margin-top: 20px;
        text-align: right;
      }
      .oauth-consent-container .consent-actions .btn {
        margin-left: 10px;
      }
      .oauth-consent-container .error-message {
        color: #d9534f;
        padding: 15px;
        background: #fdf7f7;
        border: 1px solid #ebccd1;
        border-radius: 4px;
      }
    </style>
  </head>
  <body class="full claro">
    <div class="top-container">
      <div class="navbar navbar-static-top">
        <div class="navbar-inner">
          <div class="container-fluid">
            <img class="brand" src="../../${mediaMap['image.organisation-logo']}" alt="Logo" />
          </div>
        </div>
      </div>
      <div class="container-fluid">
        <div class="oauth-consent-container">

          <c:choose>
            <c:when test="${not empty grouperRequestContainer.oauthContainer.errorMessage}">
              <h2>${textContainer.text['oauthConsentErrorTitle']}</h2>
              <div class="error-message">
                ${grouper:escapeHtml(grouperRequestContainer.oauthContainer.errorMessage)}
              </div>
            </c:when>
            <c:otherwise>
              <h2>${textContainer.text['oauthConsentTitle']}</h2>
              <p>${textContainer.text['oauthConsentDescription']}</p>

              <div class="consent-info">
                <dl>
                  <dt>${textContainer.text['oauthConsentApplicationLabel']}</dt>
                  <dd>${grouper:escapeHtml(grouperRequestContainer.oauthContainer.clientName)}</dd>

                  <dt>${textContainer.text['oauthConsentLoggedInAsLabel']}</dt>
                  <dd>${grouper:escapeHtml(grouperRequestContainer.oauthContainer.loggedInUserName)}</dd>

                  <c:if test="${not empty grouperRequestContainer.oauthContainer.scope}">
                    <dt>${textContainer.text['oauthConsentScopeLabel']}</dt>
                    <dd>${grouper:escapeHtml(grouperRequestContainer.oauthContainer.scope)}</dd>
                  </c:if>
                </dl>
              </div>

              <p>${textContainer.text['oauthConsentApproveMessage']}</p>

              <form method="POST" action="UiV2OAuth.submitAuthorize">
                <input type="hidden" name="<csrf:token-name/>" value="<csrf:token-value />"/>
                <input type="hidden" name="oauthRequestId"
                    value="${grouper:escapeHtml(grouperRequestContainer.oauthContainer.requestId)}" />

                <div style="margin: 20px 0; padding: 15px; background: #f9f9f9; border: 1px solid #ddd; border-radius: 4px;">
                  <p style="font-weight: bold; margin-top: 0;">${textContainer.text['oauthConsentScopeHeader']}</p>

                  <%-- show "Select all" only when multiple scope checkboxes are visible --%>
                  <c:set var="oauthScopeCount" value="0" />
                  <c:if test="${grouperRequestContainer.oauthContainer.showReadonly}"><c:set var="oauthScopeCount" value="${oauthScopeCount + 1}" /></c:if>
                  <c:if test="${grouperRequestContainer.oauthContainer.showReadwrite}"><c:set var="oauthScopeCount" value="${oauthScopeCount + 1}" /></c:if>
                  <c:if test="${grouperRequestContainer.oauthContainer.showSqlReadonly}"><c:set var="oauthScopeCount" value="${oauthScopeCount + 1}" /></c:if>
                  <c:if test="${grouperRequestContainer.oauthContainer.showAdminReadonly}"><c:set var="oauthScopeCount" value="${oauthScopeCount + 1}" /></c:if>
                  <c:if test="${grouperRequestContainer.oauthContainer.showAdminReadwrite}"><c:set var="oauthScopeCount" value="${oauthScopeCount + 1}" /></c:if>

                  <c:if test="${oauthScopeCount > 1}">
                    <div style="margin-bottom: 12px; padding-bottom: 8px; border-bottom: 1px solid #ddd;">
                      <label>
                        <input type="checkbox" id="oauthScopeSelectAll"
                            onclick="var cbs = document.querySelectorAll('input[name^=oauthScope]'); for (var i = 0; i < cbs.length; i++) { cbs[i].checked = this.checked; }" />
                        <strong>${textContainer.text['oauthConsentScopeSelectAll']}</strong>
                      </label>
                    </div>
                  </c:if>

                  <c:if test="${grouperRequestContainer.oauthContainer.showReadonly}">
                    <div style="margin-bottom: 8px;">
                      <label>
                        <input type="checkbox" name="oauthScopeReadonly" value="true" checked="checked"
                            onclick="var sa = document.getElementById('oauthScopeSelectAll'); if (sa) { var cbs = document.querySelectorAll('input[name^=oauthScope]'); var all = true; for (var i = 0; i < cbs.length; i++) { if (!cbs[i].checked) all = false; } sa.checked = all; }" />
                        ${textContainer.text['oauthConsentScopeReadonly']}
                      </label>
                    </div>
                  </c:if>

                  <c:if test="${grouperRequestContainer.oauthContainer.showReadwrite}">
                    <div style="margin-bottom: 8px;">
                      <label>
                        <input type="checkbox" name="oauthScopeReadwrite" value="true"
                            onclick="var sa = document.getElementById('oauthScopeSelectAll'); if (sa) { var cbs = document.querySelectorAll('input[name^=oauthScope]'); var all = true; for (var i = 0; i < cbs.length; i++) { if (!cbs[i].checked) all = false; } sa.checked = all; }" />
                        ${textContainer.text['oauthConsentScopeReadwrite']}
                      </label>
                    </div>
                  </c:if>

                  <c:if test="${grouperRequestContainer.oauthContainer.showSqlReadonly}">
                    <div style="margin-bottom: 8px;">
                      <label>
                        <input type="checkbox" name="oauthScopeSqlReadonly" value="true"
                            onclick="var sa = document.getElementById('oauthScopeSelectAll'); if (sa) { var cbs = document.querySelectorAll('input[name^=oauthScope]'); var all = true; for (var i = 0; i < cbs.length; i++) { if (!cbs[i].checked) all = false; } sa.checked = all; }" />
                        ${textContainer.text['oauthConsentScopeSqlReadonly']}
                      </label>
                    </div>
                  </c:if>

                  <c:if test="${grouperRequestContainer.oauthContainer.showAdminReadonly}">
                    <div style="margin-bottom: 8px;">
                      <label>
                        <input type="checkbox" name="oauthScopeAdminReadonly" value="true"
                            onclick="var sa = document.getElementById('oauthScopeSelectAll'); if (sa) { var cbs = document.querySelectorAll('input[name^=oauthScope]'); var all = true; for (var i = 0; i < cbs.length; i++) { if (!cbs[i].checked) all = false; } sa.checked = all; }" />
                        ${textContainer.text['oauthConsentScopeAdminReadonly']}
                      </label>
                    </div>
                  </c:if>

                  <c:if test="${grouperRequestContainer.oauthContainer.showAdminReadwrite}">
                    <div style="margin-bottom: 8px;">
                      <label>
                        <input type="checkbox" name="oauthScopeAdminReadwrite" value="true"
                            onclick="var sa = document.getElementById('oauthScopeSelectAll'); if (sa) { var cbs = document.querySelectorAll('input[name^=oauthScope]'); var all = true; for (var i = 0; i < cbs.length; i++) { if (!cbs[i].checked) all = false; } sa.checked = all; }" />
                        ${textContainer.text['oauthConsentScopeAdminReadwrite']}
                      </label>
                    </div>
                  </c:if>
                </div>

                <div class="consent-actions">
                  <button type="submit" name="oauthAction" value="deny"
                      class="btn btn-cancel">${textContainer.text['oauthConsentDenyButton']}</button>
                  <button type="submit" name="oauthAction" value="approve"
                      class="btn btn-primary">${textContainer.text['oauthConsentApproveButton']}</button>
                </div>
              </form>
            </c:otherwise>
          </c:choose>

        </div>
      </div>
    </div>
  </body>
<!-- end grouperUi2/oauth/oauthAuthorize.jsp -->
</html>
