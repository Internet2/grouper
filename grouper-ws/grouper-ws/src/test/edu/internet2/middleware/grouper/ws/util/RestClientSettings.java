/*
 * @author mchyzer $Id: RestClientSettings.java,v 1.1 2008-03-24 20:19:49 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.util;


/**
 * rest client settings
 */
public class RestClientSettings {
    /** client version.  keep this updated as the version changes */
    public static final String VERSION = "v1_3_000";

    /** user to login as */
    public static final String USER = "GrouperSystem";

    /** user to login as */
    public static final String PASS = "pass";

    /** port for auth settings */
    public static final int PORT = 8093;
    
    /** host for auth settings */
    public static final String HOST = "localhost";
    
    /** url prefix */
    public static final String URL = "http://localhost:8093/grouper-ws/servicesLite";
}
