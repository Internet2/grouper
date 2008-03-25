<%-- @annotation@
		  Main page for the 'Create' browse mode
--%><%--
  @author Gary Brown.
  @version $Id: CreateGroups.jsp,v 1.3 2008-03-25 14:59:51 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<c:choose>
	<c:when test="${!isAdvancedSearch}">
<div class="pageBlurb">
	<grouper:message bundle="${nav}" key="groups.create.can"/>
</div>
<h2 class="actionheader">
	<grouper:message bundle="${nav}" key="stems.heading.browse"/>
</h2>
<tiles:insert definition="browseStemsDef"/>
<tiles:insert definition="flattenDef"/>

<tiles:insert definition="simpleSearchStemsDef"/>

<tiles:insert definition="stemLinksDef"/>
</c:when>
<c:otherwise>

<tiles:insert definition="advancedSearchStemsDef"/>
</c:otherwise>
</c:choose> 




