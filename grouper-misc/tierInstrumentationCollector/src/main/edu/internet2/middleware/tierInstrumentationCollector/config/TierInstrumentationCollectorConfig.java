/*******************************************************************************
 * Copyright 2016 Internet2
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
 *******************************************************************************/
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.tierInstrumentationCollector.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;


/**
 * configuration for a specific client of the service
 */
public class TierInstrumentationCollectorConfig extends ConfigPropertiesCascadeBase {

  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = LogFactory.getLog(TierInstrumentationCollectorConfig.class);

  /**
   * @see edu.internet2.middleware.tierInstrumentationCollector.config.TicConfigPropertiesCascadeBase#clearCachedCalculatedValues()
   */
  @Override
  public void clearCachedCalculatedValues() {
  }


  /**
   * @see ConfigPropertiesCascadeBase#getHierarchyConfigKey
   */
  @Override
  protected String getHierarchyConfigKey() {
    return "tierInstrumentationCollector.config.hierarchy";
  }

  /**
   * @see ConfigPropertiesCascadeBase#getMainConfigClasspath
   */
  @Override
  protected String getMainConfigClasspath() {
    return "tierInstrumentationCollector.properties";
  }

  /**
   * @see ConfigPropertiesCascadeBase#getMainExampleConfigClasspath
   */
  @Override
  protected String getMainExampleConfigClasspath() {
    return "tierInstrumentationCollector.base.properties";
  }

  /**
   * @see ConfigPropertiesCascadeBase#getSecondsToCheckConfigKey
   */
  @Override
  protected String getSecondsToCheckConfigKey() {
    return "tierInstrumentationCollector.config.secondsBetweenUpdateChecks";
  }

  /**
   * retrieve a config from the config file or from cache
   * @return the config object
   */
  public static TierInstrumentationCollectorConfig retrieveConfig() {
    return retrieveConfig(TierInstrumentationCollectorConfig.class);
  }

  
}
