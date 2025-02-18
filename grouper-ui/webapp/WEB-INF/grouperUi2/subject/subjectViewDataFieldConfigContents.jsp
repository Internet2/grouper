<%@ include file="../assetsJsp/commonTaglib.jsp"%>
<div class="row-fluid">
     
      <table
        class="table table-hover table-bordered table-striped table-condensed data-table">
        <thead>
          <tr>
            <th>${textContainer.text['subjectDataFieldConfigHeaderDataFieldAliases']}</th>
            <th>${textContainer.text['subjectDataFieldConfigHeaderDataFieldValue']}</th> 
          </tr>
        </thead>
        <tbody>
         <c:set var="i" value="0" />
         <c:forEach items="${grouperRequestContainer.subjectContainer.guiSubjectDataFieldConfigs}" var="guiSubjectDataFieldConfig">
              
            <tr>
            
             <td style="white-space: nowrap;">
                ${guiSubjectDataFieldConfig.aliases}
              </td>
              
              <td style="white-space: nowrap;">
                ${guiSubjectDataFieldConfig.uiFriendlyValue}
              </td>
              
              </tr>
              <c:set var="i" value="${i+1}" />
         </c:forEach>
              
        </tbody>
       </table>
       
       
      <c:forEach items="${grouperRequestContainer.subjectContainer.dataRowConfigIdToFieldConfigIds}" var="dataRowConfigIdToFieldConfigId">
      
      <p class="lead"> ${textContainer.text['subjectViewDataRowConfigIdPrefix']} '${dataRowConfigIdToFieldConfigId.key}' </p>
      
      <table
        class="table table-hover table-bordered table-striped table-condensed data-table">
        <thead>
          <tr>
              <c:forEach var="entry" items="${dataRowConfigIdToFieldConfigId.value}">
                  <th>${entry.key}</th> <%-- Column Headers --%>
              </c:forEach>
          </tr>
        </thead>
        <tbody>
        
        <%-- Find the maximum row count --%>
        <c:set var="maxRows" value="0" />
        <c:forEach var="entry" items="${dataRowConfigIdToFieldConfigId.value}">
            <c:if test="${fn:length(entry.value) > maxRows}">
                <c:set var="maxRows" value="${fn:length(entry.value)}" />
            </c:if>
        </c:forEach>
        
         <%-- Loop through rows dynamically --%>
         <c:forEach var="rowIndex" begin="0" end="${maxRows - 1}">
            <tr>
                <c:forEach var="entry" items="${dataRowConfigIdToFieldConfigId.value}">
                    <td style="white-space: nowrap;">
                        <c:choose>
                            <c:when test="${rowIndex < fn:length(entry.value)}">
                                ${entry.value[rowIndex]}
                            </c:when>
                            <c:otherwise> - </c:otherwise> <%-- Empty cells if list size is shorter --%>
                        </c:choose>
                    </td>
                </c:forEach>
            </tr>
        </c:forEach>
              
        </tbody>
       </table>
      
      </c:forEach>
</div>
