<%-- @annotation@
		 Standard tile used in baseDef which appears at the right
		 of all page - empty in the standard UI
--%><%--
  @author Gary Brown.
  @version $Id: right.jsp,v 1.1.1.1 2005-08-23 13:04:20 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
</grouper:recordTile>
