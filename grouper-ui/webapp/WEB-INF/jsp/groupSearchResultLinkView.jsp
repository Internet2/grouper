<%-- @annotation@
		  Dynamic tile which renders a group found
		  by a search (not 'Find' mode) as a link to 
		  the group summary
--%><%--
  @author Gary Brown.
  @version $Id: groupSearchResultLinkView.jsp,v 1.2 2007-04-20 08:26:40 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute name="viewObject"/>
<c:set target="${viewObject}" property="advancedSearch" value="false"/>
<html:link page="/populateGroupSummary.do" name="viewObject">
  <tiles:insert definition="dynamicTileDef" flush="false">
	  <tiles:put name="viewObject" beanName="viewObject"/>
	  <tiles:put name="view" value="searchResultItem"/>
  </tiles:insert>
</html:link>