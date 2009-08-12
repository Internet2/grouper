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

  private String id;

  private AccessResolver accessResolver;

  private Privilege privilege;

  public PrivilegeField(String id, AccessResolver accessResolver, Privilege privilege) {
    this.id = id;
    this.accessResolver = accessResolver;
    this.privilege = privilege;
  }

  public BaseAttribute<Subject> getAttribute(Group group) {

    Set<Subject> subjects = accessResolver.getSubjectsWithPrivilege(group, privilege);
    if (!subjects.isEmpty()) {
      BasicAttribute<Subject> attribute = new BasicAttribute<Subject>(id);
      attribute.setValues(subjects);
      return attribute;
    }

    return null;
  }

  public BaseAttribute<Group> getAttribute(Subject subject) {

    Set<Group> groups = accessResolver.getGroupsWhereSubjectHasPrivilege(subject, privilege);
    if (!groups.isEmpty()) {
      BasicAttribute<Group> attribute = new BasicAttribute<Group>(id);
      attribute.setValues(groups);
      return attribute;
    }

    return null;
  }

  public String getId() {
    return id;
  }
}
