<%-- @annotation@
		  Main page for the 'Create' browse mode
--%><%--
  @author Gary Brown.
  @version $Id: CreateGroups.jsp,v 1.1.1.1 2005-08-23 13:04:20 isgwb Exp $
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
<tiles:insert definition="flattenDef">
	<tiles:put name="pageName" value="populateCreateGroups"/>
</tiles:insert>

<tiles:insert definition="simpleSearchStemsDef"/>

<tiles:insert definition="stemLinksDef"/>
</c:when>
<c:otherwise>

<tiles:insert definition="advancedSearchStemsDef"/>
</c:otherwise>
</c:choose> 




