<%-- @annotation@ 
			Splash page for unauthenticated users, Displays getting started
			info and a login link
--%><%--
  @author Gary Brown.
  @version $Id: index.jsp,v 1.5 2009-10-16 14:33:56 isgwb Exp $
--%>
<%@ include file="/WEB-INF/jsp/include.jsp" %>
<div id="grouperLogin" >
<h2><grouper:message key="access.grouper"/></h2>
<html:link page="/callLogin.do"><grouper:message key="login"/></html:link>
<c:if test="${mediaMap['login.ui-lite.show-link']=='true'}">
<p><br/></p>
<hr/>
<p><br/><grouper:message key="ui-lite.login.intro"/><br/><html:link page="${mediaMap['login.ui-lite.link']}"><grouper:message key="ui-lite.login.link-text"/></html:link></p>
</c:if>
</div>
<div id="grouperIntro">
<tiles:insert definition="gettingStartedDef"/>
</div>




