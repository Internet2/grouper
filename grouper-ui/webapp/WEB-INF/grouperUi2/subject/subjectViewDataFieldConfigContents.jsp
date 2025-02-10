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
      
</div>
