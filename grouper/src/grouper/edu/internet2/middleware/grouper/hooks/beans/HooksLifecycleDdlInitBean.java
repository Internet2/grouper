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
/*
 * @author mchyzer
 * $Id: HooksLifecycleDdlInitBean.java,v 1.1 2008-07-27 07:37:24 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import java.util.List;

import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;



/**
 * <pre>
 * bean to hold objects for ddl init (add ddl object names here).
 * 
 * </pre>
 */
@GrouperIgnoreDbVersion
public class HooksLifecycleDdlInitBean extends HooksBean {

  /** hibernate configuration */
  private List<String> ddlObjectNames = null;
  
  /**
   * 
   */
  public HooksLifecycleDdlInitBean() {
    super();
  }


  /**
   * @param theDdlObjectNames is the configuration
   */
  public HooksLifecycleDdlInitBean(List<String> theDdlObjectNames) {
    this.ddlObjectNames = theDdlObjectNames;
  }

  
  
  /**
   * @return the ddlObjectNames
   */
  public List<String> getDdlObjectNames() {
    return this.ddlObjectNames;
  }


  /**
   * deep clone the fields in this object
   */
  @Override
  public HooksLifecycleDdlInitBean clone() {
    throw new RuntimeException("You cant clone this bean: " + HooksLifecycleDdlInitBean.class.getName());
  }
}
