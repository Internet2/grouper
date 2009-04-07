<%-- @annotation@
		  Displays search results when searching for stems
--%><%--
  @author Gary Brown.
  @version $Id: SearchStemResults.jsp,v 1.4 2008-03-25 14:59:51 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<c:set var="termMode" value="query"/>
<c:set var="termText" value=""/>
<c:forEach var="outTerm" items="${queryOutTerms}">
	<c:choose>
		<c:when test="${termMode == 'query'}">
			<c:set var="termText"><c:out value="${termText}" escapeXml="false"/> <span class="termQuery"><c:out value="${outTerm}" escapeXml="false"/></span></c:set>
			<c:set var="termMode" value="field"/>
		</c:when>
		<c:when test="${termMode == 'field'}">
			<c:set var="termText"><c:out value="${termText}" escapeXml="false"/> 
			<span class="termIn"><grouper:message bundle="${nav}" key="find.results.search-in"/></span> 
				<span class="termField"><c:out value="${outTerm}" escapeXml="false"/></span></c:set>
			<c:set var="termMode" value="andOrNot"/>
		</c:when>
		<c:otherwise>
			<c:set var="termAndOrNotKey" value="find.search.${outTerm}"/>
			<c:set var="termText"><c:out value="${termText}" escapeXml="false"/> 
				<span class="termAndOrNot"><grouper:message bundle="${nav}" key="${termAndOrNotKey}"/></span></c:set>	
			<c:set var="termMode" value="query"/>
		</c:otherwise>
	</c:choose>
</c:forEach>
<div class="searchedFor"><grouper:message bundle="${nav}" key="find.groups.searched-for">
	<grouper:param value="${termText}"/>
</grouper:message></div>
<c:if test="${!empty pager.params.searchFromDisplay}">
<div class="searchedFrom"><grouper:message bundle="${nav}" key="find.stems.searched-from">
	<grouper:param value="${pager.params.searchFromDisplay}"/>
</grouper:message></div>
</c:if>
<tiles:insert definition="dynamicTileDef">
	<tiles:put name="viewObject" beanName="pager" beanProperty="collection"/>
	<tiles:put name="view" value="searchResult"/>
	<tiles:put name="headerView" value="searchResultHeader"/>
	<tiles:put name="itemView" value="stemSearchResultLink"/>
	<tiles:put name="footerView" value="searchResultFooter"/>
	<tiles:put name="pager" beanName="pager"/>
	<tiles:put name="listInstruction" value="list.instructions.search-result-stem"/>
</tiles:insert>

<c:if test="${pager.count==0}">
<div class="searchCountZero"><grouper:message bundle="${nav}" key="find.stems.no-results"/></div>
</c:if>
<div class="linkButton">
<tiles:insert definition="callerPageButtonDef"/>
</div>
