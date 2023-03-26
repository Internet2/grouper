 <%@ include file="../assetsJsp/commonTaglib.jsp"%>

    <c:if test="${grouperRequestContainer.provisionerConfigurationContainer.grouperProvisioningErrors != null && fn:length(grouperRequestContainer.provisionerConfigurationContainer.grouperProvisioningErrors) > 0}">
     
     <table class="table table-hover table-bordered table-striped table-condensed data-table table-bulk-update footable">
      <thead>
        <tr>
        
          <th><span>${textContainer.text['provisionerErrorsTableErrorTimestamp'] }</span></th>
          <th><span>${textContainer.text['provisionerErrorsTableObjectType'] }</span></th>
          <th><span>${textContainer.text['provisionerErrorsTableGroupName'] }</span></th>
          <th><span>${textContainer.text['provisionerErrorsTableSubjectId'] }</span></th>
          <th><span>${textContainer.text['provisionerErrorsTableSubjectIdentifier'] }</span></th>
          <th><span>${textContainer.text['provisionerErrorsTableSubjectFatal'] }</span></th>
          <th><span>${textContainer.text['provisionerErrorsTableErrorCode'] }</span></th>
          <th><span>${textContainer.text['provisionerErrorsTableErrorDescription'] }</span></th>
         
        </tr>
      </thead>
      <tbody>
      
        <c:forEach items="${grouperRequestContainer.provisionerConfigurationContainer.grouperProvisioningErrors}" var="error">
          <tr>
            <td>${error.errorTimestamp}</td>
            <td>${error.objectType}</td>
            <td>${error.groupName}</td>
            <td>${error.subjectId}</td>
            <td>${error.subjectIdentifier}</td>
            <td>${error.fatal}</td>
            <td>${error.errorCode}</td>
            <td style="white-space: nowrap"><grouper:abbreviateTextarea text="${error.errorDescription}" showCharCount="30" cols="20" rows="3"/></td>
          </tr>
        </c:forEach>
      
      </tbody>
    </table>
     
   </c:if>