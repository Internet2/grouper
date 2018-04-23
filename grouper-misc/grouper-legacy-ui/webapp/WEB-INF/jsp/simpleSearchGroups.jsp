<%-- @annotation@
		Tile which displays the simple search form for groups
--%><%--
  @author Gary Brown.
  @version $Id: simpleSearchGroups.jsp,v 1.12 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<div class="section searchGroups">
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<a href="<c:out value="${pageUrl}"/>#endSearch" class="noCSSOnly"><grouper:message key="page.skip.search"/></a>
<grouper:subtitle key="groups.heading.search">
  <a class="underline subtitleLink" href="<c:out value="${pageUrlMinusQueryString}"/>?advancedSearch=true"><grouper:message 
  key="find.action.select.groups-advanced-search"/></a>
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
		      <label for="searchTerm" class="noCSSOnly"><grouper:message key="find.search-term"/></label>
          <html:text property="searchTerm" size="25" styleId="searchTerm"/>
        </td>
        <td class="formTableRight">
          <html:submit styleClass="blueButton" property="submit.search" value="${navMap['groups.action.search']}"/>
        </td>
      </tr>
  		<c:if test="${mediaMap['search.default.any']=='true'}">
        <tr class="formTableRow">
  		    <td class="formTableLeft">
            <grouper:message key="find.groups.search-in"/> 
          </td>
          <td class="formTableRight">
            <html:radio property="searchIn" value="name"/> 
        		<grouper:message key="find.groups.search-in.name"/> 
  		      &nbsp;
            <html:radio property="searchIn" value="any"/> 
            <grouper:message key="find.groups.search-in.any"/> 
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