<%-- @annotation@
		  Dynamic tile used in all browse modes except 'Find'
		  to display a child group
--%><%--
  @author Gary Brown.
  @version $Id: browseChildGroup.jsp,v 1.7 2008-04-06 03:45:43 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<c:set var="linkTitle"><grouper:message bundle="${nav}" key="browse.to.group.summary">
		 		<grouper:param value="${viewObject.displayExtension}"/>
</grouper:message></c:set>

<img src="grouper/images/group.gif" <grouper:tooltip key="group.icon.tooltip"/> class="groupIcon" />&nbsp;<html:link page="/populateGroupSummary.do" 
			paramId="groupId" 
			paramName="viewObject" 
			paramProperty="id"
			title="${linkTitle}">
				<span class="groupSubject"><c:choose><c:when test="${isFlat}"><c:out value="${viewObject[mediaMap['group.display.flat']]}"/></c:when><c:otherwise><c:out value="${viewObject[mediaMap['group.display']]}"/></c:otherwise></c:choose></span>
 </html:link>
