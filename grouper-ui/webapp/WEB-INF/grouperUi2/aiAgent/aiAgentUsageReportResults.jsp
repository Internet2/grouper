<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<%-- names and ids come from the members table, so they are escaped --%>
<c:set var="aiAgentContainer" value="${grouperRequestContainer.aiAgentContainer}" />

<c:choose>
  <c:when test="${empty aiAgentContainer.usageReportRows}">
    <p>${textContainer.text['aiAgentReportNone'] }</p>
  </c:when>
  <c:otherwise>

    <c:if test="${aiAgentContainer.usageReportTruncated}">
      <p style="color: #767676;">${textContainer.text['aiAgentReportTruncated'] }</p>
    </c:if>

    <table class="table table-hover table-bordered table-striped table-condensed data-table">
      <thead>
        <tr>
          <th>${textContainer.text['aiAgentReportUser'] }</th>
          <th style="text-align: right;">${textContainer.text['aiAgentReportMessages'] }</th>
          <th style="text-align: right;">${textContainer.text['aiAgentReportModelCalls'] }</th>
          <th style="text-align: right;">${textContainer.text['aiAgentReportInputTokens'] }</th>
          <th style="text-align: right;">${textContainer.text['aiAgentReportCachedInputTokens'] }</th>
          <th style="text-align: right;">${textContainer.text['aiAgentReportOutputTokens'] }</th>
          <%-- what the rows are sorted by, and what the token limits count --%>
          <th style="text-align: right;">${textContainer.text['aiAgentReportLimitTokens'] }</th>
        </tr>
      </thead>
      <tbody>
        <c:forEach items="${aiAgentContainer.usageReportRows}" var="reportRow">
          <tr>
            <td>
              <c:choose>
                <c:when test="${reportRow.subjectId == null}">
                  ${textContainer.text['aiAgentReportMemberGone'] } ${reportRow.memberInternalId}
                </c:when>
                <c:otherwise>
                  ${grouper:escapeHtml(reportRow.subjectName)}
                  <span style="color: #767676;">(${grouper:escapeHtml(reportRow.subjectSourceId)}:
                    ${grouper:escapeHtml(reportRow.subjectId)})</span>
                </c:otherwise>
              </c:choose>
            </td>
            <td style="text-align: right;">${aiAgentContainer.formatNumber(reportRow.messages)}</td>
            <td style="text-align: right;">${aiAgentContainer.formatNumber(reportRow.modelCalls)}</td>
            <td style="text-align: right;">${aiAgentContainer.formatNumber(reportRow.inputTokens)}</td>
            <td style="text-align: right;">${aiAgentContainer.formatNumber(reportRow.cachedInputTokens)}</td>
            <td style="text-align: right;">${aiAgentContainer.formatNumber(reportRow.outputTokens)}</td>
            <td style="text-align: right;">${aiAgentContainer.formatNumber(reportRow.limitTokens)}</td>
          </tr>
        </c:forEach>
      </tbody>
      <tfoot>
        <tr>
          <th>${textContainer.text['aiAgentReportTotal'] }</th>
          <th style="text-align: right;">${aiAgentContainer.formatNumber(aiAgentContainer.usageReportTotal.messages)}</th>
          <th style="text-align: right;">${aiAgentContainer.formatNumber(aiAgentContainer.usageReportTotal.modelCalls)}</th>
          <th style="text-align: right;">${aiAgentContainer.formatNumber(aiAgentContainer.usageReportTotal.inputTokens)}</th>
          <th style="text-align: right;">${aiAgentContainer.formatNumber(aiAgentContainer.usageReportTotal.cachedInputTokens)}</th>
          <th style="text-align: right;">${aiAgentContainer.formatNumber(aiAgentContainer.usageReportTotal.outputTokens)}</th>
          <th style="text-align: right;">${aiAgentContainer.formatNumber(aiAgentContainer.usageReportTotal.limitTokens)}</th>
        </tr>
      </tfoot>
    </table>

    <p style="color: #767676;"><small>${textContainer.text['aiAgentReportBillingNote'] }</small></p>
  </c:otherwise>
</c:choose>
