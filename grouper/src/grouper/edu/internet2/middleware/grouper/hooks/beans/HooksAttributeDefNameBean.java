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
 * $Id$
 */
package edu.internet2.middleware.grouper.hooks.beans;

import java.util.Set;

import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * bean to hold objects for attribute def name low level hooks
 */
@GrouperIgnoreDbVersion
public class HooksAttributeDefNameBean extends HooksBean {
  
  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: attributeDefName */
  public static final String FIELD_ATTRIBUTE_DEF_NAME = "attributeDefName";

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_ATTRIBUTE_DEF_NAME);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /** object being affected */
  private AttributeDefName attributeDefName = null;
  
  /**
   * 
   */
  public HooksAttributeDefNameBean() {
    super();
  }

  /**
   * @param theAttribute 
   */
  public HooksAttributeDefNameBean(AttributeDefName theAttribute) {
    this.attributeDefName = theAttribute;
  }
  
  /**
   * deep clone the fields in this object
   */
  @Override
  public HooksAttributeDefNameBean clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }

  /**
   * 
   * @return the attribute
   */
  public AttributeDefName getAttributeDefName() {
    return this.attributeDefName;
  }

}
