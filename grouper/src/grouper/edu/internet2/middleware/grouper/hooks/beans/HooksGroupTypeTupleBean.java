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
 * $Id: HooksGroupTypeTupleBean.java,v 1.2 2008-07-11 05:11:28 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import java.util.Set;

import edu.internet2.middleware.grouper.GroupTypeTuple;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * bean to hold objects for group low level hooks
 */
@GrouperIgnoreDbVersion
public class HooksGroupTypeTupleBean extends HooksBean {
  
  /** object being affected */
  private GroupTypeTuple groupTypeTuple = null;
  
  /**
   * @param theGroupTypeTuple
   */
  public HooksGroupTypeTupleBean(GroupTypeTuple theGroupTypeTuple) {
    this.groupTypeTuple = theGroupTypeTuple;
  }
  
  /**
   * 
   */
  public HooksGroupTypeTupleBean() {
    super();
  }

  /**
   * object being inserted
   * @return the Group
   */
  public GroupTypeTuple getGroupTypeTuple() {
    return this.groupTypeTuple;
  }

  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: groupTypeTuple */
  public static final String FIELD_GROUP_TYPE_TUPLE = "groupTypeTuple";

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_GROUP_TYPE_TUPLE);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /**
   * deep clone the fields in this object
   */
  @Override
  public HooksGroupTypeTupleBean clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }
}
