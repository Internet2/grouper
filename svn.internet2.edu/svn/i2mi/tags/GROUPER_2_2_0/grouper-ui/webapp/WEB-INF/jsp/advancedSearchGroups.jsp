<%-- @annotation@
		Tile which displays the advanced search form for groups
--%><%--
  @author Gary Brown.
  @version $Id: advancedSearchGroups.jsp,v 1.14 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<a href="<c:out value="${pageUrl}"/>#endSearch" class="noCSSOnly"><grouper:message key="page.skip.search"/></a>
<div class="advancedSearchGroups section">
<c:if test="${!empty subjectOfInterest}">
	<div class="groupSearchSubject"><grouper:message key="subject.action.search-groups.info"/> 
	<tiles:insert definition="dynamicTileDef" flush="false">
	  			<tiles:put name="viewObject" beanName="subjectOfInterest"/>
	  			<tiles:put name="view" value="groupSearchForPrivileges"/>
  			</tiles:insert></div>
</c:if>
<grouper:subtitle key="find.heading.groups-advanced-search">
<a class="underline subtitleLink"
  href="<c:out value="${pageUrlMinusQueryString}"/>?advancedSearch=false"><grouper:message 
key="find.action.cancel-advanced-search"/></a>
</grouper:subtitle>

  <div class="sectionBody">

<c:choose>
	<c:when test="${!empty findForNode}">
		<c:set var="submitAction" value="/searchNewMembers"/>
	</c:when>
	<c:otherwise>
		<c:set var="submitAction" value="/searchGroups${browseMode}"/>
	</c:otherwise>
</c:choose>
	<html:form styleId="SearchFormBean" action="${submitAction}" method="post">
	<input type="hidden" name="searchFor" value="groups"/>
	<input type="hidden" name="newSearch" value="Y"/>
	<input type="hidden" name="advSearch" value="Y"/>
	<input type="hidden" name="subjectSource" value="g:gsa"/>
	<input type="hidden" name="callerPageId" value="<c:out value="${thisPageId}"/>"/>
	<input type="hidden" name="previousCallerPageId" value="<c:out value="${grouperForm.map.callerPageId}"/>"/>
	<fieldset>			
    <table class="formTable">
		<fieldset class="nested">
				<tiles:insert definition="selectGroupSearchFieldsDef"/>
		</fieldset>
		<c:if test="${typesSize>0}">
			<fieldset class="nested">
					<tiles:insert definition="selectGroupSearchTypesDef"/>
			</fieldset>
		</c:if>
		<tiles:insert definition="searchFromDef"/>
		<tiles:insert definition="searchGroupResultFieldChoiceDef"/>
    </table>
    <br /><br />
		<html:submit styleClass="blueButton" property="submit.search" value="${navMap['groups.action.search']}"/>
	</fieldset>
	</html:form>
  </div>
</div>
<a name="endSearch" id="endSearch"></a>
</grouper:recordTile>