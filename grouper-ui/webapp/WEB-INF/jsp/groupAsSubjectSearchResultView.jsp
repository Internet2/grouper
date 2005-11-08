<%-- @annotation@
		  Dynamic tile used to render a subject. If a group 
		  []placed around the group name
--%><%--
  @author Gary Brown.
  @version $Id: groupAsSubjectSearchResultView.jsp,v 1.1 2005-11-08 15:21:50 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute />
<c:if test="${empty viewObject.description}"><c:set target="${viewObject}" property="description" value="${viewObject.displayExtension}"/></c:if>
[<span class="<c:out value="${viewObject.subjectType}"/>Subject"><c:out value="${viewObject.description}" /></span>]