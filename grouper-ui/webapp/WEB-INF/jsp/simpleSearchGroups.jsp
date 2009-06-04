<%-- @annotation@
		Tile which displays the simple search form for groups
--%><%--
  @author Gary Brown.
  @version $Id: simpleSearchGroups.jsp,v 1.11 2008-04-10 19:50:25 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<div class="section searchGroups">
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<a href="<c:out value="${pageUrl}"/>#endSearch" class="noCSSOnly"><grouper:message bundle="${nav}" key="page.skip.search"/></a>
<grouper:subtitle key="groups.heading.search">
  <a class="underline subtitleLink" href="<c:out value="${pageUrlMinusQueryString}"/>?advancedSearch=true"><grouper:message 
  bundle="${nav}" key="find.action.select.groups-advanced-search"/></a>
</grouper:subtitle>	
<div class="sectionBody">
	<html:form styleId="SearchFormBean" action="/searchGroups${browseMode}" method="post">
		<html:hidden property="searchInNameOrExtension"/>
		<html:hidden property="searchInDisplayNameOrExtension"/>
		<input type="hidden" name="callerPageId" value="<c:out value="${thisPageId}"/>"/>
	<fieldset>
    <table class="formTable">
      <tr class="formTableRow">
        <td class="formTableLeft">
		      <label for="searchTerm" class="noCSSOnly"><grouper:message bundle="${nav}" key="find.search-term"/></label>
          <html:text property="searchTerm" size="25" styleId="searchTerm"/>
        </td>
        <td class="formTableRight">
          <html:submit styleClass="blueButton" property="submit.search" value="${navMap['groups.action.search']}"/>
        </td>
      </tr>
  		<c:if test="${mediaMap['search.default.any']=='true'}">
        <tr class="formTableRow">
  		    <td class="formTableLeft">
            <grouper:message bundle="${nav}" key="find.groups.search-in"/> 
          </td>
          <td class="formTableRight">
            <html:radio property="searchIn" value="name"/> 
        		<grouper:message bundle="${nav}" key="find.groups.search-in.name"/> 
  		      &nbsp;
            <html:radio property="searchIn" value="any"/> 
            <grouper:message bundle="${nav}" key="find.groups.search-in.any"/> 
          </td>
        </tr>
  		</c:if>
		<tiles:insert definition="searchFromDef"/>
		<tiles:insert definition="searchGroupResultFieldChoiceDef"/>
		</table>
	</fieldset>
	</html:form>
</div>
<a name="endSearch" id="endSearch"></a>
</grouper:recordTile>
</div>