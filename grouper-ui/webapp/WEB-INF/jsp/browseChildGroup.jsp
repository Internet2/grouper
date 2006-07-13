<%-- @annotation@
		  Dynamic tile used in all browse modes except 'Find'
		  to display a child group
--%><%--
  @author Gary Brown.
  @version $Id: browseChildGroup.jsp,v 1.3 2006-07-13 10:27:14 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<c:set var="linkTitle"><fmt:message bundle="${nav}" key="browse.to.group.summary">
		 		<fmt:param value="${viewObject.displayExtension}"/>
</fmt:message></c:set>

[<html:link page="/populateGroupSummary.do" 
			paramId="groupId" 
			paramName="viewObject" 
			paramProperty="id"
			title="${linkTitle}">
				<span class="groupSubject"><c:out value="${viewObject.displayExtension}"/></span>
 </html:link>]
