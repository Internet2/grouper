<%-- @annotation@
		  Dynamic tile used to render a subject. If a group 
		  []placed around the group name
--%><%--
  @author Gary Brown.
  @version $Id: subjectView.jsp,v 1.8 2008-04-07 07:54:15 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/><c:set var="attrKey" value="*subject.display.${viewObject.source.id}"/><c:if test="${empty mediaMap[attrKey]}"><c:set var="attrKey" value="subject.display.default"/></c:if>
<c:if test="${viewObject.isGroup}">[sv</c:if><c:if test="${empty inLink}"><span class="<c:out value="${viewObject.subjectType}"/>Subject"></c:if><c:out value="${viewObject[mediaMap[attrKey]]}" /><c:if test="${empty inLink}"></span></c:if><c:if test="${viewObject.isGroup}">]</c:if>