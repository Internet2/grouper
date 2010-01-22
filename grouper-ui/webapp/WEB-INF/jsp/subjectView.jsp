<%-- @annotation@
		  Dynamic tile used to render a subject. If a group 
		  []placed around the group name
--%><%--
  @author Gary Brown.
  @version $Id: subjectView.jsp,v 1.10 2008-04-16 09:10:32 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/><c:set var="attrKey" value="*subject.display.${viewObject.source.id}"/><c:if test="${empty mediaMap[attrKey]}"><c:set var="attrKey" value="subject.display.default"/></c:if>
<c:if test="${viewObject.isGroup}"><img <grouper:tooltip key="group.icon.tooltip"/> 
    src="grouper/images/group.gif" class="groupIcon" alt="Group" 
    /></c:if><c:if test="${empty inLink}"><span class="<c:out value="${viewObject.subjectType}"/>Subject"></c:if><c:out value="${viewObject[mediaMap[attrKey]]}" /><c:if test="${empty inLink}"></span></c:if>