<%-- @annotation@ Top level JSP which displays a list of saved Subjects --%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<fmt:message bundle="${nav}" key="saved-subjects.intro"/>
<div class="savedSubjectsList">
<c:if test="${savedSubjectsSize==0}">
<fmt:message bundle="${nav}" key="saved-subjects.none"/>
</c:if>

<c:if test="${savedSubjectsSize>0}">
	
	<html:form action="/removeSavedSubjects.do">
	<ul>
	<c:forEach var="subject" items="${savedSubjects}">
		<li><input name="subjectIds" type="checkbox" value="<c:out value="${subject.id}"/>"/>
		<tiles:insert definition="dynamicTileDef" flush="false">
		  <tiles:put name="viewObject" beanName="subject"/>
		  <tiles:put name="view" value="savedSubject"/>
	  </tiles:insert> 
		</li>
	</c:forEach>
	</ul>
		<html:submit property="x" value="${navMap['saved-subjects.remove-selected']}"/> 
	</html:form>
</c:if>
</div>



