<%-- @annotation@ 
			Browse tree / search screen for finding members 
--%><%--
  @author Gary Brown.
  @version $Id: FindNewMembers.jsp,v 1.4 2008-04-03 07:48:21 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>



<c:choose>
	<c:when test="${!isAdvancedSearch}">
  <grouper:subtitle key="find.heading.browse" />
<c:if test="${!empty savedSubjectsSize && savedSubjectsSize>0}">
	<div class="linkButton">
		<html:link page="/assignSavedSubjects.do" paramId="groupId" paramName="findForNode"  >
			<grouper:message bundle="${nav}" key="saved-subjects.add-new-members"/>
		</html:link>
	</div>
</c:if>
<tiles:insert definition="browseStemsFindDef"/>
<tiles:insert definition="simpleSearchDef"/>

<div class="linkButton">
<c:if test="${forStems}">
<html:link page="/cancelFindNewMembers.do" paramId="currentNode" paramName="findForNode">
	<grouper:message bundle="${nav}" key="find.for-stems.cancel"/>
</html:link></c:if>
<c:if test="${!forStems}">
<html:link page="/cancelFindNewMembers.do" paramId="groupId" paramName="findForNode"  >
	<grouper:message bundle="${nav}" key="find.for-groups.cancel"/>
</html:link></c:if>

</div>
</c:when>
<c:otherwise>

<tiles:insert definition="advancedSearchGroupsDef"/>
</c:otherwise>
</c:choose> 

