<%-- @annotation@
		  Dynamic tile used to render a subject. If a group 
		  []placed around the group name
--%>
<%--
  @author Gary Brown.
  @version $Id: subjectSearchResultView.jsp,v 1.2 2008-04-07 07:54:15 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute ignore="true" />
<c:set var="attrKey" value="*subject.display.${viewObject.source.id}" />
<c:if test="${empty mediaMap[attrKey]}">
  <c:set var="attrKey" value="subject.display.default" />
</c:if>
<%-- note, dont do a tooltip here since there is a title attribute --%>
<img 
src="grouper/images/subject.gif"
class="subjectIcon" />&nbsp;<
  c:if test="${empty inLink}"><span class="<c:out value="${viewObject.subjectType}"/>Subject"></c:if><
  c:out value="${viewObject[mediaMap[attrKey]]}" /><c:if test="${empty inLink}"></span></c:if>