/**
 * 
 */
package edu.internet2.middleware.grouper.ws.member;

import java.util.Set;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;
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
    protected Set<Member> getMembersHelper(Group group, Field field)
        throws SchemaException {
      return field == null ? group.getMembers() : group.getMembers(field);
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

  /** retrieve members which exist due a group as a member of another group */
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
    protected Set<Member> getMembersHelper(Group group, Field field)
        throws SchemaException {
      return field == null ? group.getEffectiveMembers() : group
          .getEffectiveMembers(field);
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

  /** return only direct members of a group */
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
    protected Set<Member> getMembersHelper(Group group, Field field)
        throws SchemaException {
      return field == null ? group.getImmediateMembers() : group
          .getImmediateMembers(field);
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
   * composite operator (union, intersection, minus)
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
    protected Set<Member> getMembersHelper(Group group, Field field) {
      return GrouperUtil.nonNull(group.getCompositeMembers());
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
    public Set<Group> getGroups(Member member) {
      throw new RuntimeException(
          "getGroups with composite is not supported: member subject id: "
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
      throw new RuntimeException(
          "getGroups with composite is not supported: member subject id: "
              + member.getSubjectId());
    }
  };

  /**
   * get the members from the group based on type of filter
   * 
   * @param group
   * @param field for membership or null to not check field
   * @return the set of members (non null)
   */
  public final Set<Member> getMembers(Group group, Field field) {
    try {
      return this.getMembersHelper(group, field);
    } catch (SchemaException se) {
      throw new RuntimeException("Problem with group and field: " + group + ", field: "
          + field.getName(), se);
    }
  }

  /**
   * get the members from the group based on type of filter
   * 
   * @param group
   * @param field for membership or null to not check field
   * @return the set of members (non null)
   * @throws SchemaException if problem with field
   */
  protected abstract Set<Member> getMembersHelper(Group group, Field field)
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
   * get groups for subject
   * 
   * @param member
   * @param field to check with membership
   * @return the set of members (non null)
   */
  public abstract Set<Group> getGroups(Member member);

  /**
   * get groups for subject based on field
   * 
   * @param member
   * @param field
   * @param field to check with membership
   * @return the set of members (non null)
   */
  public abstract Set<Group> getGroups(Member member, Field field);

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
