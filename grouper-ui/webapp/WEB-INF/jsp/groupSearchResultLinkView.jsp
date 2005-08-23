<%-- @annotation@
		  Dynamic tile which renders a group found
		  by a search (not 'Find' mode) as a link to 
		  the group summary
--%><%--
  @author Gary Brown.
  @version $Id: groupSearchResultLinkView.jsp,v 1.1.1.1 2005-08-23 13:04:20 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute name="viewObject"/>
<html:link page="/populateGroupSummary.do" name="viewObject">
  <tiles:insert definition="dynamicTileDef" flush="false">
	  <tiles:put name="viewObject" beanName="viewObject"/>
	  <tiles:put name="view" value="searchResultItem"/>
  </tiles:insert>
</html:link>