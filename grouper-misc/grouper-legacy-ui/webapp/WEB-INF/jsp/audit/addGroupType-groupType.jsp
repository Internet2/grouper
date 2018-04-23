<%-- @annotation@
		  audit log view
--%><%--
  @author Gary Brown.
  @version $Id: addGroupType-groupType.jsp,v 1.3 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<em><strong><c:choose>
	<c:when test="${empty viewObject.fieldObjects.id}"><c:out value="${viewObject.fields.name}-${viewObject.fields.id}"/> (<grouper:message key="audit.result.label.unavailable"/>)</c:when>
	<c:otherwise><a title="<grouper:message key="audit.result.label.group-type.link-title" tooltipDisable="true"/>" 
	href="populateGroupTypes.do?callerPageId=<c:out value="${thisPageId}"/>#<c:out value="grouptype-${viewObject.fields.id}"/>"><c:out value="${viewObject.fields.name}"/></a></c:otherwise>
</c:choose></strong>
</em>