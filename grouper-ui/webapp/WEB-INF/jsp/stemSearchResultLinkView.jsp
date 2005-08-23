<%-- @annotation@
		  Dynamic tile used to render a stem 'found' as
		  a result of a search in 'Create' mode
--%><%--
  @author Gary Brown.
  @version $Id: stemSearchResultLinkView.jsp,v 1.1.1.1 2005-08-23 13:04:20 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute name="viewObject"/>
<html:link page="/browseStemsCreate.do" paramId="currentNode" paramName="viewObject" paramProperty="stemId">
  <tiles:insert definition="dynamicTileDef" flush="false">
	  <tiles:put name="viewObject" beanName="viewObject"/>
	  <tiles:put name="view" value="searchResultItem"/>
  </tiles:insert>
</html:link>