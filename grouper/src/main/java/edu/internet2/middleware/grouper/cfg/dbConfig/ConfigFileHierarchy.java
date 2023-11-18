/**
 * Copyright 2018 Internet2
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

package edu.internet2.middleware.grouper.cfg.dbConfig;

import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * Note, this class is not used
 */
public enum ConfigFileHierarchy {

  /**
   * the base config that ships with grouper
   */
  BASE(100), 
  
  /**
   * the config at your institution that spans across all envs
   */
  INSTITUTION(1000), 
  
  /**
   * config for the environment at your institution, dev, test, prod whatever
   */
  ENVIRONMENT(10000), 
  
  /**
   * if configs differ between UI, WS, etc put that here
   */
  GROUPER_ENGINE(100000);
  
  /**
   * order the config gets loaded
   * the lower the number the first it is read
   */
  private int order;
  
  /**
   * construct
   * @param theOrder
   */
  private ConfigFileHierarchy(int theOrder) {
    this.order = theOrder;
  }
  
  /**
   * order the config gets loaded
   * the lower the number the first it is read
   * @return the order
   */
  public int getOrder() {
    return this.order;
  }
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static ConfigFileHierarchy valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperUtil.enumValueOfIgnoreCase(ConfigFileHierarchy.class, 
        string, exceptionOnNull);

  }

}
