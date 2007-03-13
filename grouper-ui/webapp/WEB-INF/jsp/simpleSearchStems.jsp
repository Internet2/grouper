<%-- @annotation@
		Tile which displays the simple search form for stems
--%><%--
  @author Gary Brown.
  @version $Id: simpleSearchStems.jsp,v 1.7 2007-03-13 17:26:37 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<a href="<c:out value="${pageUrl}"/>#endSearch" class="noCSSOnly"><fmt:message bundle="${nav}" key="page.skip.search"/></a>
<div class="searchGroups">
	<h2 class="actionheader">
		<fmt:message bundle="${nav}" key="stems.heading.search"/>
	</h2>
	<p><a href="<c:out value="${pageUrlMinusQueryString}"/>?advancedSearch=true"><fmt:message bundle="${nav}" key="find.action.select.stems-advanced-search"/></a></p>
	
    <html:form action="/searchStems" styleId="SearchFormBean" method="post">
		<html:hidden property="searchInNameOrExtension"/>
		<html:hidden property="searchInDisplayNameOrExtension"/>
		<input type="hidden" name="callerPageId" value="<c:out value="${thisPageId}"/>"/>
	<fieldset>
    	<label for="searchTerm" class="noCSSOnly"><fmt:message bundle="${nav}" key="find.search-term"/></label><html:text property="searchTerm" size="25" styleId="searchTerm"/><br/>
		<tiles:insert definition="searchFromDef"/><br/>
		<tiles:insert definition="searchStemResultFieldChoiceDef"/>
    	<html:submit property="submit.search" value="${navMap['stems.action.search']}"/>
	</fieldset>
	</html:form>
</div> 
<a name="endSearch" id="endSearch"></a>
</grouper:recordTile>