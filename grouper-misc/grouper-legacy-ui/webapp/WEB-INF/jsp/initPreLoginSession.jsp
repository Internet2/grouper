<%-- @annotation@
		  Ensures that session is initialised with
		  ResourceBundles. May be called even  when
		  not needed - so must review
--%><%--
  @author Gary Brown.
  @version $Id: initPreLoginSession.jsp,v 1.3 2008-04-13 09:00:59 isgwb Exp $
--%>
<%@include file="/WEB-INF/jsp/include.jsp"%> 
<%  
	if(Boolean.TRUE.equals(session.getAttribute("sessionInited"))) return;
    org.apache.struts.config.ModuleConfig config1 = (org.apache.struts.config.ModuleConfig) request.getAttribute("org.apache.struts.action.MODULE");
    String module = config1.getPrefix();
    try {    
		SessionInitialiser.init(module,session);
	}catch(Exception e) {
		Log LOG = LogFactory.getLog(ErrorFilter.class);
		LOG.error("initPreLoginSession.jsp - error initialising session: " + e.getMessage());
	}
%>

