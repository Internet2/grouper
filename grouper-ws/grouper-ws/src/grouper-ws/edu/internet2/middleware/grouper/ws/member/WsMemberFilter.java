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
 * 
 */
package edu.internet2.middleware.grouper.ws.member;

import java.util.Set;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;

/**
 * member filter for retrieving members.
 * 
 * @author mchyzer
 * 
 */
public enum WsMemberFilter {

  /** retrieve all members (immediate, effective and composite) */
  All {

    /**
     * get the members from the group
     * 
     * @param group
     * @param field for membership or null to not check field
     * @return the set of members (non null)
     * @throws SchemaException if problem
     */
    @Override
    @SuppressWarnings("unchecked")
    protected Set<Member> getMembersHelper(Group group, Field field, Set<Source> sources)
        throws SchemaException {
      
      //set default field if null
      field = field == null ? Group.getDefaultList() : field;
      return group.getMembers(field, sources, null);
    }

    /**
     * get the composite memberships from the group
     * 
     * @param group
     * @param field for membership or null to not check field
     * @return the set of members (non null)
     */
    @Override
    @SuppressWarnings("unchecked")
    protected Set<Membership> getMembershipsHelper(Group group, Field field)
        throws SchemaException {
      return field == null ? group.getMemberships() : group.getMemberships(field);
    }

    /**
     * see if a group has a subject as member
     * 
     * @param group
     * @param field to check with membership
     * @return true|false
     * @throws SchemaException
     */
    @Override
    @SuppressWarnings("unchecked")
    protected boolean hasMemberHelper(Group group, Subject subject, Field field)
        throws SchemaException {
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
    public Set<Group> getGroups(Member member, Field field, String scope, 
        Stem stem, Scope stemScope, QueryOptions queryOptions, Boolean enabled) {
      return GrouperUtil.nonNull(member.getGroups(field, scope, stem, stemScope, queryOptions, enabled));
    }

    /**
     * 
     * @see edu.internet2.middleware.grouper.ws.member.WsMemberFilter#getMembershipType()
     */
    @Override
    public MembershipType getMembershipType() {
      //note, all members is the same as an "all" membership type
      return null;
    }
  },

  /**
   * retrieve members which exist due a group as a member of another group (for composite
   * groups, this will not return anything) 
   */
  Effective {

    /**
     * get the composite members from the group
     * 
     * @param group
     * @param field for membership or null to not check field
     * @return the set of members (non null)
     */
    @Override
    @SuppressWarnings("unchecked")
    protected Set<Member> getMembersHelper(Group group, Field field, Set<Source> sources)
        throws SchemaException {
      
      //set default field if null
      field = field == null ? Group.getDefaultList() : field;

      return group.getEffectiveMembers(field, sources, null);
    }

    /**
     * get the composite memberships from the group
     * 
     * @param group
     * @param field for membership or null to not check field
     * @return the set of members (non null)
     */
    @Override
    @SuppressWarnings("unchecked")
    protected Set<Membership> getMembershipsHelper(Group group, Field field)
        throws SchemaException {
      return field == null ? group.getEffectiveMemberships() : group
          .getEffectiveMemberships(field);
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
    protected boolean hasMemberHelper(Group group, Subject subject, Field field)
        throws SchemaException {
      return field == null ? group.hasEffectiveMember(subject) : group
          .hasEffectiveMember(subject, field);
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
    public Set<Group> getGroups(Member member, Field field, String scope, 
        Stem stem, Scope stemScope, QueryOptions queryOptions, Boolean enabled) {
      return GrouperUtil.nonNull(member.getEffectiveGroups(field, scope, stem, stemScope, queryOptions, enabled));
    }

    /**
     * 
     * @see edu.internet2.middleware.grouper.ws.member.WsMemberFilter#getMembershipType()
     */
    @Override
    public MembershipType getMembershipType() {
      return MembershipType.EFFECTIVE;
    }
  },
  /**
   * return only direct members of a group (for composite groups this will not return anything) 
   */
  Immediate {

    /**
     * get the composite members from the group
     * 
     * @param group
    * @param field for membership or null to not check field
     * @return the set of members (non null)
     */
    @Override
    @SuppressWarnings("unchecked")
    protected Set<Member> getMembersHelper(Group group, Field field, Set<Source> sources)
        throws SchemaException {
      
      //set default field if null
      field = field == null ? Group.getDefaultList() : field;

      return group.getImmediateMembers(field, sources, null);
    }

    /**
     * get the composite memberships from the group
     * 
     * @param group
     * @param field for membership or null to not check field
     * @return the set of members (non null)
     */
    @Override
    @SuppressWarnings("unchecked")
    protected Set<Membership> getMembershipsHelper(Group group, Field field)
        throws SchemaException {
      return field == null ? group.getImmediateMemberships() : group
          .getImmediateMemberships(field);
    }

    /**
     * see if a group has a subject as member
     * 
     * @param group
     * @param field to check with membership
     * @return true|false
     * @throws SchemaException
     */
    @Override
    @SuppressWarnings("unchecked")
    protected boolean hasMemberHelper(Group group, Subject subject, Field field)
        throws SchemaException {
      return field == null ? group.hasImmediateMember(subject) : group
          .hasImmediateMember(subject, field);
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
    public Set<Group> getGroups(Member member, Field field, String scope, 
        Stem stem, Scope stemScope, QueryOptions queryOptions, Boolean enabled) {
      return GrouperUtil.nonNull(member.getImmediateGroups(field, scope, stem, stemScope, queryOptions, enabled));
    }

    /**
     * 
     * @see edu.internet2.middleware.grouper.ws.member.WsMemberFilter#getMembershipType()
     */
    @Override
    public MembershipType getMembershipType() {
      return MembershipType.IMMEDIATE;
    }
  },

  /**
   * if this is a composite group, then return all the memberships that match the 
   * composite operator (union, intersection, complement).  This will be the same as
   * All for composite groups.
   */
  Composite {

    /**
     * get the composite members from the group
     * 
     * @param group
     * @param field for membership or null to not check field
     * @return the set of members (non null)
     */
    @Override
    @SuppressWarnings("unchecked")
    protected Set<Member> getMembersHelper(Group group, Field field, Set<Source> sources) {

      //set default field if null
      field = field == null ? Group.getDefaultList() : field;
      return GrouperUtil.nonNull(group.getCompositeMembers(field, sources, null));
      
    }

    /**
     * get the composite memberships from the group
     * 
     * @param group
     * @param field for membership or null to not check field
     * @return the set of members (non null)
     */
    @Override
    @SuppressWarnings("unchecked")
    protected Set<Membership> getMembershipsHelper(Group group, Field field) {
      if (field != null) {
        throw new WsInvalidQueryException("Field '" + field.getName()
            + "' cannot be nonnull if member filter is composite");
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
      throw new RuntimeException("hasMember with composite is not supported: groupName: "
          + group.getName() + ", subject: " + subject.getName() + ", field: "
          + field.getName());
    }

    /**
     * get groups for subject
     * 
     * @param member
     * @return the set of members (non null)
     */
    @Override
    public Set<Group> getGroups(Member member, Field field, String scope, 
        Stem stem, Scope stemScope, QueryOptions queryOptions, Boolean enabled) {
      throw new RuntimeException(
          "getGroups with composite is not supported: member subject id: "
              + member.getSubjectId());
    }

    /**
     * 
     * @see edu.internet2.middleware.grouper.ws.member.WsMemberFilter#getMembershipType()
     */
    @Override
    public MembershipType getMembershipType() {
      return MembershipType.COMPOSITE;
    }
  }, 
  
  /**
   * return only direct members of a group (for composite groups this will not return anything) 
   */
  NonImmediate {
  
    /**
     * get the nonimmediate members from the group
     * 
     * @param group
     * @param field for membership or null to not check field
     * @return the set of members (non null)
     */
    @Override
    protected Set<Member> getMembersHelper(Group group, Field field, Set<Source> sources)
        throws SchemaException {

      //set default field if null
      field = field == null ? Group.getDefaultList() : field;

      return group.getNonImmediateMembers(field, sources, null);
    }
  
    /**
     * get the composite memberships from the group
     * 
     * @param group
     * @param field for membership or null to not check field
     * @return the set of members (non null)
     */
    @Override
    @SuppressWarnings("unchecked")
    protected Set<Membership> getMembershipsHelper(Group group, Field field)
        throws SchemaException {
      return field == null ? group.getNonImmediateMemberships() : group
          .getNonImmediateMemberships(field);
    }
  
    /**
     * see if a group has a subject as member
     * 
     * @param group
     * @param field to check with membership
     * @return true|false
     * @throws SchemaException
     */
    @Override
    @SuppressWarnings("unchecked")
    protected boolean hasMemberHelper(Group group, Subject subject, Field field)
        throws SchemaException {
      return field == null ? group.hasNonImmediateMember(subject) : group
          .hasNonImmediateMember(subject, field);
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
    public Set<Group> getGroups(Member member, Field field, String scope, 
        Stem stem, Scope stemScope, QueryOptions queryOptions, Boolean enabled) {
      return GrouperUtil.nonNull(member.getNonImmediateGroups(field, scope, stem, stemScope, queryOptions, enabled));
    }

    /**
     * 
     * @see edu.internet2.middleware.grouper.ws.member.WsMemberFilter#getMembershipType()
     */
    @Override
    public MembershipType getMembershipType() {
      return MembershipType.NONIMMEDIATE;
    }
  };

  /**
   * get the members from the group based on type of filter
   * 
   * @param group
   * @param field for membership or null to not check field
   * @param sources are the sources to filter the members
   * @return the set of members (non null)
   */
  public final Set<Member> getMembers(Group group, Field field, Set<Source> sources) {
    try {
      return this.getMembersHelper(group, field, sources);
    } catch (SchemaException se) {
      throw new RuntimeException("Problem with group and field: " + group + ", field: "
          + field.getName() + ", " + GrouperUtil.toString(sources), se);
    }
  }

  /**
   * get the membership type for this member filter
   * @return the membership type
   */
  public abstract MembershipType getMembershipType();
  
  /**
   * get the members from the group based on type of filter
   * 
   * @param group
   * @param field for membership or null to not check field
   * @param sources
   * @return the set of members (non null)
   * @throws SchemaException if problem with field
   */
  protected abstract Set<Member> getMembersHelper(Group group, Field field, Set<Source> sources)
      throws SchemaException;

  /**
   * get the memberships from the group based on type of filter
   * 
   * @param group
   * @param field for membership or null to not check field
   * @return the set of members (non null)
   * @throws SchemaException is there is a problem with field
   */
  protected abstract Set<Membership> getMembershipsHelper(Group group, Field field)
      throws SchemaException;

  /**
   * get the memberships from the group based on type of filter
   * 
   * @param group
   * @param field for membership or null to not check field
   * @return the set of members (non null)
   * @throws SchemaException is there is a problem with field
   */
  public final Set<Membership> getMemberships(Group group, Field field) {
    try {
      return this.getMembershipsHelper(group, field);
    } catch (SchemaException se) {
      throw new RuntimeException("Problem with group and field: " + group + ", field: "
          + field.getName(), se);
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
  protected abstract boolean hasMemberHelper(Group group, Subject subject, Field field)
      throws SchemaException;

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
      throw new RuntimeException("Problem with group and field: " + group + ", field: "
          + field.getName(), se);
    }
  }

  /**
   * get groups for subject based on field
   * 
   * @param member
   * @param field
   * @param field to check with membership
   * @param scope 
   * @param stem 
   * @param stemScope 
   * @param enabled 
   * @param queryOptions 
   * @return the set of members (non null)
   */
  public abstract Set<Group> getGroups(Member member, Field field, String scope, 
      Stem stem, Scope stemScope, QueryOptions queryOptions, Boolean enabled);

  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @return the enum or null or exception if not found
   */
  public static WsMemberFilter valueOfIgnoreCase(String string) {
    return GrouperServiceUtils.enumValueOfIgnoreCase(WsMemberFilter.class, string, false);
  }
}
