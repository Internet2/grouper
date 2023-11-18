/*******************************************************************************
 * Copyright 2012 Internet2
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
 ******************************************************************************/
/**
 * 
 */
package edu.internet2.middleware.grouper.ws;

import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;

/**
 * config constants for WS
 * 
 * @author mchyzer
 * 
 */
public final class GrouperWsConfigInApi extends ConfigPropertiesCascadeBase {

  /**
   * use the factory
   */
  private GrouperWsConfigInApi() {

  }

  /**
   * retrieve a config from the config file or from cache
   * @return the config object
   */
  public static GrouperWsConfigInApi retrieveConfig() {
    return retrieveConfig(GrouperWsConfigInApi.class);
  }

  /**
   * @see ConfigPropertiesCascadeBase#clearCachedCalculatedValues
   */
  @Override
  public void clearCachedCalculatedValues() {
    //nothing to do at this point
  }

  /**
   * @see ConfigPropertiesCascadeBase#getHierarchyConfigKey
   */
  @Override
  protected String getHierarchyConfigKey() {
    return "ws.config.hierarchy";
  }

  /**
   * @see ConfigPropertiesCascadeBase#getMainConfigClasspath
   */
  @Override
  protected String getMainConfigClasspath() {
    return "grouper-ws.properties";
  }

  /**
   * @see ConfigPropertiesCascadeBase#getMainExampleConfigClasspath
   */
  @Override
  protected String getMainExampleConfigClasspath() {
    return "grouper-ws-ng.base.properties";
  }

  /**
   * @see ConfigPropertiesCascadeBase#getSecondsToCheckConfigKey
   */
  @Override
  protected String getSecondsToCheckConfigKey() {
    return "ws.config.secondsBetweenUpdateChecks";
  }

}
