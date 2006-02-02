<%-- @annotation@ 
			Displays a group when part of a chain
--%><%--
  @author Gary Brown.
  @version $Id: groupChainPathView.jsp,v 1.2 2006-02-02 16:38:08 isgwb Exp $
--%><%@page import="org.apache.struts.tiles.ComponentContext"%><%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute name="viewObject"/>
 <%--  Use params to make link title descriptive for accessibility --%>
 <span class="groupSummaryLink">		
<c:set var="linkTitle"><fmt:message bundle="${nav}" key="browse.to.group.summary">
		 		<fmt:param value="${viewObject.displayExtension}"/>
</fmt:message></c:set>
<html:link page="/populateGroupSummary.do" name="viewObject" title="${linkTitle}">
  <tiles:insert definition="dynamicTileDef" flush="false">
	  <tiles:put name="viewObject" beanName="viewObject"/>
	  <tiles:put name="view" value="default"/>
  </tiles:insert>
</html:link>
</span>