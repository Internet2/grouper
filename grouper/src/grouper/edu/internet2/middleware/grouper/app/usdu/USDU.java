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
package edu.internet2.middleware.grouper.app.usdu;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.MemberDeleteException;
import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import edu.internet2.middleware.grouper.exception.RevokePrivilegeException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;
import edu.internet2.middleware.subject.provider.SubjectImpl;

/**
 * The Unresolvable Subject Deletion Utility finds and optionally deletes
 * members whose subjects can not be resolved by their source.
 * 
 * Documentation is available via the 'Unresolvable Subject Deletion Utility' on
 * the Grouper Product wiki
 * https://spaces.internet2.edu/pages/viewpage.action?pageId=14517820
 */

public class USDU {

  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(USDU.class);

  /** map list names to corresponding privileges, a better way probably exists */
  private static Map<String, Privilege> list2priv = new HashMap<String, Privilege>();
  static {
    list2priv.put(Field.FIELD_NAME_ADMINS, AccessPrivilege.ADMIN);
    list2priv.put(Field.FIELD_NAME_OPTINS, AccessPrivilege.OPTIN);
    list2priv.put(Field.FIELD_NAME_OPTOUTS, AccessPrivilege.OPTOUT);
    list2priv.put(Field.FIELD_NAME_READERS, AccessPrivilege.READ);
    list2priv.put(Field.FIELD_NAME_UPDATERS, AccessPrivilege.UPDATE);
    list2priv.put(Field.FIELD_NAME_VIEWERS, AccessPrivilege.VIEW);
    list2priv.put(Field.FIELD_NAME_GROUP_ATTR_READERS, AccessPrivilege.GROUP_ATTR_READ);
    list2priv.put(Field.FIELD_NAME_GROUP_ATTR_UPDATERS, AccessPrivilege.GROUP_ATTR_UPDATE);
    list2priv.put(Field.FIELD_NAME_CREATORS, NamingPrivilege.CREATE);
    list2priv.put(Field.FIELD_NAME_STEM_ADMINS, NamingPrivilege.STEM_ADMIN);
    list2priv.put(Field.FIELD_NAME_STEM_VIEWERS, NamingPrivilege.STEM_VIEW);
    list2priv.put(Field.FIELD_NAME_STEM_ATTR_READERS, NamingPrivilege.STEM_ATTR_READ);
    list2priv.put(Field.FIELD_NAME_STEM_ATTR_UPDATERS, NamingPrivilege.STEM_ATTR_UPDATE);
  }

  /**
   * Deprecated - USDU is now run using the daemon and doesn't call this method
   * 
   * Find and optionally delete memberships and privileges for the unresolvable
   * subject with given member uuid.
   * 
   * @param s
   *          the Grouper session
   * @param uuid
   *          the uuid of the member
   * @param delete
   *          if true will delete memberships and privileges
   * @deprecated
   * @throws IllegalArgumentException
   * @throws InsufficientPrivilegeException
   * @throws GroupNotFoundException
   * @throws MemberDeleteException
   * @throws MemberNotFoundException
   * @throws RevokePrivilegeException
   * @throws SchemaException
   * @throws SourceUnavailableException
   * @throws StemNotFoundException
   */
  public static void resolveMember(GrouperSession s, String uuid, boolean delete) throws IllegalArgumentException,
      InsufficientPrivilegeException, GroupNotFoundException, MemberDeleteException, MemberNotFoundException,
      RevokePrivilegeException, SchemaException, SourceUnavailableException, StemNotFoundException {

    Member member;
    try {
      member = MemberFinder.findByUuid(s, uuid, true);
    } catch (MemberNotFoundException e) {
      System.out.println("member with uuid '" + uuid + "' not found");
      return;
    }

    if (isMemberResolvable(s, member)) {
      System.out.println("member " + member + " is resolvable");
      return;
    }

    Set<Member> members = new HashSet<Member>();
    members.add(member);
    resolveMembers(members, delete);
  }

  /**
   * Deprecated - USDU is now run using the daemon and doesn't call this method
   * 
   * Find and optionally delete memberships and privileges for unresolvable
   * subjects from all sources.
   * 
   * @param s
   *          the Grouper session
   * @param delete
   *          if true will delete memberships and privileges
   * @deprecated
   * @throws IllegalArgumentException
   * @throws InsufficientPrivilegeException
   * @throws GroupNotFoundException
   * @throws MemberDeleteException
   * @throws MemberNotFoundException
   * @throws RevokePrivilegeException
   * @throws SchemaException
   * @throws SourceUnavailableException
   * @throws StemNotFoundException
   */
  public static void resolveMembers(GrouperSession s, boolean delete) throws IllegalArgumentException,
      InsufficientPrivilegeException, GroupNotFoundException, MemberDeleteException, MemberNotFoundException,
      RevokePrivilegeException, SchemaException, SourceUnavailableException, StemNotFoundException {

    resolveMembers(s, null, delete);
  }

