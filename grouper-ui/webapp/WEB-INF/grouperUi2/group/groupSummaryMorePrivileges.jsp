  <%@ include file="../assetsJsp/commonTaglib.jsp"%>
 
            <c:choose>
              <c:when test="${grouperRequestContainer.groupSummaryContainer.nonGroupTotalPrivilegesCount > 0 or 
                grouperRequestContainer.groupSummaryContainer.totalPrivilegesCount > 0 or
                grouperRequestContainer.groupSummaryContainer.directPrivilegesCount > 0
               }">
                 <li style="margin-left: 12px;">${textContainer.text['groupSummaryPagePrivilegesNotGroupPrivilegesUsedCountMessage']}</li>
                 <li style="margin-left: 12px;">${textContainer.text['groupSummaryPagePrivilegesTotalGroupPrivilegesUsedCountMessage']}</li>
                 <li style="margin-left: 12px;">${textContainer.text['groupSummaryPagePrivilegesDirectGroupPrivilegesUsedCountMessage']}</li>
                 <c:if test="${not empty grouperRequestContainer.groupSummaryContainer.directGroupPrivilegesGroups}">
                    <li style="margin-left: 12px;">${textContainer.text['groupSummaryPagePrivilegesDirectGroupPrivileges']} 
                      <c:forEach var="directGroupPrivilegesGroup" varStatus="status" items="${grouperRequestContainer.groupSummaryContainer.directGroupPrivilegesGroups}">
                        ${directGroupPrivilegesGroup.shortLinkWithIcon}
                        <c:if test="${!status.last}">,</c:if>
                      </c:forEach>
                    </li>
                 </c:if>
              </c:when>
              <c:otherwise>
                <li style="margin-left: 12px;">${textContainer.text['groupSummaryPageMembershipsGroupNotUsedMessage'] }</li>
              </c:otherwise>
            </c:choose>
            
            <c:choose>
              <c:when test="${grouperRequestContainer.groupSummaryContainer.countOfWhereGroupIsBeingUsedInPrivileges > 0}">
                 <li style="margin-left: 12px;">${textContainer.text['groupSummaryPagePrivilegesGroupUsedInOtherGroupsPrivileges']}
                 <c:if test="${not empty grouperRequestContainer.groupSummaryContainer.groupsWhereGroupIsBeingUsedInPrivileges}"> 
                      <c:forEach var="groupWhereGroupIsBeingUsedInPrivileges" varStatus="status" items="${grouperRequestContainer.groupSummaryContainer.groupsWhereGroupIsBeingUsedInPrivileges}">
                        ${groupWhereGroupIsBeingUsedInPrivileges.shortLinkWithIcon}
                        <c:if test="${!status.last}">,</c:if>
                      </c:forEach>
                 </c:if>
                 </li>
              </c:when>
              <c:otherwise>
                <li style="margin-left: 12px;">${textContainer.text['groupSummaryPagePrivilegesGroupNotUsedMessage']}</li>
              </c:otherwise>
            </c:choose>
