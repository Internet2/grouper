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
              <c:forEach var="fieldConfigId" items="${dataRowConfigIdToFieldConfigId.value.fieldConfigIds}">
                  <th>${fieldConfigId}</th> <%-- Column Headers --%>
              </c:forEach>
          </tr>
        </thead>
        <tbody>

         <%-- One table row per data row the subject has.  Each "row" is a map of fieldConfigId -> value,
              so every cell is looked up by field id against its own row and cannot shift into another row. --%>
         <c:forEach var="row" items="${dataRowConfigIdToFieldConfigId.value.rows}">
            <tr>
                <c:forEach var="fieldConfigId" items="${dataRowConfigIdToFieldConfigId.value.fieldConfigIds}">
                    <td style="white-space: nowrap;">
                        <c:choose>
                            <c:when test="${not empty row[fieldConfigId]}">
                                ${row[fieldConfigId]}
                            </c:when>
                            <c:otherwise> - </c:otherwise> <%-- this row has no value for this field --%>
                        </c:choose>
                    </td>
                </c:forEach>
            </tr>
        </c:forEach>

        </tbody>
       </table>

      </c:forEach>
</div>
