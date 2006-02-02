<%-- @annotation@ 
			Displays link to show access privileges for given subject on given group
--%><%--
  @author Gary Brown.
  @version $Id: accessPrivilegesLinkView.jsp,v 1.1 2006-02-02 16:40:48 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<c:set var="linkTitle"><fmt:message bundle="${nav}" key="browse.assign.title">
	<fmt:param value="${viewObject.subject.desc}"/>
	<fmt:param value="${browseParent.desc}"/>
</fmt:message></c:set>
<c:set var="linkText" value="groups.privilege.direct"/>
<c:if test="${!viewObject.isDirect}"><c:set var="linkText" value="groups.privilege.indirect"/></c:if>		

<html:link page="/populateGroupMember.do" name="params" 
	title="${linkTitle}"><fmt:message bundle="${nav}" key="${linkText}"/></html:link> 