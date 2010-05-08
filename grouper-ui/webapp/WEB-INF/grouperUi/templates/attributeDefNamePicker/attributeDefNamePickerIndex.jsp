<%@ include file="../common/commonTaglib.jsp"%>
<!-- subjectPicker/subjectPickerIndex.jsp: main page -->

<grouper:title label="${attributeDefNamePickerContainer.header}" />

<div class="section">
  <grouper:subtitle label="${attributeDefNamePickerContainer.searchSectionTitle}">
  &nbsp; &nbsp; &nbsp; &nbsp; 
  &nbsp; &nbsp; &nbsp; &nbsp; 
  &nbsp; &nbsp; &nbsp; &nbsp; 
  <a href="#" onclick="guiWindowClose(); return false;" class="smallLink">${attributeDefNamePickerContainer.cancelText}</a>
  </grouper:subtitle>
  <div class="sectionBody">
    <%-- dont sub --%>
    <form name="attributeDefNamePickerSearchFormName" id="attributeDefNamePickerSearchFormId" onsubmit="return false;"  >
      <input type="text" name="attributeDefNamePickerSearchFieldName"/> <input class="blueButton" type="submit" 
        onclick="ajax('AttributeDefNamePicker.search?attributeDefNamePickerName=${attributeDefNamePickerContainer.attributeDefNamePickerName}&attributeDefNamePickerElementName=${attributeDefNamePickerContainer.attributeDefNamePickerElementName}&searchField=' + guiFieldValue($('input[name=attributeDefNamePickerSearchFieldName]')[0])); return false;" 
        value="${attributeDefNamePickerContainer.searchButtonText}" />
      
    </form>
  </div>
</div>

<c:if test="${attributeDefNamePickerContainer.submitToUrl}">

  <%-- if submitting to url, this is the form to submit --%>
  <form name="submitToUrlFormName" action="${attributeDefNamePickerContainer.submitResultToUrl }" id="submitToUrlFormId" method="get">
    <input type="hidden" name="attributeDefName.id" id="attributeDefName.id.elementId" />
    <input type="hidden" name="attributeDefName.attributeDefNamePickerElementName" id="attributeDefName.attributeDefNamePickerElementName.elementId" />
    <input type="hidden" name="attributeDefName.screenLabel" id="attributeDefName.screenLabel.elementId" />
    <input type="hidden" name="attributeDefName.attributeDefNameName" id="attributeDefName.attributeDefNameName.elementId" />
    <input type="hidden" name="attributeDefName.attributeDefNameDisplayName" id="attributeDefName.attributeDefNameDisplayName.elementId" />
    <input type="hidden" name="attributeDefName.attributeDefNameDescription" id="attributeDefName.attributeDefNameDescription.elementId" />
  </form>

</c:if>

<div id="searchResultsDiv">

</div>
<!-- end attributeDefNamePicker/attributeDefNamePickerIndex.jsp: main page -->