  /**
   * Deprecated - USDU is now run using the daemon and doesn't call this method
   * 
   * Find and optionally delete memberships and privileges for unresolvable
   * subjects from the specified source.
   * 
   * @param s
   *          the Grouper session
   * @param source 
   * @param delete
   *          if true will delete memberships and privileges
   * @deprecated
   * @throws IllegalArgumentException
   * @throws InsufficientPrivilegeException
   * @throws GroupNotFoundException
   * @throws MemberDeleteException
   * @throws MemberNotFoundException
   * @throws RevokePrivilegeException
   * @throws SchemaException
   * @throws SourceUnavailableException
   * @throws StemNotFoundException
   */
  public static void resolveMembers(GrouperSession s, Source source, boolean delete) throws IllegalArgumentException,
      InsufficientPrivilegeException, GroupNotFoundException, MemberDeleteException, MemberNotFoundException,
      RevokePrivilegeException, SchemaException, SourceUnavailableException, StemNotFoundException {

    resolveMembers(getUnresolvableMembers(s, source), delete);
  }

  /**
   * Deprecated - USDU is now run using the daemon and doesn't call this method
   * 
   * Print to stdout and optionally delete memberships and privileges for the
   * given unresolvable subjects.
   * 
   * @param unresolvables
   *          a set of unresolvable members
   * @param delete
   *          if true will delete memberships and privileges
   * @deprecated
   * @throws IllegalArgumentException
   * @throws InsufficientPrivilegeException
   * @throws GroupNotFoundException
   * @throws MemberDeleteException
   * @throws MemberNotFoundException
   * @throws RevokePrivilegeException
   * @throws SchemaException
   * @throws SourceUnavailableException
   * @throws StemNotFoundException
   */
  protected static void resolveMembers(Set<Member> unresolvables, boolean delete) throws IllegalArgumentException,
      InsufficientPrivilegeException, GroupNotFoundException, MemberDeleteException, MemberNotFoundException,
      RevokePrivilegeException, SchemaException, SourceUnavailableException, StemNotFoundException {

    int maxAllowed = GrouperConfig.retrieveConfig().propertyValueInt("usdu.failsafe.maxUnresolvableSubjects", 200);
    if (delete && unresolvables.size() > maxAllowed) {
      throw new RuntimeException("Found too many unresolvable subjects: " + unresolvables.size() + ". Maximum allowed: " + maxAllowed);
    }
    
    Set<Field> fields = getMemberFields();

    for (Member member : unresolvables) {
      Set<Membership> memberships = getAllImmediateMemberships(member, fields);
      if (memberships.isEmpty()) {
        System.out.println("member_uuid='" + member.getUuid() + "' subject=" + member + " no_memberships");
      } else {
        for (Membership membership : memberships) {

          System.out.print("member_uuid='" + member.getUuid() + "' subject=" + member);
          if (membership.getList().getType().equals(FieldType.LIST)
              || membership.getList().getType().equals(FieldType.ACCESS)) {
            System.out.print(" group='" + membership.getOwnerGroup().getName());
          }
          if (membership.getList().getType().equals(FieldType.NAMING)) {
            System.out.print(" stem='" + membership.getStem().getName());
          }
          System.out.print(" list='" + membership.getList().getName() + "'");

          if (delete) {
            System.out.print(" delete");
            if (membership.getList().getType().equals(FieldType.LIST)) {
              USDU.deleteUnresolvableMember(membership.getMember(), membership.getOwnerGroup(), membership.getList());
            }
            if (membership.getList().getType().equals(FieldType.ACCESS)) {
              USDU.deleteUnresolvableMember(membership.getMember(), membership.getOwnerGroup(), getPrivilege(membership
                  .getList()));
            }
            if (membership.getList().getType().equals(FieldType.NAMING)) {
              USDU.deleteUnresolvableMember(membership.getMember(), membership.getStem(), getPrivilege(membership
                  .getList()));
            }
          }
          System.out.println();
        }
      }
    }
  }

