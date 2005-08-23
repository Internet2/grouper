<%-- @annotation@
		  Dynamic tile used to render a subject. If a group 
		  []placed around the group name
--%><%--
  @author Gary Brown.
  @version $Id: subjectView.jsp,v 1.1.1.1 2005-08-23 13:04:20 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute />
<c:if test="${viewObject.isGroup}">[</c:if><span class="<c:out value="${viewObject.subjectType}"/>Subject"><c:out value="${viewObject.desc}" /></span><c:if test="${viewObject.isGroup}">]</c:if>