<%-- @annotation@
		Tile which displays the simple search form for people and groups
--%><%--
  @author Gary Brown.
  @version $Id: simpleSearch.jsp,v 1.2 2005-11-08 16:19:56 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<a href="<c:out value="${pageUrl}"/>#endSearch" class="noCSSOnly"><fmt:message bundle="${nav}" key="page.skip.search"/></a>
<div class="searchGroups">
<h2 class="actionheader">
	<fmt:message bundle="${nav}" key="find.heading.search"/>
</h2>
<p><a href="<c:out value="${pageUrlMinusQueryString}"/>?advancedSearch=true"><fmt:message bundle="${nav}" key="find.action.select.groups-advanced-search"/></a></p>

 <html:form styleId="SearchFormBean" action="/searchNewMembers">
 		<html:hidden property="searchInNameOrExtension"/>
		<html:hidden property="searchInDisplayNameOrExtension"/>
		<input type="hidden" name="callerPageId" value="<c:out value="${thisPageId}"/>"/>
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

<tiles:insert definition="subjectSearchFragmentDef">
<c:if test="${!empty browsePath}">
	<tiles:put name="groupInsert" value="searchFromDef"/>
</c:if>
</tiles:insert>





<div class="formRow"><html:submit property="submit.group.member" value="${navMap['find.action.search']}"/></div>
<input type="hidden" name="newSearch" value="Y"/>
</fieldset>
</html:form>
</div>
<a name="endSearch" id="endSearch"></a>
</grouper:recordTile>