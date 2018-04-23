<%-- @annotation@ Sets up LocalizationContext for JSTL --%>
<%--
  @author Gary Brown.
  @version $Id: InitSession.jsp,v 1.2 2006-11-07 00:08:04 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%>


<!--InitSession-->
<c:set var="grouper.locale" scope="request"><req:parameter name='lang'/></c:set>
<%
Locale locale = new Locale((String)request.getAttribute("grouper.locale"));
session.setAttribute("org.apache.struts.action.LOCALE",locale);

    org.apache.struts.config.ModuleConfig configx = (org.apache.struts.config.ModuleConfig) request.getAttribute("org.apache.struts.action.MODULE");
    String module = configx.getPrefix();    
    SessionInitialiser.init(module,locale.toString(),session);
%>


<fmt:setLocale scope="session" value="${grouper.locale}"  />



<!--c:set var="sessionInited" scope="session" value="${grouper.locale}"/-->
<!--/InitSession-->
<%-- @annotation@ tiles:insert #1 --%>


        <!--tiles:insert page="${requestScope['org.apache.struts.action.MODULE'].prefix}/home.do"/-->

<div style="visibility:hidden"><html:link page="/home.do"></html:link></div>