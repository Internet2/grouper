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
 * $Id: HooksFieldBean.java,v 1.2 2008-07-11 05:11:28 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import java.util.Set;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * bean to hold objects for field low level hooks
 */
@GrouperIgnoreDbVersion
public class HooksFieldBean extends HooksBean {
  
  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: field */
  public static final String FIELD_FIELD = "field";

  /**
   * fields which are included in db version
   */
  private static final Set<String> DB_VERSION_FIELDS = GrouperUtil.toSet(
      FIELD_FIELD);

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_FIELD);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /** object being affected */
  private Field field = null;
  
  /**
   * 
   */
  public HooksFieldBean() {
    super();
  }

  /**
   * @param theField
   */
  public HooksFieldBean(Field theField) {
    this.field = theField;
  }
  
  /**
   * object being inserted
   * @return the Field
   */
  public Field getField() {
    return this.field;
  }

  /**
   * deep clone the fields in this object
   */
  @Override
  public HooksFieldBean clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }

}
