<%-- @annotation@
		  Displays search results when searching for groups in all modes 
		  except 'Find' mode 
--%><%--
  @author Gary Brown.
  @version $Id: SearchGroupResults.jsp,v 1.3 2006-02-24 13:44:21 isgwb Exp $
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
			<span class="termIn"><fmt:message bundle="${nav}" key="find.results.search-in"/></span> 
				<span class="termField"><c:out value="${outTerm}" escapeXml="false"/></span></c:set>
			<c:set var="termMode" value="andOrNot"/>
		</c:when>
		<c:otherwise>
			<c:set var="termAndOrNotKey" value="find.search.${outTerm}"/>
			<c:set var="termText"><c:out value="${termText}" escapeXml="false"/> 
				<span class="termAndOrNot"><fmt:message bundle="${nav}" key="${termAndOrNotKey}"/></span></c:set>	
			<c:set var="termMode" value="query"/>
		</c:otherwise>
	</c:choose>
</c:forEach>
<div class="searchedFor"><fmt:message bundle="${nav}" key="find.groups.searched-for">
	<fmt:param value="${termText}"/>
</fmt:message></div>
<c:if test="${!empty pager.params.searchFromDisplay}">
<div class="searchedFrom"><fmt:message bundle="${nav}" key="find.groups.searched-from">
	<fmt:param value="${pager.params.searchFromDisplay}"/>
</fmt:message></div>
</c:if>
<tiles:insert definition="dynamicTileDef">
	<tiles:put name="viewObject" beanName="pager" beanProperty="collection"/>
	<tiles:put name="view" value="searchResult"/>
	<tiles:put name="headerView" value="searchResultHeader"/>
	<tiles:put name="itemView" value="groupSearchResultLink"/>
	<tiles:put name="footerView" value="searchResultFooter"/>
	<tiles:put name="pager" beanName="pager"/>
	<tiles:put name="listInstruction" value="list.instructions.search-result-group"/>
</tiles:insert>

<c:if test="${pager.count==0}">
<div class="searchCountZero"><fmt:message bundle="${nav}" key="find.groups.no-results"/></div>
</c:if>
<div class="linkButton">
<tiles:insert definition="callerPageButtonDef">
	<tiles:put name="buttonTitle"><fmt:message bundle="${nav}" key="find.results.search-again"/></tiles:put>
	<tiles:put name="buttonText"><fmt:message bundle="${nav}" key="find.results.search-again"/></tiles:put>
</tiles:insert>
<c:if test="${grouperForm.map.advSearch=='Y'}">
<a href="populate<c:out value="${browseMode}"/>Groups.do?advancedSearch=false"><fmt:message bundle="${nav}" key="find.action.cancel-advanced-search"/></a>
</c:if>
</div>
