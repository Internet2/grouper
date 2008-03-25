<%-- @annotation@ 
			Displays link to show access privileges for given subject on given group
--%><%--
  @author Gary Brown.
  @version $Id: accessPrivilegesLinkView.jsp,v 1.3 2008-03-25 16:30:18 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<c:set var="linkTitle"><grouper:message bundle="${nav}" key="browse.assign.title">
	<grouper:param value="${viewObject.subject.desc}"/>
	<grouper:param value="${browseParent.desc}"/>
</grouper:message></c:set>
<c:set var="linkText" value="groups.privilege.direct"/>
<c:if test="${!viewObject.isDirect}"><c:set var="linkText" value="groups.privilege.indirect"/></c:if>		

<html:link page="/populateGroupMember.do" name="params" 
	title="${linkTitle}"><fmt:message bundle="${nav}" key="${linkText}"/></html:link> 