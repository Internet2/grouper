<%-- @annotation@
		  Ensures that session is initialised with
		  ResourceBundles. May be called even  when
		  not needed - so must review
--%><%--
  @author Gary Brown.
  @version $Id: initPreLoginSession.jsp,v 1.2 2006-10-05 09:07:38 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%> 
<%  
	if(Boolean.TRUE.equals(session.getAttribute("sessionInited"))) return;
    org.apache.struts.config.ModuleConfig config1 = (org.apache.struts.config.ModuleConfig) request.getAttribute("org.apache.struts.action.MODULE");
    String module = config1.getPrefix();    
    SessionInitialiser.init(module,session);
%>

