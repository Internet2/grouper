/*******************************************************************************
 * Copyright 2014 Internet2
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
package edu.internet2.middleware.grouper.ui.util;

import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * hierarchical config class for grouper-ui.properties
 * @author mchyzer
 *
 */
public class GrouperUiConfigInApi extends ConfigPropertiesCascadeBase {

  /**
   * use the factory
   */
  protected GrouperUiConfigInApi() {
    
  }

  /**
   * retrieve a config from the config file or from cache
   * @return the config object
   */
  public static GrouperUiConfigInApi retrieveConfig() {
    return retrieveConfig(GrouperUiConfigInApi.class);
  }

  /** tests can say whether grouper-ui.properties is on the classpath, null means look */
  private static Boolean mainConfigFileOnClasspathForTesting = null;

  /**
   * for tests: pretend grouper-ui.properties is (true) or is not (false) on the classpath, or
   * null to look again
   * @param theMainConfigFileOnClasspath
   */
  public static void assignMainConfigFileOnClasspathForTesting(
      Boolean theMainConfigFileOnClasspath) {
    mainConfigFileOnClasspathForTesting = theMainConfigFileOnClasspath;
  }

  /**
   * whether the institution's grouper-ui.properties is on the classpath of this server.  without
   * it the UI config read here is only the base defaults and the database (a missing classpath
   * file is read as blank), so a value set in that file is not seen.  the UI has it; another
   * component, e.g. web services serving MCP, might not
   * @return true if it is on the classpath
   */
  public static boolean isMainConfigFileOnClasspath() {
    if (mainConfigFileOnClasspathForTesting != null) {
      return mainConfigFileOnClasspathForTesting;
    }
    return GrouperClientUtils.computeUrl(retrieveConfig().getMainConfigClasspath(), true) != null;
  }
  
  /**
   * @see ConfigPropertiesCascadeBase#clearCachedCalculatedValues()
   */
  @Override
  public void clearCachedCalculatedValues() {
    
  }

  /**
   * @see ConfigPropertiesCascadeBase#getHierarchyConfigKey
   */
  @Override
  protected String getHierarchyConfigKey() {
    return "grouperUi.config.hierarchy";
  }

  /**
   * @see ConfigPropertiesCascadeBase#getMainConfigClasspath
   */
  @Override
  protected String getMainConfigClasspath() {
    return "grouper-ui.properties";
  }
  
  /**
   * @see ConfigPropertiesCascadeBase#getMainExampleConfigClasspath
   */
  @Override
  protected String getMainExampleConfigClasspath() {
    return "grouper-ui-ng.base.properties";
  }

  /**
   * @see ConfigPropertiesCascadeBase#getSecondsToCheckConfigKey
   */
  @Override
  protected String getSecondsToCheckConfigKey() {
    return "grouperUi.config.secondsBetweenUpdateChecks";
  }

  
  

}
