<%-- @annotation@ 
			Splash page for unauthenticated users, Displays getting started
			info and a login link
--%><%--
  @author Gary Brown.
  @version $Id: index.jsp,v 1.3 2008-03-25 14:59:51 mchyzer Exp $
--%>
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<div id="grouperLogin" >
<h2><grouper:message bundle="${nav}" key="access.grouper"/></h2>
<html:link page="/callLogin.do"><grouper:message bundle="${nav}" key="login"/></html:link>
</div>
<div id="grouperIntro">
<tiles:insert definition="gettingStartedDef"/>
</div>




