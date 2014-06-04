<%@ include file="../common/commonTaglib.jsp"%>
<!-- Start: subjectPicker/subjectPickerIndex.jsp: main page -->

<grouper:title label="${subjectPickerContainer.header}" />

<div class="section">
  <grouper:subtitle label="${subjectPickerContainer.searchSectionTitle}">
  &nbsp; &nbsp; &nbsp; &nbsp; 
  &nbsp; &nbsp; &nbsp; &nbsp; 
  &nbsp; &nbsp; &nbsp; &nbsp; 
  <a href="#" onclick="guiWindowClose(); return false;" class="smallLink">${subjectPickerContainer.cancelText}</a>
  </grouper:subtitle>
  <div class="sectionBody">
    <%-- dont sub --%>
    <form name="subjectPickerSearchFormName" id="subjectPickerSearchFormId" onsubmit="return false;"  >
      <input type="text" name="subjectPickerSearchFieldName"/> <input class="blueButton" type="submit" 
        onclick="ajax('SubjectPicker.search?subjectPickerName=${subjectPickerContainer.subjectPickerName}&subjectPickerElementName=${subjectPickerContainer.subjectPickerElementName}&searchField=' + guiFieldValue($('input[name=subjectPickerSearchFieldName]')[0])); return false;" 
        value="${subjectPickerContainer.searchButtonText}" />
      
    </form>
  </div>
</div>

<c:if test="${subjectPickerContainer.submitToUrl}">

  <%-- if submitting to url, this is the form to submit --%>
  <form name="submitToUrlFormName" action="${subjectPickerContainer.submitResultToUrl }" id="submitToUrlFormId" method="get">
    <input type="hidden" name="subject.id" id="subject.id.elementId" />
    <input type="hidden" name="subject.subjectPickerElementName" id="subject.subjectPickerElementName.elementId" />
    <input type="hidden" name="subject.screenLabel" id="subject.screenLabel.elementId" />
  </form>

</c:if>

<div id="searchResultsDiv">

</div>
<!-- end subjectPicker/subjectPickerIndex.jsp: main page -->
