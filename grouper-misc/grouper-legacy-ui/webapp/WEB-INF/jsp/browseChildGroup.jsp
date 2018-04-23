<%-- @annotation@
		  Dynamic tile used in all browse modes except 'Find'
		  to display a child group
--%><%--
  @author Gary Brown.
  @version $Id: browseChildGroup.jsp,v 1.11 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<c:set var="linkTitle"><grouper:message key="browse.to.group.summary" tooltipDisable="true">
		 		<grouper:param value="${viewObject.displayExtension}"/>
</grouper:message></c:set>

<img src="grouper/images/group.gif" <grouper:tooltip 
key="group.icon.tooltip"/> class="groupIcon"  alt="Group"
/><html:link page="/populateGroupSummary.do" 
			paramId="groupId" 
			paramName="viewObject" 
			paramProperty="id"
			title="${linkTitle}">
				<span class="groupSubject"><c:choose><c:when test="${isFlat}"><c:out value="${viewObject[mediaMap['group.display.flat']]}"/></c:when><c:otherwise><c:out value="${viewObject[mediaMap['group.display']]}"/></c:otherwise></c:choose></span>
 </html:link>
