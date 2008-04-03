<%-- @annotation@
		  Main page for the 'Create' browse mode
--%><%--
  @author Gary Brown.
  @version $Id: CreateGroups.jsp,v 1.4 2008-04-03 07:48:21 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<c:choose>
	<c:when test="${!isAdvancedSearch}">
<div class="pageBlurb">
	<grouper:message bundle="${nav}" key="groups.create.can"/>
</div>
  <grouper:subtitle key="stems.heading.browse" />
<tiles:insert definition="browseStemsDef"/>
<tiles:insert definition="flattenDef"/>

<tiles:insert definition="simpleSearchStemsDef"/>

<tiles:insert definition="stemLinksDef"/>
</c:when>
<c:otherwise>

<tiles:insert definition="advancedSearchStemsDef"/>
</c:otherwise>
</c:choose> 




