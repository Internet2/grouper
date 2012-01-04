<%-- @annotation@
			Link to subject summary page
--%><%--
  @author Gary Brown.
  @version $Id: subjectSearchResultLinkView.jsp,v 1.4 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute name="viewObject"/>
<c:set var="linkTitle"><grouper:message key="browse.to.subject.summary" tooltipDisable="true">
		 		<grouper:param value="${viewObject.description}"/>
		</grouper:message></c:set>
<html:link page="/populateSubjectSummary.do" name="viewObject" title="${linkTitle}">
  <tiles:insert definition="dynamicTileDef" flush="false">
	  <tiles:put name="viewObject" beanName="viewObject"/>
	  <tiles:put name="view" value="subjectSearchResult"/>
  </tiles:insert>
</html:link>xx