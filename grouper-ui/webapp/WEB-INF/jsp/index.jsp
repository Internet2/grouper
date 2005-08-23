<%-- @annotation@ 
			Splash page for unauthenticated users, Displays getting started
			info and a login link
--%><%--
  @author Gary Brown.
  @version $Id: index.jsp,v 1.1.1.1 2005-08-23 13:04:20 isgwb Exp $
--%>
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<div id="grouperLogin" >
<h2>Access GroupsManager</h2>
<html:link page="/callLogin.do"><fmt:message bundle="${nav}" key="login"/></html:link>
</div>
<div id="grouperIntro">
<tiles:insert definition="gettingStartedDef"/>
</div>




