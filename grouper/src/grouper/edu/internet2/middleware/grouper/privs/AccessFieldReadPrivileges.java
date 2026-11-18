/**
 * Copyright 2026 Internet2
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
package edu.internet2.middleware.grouper.privs;

import java.util.Collection;
import java.util.Set;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.service.ServiceRole;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.subject.Subject;

/**
 * <p>Which group privilege a caller needs to read rows of the group privilege lists (admins,
 * updaters, readers, viewers, optins, optouts, groupAttrReaders, groupAttrUpdaters) in the bulk
 * membership queries.</p>
 *
 * <p>The privilege lists are registered with read privilege ADMIN, and PrivilegeHelper.canViewMembership
 * enforces that for single checks.  The bulk queries (Hib3MembershipDAO.findAllByGroupOwnerOptions,
 * Hib3GroupDAO / Hib3PITGroupDAO.getAllGroupsMembershipSecure, the service role queries in
 * Hib3AttributeDefNameDAO) picked READ unless the FIRST field passed in was an access field, so a
 * caller with READ on a group could list who administers or updates it by passing no field with
 * fieldType access, a serviceRole of admin, or an access field to "groups where subject X is in field F".</p>
 *
 * <p>Rows about the caller itself are not restricted further: a caller may always learn its own
 * privileges, and those queries keep the privilege set they had before.</p>
 *
 * <p>Only uses APIs present since 2.6 so the same class can be shipped to every release.</p>
 */
public class AccessFieldReadPrivileges {

  /**
   * no instances
   */
  private AccessFieldReadPrivileges() {
  }

  /**
   * see if a membership query (Hib3MembershipDAO.findAllByGroupOwnerOptions) can return privilege
   * rows (access fields) of someone other than the caller.  If so the caller needs ADMIN on the group.
   * @param grouperSession session of the caller
   * @param memberIds member ids the query is restricted to, or null / empty for all members
   * @param fields fields asked for, or null / empty
   * @param fieldType field type asked for (only used when no fields), or null
   * @param serviceRole service role asked for (only used when no fields), or null
   * @return true if ADMIN is needed on the group
   */
  public static boolean membershipQueryReturnsOthersAccessRows(GrouperSession grouperSession,
      Collection<String> memberIds, Collection<Field> fields, FieldType fieldType, ServiceRole serviceRole) {

    boolean returnsAccessRows = false;

    if (fields != null && fields.size() > 0) {
      // every field counts, not just the first one
      returnsAccessRows = containsAccessField(fields);
    } else if (serviceRole != null) {
      // the service role picks the fields itself (admin is admins and updaters)
      returnsAccessRows = containsAccessField(serviceRole.fieldsForGroupQuery());
    } else if (fieldType == null || fieldType == FieldType.LIST) {
      // the query adds f.typeString = 'list'
      returnsAccessRows = false;
    } else {
      // fieldType access returns only privilege rows, other field types do not narrow the rows at all
      returnsAccessRows = true;
    }

    if (!returnsAccessRows) {
      return false;
    }

    // only the caller's own rows: that is about the caller, nothing to hide
    return !onlySelf(grouperSession, memberIds);
  }

  /**
   * privileges the caller needs on a group to see it in "groups where subject X is in field F"
   * (Hib3GroupDAO / Hib3PITGroupDAO.getAllGroupsMembershipSecure)
   * @param grouperSession session of the caller
   * @param accessField true if F is a group privilege list (or, for a deleted point in time field, not a list field)
   * @param self true if X is the caller
   * @param privilegesSoFar the privileges the query used before (READ, or OPT_OR_READ for the caller's own lists)
   * @return ADMIN_PRIVILEGES for someone else's privilege list, else privilegesSoFar
   */
  public static Set<Privilege> groupsOfSubjectPrivileges(GrouperSession grouperSession,
      boolean accessField, boolean self, Set<Privilege> privilegesSoFar) {
    if (accessField && !self) {
      return AccessPrivilege.ADMIN_PRIVILEGES;
    }
    return privilegesSoFar;
  }

  /**
   * post query check, for the access resolvers that cannot filter in HQL: may the subject read
   * the group with the privilege set the query used
   * @param rootSession root session to run the check in
   * @param group the group
   * @param subject the caller
   * @param privileges the privilege set from groupsOfSubjectPrivileges
   * @return true if allowed
   */
  public static boolean canReadGroupForPrivileges(GrouperSession rootSession, Group group, Subject subject,
      Set<Privilege> privileges) {
    // READ (or more) was the rule before; only ADMIN_PRIVILEGES is stricter
    if (privileges != null && privileges.contains(AccessPrivilege.READ)) {
      return PrivilegeHelper.canRead(rootSession, group, subject);
    }
    return PrivilegeHelper.canAdmin(rootSession, group, subject);
  }

  /**
   * privileges the caller needs on the groups of a service to see that a member has a role in that
   * service (Hib3AttributeDefNameDAO, filtering by serviceRole or any service role)
   * @param grouperSession session of the caller
   * @param member member whose services are listed
   * @param serviceRole the role, or null if anyServiceRole
   * @param anyServiceRole true if any role
   * @return READ for the caller's own services or the user role, else ADMIN
   */
  public static Set<Privilege> serviceRoleGroupPrivileges(GrouperSession grouperSession, Member member,
      ServiceRole serviceRole, boolean anyServiceRole) {
    if (isSelf(grouperSession, member)) {
      return AccessPrivilege.READ_PRIVILEGES;
    }
    // the admin role is the admins and updaters privilege lists, any role includes them
    if (anyServiceRole || (serviceRole != null && containsAccessField(serviceRole.fieldsForGroupQuery()))) {
      return AccessPrivilege.ADMIN_PRIVILEGES;
    }
    return AccessPrivilege.READ_PRIVILEGES;
  }

  /**
   * @param fields fields
   * @return true if one is a group privilege list
   */
  private static boolean containsAccessField(Collection<Field> fields) {
    if (fields == null) {
      return false;
    }
    for (Field field : fields) {
      if (field != null && field.isGroupAccessField()) {
        return true;
      }
    }
    return false;
  }

  /**
   * @param grouperSession session
   * @param member member
   * @return true if the member is the caller
   */
  private static boolean isSelf(GrouperSession grouperSession, Member member) {
    if (grouperSession == null || member == null) {
      return false;
    }
    return SubjectHelper.eq(grouperSession.getSubject(), member);
  }

  /**
   * @param grouperSession session
   * @param memberIds member ids of the query
   * @return true if the query is restricted to the caller's member
   */
  private static boolean onlySelf(GrouperSession grouperSession, Collection<String> memberIds) {
    if (grouperSession == null || memberIds == null || memberIds.size() == 0) {
      return false;
    }
    Subject sessionSubject = grouperSession.getSubject();
    if (sessionSubject == null) {
      return false;
    }
    Member sessionMember = MemberFinder.internal_findBySubject(sessionSubject, null, false);
    if (sessionMember == null) {
      return false;
    }
    for (String memberId : memberIds) {
      if (!sessionMember.getId().equals(memberId)) {
        return false;
      }
    }
    return true;
  }
}
