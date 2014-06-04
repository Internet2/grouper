<%-- @annotation@
		  Display paged results for Subject search
--%><%--
  @author Gary Brown.
  @version $Id: SubjectSearchResults.jsp,v 1.6 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%><tiles:importAttribute />
<%-- adding this in so that we have css handle on list --%>
<div class="subjectSearchResults section">
<div class="sectionBody">
<div class="searchedFor"><grouper:message key="find.subjects.searched-for">
	<grouper:param value="${pager.params.searchTerm}"/>
</grouper:message></div>

<c:if test="${empty groupSearchResultField}"><c:set scope="request" var="groupSearchResultField" value="${mediaMap['search.group.result-field']}"/></c:if>

	<tiles:insert definition="dynamicTileDef">
		<tiles:put name="viewObject" beanName="pager" beanProperty="collection"/>
		<tiles:put name="view" value="subjectSearchResults"/>
		<tiles:put name="headerView" value="genericListHeader"/>
		<tiles:put name="itemView" value="subjectSearchResultLink"/>
		
		<tiles:put name="footerView" value="genericListFooter"/>
		<tiles:put name="pager" beanName="pager"/>
		<tiles:put name="listInstruction" value="list.instructions.search-result-subject"/>
		<tiles:put name="noResultsMsg" value="${navMap['find.subjects.no-results']}"/>
	</tiles:insert>
<tiles:insert definition="subjectLinksDef"/>
</div>
</div>