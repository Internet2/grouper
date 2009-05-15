/*
 * Copyright 2006-2007 The University Of Chicago Copyright 2006-2007 University
 * Corporation for Advanced Internet Development, Inc. Copyright 2006-2007 EDUCAUSE
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package edu.internet2.middleware.ldappc.util;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.slf4j.Logger;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * A utility for accessing data in a ResourceBundle.
 * 
 * @author Gil Singer
 */
public final class ResourceBundleUtil {

  private static final Logger LOG = GrouperUtil.getLogger(ResourceBundleUtil.class);

  /**
   * The ldappc properties file name used for test data.
   */
  private static final String LDAPPC_PROPERTIES_FILE = "ldappc";

  /**
   * The resource bundle.
   */
  private static ResourceBundle bundle;

  static {
    Locale currentLocale;
    currentLocale = Locale.getDefault();
    bundle = ResourceBundle.getBundle(LDAPPC_PROPERTIES_FILE, currentLocale);
  }

  /**
   * Prevent instantiation.
   */
  private ResourceBundleUtil() {
  }

  /**
   * This method gets a String value from the message resource bundle. This method handles
   * all key exceptions and creates a log message in case of an error.
   * 
   * @param key
   *          String identifying the key to the object searched for in the resource
   *          bundle.
   * @return String value stored in the class resource bundle.
   */
  public static String getString(String key) {
    String stringValue = "ResourceBundleUtil ERROR, could not find value for key: " + key;

    try {
      stringValue = bundle.getString(key);
    } catch (MissingResourceException mre) {
      LOG.error("An error occurred.", mre);
    } catch (ClassCastException cce) {
      LOG.error("An error occurred.", cce);
    } catch (NullPointerException npe) {
      LOG.error("An error occurred.", npe);
    }
    return stringValue;
  }
}
