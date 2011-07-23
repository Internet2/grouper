/*
 * @author mchyzer $Id: GeneratedClientSettings.java,v 1.7 2009-04-13 03:03:12 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.webservicesClient.util;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;


/**
 * generated client settings
 */
public class GeneratedClientSettings {
    /** properties for testing */
    static Properties properties = null;

    static {
        try {
            // create and load default properties
            properties = new Properties();

            InputStream in = GeneratedClientSettings.class.getResourceAsStream(
                    "/grouper-ws-generated.properties");
            properties.load(in);
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /** client version.  keep this updated as the version changes */
    public static String VERSION = properties.getProperty("ws.testing.version");

    /** user to login as */
    public static String USER = properties.getProperty("ws.testing.user");

    /** user to login as */
    public static String PASS = properties.getProperty("ws.testing.pass");

    /** url prefix */
    public static String URL = properties.getProperty("ws.testing.httpPrefix") +
        "://" + properties.getProperty("ws.testing.host") 
        + (("443".equals(properties.getProperty("ws.testing.port")) 
            || "80".equals(properties.getProperty("ws.testing.port"))) ? "" 
                : (":" + properties.getProperty("ws.testing.port"))) + "/" +
        properties.getProperty("ws.testing.appName") +
        "/services/" + properties.getProperty("ws.testing.endpoint");

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
            String error = "computeUrl() Could not find resource file: " +
                resourceName;
            throw new RuntimeException(error, npe);
        }

        if (!canBeNull && (url == null)) {
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
