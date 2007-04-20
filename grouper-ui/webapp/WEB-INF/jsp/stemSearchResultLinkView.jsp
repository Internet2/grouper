<%-- @annotation@
		  Dynamic tile used to render a stem 'found' as
		  a result of a search in 'Create' mode
--%><%--
  @author Gary Brown.
  @version $Id: stemSearchResultLinkView.jsp,v 1.2 2007-04-20 08:26:40 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute name="viewObject"/>
<jsp:useBean id="params" scope="page" class="java.util.HashMap"/>
<c:set target="${params}" property="currentNode" value="${viewObject.stemId}"/>
<c:set target="${params}" property="advancedSearch" value="false"/>

<html:link page="/browseStemsCreate.do" name="params">
  <tiles:insert definition="dynamicTileDef" flush="false">
	  <tiles:put name="viewObject" beanName="viewObject"/>
	  <tiles:put name="view" value="searchResultItem"/>
  </tiles:insert>
</html:link>