  /**
   * Get memberships for a member for the given fields.
   * 
   * @param member
   * @param fields
   *          a set of 'list' fields
   * @return a set of memberships
   * @throws SchemaException
   */
  protected static Set<Membership> getAllImmediateMemberships(Member member, Set<Field> fields) throws SchemaException {

    Set<Membership> memberships = new LinkedHashSet<Membership>();
    for (Field field : fields) {
      
      Set<Object[]> rows = new MembershipFinder()
        .addMemberId(member.getId()).addField(field).assignEnabled(null).assignMembershipType(MembershipType.IMMEDIATE)
        .findMembershipsMembers();
      for (Object[] row : GrouperUtil.nonNull(rows)) {
        memberships.add((Membership) row[0]);
      }
    }
    return memberships;
  }

  /**
   * Delete unresolvable member from group and field.
   * 
   * @param member
   * @param group
   * @param field
   * @throws IllegalArgumentException
   * @throws InsufficientPrivilegeException
   * @throws MemberDeleteException
   * @throws SourceUnavailableException
   * @throws SchemaException
   */
  public static void deleteUnresolvableMember(Member member, Group group, Field field) throws IllegalArgumentException,
      InsufficientPrivilegeException, MemberDeleteException, SourceUnavailableException, SchemaException {

    group.deleteMember(getUSDUSubject(member), field, false);
  }

  /**
   * Revoke unresolvable member's privilege from group.
   * 
   * @param member
   * @param group
   * @param privilege
   * @throws IllegalArgumentException
   * @throws InsufficientPrivilegeException
   * @throws RevokePrivilegeException
   * @throws SchemaException
   * @throws SourceUnavailableException
   */
  public static void deleteUnresolvableMember(Member member, Group group, Privilege privilege)
      throws IllegalArgumentException, InsufficientPrivilegeException, RevokePrivilegeException, SchemaException,
      SourceUnavailableException {

    group.revokePriv(getUSDUSubject(member), privilege, false);
  }

  /**
   * Revoke unresolvable member's privilege from stem.
   * 
   * @param member
   * @param stem
   * @param privilege
   * @throws IllegalArgumentException
   * @throws InsufficientPrivilegeException
   * @throws RevokePrivilegeException
   * @throws SchemaException
   * @throws SourceUnavailableException
   */
  public static void deleteUnresolvableMember(Member member, Stem stem, Privilege privilege)
      throws IllegalArgumentException, InsufficientPrivilegeException, RevokePrivilegeException, SchemaException,
      SourceUnavailableException {

    stem.revokePriv(getUSDUSubject(member), privilege, false);
  }
  
  /**
   * Revoke unresolvable member's privilege from attribute def.
   * 
   * @param member
   * @param attributeDef
   * @param privilege
   * @throws IllegalArgumentException
   * @throws InsufficientPrivilegeException
   * @throws RevokePrivilegeException
   * @throws SchemaException
   * @throws SourceUnavailableException
   */
  public static void deleteUnresolvableMember(Member member, AttributeDef attributeDef, Privilege privilege)
      throws IllegalArgumentException, InsufficientPrivilegeException, RevokePrivilegeException, SchemaException,
      SourceUnavailableException {

    attributeDef.getPrivilegeDelegate().revokePriv(getUSDUSubject(member), privilege, false);
  }

  /**
   * Return a subject suitable for deleting membership and privileges.
   * 
   * @param member
   * @return a contrived subject
   * @throws IllegalArgumentException
   * @throws SourceUnavailableException
   */
  protected static Subject getUSDUSubject(Member member) throws IllegalArgumentException, SourceUnavailableException {

    return new SubjectImpl(member.getSubjectId(), member.getSubjectId(), 
        null, member.getSubjectTypeId(), member.getSubjectSourceId());
  }

  /**
   * Return the identifier of the
   * <code>GrouperSourceAdapter/code>, probably but not
   * definitely 'g:gsa'.
   * 
   * @return
   *  the GrouperSourceAdapter identifier
   */
  protected static String getGrouperSourceAdapterId() {
    
    return SubjectFinder.internal_getGSA().getId();
  }

  /**
   * Get fields of which a subject might be a member. Includes all fields of
   * type FieldType.LIST, FieldType.ACCESS, and FieldType.NAMING.
   * 
   * @return set of fields
   * @throws SchemaException
   */
  protected static Set<Field> getMemberFields() throws SchemaException {

    Set<Field> listFields = new LinkedHashSet<Field>();
    for (Object field : FieldFinder.findAllByType(FieldType.LIST)) {
      listFields.add((Field) field);
    }
    for (Object field : FieldFinder.findAllByType(FieldType.ACCESS)) {
      listFields.add((Field) field);
    }
    for (Object field : FieldFinder.findAllByType(FieldType.NAMING)) {
      listFields.add((Field) field);
    }
    return listFields;
  }

