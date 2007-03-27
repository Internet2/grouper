<%-- @annotation@
		  Dynamic tile used to render a subject. If a group 
		  []placed around the group name
--%><%--
  @author Gary Brown.
  @version $Id: groupAsSubjectSearchResultView.jsp,v 1.4 2007-03-27 12:10:29 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute />
<c:if test="${empty groupSearchResultField}"><c:set scope="request" var="groupSearchResultField" value="${mediaMap['search.group.result-field']}"/></c:if>
[<span class="<c:out value="${viewObject.subjectType}"/>Subject"><c:out value="${viewObject[groupSearchResultField]}" /></span>]