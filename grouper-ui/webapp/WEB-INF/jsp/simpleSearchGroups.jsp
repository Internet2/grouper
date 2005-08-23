<%-- @annotation@
		Tile which displays the simple search form for groups
--%><%--
  @author Gary Brown.
  @version $Id: simpleSearchGroups.jsp,v 1.1.1.1 2005-08-23 13:04:20 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<a href="<c:out value="${pageUrl}"/>#endSearch" class="noCSSOnly"><fmt:message bundle="${nav}" key="page.skip.search"/></a>
<div class="searchGroups">
	<h2 class="actionheader">
		<fmt:message bundle="${nav}" key="groups.heading.search"/>
	</h2>
	<p><a href="<c:out value="${pageUrlMinusQueryString}"/>?advancedSearch=true"><fmt:message bundle="${nav}" key="find.action.select.groups-advanced-search"/></a></p>
	<html:form styleId="SearchGroupsForm" action="/searchGroups${browseMode}">
		<html:hidden property="searchInNameOrExtension"/>
		<html:hidden property="searchInDisplayNameOrExtension"/>
	<fieldset>
		<label for="searchTerm" class="noCSSOnly"><fmt:message bundle="${nav}" key="find.search-term"/></label><html:text property="searchTerm" size="25" styleId="searchTerm"/><br/>
		<tiles:insert definition="searchFromDef"/><br/>
		<html:submit property="submit.search" value="${navMap['groups.action.search']}"/>
	</fieldset>
	</html:form>
</div>
<a name="endSearch" id="endSearch"></a>
</grouper:recordTile>