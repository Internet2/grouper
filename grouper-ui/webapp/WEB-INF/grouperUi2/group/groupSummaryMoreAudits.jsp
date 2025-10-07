 <%@ include file="../assetsJsp/commonTaglib.jsp"%>
 
    <c:choose>
      <c:when test="${grouperRequestContainer.groupSummaryContainer.auditsInTheLastMonth > 0}">
        ${textContainer.text['groupSummaryPageRecentAuditsMessage']}
      </c:when>
      <c:otherwise>
        ${textContainer.text['groupSummaryPageNoRecentAuditsMessage']}
      </c:otherwise>
    </c:choose>
