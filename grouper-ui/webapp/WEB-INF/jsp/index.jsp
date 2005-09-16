<%-- @annotation@ 
			Splash page for unauthenticated users, Displays getting started
			info and a login link
--%><%--
  @author Gary Brown.
  @version $Id: index.jsp,v 1.2 2005-09-16 09:51:56 isgwb Exp $
--%>
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<div id="grouperLogin" >
<h2><fmt:message bundle="${nav}" key="access.grouper"/></h2>
<html:link page="/callLogin.do"><fmt:message bundle="${nav}" key="login"/></html:link>
</div>
<div id="grouperIntro">
<tiles:insert definition="gettingStartedDef"/>
</div>




