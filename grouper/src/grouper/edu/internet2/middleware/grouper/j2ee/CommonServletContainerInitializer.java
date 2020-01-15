package edu.internet2.middleware.grouper.j2ee;

import java.util.Set;

import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class CommonServletContainerInitializer implements ServletContainerInitializer {
  
  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(CommonServletContainerInitializer.class);

  @Override
  public void onStartup(Set<Class<?>> arg0, ServletContext context) throws ServletException {
    
    try {
      
      boolean runGrouperUi = GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.is.ui", false);
      boolean runGrouperUiWithBasicAuth = GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.is.ui.basicAuthn", false);
      
      boolean runGrouperWs = GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.is.ws", false);
      boolean runGrouperWsWithBasicAuth = GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.is.ws.basicAuthn", false);
      
      boolean runGrouperScim = GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.is.scim", false);
      boolean runGrouperScimWithBasicAuth = GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.is.scim.basicAuthn", false);
      
      boolean runGrouperDaemon = GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.is.daemon", false);
      
      {
        String statusServletName = "StatusServlet";
        Class statusServletClass = Class.forName("edu.internet2.middleware.grouper.j2ee.status.GrouperStatusServlet");
        javax.servlet.ServletRegistration.Dynamic statusServlet = context.addServlet(statusServletName, statusServletClass);
        statusServlet.addMapping("/status");
        statusServlet.setLoadOnStartup(1);
      }
      
      if (runGrouperUi) {
        String grouperUiFilterName = "GrouperUi";
        Class grouperUiFilterClass = Class.forName("edu.internet2.middleware.grouper.ui.GrouperUiFilter");
        Dynamic grouperUiFilter = context.addFilter(grouperUiFilterName, grouperUiFilterClass);
        grouperUiFilter.addMappingForUrlPatterns(null, false, "*.jsp");
        grouperUiFilter.addMappingForUrlPatterns(null, false, "/grouperUi/app/*");
        grouperUiFilter.addMappingForUrlPatterns(null, false, "/grouperUi/appHtml/*");
        grouperUiFilter.addMappingForUrlPatterns(null, false, "/grouperExternal/app/*");
        grouperUiFilter.addMappingForUrlPatterns(null, false, "/grouperExternal/public/UiV2Public.index");
        grouperUiFilter.addMappingForUrlPatterns(null, false, "/grouperExternal/public/UiV2Public.postIndex");
        
        String grouperUiCsrfFilterName = "CSRFGuard";
        Class grouperUiCsfrFilterClass = Class.forName("org.owasp.csrfguard.CsrfGuardFilter");
        Dynamic grouperUiCsrfFilter = context.addFilter(grouperUiCsrfFilterName, grouperUiCsfrFilterClass);
        grouperUiCsrfFilter.addMappingForUrlPatterns(null, false, "/*");

        Class grouperSessionAttributeListener = Class.forName("edu.internet2.middleware.grouper.ui.GrouperSessionAttributeListener");
        context.addListener(grouperSessionAttributeListener);
        
        Class csrfGuardServletContextListener = Class.forName("org.owasp.csrfguard.CsrfGuardServletContextListener");
        context.addListener(csrfGuardServletContextListener);
        
        Class csrfGuardHttpSessionListener = Class.forName("org.owasp.csrfguard.CsrfGuardHttpSessionListener");
        context.addListener(csrfGuardHttpSessionListener);
        
        String uiServletName = "UiServlet";
        Class uiServletClass = Class.forName("edu.internet2.middleware.grouper.j2ee.GrouperUiRestServlet");
        javax.servlet.ServletRegistration.Dynamic uiServlet = context.addServlet(uiServletName, uiServletClass);
        uiServlet.addMapping("/grouperUi/app/*");
        uiServlet.addMapping("/grouperExternal/app/*");
        uiServlet.addMapping("/grouperExternal/public/UiV2Public.index");
        uiServlet.addMapping("/grouperExternal/public/UiV2Public.index");
        uiServlet.addMapping("/grouperExternal/public/UiV2Public.postIndex");
        
        String owaspJavascriptServletName = "OwaspJavaScriptServlet";
        Class owaspJavascriptServletClass = Class.forName("org.owasp.csrfguard.servlet.JavaScriptServlet");
        javax.servlet.ServletRegistration.Dynamic owaspJavascriptServlet = context.addServlet(owaspJavascriptServletName, owaspJavascriptServletClass);
        owaspJavascriptServlet.addMapping("/grouperExternal/public/OwaspJavaScriptServlet");
      }
      
      if (runGrouperWs) {
        
        String grouperWsLoggingFilterName = "Grouper logging filter";
        Class grouperWsLoggingFilterClass = Class.forName("edu.internet2.middleware.grouper.ws.j2ee.ServletFilterLogger");
        Dynamic grouperWsLoggingFilter = context.addFilter(grouperWsLoggingFilterName, grouperWsLoggingFilterClass);
       
        String grouperWsServiceFilterName = "Grouper service filter";
        Class grouperWsServiceFilterClass = Class.forName("edu.internet2.middleware.grouper.ws.GrouperServiceJ2ee");
        Dynamic grouperWsServiceFilter = context.addFilter(grouperWsServiceFilterName, grouperWsServiceFilterClass);
        grouperWsServiceFilter.addMappingForUrlPatterns(null, false, "/services/*");
        grouperWsServiceFilter.addMappingForUrlPatterns(null, false, "/servicesRest/*");
        
        Class grouperWsJ2eeListener = Class.forName("edu.internet2.middleware.grouper.ws.j2ee.GrouperJ2eeListener");
        context.addListener(grouperWsJ2eeListener);
        
        String axisServletName = "AxisServlet";
        Class axisServletClass = Class.forName("edu.internet2.middleware.grouper.ws.GrouperServiceAxisServlet");
        javax.servlet.ServletRegistration.Dynamic axisServlet = context.addServlet(axisServletName, axisServletClass);
        axisServlet.addMapping("/services/*");
        axisServlet.setLoadOnStartup(1);
        
        String restServletName = "RestServlet";
        Class restServletClass = Class.forName("edu.internet2.middleware.grouper.ws.rest.GrouperRestServlet");
        javax.servlet.ServletRegistration.Dynamic restServlet = context.addServlet(restServletName, restServletClass);
        restServlet.addMapping("/servicesRest/*");
        restServlet.setLoadOnStartup(1);
        
      }
      
      if (runGrouperScim) {
        String baseScimServletName = "edu.internet2.middleware.grouper.ws.scim.RestApplication";
        Class baseScimServletClass = Class.forName("edu.internet2.middleware.grouper.ws.scim.RestApplication");
        javax.servlet.ServletRegistration.Dynamic baseScimServlet = context.addServlet(baseScimServletName, baseScimServletClass);
        baseScimServlet.addMapping("/v2/*");
        baseScimServlet.setInitParameter("javax.ws.rs.Application", "edu.internet2.middleware.grouper.ws.scim.RestApplication");
        
        Class scimConfiguratorListener = Class.forName("edu.internet2.middleware.grouper.ws.scim.ScimConfigurator");
        context.addListener(scimConfiguratorListener);
        
        Class swaggerJaxrsConfigListener = Class.forName("edu.internet2.middleware.grouper.ws.scim.SwaggerJaxrsConfig");
        context.addListener(swaggerJaxrsConfigListener);
        
        String tierFilterName = "TierFilter";
        Class tierFilterClass = Class.forName("edu.internet2.middleware.grouper.ws.scim.TierFilter");
        Dynamic tierFilter = context.addFilter(tierFilterName, tierFilterClass);
        tierFilter.addMappingForUrlPatterns(null, false, "/v2/*");
      }
      
      if (runGrouperDaemon) {
        
        Thread thread = new Thread(new Runnable() {
          
          public void run() {
            try {
              GrouperLoader.main(new String[] {});
            } catch (RuntimeException e) {
              throw new RuntimeException("could not start loader", e);
            }
          }
        });
        
        thread.start();
      }
      
      
      
      
    } catch (Exception e) {
      throw new RuntimeException("Error in registering filters, servlets and listeners", e);
    }
    
  }

}
