<%-- @annotation@
		Tile which displays the simple search form for stems
--%><%--
  @author Gary Brown.
  @version $Id: simpleSearchStems.jsp,v 1.12 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<a href="<c:out value="${pageUrl}"/>#endSearch" class="noCSSOnly"><grouper:message key="page.skip.search"/></a>
<div class="searchGroups section">
<grouper:subtitle key="stems.heading.search">
	<a class="subtitleLink underlined" href="<c:out value="${pageUrlMinusQueryString}"/>?advancedSearch=true"><grouper:message key="find.action.select.stems-advanced-search"/></a>
</grouper:subtitle>
<div class="sectionBody">	
    <html:form action="/searchStems" styleId="SearchFormBean" method="post">
    <html:hidden property="searchInNameOrExtension"/>
    <html:hidden property="searchInDisplayNameOrExtension"/>
    <input type="hidden" name="callerPageId" value="<c:out value="${thisPageId}"/>"/>
  <fieldset>
<table class="formTable formTableSpaced">

  <tr class="formTableRow">
  <td class="formTableLeft">
    	<label for="searchTerm" class="noCSSOnly"><grouper:message key="find.search-term"/></label>
      <html:text property="searchTerm" size="25" styleId="searchTerm"/>
    </td>
    <td class="formTableRight">
          <html:submit styleClass="blueButton" property="submit.search" value="${navMap['stems.action.search']}"/>
    </td>
  </tr>
		<tiles:insert definition="searchFromDef"/>
		<tiles:insert definition="searchStemResultFieldChoiceDef"/>
    </table>
	</fieldset>
	</html:form>
  </div>
</div> 
<a name="endSearch" id="endSearch"></a>
</grouper:recordTile>