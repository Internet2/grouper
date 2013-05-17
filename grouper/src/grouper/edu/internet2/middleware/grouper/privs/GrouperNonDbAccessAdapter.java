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

package edu.internet2.middleware.grouper.privs;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.exception.GrantPrivilegeAlreadyExistsException;
import edu.internet2.middleware.grouper.exception.GrantPrivilegeException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.MemberAddAlreadyExistsException;
import edu.internet2.middleware.grouper.exception.MemberAddException;
import edu.internet2.middleware.grouper.exception.MemberDeleteAlreadyDeletedException;
import edu.internet2.middleware.grouper.exception.MemberDeleteException;
import edu.internet2.middleware.grouper.exception.MembershipAlreadyExistsException;
import edu.internet2.middleware.grouper.exception.RevokePrivilegeAlreadyRevokedException;
import edu.internet2.middleware.grouper.exception.RevokePrivilegeException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.internal.dao.MembershipDAO;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.QueryPaging;
import edu.internet2.middleware.grouper.internal.dao.QuerySort;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.subj.LazySubject;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/** 
 * This is the base grouper implementation which implements the required
 * access adapter methods, but not the db specific ones.  This should be
 * slower and more explicit than the GrouperAccessAdapter (subclass)
 * </p>
 * @author  blair christensen.
 * @version $Id: GrouperNonDbAccessAdapter.java,v 1.8 2009-12-07 07:31:09 mchyzer Exp $
 */
public class GrouperNonDbAccessAdapter extends BaseAccessAdapter implements AccessAdapter {

  /** */
  protected final static Map<Privilege, String> priv2list;

  // STATIC //
  static {
    Map<Privilege, String> map = new HashMap<Privilege, String>();
    map.put(  AccessPrivilege.ADMIN , Field.FIELD_NAME_ADMINS    );
    map.put(  AccessPrivilege.OPTIN , Field.FIELD_NAME_OPTINS    );
    map.put(  AccessPrivilege.OPTOUT, Field.FIELD_NAME_OPTOUTS   );
    map.put(  AccessPrivilege.READ  , Field.FIELD_NAME_READERS   );
    map.put(  AccessPrivilege.UPDATE, Field.FIELD_NAME_UPDATERS  );
    map.put(  AccessPrivilege.VIEW  , Field.FIELD_NAME_VIEWERS   );
    map.put(  AccessPrivilege.GROUP_ATTR_READ  , Field.FIELD_NAME_GROUP_ATTR_READERS   );
    map.put(  AccessPrivilege.GROUP_ATTR_UPDATE  , Field.FIELD_NAME_GROUP_ATTR_UPDATERS   );
    priv2list = Collections.unmodifiableMap(new HashMap(map));
  } // static


