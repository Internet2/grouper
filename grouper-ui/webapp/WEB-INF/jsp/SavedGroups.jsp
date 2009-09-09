<%-- @annotation@ Top level JSP which displays a list of saved groups --%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:message key="saved-subjects.groups.intro"/>
<div class="savedGroupsList section">
<div class="sectionBody">
<c:if test="${savedSubjectsSize==0}">
<grouper:message key="saved-subjects.groups.none"/>
</c:if>

<c:if test="${savedSubjectsSize>0}">
	<html:form action="/populateListSavedGroups.do" method="post">
  <table class="formTable formTableSpaced">
	<tiles:insert definition="searchGroupResultFieldChoiceDef"/>
  <c:if test="${!empty groupSearchResultField && !empty mediaMap['search.group.result-field']}">
    <tr class="formTableRow">
    <td class="formTableLeft">&nbsp;</td>
    <td class="formTableRight">
    <html:submit styleClass="blueButton" property="x" value="${navMap['saved-subjects.groups.change-field']}"/>
    </td>
    </tr>
  </c:if>
  </table>
	</html:form>
	<html:form action="/removeSavedGroups.do" method="post">
	<ul class="savedGroups">
	<c:forEach var="group" items="${savedSubjects}">
		<li><input name="subjectIds" type="checkbox" value="<c:out value="${group.id}"/>"/>
		<tiles:insert definition="dynamicTileDef" flush="false">
		  <tiles:put name="viewObject" beanName="group"/>
		  <tiles:put name="view" value="savedGroup"/>
	  </tiles:insert> 
		</li>
	</c:forEach>
	</ul>
		<html:submit styleClass="blueButton" property="x" value="${navMap['saved-subjects.groups.remove-selected']}"/> 
	</html:form>
</c:if>
</div>
</div>



