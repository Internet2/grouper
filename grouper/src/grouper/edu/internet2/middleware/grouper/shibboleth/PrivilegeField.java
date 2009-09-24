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

package edu.internet2.middleware.grouper.shibboleth;

import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.privs.AccessResolver;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.shibboleth.common.attribute.BaseAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.provider.BasicAttribute;
import edu.internet2.middleware.subject.Subject;

public class PrivilegeField {

  /** the attribute name */
  private String id;

  /** the access resolver */
  private AccessResolver accessResolver;

  /** the underlying privilege */
  private Privilege privilege;

  /**
   * Constructor.
   * 
   * @param id
   *          the attribute name
   * @param accessResolver
   *          the access resolver
   * @param privilege
   *          the privilege
   */
  public PrivilegeField(String id, AccessResolver accessResolver, Privilege privilege) {
    this.id = id;
    this.accessResolver = accessResolver;
    this.privilege = privilege;
  }

  /**
   * Get the resultant attribute whose values are the Subjects with the privilege for the
   * given Group.
   * 
   * @param group
   *          the group
   * @return the attribute consisting of Subjects or <tt>null</tt> if there are no
   *         subjects with the privilege
   */
  public BaseAttribute<Subject> getAttribute(Group group) {
    Set<Subject> subjects = accessResolver.getSubjectsWithPrivilege(group, privilege);
    if (!subjects.isEmpty()) {
      BasicAttribute<Subject> attribute = new BasicAttribute<Subject>(id);
      attribute.setValues(subjects);
      return attribute;
    }

    return null;
  }

  /**
   * Get the resultant attribute whose values are the Groups to which the subject has the
   * privilege.
   * 
   * @param subject
   *          the subject
   * @return the attribute consisting of Groups or <tt>null</tt> if there are no groups to
   *         which the subject has the privilege
   */
  public BaseAttribute<Group> getAttribute(Subject subject) {
    Set<Group> groups = accessResolver.getGroupsWhereSubjectHasPrivilege(subject, privilege);
    if (!groups.isEmpty()) {
      BasicAttribute<Group> attribute = new BasicAttribute<Group>(id);
      attribute.setValues(groups);
      return attribute;
    }

    return null;
  }

  /**
   * Get the attribute id.
   * 
   * @return the name of the underlying attribute
   */
  public String getId() {
    return id;
  }
}
