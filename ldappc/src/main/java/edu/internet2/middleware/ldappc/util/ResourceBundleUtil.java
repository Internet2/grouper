/*
  Copyright 2006-2007 The University Of Chicago
  Copyright 2006-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright 2006-2007 EDUCAUSE
  
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
  
      http://www.apache.org/licenses/LICENSE-2.0
  
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */

package edu.internet2.middleware.ldappc.util;

import java.util.ResourceBundle;
import java.util.Locale;
import java.util.MissingResourceException;

import edu.internet2.middleware.ldappc.logging.ErrorLog;

/**
 * A utility for accessing data in a ResourceBundle.
 * 
 * @author Gil Singer
 */
public final class ResourceBundleUtil
{

    /**
     * The ldappc properties file name used for test data.
     */
    private static final String   LDAPPC_PROPERTIES_FILE = "ldappc";

    /**
     * The resource bundle.
     */
    private static ResourceBundle bundle;

    static
    {
        Locale currentLocale;
        ResourceBundle messages;
        currentLocale = Locale.getDefault();
        bundle = ResourceBundle.getBundle(LDAPPC_PROPERTIES_FILE, currentLocale);
    }

    /**
     * Prevent instantiation.
     */
    private ResourceBundleUtil()
    {
    }

    /**
     * This method gets a String value from the message resource bundle. This
     * method handles all key exceptions and creates a log message in case of an
     * error.
     * 
     * @param key
     *            String identifying the key to the object searched for in the
     *            resource bundle.
     * @return String value stored in the class resource bundle.
     */
    public static String getString(String key)
    {
        String stringValue = "ResourceBundleUtil ERROR, could not find value for key: " + key;

        try
        {
            stringValue = bundle.getString(key);
        }
        catch (MissingResourceException mre)
        {
            ErrorLog.error(ResourceBundleUtil.class, "MissingResourceException: " + mre.getMessage());
        }
        catch (ClassCastException cce)
        {
            ErrorLog.error(ResourceBundleUtil.class, "ClassCastException: " + cce.getMessage());
        }
        catch (NullPointerException npe)
        {
            ErrorLog.error(ResourceBundleUtil.class, "null pointer exception: " + npe.getMessage());
        }
        return stringValue;
    }
}
