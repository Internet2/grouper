<%@ include file="../common/commonTaglib.jsp"%>
<!-- subjectPicker/subjectPickerIndex.jsp: main page -->

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

<div id="searchResultsDiv">

</div>
<!-- end subjectPicker/subjectPickerIndex.jsp: main page -->
