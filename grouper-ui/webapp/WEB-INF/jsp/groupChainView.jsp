<%-- @annotation@
      Dynamic tile used to render a subject. If a group 
      []placed around the group name
--%><%--
  @author Gary Brown.
  @version $Id: groupChainView.jsp,v 1.1 2008-04-07 07:54:15 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute />
<c:if test="${empty groupSearchResultField}"><c:set scope="request" var="groupSearchResultField" value="${mediaMap['search.group.result-field']}"/></c:if>
<%-- note, dont do a tooltip here since there is a title attribute --%>
<img src="grouper/images/group.gif" <grouper:tooltip key="group.icon.tooltip"/>
 class="groupIcon" />&nbsp;<span class="<c:out value="${viewObject.subjectType}"/>Subject"><c:out value="${viewObject[groupSearchResultField]}" /></span>
