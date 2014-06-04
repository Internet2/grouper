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
 * @author mchyzer
 * $Id: HooksLifecycleHibInitBean.java,v 1.2 2008-07-11 05:11:28 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import java.util.Set;

import org.hibernate.cfg.Configuration;

import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;



/**
 * <pre>
 * bean to hold objects for hibernate init (add mappings here).
 * To add a mapping, call configuration.addResource(path);
 * where path is something like this:  some/package/here/Mapping.hbm.xml
 * 
 * </pre>
 */
@GrouperIgnoreDbVersion
public class HooksLifecycleHibInitBean extends HooksBean {
  
  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: configuration */
  public static final String FIELD_CONFIGURATION = "configuration";

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_CONFIGURATION);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//
  
  /** hibernate configuration */
  private Configuration configuration = null;
  
  /**
   * 
   */
  public HooksLifecycleHibInitBean() {
    super();
  }


  /**
   * 
   * @param theConfiguration is the configuration
   */
  public HooksLifecycleHibInitBean(Configuration theConfiguration) {
    this.configuration = theConfiguration;
  }

  
  /**
   * hibernate configuration
   * @return the configuration
   */
  public Configuration getConfiguration() {
    return this.configuration;
  }
  
  /**
   * deep clone the fields in this object
   */
  @Override
  public HooksLifecycleHibInitBean clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }
}
