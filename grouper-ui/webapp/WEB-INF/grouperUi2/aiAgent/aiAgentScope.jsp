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
            <input type="checkbox" name="aiAgentScope" value="${grouper:escapeHtml(scopeOption.category)}"
              <c:if test="${scopeOption.selected}">checked="checked"</c:if> />
            ${textContainer.text[aiAgentScopeTextKey] }
          </label>
        </c:forEach>
        <input type="submit" class="btn" aria-controls="aiAgentScopeDivId" value="${textContainer.text['aiAgentScopeSubmit'] }" />
      </c:otherwise>
    </c:choose>
  </fieldset>
</form>
