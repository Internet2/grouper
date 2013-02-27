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
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.exception.AttributeDefNotFoundException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import edu.internet2.middleware.grouper.exception.MembershipNotFoundException;
import edu.internet2.middleware.grouper.exception.QueryException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.internal.dao.MembershipDAO;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.QueryPaging;
import edu.internet2.middleware.grouper.membership.MembershipResult;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.subj.LazySubject;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;

/**
 * Find memberships within the Groups Registry.
 * 
 * A membership is the object which represents a join of member
 * and group.  Has metadata like type and creator,
 * and, if an effective membership, the parent membership
 * <p/>
 * @author  blair christensen.
 * @version $Id: MembershipFinder.java,v 1.108 2009-12-17 06:57:57 mchyzer Exp $
 */
public class MembershipFinder {
  

  /** membership ids to search for */
  private Collection<String> membershipIds;

  /** membership type to look for */
  private MembershipType membershipType;

  /** field to look for */
  private Field field;

  /** sources to look in */
  private Set<Source> sources;

  /** stem to look in */
  private Stem stem;

  /** stem scope to look in */
  private Scope stemScope;
  
  /** if we should check security */
  private boolean checkSecurity = true;

  /**
   * assign a stem scope to look in
   * @param theStemScope
   * @return this for chaining
   */
  public MembershipFinder assignStemScope(Scope theStemScope) {
    this.stemScope = theStemScope;
    return this;
  }
  
  /**
   * assign a stem to search in
   * @param theStem
   * @return this for chaining
   */
  public MembershipFinder assignStem(Stem theStem) {
    this.stem = theStem;
    return this;
  }
  
  /**
   * assign if this should check security or run as grouper system
   * @param shouldCheckSecurity
   * @return this for chaining
   */
  public MembershipFinder assignCheckSecurity(boolean shouldCheckSecurity) {
    this.checkSecurity = shouldCheckSecurity;
    return this;
  }
  
  
  /**
   * 
   */
  private Collection<String> memberIds = null;
  
  /**
   * add a member id to the search criteria
   * @param memberId
   * @return this for chaining
   */
  public MembershipFinder addMemberId(String memberId) {
    if (this.memberIds == null) {
      this.memberIds = new ArrayList<String>();
    }
    //no need to look for dupes
    if (!this.memberIds.contains(memberId)) {
      this.memberIds.add(memberId);
    }
    return this;
  }

  /**
   * add subjects
   * @param subjects
   * @return this for chaining
   */
  public MembershipFinder addSubjects(Collection<Subject> subjects) {
    
    Set<Member> members = MemberFinder.findBySubjects(subjects, false);
    
    for (Member member : GrouperUtil.nonNull(members)) {
      this.addMemberId(member.getUuid());
    }
    return this;
  }
  
  /**
   * add a collection of member ids to look for
   * @param theMemberIds
   * @return this for chaining
   */
  public MembershipFinder assignMemberIds(Collection<String> theMemberIds) {
    this.memberIds = theMemberIds;
    return this;
  }
  