  /**
   * Map fields to privileges.
   * 
   * @param field
   * @return the privilege matching the given field or null
   */
  protected static Privilege getPrivilege(Field field) {

    return list2priv.get(field.getName());
  }
  
  /**
   * Deprecated - USDU is now run using the daemon and doesn't call this method
   * 
   * Find members whose subjects can not be found by their source.
   * 
   * @param s
   *          GrouperSession
   * @param source
   *          if null will find members from all sources
   * @deprecated
   * @return unresolvable members
   */
  public static Set<Member> getUnresolvableMembers(GrouperSession s, Source source) {
    return getUnresolvableMembers(s, source, null);
  }

  /**
   * Deprecated - USDU is now run using the daemon and doesn't call this method
   * 
   * Find members whose subjects can not be found by their source.
   * 
   * @param s
   *          GrouperSession
   * @param source
   *          if null will find members from all sources
   * @param memberIdToSubjectMap 
   *          if you'd like this map filled with subjects as they are resolved
   * @deprecated
   * @return unresolvable members
   */
  public static Set<Member> getUnresolvableMembers(GrouperSession s, Source source, Map<String, Subject> memberIdToSubjectMap) {

    Set<Member> members = new LinkedHashSet<Member>();

    // removing this code that was added recently to make sure we resolve all members again to update grouper_members table
    /*
    Set<MultiKey> resolvedSourceIdsSubjectIds = new HashSet<MultiKey>();
    
    for (Source currentSource : SourceManager.getInstance().getSources()) {
      
      if (source == null || StringUtils.equals(currentSource.getId(),source.getId())) {
        try {
          Set<String> subjectIds = currentSource.retrieveAllSubjectIds();
          for (String subjectId : GrouperUtil.nonNull(subjectIds)) {
            resolvedSourceIdsSubjectIds.add(new MultiKey(currentSource.getId(), subjectId));
          }

        } catch (UnsupportedOperationException uoe) {
          // ignore
        }
      }
    }
    */
    
    for (Object m : MemberFinder.findAllUsed(s, source)) {

      Member member = (Member) m;

      /*
      //see if in the bulk retrieve
      MultiKey multiKey = new MultiKey(member.getSubjectSourceId(), member.getSubjectId());
      if (resolvedSourceIdsSubjectIds.contains(multiKey)) {
        continue;
      }
      */
      
      if (!isMemberResolvable(s, member, memberIdToSubjectMap)) {
        members.add(member);
      }
    }

    return members;
  }
  
  /**
   * Check if this member's subject can be found in a source.
   * 
   * @param s
   * @param member
   * @return Boolean true if member's subject is found in source
   */
  public static boolean isMemberResolvable(GrouperSession s, Member member) {
    return isMemberResolvable(s, member, null);
  }

  /**
   * Check if this member's subject can be found in a source.
   * 
   * @param s
   * @param member
   * @param memberIdToSubjectMap if you'd like this map filled with subjects as they are resolved
   * @return Boolean true if member's subject is found in source
   */
  public static boolean isMemberResolvable(GrouperSession s, Member member, Map<String, Subject> memberIdToSubjectMap) {

    /*
     * Speedup ala Gary Brown: Calling member.getSubject() causes a
     * GrouperSubject to be initialised which calls group.getAttributes() which
     * might be expensive for groups with a large number of attributes. If this
     * is the GrouperSourceAdapter (g:gsa) source call GroupFinder.findByUuid
     * instead.
     */
    if (member.getSubjectSourceId().equals(getGrouperSourceAdapterId())) {
      try {
        GroupFinder.findByUuid(s, member.getSubjectId(), true);
        return true;
      } catch (GroupNotFoundException e) {
        return false;
      }
    }

    try {
      // Changed because member.getSubject now always returns a LazySubject
      //member.getSubject();
      Subject subject = SubjectFinder.findByIdAndSource(member.getSubjectId(),member.getSubjectSourceId(), true, true);
      if (memberIdToSubjectMap != null) {
        memberIdToSubjectMap.put(member.getId(), subject);
        member.updateMemberAttributes(subject, true);
      }
      return true;
    } catch (SubjectNotFoundException e) {
      return false;
    } catch (SubjectNotUniqueException e) {
    	return false;
    }catch (SourceUnavailableException e) {
    	return true;
    }
  }


}
