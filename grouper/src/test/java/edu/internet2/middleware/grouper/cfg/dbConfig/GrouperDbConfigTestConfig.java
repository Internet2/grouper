/**
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
 */
/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

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

package edu.internet2.middleware.grouper.cfg.dbConfig;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;


/** 
 * Grouper configuration information.
 * <p><b>This class is being deprecated by the {@link edu.internet2.middleware.grouper.cfg.Configuration} interface.</b></p>
 * @author  blair christensen.
 * @version $Id: GrouperConfig.java,v 1.9 2009-12-16 06:02:30 mchyzer Exp $
 * @since   ?
 */
public class GrouperDbConfigTestConfig extends ConfigPropertiesCascadeBase {

  /**
   * use the factory
   */
  private GrouperDbConfigTestConfig() {
    
  }

  /**
   * retrieve a config from the config file or from cache
   * @return the config object
   */
  public static GrouperDbConfigTestConfig retrieveConfig() {
    return retrieveConfig(GrouperDbConfigTestConfig.class);
  }


  /**
   * @see ConfigPropertiesCascadeBase#getHierarchyConfigKey
   */
  @Override
  protected String getHierarchyConfigKey() {
    return "grouper.dbConfigTest.config.hierarchy";
  }

  /**
   * @see ConfigPropertiesCascadeBase#getMainConfigClasspath
   */
  @Override
  public String getMainConfigClasspath() {
    return "edu/internet2/middleware/grouper/cfg/dbConfig/grouper.dbConfigTest.properties";
  }

  /**
   * @see ConfigPropertiesCascadeBase#getMainExampleConfigClasspath
   */
  @Override
  protected String getMainExampleConfigClasspath() {
    return "edu/internet2/middleware/grouper/cfg/dbConfig/grouper.dbConfigTest.base.properties";
  }

  /**
   * @see ConfigPropertiesCascadeBase#getSecondsToCheckConfigKey
   */
  @Override
  protected String getSecondsToCheckConfigKey() {
    return "grouper.dbConfigTest.config.secondsBetweenUpdateChecks";
  }

  @Override
  public void clearCachedCalculatedValues() {
    
  }
  

  
} 
