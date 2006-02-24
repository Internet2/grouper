<%-- @annotation@ 
		  Main page for the 'Join' browse mode
--%><%--
  @author Gary Brown.
  @version $Id: JoinGroups.jsp,v 1.2 2005-09-13 12:18:29 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<c:choose>
	<c:when test="${!isAdvancedSearch}">
<div class="pageBlurb">
	<fmt:message bundle="${nav}" key="groups.join.can"/>
</div>
<h2 class="actionheader">
	<fmt:message bundle="${nav}" key="groups.heading.browse"/>
</h2>
<tiles:insert definition="browseStemsDef"/>
<tiles:insert definition="flattenDef">
	<tiles:put name="pageName" value="populateJoinGroups"/>
</tiles:insert>

<tiles:insert definition="simpleSearchGroupsDef"/>
</c:when>
<c:otherwise>

<tiles:insert definition="advancedSearchGroupsDef"/>
</c:otherwise>
</c:choose> 
