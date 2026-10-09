<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<%-- everything shown here came from the user, the model or Grouper data, so all of it is
     escaped, and shown as plain text: the model's answers are not rendered as markup.
     secondary text is #767676, the lightest grey with enough contrast on white (GRP-7210), not
     bootstrap's muted, which has too little --%>
<c:set var="aiAgentContainer" value="${grouperRequestContainer.aiAgentContainer}" />

<c:if test="${aiAgentContainer.summarized}">
  <p style="color: #767676;">${textContainer.text['aiAgentSummarized'] }</p>
</c:if>

<c:if test="${empty aiAgentContainer.guiMessages}">
  <p style="color: #767676;">${textContainer.text['aiAgentEmpty'] }</p>
</c:if>

<c:forEach items="${aiAgentContainer.guiMessages}" var="guiMessage">
  <c:choose>
    <c:when test="${guiMessage.type == 'user'}">
      <div class="well well-small" style="margin-bottom: 0.5em;">
        <strong>${textContainer.text['aiAgentYou'] }</strong>
        <div style="white-space: pre-wrap; word-wrap: break-word;">${grouper:escapeHtml(guiMessage.text)}</div>
      </div>
    </c:when>
    <c:when test="${guiMessage.type == 'assistant'}">
      <div style="margin: 0.5em 0 1em 0;">
        <strong>${textContainer.text['aiAgentAssistant'] }</strong>
        <div style="white-space: pre-wrap; word-wrap: break-word;">${grouper:escapeHtml(guiMessage.text)}</div>
      </div>
    </c:when>
    <c:otherwise>
      <details style="margin: 0.25em 0 0.5em 1em;">
        <summary style="color: #767676;">
          ${textContainer.text['aiAgentToolCall'] } ${grouper:escapeHtml(guiMessage.toolName)}
          <c:choose>
            <c:when test="${guiMessage.awaitingApproval}"> - ${textContainer.text['aiAgentToolAwaitingApproval'] }</c:when>
            <c:when test="${guiMessage.error}"> - ${textContainer.text['aiAgentToolError'] }</c:when>
          </c:choose>
        </summary>
        <div><strong>${textContainer.text['aiAgentToolArguments'] }</strong></div>
        <pre style="white-space: pre-wrap; word-wrap: break-word;">${grouper:escapeHtml(guiMessage.argumentsJson)}</pre>
        <c:if test="${guiMessage.resultText != null}">
          <div><strong>${textContainer.text['aiAgentToolResult'] }</strong></div>
          <pre style="white-space: pre-wrap; word-wrap: break-word; max-height: 20em; overflow: auto;">${grouper:escapeHtml(guiMessage.resultText)}</pre>
        </c:if>
      </details>
    </c:otherwise>
  </c:choose>
</c:forEach>

<c:if test="${aiAgentContainer.running}">
  <div id="aiAgentWorkingDivId">
    <%@ include file="aiAgentWorking.jsp"%>
  </div>
</c:if>

<%-- the turn's status, here next to the approval box rather than at the top of the page, which
     would scroll the page away from the conversation.  the text is from the text file --%>
<div id="aiAgentTurnStatusId"><c:if test="${!empty aiAgentContainer.turnStatusText}">
  <div class="alert ${aiAgentContainer.turnStatusError ? 'alert-error' : 'alert-info'}">${aiAgentContainer.turnStatusText}</div>
</c:if></div>

<%-- the approval: the summary is what is being approved.  only the ids go back, and only ids of
     calls actually waiting are accepted --%>
