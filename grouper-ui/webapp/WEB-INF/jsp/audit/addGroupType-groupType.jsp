<%-- @annotation@
		  audit log view
--%><%--
  @author Gary Brown.
  @version $Id: addGroupType-groupType.jsp,v 1.2 2009-08-10 14:03:01 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<em><strong><c:choose>
	<c:when test="${empty viewObject.fieldObjects.id}"><c:out value="${viewObject.fields.name}-${viewObject.fields.id}"/> (<grouper:message bundle="${nav}" key="audit.result.label.unavailable"/>)</c:when>
	<c:otherwise><a title="<grouper:message bundle="${nav}" key="audit.result.label.group-type.link-title"/>" 
	href="populateGroupTypes.do?callerPageId=<c:out value="${thisPageId}"/>#<c:out value="grouptype-${viewObject.fields.id}"/>"><c:out value="${viewObject.fields.name}"/></a></c:otherwise>
</c:choose></strong>
</em>