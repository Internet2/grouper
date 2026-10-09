<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<%-- while a turn runs, the poll only replaces the elapsed time.  the status text is the only part
     screen readers announce, and it changes once, when the user stops the turn --%>
<p>
  <i class="fa fa-spinner fa-spin" aria-hidden="true"></i>
  <span id="aiAgentWorkingStatusId" role="status"><c:choose>
    <c:when test="${grouperRequestContainer.aiAgentContainer.stopping}">${textContainer.text['aiAgentStopping'] }</c:when>
    <c:otherwise>${textContainer.text['aiAgentWorking'] }</c:otherwise>
  </c:choose></span>
  <span id="aiAgentElapsedId">(${grouperRequestContainer.aiAgentContainer.elapsedSeconds}s)</span>
  <c:if test="${!grouperRequestContainer.aiAgentContainer.stopping}">
    <%-- a normal size button: btn-mini is under the 24x24 minimum target size (GRP-7314) --%>
    &nbsp;<button type="button" class="btn" id="aiAgentStopButtonId" aria-controls="aiAgentWorkingStatusId"
      onclick="ajax('../app/UiV2AiAgent.aiAgentStop?runId=${grouper:escapeHtml(grouperRequestContainer.aiAgentContainer.runId)}'); return false;"
      >${textContainer.text['aiAgentStop'] }</button>
  </c:if>
  <%-- filled in by the poll while the turn waits for the AI provider's rate limit to clear --%>
  <br /><span id="aiAgentRateLimitId"><c:if test="${grouperRequestContainer.aiAgentContainer.rateLimitWaitSeconds > 0}">${textContainer.text['aiAgentRateLimitWaiting'] }</c:if></span>
</p>
