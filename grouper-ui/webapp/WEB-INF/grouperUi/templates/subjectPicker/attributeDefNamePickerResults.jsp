<%@ include file="../common/commonTaglib.jsp"%>
<!-- attributeDefNamePicker/attributeDefNamePickerResults.jsp: search results page -->

<div class="section">
  <grouper:subtitle label="${attributeDefNamePickerContainer.resultsSectionTitle}" />
  <div class="sectionBody">
    <ul>
      <c:forEach items="${attributeDefNamePickerContainer.pickerResultAttributeDefNames}" var="pickerResultAttributeDefName">
        <div>
          ${fn:escapeXml(pickerResultAttributeDefName.screenLabel) } 
          <c:if test="${attributeDefNamePickerContainer.submitToUrl}">
            (<a href="#" onclick="return guiSubmitAttributeDefNamePickerToUrl('${attributeDefNamePickerContainer.attributeDefNamePickerElementName}', '${grouper:escapeJavascript(pickerResultAttributeDefName.attributeDefNameId) }','${grouper:escapeJavascript(pickerResultAttributeDefName.screenLabel) }' ); ">Select</a>)
          </c:if>
          <%-- c:if test="${!attributeDefNamePickerContainer.submitToUrl}">
            (<a href="#" onclick="guiOpener().grouperAttributeDefNameSelected('${attributeDefNamePickerContainer.attributeDefNamePickerElementName}', '${grouper:escapeJavascript(pickerResultAttributeDefName.attributeDefNameId) }','${grouper:escapeJavascript(pickerResultAttributeDefName.screenLabel) }', ${pickerResultAttributeDefName.attributeDefNameObjectName} ); window.close(); return false;">Select</a>)
          </c:if --%>
            (<a href="#" onclick="window.close(); return false;">Select</a>)
          
        </div>
      </c:forEach>
    </ul>
  </div>
</div>

<%-- all the attributeDefName objects converted to javascript --%>
${attributeDefNamePickerContainer.attributeDefNamesScript }

<!-- end attributeDefNamePicker/attributeDefNamePickerResults.jsp: search results page -->