<c:if test="${!aiAgentContainer.running && !empty aiAgentContainer.pendingConfirmations}">
  <form id="aiAgentApproveFormId" onsubmit="return false;">
    <div class="alert alert-block">
      <h4 id="aiAgentApprovalsHeaderId" tabindex="-1">${textContainer.text['aiAgentApprovalsHeader'] }</h4>
      <p>${textContainer.text['aiAgentApprovalsDescription'] }</p>
      <c:choose>
        <%-- one change: nothing to pick between, so no checkbox.  bootstrap takes the bottom margin
             off a paragraph in an alert, so it is put back to keep the buttons off the text --%>
        <c:when test="${aiAgentContainer.pendingConfirmationCount == 1}">
          <c:forEach items="${aiAgentContainer.pendingConfirmations}" var="pendingConfirmation">
            <p style="word-wrap: break-word; margin-bottom: 10px;">${grouper:escapeHtml(pendingConfirmation.summary)}</p>
          </c:forEach>
        </c:when>
        <c:otherwise>
          <%-- every change starts checked; unchecking one leaves it out.  the button says how many
               changes it makes, kept up to date as boxes change, so approving is still explicit --%>
          <c:forEach items="${aiAgentContainer.pendingConfirmations}" var="pendingConfirmation">
            <label class="checkbox" style="word-wrap: break-word;">
              <input type="checkbox" name="aiAgentApprove" value="${grouper:escapeHtml(pendingConfirmation.toolCallId)}"
                checked="checked" onchange="aiAgentApproveCountChanged();" />
              ${grouper:escapeHtml(pendingConfirmation.summary)}
            </label>
          </c:forEach>
        </c:otherwise>
      </c:choose>
      <%-- exactly which changes this form showed, as one field, so a form left open in another tab
           cannot decide on changes it never showed --%>
      <input type="hidden" name="aiAgentShownFingerprint"
        value="${grouper:escapeHtml(aiAgentContainer.pendingConfirmationsFingerprint)}" />
      <%-- all, checked or decline, set by the button --%>
      <input type="hidden" name="aiAgentDecision" id="aiAgentDecisionId" value="checked" />
      <c:choose>
        <c:when test="${aiAgentContainer.pendingConfirmationCount == 1}">
          <button type="button" class="btn btn-primary" aria-controls="aiAgentConversationDivId"
            onclick="$('#aiAgentDecisionId').val('all'); ajax('../app/UiV2AiAgent.aiAgentResolve', {formIds: 'aiAgentApproveFormId'}); return false;"
            >${textContainer.text['aiAgentApproveOne'] }</button>
        </c:when>
        <c:otherwise>
          <c:set var="aiAgentApproveCountText" value="${textContainer.text['aiAgentApproveCount'] }" />
          <button type="button" class="btn btn-primary" id="aiAgentApproveButtonId" aria-controls="aiAgentConversationDivId"
            data-text-one="${grouper:escapeHtml(textContainer.text['aiAgentApproveCountOne'])}"
            data-text-many="${grouper:escapeHtml(aiAgentApproveCountText)}"
            onclick="$('#aiAgentDecisionId').val('checked'); ajax('../app/UiV2AiAgent.aiAgentResolve', {formIds: 'aiAgentApproveFormId'}); return false;"
            >${grouper:escapeHtml(fn:replace(aiAgentApproveCountText, '##', aiAgentContainer.pendingConfirmationCount))}</button>
          <script type="text/javascript">
            // the approve button says how many changes it will make, and cannot be used with none
            function aiAgentApproveCountChanged() {
              var count = $('#aiAgentApproveFormId input[name="aiAgentApprove"]:checked').length;
              var button = $('#aiAgentApproveButtonId');
              button.text(count == 1 ? button.attr('data-text-one') : button.attr('data-text-many').replace('##', count));
              button.prop('disabled', count == 0);
            }
          </script>
        </c:otherwise>
      </c:choose>
      &nbsp;
      <button type="button" class="btn" aria-controls="aiAgentConversationDivId"
        onclick="$('#aiAgentDecisionId').val('decline'); ajax('../app/UiV2AiAgent.aiAgentResolve', {formIds: 'aiAgentApproveFormId'}); return false;"
        ><c:choose>
          <c:when test="${aiAgentContainer.pendingConfirmationCount == 1}">${textContainer.text['aiAgentDeclineOne'] }</c:when>
          <c:otherwise>${textContainer.text['aiAgentDeclineAll'] }</c:otherwise>
        </c:choose></button>
    </div>
  </form>
</c:if>

<%-- the question count is the one people are expected to watch, so it comes first --%>
<%-- counts are grouped by the browser's locale, e.g. 1,234,567 --%>
<c:if test="${aiAgentContainer.maxQuestionsPerDay != null}">
  <p style="color: #767676;"><small>${textContainer.text['aiAgentQuestionsToday'] }
    ${aiAgentContainer.formatNumber(aiAgentContainer.questionsUsedToday)} / ${aiAgentContainer.formatNumber(aiAgentContainer.maxQuestionsPerDay)}</small></p>
</c:if>

<%-- with a limit, one line: what counts toward it, then where that number comes from.  with no
     limit, just the breakdown --%>
<c:choose>
  <c:when test="${aiAgentContainer.maxTokensPerConversation != null}">
    <p style="color: #767676;"><small>${textContainer.text['aiAgentConversationUsage'] }
      ${aiAgentContainer.formatNumber(aiAgentContainer.conversationTokensUsed)} / ${aiAgentContainer.formatNumber(aiAgentContainer.maxTokensPerConversation)}
      <c:if test="${aiAgentContainer.inputTokens > 0}">(${aiAgentContainer.formatNumber(aiAgentContainer.inputTokens)} ${textContainer.text['aiAgentTokensInput'] },
        ${textContainer.text['aiAgentTokensOfWhich'] } ${aiAgentContainer.formatNumber(aiAgentContainer.cacheReadTokens)} ${textContainer.text['aiAgentTokensCachedNotCounted'] },
        ${aiAgentContainer.formatNumber(aiAgentContainer.outputTokens)} ${textContainer.text['aiAgentTokensOutput'] })</c:if></small></p>
  </c:when>
  <c:when test="${aiAgentContainer.inputTokens > 0}">
    <p style="color: #767676;"><small>${textContainer.text['aiAgentTokensLabel'] }
      ${aiAgentContainer.formatNumber(aiAgentContainer.inputTokens)} ${textContainer.text['aiAgentTokensInput'] }
      (${aiAgentContainer.formatNumber(aiAgentContainer.cacheReadTokens)} ${textContainer.text['aiAgentTokensCached'] }),
      ${aiAgentContainer.formatNumber(aiAgentContainer.outputTokens)} ${textContainer.text['aiAgentTokensOutput'] }</small></p>
  </c:when>
</c:choose>

<c:if test="${aiAgentContainer.maxTokensPerDay != null}">
  <p style="color: #767676;"><small>${textContainer.text['aiAgentUsageToday'] }
    ${aiAgentContainer.formatNumber(aiAgentContainer.tokensUsedToday)} / ${aiAgentContainer.formatNumber(aiAgentContainer.maxTokensPerDay)}</small></p>
</c:if>
