<%-- @annotation@
		  Displays search results when searching for stems
--%><%--
  @author Gary Brown.
  @version $Id: SearchStemResults.jsp,v 1.1.1.1 2005-08-23 13:04:20 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<div class="searchedFor"><fmt:message bundle="${nav}" key="find.stems.searched-for">
	<fmt:param value="${pager.params.searchTerm}"/>
</fmt:message></div>
<c:if test="${!empty pager.params.searchFromDisplay}">
<div class="searchedFrom"><fmt:message bundle="${nav}" key="find.stems.searched-from">
	<fmt:param value="${pager.params.searchFromDisplay}"/>
</fmt:message></div>
</c:if>
<tiles:insert definition="dynamicTileDef">
	<tiles:put name="viewObject" beanName="pager" beanProperty="collection"/>
	<tiles:put name="view" value="searchResult"/>
	<tiles:put name="headerView" value="searchResultHeader"/>
	<tiles:put name="itemView" value="stemSearchResultLink"/>
	<tiles:put name="footerView" value="searchResultFooter"/>
	<tiles:put name="pager" beanName="pager"/>
	<tiles:put name="listInstruction" value="list.instructions.search-result-group"/>
</tiles:insert>

<c:if test="${pager.count==0}">
<div class="searchCountZero"><fmt:message bundle="${nav}" key="find.stems.no-results"/></div>
</c:if>
