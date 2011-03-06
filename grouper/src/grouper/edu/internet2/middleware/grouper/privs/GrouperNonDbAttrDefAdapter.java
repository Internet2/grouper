/*
 * Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004-2007 The University Of Chicago
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
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.attr.AttributeDef;
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
 * slower and more explicit than the GrouperAttributeDefAdapter (subclass)
 * </p>
 * @author  blair christensen.
 * @version $Id: GrouperNonDbAttrDefAdapter.java,v 1.4 2009-12-07 07:31:09 mchyzer Exp $
 */
public class GrouperNonDbAttrDefAdapter extends BaseAttrDefAdapter implements
    AttributeDefAdapter {

  /** */
  protected final static Map<Privilege, String> priv2list;

  // STATIC //
  static {
    Map<Privilege, String> map = new HashMap<Privilege, String>();
    map.put(AttributeDefPrivilege.ATTR_ADMIN, "attrAdmins");
    map.put(AttributeDefPrivilege.ATTR_OPTIN, "attrOptins");
    map.put(AttributeDefPrivilege.ATTR_OPTOUT, "attrOptouts");
    map.put(AttributeDefPrivilege.ATTR_READ, "attrReaders");
    map.put(AttributeDefPrivilege.ATTR_UPDATE, "attrUpdaters");
    map.put(AttributeDefPrivilege.ATTR_VIEW, "attrViewers");
    priv2list = Collections.unmodifiableMap(new HashMap(map));
  } // static

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperNonDbAttrDefAdapter.class);

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefAdapter#getSubjectsWithPriv(edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.grouper.attr.AttributeDef, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public Set getSubjectsWithPriv(GrouperSession s, AttributeDef attributeDef,
      Privilege priv)
      throws SchemaException {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(s);
    return MembershipFinder.internal_findAttributeDefSubjects(
        s, attributeDef, FieldFinder.find(priv.getListName(), true)
        );
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefAdapter#getAttributeDefsWhereSubjectHasPriv(edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public Set<AttributeDef> getAttributeDefsWhereSubjectHasPriv(
      GrouperSession s, Subject subj, Privilege priv)
      throws SchemaException {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(s);
    Set<AttributeDef> attributeDefs = new LinkedHashSet<AttributeDef>();
    Field f = priv.getField();
    // This subject
    attributeDefs.addAll( GrouperPrivilegeAdapter.internal_getAttributeDefsWhereSubjectHasPriv(s, MemberFinder
        .findBySubject(s, subj, true), f)
        );
    //this is done in dao
    // The ALL subject
//    if (!(SubjectHelper.eq(subj, SubjectFinder.findAllSubject()))) {
//      attributeDefs.addAll(
//          GrouperPrivilegeAdapter.internal_getAttributeDefsWhereSubjectHasPriv(s, MemberFinder
//          .internal_findAllMember(), f)
//          );
//    }
    return attributeDefs;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefAdapter#getPrivs(edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.grouper.attr.AttributeDef, edu.internet2.middleware.subject.Subject)
   */
  public Set<AttributeDefPrivilege> getPrivs(final GrouperSession grouperSession, final AttributeDef attributeDef, Subject subj) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("getPrivs() of attributeDef: " + attributeDef.getName() + " for subject " + subj.getId() + " in grouper session: " + grouperSession.getSubject().getId());
    }
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(grouperSession);

