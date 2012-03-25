package edu.internet2.middleware.grouper.rules;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;
import edu.internet2.middleware.grouper.permissions.PermissionEntry.PermissionType;
import edu.internet2.middleware.grouper.permissions.role.Role;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * methods available to EL of rules through the alias: ruleElUtils.
 * Note the methods here are static, and generally should input/output
 * primitives or strings for security reasons
 * 
 * @author mchyzer
 *
 */
public class RuleElUtils {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(RuleElUtils.class);

  /**
   * 
   * @param groupId
   * @param memberId
   * @param membershipType @see {@link MembershipType}, null for all
   * @param enabled null for all, T for only enabled, F for only disabled
   * @return true if has immediate enabled membership
   */
  public static boolean hasMembershipByGroupId(String groupId, String memberId, 
      String membershipType, String enabled) {
    
    if (LOG.isDebugEnabled()) {
      LOG.debug("Has member: " + memberId + ", from group: " + groupId 
          + ", membershipType: " + membershipType + ", enabled: " + enabled);
    }
    Group group = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), groupId, true);
    Member member = MemberFinder.findByUuid(GrouperSession.startRootSession(), memberId, true);
    
    MembershipType membershipTypeEnum = MembershipType.valueOfIgnoreCase(membershipType, false);
    
    Boolean enabledBoolean = GrouperUtil.booleanObjectValue(enabled);
    
    Set<Object[]> membershipSetArray = MembershipFinder.findMemberships(GrouperUtil.toSet(groupId), GrouperUtil.toSet(memberId), 
        null, membershipTypeEnum, Group.getDefaultList(), null, null, null, null, enabledBoolean);
    
    boolean result = GrouperUtil.length(membershipSetArray) > 0;
    
    if (LOG.isDebugEnabled()) {
      LOG.debug("Has member: " + member.getSubjectId() + ", from group: " + group.getName() 
          + ", membershipType: " + membershipType + ", enabled: " + enabled + ", result: " + result);
    }
    return result;
  
  }

  /**
   * remove a member of a group
   * @param groupId
   * @param memberId
   * @return true if removed, false if not
   */
  public static boolean removeMemberFromGroupId(String groupId, String memberId) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Removing member: " + memberId + ", from group: " + groupId);
    }
    Group group = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), groupId, true);
    Member member = MemberFinder.findByUuid(GrouperSession.startRootSession(), memberId, true);
    if (LOG.isDebugEnabled()) {
      LOG.debug("Removing subject: " + member.getSubjectId() + ", from group: " + group.getName());
    }
    return group.deleteMember(member, false);
  }

  /**
   * remove a member of a group
   * @param groupName
   * @param memberId
   * @return true if removed, false if not
   */
  public static boolean removeMemberFromGroupName(String groupName, String memberId) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Removing member: " + memberId + ", from group: " + groupName);
    }
    Group group = GroupFinder.findByName(GrouperSession.staticGrouperSession(), groupName, true);
    Member member = MemberFinder.findByUuid(GrouperSession.startRootSession(), memberId, true);
    if (LOG.isDebugEnabled()) {
      LOG.debug("Removing subject: " + member.getSubjectId() + ", from group: " + group.getName());
    }
    return group.deleteMember(member, false);
  }

  /**
   * assign group privileges
   * @param groupId 
   * @param sourceId 
   * @param subjectId 
   * @param subjectIdentifier 
   * @param privilegeNamesCommaSeparated 
   * @return true if assigned, false if already ther
   */
  public static boolean assignGroupPrivilege(String groupId, String sourceId, String subjectId, String subjectIdentifier, String privilegeNamesCommaSeparated) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("assignGroupPrivilege: from group: " + groupId 
          + ", sourceId: " + sourceId + ", subjectId: " + subjectId 
          + ", subjectIdentifier: " + subjectIdentifier + " privilegeNamesCommaSeparated: " + privilegeNamesCommaSeparated);
    }
    boolean result = false;
    Group group = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), groupId, true);
    Subject subject = SubjectFinder.findByOptionalArgs(sourceId, subjectId, subjectIdentifier, true);

    String[] privileges = GrouperUtil.splitTrim(privilegeNamesCommaSeparated, ",");
    
    for (String privilegeString : privileges) {
      Privilege privilege = Privilege.getInstance(privilegeString);
      if (!PrivilegeHelper.hasPrivilege(GrouperSession.staticGrouperSession(), group, subject, GrouperUtil.toSet(privilege))) {
        result = true;
        group.grantPriv(subject, privilege, true);
      }
    }
    
    return result;
    
  }

  /**
   * assign a disabled date in the future by X days
   * @param attributeDefId
   * @param memberId
   * @param daysInFuture
   * @return false if assignments werent there, true if it was
   */
  public static boolean assignPermissionDisabledDaysForAttributeDefId(String attributeDefId, String memberId, int daysInFuture) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Has member: " + memberId + ", from attributeDefId: " + attributeDefId 
          + ", daysInFuture: " + daysInFuture);
    }

    if (StringUtils.isBlank(attributeDefId)) {
      throw new RuntimeException("Why is attributeDefId null?");
    }

    Member member = MemberFinder.findByUuid(GrouperSession.startRootSession(), memberId, true);
    
    Set<PermissionEntry> permissionEntries = RuleUtils.permissionsForUser(attributeDefId, memberId, true);

    RuntimeException runtimeException = null;
    
    boolean result = false;
    
    //first assign disabled dates on individual assignments
    for (PermissionEntry permissionEntry : GrouperUtil.nonNull(permissionEntries)) {
      if (permissionEntry.isImmediatePermission() && permissionEntry.getPermissionType() == PermissionType.role_subject) {
        
        try {
          Role role = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), permissionEntry.getRoleId(), true);
          
          //get this once
          if (member == null) {
            member = MemberFinder.findByUuid(GrouperSession.staticGrouperSession(), permissionEntry.getMemberId(), true);
          }
          
          AttributeDefName attributeDefName = AttributeDefNameFinder.findById(permissionEntry.getAttributeDefNameId(), true);
          
          AttributeAssign attributeAssign = role.getPermissionRoleDelegate().retrieveAssignment(member, null, attributeDefName, true, false);
          
          if (attributeAssign != null) {
            
            attributeAssign.setDisabledTime(new Timestamp(System.currentTimeMillis() + (daysInFuture * 24 * 60 * 60 * 1000)));
            attributeAssign.saveOrUpdate();
            
            result = true;
          }
          
          
        } catch (RuntimeException re) {
          if (runtimeException == null) {
            runtimeException = re;
          }
          LOG.error("error assigning disabled date permission assignments: " + permissionEntry, re);
        }
      }
    }
    
    Set<String> roleIdsAssigned = new HashSet<String>();
    
    //then remove immediate role assignment
    for (PermissionEntry permissionEntry : GrouperUtil.nonNull(permissionEntries)) {

      if (permissionEntry.isImmediateMembership() && permissionEntry.getPermissionType() == PermissionType.role) {
        
        try {
          String roleId = permissionEntry.getRoleId();
          
          if (roleIdsAssigned.contains(roleId)) {
            continue;
          }
          
          Role role = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), roleId, true);
          
          //get this once
          if (member == null) {
            member = MemberFinder.findByUuid(GrouperSession.staticGrouperSession(), permissionEntry.getMemberId(), true);
          }
          
          //dont try again
          roleIdsAssigned.add(roleId);
          
          Membership membership = ((Group)role).getImmediateMembership(Group.getDefaultList(), member, true, false);

          if (membership != null) {
            membership.setDisabledTime(new Timestamp(System.currentTimeMillis() + (daysInFuture * 24 * 60 * 60 * 1000)));
            membership.update();
            result = true;
          }
          
        } catch (RuntimeException re) {
          if (runtimeException == null) {
            runtimeException = re;
          }
          LOG.error("error assigning delete date on role assignments: " + permissionEntry, re);
        }
      }
    }
    
    if (runtimeException != null) {
      throw runtimeException;
    }
    return result;
    
  }

  
  /**
   * assign a disabled date in the future by X days
   * @param groupId
   * @param memberId
   * @param daysInFuture
   * @param addIfNotThere 
   * @return false if membership wasnt there, true if it was
   */
  public static boolean assignMembershipDisabledDaysForGroupId(String groupId, String memberId, int daysInFuture, boolean addIfNotThere) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Has member: " + memberId + ", from group: " + groupId 
          + ", daysInFuture: " + daysInFuture);
    }

    if (StringUtils.isBlank(groupId)) {
      throw new RuntimeException("Why is groupId null?");
    }

    Group group = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), groupId, true);
    Member member = MemberFinder.findByUuid(GrouperSession.startRootSession(), memberId, true);
    Membership membership = group.getImmediateMembership(Group.getDefaultList(), member, true, false);
    if (membership == null) {
      if (!addIfNotThere) {
        return false;
      }
      group.addMember(member.getSubject(), true);
      membership = group.getImmediateMembership(Group.getDefaultList(), member, true, false);
    }
    membership.setDisabledTime(new Timestamp(System.currentTimeMillis() + (daysInFuture * 24 * 60 * 60 * 1000)));
    membership.update();
    return true;
  }

  /**
   * veto this for some reason
   * @param reasonKey
   * @param reason
   * @return the exception
   */
  public static RuleVeto veto(String reasonKey, String reason) {
    return new RuleVeto(reasonKey, reason);
  }

  /**
   * assign stem privileges
   * @param stemId 
   * @param sourceId 
   * @param subjectId 
   * @param subjectIdentifier 
   * @param privilegeNamesCommaSeparated 
   * @return true if assigned, false if already ther
   */
  public static boolean assignStemPrivilege(String stemId, String sourceId, String subjectId, String subjectIdentifier, String privilegeNamesCommaSeparated) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("assignStemPrivilege: from stem: " + stemId 
          + ", sourceId: " + sourceId + ", subjectId: " + subjectId 
          + ", subjectIdentifier: " + subjectIdentifier + " privilegeNamesCommaSeparated: " + privilegeNamesCommaSeparated);
    }
    boolean result = false;
    Stem stem = StemFinder.findByUuid(GrouperSession.staticGrouperSession(), stemId, true);
    Subject subject = SubjectFinder.findByOptionalArgs(sourceId, subjectId, subjectIdentifier, true);
    String[] privileges = GrouperUtil.splitTrim(privilegeNamesCommaSeparated, ",");
    
    for (String privilegeString : privileges) {
      Privilege privilege = Privilege.getInstance(privilegeString);
      if (!PrivilegeHelper.hasPrivilege(GrouperSession.staticGrouperSession(), stem, subject, GrouperUtil.toSet(privilege))) {
        result = true;
        stem.grantPriv(subject, privilege, true);
      }
    }
    
    return result;
    
  }

  /**
   * assign attributeDef privileges
   * @param attributeDefId 
   * @param sourceId 
   * @param subjectId 
   * @param subjectIdentifier 
   * @param privilegeNamesCommaSeparated 
   * @return true if assigned, false if already ther
   */
  public static boolean assignAttributeDefPrivilege(String attributeDefId, String sourceId, 
      String subjectId, String subjectIdentifier, String privilegeNamesCommaSeparated) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("assignAttributeDefPrivilege: from attributeDef: " + attributeDefId 
          + ", sourceId: " + sourceId + ", subjectId: " + subjectId 
          + ", subjectIdentifier: " + subjectIdentifier + " privilegeNamesCommaSeparated: " + privilegeNamesCommaSeparated);
    }
    boolean result = false;
    AttributeDef attributeDef = AttributeDefFinder.findById(attributeDefId, true);
    Subject subject = SubjectFinder.findByOptionalArgs(sourceId, subjectId, subjectIdentifier, true);
    String[] privileges = GrouperUtil.splitTrim(privilegeNamesCommaSeparated, ",");
    
    for (String privilegeString : privileges) {
      Privilege privilege = Privilege.getInstance(privilegeString);
      if (!PrivilegeHelper.hasPrivilege(GrouperSession.staticGrouperSession(), attributeDef, subject, GrouperUtil.toSet(privilege))) {
        result = true;
        attributeDef.getPrivilegeDelegate().grantPriv(subject, privilege, true);
      }
    }
    
    return result;
    
  }
  
  /**
   * format a date, default to yyy/MM/dd.  Docs in the Java SimpleDateFormat class.
   * @see SimpleDateFormat
   * @param date
   * @param format
   * @return the formatted date
   */
  public static String formatDate(Date date, String format) {
    if (date == null) {
      return null;
    }
    SimpleDateFormat dateFormat = new SimpleDateFormat(StringUtils.isBlank(format) ? "yyyy/MM/dd" : format);
    return dateFormat.format(date);
  }
  
}
