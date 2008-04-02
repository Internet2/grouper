/*
 * @author mchyzer $Id: GeneratedClientSettings.java,v 1.4 2008-04-02 08:11:17 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.webservicesClient.util;

import java.io.File;
import java.net.URL;


/**
 * generated client settings
 */
public class GeneratedClientSettings {
    /** client version.  keep this updated as the version changes */
    public static final String VERSION = "v1_3_000";

    /** user to login as */
    public static final String USER = "GrouperSystem";

    /** user to login as */
    public static final String PASS = "pass";

    /** url prefix */
    public static final String URL = "http://localhost:8093/grouper-ws/services/GrouperService";

    /**
     * make sure a array is non null.  If null, then return an empty array.
     * Note: this will probably not work for primitive arrays (e.g. int[])
     * @param <T>
     * @param array
     * @return the list or empty list if null
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] nonNull(T[] array) {
        return (array == null) ? ((T[]) new Object[0]) : array;
    }
    
    /**
     * get a file name from a resource name
     * 
     * @param resourceName
     *          is the classpath location
     * 
     * @return the file path on the system
     */
    public static File fileFromResourceName(String resourceName) {
      
      URL url = computeUrl(resourceName, true);

      if (url == null) {
        return null;
      }

      File configFile = new File(url.getFile());

      return configFile;
    }
    

    /**
     * compute a url of a resource
     * @param resourceName
     * @param canBeNull if cant be null, throw runtime
     * @return the URL
     */
    public static URL computeUrl(String resourceName, boolean canBeNull) {
      //get the url of the navigation file
      ClassLoader cl = classLoader();

      URL url = null;

      try {
        url = cl.getResource(resourceName);
      } catch (NullPointerException npe) {
        String error = "computeUrl() Could not find resource file: " + resourceName;
        throw new RuntimeException(error, npe);
      }

      if (!canBeNull && url == null) {
        throw new RuntimeException("Cant find resource: " + resourceName);
      }

      return url;
    }


    /**
     * fast class loader
     * @return the class loader
     */
    public static ClassLoader classLoader() {
      return GeneratedClientSettings.class.getClassLoader();
    }


    
}
