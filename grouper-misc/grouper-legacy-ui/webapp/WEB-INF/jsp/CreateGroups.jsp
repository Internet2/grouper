<%-- @annotation@
		  Main page for the 'Create' browse mode
--%><%--
  @author Gary Brown.
  @version $Id: CreateGroups.jsp,v 1.6 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<c:choose>
	<c:when test="${!isAdvancedSearch}">
<div class="pageBlurb">
	<grouper:message key="groups.create.can"/>
</div>

<%-- capture output since it does logic we need --%>
<c:set var="browseStemsHtml">
  <tiles:insert definition="browseStemsDef" flush="false" />
</c:set>

<div class="section">
    <grouper:subtitle key="groups.heading.browse">
      <tiles:insert definition="flattenDef" flush="false"/>
    </grouper:subtitle>
<div class="sectionBody">
  <c:out value="${browseStemsHtml}"  escapeXml="false"/>
</div>
</div>

<tiles:insert definition="simpleSearchStemsDef"/>

<tiles:insert definition="stemLinksDef"/>
</c:when>
<c:otherwise>

<tiles:insert definition="advancedSearchStemsDef"/>
</c:otherwise>
</c:choose> 




