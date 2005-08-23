<%-- @annotation@
		  Ensures that session is initialised with
		  ResourceBundles. May be called even  when
		  not needed - so must review
--%><%--
  @author Gary Brown.
  @version $Id: initPreLoginSession.jsp,v 1.1.1.1 2005-08-23 13:04:20 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%> 
<%  
    org.apache.struts.config.ModuleConfig config1 = (org.apache.struts.config.ModuleConfig) request.getAttribute("org.apache.struts.action.MODULE");
    String module = config1.getPrefix();    
    SessionInitialiser.init(module,session);
%>

