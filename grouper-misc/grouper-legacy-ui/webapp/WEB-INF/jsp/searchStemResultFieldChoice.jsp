<%-- @annotation@
		  Tile which lets user select which field to display in search results
--%><%--
  @author Gary Brown.
  @version $Id: searchStemResultFieldChoice.jsp,v 1.6 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<tiles:importAttribute ignore="true"/>
<%
	Map map = (Map)session.getAttribute("mediaMap");
	String fieldList = (String)map.get("search.stem.result-field-choice");
	if(fieldList != null && !fieldList.startsWith("???") && !"".equals(fieldList)) {
%>
<tr class="formTableRow">
<c:if test="${empty stemSearchResultField}"><c:set var="stemSearchResultField" value="${mediaMap['search.stem.result-field']}"/></c:if>
<c:forTokens items="${mediaMap['search.stem.result-field-choice']}" delims=" " var="field" varStatus="counter">
	<c:if test="${counter.count==1}">
    <td class="formTableLeft"><grouper:message key="find.stems.select-result-field"/>
    </td><td class="formTableRight"></c:if>
	
  <c:set var="checked" value=""/>
	<c:set var="fieldLookup">field.stem.displayName.<c:out value="${field}"/></c:set>
	<c:if test="${field==stemSearchResultField}"><c:set var="checked" value="checked='checked'"/></c:if>
<input type="radio" name="stemSearchResultField" value="<c:out value="${field}"/>" <c:out value="${checked}" escapeXml="false"/>/> <c:out value="${navMap[fieldLookup]}"/>
</c:forTokens>
</td>
</tr>

<%
}
%>
</grouper:recordTile>