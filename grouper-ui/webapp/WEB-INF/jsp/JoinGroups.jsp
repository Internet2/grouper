<%-- @annotation@ 
		  Main page for the 'Join' browse mode
--%><%--
  @author Gary Brown.
  @version $Id: JoinGroups.jsp,v 1.5 2008-04-03 07:48:21 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<c:choose>
	<c:when test="${!isAdvancedSearch}">
<div class="pageBlurb">
	<grouper:message bundle="${nav}" key="groups.join.can"/>
</div>
<grouper:subtitle key="groups.heading.browse" />
<tiles:insert definition="browseStemsDef"/>
<tiles:insert definition="flattenDef"/>

<tiles:insert definition="simpleSearchGroupsDef"/>
</c:when>
<c:otherwise>

<tiles:insert definition="advancedSearchGroupsDef"/>
</c:otherwise>
</c:choose> 
