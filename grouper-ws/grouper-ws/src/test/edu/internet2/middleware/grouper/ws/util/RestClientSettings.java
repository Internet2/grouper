/*
 * @author mchyzer $Id: RestClientSettings.java,v 1.2.2.1 2008-09-04 05:43:30 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.util;

import java.io.InputStream;
import java.io.StringWriter;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.io.IOUtils;

import edu.internet2.middleware.grouper.ws.GrouperWsConfig;


/**
 * rest client settings
 */
public class RestClientSettings {
    /** client version.  keep this updated as the version changes */
    public static final String VERSION = GrouperWsConfig.getPropertyString("ws.testing.version");

    /** user to login as */
    public static final String USER = GrouperWsConfig.getPropertyString("ws.testing.user");

    /** user to login as */
    public static final String PASS = GrouperWsConfig.getPropertyString("ws.testing.pass");

    /** port for auth settings */
    public static final int PORT = Integer.parseInt(GrouperWsConfig.getPropertyString("ws.testing.port"));
    
    /** host for auth settings */
    public static final String HOST = GrouperWsConfig.getPropertyString("ws.testing.host");
    
    /** url prefix */
    public static final String URL = GrouperWsConfig.getPropertyString("ws.testing.httpPrefix") + 
      "://" + GrouperWsConfig.getPropertyString("ws.testing.host") + ":" 
      + GrouperWsConfig.getPropertyString("ws.testing.port") + "/" 
      + GrouperWsConfig.getPropertyString("ws.testing.appName") 
      + "/servicesRest";
    
    /**
     * for testing, get the response body as a string
     * @param method
     * @return the string of response body
     */
    public static String responseBodyAsString(HttpMethodBase method) {
      InputStream inputStream = null;
      try {
        
        StringWriter writer = new StringWriter();
        inputStream = method.getResponseBodyAsStream();
        IOUtils.copy(inputStream, writer);
        return writer.toString();
      } catch (Exception e) {
        throw new RuntimeException(e);
      } finally {
        IOUtils.closeQuietly(inputStream);
      }
      
    }
}
