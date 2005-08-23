<%-- @annotation@
		  Page which shows a set of privileges and a list of Subjects. 
		  The privileges depend on whether the target is a group or a stem.
		  The list of Subjects may be the result of a search, in which case
		  the results will be paged. Alternatively, the subjects may have been 
		  selected whilst browsing, in which case the list will not need to be 
		  paged, however, all the subjects will be checked.
--%><%--
  @author Gary Brown.
  @version $Id: AssignNewMembers.jsp,v 1.1.1.1 2005-08-23 13:04:20 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%><tiles:importAttribute />
<tiles:insert definition="showStemsLocationDef"/>
<c:choose>
	<c:when test="${alreadyChecked}">
		<c:set var="checked" scope="request"> checked="checked"</c:set>
	</c:when>
	<c:otherwise>
		<c:set var="checked"></c:set>
	</c:otherwise>
</c:choose>


	<tiles:insert definition="dynamicTileDef">
		<tiles:put name="viewObject" beanName="pager" beanProperty="collection"/>
		<tiles:put name="view" value="assign"/>
		<tiles:put name="headerView" value="searchForPrivAssignHeader"/>
		<tiles:put name="itemView" value="assignFoundMember"/>
		<tiles:put name="itemViewInFieldset" value="true"/>
		<tiles:put name="footerView" value="searchForPrivAssignFooter"/>
		<tiles:put name="pager" beanName="pager"/>
		<tiles:put name="listInstruction" value="list.instructions.assign"/>
		<tiles:put name="noResultsMsg" value="${navMap['find.subjects.no-results']}"/>
	</tiles:insert>