  // PUBLIC INSTANCE METHODS //

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperNonDbAccessAdapter.class);

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AccessAdapter#getSubjectsWithPriv(edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.grouper.Group, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public Set getSubjectsWithPriv(GrouperSession s, Group g, Privilege priv) 
    throws  SchemaException
  {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(s);
    return MembershipFinder.internal_findGroupSubjects(
      s, g, FieldFinder.find( priv.getListName(), true )
    );
  }

  
  /**
   * Get all groups where this subject has this privilege.
   * <pre class="eg">
   * try {
   *   Set isAdmin = ap.getGroupsWhereSubjectHasPriv(
   *     s, subj, AccessPrivilege.ADMIN
   *   );
   * }
   * catch (SchemaException eS) {
   *   // Invalid priv
   * }
   * </pre>
   * @param   s     Get privileges within this session context.
   * @param   subj  Get privileges for this subject.
   * @param   priv  Get this privilege.
   * @return  Set of {@link Group} objects.
   * @throws  SchemaException
   */
  public Set<Group> getGroupsWhereSubjectHasPriv(
    GrouperSession s, Subject subj, Privilege priv
  ) 
    throws  SchemaException
  {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(s);
    Set<Group> groups = new LinkedHashSet<Group>();
    try {
      Field f = priv.getField();
      // This subject
      groups.addAll( 
        GrouperPrivilegeAdapter.internal_getGroupsWhereSubjectHasPriv( s, MemberFinder.findBySubject(s, subj, true), f ) 
      );
    }
    catch (GroupNotFoundException eGNF) {
      String msg = E.GAA_GNF + eGNF.getMessage();
      LOG.error(msg);
    }
    return groups;
  }  

  /**
   * Get all groups where this subject doesnt have this privilege.
   * @param grouperSession 
   * @param stemId 
   * @param scope 
   * @param subject 
   * @param privilege 
   * @param considerAllSubject
   * @param sqlLikeString
   * @return groups
   */
  public Set<Group> getGroupsWhereSubjectDoesntHavePrivilege(
      GrouperSession grouperSession, String stemId, Scope scope, Subject subject,
      Privilege privilege, boolean considerAllSubject, 
      String sqlLikeString) {

    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(grouperSession);
    Set<Group> groups = new LinkedHashSet();

    // This subject
    groups.addAll( 
      GrouperPrivilegeAdapter.internal_getGroupsWhereSubjectDoesntHavePriv( grouperSession, 
          stemId, scope, subject, privilege, considerAllSubject, sqlLikeString) 
    );
    return groups;
  
  }  

  /**
   * @see edu.internet2.middleware.grouper.privs.AccessAdapter#getStemsWhereGroupThatSubjectHasPrivilege(edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public Set<Stem> getStemsWhereGroupThatSubjectHasPrivilege(
      GrouperSession grouperSession, Subject subject, Privilege privilege) {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(grouperSession);
    Field field = privilege.getField();

    Set<Stem> stems =
        GrouperPrivilegeAdapter.internal_getStemsWithGroupsWhereSubjectHasPriv( grouperSession, 
            MemberFinder.findBySubject(grouperSession, subject, true), field ); 

    return stems;
  }


  /**
   * Get all privileges held by this subject on this group.
   * <pre class="eg">
   * Set privs = ap.getPrivs(s, g, subj);
   * </pre>
   * @param   s     Get privileges within this session context.
   * @param   g     Get privileges on this group.
   * @param   subj  Get privileges for this member.
   * @return  Set of privileges.
   */
  public Set getPrivs(GrouperSession s, Group g, Subject subj) {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(s);
    Set privs = new LinkedHashSet();
    try {
      Member        m     = MemberFinder.findBySubject(s, subj, true);
      //Member        all   = MemberFinder.internal_findAllMember();     
      MembershipDAO dao   = GrouperDAOFactory.getFactory().getMembership();
      Iterator      it;
  
  	
     //2007-11-02 Gary Brown
     //Avoid doing 6 queries - get everything at once
     //Also don't add GropuperAll privs - do that in 
     //GrouperAllAccessResolver
        it  = dao.findAllByGroupOwnerAndMember(g.getUuid(), m.getUuid(), true).iterator(); 
        privs.addAll( GrouperPrivilegeAdapter.internal_getPrivs(s, g,subj, m, null, it) );
        /*
         * Done through GrouperAllAccessAdapter
         * if (!m.equals(all)) {
          it  = dao.findAllByOwnerAndMemberAndField( g.getUuid(), ( (MemberDTO) all.getDTO() ).getUuid(), f ).iterator();
          privs.addAll( GrouperPrivilegeAdapter.internal_getPrivs(s, subj, all, p, it) );
        }*/
     
    }
    catch (SchemaException eS) {
      LOG.error( eS.getMessage());
    }
    return privs;
  }



  // public Set getPrivs(s, g, subj)
  
  /**
   * Grant the privilege to the subject on this group.
   * <pre class="eg">
   * try {
   *   ap.grantPriv(s, g, subj, AccessPrivilege.ADMIN);
   * }
   * catch (GrantPrivilegeException e0) {
   *   // Unable to grant the privilege
   * }
   * catch (InsufficientPrivilegeException e1) {
   *   // Not privileged to grant the privilege
   * }
   * </pre>
   * @param   s     Grant privilege in this session context.
   * @param   g     Grant privilege on this group.
   * @param   subj  Grant privilege to this subject.
   * @param   priv  Grant this privilege.   
   * @param uuid
   * @throws  GrantPrivilegeException
   * @throws  InsufficientPrivilegeException
   * @throws  SchemaException
   */
  public void grantPriv(
    GrouperSession s, final Group g, final Subject subj, final Privilege priv, final String uuid)
    throws  GrantPrivilegeException, InsufficientPrivilegeException, SchemaException {
    try {
      GrouperSession.callbackGrouperSession(s, new GrouperSessionHandler() {
  
        public Object callback(GrouperSession grouperSession)
            throws GrouperSessionException {
          try {
            GrouperSession.validate(grouperSession);
            Field f = priv.getField();
            if ( !FieldType.ACCESS.equals( f.getType() ) ) {
              throw new SchemaException( E.FIELD_INVALID_TYPE + f.getType() );
            }
            if ( !g.internal_canWriteField( grouperSession.getSubject(), f ) ) {
              throw new GrouperSessionException(new InsufficientPrivilegeException());
            }
            Membership.internal_addImmediateMembership(grouperSession, g, subj, f, uuid);
          } catch (MemberAddException eMA) {
            if (eMA instanceof MemberAddAlreadyExistsException) {
              throw new GrouperSessionException(new GrantPrivilegeAlreadyExistsException(eMA.getMessage(), eMA));
            }
            throw new GrouperSessionException(new GrantPrivilegeException(eMA.getMessage(), eMA));
          } catch (SchemaException se) {
            throw new GrouperSessionException(se);
          }
          return null;
        }
        
      });
    } catch (GrouperSessionException gse) {
      if (gse.getCause() instanceof GrantPrivilegeException) {
        throw (GrantPrivilegeException)gse.getCause();
      }
      if (gse.getCause() instanceof InsufficientPrivilegeException) {
        throw (InsufficientPrivilegeException)gse.getCause();
      }
      if (gse.getCause() instanceof SchemaException) {
        throw (SchemaException)gse.getCause();
      }
      throw gse;
    }
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AccessAdapter#hasPriv(edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.grouper.Group, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public boolean hasPriv(GrouperSession s, Group g, Subject subj, Privilege priv)
    throws  SchemaException
  {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(s);
    boolean rv = false;
    Member m = MemberFinder.findBySubject(s, subj, true);
    rv = m.isMember(g, priv.getField() );
    return rv;
  }


  // public void revokePriv(s, g, subj, priv)
  
  /**
   * Copies privileges for subjects that have the specified privilege on g1 to g2.
   * @param s 
   * @param g1 
   * @param g2 
   * @param priv 
   * @throws InsufficientPrivilegeException 
   * @throws GrantPrivilegeException 
   * @throws SchemaException 
   */
  public void privilegeCopy(GrouperSession s, Group g1, Group g2, Privilege priv)
      throws InsufficientPrivilegeException, GrantPrivilegeException, SchemaException {
    GrouperSession.validate(s);
    
    Field f = priv.getField();
    PrivilegeHelper.dispatch(s, g1, s.getSubject(), f.getReadPriv());
    
    Iterator<Membership> membershipsIter = GrouperDAOFactory.getFactory().getMembership()
      .findAllByGroupOwnerAndFieldAndType(g1.getUuid(), f,
          MembershipType.IMMEDIATE.getTypeString(), false).iterator();

    while (membershipsIter.hasNext()) {
      Membership existingMembership = membershipsIter.next();
      Membership copiedMembership = existingMembership.clone();
      copiedMembership.setOwnerGroupId(g2.getUuid());
      copiedMembership.setCreatorUuid(s.getMemberUuid());
      copiedMembership.setCreateTimeLong(new Date().getTime());
      copiedMembership.setImmediateMembershipId(GrouperUuid.getUuid());
      copiedMembership.setHibernateVersionNumber(-1L);
      
      try {
        GrouperDAOFactory.getFactory().getMembership().save(copiedMembership);
      } catch (MembershipAlreadyExistsException e) {
        // this is okay
      }
    }
  }


  /**
   * Copies privileges of type priv on any subject for the given Subject subj1 to the given Subject subj2.
   * For instance, if subj1 has ADMIN privilege to Group x, this method will result with subj2
   * having ADMIN privilege to Group x.
   * @param s 
   * @param subj1
   * @param subj2
   * @param priv 
   * @throws InsufficientPrivilegeException 
   * @throws GrantPrivilegeException 
   * @throws SchemaException 
   */
  public void privilegeCopy(GrouperSession s, Subject subj1, Subject subj2, Privilege priv)
      throws InsufficientPrivilegeException, GrantPrivilegeException, SchemaException {
    GrouperSession.validate(s);
    
    Field f = priv.getField();

    Set<Membership> memberships = GrouperDAOFactory.getFactory().getMembership()
        .findAllImmediateByMemberAndField(MemberFinder.findBySubject(s, subj1, true).getUuid(), f, false);
    
    if (memberships.size() == 0) {
      return;
    }
        
    Member member = MemberFinder.findBySubject(s, subj2, true);
  
    Iterator<Membership> membershipsIter = memberships.iterator();
    while (membershipsIter.hasNext()) {
      Membership existingMembership = membershipsIter.next();
      Group g;
      try {
        g = existingMembership.getOwnerGroup();
      } catch (GroupNotFoundException e1) {
        throw new GrouperException(e1.getMessage(), e1);
      }
      PrivilegeHelper.dispatch(s, g, s.getSubject(), f.getWritePriv());
      
      Membership copiedMembership = existingMembership.clone();
      copiedMembership.setMemberUuid(member.getUuid());
      copiedMembership.setMember(member);
      copiedMembership.setCreatorUuid(s.getMemberUuid());
      copiedMembership.setCreateTimeLong(new Date().getTime());
      copiedMembership.setImmediateMembershipId(GrouperUuid.getUuid());
      copiedMembership.setHibernateVersionNumber(-1L);
      GrouperDAOFactory.getFactory().getMembership().save(copiedMembership);
    }
    
  }


  // public boolean hasPriv(s, g, subj, priv)
  
  /**
   * Revoke this privilege from everyone on this group.
   * <pre class="eg">
   * try {
   *   ap.revokePriv(s, g, AccessPrivilege.ADMIN);
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to revoke the privilege
   * }
   * catch (RevokePrivilegeException eRP) {
   *   // Unable to revoke the privilege
   * }
   * </pre>
   * @param   s     Revoke privilege in this session context.
   * @param   g     Revoke privilege on this group.
   * @param   priv  Revoke this privilege.   
   * @throws  InsufficientPrivilegeException
   * @throws  RevokePrivilegeException
   * @throws  SchemaException
   */
  public void revokePriv(GrouperSession s, Group g, Privilege priv)
    throws  InsufficientPrivilegeException, 
            RevokePrivilegeException,
            SchemaException
  {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(s);
    Field f = priv.getField();
    if ( !FieldType.ACCESS.equals( f.getType() ) ) {
      throw new SchemaException( E.FIELD_INVALID_TYPE + f.getType() );
    }
    if ( !g.internal_canWriteField( s.getSubject(), f ) ) {
      throw new InsufficientPrivilegeException();
    }
    try {
      Membership.internal_deleteAllField(s, g, f);
    }
    catch (MemberDeleteException eMD) {
      throw new RevokePrivilegeException( eMD.getMessage(), eMD );
    }
  }


  /**
   * Revoke the privilege from the subject on this group.
   * <pre class="eg">
   * try {
   *   ap.revokePriv(s, g, subj, AccessPrivilege.ADMIN);
   * }
   * catch (InsufficientPrivilegeException e0) {
   *   // Not privileged to grant the privilege
   * }
   * catch (RevokePrivilegeException e2) {
   *   // Unable to revoke the privilege
   * }
   * </pre>
   * @param   s     Revoke privilege in this session context.
   * @param   g     Revoke privilege on this group.
   * @param   subj  Revoke privilege from this subject.
   * @param   priv  Revoke this privilege.   
   * @throws  InsufficientPrivilegeException
   * @throws  RevokePrivilegeException
   * @throws  SchemaException
   */
  public void revokePriv(
    GrouperSession s, Group g, Subject subj, Privilege priv
  )
    throws  InsufficientPrivilegeException, 
            RevokePrivilegeException,
            SchemaException
  {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(s);
    Field f = priv.getField();
    if ( !FieldType.ACCESS.equals( f.getType() ) ) {
      throw new SchemaException( E.FIELD_INVALID_TYPE + f.getType() );
    }
    if ( !g.internal_canWriteField( s.getSubject(), f ) ) {
      throw new InsufficientPrivilegeException();
    }
    try {
      Membership.internal_delImmediateMembership(s, g, subj, f);
    } catch (MemberDeleteAlreadyDeletedException eMD) {
      throw new RevokePrivilegeAlreadyRevokedException( eMD.getMessage(), eMD );
    } catch (MemberDeleteException eMD) {
      throw new RevokePrivilegeException( eMD.getMessage(), eMD );
    }
  }


  /**
   * @see edu.internet2.middleware.grouper.privs.AccessAdapter#revokeAllPrivilegesForSubject(edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.subject.Subject)
   */
  public void revokeAllPrivilegesForSubject(GrouperSession grouperSession, Subject subject) {
    GrouperSession.validate(grouperSession);
    
    // right now this method only gets executed as the root subject.
    // so we're not doing any privilege checking just to save on performance.
    if (!SubjectHelper.eq(SubjectFinder.findRootSubject(), grouperSession.getSubject())) {
      throw new InsufficientPrivilegeException();
    }
    
    Member member = MemberFinder.findBySubject(grouperSession, subject, true);
    Set<Membership> memberships = GrouperDAOFactory.getFactory().getMembership()
        .findAllImmediateByMemberAndFieldType(member.getUuid(), FieldType.ACCESS.getType(), false);
    Iterator<Membership> iter = memberships.iterator();
    while (iter.hasNext()) {
      Membership mship = iter.next();
      mship.delete();
    }
  }


  /**
   * @see edu.internet2.middleware.grouper.privsAccessAdapter#retrievePrivileges(edu.internet2.middleware.grouper.GrouperSession, Group, java.util.Set, edu.internet2.middleware.grouper.membership.MembershipType, edu.internet2.middleware.grouper.internal.dao.QueryPaging, Set)
   */
  public Set<PrivilegeSubjectContainer> retrievePrivileges(GrouperSession grouperSession,
      Group group, Set<Privilege> privileges,
      MembershipType membershipType, QueryPaging queryPaging, Set<Member> additionalMembers) {
    
    Map<String, Object> debugLog = null;
    if (LOG.isDebugEnabled()) {
       debugLog = new LinkedHashMap<String, Object>();
       debugLog.put("groupId", group.getId());
       debugLog.put("groupName", group.getName());
       if (GrouperUtil.length(privileges) > 0) {
         debugLog.put("privileges", GrouperUtil.collectionToString(privileges));
       }
       if (membershipType != null) {
         debugLog.put("membershipType", membershipType.name());
       }
       debugLog.put("additionalMembersSize", GrouperUtil.length(additionalMembers));
       if (queryPaging != null) {
         debugLog.put("queryPaging", queryPaging);
       }
    }
    Exception exception = null;
    try {
      //note, no need for GrouperSession inverse of control
      GrouperSession.validate(grouperSession);
  
      ///check privs
      if (!grouperSession.getMember().hasAdmin(group)) {
        throw new InsufficientPrivilegeException("Subject: " 
            + GrouperUtil.subjectToString(grouperSession.getSubject()) 
            + " does not have admin on group: " + group.getName());
      }
      
      Set<Field> fields = null;
      if (GrouperUtil.length(privileges) > 0) {
        fields = new LinkedHashSet<Field>();
        for (Privilege privilege : privileges) {
          fields.add(privilege.getField());
        }
      }
      
      Set<Object[]> memberships = null;
      
      QuerySort querySort = new QuerySort("m.subjectIdDb", true);
      querySort.insertSortToBeginning("m.subjectSourceIdDb", true);
      QueryOptions queryOptions = new QueryOptions();
      queryOptions.sort(querySort);
  
      //see if there is paging
      if (queryPaging != null) {
        
        queryOptions.paging(queryPaging);
        //dont cache, changing the effective etc messes it up
        queryPaging.setCacheTotalCount(false);
        
        //lets get the members
        List<Member> members = GrouperDAOFactory.getFactory().getMembership().findAllMembersByGroupOwnerOptions(
            group.getId(), membershipType, fields, null, true, queryOptions);
        
        if (LOG.isDebugEnabled()) {
          debugLog.put("membersSize", GrouperUtil.length(members));
          
        }
        
        //ok, if there are results...
        if (GrouperUtil.length(members) > 0) {
          
          //lets get the memberships for these members
          List<String> memberIds = new ArrayList<String>();
          for (Member member : members) {
            memberIds.add(member.getUuid());
          }
          queryOptions = new QueryOptions();
          queryOptions.sort(querySort);
          //note, we arent paging here... we paged the members
          memberships = GrouperDAOFactory.getFactory().getMembership().findAllByGroupOwnerOptions(
              group.getId(), memberIds, membershipType, fields, null, true, queryOptions);
          
          if (LOG.isDebugEnabled()) {
            debugLog.put("membershipsSize", GrouperUtil.length(memberships));
          }
        }
        
      } else {
        
        //note, still sort by subject
        memberships = GrouperDAOFactory.getFactory().getMembership().findAllByGroupOwnerOptions(
            group.getId(), membershipType, fields, null, true, queryOptions);
        
        if (LOG.isDebugEnabled()) {
          debugLog.put("membershipsSize", GrouperUtil.length(memberships));
        }
  
      }
      
      //lets non-null it
      memberships = GrouperUtil.nonNull(memberships);
      
      //lets put it all back together...
      Set<PrivilegeSubjectContainer> results = new LinkedHashSet<PrivilegeSubjectContainer>();
      Map<MultiKey, PrivilegeSubjectContainerImpl> resultsMap = new HashMap<MultiKey, PrivilegeSubjectContainerImpl>();
      
  
      //now we need to factor in the additionals
      if (GrouperUtil.length(additionalMembers) > 0) {
        
        if (GrouperUtil.length(additionalMembers) > 0) {
          
          //lets get the memberships for these members
          List<String> memberIds = new ArrayList<String>();
          for (Member member : additionalMembers) {
            memberIds.add(member.getUuid());
            
            //put in a placeholder
            PrivilegeSubjectContainerImpl privilegeSubjectContainerImpl = new PrivilegeSubjectContainerImpl();
            privilegeSubjectContainerImpl.setSubject(new LazySubject(member));
            results.add(privilegeSubjectContainerImpl);
            MultiKey multiKey = new MultiKey(member.getSubjectSourceId(), member.getSubjectId());
            resultsMap.put(multiKey, privilegeSubjectContainerImpl);
            
          }
          //note, we arent paging here...
          //get those results
          Set<Object[]> additionalMemberships = GrouperDAOFactory.getFactory().getMembership().findAllByGroupOwnerOptions(
              group.getId(), memberIds, membershipType, fields, null, true, null);
  
          if (LOG.isDebugEnabled()) {
            debugLog.put("additionalMembershipsSize", GrouperUtil.length(additionalMemberships));
          }
          
          //collate this
          if (GrouperUtil.length(additionalMemberships) > 0) {
            Set<Object[]> newMemberships = new LinkedHashSet<Object[]>(additionalMemberships);
            newMemberships.addAll(memberships);
            memberships = newMemberships;
            
          }
        }
        
      }
      
      if (GrouperUtil.length(memberships) > 0) {
        
        Map<MultiKey, List<Object[]>> membershipsMap = new HashMap<MultiKey, List<Object[]>>();
        
        //this multikey is sourceid, subjectid, groupid, fieldid, 
        Map<MultiKey, PrivilegeAssignType> privilegeAssignTypeMap = new HashMap<MultiKey, PrivilegeAssignType>();
        
        //lets get all the members first, and keep the answer
        for (Object[] objectArray: memberships) {
          
          Member member = (Member)objectArray[1];
          
          MultiKey subjectKey = new MultiKey(member.getSubjectSourceId(), member.getSubjectId());
  
          PrivilegeSubjectContainerImpl privilegeSubjectContainerImpl = resultsMap.get(subjectKey);
          if (privilegeSubjectContainerImpl == null) {
            privilegeSubjectContainerImpl = new PrivilegeSubjectContainerImpl();
            privilegeSubjectContainerImpl.setSubject(new LazySubject(member));
            resultsMap.put(subjectKey, privilegeSubjectContainerImpl);
            results.add(privilegeSubjectContainerImpl);
  
          }            
  
          Membership membership = (Membership)objectArray[0];
  
          List<Object[]> membershipList = membershipsMap.get(subjectKey);
          
          if (membershipList == null) {
  
            membershipList = new ArrayList<Object[]>();
            
            membershipsMap.put(subjectKey, membershipList);
  
          }
  
          membershipList.add(objectArray);
          
          MultiKey privilegeAssignTypeKey = new MultiKey(member.getSubjectSourceId(), member.getSubjectId(), group.getId(), membership.getFieldId());
          
          PrivilegeAssignType privilegeAssignType = privilegeAssignTypeMap.get(privilegeAssignTypeKey);
          privilegeAssignType = PrivilegeAssignType.convertMembership(privilegeAssignType, membership);
          privilegeAssignTypeMap.put(privilegeAssignTypeKey, privilegeAssignType);
          
        }
        
        if (LOG.isDebugEnabled()) {
          debugLog.put("resultsSize", GrouperUtil.length(results));
        }
  
        for (PrivilegeSubjectContainer privilegeSubjectContainer : results) {
          PrivilegeSubjectContainerImpl privilegeSubjectContainerImpl = (PrivilegeSubjectContainerImpl)privilegeSubjectContainer;
          
          privilegeSubjectContainerImpl.setPrivilegeContainers(new TreeMap<String, PrivilegeContainer>());
          
          Subject subject = privilegeSubjectContainerImpl.getSubject();
          MultiKey subjectKey = new MultiKey(subject.getSourceId(), subject.getId());
          
          //lets get the memberships
          List<Object[]> membershipList = membershipsMap.get(subjectKey);
          
          if (membershipList != null) {
            for (Object[] objectArray : membershipList) {
              
              Membership membership = (Membership)objectArray[0];
              Member member = (Member)objectArray[1];
              Field field = FieldFinder.findById(membership.getFieldId(), true);
              Privilege privilege = AccessPrivilege.listToPriv(field.getName());
              if (privilege == null) {
                throw new RuntimeException("Privilege not found by list name! " + field.getName());
              }
              String privilegeName = privilege.getName();
              
              //multiple memberships could have the same result, just skip is already set
              if (privilegeSubjectContainerImpl.getPrivilegeContainers().get(privilegeName) == null) {
                PrivilegeContainerImpl privilegeContainerImpl = new PrivilegeContainerImpl();
                privilegeContainerImpl.setPrivilegeName(privilegeName);
                
                //if the subject, field, groupId match, then correlate the assign type...
                
                MultiKey privilegeAssignTypeKey = new MultiKey(member.getSubjectSourceId(), member.getSubjectId(), group.getId(), membership.getFieldId());
    
                PrivilegeAssignType privilegeAssignType = privilegeAssignTypeMap.get(privilegeAssignTypeKey);
                if (privilegeAssignType == null) {
                  throw new RuntimeException("Why is result not there???");
                }
                privilegeContainerImpl.setPrivilegeAssignType(privilegeAssignType);
                privilegeSubjectContainerImpl.getPrivilegeContainers().put(privilegeName, privilegeContainerImpl);
                
              }
            }
          }
        }
      }
      
      
      return results;
    } catch (RuntimeException e) {
      exception = e;
      LOG.error(GrouperUtil.mapToString(debugLog), e);
      throw e;
    } finally {
      if (LOG.isDebugEnabled() && exception == null) {
        LOG.debug(GrouperUtil.mapToString(debugLog));
      }
    }
  }


} // public class GrouperAccessAdapter 

