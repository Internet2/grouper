<%-- @annotation@
		Tile which displays the advanced search form for stems
--%><%--
  @author Gary Brown.
  @version $Id: advancedSearchStems.jsp,v 1.8 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">

<div class="section">
<a href="<c:out value="${pageUrl}"/>#endSearch" class="noCSSOnly"><grouper:message key="page.skip.search"/></a>
<grouper:subtitle key="find.heading.stems-advanced-search" />
<div class="sectionBody">
<p><a href="<c:out value="${pageUrlMinusQueryString}"/>?advancedSearch=false"><grouper:message key="find.action.cancel-advanced-search"/></a></p>

		<c:set var="submitAction" value="/searchStems"/>

	<html:form styleId="SearchFormBean" action="${submitAction}" method="post">
	<input type="hidden" name="searchFor" value="stems"/>
	<input type="hidden" name="newSearch" value="Y"/>
	<input type="hidden" name="advSearch" value="Y"/>
	
	<input type="hidden" name="callerPageId" value="<c:out value="${thisPageId}"/>"/>
	<input type="hidden" name="previousCallerPageId" value="<c:out value="${grouperForm.map.callerPageId}"/>"/>
	<fieldset>			
    <table class="formTable">
		<fieldset class="nested">
				<tiles:insert definition="selectStemSearchFieldsDef"/>
		</fieldset>
		<tiles:insert definition="searchFromDef"/>
    	

		<tiles:insert definition="searchStemResultFieldChoiceDef"/>
    </table>
		<html:submit styleClass="blueButton" property="submit.search" value="${navMap['stems.action.search']}"/>
   
	</fieldset>
	</html:form>
</div>
<a name="endSearch" id="endSearch"></a>
</div>
</grouper:recordTile>