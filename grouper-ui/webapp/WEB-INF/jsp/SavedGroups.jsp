<%-- @annotation@ Top level JSP which displays a list of saved groups --%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<fmt:message bundle="${nav}" key="saved-subjects.groups.intro"/>
<div class="savedGroupsList">
<c:if test="${savedSubjectsSize==0}">
<fmt:message bundle="${nav}" key="saved-subjects.groups.none"/>
</c:if>

<c:if test="${savedSubjectsSize>0}">
	<html:form action="/populateListSavedGroups.do">
	<tiles:insert definition="searchGroupResultFieldChoiceDef"/>
	<c:if test="${!empty groupSearchResultField && !empty mediaMap['search.group.result-field']}">
		<html:submit property="x" value="${navMap['saved-subjects.groups.change-field']}"/>
	</c:if>
	</html:form>
	<html:form action="/removeSavedGroups.do">
	<ul>
	<c:forEach var="group" items="${savedSubjects}">
		<li><input name="subjectIds" type="checkbox" value="<c:out value="${group.id}"/>"/>
		<tiles:insert definition="dynamicTileDef" flush="false">
		  <tiles:put name="viewObject" beanName="group"/>
		  <tiles:put name="view" value="savedGroup"/>
	  </tiles:insert> 
		</li>
	</c:forEach>
	</ul>
		<html:submit property="x" value="${navMap['saved-subjects.groups.remove-selected']}"/> 
	</html:form>
</c:if>
</div>



