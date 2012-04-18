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
 * Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package edu.internet2.middleware.grouper.shibboleth.dataConnector.field;

import java.util.Set;
import java.util.TreeSet;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.shibboleth.common.attribute.BaseAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.provider.BasicAttribute;

/**
 * A representation of an attribute consisting of Members.
 */
public class MembersField extends BaseMembershipField {

  /** the first element of the identifier */
  public static final String NAME = "members";

  /**
   * @see edu.internet2.middleware.grouper.shibboleth.dataConnector.field.BaseField#constructor(String id)
   */
  public MembersField(String id) {
    super(id);
  }

  /**
   * Get the resultant attribute whose values are the {@link Member}s of the given {@link Group}.
   * 
   * @param group
   *          the group
   * @return the attribute consisting of Members or <tt>null</tt> if there are no members
   */
  public BaseAttribute<Member> getAttribute(Group group) {

    // FUTURE make sorting optional ?
    Set<Member> members = new TreeSet<Member>(this.getMemberFilter().getMembers(group, this.getField()));

    if (!members.isEmpty()) {
      BasicAttribute<Member> attribute = new BasicAttribute<Member>(this.getId());
      attribute.setValues(members);
      return attribute;
    }

    return null;
  }

}
