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
 * Copyright 2007 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.internet2.middleware.grouperKimConnector.xsl;

import java.util.Properties;

import org.apache.log4j.Logger;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;
import edu.internet2.middleware.grouperKimConnector.identity.GrouperKimIdentityServiceImpl;


/**
 * Custom workflow functions to get text from resource
 * 
 * @author mchyzer
 *
 */
public class XslUtils {

  /**
   * logger
   */
  private static final Logger LOG = Logger.getLogger(GrouperKimIdentityServiceImpl.class);

  /**
   * This method retrieves properties from a resource on classpath,
   * and dereferences a key from them
   * 
   * @param resourceName
   * @param key
   * @return the value
   */
  public static String propertyValue(String resourceName, String key) {
    try {
      Properties properties = GrouperClientUtils.propertiesFromResourceName(resourceName, true, true, null, null);
      return properties.getProperty(key);
    } catch (RuntimeException re) {
      LOG.error("Problem with resourceName: " + resourceName + ", and key: " + key, re);
      throw re;
    }
  }


}
