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

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * member filter for retrieving members.
 * 
 * Originally WsMemberFilter; modified slightly to suit the needs of ldappc - tz
 * 
 * @author mchyzer
 * 
 */

public enum FieldMemberFilter {

  /** retrieve all members (immediate, effective and composite) */
  all {

    /**
     * get the members from the group
     * 
     * @param group
     * @param field
     *          for membership or null to not check field
     * @return the set of members (non null)
     * @throws SchemaException
     *           if problem
     */
    @Override
    @SuppressWarnings("unchecked")
    protected Set<Member> getMembersHelper(Group group, Field field) throws SchemaException {
      return field == null ? group.getMembers() : group.getMembers(field);
    }

    /**
     * get the composite memberships from the group
     * 
     * @param group
     * @param field
     *          for membership or null to not check field
     * @return the set of members (non null)
     */
    @Override
    @SuppressWarnings("unchecked")
    protected Set<Membership> getMembershipsHelper(Group group, Field field) throws SchemaException {
      return field == null ? group.getMemberships() : group.getMemberships(field);
    }

    /**
     * see if a group has a subject as member
     * 
     * @param group
     * @param field
     *          to check with membership
     * @return true|false
     * @throws SchemaException
     */
    @Override
    @SuppressWarnings("unchecked")
    protected boolean hasMemberHelper(Group group, Subject subject, Field field) throws SchemaException {
      return field == null ? group.hasMember(subject) : group.hasMember(subject, field);
    }

    /**
     * get groups for subject
     * 
     * @param member
     * @return the set of members (non null)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Set<Group> getGroups(Member member) {
      return GrouperUtil.nonNull(member.getGroups());
    }

    /**
     * get groups for subject
     * 
     * @param member
     * @return the set of members (non null)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Set<Group> getGroups(Member member, Field field) {
      return GrouperUtil.nonNull(member.getGroups(field));
    }
  },

  /**
   * retrieve members which exist due a group as a member of another group (for composite
   * groups, this will not return anything)
   */
  effective {

    /**
     * get the composite members from the group
     * 
     * @param group
     * @param field
     *          for membership or null to not check field
     * @return the set of members (non null)
     */
    @Override
    @SuppressWarnings("unchecked")
    protected Set<Member> getMembersHelper(Group group, Field field) throws SchemaException {
      return field == null ? group.getEffectiveMembers() : group.getEffectiveMembers(field);
    }

    /**
     * get the composite memberships from the group
     * 
     * @param group
     * @param field
     *          for membership or null to not check field
     * @return the set of members (non null)
     */
    @Override
    @SuppressWarnings("unchecked")
    protected Set<Membership> getMembershipsHelper(Group group, Field field) throws SchemaException {
      return field == null ? group.getEffectiveMemberships() : group.getEffectiveMemberships(field);
    }

    /**
     * see if a group has a subject as member
     * 
     * @param group
     * @return true|false
     * @throws SchemaException
     */
    @Override
    @SuppressWarnings("unchecked")
    protected boolean hasMemberHelper(Group group, Subject subject, Field field) throws SchemaException {
      return field == null ? group.hasEffectiveMember(subject) : group.hasEffectiveMember(subject, field);
    }

    /**
     * get groups for subject
     * 
     * @param member
     * @return the set of members (non null)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Set<Group> getGroups(Member member) {
      return GrouperUtil.nonNull(member.getEffectiveGroups());
    }

    /**
     * get groups for subject
     * 
     * @param member
     * @param field
     * @return the set of members (non null)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Set<Group> getGroups(Member member, Field field) {
      return GrouperUtil.nonNull(member.getEffectiveGroups(field));
    }
  },
  /**
   * return only direct members of a group (for composite groups this will not return
   * anything)
   */
  immediate {

    /**
     * get the composite members from the group
     * 
     * @param group
     * @param field
     *          for membership or null to not check field
     * @return the set of members (non null)
     */
    @Override
    @SuppressWarnings("unchecked")
    protected Set<Member> getMembersHelper(Group group, Field field) throws SchemaException {
      return field == null ? group.getImmediateMembers() : group.getImmediateMembers(field);
    }

    /**
     * get the composite memberships from the group
     * 
     * @param group
     * @param field
     *          for membership or null to not check field
     * @return the set of members (non null)
     */
    @Override
    @SuppressWarnings("unchecked")
    protected Set<Membership> getMembershipsHelper(Group group, Field field) throws SchemaException {
      return field == null ? group.getImmediateMemberships() : group.getImmediateMemberships(field);
    }

    /**
     * see if a group has a subject as member
     * 
     * @param group
     * @param field
     *          to check with membership
     * @return true|false
     * @throws SchemaException
     */
    @Override
    @SuppressWarnings("unchecked")
    protected boolean hasMemberHelper(Group group, Subject subject, Field field) throws SchemaException {
      return field == null ? group.hasImmediateMember(subject) : group.hasImmediateMember(subject, field);
    }

    /**
     * get groups for subject
     * 
     * @param member
     * @return the set of members (non null)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Set<Group> getGroups(Member member) {
      return GrouperUtil.nonNull(member.getImmediateGroups());
    }

    /**
     * get groups for subject
     * 
     * @param member
     * @param field
     * @return the set of members (non null)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Set<Group> getGroups(Member member, Field field) {
      return GrouperUtil.nonNull(member.getImmediateGroups(field));
    }
  },

  /**
   * if this is a composite group, then return all the memberships that match the
   * composite operator (union, intersection, complement). This will be the same as All
   * for composite groups.
   */
  composite {

    /**
     * get the composite members from the group
     * 
     * @param group
     * @param field
     *          for membership or null to not check field
     * @return the set of members (non null)
     */
    @Override
    @SuppressWarnings("unchecked")
    protected Set<Member> getMembersHelper(Group group, Field field) {
      return GrouperUtil.nonNull(group.getCompositeMembers());
    }

    /**
     * get the composite memberships from the group
     * 
     * @param group
     * @param field
     *          for membership or null to not check field
     * @return the set of members (non null)
     */
    @Override
    @SuppressWarnings("unchecked")
    protected Set<Membership> getMembershipsHelper(Group group, Field field) {
      if (field != null) {
        throw new RuntimeException("Field '" + field.getName() + "' cannot be nonnull if member filter is composite");
      }
      return group.getCompositeMemberships();
    }

    /**
     * see if a group has a subject as member
     * 
     * @param group
     * @return true|false
     */
    @Override
    @SuppressWarnings("unchecked")
    protected boolean hasMemberHelper(Group group, Subject subject, Field field) {
      throw new RuntimeException("hasMember with composite is not supported: groupName: " + group.getName()
          + ", subject: " + subject.getName() + ", field: " + field.getName());
    }

    /**
     * get groups for subject
     * 
     * @param member
     * @return the set of members (non null)
     */
    @Override
    public Set<Group> getGroups(Member member) {
      throw new RuntimeException("getGroups with composite is not supported: member subject id: "
          + member.getSubjectId());
    }

    /**
     * get groups for subject
     * 
     * @param member
     * @return the set of members (non null)
     */
    @Override
    public Set<Group> getGroups(Member member, Field field) {
      throw new RuntimeException("getGroups with composite is not supported: member subject id: "
          + member.getSubjectId());
    }
  };

  /**
   * get the members from the group based on type of filter
   * 
   * @param group
   * @param field
   *          for membership or null to not check field
   * @return the set of members (non null)
   */
  public final Set<Member> getMembers(Group group, Field field) {
    try {
      return this.getMembersHelper(group, field);
    } catch (SchemaException se) {
      throw new RuntimeException("Problem with group and field: " + group + ", field: " + field.getName(), se);
    }
  }

  /**
   * get the members from the group based on type of filter
   * 
   * @param group
   * @param field
   *          for membership or null to not check field
   * @return the set of members (non null)
   * @throws SchemaException
   *           if problem with field
   */
  protected abstract Set<Member> getMembersHelper(Group group, Field field) throws SchemaException;

  /**
   * get the memberships from the group based on type of filter
   * 
   * @param group
   * @param field
   *          for membership or null to not check field
   * @return the set of members (non null)
   * @throws SchemaException
   *           is there is a problem with field
   */
  protected abstract Set<Membership> getMembershipsHelper(Group group, Field field) throws SchemaException;

  /**
   * get the memberships from the group based on type of filter
   * 
   * @param group
   * @param field
   *          for membership or null to not check field
   * @return the set of members (non null)
   * @throws SchemaException
   *           is there is a problem with field
   */
  public final Set<Membership> getMemberships(Group group, Field field) {
    try {
      return this.getMembershipsHelper(group, field);
    } catch (SchemaException se) {
      throw new RuntimeException("Problem with group and field: " + group + ", field: " + field.getName(), se);
    }
  }

  /**
   * see if a subject is in a group
   * 
   * @param group
   * @param subject
   * @param field
   * @return the set of members (non null)
   * @throws SchemaException
   */
  protected abstract boolean hasMemberHelper(Group group, Subject subject, Field field) throws SchemaException;

  /**
   * see if a subject is in a group
   * 
   * @param group
   * @param subject
   * @param field
   * @return the set of members (non null)
   * @throws SchemaException
   */
  public final boolean hasMember(Group group, Subject subject, Field field) {
    try {
      return this.hasMemberHelper(group, subject, field);
    } catch (SchemaException se) {
      throw new RuntimeException("Problem with group and field: " + group + ", field: " + field.getName(), se);
    }
  }

  /**
   * get groups for subject
   * 
   * @param member
   * @param field
   *          to check with membership
   * @return the set of members (non null)
   */
  public abstract Set<Group> getGroups(Member member);

  /**
   * get groups for subject based on field
   * 
   * @param member
   * @param field
   * @param field
   *          to check with membership
   * @return the set of members (non null)
   */
  public abstract Set<Group> getGroups(Member member, Field field);

  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @return the enum or null or exception if not found
   */
  public static FieldMemberFilter valueOfIgnoreCase(String string) {
    return enumValueOfIgnoreCase(FieldMemberFilter.class, string, false);
  }

  /**
   * do a case-insensitive matching
   * 
   * @param theEnumClass
   *          class of the enum
   * @param <E>
   *          generic type
   * 
   * @param string
   * @param exceptionOnNotFound
   *          true if exception should be thrown on not found
   * @return the enum or null or exception if not found
   * @throws GrouperRestInvalidRequest
   *           if there is a problem
   */
  public static <E extends Enum<?>> E enumValueOfIgnoreCase(Class<E> theEnumClass, String string,
      boolean exceptionOnNotFound) throws RuntimeException {

    if (!exceptionOnNotFound && StringUtils.isBlank(string)) {
      System.err.println("getenum " + null);
      return null;
    }

    for (E e : theEnumClass.getEnumConstants()) {
      System.err.println("getenum " + e);
    }

    for (E e : theEnumClass.getEnumConstants()) {
      if (StringUtils.equalsIgnoreCase(string, e.name())) {
        return e;
      }
    }
    StringBuilder error = new StringBuilder("Cant find " + theEnumClass.getSimpleName() + " from string: '")
        .append(string);
    error.append("', expecting one of: ");
    for (E e : theEnumClass.getEnumConstants()) {
      error.append(e.name()).append(", ");
    }
    throw new RuntimeException(error.toString());

  }
}
