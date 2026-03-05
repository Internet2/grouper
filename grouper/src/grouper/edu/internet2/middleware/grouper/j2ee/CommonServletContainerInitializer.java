package edu.internet2.middleware.grouper.j2ee;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import edu.internet2.middleware.grouper.misc.GrouperStartup;
import org.apache.commons.lang3.StringUtils;
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
      GrouperStartup.startup();
      GrouperStartup.waitForGrouperStartup();

      // setup ServletContainerInitializer from OSGI
      // Note: OSGi classes are loaded lazily via a separate method to avoid ClassNotFoundException
      // when OSGi JARs are not on the classpath (they are provided scope)
      if (GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.osgi.enable", false)) {
        try {
          initOsgiServlets(arg0, context);
        } catch (NoClassDefFoundError e) {
          LOG.error("OSGi is enabled but OSGi classes are not on the classpath", e);
        }
      }

      boolean runGrouperUi = GrouperHibernateConfig.retrieveConfig().propertyValueBoolean("grouper.is.ui", false);

      boolean runMockServices = GrouperHibernateConfig.retrieveConfig().propertyValueBoolean("grouper.is.mockServices", false);

      boolean runGrouperWs = GrouperHibernateConfig.retrieveConfig().propertyValueBoolean("grouper.is.ws", false);

      boolean runGrouperWsSOAP = GrouperHibernateConfig.retrieveConfig().propertyValueBoolean("grouper.is.ws.soap", false);
      
      boolean runGrouperScim = GrouperHibernateConfig.retrieveConfig().propertyValueBoolean("grouper.is.scim", false);
      
      // MCP (Model Context Protocol) enables AI tools (Claude, Cursor, etc.) to interact with
      // Grouper via the MCP Streamable HTTP transport.  The MCP servlets are registered inside the
      // WS block below, so grouper.is.ws must also be true for the WS-side servlets.
      // The UI-side consent page and MCP info page (UiV2Mcp) also check this property
      // and throw an exception if MCP is not enabled.
      boolean runGrouperMcp = GrouperHibernateConfig.retrieveConfig().propertyValueBoolean("grouper.is.mcp", false);

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
          if (GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.dev.env.allowMissingServlets", false)) {
            LOG.error("You can't access grouper ui because required class is not on the classpath: " + e.getMessage(), e);
          } else {
            LOG.error("Required class for grouper ui is not on the classpath: " + e.getMessage()
                + ". If you are developing, put grouper.dev.env.allowMissingServlets=true in config file grouper.properties.", e);
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

          if (runGrouperWs && runGrouperWsSOAP) {
            grouperWsLoggingFilter.addMappingForUrlPatterns(null, false, "/services/*");
            grouperWsServiceFilter.addMappingForUrlPatterns(null, false, "/services/*");
          }
          
          grouperWsLoggingFilter.addMappingForUrlPatterns(null, false, "/servicesRest/*");
          grouperWsServiceFilter.addMappingForUrlPatterns(null, false, "/servicesRest/*");
          
          if (runGrouperScim) {
            grouperWsLoggingFilter.addMappingForUrlPatterns(null, false, "/scim/*");
            grouperWsServiceFilter.addMappingForUrlPatterns(null, false, "/scim/*");
          }

          Class grouperWsJ2eeListener = Class.forName("edu.internet2.middleware.grouper.ws.j2ee.GrouperJ2eeListener");
          context.addListener(grouperWsJ2eeListener);
          
          if (runGrouperWs && runGrouperWsSOAP) {
            String axisServletName = "AxisServlet";
            Class axisServletClass = Class.forName("edu.internet2.middleware.grouper.ws.GrouperServiceAxisServlet");
            javax.servlet.ServletRegistration.Dynamic axisServlet = context.addServlet(axisServletName, axisServletClass);
            axisServlet.addMapping("/services/*");
            axisServlet.setLoadOnStartup(1);
            
            if (GrouperConfig.retrieveConfig().propertyValueBoolean("grouperWsAxisWssec", false)) {
              axisServlet.setInitParameter("wssec", "true");
            }
          }
          
          String restServletName = "RestServlet";
          Class restServletClass = Class.forName("edu.internet2.middleware.grouper.ws.rest.GrouperRestServlet");
          javax.servlet.ServletRegistration.Dynamic restServlet = context.addServlet(restServletName, restServletClass);
          restServlet.addMapping("/servicesRest/*");
          restServlet.setLoadOnStartup(1);

          if (runGrouperScim) {
            String scimServletName = "SCIMRestServlet";
            Class scimServletClass = Class.forName("edu.internet2.middleware.grouper.ws.scim.GrouperScimServlet");
            javax.servlet.ServletRegistration.Dynamic scimServlet = context.addServlet(scimServletName, scimServletClass);
            scimServlet.addMapping("/scim/*");
            scimServlet.setInitParameter("jersey.config.server.provider.packages", "edu.internet2.middleware.grouper.ws.scim.providers");
            scimServlet.setLoadOnStartup(1);
          }

          // ---- MCP (Model Context Protocol) servlets ----
          // Enables AI assistants to interact with Grouper via the MCP Streamable HTTP transport.
          // Secured by OAuth 2.1 with PKCE; the user authenticates through the Grouper UI and
          // approves access on a consent page (UiV2OAuth.authorize in the UI module).
          // Five servlets are registered:
          //   1. GrouperMcpServlet        - main MCP JSON-RPC 2.0 endpoint (/mcp)
          //   2. GrouperOAuthServlet      - token exchange and dynamic client registration
          //   3. GrouperMcpProtectedResourceServlet - RFC 9728 resource metadata discovery
          //   4. GrouperMcpWellKnownServlet         - RFC 8414 authorization server metadata discovery
          if (runGrouperMcp) {

            // 1. Main MCP protocol endpoint - JSON-RPC 2.0 over HTTP (POST for messages, DELETE
            //    for session termination).  Requires a valid JWT bearer token on every request.
            //    This path IS behind the WS logging and service filters for request logging and
            //    Grouper session setup.
            String mcpServletName = "McpServlet";
            Class mcpServletClass = Class.forName("edu.internet2.middleware.grouper.ws.mcp.GrouperMcpServlet");
            javax.servlet.ServletRegistration.Dynamic mcpServlet = context.addServlet(mcpServletName, mcpServletClass);
            mcpServlet.addMapping("/mcp/*");
            mcpServlet.setLoadOnStartup(1);

            grouperWsLoggingFilter.addMappingForUrlPatterns(null, false, "/mcp/*");
            grouperWsServiceFilter.addMappingForUrlPatterns(null, false, "/mcp/*");

            // 2. OAuth 2.1 token and dynamic client registration endpoints.
            //    NOT behind the WS auth filter because they handle their own authentication:
            //    - POST /mcp/oauth/token    : exchanges auth code + PKCE code_verifier for a signed JWT
            //    - POST /mcp/oauth/register : RFC 7591 dynamic client registration (returns client_id)
            //    Also mapped at context-root /register and /token as RFC 8414 fallback paths
            //    for MCP clients that construct endpoint URLs relative to the issuer root.
            String oauthServletName = "OAuthServlet";
            Class oauthServletClass = Class.forName("edu.internet2.middleware.grouper.ws.mcp.GrouperOAuthServlet");
            javax.servlet.ServletRegistration.Dynamic oauthServlet = context.addServlet(oauthServletName, oauthServletClass);
            oauthServlet.addMapping("/mcp/oauth/*");
            oauthServlet.addMapping("/register");
            oauthServlet.addMapping("/token");

            // logging filter for the context-root fallback paths (/register, /token)
            // /mcp/oauth/* is already covered by the /mcp/* mapping above
            grouperWsLoggingFilter.addMappingForUrlPatterns(null, false, "/register");
            grouperWsLoggingFilter.addMappingForUrlPatterns(null, false, "/token");
            oauthServlet.setLoadOnStartup(1);

            // 3. Protected Resource Metadata (RFC 9728) - NOT behind the WS auth filter.
            //    This is the first discovery step: when an MCP client hits /mcp without a token
            //    it gets a 401 with a WWW-Authenticate header pointing to this endpoint.  The
            //    response tells the client which authorization server protects the MCP resource.
            String mcpProtectedResourceServletName = "McpProtectedResourceServlet";
            Class mcpProtectedResourceServletClass = Class.forName("edu.internet2.middleware.grouper.ws.mcp.GrouperMcpProtectedResourceServlet");
            javax.servlet.ServletRegistration.Dynamic mcpProtectedResourceServlet = context.addServlet(mcpProtectedResourceServletName, mcpProtectedResourceServletClass);
            mcpProtectedResourceServlet.addMapping("/.well-known/oauth-protected-resource");
            mcpProtectedResourceServlet.setLoadOnStartup(1);

            // 4. Authorization Server Metadata (RFC 8414) - NOT behind the WS auth filter.
            //    Second discovery step: the client fetches this to learn the authorization_endpoint,
            //    token_endpoint, registration_endpoint, and supported PKCE methods.
            //    Mapped to both oauth-authorization-server (RFC 8414) and openid-configuration
            //    (OIDC Discovery) because some MCP clients try the OIDC path as a fallback when
            //    the RFC 8414 path-based URL is outside the webapp context.
            String mcpWellKnownServletName = "McpWellKnownServlet";
            Class mcpWellKnownServletClass = Class.forName("edu.internet2.middleware.grouper.ws.mcp.GrouperMcpWellKnownServlet");
            javax.servlet.ServletRegistration.Dynamic mcpWellKnownServlet = context.addServlet(mcpWellKnownServletName, mcpWellKnownServletClass);
            mcpWellKnownServlet.addMapping("/.well-known/oauth-authorization-server");
            mcpWellKnownServlet.addMapping("/.well-known/openid-configuration");
            mcpWellKnownServlet.setLoadOnStartup(1);

            // logging filter for well-known discovery endpoints
            grouperWsLoggingFilter.addMappingForUrlPatterns(null, false, "/.well-known/oauth-protected-resource");
            grouperWsLoggingFilter.addMappingForUrlPatterns(null, false, "/.well-known/oauth-authorization-server");
            grouperWsLoggingFilter.addMappingForUrlPatterns(null, false, "/.well-known/openid-configuration");
          }

        } catch (ClassNotFoundException e) {
          if (GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.dev.env.allowMissingServlets", false)) {
            LOG.error("You can't access grouper ws because required class is not on the classpath: " + e.getMessage(), e);
          } else {
            LOG.error("Required class for grouper ws is not on the classpath: " + e.getMessage()
                + ". If you are developing, put grouper.dev.env.allowMissingServlets=true in config file grouper.properties.", e);
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

  /**
   * Initialize servlet container initializers from OSGi bundles.
   * This method is in a separate method so that OSGi classes (BundleContext, InvalidSyntaxException)
   * are only resolved by the JVM when this method is actually called, not when the enclosing
   * class is loaded. This prevents ClassNotFoundException when OSGi JARs are not on the classpath.
   * @param classes the classes
   * @param context the servlet context
   * @throws ServletException
   */
  private void initOsgiServlets(Set<Class<?>> classes, ServletContext context) throws ServletException {
    org.osgi.framework.BundleContext bundleContext = edu.internet2.middleware.grouper.plugins.FrameworkStarter.getInstance()
        .getFramework().getBundleContext();
    try {
      Collection<ServletContainerInitializer> initializerCollection = edu.internet2.middleware.grouper.plugins.FrameworkStarter.getInstance()
          .getFramework().getBundleContext()
          .getServiceReferences(ServletContainerInitializer.class, null)
          .stream()
          .map(r -> bundleContext.getService(r))
          .collect(Collectors.toList());
      initializerCollection.stream().forEach(r -> {
        try {
          r.onStartup(classes, context);
        } catch (ServletException e) {
          throw new RuntimeException(e);
        }
      });
    } catch (org.osgi.framework.InvalidSyntaxException e) {
      throw new RuntimeException(e);
    }
  }
}
