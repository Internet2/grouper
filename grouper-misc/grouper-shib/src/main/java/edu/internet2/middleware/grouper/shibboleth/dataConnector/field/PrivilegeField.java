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

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AccessResolver;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.shibboleth.common.attribute.BaseAttribute;
import edu.internet2.middleware.shibboleth.common.attribute.provider.BasicAttribute;
import edu.internet2.middleware.subject.Subject;

public class PrivilegeField extends BaseField {

  /** the access resolver */
  private AccessResolver accessResolver;

  /** the underlying privilege */
  private Privilege privilege;

  /**
   * Construct a representation of an {@link Privilege} attribute.
   * 
   * @see edu.internet2.middleware.grouper.shibboleth.dataConnector.field.BaseField#constructor(String id)
   * 
   * @param id
   *          the identifier
   * @param accessResolver
   *          the access resolver
   * @throws GrouperException
   * 
   */
  public PrivilegeField(String id, AccessResolver accessResolver) throws GrouperException {
    super(id);
    this.accessResolver = accessResolver;
    try {
      Field field = FieldFinder.find(id, true);
      if (!field.getType().equals(FieldType.ACCESS)) {
        throw new GrouperException("Field '" + id + "' is not an access privilege");
      }
      privilege = AccessPrivilege.listToPriv(id);
      if (privilege == null) {
        throw new GrouperException("Unknown access privilege '" + id + "'");
      }
    } catch (SchemaException e) {
      throw new GrouperException("Unknown field '" + id + "'", e);
    }
  }

  /**
   * Get the resultant attribute whose values are the {@link Subject}s with the privilege for the given {@link Group}.
   * 
   * Does not include the GrouperAll or GrouperSystem subjects.
   * 
   * @param group
   *          the group
   * @return the attribute consisting of Subjects or <tt>null</tt> if there are no subjects with the privilege
   */
  public BaseAttribute<Subject> getAttribute(Group group) {
    Set<Subject> subjects = accessResolver.getSubjectsWithPrivilege(group, privilege);
    filterInternalSubjects(subjects);
    if (!subjects.isEmpty()) {
      BasicAttribute<Subject> attribute = new BasicAttribute<Subject>(this.getId());
      attribute.setValues(subjects);
      return attribute;
    }

    return null;
  }

  /**
   * Get the resultant attribute whose values are the {@link Group}s to which the {@link Subject} has the privilege.
   * 
   * @param subject
   *          the subject
   * @return the attribute consisting of Groups or <tt>null</tt> if there are no groups to which the subject has the
   *         privilege
   */
  public BaseAttribute<Group> getAttribute(Subject subject) {
    Set<Group> groups = accessResolver.getGroupsWhereSubjectHasPrivilege(subject, privilege);
    if (!groups.isEmpty()) {
      BasicAttribute<Group> attribute = new BasicAttribute<Group>(this.getId());
      attribute.setValues(groups);
      return attribute;
    }

    return null;
  }

  /**
   * Remove GrouperAll and GrouperSystem from the returned subjects.
   * 
   * @param subjects
   * @return the filtered set of subjects
   */
  public Set<Subject> filterInternalSubjects(Set<Subject> subjects) {

    // filter GrouperSystem
    if (subjects.contains(SubjectFinder.findRootSubject())) {
      subjects.remove(SubjectFinder.findRootSubject());
    }

    // filter GrouperAll
    if (subjects.contains(SubjectFinder.findAllSubject())) {
      subjects.remove(SubjectFinder.findAllSubject());
    }

    return subjects;
  }

}
