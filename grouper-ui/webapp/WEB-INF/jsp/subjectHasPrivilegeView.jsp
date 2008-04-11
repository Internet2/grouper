<%-- @annotation@
		  Dynamic tile used to display has privilege links + actual privileges
--%><%--
  @author Gary Brown.
  @version $Id: subjectHasPrivilegeView.jsp,v 1.6 2008-04-11 14:49:36 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<jsp:useBean id="params" class="java.util.HashMap" />
		<span class="hasPriv">
		<c:choose>
			<c:when test="${params.subjectId=='GrouperSystem'}">
			<grouper:message bundle="${nav}" key="subject.privileges.has-for"/>
			</c:when>
			<c:otherwise>
		<html:link page="${memberPage}" name="params" title="${linkTitle}">
			<fmt:message bundle="${nav}" key="subject.privileges.has-for"/></html:link> 
			</c:otherwise>
		</c:choose>
		<c:forEach var="priv" items="${possibleEffectivePrivs}">
			<c:if test="${!empty privMap[priv]}">
				<c:out value="${linkSeparator}" escapeXml="false"/><c:out value="${priv}"/>
			</c:if>
		</c:forEach>
		</span>	
