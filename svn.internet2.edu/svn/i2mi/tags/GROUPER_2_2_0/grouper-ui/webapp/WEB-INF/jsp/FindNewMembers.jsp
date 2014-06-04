<%-- @annotation@ 
			Browse tree / search screen for finding members 
--%><%--
  @author Gary Brown.
  @version $Id: FindNewMembers.jsp,v 1.7 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>

<div class="section">

<c:choose>
	<c:when test="${!isAdvancedSearch}">
  <grouper:subtitle key="find.heading.browse" >
<c:if test="${!empty savedSubjectsSize && savedSubjectsSize>0}">
		<html:link styleClass="underline subtitleLink" page="/assignSavedSubjects.do" paramId="groupId" paramName="findForNode"  >
			<grouper:message key="saved-subjects.add-new-members"/>
		</html:link>
</c:if>
</grouper:subtitle>
<div class="sectionBody">
<tiles:insert definition="browseStemsFindDef"/>

</div>
</div>

<tiles:insert definition="simpleSearchDef"/>

<div class="section">
<div class="sectionBody">
<div class="linkButton">
<c:if test="${forStems}">
<html:link page="/cancelFindNewMembers.do" paramId="currentNode" paramName="findForNode">
	<grouper:message key="find.for-stems.cancel"/>
</html:link></c:if>
<c:if test="${!forStems}">
<html:link page="/cancelFindNewMembers.do" paramId="groupId" paramName="findForNode"  >
	<grouper:message key="find.for-groups.cancel"/>
</html:link></c:if>
</div>
</div>
</div>
</c:when>
<c:otherwise>

<tiles:insert definition="advancedSearchGroupsDef"/>
</c:otherwise>
</c:choose> 

