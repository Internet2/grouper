<%-- @annotation@
		  Dynamic tile used to render a subject. If a group 
		  []placed around the group name
--%><%--
  @author Gary Brown.
  @version $Id: groupAsSubjectSearchResultView.jsp,v 1.5 2008-04-06 03:45:43 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute />
<c:if test="${empty groupSearchResultField}"><c:set scope="request" var="groupSearchResultField" value="${mediaMap['search.group.result-field']}"/></c:if>

<img src="grouper/images/group.gif" <grouper:tooltip key="group.icon.tooltip"/> class="groupIcon" />&nbsp;<span class="<c:out value="${viewObject.subjectType}"/>Subject"><c:out value="${viewObject[groupSearchResultField]}" /></span>
