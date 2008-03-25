<%-- @annotation@
			Link to subject summary page
--%><%--
  @author Gary Brown.
  @version $Id: subjectSearchResultLinkView.jsp,v 1.2 2008-03-25 14:59:51 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute name="viewObject"/>
<c:set var="linkTitle"><grouper:message bundle="${nav}" key="browse.to.subject.summary">
		 		<grouper:param value="${viewObject.description}"/>
		</grouper:message></c:set>
<html:link page="/populateSubjectSummary.do" name="viewObject" title="${linkTitle}">
  <tiles:insert definition="dynamicTileDef" flush="false">
	  <tiles:put name="viewObject" beanName="viewObject"/>
	  <tiles:put name="view" value="subjectSearchResult"/>
  </tiles:insert>
</html:link>