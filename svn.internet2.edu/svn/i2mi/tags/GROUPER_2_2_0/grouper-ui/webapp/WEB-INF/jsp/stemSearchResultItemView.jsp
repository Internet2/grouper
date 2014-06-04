<%-- @annotation@
		  Dynamic tile used to render a group as a search result
--%><%--
  @author Gary Brown.
  @version $Id: stemSearchResultItemView.jsp,v 1.1 2006-02-24 13:47:38 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true"/>
<c:if test="${empty stemSearchResultField}"><c:set var="stemSearchResultField" value="${mediaMap['search.stem.result-field']}"/></c:if>
<span class="<c:out value="${viewObject.subjectType}"/>Subject"><c:out value="${viewObject[stemSearchResultField]}" /></span>