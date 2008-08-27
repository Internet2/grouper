<%-- @annotation@
		  Dynamic tile used to display has privilege links + actual privileges
--%><%--
  @author Gary Brown.
  @version $Id: subjectHasPrivilegeView.jsp,v 1.6.4.1 2008-08-27 15:03:13 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
		<span class="hasPriv">
		<c:choose>
			<c:when test="${linkParams.subjectId=='GrouperSystem'}">
			<grouper:message bundle="${nav}" key="subject.privileges.has-for"/>
			</c:when>
			<c:otherwise>
		<html:link page="${memberPage}" name="linkParams" title="${linkTitle}">
			<fmt:message bundle="${nav}" key="subject.privileges.has-for"/></html:link> 
			</c:otherwise>
		</c:choose>
		<c:forEach var="priv" items="${possibleEffectivePrivs}">
			<c:if test="${!empty subject.privMap[priv]}">
				<c:out value="${linkSeparator}" escapeXml="false"/><c:out value="${priv}"/>
			</c:if>
		</c:forEach>
		</span>	
