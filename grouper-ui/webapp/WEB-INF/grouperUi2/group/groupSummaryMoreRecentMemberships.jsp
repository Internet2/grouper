  <%@ include file="../assetsJsp/commonTaglib.jsp"%>
 
  <c:choose>
    <c:when test="${grouperRequestContainer.groupSummaryContainer.newMembershipsInTheLastMonth > 0 or grouperRequestContainer.groupSummaryContainer.membershipsRemovedInTheLastMonth > 0}">
      ${textContainer.text['groupSummaryPageRecentMembershipChangesMessage']}
    </c:when>
    <c:otherwise>
      ${textContainer.text['groupSummaryPageNoRecentMembershipChangesMessage']}
    </c:otherwise>
  </c:choose>

