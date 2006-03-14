<%-- @annotation@
		  Dynamic tile used to render a subject. If a group 
		  []placed around the group name
--%><%--
  @author Gary Brown.
  @version $Id: groupAsSubjectSearchResultView.jsp,v 1.1.2.1 2006-03-14 09:52:20 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute />
[<span class="<c:out value="${viewObject.subjectType}"/>Subject"><c:out value="${viewObject[mediaMap['search.group.result-field']]}" /></span>]