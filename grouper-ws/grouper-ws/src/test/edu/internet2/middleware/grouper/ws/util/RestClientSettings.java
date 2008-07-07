/*
 * @author mchyzer $Id: RestClientSettings.java,v 1.3 2008-07-07 06:26:20 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.util;

import java.io.InputStream;
import java.io.StringWriter;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.io.IOUtils;


/**
 * rest client settings
 */
public class RestClientSettings {
    /** client version.  keep this updated as the version changes */
    public static final String VERSION = "v1_3_000";

    /** user to login as */
    public static final String USER = "mchyzer";

    /** user to login as */
    public static final String PASS = "1234";

    /** port for auth settings */
    public static final int PORT = 8090;
    
    /** host for auth settings */
    public static final String HOST = "localhost";
    
    /** url prefix */
    public static final String URL = "http://localhost:8090/grouperWs/servicesRest";
    
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