  /**
   * add a subject to look for.
   * @param subject
   * @return this for chaining
   */
  public MembershipFinder addSubject(Subject subject) {
    
    //note, since we are chaining, we need to add if not found, since if we dont, it will find for
    //all subjects if no more are added
    Member member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject, true);
    return this.addMemberId(member.getUuid());
  }
  
  /**
   * 
   */
  private Collection<String> groupIds = null;
  
  /**
   * add a role id to the search criteria
   * @param groupId
   * @return this for chaining
   */
  public MembershipFinder addGroupId(String groupId) {
    if (!StringUtils.isBlank(groupId)) {
      if (this.groupIds == null) {
        this.groupIds = new ArrayList<String>();
      }
      //no need to look for dupes
      if (!this.groupIds.contains(groupId)) {
        this.groupIds.add(groupId);
      }
    }
    return this;
  }

  /**
   * assign a collection of role ids to look for
   * @param theRoleIds
   * @return this for chaining
   */
  public MembershipFinder assignRoleIds(Collection<String> theRoleIds) {
    this.groupIds = theRoleIds;
    return this;
  }
  
  /**
   * add a role to look for.
   * @param group
   * @return this for chaining
   */
  public MembershipFinder addGroup(Group group) {
    
    return this.addGroupId(group.getId());
  }
  
  /**
   * add a role to look for by name.
   * @param name
   * @return this for chaining
   */
  public MembershipFinder addGroup(String name) {
    
    Group group = GroupFinder.findByName(GrouperSession.staticGrouperSession(), name, true);
    
    return this.addGroupId(group.getId());
  }
  
  
  /** if we should look for all, or enabled only.  default is all */
  private Boolean enabled;
  
  /**
   * true means enabled only, false, means disabled only, and null means all
   * @param theEnabled
   * @return this for chaining
   */
  public MembershipFinder assignEnabled(Boolean theEnabled) {
    this.enabled = theEnabled;
    return this;
  }
  
  /**
   * based on what you are querying for, see if has membership.
   * Note, you should be looking for one subject, 
   * one group, one field, etc
   * If you are looking for multiple, it will see if anyone has that membership or any group
   * @return true if has membership, false if not
   */
  public boolean hasMembership() {
    
    return GrouperUtil.length(findMembershipsGroupsMembers()) > 0;
  }
  
  /**
   * membership result gives helper methods in processing the results
   * @return the membership result
   */
  public MembershipResult findMembershipResult() {
    
    Set<Object[]> membershipsGroupsMembers = this.findMembershipsGroupsMembers();
    String theFieldId = this.field == null ? null : this.field.getUuid();
    return new MembershipResult(membershipsGroupsMembers, theFieldId);
  }
  
  /**
   * find a set of object arrays which have a membership, group, and member inside
   * @return the set of arrays never null
   */
  public Set<Object[]> findMembershipsGroupsMembers() {

    return edu.internet2.middleware.grouper.MembershipFinder.findMemberships(this.groupIds, this.memberIds, 
        this.membershipIds, this.membershipType, this.field, this.sources, null, this.stem, this.stemScope, this.enabled, this.checkSecurity);
    
  }

  /**
   * find a permission
   * @param exceptionIfNotFound true if exception should be thrown if permission not found
   * @return the permission or null
   */
  public Membership findMembership(boolean exceptionIfNotFound) {

    Set<Object[]> memberships = findMembershipsGroupsMembers();
    
    //this should find one if it is there...
    Membership membership = null;
    
    if (GrouperUtil.length(memberships) > 1) {
      throw new RuntimeException("Why is there more than one membership found? " + this);
    }
    
    if (GrouperUtil.length(memberships) == 1) {
      membership = (Membership)memberships.iterator().next()[0];
    }
    
    if (membership == null && exceptionIfNotFound) {
      throw new RuntimeException("could not find membership: " 
          + this);
    }
    return membership;
    
  }

  /**
   * @see Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    if (enabled != null) {
      result.append("enabled: ").append(this.enabled);
    }
    if (GrouperUtil.length(this.memberIds) > 0) {
      result.append("memberIds: ").append(GrouperUtil.toStringForLog(this.memberIds, 100));
    }
    if (this.field != null) {
      result.append("field: ").append(this.field);
    }
    if (GrouperUtil.length(this.groupIds) > 0) {
      result.append("groupIds: ").append(GrouperUtil.toStringForLog(this.groupIds, 100));
    }
    if (GrouperUtil.length(this.membershipIds) > 0) {
      result.append("membershipIds: ").append(GrouperUtil.toStringForLog(this.membershipIds, 100));
    }
    if (GrouperUtil.length(this.membershipType) > 0) {
      result.append("membershipType: ").append(this.membershipType);
    }
    if (GrouperUtil.length(this.sources) > 0) {
      result.append("sources: ").append(GrouperUtil.toStringForLog(this.sources, 100));
    }
    if (GrouperUtil.length(this.stem) > 0) {
      result.append("stem: ").append(this.stem);
    }
    if (GrouperUtil.length(this.stemScope) > 0) {
      result.append("membershipType: ").append(this.membershipType);
    }
    return result.toString();
  }

  /**
   * Return the immediate membership if it exists.  
   * 
   * An immediate member is directly assigned to a stem.
   * A stem can have potentially unlimited effective 
   * memberships
   * 
   * <p/>
   * <pre class="eg">
   * </pre>
   * @param   s     Get membership within this session context.
   * @param   stem     Immediate membership has this group.
   * @param   subj  Immediate membership has this subject.
   * @param   f     Immediate membership has this list.
   * @param exceptionIfNotFound
   * @return  A {@link Membership} object
   * @throws  MembershipNotFoundException 
   * @throws  SchemaException
   */
  public static Membership findImmediateMembership(
    GrouperSession s, Stem stem, Subject subj, Field f, boolean exceptionIfNotFound
  ) throws  MembershipNotFoundException, SchemaException {
    //note, no need for GrouperSession inverse of control
    // @filtered  true
    // @session   true
    GrouperSession.validate(s);
    try {
      Member      m   = MemberFinder.findBySubject(s, subj, true);
      Membership  ms  = GrouperDAOFactory.getFactory().getMembership().findByStemOwnerAndMemberAndFieldAndType( 
          stem.getUuid(), m.getUuid(), f, MembershipType.IMMEDIATE.getTypeString(), true, true);
      PrivilegeHelper.dispatch( s, ms.getStem(), s.getSubject(), f.getReadPriv() );
      return ms;
    } catch (MembershipNotFoundException mnfe)         {
      if (exceptionIfNotFound) {
        throw mnfe;
      }
      return null;
    } catch (StemNotFoundException eGNF)         {
      //not sure why this should happen in a non-corrupt db
      if (exceptionIfNotFound) {
        throw new MembershipNotFoundException(eGNF.getMessage(), eGNF);
      }
      return null;
    } catch (InsufficientPrivilegeException eIP)  {
      if (exceptionIfNotFound) {
        throw new MembershipNotFoundException(eIP.getMessage(), eIP);
      }
      return null;
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findAllByGroupOwnerOptions(java.util.Collection, java.util.Collection, java.util.Collection, edu.internet2.middleware.grouper.membership.MembershipType, edu.internet2.middleware.grouper.Field, Set, java.lang.String, edu.internet2.middleware.grouper.Stem, edu.internet2.middleware.grouper.Stem.Scope, java.lang.Boolean)
   * @param groupIds to limit memberships to (cant have more than 100 bind variables)
   * @param memberIds to limit memberships to (cant have more than 100 bind variables)
   * @param membershipIds to limit memberships to (cant have more than 100 bind variables)
   * @param membershipType Immediate, NonImmediate, etc
   * @param field if finding one field, list here, otherwise all list fields will be returned
   * @param sources if limiting memberships of members in certain sources, list here
   * @param scope sql like string which will have a % appended to it
   * @param stem if looking in a certain stem
   * @param stemScope if looking only in this stem, or all substems
   * @param enabled null for all, true for enabled only, false for disabled only
   * @return the set of arrays of Membership, Group, and Member
   */
  public static Set<Object[]> findMemberships(Collection<String> groupIds, Collection<String> memberIds,
      Collection<String> membershipIds, MembershipType membershipType,
      Field field,  
      Set<Source> sources, String scope, Stem stem, Scope stemScope, Boolean enabled) {
    
    return findMemberships(groupIds, memberIds, membershipIds, membershipType, field, sources, scope, stem, stemScope, enabled, null);
    
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findAllByGroupOwnerOptions(java.util.Collection, java.util.Collection, java.util.Collection, edu.internet2.middleware.grouper.membership.MembershipType, edu.internet2.middleware.grouper.Field, Set, java.lang.String, edu.internet2.middleware.grouper.Stem, edu.internet2.middleware.grouper.Stem.Scope, java.lang.Boolean)
   * @param groupIds to limit memberships to (cant have more than 100 bind variables)
   * @param memberIds to limit memberships to (cant have more than 100 bind variables)
   * @param membershipIds to limit memberships to (cant have more than 100 bind variables)
   * @param membershipType Immediate, NonImmediate, etc
   * @param field if finding one field, list here, otherwise all list fields will be returned
   * @param sources if limiting memberships of members in certain sources, list here
   * @param scope sql like string which will have a % appended to it
   * @param stem if looking in a certain stem
   * @param stemScope if looking only in this stem, or all substems
   * @param enabled null for all, true for enabled only, false for disabled only
   * @param shouldCheckSecurity if we should check security, default to true
   * @return the set of arrays of Membership, Group, and Member
   */
  public static Set<Object[]> findMemberships(Collection<String> groupIds, Collection<String> memberIds,
      Collection<String> membershipIds, MembershipType membershipType,
      Field field,  
      Set<Source> sources, String scope, Stem stem, Scope stemScope, Boolean enabled, Boolean shouldCheckSecurity) {
    return GrouperDAOFactory.getFactory().getMembership().findAllByGroupOwnerOptions(groupIds, memberIds,
        membershipIds, membershipType, field, sources, scope, stem, stemScope, enabled, shouldCheckSecurity);  
  }
  
  /**
   * Return the composite membership if it exists. 
   *
   * A composite group is composed of two groups and a set operator 
   * (stored in grouper_composites table)
   * (e.g. union, intersection, etc).  A composite group has no immediate members.
   * All subjects in a composite group are effective members.
   * 
   * <p/>
   * <pre class="eg">
   * </pre>
   * @param   s     Get membership within this session context.
   * @param   g     Composite membership has this group.
   * @param   subj  Composite membership has this subject.
   * @return  A {@link Membership} object
   * @throws  MembershipNotFoundException 
   * @throws  SchemaException
   * @since   1.0
   * @deprecated see overload
   */
  @Deprecated
  public static Membership findCompositeMembership(GrouperSession s, Group g, Subject subj)
      throws  MembershipNotFoundException, SchemaException {
    
    return findCompositeMembership(s, g, subj, true);
    
  }

  /**
   * Return the composite membership if it exists. 
   *
   * A composite group is composed of two groups and a set operator 
   * (stored in grouper_composites table)
   * (e.g. union, intersection, etc).  A composite group has no immediate members.
   * All subjects in a composite group are effective members.
   * 
   * <p/>
   * <pre class="eg">
   * </pre>
   * @param   s     Get membership within this session context.
   * @param   g     Composite membership has this group.
   * @param   subj  Composite membership has this subject.
   * @param   exceptionOnNull 
   * @return  A {@link Membership} object
   * @throws  MembershipNotFoundException 
   * @throws  SchemaException
   * @since   1.0
   */
  public static Membership findCompositeMembership(GrouperSession s, Group g, Subject subj, boolean exceptionOnNull)
    throws  MembershipNotFoundException, SchemaException {

    //note, no need for GrouperSession inverse of control
     // @filtered  true
     // @session   true
    GrouperSession.validate(s);
    try {
      Field       f   = Group.getDefaultList();
      Member      m   = MemberFinder.findBySubject(s, subj, true);
      Membership  ms  = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType( 
          g.getUuid(), m.getUuid(), f, MembershipType.COMPOSITE.getTypeString(), true, true);
      PrivilegeHelper.dispatch( s, ms.getOwnerGroup(), s.getSubject(), f.getReadPriv() );
      return ms;
    } catch (MembershipNotFoundException mnfe)  {
      if (exceptionOnNull) {
        throw mnfe;
      }
      return null;
    } catch (InsufficientPrivilegeException eIP)  {
      if (exceptionOnNull) {
        throw new MembershipNotFoundException(eIP.getMessage(), eIP);
      }
      return null;
    }
  }

  /**
   * Return effective memberships.  
   * 
   * An effective member has an indirect membership to a group
   * (e.g. in a group within a group).  All subjects in a 
   * composite group are effective members (since the composite 
   * group has two groups and a set operator and no other immediate 
   * members).  Note that a member can have an immediate membership 
   * and an effective membership.
   * 
   * <p/>
   * <pre class="eg">
   * </pre>
   * @param   s     Get membership within this session context.
   * @param   g     Effective membership has this group.
   * @param   subj  Effective membership has this subject.
   * @param   f     Effective membership has this list.
   * @param   via   Effective membership has this via group.
   * @param   depth Effective membership has this depth.
   * @return  A set of {@link Membership} objects.
   * @throws  MembershipNotFoundException
   * @throws  SchemaException
   */
  public static Set<Membership> findEffectiveMemberships(
    GrouperSession s, Group g, Subject subj, Field f, Group via, int depth
  )
    throws  MembershipNotFoundException,
            SchemaException
  {
    //note, no need for GrouperSession inverse of control
     // @filtered  true
     // @session   true
    GrouperSession.validate(s);
    Set mships = new LinkedHashSet();
    Member m = MemberFinder.findBySubject(s, subj, true);
    try {
      PrivilegeHelper.dispatch( s, g, s.getSubject(), f.getReadPriv() );
      Iterator  it    = GrouperDAOFactory.getFactory().getMembership().findAllEffectiveByGroupOwner(
        g.getUuid(), m.getUuid(), f, via.getUuid(), depth, true
      ).iterator();
      Membership eff;
      while (it.hasNext()) {
        eff = (Membership) it.next();
        mships.add(eff);
      }
    }
    catch (InsufficientPrivilegeException eIP) {
      // ??? ignore
    }
    return mships;
  } // public static Membership findEffectiveMembership(s, g, subj, f, via, depth)


  /**
   * Return the immediate membership if it exists.  
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * <p/>
   * <pre class="eg">
   * </pre>
   * @param   s     Get membership within this session context.
   * @param   g     Immediate membership has this group.
   * @param   subj  Immediate membership has this subject.
   * @param   f     Immediate membership has this list.
   * @return  A {@link Membership} object
   * @throws  MembershipNotFoundException 
   * @throws  SchemaException
   * @deprecated see overload
   */
  @Deprecated
  public static Membership findImmediateMembership(
    GrouperSession s, Group g, Subject subj, Field f
    ) throws  MembershipNotFoundException, SchemaException {
    return findImmediateMembership(s, g, subj, f, true);
  }

  /**
   * Return the immediate membership if it exists.  
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * <p/>
   * <pre class="eg">
   * </pre>
   * @param   s     Get membership within this session context.
   * @param   g     Immediate membership has this group.
   * @param   subj  Immediate membership has this subject.
   * @param   f     Immediate membership has this list.
   * @param exceptionIfNotFound
   * @return  A {@link Membership} object
   * @throws  MembershipNotFoundException 
   * @throws  SchemaException
   */
  public static Membership findImmediateMembership(
    GrouperSession s, Group g, Subject subj, boolean exceptionIfNotFound) 
      throws  MembershipNotFoundException, SchemaException {
    return findImmediateMembership(s, g, subj, Group.getDefaultList(), exceptionIfNotFound);
  }

  /**
   * Return the immediate membership if it exists.  
   * 
   * An immediate member is directly assigned to a group.
   * A composite group has no immediate members.  Note that a 
   * member can have 0 to 1 immediate memberships
   * to a single group, and 0 to many effective memberships to a group.
   * A group can have potentially unlimited effective 
   * memberships
   * 
   * <p/>
   * <pre class="eg">
   * </pre>
   * @param   s     Get membership within this session context.
   * @param   g     Immediate membership has this group.
   * @param   subj  Immediate membership has this subject.
   * @param   f     Immediate membership has this list.
   * @param exceptionIfNotFound
   * @return  A {@link Membership} object
   * @throws  MembershipNotFoundException 
   * @throws  SchemaException
   */
  public static Membership findImmediateMembership(
    GrouperSession s, Group g, Subject subj, Field f, boolean exceptionIfNotFound
  ) throws  MembershipNotFoundException, SchemaException {
    //note, no need for GrouperSession inverse of control
    // @filtered  true
    // @session   true
    GrouperSession.validate(s);
    try {
      Member      m   = MemberFinder.findBySubject(s, subj, true);
      Membership  ms  = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType( 
          g.getUuid(), m.getUuid(), f, MembershipType.IMMEDIATE.getTypeString(), true, true);
      PrivilegeHelper.dispatch( s, ms.getOwnerGroup(), s.getSubject(), f.getReadPriv() );
      return ms;
    } catch (MembershipNotFoundException mnfe)         {
      if (exceptionIfNotFound) {
        throw mnfe;
      }
      return null;
    } catch (GroupNotFoundException eGNF)         {
      //not sure why this should happen in a non-corrupt db
      if (exceptionIfNotFound) {
        throw new MembershipNotFoundException(eGNF.getMessage(), eGNF);
      }
      return null;
    } catch (InsufficientPrivilegeException eIP)  {
      if (exceptionIfNotFound) {
        throw new MembershipNotFoundException(eIP.getMessage(), eIP);
      }
      return null;
    }
  }


  /**
   * Return the immediate membership if it exists.  
   * 
   * An immediate member is directly assigned to an attributeDef.
   * An attributeDef can have potentially unlimited effective 
   * memberships
   * 
   * <p/>
   * <pre class="eg">
   * </pre>
   * @param   s     Get membership within this session context.
   * @param    SchemaException     Immediate membership has this attribute def.
   * @param   subj  Immediate membership has this subject.
   * @param   f     Immediate membership has this list.
   * @param exceptionIfNotFound
   * @return  A {@link Membership} object
   * @throws  MembershipNotFoundException 
   * @throws  SchemaException
   */
  public static Membership findImmediateMembership(
    GrouperSession s, AttributeDef attributeDef, Subject subj, Field f, boolean exceptionIfNotFound
  ) throws  MembershipNotFoundException, SchemaException {
    //note, no need for GrouperSession inverse of control
    // @filtered  true
    // @session   true
    GrouperSession.validate(s);
    try {
      Member      m   = MemberFinder.findBySubject(s, subj, true);
      Membership  ms  = GrouperDAOFactory.getFactory().getMembership().findByAttrDefOwnerAndMemberAndFieldAndType(
          attributeDef.getUuid(), m.getUuid(), f, MembershipType.IMMEDIATE.getTypeString(), true, true);
      PrivilegeHelper.dispatch( s, ms.getAttributeDef(), s.getSubject(), f.getReadPriv() );
      return ms;
    } catch (MembershipNotFoundException mnfe)         {
      if (exceptionIfNotFound) {
        throw mnfe;
      }
      return null;
    } catch (AttributeDefNotFoundException eGNF)         {
      //not sure why this should happen in a non-corrupt db
      if (exceptionIfNotFound) {
        throw new MembershipNotFoundException(eGNF.getMessage(), eGNF);
      }
      return null;
    } catch (InsufficientPrivilegeException eIP)  {
      if (exceptionIfNotFound) {
        throw new MembershipNotFoundException(eIP.getMessage(), eIP);
      }
      return null;
    }
  }

  /**
   * @param dto
   * @return set of memberships
   */
  public static Set<Membership> internal_findAllChildrenNoPriv(Membership dto) {
    Set           children  = new LinkedHashSet();
    Membership child;
    Iterator      it        = GrouperDAOFactory.getFactory().getMembership().findAllChildMemberships(dto, true).iterator();
    while (it.hasNext()) {
      child = (Membership) it.next();
      children.addAll( internal_findAllChildrenNoPriv(child) );
      children.add(child);
    }
    return children;
  } // protected static Set internal_findAllChildrenNoPriv(dto)
  
  /** 
   * @param group 
   * @param field 
   * @return  A set of all <code>Member</code>'s in <i>group</i>'s list <i>field</i>.
   * @throws  IllegalArgumentException if any parameter is null.
   * @since   1.2.1
   */
  public static Set<Member> findMembers(Group group, Field field)
    throws  IllegalArgumentException {
    return findMembers(group, field, null);
  }

  /** 
   * @param group 
   * @param field 
   * @param queryOptions 
   * @return  A set of all <code>Member</code>'s in <i>group</i>'s list <i>field</i>.
   * @throws  IllegalArgumentException if any parameter is null.
   * @since   1.2.1
   */
  public static Set<Member> findMembers(Group group, Field field, QueryOptions queryOptions)
    throws  IllegalArgumentException
  {
    return findMembers(group, field, null, queryOptions);
  }
  /** 
   * @param group 
   * @param field 
   * @param sources set of sources to retrieve from or null for all
   * @param queryOptions 
   * @return  A set of all <code>Member</code>'s in <i>group</i>'s list <i>field</i>.
   * @throws  IllegalArgumentException if any parameter is null.
   * @since   1.2.1
   */
  public static Set<Member> findMembers(Group group, Field field, Set<Source> sources, QueryOptions queryOptions)
    throws  IllegalArgumentException
  {
    //note, no need for GrouperSession inverse of control
    if (group == null) { // TODO 20070814 ParameterHelper
      throw new IllegalArgumentException("null Group");
    }
    if (field == null) { // TODO 20070814 ParameterHelper
      throw new IllegalArgumentException("null Field");
    }
    Set<Member> members = null;
    try {
      GrouperSession  s   = GrouperSession.staticGrouperSession();
      PrivilegeHelper.dispatch( s, group, s.getSubject(), field.getReadPriv() );
      members = GrouperDAOFactory.getFactory().getMembership().findAllMembersByGroupOwnerAndField( 
          group.getUuid(), field, sources, queryOptions, true);
    }
    catch (InsufficientPrivilegeException eIP) {
      // ignore  
    }
    catch (SchemaException eSchema) {
      String groupName = null;
      try {
        groupName = group.getName();
      } catch (Exception e) {
        LOG.error("error getting group name", e);
      }
      throw new RuntimeException("Error retrieving members for group: " + groupName, eSchema);
    }
    return members;
  } 

  /**
   * 
   * @param s
   * @param group
   * @param f
   * @return set of subjects
   * @throws GrouperException
   */
  public static Set<Subject> internal_findGroupSubjects(GrouperSession s, Group group, Field f) 
    throws  GrouperException
  {
    GrouperSession.validate(s);
    Set       subjs = new LinkedHashSet();
    Iterator  it    = PrivilegeHelper.canViewMemberships(
      s, GrouperDAOFactory.getFactory().getMembership().findAllByGroupOwnerAndField(group.getUuid(), f, true)
    ).iterator();
    try {
      while (it.hasNext()) {
    	//2007-12-18 Gary Brown
        //Instantiating all the Subjects can be very slow. LazySubjects
    	//only make expensive calls when necessary - so a client can page 
        //results.
    	//A partial alternative may have been to always instantiate the Member of
    	//a Membership when the latter is created - assuming one query.
    	try {
    		subjs.add ( new LazySubject((Membership) it.next()) );
    	}catch(GrouperException gre) {
    		if(gre.getCause() instanceof MemberNotFoundException) {
    			throw (MemberNotFoundException) gre.getCause();
    		}
    		if(gre.getCause() instanceof SubjectNotFoundException) {
    			throw (SubjectNotFoundException) gre.getCause();
    		}
    	}
      }
    }
    catch (MemberNotFoundException eMNF) {
      String msg = "internal_findSubjects: " + eMNF.getMessage();
      LOG.fatal(msg);
      throw new GrouperException(msg, eMNF);
    }
    catch (SubjectNotFoundException eSNF) {
      String msg = "internal_findSubjects: " + eSNF.getMessage();
      LOG.fatal(msg);
      throw new GrouperException(msg, eSNF);
    }
    return subjs;
  } // public static Set internal_findSubjects(s, o, f)

  /**
   * 
   * @param s
   * @param attributeDef
   * @param f
   * @return set of subjects
   * @throws GrouperException
   */
  public static Set<Subject> internal_findAttributeDefSubjectsImmediateOnly(GrouperSession s,
      AttributeDef attributeDef, Field f) throws GrouperException {
    GrouperSession.validate(s);
    Set<Subject> subjs = new LinkedHashSet();
    try {
      PrivilegeHelper.dispatch(s, attributeDef, s.getSubject(), f.getReadPriv());
      Iterator<Member> it = null; 
      //TODO 20090919 fix this
//        GrouperDAOFactory.getFactory().getMembership()
//          .findAllMembersByGroupOwnerAndFieldAndType(group.getUuid(), f,
//              MembershipType.IMMEDIATE.getTypeString(), null, true).iterator();

      while (it.hasNext()) {
        try {
          subjs.add(new LazySubject(it.next()));
        } catch (GrouperException gre) {
          if (gre.getCause() instanceof MemberNotFoundException) {
            throw (MemberNotFoundException) gre.getCause();
          }
          if (gre.getCause() instanceof SubjectNotFoundException) {
            throw (SubjectNotFoundException) gre.getCause();
          }
        }
      }
    } catch (MemberNotFoundException eMNF) {
      String msg = "internal_findGroupSubjectsImmediateOnly: " + eMNF.getMessage();
      LOG.fatal(msg);
      throw new GrouperException(msg, eMNF);
    } catch (SubjectNotFoundException eSNF) {
      String msg = "internal_findGroupSubjectsImmediateOnly: " + eSNF.getMessage();
      LOG.fatal(msg);
      throw new GrouperException(msg, eSNF);
    } catch (InsufficientPrivilegeException e) {
      String msg = "internal_findGroupSubjectsImmediateOnly: " + e.getMessage();
      LOG.fatal(msg);
      throw new GrouperException(msg, e);
    } catch (SchemaException e) {
      String msg = "internal_findGroupSubjectsImmediateOnly: " + e.getMessage();
      LOG.fatal(msg);
      throw new GrouperException(msg, e);
    }
    return subjs;
  } 
  
  /**
   * 
   * @param s
   * @param stem
   * @param f
   * @return set of subjects
   * @throws GrouperException
   */
  public static Set<Subject> internal_findStemSubjectsImmediateOnly(GrouperSession s,
      Stem stem, Field f) throws GrouperException {
    GrouperSession.validate(s);
    Set<Subject> subjs = new LinkedHashSet();
    try {
      PrivilegeHelper.dispatch(s, stem, s.getSubject(), f.getReadPriv());
      Iterator<Member> it = GrouperDAOFactory.getFactory().getMembership()
          .findAllMembersByStemOwnerAndFieldAndType(stem.getUuid(), f,
              MembershipType.IMMEDIATE.getTypeString(), null, true).iterator();

      while (it.hasNext()) {
        try {
          subjs.add(new LazySubject(it.next()));
        } catch (GrouperException gre) {
          if (gre.getCause() instanceof MemberNotFoundException) {
            throw (MemberNotFoundException) gre.getCause();
          }
          if (gre.getCause() instanceof SubjectNotFoundException) {
            throw (SubjectNotFoundException) gre.getCause();
          }
        }
      }
    } catch (MemberNotFoundException eMNF) {
      String msg = "internal_findStemSubjectsImmediateOnly: " + eMNF.getMessage();
      LOG.fatal(msg);
      throw new GrouperException(msg, eMNF);
    } catch (SubjectNotFoundException eSNF) {
      String msg = "internal_findStemSubjectsImmediateOnly: " + eSNF.getMessage();
      LOG.fatal(msg);
      throw new GrouperException(msg, eSNF);
    } catch (InsufficientPrivilegeException e) {
      String msg = "internal_findStemSubjectsImmediateOnly: " + e.getMessage();
      LOG.fatal(msg);
      throw new GrouperException(msg, e);
    } catch (SchemaException e) {
      String msg = "internal_findStemSubjectsImmediateOnly: " + e.getMessage();
      LOG.fatal(msg);
      throw new GrouperException(msg, e);
    }
    return subjs;
  } 
  

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(MemberFinder.class);

  /**
   * 
   * @param s
   * @param stem
   * @param f
   * @return set of subjects
   */
  public static Set<Subject> internal_findSubjectsStemPriv(GrouperSession s, Stem stem, Field f) {
     // @filtered  false
     // @session   true 
    GrouperSession.validate(s);
    Membership mbs;
    Set           subjs = new LinkedHashSet();
    Iterator      it    = GrouperDAOFactory.getFactory().getMembership().findAllByStemOwnerAndField(stem.getUuid(), f, true).iterator();
    while (it.hasNext()) {
      mbs = (Membership) it.next();
      try {
    	  subjs.add ( new LazySubject(mbs) );
        //_m = dao.findByUuid( ms.getMemberUuid() );
        //subjs.add( SubjectFinder.findById( _m.getSubjectId(), _m.getSubjectTypeId(), _m.getSubjectSourceId() ) );
      }
      catch (Exception e) {
        // @exception MemberNotFoundException
        // @exception SubjectNotFoundException
        LOG.error(E.MSF_FINDSUBJECTS + e.getMessage());
      }
    }
    return subjs;
  } // public static Set internal_findSubjectsNoPriv(s, o, f)

  /**
   * 
   * @param s
   * @param g
   * @param f
   * @param type
   * @return set of members
   */
  public static Set<Member> internal_findMembersByType(GrouperSession s, Group g, Field f, String type) {
    GrouperSession.validate(s);
    Set         members = new LinkedHashSet();
    Membership  ms;
    Iterator    it      = internal_findAllByGroupOwnerAndFieldAndType(s, g, f, type).iterator();
    while (it.hasNext()) {
      ms = (Membership) it.next();
      try {
        members.add(ms.getMember());
      }
      catch (MemberNotFoundException eMNF) {
        // Ignore
      }
    }
    return members;
  } // public static Set internal_findMembersByType(s, g, f, type)

  /**
   * 
   * @param s
   * @param d
   * @param f
   * @return set of memberships
   * @throws QueryException
   */
  public static Set<Membership> internal_findAllByCreatedAfter(@SuppressWarnings("unused") GrouperSession s, Date d, Field f) 
    throws QueryException 
  {
    //note, no need for GrouperSession inverse of control
    // @filtered  false
    // @session   true
    Set         mships  = new LinkedHashSet();
    Membership  ms;
    Iterator    it      = GrouperDAOFactory.getFactory().getMembership().findAllByCreatedAfter(d, f, true).iterator();
    while (it.hasNext()) {
      ms = (Membership) it.next();
      mships.add(ms);
    }
    return mships;
  } 

  /**
   * 
   * @param s
   * @param d
   * @param f
   * @return set of memberships
   * @throws QueryException
   */
  public static Set<Membership> internal_findAllByCreatedBefore(GrouperSession s, Date d, Field f) 
    throws QueryException {
    //note, no need for GrouperSession inverse of control
    // @filtered  false
    // @session   true
    Set         mships  = new LinkedHashSet();
    Membership  ms;
    Iterator    it      = GrouperDAOFactory.getFactory().getMembership().findAllByCreatedBefore(d, f, true).iterator();
    while (it.hasNext()) {
      ms = (Membership) it.next();
      mships.add(ms);
    }
    return mships;
  } // public static Set internal_findAllByCreatedBefore(s, d, f)

  /**
   * 
   * @param s
   * @param groupOwner
   * @param f
   * @param type
   * @return set of memberships
   */
  public static Set<Membership> internal_findAllByGroupOwnerAndFieldAndType(GrouperSession s, Group groupOwner, Field f, String type) {
     // @filtered  true
     // @session   true
    GrouperSession.validate(s);
    return PrivilegeHelper.canViewMemberships(
      s, GrouperDAOFactory.getFactory().getMembership().findAllByGroupOwnerAndFieldAndType(groupOwner.getUuid(), f, type, true)
    );
  } // public static Set internal_findAllByOwnerAndFieldAndType(s, o, f, type)

  /**
   * 
   * @param s
   * @param m
   * @param f
   * @return set of memberships
   */
  public static Set<Membership> internal_findAllEffectiveByMemberAndField(
    GrouperSession s, Member m, Field f
  ) 
  {
    // @filtered  true
    // @session   true
    GrouperSession.validate(s);
    return PrivilegeHelper.canViewMemberships( 
      s, GrouperDAOFactory.getFactory().getMembership().findAllEffectiveByMemberAndField(m.getUuid(), f, true) 
    );
  } // public static Set internal_findAllEffectiveByMemberAndField(s, m, f)

  /**
   * 
   * @param s
   * @param m
   * @param f
   * @return set of memberships
   */
  public static Set<Membership> internal_findAllImmediateByMemberAndField(GrouperSession s, Member m, Field f) {
    // @filtered  true
    // @session   true
    GrouperSession.validate(s);
    return PrivilegeHelper.canViewMemberships( 
      s, GrouperDAOFactory.getFactory().getMembership().findAllImmediateByMemberAndField(m.getUuid(), f, true) 
    );
  } 

  /**
   * 
   * @param s
   * @param m
   * @param f
   * @return set of memberships
   */
  public static Set<Membership> internal_findAllNonImmediateByMemberAndField(GrouperSession s, Member m, Field f) {
    // @filtered  true
    // @session   true
    GrouperSession.validate(s);
    return PrivilegeHelper.canViewMemberships( 
      s, GrouperDAOFactory.getFactory().getMembership().findAllNonImmediateByMemberAndField(m.getUuid(), f, true) 
    );
  } 

  /**
   * 
   * @param s
   * @param m
   * @param f
   * @return set of memberships
   */
  public static Set<Membership> internal_findMemberships(GrouperSession s, Member m, Field f) {
     // @filtered  true
     // @session   true
    GrouperSession.validate(s);
    MembershipDAO dao     = GrouperDAOFactory.getFactory().getMembership();
    return dao.findMembershipsByMemberAndFieldSecure(s, m.getUuid(), f, true);
  } // public static Set internal_findMemberships(s, m, f)

  /**
   * @param start
   * @param pageSize
   * @param group
   * @param field
   * @param sortLimit
   * @param numberOfRecords (pass in array of size one to get the result size back)
   * @return the set of membership
   * @throws SchemaException
   */
  public static Set<Membership> internal_findAllImmediateByGroupAndFieldAndPage(Group group,
      Field field, int start, int pageSize, int sortLimit, int[] numberOfRecords) throws SchemaException {
    Set<Membership> allChildren;
    //get the size
    QueryOptions queryOptions = new QueryOptions().retrieveCount(true).retrieveResults(false);
    group.getImmediateMembers(field, queryOptions);
    int totalSize = queryOptions.getCount().intValue();
    
    if (GrouperUtil.length(numberOfRecords) > 0) {
      numberOfRecords[0] = totalSize;
    }

    //if there are less than the sort limit, then just get all, no problem
    if (totalSize <= sortLimit) {
      allChildren = group.getImmediateMemberships(field);
    } else {
      //get the members that we will display, sorted by subjectId
      //TODO in 1.5 sort by subject sort string when under a certain limit, huge resultsets
      //are slow for mysql
      
      QueryPaging queryPaging = new QueryPaging();
      queryPaging.setPageSize(pageSize);
      queryPaging.setFirstIndexOnPage(start);

      //.sortAsc("m.subjectIdDb")   this kills performance
      queryOptions = new QueryOptions().paging(queryPaging);

      Set<Member> members = group.getImmediateMembers(field, queryOptions);
      allChildren = group.getImmediateMemberships(field, members);
    }
    return allChildren;
  }

  /**
   * @param start
   * @param pageSize
   * @param group
   * @param sortLimit
   * @param numberOfRecords (pass in array of size one to get the result size back)
   * @return the set of membership
   * @throws SchemaException
   */
  public static Set<Membership> internal_findAllCompositeByGroupAndPage(Group group,
      int start, int pageSize, int sortLimit, int[] numberOfRecords) throws SchemaException {
    Set<Membership> allChildren;
    //get the size
    QueryOptions queryOptions = new QueryOptions().retrieveCount(true).retrieveResults(false);
    group.getCompositeMembers(queryOptions);
    int totalSize = queryOptions.getCount().intValue();
    
    if (GrouperUtil.length(numberOfRecords) > 0) {
      numberOfRecords[0] = totalSize;
    }

    //if there are less than the sort limit, then just get all, no problem
    if (totalSize <= sortLimit) {
      allChildren = group.getCompositeMemberships();
    } else {
      //get the members that we will display, sorted by subjectId
      //TODO in 1.5 sort by subject sort string when under a certain limit, huge resultsets
      //are slow for mysql
      
      QueryPaging queryPaging = new QueryPaging();
      queryPaging.setPageSize(pageSize);
      queryPaging.setFirstIndexOnPage(start);

      //.sortAsc("m.subjectIdDb")   this kills performance
      queryOptions = new QueryOptions().paging(queryPaging);

      Set<Member> members = group.getCompositeMembers(queryOptions);
      allChildren = group.getCompositeMemberships(members);
    }
    return allChildren;
  }

  /**
   * @param start
   * @param pageSize
   * @param group
   * @param field
   * @param sortLimit
   * @param numberOfRecords (pass in array of size one to get the result size back)
   * @return the set of membership
   * @throws SchemaException
   */
  public static Set<Membership> internal_findAllEffectiveByGroupAndFieldAndPage(Group group,
      Field field, int start, int pageSize, int sortLimit, int[] numberOfRecords) throws SchemaException {
    Set<Membership> allChildren;
    //get the size
    QueryOptions queryOptions = new QueryOptions().retrieveCount(true).retrieveResults(false);
    group.getEffectiveMembers(field, queryOptions);
    int totalSize = queryOptions.getCount().intValue();
    
    if (GrouperUtil.length(numberOfRecords) > 0) {
      numberOfRecords[0] = totalSize;
    }

    //if there are less than the sort limit, then just get all, no problem
    if (totalSize <= sortLimit) {
      allChildren = group.getEffectiveMemberships(field);
    } else {
      //get the members that we will display, sorted by subjectId
      //TODO in 1.5 sort by subject sort string when under a certain limit, huge resultsets
      //are slow for mysql
      
      QueryPaging queryPaging = new QueryPaging();
      queryPaging.setPageSize(pageSize);
      queryPaging.setFirstIndexOnPage(start);

      //.sortAsc("m.subjectIdDb")   this kills performance
      queryOptions = new QueryOptions().paging(queryPaging);

      Set<Member> members = group.getEffectiveMembers(field, queryOptions);
      allChildren = group.getEffectiveMemberships(field, members);
    }
    return allChildren;
  }

  /**
   * @param start
   * @param pageSize
   * @param group
   * @param field
   * @param sortLimit
   * @param numberOfRecords (pass in array of size one to get the result size back)
   * @return the set of membership
   * @throws SchemaException
   */
  public static Set<Membership> internal_findAllByGroupAndFieldAndPage(Group group,
      Field field, int start, int pageSize, int sortLimit, int[] numberOfRecords) throws SchemaException {
    Set<Membership> allChildren;
    //get the size
    QueryOptions queryOptions = new QueryOptions().retrieveCount(true).retrieveResults(false);
    group.getMembers(field, queryOptions);
    int totalSize = queryOptions.getCount().intValue();
    
    if (GrouperUtil.length(numberOfRecords) > 0) {
      numberOfRecords[0] = totalSize;
    }
    
    //if there are less than the sort limit, then just get all, no problem
    if (totalSize <= sortLimit) {
      allChildren = group.getMemberships(field);
    } else {
      //get the members that we will display, sorted by subjectId
      //TODO in 1.5 sort by subject sort string when under a certain limit, huge resultsets
      //are slow for mysql
      
      QueryPaging queryPaging = new QueryPaging();
      queryPaging.setPageSize(pageSize);
      queryPaging.setFirstIndexOnPage(start);

      //.sortAsc("m.subjectIdDb")   this kills performance
      queryOptions = new QueryOptions().paging(queryPaging);

      Set<Member> members = group.getMembers(field, queryOptions);
      allChildren = group.getMemberships(field, members);
    }
    return allChildren;
    }

  /**
   * 
   * @param s
   * @param attributeDef
   * @param f
   * @return set of subjects
   * @throws GrouperException
   */
  public static Set<Subject> internal_findAttributeDefSubjects(GrouperSession s, AttributeDef attributeDef, Field f) 
    throws  GrouperException {
    GrouperSession.validate(s);
    Set       subjs = new LinkedHashSet();
    Iterator  it    = null;
    //TODO 20090919 fix this
//    PrivilegeHelper.canViewAttributeDefs(
//      s, GrouperDAOFactory.getFactory().getMembership().findAllByAttrDefOwnerAndField(attributeDef.getId(), f, true)
//    ).iterator();
    try {
      while (it.hasNext()) {
    	//2007-12-18 Gary Brown
        //Instantiating all the Subjects can be very slow. LazySubjects
    	//only make expensive calls when necessary - so a client can page 
        //results.
    	//A partial alternative may have been to always instantiate the Member of
    	//a Membership when the latter is created - assuming one query.
    	try {
    		subjs.add ( new LazySubject((Membership) it.next()) );
    	}catch(GrouperException gre) {
    		if(gre.getCause() instanceof MemberNotFoundException) {
    			throw (MemberNotFoundException) gre.getCause();
    		}
    		if(gre.getCause() instanceof SubjectNotFoundException) {
    			throw (SubjectNotFoundException) gre.getCause();
    		}
    	}
      }
    }
    catch (MemberNotFoundException eMNF) {
      String msg = "internal_findSubjects: " + eMNF.getMessage();
      LOG.fatal(msg);
      throw new GrouperException(msg, eMNF);
    }
    catch (SubjectNotFoundException eSNF) {
      String msg = "internal_findSubjects: " + eSNF.getMessage();
      LOG.fatal(msg);
      throw new GrouperException(msg, eSNF);
    }
    return subjs;
  } // public static Set internal_findSubjects(s, o, f)

  /**
   * 
   * @param s
   * @param group
   * @param f
   * @return set of subjects
   * @throws GrouperException
   */
  public static Set<Subject> internal_findGroupSubjectsImmediateOnly(GrouperSession s,
      Group group, Field f) throws GrouperException {
    GrouperSession.validate(s);
    Set<Subject> subjs = new LinkedHashSet();
    try {
      PrivilegeHelper.dispatch(s, group, s.getSubject(), f.getReadPriv());
      Iterator<Member> it = GrouperDAOFactory.getFactory().getMembership()
          .findAllMembersByGroupOwnerAndFieldAndType(group.getUuid(), f,
              MembershipType.IMMEDIATE.getTypeString(), null, true).iterator();
  
      while (it.hasNext()) {
        try {
          subjs.add(new LazySubject(it.next()));
        } catch (GrouperException gre) {
          if (gre.getCause() instanceof MemberNotFoundException) {
            throw (MemberNotFoundException) gre.getCause();
          }
          if (gre.getCause() instanceof SubjectNotFoundException) {
            throw (SubjectNotFoundException) gre.getCause();
          }
        }
      }
    } catch (MemberNotFoundException eMNF) {
      String msg = "internal_findGroupSubjectsImmediateOnly: " + eMNF.getMessage();
      LOG.fatal(msg);
      throw new GrouperException(msg, eMNF);
    } catch (SubjectNotFoundException eSNF) {
      String msg = "internal_findGroupSubjectsImmediateOnly: " + eSNF.getMessage();
      LOG.fatal(msg);
      throw new GrouperException(msg, eSNF);
    } catch (InsufficientPrivilegeException e) {
      String msg = "internal_findGroupSubjectsImmediateOnly: " + e.getMessage();
      LOG.fatal(msg);
      throw new GrouperException(msg, e);
    } catch (SchemaException e) {
      String msg = "internal_findGroupSubjectsImmediateOnly: " + e.getMessage();
      LOG.fatal(msg);
      throw new GrouperException(msg, e);
    }
    return subjs;
  }

  /**
   * Find a membership within the registry by UUID.  This will be either the uuid or immediate uuid.
   * Security will be checked to see if the grouper session is allowed to see the membership
   * <pre class="eg">
   *   Membership membership = MembershipFinder.findByUuid(grouperSession, uuid);
   * </pre>
   * @param   grouperSession     Find membership within this session context.
   * @param   uuid  UUID of membership to find.
   * @param exceptionIfNotFound true if exception if not found
   * @param enabledOnly true for enabled only
   * @return  A {@link Membership}
   * @throws MembershipNotFoundException if not found an exceptionIfNotFound is true
   */
  public static Membership findByUuid(GrouperSession grouperSession, String uuid, boolean exceptionIfNotFound, boolean enabledOnly) 
      throws MembershipNotFoundException {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(grouperSession);
    Membership membership = GrouperDAOFactory.getFactory().getMembership().findByUuid(uuid, exceptionIfNotFound, enabledOnly);
    if ( PrivilegeHelper.canViewMembership( grouperSession.internal_getRootSession(), membership ) ) {
      return membership;
    }
    if (exceptionIfNotFound) {
      throw new MembershipNotFoundException("Not allowed to view membership: " + uuid);
    }
    return null;
  }

} // public class MembershipFinder

