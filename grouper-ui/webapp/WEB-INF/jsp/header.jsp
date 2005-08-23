<%-- @annotation@
		 Standard tile used in baseDef which appears at the top
		 of all pages unless otherwise configured 
--%><%--
  @author Gary Brown.
  @version $Id: header.jsp,v 1.1.1.1 2005-08-23 13:04:20 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>
<grouper:recordTile key="Not dynamic" tile="${requestScope['javax.servlet.include.servlet_path']}">
  <div id="Logo"><html:img src="${mediaMap['image.organisation-logo']}" alt="logo" /></div>
  <div id="Signet"><html:img src="${mediaMap['image.grouper-logo']}" alt="Grouper logo" /></div>
</grouper:recordTile>