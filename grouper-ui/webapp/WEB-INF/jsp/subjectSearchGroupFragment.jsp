<%-- @annotation@
		Tile which displays the group subject search options in the subject search form.
		Designed to be embedded in actual forms.
--%><%--
  @author Gary Brown.
  @version $Id: subjectSearchGroupFragment.jsp,v 1.1 2007-11-08 14:40:03 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute/>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
<c:if test="${!empty browsePath}">
	<tiles:insert definition="searchFromDef"/>
</c:if>
<tiles:insert definition="searchGroupResultFieldChoiceDef"/>
</grouper:recordTile>