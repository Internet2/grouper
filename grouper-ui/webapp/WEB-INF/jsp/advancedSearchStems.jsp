<%-- @annotation@
		Tile which displays the advanced search form for stems
--%><%--
  @author Gary Brown.
  @version $Id: advancedSearchStems.jsp,v 1.3 2007-03-13 17:26:37 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">

<a href="<c:out value="${pageUrl}"/>#endSearch" class="noCSSOnly"><fmt:message bundle="${nav}" key="page.skip.search"/></a>
<div class="advancedSearchStems">
<h2 class="actionheader">
	<fmt:message bundle="${nav}" key="find.heading.stems-advanced-search"/>
</h2><p><a href="<c:out value="${pageUrlMinusQueryString}"/>?advancedSearch=false"><fmt:message bundle="${nav}" key="find.action.cancel-advanced-search"/></a></p>

		<c:set var="submitAction" value="/searchStems"/>

	<html:form styleId="SearchFormBean" action="${submitAction}" method="post">
	<input type="hidden" name="searchFor" value="stems"/>
	<input type="hidden" name="newSearch" value="Y"/>
	<input type="hidden" name="advSearch" value="Y"/>
	
	<input type="hidden" name="callerPageId" value="<c:out value="${thisPageId}"/>"/>
	<input type="hidden" name="previousCallerPageId" value="<c:out value="${grouperForm.map.callerPageId}"/>"/>
	<fieldset>			
		<fieldset class="nested">
				<tiles:insert definition="selectStemSearchFieldsDef"/>
		</fieldset>
		<div class="formRow"><tiles:insert definition="searchFromDef"/></div><br/>
    	

		<tiles:insert definition="searchStemResultFieldChoiceDef"/>
		<html:submit property="submit.search" value="${navMap['stems.action.search']}"/>

	</fieldset>
	</html:form>
</div>
<a name="endSearch" id="endSearch"></a>

</grouper:recordTile>