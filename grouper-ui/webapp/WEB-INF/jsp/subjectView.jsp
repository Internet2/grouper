<%-- @annotation@
		  Dynamic tile used to render a subject. If a group 
		  []placed around the group name
--%><%--
  @author Gary Brown.
  @version $Id: subjectView.jsp,v 1.3 2006-07-17 10:05:42 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<c:if test="${viewObject.isGroup}">[</c:if><c:if test="${empty inLink}"><span class="<c:out value="${viewObject.subjectType}"/>Subject"></c:if><c:out value="${viewObject.desc}" /><c:if test="${empty inLink}"></span></c:if><c:if test="${viewObject.isGroup}">]</c:if>