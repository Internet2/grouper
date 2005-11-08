<%-- @annotation@
		  Display paged results for Subject search
--%><%--
  @author Gary Brown.
  @version $Id: SubjectSearchResults.jsp,v 1.1 2005-11-08 15:34:48 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%><tiles:importAttribute />
<div class="searchedFor"><fmt:message bundle="${nav}" key="find.subjects.searched-for">
	<fmt:param value="${pager.params.searchTerm}"/>
</fmt:message></div>
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

