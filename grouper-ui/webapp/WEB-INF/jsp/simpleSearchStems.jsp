<%-- @annotation@
		Tile which displays the simple search form for stems
--%><%--
  @author Gary Brown.
  @version $Id: simpleSearchStems.jsp,v 1.10 2008-04-10 19:50:25 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<a href="<c:out value="${pageUrl}"/>#endSearch" class="noCSSOnly"><grouper:message bundle="${nav}" key="page.skip.search"/></a>
<div class="searchGroups">
<grouper:subtitle key="stems.heading.search" />
	<p><a href="<c:out value="${pageUrlMinusQueryString}"/>?advancedSearch=true"><grouper:message bundle="${nav}" key="find.action.select.stems-advanced-search"/></a></p>
	
    <html:form action="/searchStems" styleId="SearchFormBean" method="post">
		<html:hidden property="searchInNameOrExtension"/>
		<html:hidden property="searchInDisplayNameOrExtension"/>
		<input type="hidden" name="callerPageId" value="<c:out value="${thisPageId}"/>"/>
	<fieldset>
    	<label for="searchTerm" class="noCSSOnly"><grouper:message bundle="${nav}" key="find.search-term"/></label><html:text property="searchTerm" size="25" styleId="searchTerm"/><br/>
		<tiles:insert definition="searchFromDef"/><br/>
		<tiles:insert definition="searchStemResultFieldChoiceDef"/>
    	<html:submit styleClass="blueButton" property="submit.search" value="${navMap['stems.action.search']}"/>
	</fieldset>
	</html:form>
</div> 
<a name="endSearch" id="endSearch"></a>
</grouper:recordTile>