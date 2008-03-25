<%-- @annotation@ 
			Displays a group when part of a chain
--%><%--
  @author Gary Brown.
  @version $Id: groupChainPathView.jsp,v 1.4 2008-03-25 14:59:51 mchyzer Exp $
--%><%@page import="org.apache.struts.tiles.ComponentContext"%><%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>

	<c:set target="${viewObject}" property="callerPageId" value="${thisPageId}"/>

 <%--  Use params to make link title descriptive for accessibility --%>
 <span class="groupSummaryLink">		
<c:set var="linkTitle"><grouper:message bundle="${nav}" key="browse.to.group.summary">
		 		<grouper:param value="${viewObject.displayExtension}"/>
</grouper:message></c:set>
<html:link page="/populateGroupSummary.do" name="viewObject" title="${linkTitle}">
  <tiles:insert definition="dynamicTileDef" flush="false">
	  <tiles:put name="viewObject" beanName="viewObject"/>
	  <tiles:put name="view" value="default"/>
  </tiles:insert>
</html:link>
</span>