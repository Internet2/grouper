<%-- @annotation@
		  Wrapper which inserts the 'content' attribute into the page.
		  The wrapper can be redefined to allow sites to add extra 
		  XHTML around the page specific content, or to re-arrange
		  the page specific output.
--%><%--
  @author Gary Brown.
  @version $Id: content.jsp,v 1.1.1.1 2005-08-23 13:04:20 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<tiles:importAttribute name="tile"/>
<grouper:recordTile key="Not dynamic" tile="${tile}">
	<tiles:insert page="${tile}"/>
</grouper:recordTile>