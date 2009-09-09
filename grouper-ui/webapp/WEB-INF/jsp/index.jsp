<%-- @annotation@ 
			Splash page for unauthenticated users, Displays getting started
			info and a login link
--%><%--
  @author Gary Brown.
  @version $Id: index.jsp,v 1.4 2009-09-09 15:10:03 mchyzer Exp $
--%>
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<div id="grouperLogin" >
<h2><grouper:message key="access.grouper"/></h2>
<html:link page="/callLogin.do"><grouper:message key="login"/></html:link>
</div>
<div id="grouperIntro">
<tiles:insert definition="gettingStartedDef"/>
</div>




