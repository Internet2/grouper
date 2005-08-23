<%-- @annotation@ 
		  Main page for the 'Join' browse mode
--%><%--
  @author Gary Brown.
  @version $Id: JoinGroups.jsp,v 1.1.1.1 2005-08-23 13:04:20 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<c:choose>
	<c:when test="${!isAdvancedSearch}">
<div class="pageBlurb">
	<fmt:message bundle="${nav}" key="groups.join.can"/></html:link>
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
