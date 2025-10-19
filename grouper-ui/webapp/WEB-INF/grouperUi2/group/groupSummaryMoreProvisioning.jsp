 <%@ include file="../assetsJsp/commonTaglib.jsp"%>
 
           <c:if test="${grouperRequestContainer.groupSummaryContainer.provisioningAssignmentCount > 0}">
              ${textContainer.text['groupSummaryPageProvisionedTargetMessage']}
              <c:if test="${not empty grouperRequestContainer.groupSummaryContainer.guiGrouperProvisioningAttributeValues}"> 
                <c:forEach var="guiGrouperProvisioningAttributeValue" varStatus="status" items="${grouperRequestContainer.groupSummaryContainer.guiGrouperProvisioningAttributeValues}">
                  ${guiGrouperProvisioningAttributeValue.externalizedName}
                  <c:if test="${!status.last}">,</c:if>
                </c:forEach>
              </c:if>
          </c:if>
 