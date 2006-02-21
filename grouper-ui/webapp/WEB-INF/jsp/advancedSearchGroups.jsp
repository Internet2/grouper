<%-- @annotation@
		Tile which displays the advanced search form for groups
--%><%--
  @author Gary Brown.
  @version $Id: advancedSearchGroups.jsp,v 1.3 2006-02-21 16:15:21 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<a href="<c:out value="${pageUrl}"/>#endSearch" class="noCSSOnly"><fmt:message bundle="${nav}" key="page.skip.search"/></a>
<div class="advancedSearchGroups">
<h2 class="actionheader">
	<fmt:message bundle="${nav}" key="find.heading.groups-advanced-search"/>
</h2><p><a href="<c:out value="${pageUrlMinusQueryString}"/>?advancedSearch=false"><fmt:message bundle="${nav}" key="find.action.cancel-advanced-search"/></a></p>
<c:choose>
	<c:when test="${!empty findForNode}">
		<c:set var="submitAction" value="/searchNewMembers"/>
	</c:when>
	<c:otherwise>
		<c:set var="submitAction" value="/searchGroups${browseMode}"/>
	</c:otherwise>
</c:choose>
	<html:form styleId="SearchFormBean" action="${submitAction}">
	<input type="hidden" name="searchFor" value="groups"/>
	<input type="hidden" name="newSearch" value="Y"/>
	<input type="hidden" name="advSearch" value="Y"/>
	<input type="hidden" name="subjectSource" value="g:gsa"/>
	<fieldset>			
		<fieldset class="nested">
				<tiles:insert definition="selectGroupSearchFieldsDef"/>
		</fieldset>
		<div class="formRow"><tiles:insert definition="searchFromDef"/></div><br/>
		<tiles:insert definition="searchGroupResultFieldChoiceDef"/>
		<html:submit property="submit.search" value="${navMap['groups.action.search']}"/>
	</fieldset>
	</html:form>
</div>
<a name="endSearch" id="endSearch"></a>
</grouper:recordTile>