<%-- @annotation@ 
			Main page for the 'Manage' browse mode
--%><%--
  @author Gary Brown.
  @version $Id: ManageGroups.jsp,v 1.3 2008-03-25 14:59:51 mchyzer Exp $
--%>

<%@include file="/WEB-INF/jsp/include.jsp"%>
<c:choose>
	<c:when test="${!isAdvancedSearch}">
<div class="pageBlurb">
	<grouper:message bundle="${nav}" key="groups.manage.can"/>
</div>
<h2 class="actionheader">
	<grouper:message bundle="${nav}" key="groups.heading.browse"/>
</h2>
<tiles:insert definition="browseStemsDef"/>
<tiles:insert definition="flattenDef"/>

<tiles:insert definition="simpleSearchGroupsDef"/>
<tiles:insert definition="stemLinksDef"/></c:when>
<c:otherwise>

<tiles:insert definition="advancedSearchGroupsDef"/>
</c:otherwise>
</c:choose> 