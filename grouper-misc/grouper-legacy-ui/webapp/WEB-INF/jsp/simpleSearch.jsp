<%-- @annotation@
		Tile which displays the simple search form for people and groups
--%><%--
  @author Gary Brown.
  @version $Id: simpleSearch.jsp,v 1.12 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<a href="<c:out value="${pageUrl}"/>#endSearch" class="noCSSOnly"><grouper:message key="page.skip.search"/></a>
<div class="section searchGroups">
<grouper:subtitle key="find.heading.search">
  <a class="underline subtitleLink" href="<c:out value="${pageUrlMinusQueryString}"/>?advancedSearch=true"
    ><grouper:message key="find.action.select.groups-advanced-search"/></a>
    
    <a href="#" onclick="return grouperHideShow(event, 'advancedSubjectSearch');" 
      class="underline subtitleLink"><grouper:message key="find.search.subjects.specifySource" ignoreTooltipStyle="true"/></a>
    
</grouper:subtitle>
<div class="sectionBody">
 <html:form styleId="SearchFormBean" action="/searchNewMembers" method="post">
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
<table class="formTable formTableSpaced">
<tr class="formTableRow">
<td class="formTableLeft">
	<label for="searchTerm" class="noCSSOnly"><grouper:message
          key="find.search-term"
        /></label><input name="searchTerm" type="text" id="searchTerm"/>
  </td>
<td class="formTableRight">
	<html:submit styleClass="blueButton" property="submit.group.member" value="${navMap['find.action.search']}"/>
    </td>
</tr>

<tiles:insert definition="subjectSearchFragmentDef">
<c:if test="${!empty browsePath}">
	<tiles:put name="groupInsert" value="searchFromDef"/>
</c:if>
</tiles:insert>
</table>





<input type="hidden" name="newSearch" value="Y"/>
</fieldset>
</html:form>
</div>
</div>
<a name="endSearch" id="endSearch"></a>
</grouper:recordTile>
