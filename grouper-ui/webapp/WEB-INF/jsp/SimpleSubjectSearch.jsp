<%-- @annotation@
		Tile which displays the simple subject search form  - allows any configured source to be selected
--%><%--
  @author Gary Brown.
  @version $Id: SimpleSubjectSearch.jsp,v 1.2 2005-12-08 15:33:28 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">

<div class="searchSubjects">
<h2 class="actionheader">
	<fmt:message bundle="${nav}" key="find.heading.search"/>
</h2>
<c:if test="${mediaMap['allow.self-subject-summary'] == 'true'}">
<div class="subjectAsSelfLink"><html:link page="/populateSubjectSummary.do" name="AuthSubject">
					<fmt:message bundle="${nav}" key="subject.view.yourself"/>
		</html:link></div>
</c:if>
<!--<p><a href="<c:out value="${pageUrlMinusQueryString}"/>?advancedSearch=true"><fmt:message bundle="${nav}" key="find.action.select.groups-advanced-search"/></a></p>
-->
 <html:form styleId="SearchFormBean" action="/doSearchSubjects">
 		<html:hidden property="searchInNameOrExtension"/>
		<html:hidden property="searchInDisplayNameOrExtension"/>
<fieldset>


<div class="formRow">
	<div class="formLeft">
	<label for="searchTerm"><fmt:message bundle="${nav}" key="find.search-term"/></label>
	</div>
	<div class="formRight">
	<input name="searchTerm" type="text" id="searchTerm"/>
	</div>
</div>

<tiles:insert definition="subjectSearchFragmentDef"/>


<div style="clear:left"><html:submit property="submit.group.member" value="${navMap['find.action.search']}"/></div>
<input type="hidden" name="newSearch" value="Y"/>
</fieldset>
</html:form>
</div>
<a name="endSearch" id="endSearch"></a>
</grouper:recordTile>