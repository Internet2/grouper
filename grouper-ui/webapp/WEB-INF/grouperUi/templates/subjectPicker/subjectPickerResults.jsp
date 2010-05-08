<%@ include file="../common/commonTaglib.jsp"%>
<!-- subjectPicker/subjectPickerResults.jsp: search results page -->

<div class="section">
  <grouper:subtitle label="${subjectPickerContainer.resultsSectionTitle}" />
  <div class="sectionBody">
    <ul>
      <c:forEach items="${subjectPickerContainer.pickerResultSubjects}" var="pickerResultSubject">
        <div>
          <%-- show an icon for the subject --%>
          <grouper:subjectIcon subject="${pickerResultSubject.subject}" /> 
          ${fn:escapeXml(pickerResultSubject.screenLabel) } 
          <c:if test="${subjectPickerContainer.submitToUrl}">
            (<a href="#" onclick="return guiSubmitSubjectPickerToUrl('${subjectPickerContainer.subjectPickerElementName}', '${grouper:escapeJavascript(pickerResultSubject.subjectId) }','${grouper:escapeJavascript(pickerResultSubject.screenLabel) }' ); ">Select</a>)
          </c:if>
          <c:if test="${!subjectPickerContainer.submitToUrl}">
            (<a href="#" onclick="guiOpener().grouperSubjectSelected('${subjectPickerContainer.subjectPickerElementName}', '${grouper:escapeJavascript(pickerResultSubject.subjectId) }','${grouper:escapeJavascript(pickerResultSubject.screenLabel) }', ${pickerResultSubject.subjectObjectName} ); window.close(); return false;">Select</a>)
          </c:if>
          
        </div>
      </c:forEach>
    </ul>
  </div>
</div>

<%-- all the subject objects converted to javascript --%>
${subjectPickerContainer.subjectsScript }

<!-- end subjectPicker/subjectPickerResults.jsp: search results page -->
