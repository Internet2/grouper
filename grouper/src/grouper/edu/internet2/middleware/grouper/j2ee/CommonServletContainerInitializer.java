package edu.internet2.middleware.grouper.j2ee;

import java.util.Set;

import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.GrouperHibernateConfig;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfigInApi;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class CommonServletContainerInitializer implements ServletContainerInitializer {
  
  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(CommonServletContainerInitializer.class);

  @Override
  public void onStartup(Set<Class<?>> arg0, ServletContext context) throws ServletException {
    
      boolean runGrouperUi = GrouperHibernateConfig.retrieveConfig().propertyValueBoolean("grouper.is.ui", false);

      boolean runMockServices = GrouperHibernateConfig.retrieveConfig().propertyValueBoolean("grouper.is.mockServices", false);

      boolean runGrouperWs = GrouperHibernateConfig.retrieveConfig().propertyValueBoolean("grouper.is.ws", false);
      
      boolean runGrouperScim = GrouperHibernateConfig.retrieveConfig().propertyValueBoolean("grouper.is.scim", false);
      
      boolean runGrouperDaemon = GrouperHibernateConfig.retrieveConfig().propertyValueBoolean("grouper.is.daemon", false);
      
      try {
        String statusServletName = "StatusServlet";
        Class statusServletClass = Class.forName("edu.internet2.middleware.grouper.j2ee.status.GrouperStatusServlet");
        javax.servlet.ServletRegistration.Dynamic statusServlet = context.addServlet(statusServletName, statusServletClass);
        statusServlet.addMapping("/status");
        statusServlet.setLoadOnStartup(1);
      } catch (ClassNotFoundException e) {
        throw new RuntimeException("why edu.internet2.middleware.grouper.j2ee.status.GrouperStatusServlet is not there??");
      }
     
      if (runMockServices) {
        
        String uiServletName = "MockServices";
        javax.servlet.ServletRegistration.Dynamic uiServlet = context.addServlet(uiServletName, MockServiceServlet.class);
        uiServlet.addMapping("/mockServices/*");
          
      }

      
      if (runGrouperUi) {
        
        String[] urlPatterns = new String[] {"/grouperUi/app/*", "/grouperExternal/app/*", "/grouperExternal/public/UiV2Public.index", "/grouperExternal/public/UiV2Public.postIndex"};
        
        try {
          String grouperUiFilterName = "GrouperUi";
          Class grouperUiFilterClass = Class.forName("edu.internet2.middleware.grouper.ui.GrouperUiFilter");
          Dynamic grouperUiFilter = context.addFilter(grouperUiFilterName, grouperUiFilterClass);
          grouperUiFilter.addMappingForUrlPatterns(null, false, "*.jsp");
          for (String urlPattern : urlPatterns) {
            grouperUiFilter.addMappingForUrlPatterns(null, false, urlPattern);
          }
          grouperUiFilter.addMappingForUrlPatterns(null, false, "/grouperUi/appHtml/*");

          String grouperUiCsrfFilterName = "CSRFGuard";
          Class grouperUiCsfrFilterClass = Class.forName("org.owasp.csrfguard.CsrfGuardFilter");
          Dynamic grouperUiCsrfFilter = context.addFilter(grouperUiCsrfFilterName, grouperUiCsfrFilterClass);
          //grouperUiCsrfFilter.addMappingForUrlPatterns(null, false, "/*");
          grouperUiCsrfFilter.addMappingForUrlPatterns(null, false, "/grouperExternal/public/OwaspJavaScriptServlet");
          for (String urlPattern : urlPatterns) {
            grouperUiCsrfFilter.addMappingForUrlPatterns(null, false, urlPattern);
          }
          grouperUiCsrfFilter.addMappingForUrlPatterns(null, false, "/grouperUi/appHtml/*");

          if (!StringUtils.isBlank(GrouperUiConfigInApi.retrieveConfig().propertyValueString("csrfguard.extraFilterPatterns"))) {
            for (String pattern : GrouperUtil.splitTrim(GrouperUiConfigInApi.retrieveConfig().propertyValueString("csrfguard.extraFilterPatterns"), ",")) {
              grouperUiCsrfFilter.addMappingForUrlPatterns(null, false, pattern);
            }
          }
          

          Class grouperSessionAttributeListener = Class.forName("edu.internet2.middleware.grouper.ui.GrouperSessionAttributeListener");
          context.addListener(grouperSessionAttributeListener);
          
          Class csrfGuardServletContextListener = Class.forName("org.owasp.csrfguard.CsrfGuardServletContextListener");
          context.addListener(csrfGuardServletContextListener);
          
          Class csrfGuardHttpSessionListener = Class.forName("org.owasp.csrfguard.CsrfGuardHttpSessionListener");
          context.addListener(csrfGuardHttpSessionListener);
          
          String uiServletName = "UiServlet";
          Class uiServletClass = Class.forName("edu.internet2.middleware.grouper.j2ee.GrouperUiRestServlet");
          javax.servlet.ServletRegistration.Dynamic uiServlet = context.addServlet(uiServletName, uiServletClass);
          for (String urlPattern : urlPatterns) {
            uiServlet.addMapping(urlPattern);
          }
          
          String owaspJavascriptServletName = "OwaspJavaScriptServlet";
          Class owaspJavascriptServletClass = Class.forName("org.owasp.csrfguard.servlet.JavaScriptServlet");
          javax.servlet.ServletRegistration.Dynamic owaspJavascriptServlet = context.addServlet(owaspJavascriptServletName, owaspJavascriptServletClass);
          owaspJavascriptServlet.addMapping("/grouperExternal/public/OwaspJavaScriptServlet");
        } catch (ClassNotFoundException e) {
          if (GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.dev.env.allowMissingServlets", true)) {
            LOG.info("you can't access grouper ui because required classes are not on the classpath.");
          } else {
            LOG.info("if you are developing and got this exception put grouper.dev.env.allowMissingServlets=true in config file grouper.properties.");
            throw new RuntimeException("required classes for grouper ui are not on the classpath", e);
          }
        }
        
      }
      
      if (runGrouperWs) {
        
        try {
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
          
          if (GrouperConfig.retrieveConfig().propertyValueBoolean("grouperWsAxisWssec", false)) {
            axisServlet.setInitParameter("wssec", "true");
          }
          
          String restServletName = "RestServlet";
          Class restServletClass = Class.forName("edu.internet2.middleware.grouper.ws.rest.GrouperRestServlet");
          javax.servlet.ServletRegistration.Dynamic restServlet = context.addServlet(restServletName, restServletClass);
          restServlet.addMapping("/servicesRest/*");
          restServlet.setLoadOnStartup(1);
        } catch (ClassNotFoundException e) {
          if (GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.dev.env.allowMissingServlets", true)) {
            LOG.info("you can't access grouper ws because required classes are not on the classpath.");
          } else {
            LOG.info("if you are developing and got this exception put grouper.dev.env.allowMissingServlets=true in config file grouper.properties.");
            throw new RuntimeException("required classes for grouper ws are not on the classpath", e);
          }
        }
        
      }
      
      if (runGrouperScim) {
        // logic to enable/disable filters, web listeners is in the grouper ws scim project itself. One eg. is RestApplication.java
      }
      
      if (runGrouperDaemon) {
        
        Thread thread = new Thread(new Runnable() {
          
          public void run() {
            try {
              GrouperLoader.main(new String[] {});
            } catch (RuntimeException e) {
              LOG.error("error in loader. " + e.getMessage());
            }
          }
        });
        thread.setDaemon(true);
        thread.start();
      }
      
  }

}
