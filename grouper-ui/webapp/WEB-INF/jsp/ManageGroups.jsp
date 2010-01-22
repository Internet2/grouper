<%-- @annotation@ 
			Main page for the 'Manage' browse mode
--%><%--
  @author Gary Brown.
  @version $Id: ManageGroups.jsp,v 1.6 2009-09-09 15:10:03 mchyzer Exp $
--%>

<%@include file="/WEB-INF/jsp/include.jsp"%>
<c:choose>
	<c:when test="${!isAdvancedSearch}">
<div class="pageBlurb">
	<grouper:message key="groups.manage.can"/>
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

<tiles:insert definition="simpleSearchGroupsDef"/>
<tiles:insert definition="stemLinksDef"/></c:when>
<c:otherwise>

<tiles:insert definition="advancedSearchGroupsDef"/>
</c:otherwise>
</c:choose> 