/**
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
 */
package edu.internet2.middleware.grouperClient.util;

import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;

/**
 * hierarchical config class for grouper.client.properties
 * @author mchyzer
 *
 */
public class GrouperClientConfig extends ConfigPropertiesCascadeBase {

  /**
   * use the factory
   */
  private GrouperClientConfig() {
    
  }
  
  /**
   * retrieve a config from the config file or from cache
   * @return the config object
   */
  public static GrouperClientConfig retrieveConfig() {
    return retrieveConfig(GrouperClientConfig.class);
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
    return "grouperClient.config.hierarchy";
  }

  /**
   * @see ConfigPropertiesCascadeBase#getMainConfigClasspath
   */
  @Override
  protected String getMainConfigClasspath() {
    return "grouper.client.properties";
  }
  
  /**
   * @see ConfigPropertiesCascadeBase#getMainExampleConfigClasspath
   */
  @Override
  protected String getMainExampleConfigClasspath() {
    return "grouper.client.base.properties";
  }

  /**
   * @see ConfigPropertiesCascadeBase#getSecondsToCheckConfigKey
   */
  @Override
  protected String getSecondsToCheckConfigKey() {
    return "grouperClient.config.secondsBetweenUpdateChecks";
  }

  /**
   * @see edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase#getConfigClassInJarToLookForConfigFiles()
   */
  @Override
  protected Class<?> getClassInSiblingJar() {
    return GrouperClientCommonUtils.class;
  }
  
}