//this should let anyone read the privs, like the other access and naming adapters
//    GrouperSession.callbackGrouperSession(grouperSession.internal_getRootSession(), new GrouperSessionHandler() {
//      
//      public Object callback(GrouperSession rootSession) throws GrouperSessionException {
//        if ( !attributeDef.getPrivilegeDelegate().canAttrAdmin( grouperSession.getSubject() ) ) {
//          throw new InsufficientPrivilegeException("Subject " 
//              + GrouperUtil.subjectToString(grouperSession.getSubject()) 
//              + " cannot attrAdmin attributeDef: " + attributeDef.getName());
//        }
//        return null;
//      }
//    });

    Set<AttributeDefPrivilege> privs = new LinkedHashSet<AttributeDefPrivilege>();
    try {
      Member m = MemberFinder.findBySubject(grouperSession, subj, true);
      //Member        all   = MemberFinder.internal_findAllMember();     
      MembershipDAO dao = GrouperDAOFactory.getFactory().getMembership();
      Iterator it;

      //2007-11-02 Gary Brown
      //Avoid doing 6 queries - get everything at once
      //Also don't add GropuperAll privs - do that in 
      //GrouperAllAccessResolver
      Set<Membership> memberships = dao.findAllByAttrDefOwnerAndMember(attributeDef.getUuid(), m.getUuid(), true);
      it = memberships.iterator();
      Set<AttributeDefPrivilege> attributeDefPrivileges = (Set<AttributeDefPrivilege>)(Object)GrouperPrivilegeAdapter.internal_getPrivs(grouperSession, attributeDef, subj, m, null, it);
      privs.addAll(attributeDefPrivileges);

      if (LOG.isDebugEnabled()) {
        StringBuilder result = new StringBuilder();
        for (AttributeDefPrivilege attributeDefPrivilege : attributeDefPrivileges) {
          result.append(attributeDefPrivilege.getName()).append(", ");
        }
        LOG.debug("getPrivs() of attributeDef: " + attributeDef.getName() + " for subject " + subj.getId() + " in grouper session: " + grouperSession.getSubject().getId() + ", returned: " + result);
      }

      /*
       * Done through GrouperAllAccessAdapter
       * if (!m.equals(all)) {
        it  = dao.findAllByOwnerAndMemberAndField( g.getUuid(), ( (MemberDTO) all.getDTO() ).getUuid(), f ).iterator();
        privs.addAll( GrouperPrivilegeAdapter.internal_getPrivs(s, subj, all, p, it) );
      }*/

    } catch (SchemaException eS) {
      LOG.error(eS.getMessage());
    }
    return privs;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefAdapter#grantPriv(edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.grouper.attr.AttributeDef, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.privs.Privilege, String)
   */
  public void grantPriv(
      GrouperSession s, final AttributeDef attributeDef, final Subject subj, final Privilege priv, final String uuid)
      throws GrantPrivilegeException, InsufficientPrivilegeException, SchemaException {
    try {
      GrouperSession.callbackGrouperSession(s, new GrouperSessionHandler() {

        public Object callback(GrouperSession grouperSession)
            throws GrouperSessionException {
          try {
            GrouperSession.validate(grouperSession);
            Field f = priv.getField();
            if (!FieldType.ATTRIBUTE_DEF.equals(f.getType())) {
              throw new SchemaException(E.FIELD_INVALID_TYPE + f.getType());
            }
            if ( !attributeDef.getPrivilegeDelegate().canAttrAdmin( grouperSession.getSubject() ) ) {
              throw new InsufficientPrivilegeException("Subject " 
                  + GrouperUtil.subjectToString(grouperSession.getSubject()) 
                  + " cannot admin attributeDef: " + attributeDef.getName());
            }
            Membership.internal_addImmediateMembership(grouperSession, attributeDef, subj, f, uuid);
          } catch (MemberAddException eMA) {
            if (eMA instanceof MemberAddAlreadyExistsException) {
              throw new GrouperSessionException(new GrantPrivilegeAlreadyExistsException(
                  eMA.getMessage(), eMA));
            }
            throw new GrouperSessionException(new GrantPrivilegeException(eMA
                .getMessage(), eMA));
          } catch (SchemaException se) {
            throw new GrouperSessionException(se);
          }
          return null;
          }

      });
    } catch (GrouperSessionException gse) {
      if (gse.getCause() instanceof GrantPrivilegeException) {
        throw (GrantPrivilegeException) gse.getCause();
      }
      if (gse.getCause() instanceof InsufficientPrivilegeException) {
        throw (InsufficientPrivilegeException) gse.getCause();
      }
      if (gse.getCause() instanceof SchemaException) {
        throw (SchemaException) gse.getCause();
      }
      throw gse;
    }
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefAdapter#hasPriv(edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.grouper.attr.AttributeDef, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public boolean hasPriv(GrouperSession grouperSession, AttributeDef attributeDef, Subject subj, Privilege priv)
      throws SchemaException {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(grouperSession);
    boolean rv = false;
    if ( !attributeDef.getPrivilegeDelegate().hasAttrRead( grouperSession.getSubject() ) ) {
      throw new InsufficientPrivilegeException("Subject " 
          + GrouperUtil.subjectToString(grouperSession.getSubject()) 
          + " cannot admin attributeDef: " + attributeDef.getName());
    }
    Member m = MemberFinder.findBySubject(grouperSession, subj, true);
    rv = m.isMember(attributeDef.getId(), priv.getField());
    return rv;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefAdapter#privilegeCopy(edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.grouper.attr.AttributeDef, edu.internet2.middleware.grouper.attr.AttributeDef, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public void privilegeCopy(GrouperSession s, AttributeDef attributeDef1, AttributeDef attributeDef2, Privilege priv)
      throws InsufficientPrivilegeException, GrantPrivilegeException, SchemaException {
    GrouperSession.validate(s);

    Field f = priv.getField();
    PrivilegeHelper.dispatch(s, attributeDef1, s.getSubject(), f.getReadPriv());

    Iterator<Membership> membershipsIter = GrouperDAOFactory.getFactory().getMembership()
        .findAllByGroupOwnerAndFieldAndType(attributeDef1.getUuid(), f,
        MembershipType.IMMEDIATE.getTypeString(), false).iterator();

    while (membershipsIter.hasNext()) {
      Membership existingMembership = membershipsIter.next();
      Membership copiedMembership = existingMembership.clone();
      copiedMembership.setOwnerGroupId(attributeDef2.getUuid());
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
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefAdapter#privilegeCopy(edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public void privilegeCopy(GrouperSession s, Subject subj1, Subject subj2, Privilege priv)
      throws InsufficientPrivilegeException, GrantPrivilegeException, SchemaException {
    GrouperSession.validate(s);

    Field f = priv.getField();

    Set<Membership> memberships = GrouperDAOFactory.getFactory().getMembership()
        .findAllImmediateByMemberAndField(
        MemberFinder.findBySubject(s, subj1, true).getUuid(), f, false);

    if (memberships.size() == 0) {
      return;
    }

    Member member = MemberFinder.findBySubject(s, subj2, true);

    Iterator<Membership> membershipsIter = memberships.iterator();
    while (membershipsIter.hasNext()) {
      Membership existingMembership = membershipsIter.next();
      Group g;
      try {
        g = existingMembership.getGroup();
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

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefAdapter#revokePriv(edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.grouper.attr.AttributeDef, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public void revokePriv(GrouperSession grouperSession, AttributeDef attributeDef, Privilege priv)
      throws InsufficientPrivilegeException,
      RevokePrivilegeException,
      SchemaException {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(grouperSession);
    Field f = priv.getField();
    if (!FieldType.ATTRIBUTE_DEF.equals(f.getType())) {
      throw new SchemaException(E.FIELD_INVALID_TYPE + f.getType());
    }
    if ( !attributeDef.getPrivilegeDelegate().canAttrAdmin( grouperSession.getSubject() ) ) {
      throw new InsufficientPrivilegeException("Subject " 
          + GrouperUtil.subjectToString(grouperSession.getSubject()) 
          + " cannot admin attributeDef: " + attributeDef.getName());
    }
    try {
      Membership.internal_deleteAllField(grouperSession, attributeDef, f);
    } catch (MemberDeleteException eMD) {
      throw new RevokePrivilegeException(eMD.getMessage(), eMD);
    }
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefAdapter#revokePriv(edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.grouper.attr.AttributeDef, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.privs.Privilege)
   */
  public void revokePriv(
      GrouperSession grouperSession, AttributeDef attributeDef, Subject subj, Privilege priv)
      throws InsufficientPrivilegeException,
      RevokePrivilegeException,
      SchemaException {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(grouperSession);
    Field f = priv.getField();
    if (!FieldType.ATTRIBUTE_DEF.equals(f.getType())) {
      throw new SchemaException(E.FIELD_INVALID_TYPE + f.getType());
    }
    if ( !attributeDef.getPrivilegeDelegate().canAttrAdmin( grouperSession.getSubject() ) ) {
      throw new InsufficientPrivilegeException("Subject " 
          + GrouperUtil.subjectToString(grouperSession.getSubject()) 
          + " cannot admin attributeDef: " + attributeDef.getName());
    }
    try {
      Membership.internal_delImmediateMembership(grouperSession, attributeDef, subj, f);
    } catch (MemberDeleteAlreadyDeletedException eMD) {
      throw new RevokePrivilegeAlreadyRevokedException(eMD.getMessage(), eMD);
    } catch (MemberDeleteException eMD) {
      throw new RevokePrivilegeException(eMD.getMessage(), eMD);
    }
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.privs.AttributeDefAdapter#revokeAllPrivilegesForSubject(edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.subject.Subject)
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
        .findAllImmediateByMemberAndFieldType(member.getUuid(), FieldType.ATTRIBUTE_DEF.getType(), false);
    Iterator<Membership> iter = memberships.iterator();
    while (iter.hasNext()) {
      Membership mship = iter.next();
      mship.delete();
    }
  }

  /**
   * Get all attributedefs where this subject doesnt have this privilege.
   * @param grouperSession 
   * @param stemId 
   * @param scope 
   * @param subject 
   * @param privilege 
   * @param considerAllSubject
   * @param sqlLikeString
   * @return attributedefs
   */
  public Set<AttributeDef> getAttributeDefsWhereSubjectDoesntHavePrivilege(
      GrouperSession grouperSession, String stemId, Scope scope, Subject subject,
      Privilege privilege, boolean considerAllSubject, 
      String sqlLikeString) {

    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(grouperSession);
    Set<AttributeDef> attributeDefs = new LinkedHashSet();

    // This subject
    attributeDefs.addAll( 
      GrouperPrivilegeAdapter.internal_getAttributeDefsWhereSubjectDoesntHavePriv( grouperSession, 
          stemId, scope, subject, privilege, considerAllSubject, sqlLikeString) 
    );
    return attributeDefs;
  
  }

  /**
   * @see edu.internet2.middleware.grouper.privs.AttributeDefAdapter#retrievePrivileges(edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.grouper.attr.AttributeDef, java.util.Set, edu.internet2.middleware.grouper.membership.MembershipType, edu.internet2.middleware.grouper.internal.dao.QueryPaging, Set)
   */
  public Set<PrivilegeSubjectContainer> retrievePrivileges(GrouperSession grouperSession,
      AttributeDef attributeDef, Set<Privilege> privileges,
      MembershipType membershipType, QueryPaging queryPaging, Set<Member> additionalMembers) {
    
    Map<String, Object> debugLog = null;
    if (LOG.isDebugEnabled()) {
       debugLog = new LinkedHashMap<String, Object>();
       debugLog.put("attributeDefId", attributeDef.getId());
       debugLog.put("attributeDefName", attributeDef.getName());
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
      if (!grouperSession.getMember().hasAttrAdmin(attributeDef)) {
        throw new InsufficientPrivilegeException("Subject: " 
            + GrouperUtil.subjectToString(grouperSession.getSubject()) 
            + " does not have attrAdmin on attributeDef: " + attributeDef.getName());
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
        
        //lets get the members
        List<Member> members = GrouperDAOFactory.getFactory().getMembership().findAllMembersByAttributeDefOwnerOptions(
            attributeDef.getId(), membershipType, fields, null, true, queryOptions);
        
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
          memberships = GrouperDAOFactory.getFactory().getMembership().findAllByAttributeDefOwnerOptions(
              attributeDef.getId(), memberIds, membershipType, fields, null, true, queryOptions);
          
          if (LOG.isDebugEnabled()) {
            debugLog.put("membershipsSize", GrouperUtil.length(memberships));
          }
        }
        
      } else {
        
        //note, still sort by subject
        memberships = GrouperDAOFactory.getFactory().getMembership().findAllByAttributeDefOwnerOptions(
            attributeDef.getId(), membershipType, fields, null, true, queryOptions);
        
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
          Set<Object[]> additionalMemberships = GrouperDAOFactory.getFactory().getMembership().findAllByAttributeDefOwnerOptions(
              attributeDef.getId(), memberIds, membershipType, fields, null, true, null);
  
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
        
        //this multikey is sourceid, subjectid, attributedefid, fieldid, 
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
          
          MultiKey privilegeAssignTypeKey = new MultiKey(member.getSubjectSourceId(), member.getSubjectId(), attributeDef.getId(), membership.getFieldId());
          
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
              Privilege privilege = AttributeDefPrivilege.listToPriv(field.getName());
              if (privilege == null) {
                throw new RuntimeException("Privilege not found by list name! " + field.getName());
              }
              String privilegeName = privilege.getName();
              
              //multiple memberships could have the same result, just skip is already set
              if (privilegeSubjectContainerImpl.getPrivilegeContainers().get(privilegeName) == null) {
                PrivilegeContainerImpl privilegeContainerImpl = new PrivilegeContainerImpl();
                privilegeContainerImpl.setPrivilege(privilege);
                
                //if the subject, field, attributeDefId match, then correlate the assign type...
                
                MultiKey privilegeAssignTypeKey = new MultiKey(member.getSubjectSourceId(), member.getSubjectId(), attributeDef.getId(), membership.getFieldId());
    
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


}  

