<%-- @annotation@
		  Dynamic tile used to render a group as a search result
--%><%--
  @author Gary Brown.
  @version $Id: groupSearchResultItemView.jsp,v 1.1.2.1 2006-03-10 10:04:40 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<c:if test="${empty groupSearchResultField}"><c:set var="groupSearchResultField" value="${mediaMap['search.group.result-field']}"/></c:if>
[<span class="<c:out value="${viewObject.subjectType}"/>Subject"><c:out value="${viewObject[groupSearchResultField]}" /></span>]