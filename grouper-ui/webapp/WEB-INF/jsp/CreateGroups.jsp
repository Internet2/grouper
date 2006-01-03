<%-- @annotation@
		  Main page for the 'Create' browse mode
--%><%--
  @author Gary Brown.
  @version $Id: CreateGroups.jsp,v 1.2 2006-01-03 13:30:13 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<c:choose>
	<c:when test="${!isAdvancedSearch}">
<div class="pageBlurb">
	<fmt:message bundle="${nav}" key="groups.create.can"/>
</div>
<h2 class="actionheader">
	<fmt:message bundle="${nav}" key="stems.heading.browse"/>
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




