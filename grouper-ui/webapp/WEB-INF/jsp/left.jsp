<%-- @annotation@ 
		 Standard tile used in baseDef which appears at the left
		 of all pages 
--%><%--
  @author Gary Brown.
  @version $Id: left.jsp,v 1.2 2005-11-08 16:11:32 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>

<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">

</grouper:recordTile>