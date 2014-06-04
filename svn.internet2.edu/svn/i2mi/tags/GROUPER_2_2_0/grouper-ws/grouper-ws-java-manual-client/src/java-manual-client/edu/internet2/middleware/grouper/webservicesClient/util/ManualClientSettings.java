/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/*
 * @author mchyzer $Id: ManualClientSettings.java,v 1.4 2009-11-16 12:55:21 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.webservicesClient.util;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;


/**
 * generated client settings
 */
public class ManualClientSettings {
    /** properties for testing */
    static Properties properties = null;

    static {
        try {
            // create and load default properties
            properties = new Properties();

            InputStream in = ManualClientSettings.class.getResourceAsStream(
                    "/grouper-ws-manual.properties");
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

    /** user to login as */
    public static String HOST = properties.getProperty("ws.testing.host");

    /** user to login as */
    public static int PORT = Integer.parseInt(properties.getProperty("ws.testing.port"));

    /** url prefix before the service part with no slash: e.g. http://localhost:8093/grouper-ws */
    public static String URL = properties.getProperty("ws.testing.httpPrefix") +
        "://" + HOST 
        + (("443".equals(PORT) 
            || "80".equals(PORT)) ? "" 
                : (":" + PORT)) + "/" +
        properties.getProperty("ws.testing.appName");

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
        return ManualClientSettings.class.getClassLoader();
    }
    
    
}
