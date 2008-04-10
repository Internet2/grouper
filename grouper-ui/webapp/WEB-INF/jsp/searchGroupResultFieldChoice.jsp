<%-- @annotation@
		  Tile which lets user select which field to display in search results
--%><%--
  @author Gary Brown.
  @version $Id: searchGroupResultFieldChoice.jsp,v 1.5 2008-04-10 19:50:25 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<tiles:importAttribute ignore="true"/>
<%
	Map map = (Map)session.getAttribute("mediaMap");
	String fieldList = (String)map.get("search.group.result-field-choice");
	if(fieldList != null && !fieldList.startsWith("???") && !"".equals(fieldList)) {
%>
<tr class="formTableRow">
  <c:if test="${empty groupSearchResultField}">
    <c:set var="groupSearchResultField" value="${mediaMap['search.group.result-field']}" scope="request"/>
  </c:if>
  <td class="formTableLeft">
    <grouper:message bundle="${nav}" key="find.groups.select-result-field"/>
  </td>
  <td class="formTableRight">
    <c:forTokens items="${mediaMap['search.group.result-field-choice']}" delims=" " var="field" varStatus="counter">
    	<c:if test="${counter.count==1}"></c:if>
    	<c:set var="checked" value=""/>
    	<c:if test="${field==groupSearchResultField}"><c:set var="checked" value="checked='checked'"/></c:if>
      <input type="radio" name="groupSearchResultField" value="<c:out value="${field}"/>" 
        <c:out value="${checked}" escapeXml="false"/>/> <c:out value="${fieldList[field].displayName}"/>
    </c:forTokens>
  </td>
</tr>
<%
}
%>
</grouper:recordTile>