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
 * @author mchyzer
 * $Id: HooksAttributeDefBean.java 6921 2010-08-10 21:03:10Z mchyzer $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import java.util.Set;

import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * bean to hold objects for attribute def name low level hooks
 */
@GrouperIgnoreDbVersion
public class HooksAttributeDefBean extends HooksBean {
  
  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: attributeDef */
  public static final String FIELD_ATTRIBUTE_DEF = "attributeDef";

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_ATTRIBUTE_DEF);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /** object being affected */
  private AttributeDef attributeDef = null;
  
  /**
   * 
   */
  public HooksAttributeDefBean() {
    super();
  }

  /**
   * @param theAttribute 
   */
  public HooksAttributeDefBean(AttributeDef theAttribute) {
    this.attributeDef = theAttribute;
  }
  
  /**
   * deep clone the fields in this object
   */
  @Override
  public HooksAttributeDefBean clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }

  /**
   * 
   * @return the attribute
   */
  public AttributeDef getAttributeDef() {
    return this.attributeDef;
  }

}
