<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<%-- only the categories the user's groups allow are offered.  the saved choice can only narrow
     what the groups allow, never add to it --%>
<form id="aiAgentScopeFormId" onsubmit="ajax('../app/UiV2AiAgent.aiAgentScopeSubmit', {formIds: 'aiAgentScopeFormId'}); return false;">
  <fieldset>
    <legend style="font-size: 1.1em; margin-bottom: 0.25em;">${textContainer.text['aiAgentScopeHeader'] }</legend>
    <c:choose>
      <c:when test="${empty grouperRequestContainer.aiAgentContainer.scopeOptions}">
        <p>${textContainer.text['aiAgentScopeNone'] }</p>
      </c:when>
      <c:otherwise>
        <c:forEach items="${grouperRequestContainer.aiAgentContainer.scopeOptions}" var="scopeOption">
          <c:set var="aiAgentScopeTextKey" value="aiAgentScope_${scopeOption.category}" />
          <label class="checkbox">
            <input type="checkbox" name="aiAgentScope" id="aiAgentScope_${grouper:escapeHtml(scopeOption.category)}"
              value="${grouper:escapeHtml(scopeOption.category)}" onchange="aiAgentScopeSyncImplied();"
              <c:if test="${scopeOption.selected}">checked="checked"</c:if> />
            ${textContainer.text[aiAgentScopeTextKey] }
          </label>
        </c:forEach>
        <input type="submit" class="btn" aria-controls="aiAgentScopeDivId" value="${textContainer.text['aiAgentScopeSubmit'] }" />
        <script type="text/javascript">
          // read and write includes read-only, and admin read and write includes admin read-only, as
          // on the OAuth consent screen: while the larger one is checked, the one it includes is
          // checked and disabled.  a disabled box is not sent, so the server adds it when saving
          function aiAgentScopeSyncImplied() {
            var pairs = [['aiAgentScope_readwrite', 'aiAgentScope_readonly'],
              ['aiAgentScope_admin_readwrite', 'aiAgentScope_admin_readonly']];
            for (var i = 0; i < pairs.length; i++) {
              var writeBox = document.getElementById(pairs[i][0]);
              var readBox = document.getElementById(pairs[i][1]);
              if (writeBox && readBox) {
                if (writeBox.checked) {
                  readBox.checked = true;
                  readBox.disabled = true;
                } else {
                  readBox.disabled = false;
                }
              }
            }
          }
          aiAgentScopeSyncImplied();
        </script>
      </c:otherwise>
    </c:choose>
  </fieldset>
</form>
