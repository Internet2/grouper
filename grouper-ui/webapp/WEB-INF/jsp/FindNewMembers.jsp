<%-- @annotation@ 
			Browse tree / search screen for finding members 
--%><%--
  @author Gary Brown.
  @version $Id: FindNewMembers.jsp,v 1.1.1.1 2005-08-23 13:04:20 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>



<c:choose>
	<c:when test="${!isAdvancedSearch}">
	<h2 class="actionheader">
	<fmt:message bundle="${nav}" key="find.heading.browse"/>
</h2>
<tiles:insert definition="browseStemsFindDef"/>
<tiles:insert definition="simpleSearchDef"/>

<div class="linkButton">
<c:if test="${forStems}">
<html:link page="/cancelFindNewMembers.do" paramId="currentNode" paramName="findForNode">
	<fmt:message bundle="${nav}" key="find.for-stems.cancel"/>
</html:link></c:if>
<c:if test="${!forStems}">
<html:link page="/cancelFindNewMembers.do" paramId="groupId" paramName="findForNode"  >
	<fmt:message bundle="${nav}" key="find.for-groups.cancel"/>
</html:link></c:if>

</div>
</c:when>
<c:otherwise>

<tiles:insert definition="advancedSearchGroupsDef"/>
</c:otherwise>
</c:choose> 

