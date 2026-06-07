 <%@ include file="../assetsJsp/commonTaglib.jsp"%>
 
              <c:choose>
                
                <c:when test="${ grouperRequestContainer.groupSummaryContainer.totalMembersCount > 0 }">
                   <li style="margin-left: 12px;">${grouperRequestContainer.groupSummaryContainer.totalMembersCount} ${textContainer.text['groupSummaryPageMembershipsTotalMembers'] }</li>
                   <li style="margin-left: 12px;">${grouperRequestContainer.groupSummaryContainer.notGroupMembersCount} ${textContainer.text['groupSummaryPageMembershipsNonGroupMembers'] }</li>
                   <li style="margin-left: 12px;">${grouperRequestContainer.groupSummaryContainer.directMembersCount} ${textContainer.text['groupSummaryPageMembershipsDirectMembers'] }</li>
                   <c:if test="${grouperRequestContainer.groupSummaryContainer.directGroupMembersCount > 0}">
                      <li style="margin-left: 12px;"> ${textContainer.text['groupSummaryPageMembershipsDirectGroupMembers']}
                        <c:forEach var="directGroupMember" varStatus="status" items="${grouperRequestContainer.groupSummaryContainer.directGroupMembers}">
                          ${directGroupMember.shortLinkWithIcon}
                          <c:if test="${!status.last}">,</c:if>
                        </c:forEach>
                      </li>
                   </c:if>
                </c:when>
                <c:otherwise>
                  <li style="margin-left: 12px;">${textContainer.text['groupSummaryPageMembershipsNone'] }</li>
                </c:otherwise>
              </c:choose>
              <c:choose>
                
                <c:when test="${grouperRequestContainer.groupSummaryContainer.groupAsMemberCount > 0}">
                   <li style="margin-left: 12px;">${textContainer.text['groupSummaryPageMembershipsGroupUsedCountMessage'] }<c:if 
                   test="${not empty grouperRequestContainer.groupSummaryContainer.groupsWhereTheCurrentGroupIsMemberOf}"><c:forEach 
                   var="groupWhereTheCurrentGroupIsMemberOf" varStatus="status" items="${grouperRequestContainer.groupSummaryContainer.groupsWhereTheCurrentGroupIsMemberOf}"><c:if test="${status.first}">:</c:if>
                          ${groupWhereTheCurrentGroupIsMemberOf.shortLinkWithIcon}
                          <c:if test="${!status.last}">,</c:if>
                        </c:forEach>
                   </c:if>
                   </li>
                </c:when>
                <c:otherwise>
                  <li style="margin-left: 12px;">${textContainer.text['groupSummaryPageMembershipsGroupNotUsedMessage'] }</li>
                </c:otherwise>
              </c:choose>
 