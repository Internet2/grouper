<%-- @annotation@
		  audit log view
--%><%--
  @author Gary Brown.
  @version $Id: updateGroupField-groupField.jsp,v 1.2 2009-08-10 14:03:01 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<c:out value="${viewObject.fields.type}"/> <em><c:out value="${viewObject.fields.name}"/></em>
<grouper:message bundle="${nav}" key="audit.result.label.on"/>

<em><strong><c:choose>
	<c:when test="${empty viewObject.fieldObjects.groupTypeId}"><c:out value="${viewObject.fields.groupTypeName}-${viewObject.fields.groupTypeId}"/> (<grouper:message bundle="${nav}" key="audit.result.label.unavailable"/>)</c:when>
	<c:otherwise><a title="<grouper:message bundle="${nav}" key="audit.result.label.group-type.link-title"/>" 
	href="populateGroupTypes.do?callerPageId=<c:out value="${thisPageId}"/>#<c:out value="grouptype-${viewObject.fields.groupTypeId}"/>"><c:out value="${viewObject.fields.groupTypeName}"/></a></c:otherwise>
</c:choose></strong>
</em>
