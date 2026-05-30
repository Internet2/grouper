<%@ include file="../assetsJsp/commonTaglib.jsp"%>

<option value="">${textContainer.text['dataFieldDictionaryFilterAny']}</option>
<c:forEach items="${grouperRequestContainer.entityDataFieldsContainer.dictionaryDataFieldOptions}" var="opt">
  <option value="${grouper:escapeHtml(opt[0])}">${grouper:escapeHtml(opt[1])}</option>
</c:forEach>
