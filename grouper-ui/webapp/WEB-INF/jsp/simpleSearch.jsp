<%-- @annotation@
		Tile which displays the simple search form for people and groups
--%><%--
  @author Gary Brown.
  @version $Id: simpleSearch.jsp,v 1.1.1.1 2005-08-23 13:04:20 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<a href="<c:out value="${pageUrl}"/>#endSearch" class="noCSSOnly"><fmt:message bundle="${nav}" key="page.skip.search"/></a>
<div class="searchGroups">
<h2 class="actionheader">
	<fmt:message bundle="${nav}" key="find.heading.search"/>
</h2>
<p><a href="<c:out value="${pageUrlMinusQueryString}"/>?advancedSearch=true"><fmt:message bundle="${nav}" key="find.action.select.groups-advanced-search"/></a></p>

 <html:form styleId="FindMembersForm" action="/searchNewMembers">
 		<html:hidden property="searchInNameOrExtension"/>
		<html:hidden property="searchInDisplayNameOrExtension"/>
<fieldset>
<c:if test="${forStems}">
    <html:hidden property="stemId"/>
</c:if>
<c:if test="${!forStems}">
    <html:hidden property="groupId"/>
</c:if>
<input type="hidden" name="stems" value="<c:out value="${forStems}"/>"/>
<div class="formRow">
	<div class="formLeft">
	<label for="searchTerm"><fmt:message bundle="${nav}" key="find.search-term"/></label>
	</div>
	<div class="formRight">
	<input name="searchTerm" type="text" id="searchTerm"/>
	</div>
</div>


<div class="formRow">
	<div class="formLeft">
	<fmt:message bundle="${nav}" key="find.search-for"/>
	</div>
	<div class="formRight">
	<c:choose>
		<c:when test="${personSourcesSize > 0}">
		<input type="radio" name="searchFor" checked="checked" value="people" id="searchForPeople"/><label for="searchForPeople"><fmt:message bundle="${nav}" key="find.people"/></label><br/>
		<c:choose>
			<c:when test="${personSourcesSize == 1}">
			<input type="hidden" name="personSource" value="<c:out value="${personSources[0].id}"/>"/>
			</c:when>
			<c:otherwise>
			<label for="personSource"><fmt:message bundle="${nav}" key="find.select-person-source"/></label>
			<select name="personSource">
				<c:forEach var="source" items="${personSources}">
					<option value="<c:out value="${source.id}"/>"><c:out value="${source.name}"/></option>
				</c:forEach>
			</select><br/>
			</c:otherwise>
		</c:choose>
		
		
		<input type="radio" name="searchFor" value="groups" id="searchForGroups"/><label for="searchForGroups"><fmt:message bundle="${nav}" key="find.groups"/></label> 
		</c:when>
		<c:otherwise>
			<input type="radio" name="searchFor" checked="checked" value="groups" id="searchForGroups"/><label for="searchForGroups"><fmt:message bundle="${nav}" key="find.groups"/></label> 

		</c:otherwise>
	</c:choose>
	</div>
</div>


<c:if test="${!empty browsePath}">

<tiles:insert definition="searchFromDef"/>

</c:if>


<p><html:submit property="submit.group.member" value="${navMap['find.action.search']}"/></p>
<input type="hidden" name="newSearch" value="Y"/>
</fieldset>
</html:form>
</div>
<a name="endSearch" id="endSearch"></a>
</grouper:recordTile>