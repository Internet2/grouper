<%-- @annotation@
		  Tile which lets user select which field to display in search results
--%><%--
  @author Gary Brown.
  @version $Id: searchGroupResultFieldChoice.jsp,v 1.2 2006-07-06 14:58:08 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<tiles:importAttribute ignore="true"/>
<%
	Map map = (Map)session.getAttribute("mediaMap");
	String fieldList = (String)map.get("search.group.result-field-choice");
	if(fieldList != null && !fieldList.startsWith("???") && !"".equals(fieldList)) {
%>
<div class="searchGroupResultFieldChoice"><div class="formRow">
<c:if test="${empty groupSearchResultField}"><c:set var="groupSearchResultField" value="${mediaMap['search.group.result-field']}" scope="request"/></c:if>
<c:forTokens items="${mediaMap['search.group.result-field-choice']}" delims=" " var="field" varStatus="counter">
	<c:if test="${counter.count==1}"><fmt:message bundle="${nav}" key="find.groups.select-result-field"/></c:if>
	<c:set var="checked" value=""/>
	<c:if test="${field==groupSearchResultField}"><c:set var="checked" value="checked='checked'"/></c:if>
<input type="radio" name="groupSearchResultField" value="<c:out value="${field}"/>" <c:out value="${checked}" escapeXml="false"/>/> <c:out value="${field}"/>
</c:forTokens>
</div></div>

<%
}
%>
</grouper:recordTile>