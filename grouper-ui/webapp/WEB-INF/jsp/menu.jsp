<%-- @annotation@ 
		 Standard tile used in baseDef which renders the menu. Use css to change position 
--%><%--
  @author Gary Brown.
  @version $Id: menu.jsp,v 1.1 2005-11-14 14:12:26 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>

<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<jsp:useBean id="tabStyle" class="java.util.HashMap"/>
<c:forEach var="menuItem" items="${menuItems}">
	<c:set  target="${tabStyle}" property="${menuItem.functionalArea}" value="tab"/>
</c:forEach>
<c:set  target="${tabStyle}" property="${functionalArea}" value="selectedTab"/>

<c:forEach var="menuItem" items="${menuItems}">
	<div class="actionbox">
		<html:link styleClass="${tabStyle[menuItem.functionalArea]}" page="${menuItem.action}" title="${navMap[menuItem.titlekey]}">
			<fmt:message bundle="${nav}" key="${menuItem.linkKey}"/>
		</html:link>
	</div>
 </c:forEach>       
</grouper:recordTile>