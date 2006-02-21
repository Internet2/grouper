<%-- @annotation@
		  Dynamic tile used to display has privilege links + actual privileges
--%><%--
  @author Gary Brown.
  @version $Id: subjectHasPrivilegeView.jsp,v 1.2 2006-02-21 16:31:32 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
		<span class="hasPriv">
		<html:link page="${memberPage}" name="params" title="${linkTitle}">
			<fmt:message bundle="${nav}" key="subject.privileges.has-for"/></html:link> 
		<c:forEach var="priv" items="${possibleEffectivePrivs}">
			<c:if test="${!empty privMap[priv]}">
				<c:out value="${linkSeparator}" escapeXml="false"/><c:out value="${priv}"/>
			</c:if>
		</c:forEach>
		</span>	
