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
/**
 * @author mchyzer
 * $Id: HooksMembershipChangeBean.java,v 1.5 2009-08-18 23:11:38 shilen Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import java.util.Set;

import edu.internet2.middleware.grouper.Composite;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * pre/post update bean for high level membership change (the main change, not
 * the side effects like adding the member to the groups where the group
 * to be added to is a member)
 */
@GrouperIgnoreDbVersion
public class HooksMembershipChangeBean extends HooksBean {

  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: ms */
  public static final String FIELD_MS = "ms";

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_MS);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /** object being affected */
  private Membership ms = null;
  
  
  /**
   * 
   */
  public HooksMembershipChangeBean() {
    super();
  }

  /**
   * @param ms 
   */
  public HooksMembershipChangeBean(Membership ms) {
    this.ms = ms;
  }

  /**
   * composite if applicable
   * @return the composite
   */
  public Composite getComposite() {
    return this.ms.getViaComposite();
  }

  /**
   * field for membership
   * @return the field
   */
  public Field getField() {
    return this.ms.getList();
  }

  /**
   * group for membership
   * @return the group
   */
  public Group getGroup() {
    return this.ms.getOwnerGroup();
  }

  /**
   * member being assigned
   * @return the member
   */
  public Member getMember() {
    return this.ms.getMember();
  }

  /**
   * membership dto
   * @return the membership
   */
  public Membership getMembership() {
    return this.ms;
  }

  /**
   * stem
   * @return the stem
   */
  public Stem getStem() {
    return this.ms.getStem();
  }

  /**
   * deep clone the fields in this object
   */
  @Override
  public HooksMembershipChangeBean clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }
}
