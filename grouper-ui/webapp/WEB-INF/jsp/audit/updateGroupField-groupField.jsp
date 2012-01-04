<%-- @annotation@
		  audit log view
--%><%--
  @author Gary Brown.
  @version $Id: updateGroupField-groupField.jsp,v 1.3 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<c:out value="${viewObject.fields.type}"/> <em><c:out value="${viewObject.fields.name}"/></em>
<grouper:message key="audit.result.label.on"/>

<em><strong><c:choose>
	<c:when test="${empty viewObject.fieldObjects.groupTypeId}"><c:out value="${viewObject.fields.groupTypeName}-${viewObject.fields.groupTypeId}"/> (<grouper:message key="audit.result.label.unavailable"/>)</c:when>
	<c:otherwise><a title="<grouper:message key="audit.result.label.group-type.link-title" tooltipDisable="true"/>" 
	href="populateGroupTypes.do?callerPageId=<c:out value="${thisPageId}"/>#<c:out value="grouptype-${viewObject.fields.groupTypeId}"/>"><c:out value="${viewObject.fields.groupTypeName}"/></a></c:otherwise>
</c:choose></strong>
</em>
