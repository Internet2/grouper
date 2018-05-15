<%@ include file="../common/commonTaglib.jsp"%>
<!-- attributeDefNamePicker/attributeDefNamePickerResults.jsp: search results page -->

<div class="section">
  <grouper:subtitle label="${attributeDefNamePickerContainer.resultsSectionTitle}" />
  <div class="sectionBody">
    <ul>
      <c:forEach items="${attributeDefNamePickerContainer.pickerResultAttributeDefNames}" var="pickerResultAttributeDefName">
        <li>
          ${fn:escapeXml(pickerResultAttributeDefName.screenLabel) } 
          <c:if test="${attributeDefNamePickerContainer.submitToUrl}">
            (<a href="#" onclick="return guiSubmitAttributeDefNamePickerToUrl('${attributeDefNamePickerContainer.attributeDefNamePickerElementName}', '${grouper:escapeJavascript(pickerResultAttributeDefName.attributeDefNameId) }','${grouper:escapeJavascript(pickerResultAttributeDefName.screenLabel) }', '${grouper:escapeJavascript(pickerResultAttributeDefName.attributeDefName.name) }', '${grouper:escapeJavascript(pickerResultAttributeDefName.attributeDefName.displayName) }', '${grouper:escapeJavascript(pickerResultAttributeDefName.attributeDefName.description) }' ); ">Select</a>)
          </c:if>
          <c:if test="${!attributeDefNamePickerContainer.submitToUrl}">
            (<a href="#" onclick="guiOpener().grouperAttributeDefNameSelected('${attributeDefNamePickerContainer.attributeDefNamePickerElementName}', '${grouper:escapeJavascript(pickerResultAttributeDefName.attributeDefNameId) }','${grouper:escapeJavascript(pickerResultAttributeDefName.screenLabel) }', '${grouper:escapeJavascript(pickerResultAttributeDefName.attributeDefName.name) }', '${grouper:escapeJavascript(pickerResultAttributeDefName.attributeDefName.displayName) }', '${grouper:escapeJavascript(pickerResultAttributeDefName.attributeDefName.description) }' ); window.close(); return false;">Select</a>)
          </c:if>
            
          
        </li>
      </c:forEach>
    </ul>
  </div>
</div>


<!-- end attributeDefNamePicker/attributeDefNamePickerResults.jsp: search results page -->
