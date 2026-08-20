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
      .readwrite-scope-section {
        margin-top: 12px;
        padding: 12px;
        background: #f0f4f8;
        border: 1px solid #c5d3e0;
        border-radius: 4px;
      }
      .readwrite-scope-section label {
        font-weight: bold;
        display: block;
        margin-top: 10px;
        margin-bottom: 2px;
      }
      .readwrite-scope-section label:first-child {
        margin-top: 0;
      }
      .readwrite-scope-section textarea {
        width: 100%;
        box-sizing: border-box;
        font-family: monospace;
        font-size: 0.9em;
        padding: 6px;
        border: 1px solid #ccc;
        border-radius: 3px;
        resize: vertical;
      }
      .readwrite-scope-section .field-description {
        font-size: 0.85em;
        color: #666;
        margin-top: 2px;
        margin-bottom: 8px;
      }
    </style>
  </head>
  <body class="full claro">
    <%@ include file="../assetsJsp/skipLink.jsp"%>
    <div class="top-container">
      <div class="navbar navbar-static-top">
        <div class="navbar-inner">
          <div class="container-fluid">
            <img class="brand" src="../../${mediaMap['image.organisation-logo']}" alt="Logo" />
          </div>
        </div>
      </div>
      <div class="container-fluid">
        <div id="messaging" role="alert"></div>
        <!-- main landmark (WCAG 1.3.1).  this page is not ajax driven so it has no
             content div of its own; it carries the same id as the other shells only so
             that one skip link target works across every page. -->
        <main class="oauth-consent-container" id="grouperMainContentDivId" tabindex="-1">

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
                            onclick="var cbs = document.querySelectorAll('input[name^=oauthScope]'); for (var i = 0; i < cbs.length; i++) { cbs[i].disabled = false; cbs[i].checked = this.checked; } oauthSyncImpliedScopes(); var rwCb = document.getElementById('oauthScopeReadwrite'); if (rwCb) { oauthToggleReadwriteScope(rwCb); }" />
                        <strong>${textContainer.text['oauthConsentScopeSelectAll']}</strong>
                      </label>
                    </div>
                  </c:if>

                  <c:if test="${grouperRequestContainer.oauthContainer.showReadonly}">
                    <div style="margin-bottom: 8px;">
                      <label>
                        <input type="checkbox" name="oauthScopeReadonly" value="true" checked="checked"
                            onclick="oauthUpdateSelectAll()" />
                        ${textContainer.text['oauthConsentScopeReadonly']}
                      </label>
                    </div>
                  </c:if>

                  <c:if test="${grouperRequestContainer.oauthContainer.showReadwrite}">
                    <div style="margin-bottom: 8px;">
                      <label>
                        <input type="checkbox" name="oauthScopeReadwrite" id="oauthScopeReadwrite" value="true"
                            onclick="oauthToggleReadwriteScope(this); oauthSyncImpliedScopes(); oauthUpdateSelectAll()" />
                        ${textContainer.text['oauthConsentScopeReadwrite']}
                      </label>

                      <div id="readwriteScopeSection" class="readwrite-scope-section" style="display: none;">
                        <p style="font-weight: bold; margin-top: 0; margin-bottom: 8px; font-size: 0.95em;">
                          <c:choose>
                            <c:when test="${grouperRequestContainer.oauthContainer.requireReadwriteDataScope}">
                              ${textContainer.text['oauthConsentReadwriteScopeHeaderRequired']}
                            </c:when>
                            <c:otherwise>
                              ${textContainer.text['oauthConsentReadwriteScopeHeaderOptional']}
                            </c:otherwise>
                          </c:choose>
                        </p>
                        <label for="oauthReadwriteFolders">${textContainer.text['oauthConsentReadwriteFoldersLabel']}</label>
                        <textarea id="oauthReadwriteFolders" name="oauthReadwriteFolders" rows="2"></textarea>
                        <div class="field-description">${textContainer.text['oauthConsentReadwriteFoldersDescription']}</div>

                        <label for="oauthReadwriteGroups">${textContainer.text['oauthConsentReadwriteGroupsLabel']}</label>
                        <textarea id="oauthReadwriteGroups" name="oauthReadwriteGroups" rows="2"></textarea>
                        <div class="field-description">${textContainer.text['oauthConsentReadwriteGroupsDescription']}</div>

                        <label for="oauthReadwriteSubjects">${textContainer.text['oauthConsentReadwriteSubjectsLabel']}</label>
                        <textarea id="oauthReadwriteSubjects" name="oauthReadwriteSubjects" rows="2"></textarea>
                        <div class="field-description">${textContainer.text['oauthConsentReadwriteSubjectsDescription']}</div>
                      </div>
                    </div>
                  </c:if>

                  <c:if test="${grouperRequestContainer.oauthContainer.showSqlReadonly}">
                    <div style="margin-bottom: 8px;">
                      <label>
                        <input type="checkbox" name="oauthScopeSqlReadonly" value="true"
                            onclick="oauthUpdateSelectAll()" />
                        ${textContainer.text['oauthConsentScopeSqlReadonly']}
                      </label>
                    </div>
                  </c:if>

                  <c:if test="${grouperRequestContainer.oauthContainer.showAdminReadonly}">
                    <div style="margin-bottom: 8px;">
                      <label>
                        <input type="checkbox" name="oauthScopeAdminReadonly" value="true"
                            onclick="oauthUpdateSelectAll()" />
                        ${textContainer.text['oauthConsentScopeAdminReadonly']}
                      </label>
                    </div>
                  </c:if>

                  <c:if test="${grouperRequestContainer.oauthContainer.showAdminReadwrite}">
                    <div style="margin-bottom: 8px;">
                      <label>
                        <input type="checkbox" name="oauthScopeAdminReadwrite" value="true"
                            onclick="oauthSyncImpliedScopes(); oauthUpdateSelectAll()" />
                        ${textContainer.text['oauthConsentScopeAdminReadwrite']}
                      </label>
                    </div>
                  </c:if>
                </div>

                <div class="consent-actions">
                  <button type="submit" name="oauthAction" value="deny"
                      class="btn btn-cancel">${textContainer.text['oauthConsentDenyButton']}</button>
                  <button type="button" id="oauthApproveButton"
                      class="btn btn-primary" onclick="oauthApproveWithValidation()">
                    ${textContainer.text['oauthConsentApproveButton']}
                  </button>
                </div>

                <%-- hidden submit button for actual form POST after validation --%>
                <input type="hidden" name="oauthAction" id="oauthActionHidden" value="" />
              </form>

              <script src="../../grouperExternal/public/assets/js/grouperUi.js"></script>
              <script src="../../grouperExternal/public/assets/js/jquery.blockUI.js"></script>
              <script type="text/javascript">
                function oauthToggleReadwriteScope(checkbox) {
                  var section = document.getElementById('readwriteScopeSection');
                  if (section) {
                    section.style.display = checkbox.checked ? 'block' : 'none';
                  }
                }

                function oauthSyncImpliedScopes() {
                  // readwrite implies readonly
                  var rwCb = document.getElementById('oauthScopeReadwrite');
                  var roCb = document.querySelector('input[name=oauthScopeReadonly]');
                  if (rwCb && roCb) {
                    if (rwCb.checked) {
                      roCb.checked = true;
                      roCb.disabled = true;
                    } else {
                      roCb.disabled = false;
                    }
                  }
                  // admin readwrite implies admin readonly
                  var arwCb = document.querySelector('input[name=oauthScopeAdminReadwrite]');
                  var aroCb = document.querySelector('input[name=oauthScopeAdminReadonly]');
                  if (arwCb && aroCb) {
                    if (arwCb.checked) {
                      aroCb.checked = true;
                      aroCb.disabled = true;
                    } else {
                      aroCb.disabled = false;
                    }
                  }
                }

                function oauthUpdateSelectAll() {
                  var sa = document.getElementById('oauthScopeSelectAll');
                  if (sa) {
                    var cbs = document.querySelectorAll('input[name^=oauthScope]');
                    var all = true;
                    for (var i = 0; i < cbs.length; i++) {
                      if (!cbs[i].checked) all = false;
                    }
                    sa.checked = all;
                  }
                }

                function oauthApproveWithValidation() {
                  var rwCb = document.getElementById('oauthScopeReadwrite');
                  if (!rwCb || !rwCb.checked) {
                    oauthDoSubmit();
                    return;
                  }
                  ajax('UiV2OAuth.ajaxValidateReadwriteScope', {requestParams:
                    'oauthReadwriteFolders=' + encodeURIComponent($('#oauthReadwriteFolders').val() || '')
                    + '&oauthReadwriteGroups=' + encodeURIComponent($('#oauthReadwriteGroups').val() || '')
                    + '&oauthReadwriteSubjects=' + encodeURIComponent($('#oauthReadwriteSubjects').val() || '')
                  });
                }

                function oauthDoSubmit() {
                  // re-enable any disabled checkboxes so their values are included in form submission
                  var disabledCbs = document.querySelectorAll('input[name^=oauthScope]:disabled');
                  for (var i = 0; i < disabledCbs.length; i++) {
                    disabledCbs[i].disabled = false;
                  }
                  document.getElementById('oauthActionHidden').value = 'approve';
                  document.getElementById('oauthActionHidden').closest('form').submit();
                }
              </script>
            </c:otherwise>
          </c:choose>

        </main>
      </div>
    </div>
  </body>
<!-- end grouperUi2/oauth/oauthAuthorize.jsp -->
</html>
