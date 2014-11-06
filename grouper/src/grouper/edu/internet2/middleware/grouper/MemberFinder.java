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
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.member.SearchStringEnum;
import edu.internet2.middleware.grouper.member.SortStringEnum;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.subj.LazySubject;
import edu.internet2.middleware.grouper.subj.UnresolvableSubject;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;

/**
 * Find members within the Groups Registry.
 * <p/>
 * @author  blair christensen.
 * @version $Id: MemberFinder.java,v 1.68 2009-12-28 06:08:37 mchyzer Exp $
 */
public class MemberFinder {
	
  /** GouperAll / GrouperSystem ought not to change... */
	private static Member all;
	
  /** GouperAll / GrouperSystem ought not to change... */
	private static Member root;

	/**
   * Find all members.
   * <pre class="eg">
   * Set members = MemberFinder.findAll(s);
   * </pre>
   * @param   s   Find all members within this session context.
   * @return  {@link Set} of {@link Member} objects.
   * @throws  GrouperException
   */
  public static Set findAll(GrouperSession s)
    throws  GrouperException
  {
    //note, no need for GrouperSession inverse of control
    return findAll(s, null);
  } // public static Set findAll(GrouperSession s)
  
  /**
   * Find all members by source used somewhere, e.g. with memberships or attributes etc.
   * <pre class="eg">
   * Set members = MemberFinder.findAllUsed(s, source);
   * </pre>
   * @param   grouperSession       Find all members within this session context.
   * @param   source  Find all members with this source.
   * @return  {@link Set} of {@link Member} objects.
   * @throws  GrouperException
   */
  public static Set<Member> findAllUsed(GrouperSession grouperSession, Source source)
    throws  GrouperException {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(grouperSession);
    Set<Member> members = GrouperDAOFactory.getFactory().getMember().findAllUsed(source);
    return members;
  }
  
  /**
   * Find all members by source.
   * <pre class="eg">
   * Set members = MemberFinder.findAll(s, source);
   * </pre>
   * @param   s       Find all members within this session context.
   * @param   source  Find all members with this source.
   * @return  {@link Set} of {@link Member} objects.
   * @throws  GrouperException
   */
  public static Set<Member> findAll(GrouperSession s, Source source)
    throws  GrouperException
  {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(s);
    Set members = new LinkedHashSet();
    Iterator it = GrouperDAOFactory.getFactory().getMember().findAll(source).iterator();
    while (it.hasNext()) {
      Member m = (Member)it.next();
      members.add(m);
    }
    return members;
  } // public static Set findAll(GrouperSession s)
  /**
   * make sure allowed to see them, and find the members
   * @param grouperSession
   * @param group
   * @param field
   * @param type
   * @param sources
   * @param queryOptions 
   * @param memberSortStringEnum 
   * @param memberSearchStringEnum 
   * @param memberSearchStringValue 
   * @return the members, dont return null
   */
  public static Set<Member> internal_findMembersByType(GrouperSession grouperSession, Group group, Field field, String type,
      Set<Source> sources, QueryOptions queryOptions, SortStringEnum memberSortStringEnum, SearchStringEnum memberSearchStringEnum, 
      String memberSearchStringValue) {
    GrouperSession.validate(grouperSession);

    if (!PrivilegeHelper.canViewMembers(grouperSession, group, field)) {
      return new LinkedHashSet();
    }
    Set<Member> members = GrouperDAOFactory.getFactory().getMembership()
      .findAllMembersByOwnerAndFieldAndType(group.getUuid(), field, type, sources, queryOptions, true,
          memberSortStringEnum, memberSearchStringEnum, memberSearchStringValue);
    return members;
  }

  /**
   * Convert a {@link Subject} to a {@link Member}.  Create if not exist
   * <pre class="eg">
   * // Convert a subject to a Member object, create if not exist
   *   Member m = MemberFinder.findBySubject(s, subj);
   * </pre>
   * @param   s   Find {@link Member} within this session context.
   * @param   subj  {@link Subject} to convert.
   * @return  A {@link Member} object.
   * @deprecated use overload
   */
  @Deprecated
  public static Member findBySubject(GrouperSession s, Subject subj) {
    return findBySubject(s, subj, true);
  }
  
  /**
   * convert a set of subjects to a set of members
   * @param grouperSession 
   * @param subjects to convert to members
   * @param group that subjects must be in
   * @param field that they must be in in the group (null will default to eh members list
   * @param membershipType that they must be in in the group or null for any
   * @return the members in the group
   */
  public static Set<Member> findBySubjectsInGroup(GrouperSession grouperSession,
      Set<Subject> subjects, Group group, Field field, MembershipType membershipType) {

    return GrouperDAOFactory.getFactory().getMember().findBySubjectsInGroup(
        grouperSession, subjects, group, field, membershipType);

  }

  /**
   * convert a set of subjects to a set of members
   * @param subjects to convert to members
   * @param createIfNotExists 
   * @param group that subjects must be in
   * @param field that they must be in in the group (null will default to eh members list
   * @param membershipType that they must be in in the group or null for any
   * @return the members in the group
   */
  public static Set<Member> findBySubjects(
      Collection<Subject> subjects, boolean createIfNotExists) {

    return GrouperDAOFactory.getFactory().getMember().findBySubjects(
        subjects, createIfNotExists);

  }

  /**
   * Convert a {@link Subject} to a {@link Member}.
   * <pre class="eg">
   * // Convert a subject to a Member object, create if not exist
   *   Member m = MemberFinder.findBySubject(s, subj, true);
   * </pre>
   * @param   s   Find {@link Member} within this session context.
   * @param   subj  {@link Subject} to convert.
   * @param createIfNotExist true if the member should be created if not there
   * @return  A {@link Member} object.
   */
  public static Member findBySubject(GrouperSession s, Subject subj, boolean createIfNotExist) {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(s);
    Member m = internal_findBySubject(subj, null, createIfNotExist && !(subj instanceof UnresolvableSubject));
    return m;
  }

  /**
   * Get a member by UUID.
   * <pre class="eg">
   * // Get a member by uuid.
   * try {
   *   Member m = MemberFind.findByUuid(s, uuid);
   * }
   * catch (MemberNotFoundException e) {
   *   // Member not found
   * }
   * </pre>
   * @param   s     Get {@link Member} within this session context.
   * @param   uuid  Get {@link Member} with this UUID.
   * @return  A {@link Member} object.
   * @throws  MemberNotFoundException
   * @Deprecated use overload instread
   */
  @Deprecated
  public static Member findByUuid(GrouperSession s, String uuid)
    throws MemberNotFoundException {

    return findByUuid(s, uuid, true);
  }

  /**
   * Get a member by UUID.
   * <pre class="eg">
   * // Get a member by uuid.
   * Member m = MemberFind.findByUuid(s, uuid, false);
   * </pre>
   * @param   s     Get {@link Member} within this session context.
   * @param   uuid  Get {@link Member} with this UUID.
   * @param exceptionIfNotFound true to throw exception if not found
   * @return  A {@link Member} object.
   * @throws  MemberNotFoundException
   */
  public static Member findByUuid(GrouperSession s, String uuid, 
      boolean exceptionIfNotFound) throws MemberNotFoundException {
    GrouperSession.validate(s);
    //note, no need for GrouperSession inverse of control
    Member m = GrouperDAOFactory.getFactory().getMember().findByUuid(uuid, exceptionIfNotFound);
    return m;
  }

  /**
   * 
   * @return member
   * @throws GrouperException
   */
  public static Member internal_findAllMember() 
    throws  GrouperException {
	  if(all !=null) return all;
    all= MemberFinder.internal_findBySubject( SubjectFinder.findAllSubject(), null, true); 
    return all;
  } // public static Member internal_findAllMember()

  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(MemberFinder.class);

  /**
   * 
   * @return member
   * @throws GrouperException
   */
  public static Member internal_findRootMember() 
    throws  GrouperException {
	if(root != null) return root;
    root= MemberFinder.internal_findBySubject( SubjectFinder.findRootSubject(), null, true ); 
    return root;
  }
  
  /**
   * find a member, perhaps create a new one if not there
   * @param subj
   * @param uuidIfCreate uuid to use if creating
   * @param createIfNotExist 
   * @return the member or null if not found
   */
  public static Member internal_findBySubject(Subject subj, String uuidIfCreate, boolean createIfNotExist) {
    if (subj == null) {
      throw new NullPointerException("Subject is null");
    }
    
    Member m = internal_findOrCreateBySubject(subj, uuidIfCreate, createIfNotExist);
    
    //Member m = internal_findOrCreateBySubject( subj.getId(), subj.getSource().getId(), subj.getType().getName() ) ;
    return m;
  } // public static Member internal_findBySubject(subj)

  /**
   * find a member 
   * @param id
   * @param src
   * @param type
   * @return the member 
   */
  public static Member internal_findOrCreateBySubject(String id, String src, String type) {
    return internal_findOrCreateBySubject(id, src, type, null, true);
  }

  /**
   * keep track of when member is created
   */
  private static GrouperCache<MultiKey, Boolean> memberCreatedCache = null;
  
  /**
   * member created cache
   * @return the cache
   */
  private static GrouperCache<MultiKey, Boolean> memberCreatedCache() {
    if (memberCreatedCache == null) {
      synchronized (MemberFinder.class) {
        if (memberCreatedCache == null) {
          memberCreatedCache = new GrouperCache<MultiKey, Boolean>(MemberFinder.class.getName() + ".memberCreatedCache");
        }
      }
    }
    return memberCreatedCache;
  }
  
  
  /**
   * keep a map of multikeys to synchronize on
   */
  private static GrouperCache<MultiKey, MultiKey> memberLocksCache = null;
  
  /**
   * member locks cache
   * @return the cache
   */
  private static GrouperCache<MultiKey, MultiKey> memberLocksCache() {
    if (memberLocksCache == null) {
      synchronized (MemberFinder.class) {
        if (memberLocksCache == null) {
          memberLocksCache = new GrouperCache<MultiKey, MultiKey>(MemberFinder.class.getName() + ".memberLocksCache");
        }
      }
    }
    return memberLocksCache;
  }
  /**
   * 
   * @param sourceId
   * @param subjectId
   */
  public static void memberCreatedCacheDelete(String sourceId, String subjectId) {
    memberCreatedCache().remove(new MultiKey(sourceId, subjectId));
  }

  /**
   * find a member 
   * @param subj 
   * @param memberUuidIfCreate 
   * @param createIfNotExist 
   * @return the member or null
   */
  private static Member internal_findOrCreateBySubject(final Subject subj, String memberUuidIfCreate, boolean createIfNotExist) {
    
    String sourceId = null;
    if (subj instanceof LazySubject) {
      sourceId = ((LazySubject)subj).getSourceId();
    } else {
      sourceId = subj.getSource().getId();
    }
    
    try {
      return GrouperDAOFactory.getFactory().getMember().findBySubject(subj.getId(), sourceId, subj.getType().getName(), true);
    }
    catch (MemberNotFoundException eMNF) {
      
      if (createIfNotExist) {
        
        
        MultiKey multiKey = new MultiKey(sourceId, subj.getId());
        
        synchronized (MemberFinder.class) {
          //get the same key for name
          if (!memberLocksCache().containsKey(multiKey)) {
            memberLocksCache().put(multiKey, multiKey);
          }
          multiKey = memberLocksCache().get(multiKey);
        }
        
        Member member = null;
        

        final String SOURCE_ID = sourceId;
        
        synchronized(multiKey) {
          
          //something created this recently
          if (memberCreatedCache().get(multiKey) != null) {

            //wait for the transaction to finish...
            for (int i=0;i<45;i++) {
              member = (Member)GrouperTransaction.callbackGrouperTransaction(GrouperTransactionType.NONE, new GrouperTransactionHandler() {
                
                public Object callback(GrouperTransaction grouperTransaction)
                    throws GrouperDAOException {
                  return GrouperDAOFactory.getFactory().getMember().findBySubject(subj.getId(), SOURCE_ID, subj.getType().getName(), false);
                }
              });
              
              if (member != null) {
                break;
              }

              //TODO remove
              System.out.println("Checking: " + multiKey + ", thread: " + Integer.toHexString(Thread.currentThread().hashCode()));

              GrouperUtil.sleep(1000);
            }
            
          }

        
          
          if (member == null) {
            //race conditions
            memberCreatedCache().put(multiKey, Boolean.TRUE);
  
            if (!StringUtils.isBlank(memberUuidIfCreate)) {
              member = GrouperDAOFactory.getFactory().getMember().findByUuid(memberUuidIfCreate, false);
              if (member != null) {
                throw new RuntimeException("That uuid already exists: " + memberUuidIfCreate + ", " + member);
              }
            }
            try {
              Member _m = internal_createMember(subj, memberUuidIfCreate);
              return _m;
            } catch (RuntimeException re) {
              
              //dont interrupt a hook veto
              if (re instanceof HookVeto) {
                throw re;
              }
              
              //give an out switch
              if (GrouperConfig.retrieveConfig().propertyValueBoolean("grouperDontTryCreateMemberTwiceOnException", false)) {
                throw re;
              }
              
              //GRP-903: if the same member is created on two jvms, it throws a constrain exception
              //maybe a constraint was violated?  Try to select again?
              try {
                //lets wait a little bit if transactions are finishing or whatever
                GrouperUtil.sleep(500);
                return GrouperDAOFactory.getFactory().getMember().findBySubject(subj.getId(), sourceId, 
                    subj.getType().getName(), true, new QueryOptions().secondLevelCache(false));
              }
              catch (MemberNotFoundException eMNF2) {
                //put this exception in the other one...
                GrouperUtil.injectInException(re, "... second memberNotFoundException for subject " + GrouperUtil.subjectToString(subj) 
                    + "... " + ExceptionUtils.getFullStackTrace(eMNF2) + "...");
                //ignore this exception...  throw the original create exception
                throw re;
              }
            }
          }
        }
        
        return member;
      }
      return null;
    }
  }

  /**
   * create a member
   * @param subj
   * @param memberUuidIfCreate
   * @return the member object
   */
  public static Member internal_createMember(Subject subj, String memberUuidIfCreate) {
    Member _m = new Member();
    _m.setSubjectIdDb(subj.getId());
    _m.setSubjectSourceIdDb(subj.getSourceId());
    _m.setSubjectTypeId(subj.getType().getName());
    _m.setUuid( StringUtils.isEmpty(memberUuidIfCreate) ? GrouperUuid.getUuid() : memberUuidIfCreate);
    _m.updateMemberAttributes(subj, false);
    
    GrouperDAOFactory.getFactory().getMember().create(_m);
    return _m;
  }
  
  /**
   * find a member 
   * @param id
   * @param src
   * @param type
   * @param memberUuidIfCreate 
   * @param createIfNotExist 
   * @return the member or null
   */
  private static Member internal_findOrCreateBySubject(String id, String src, String type, 
      String memberUuidIfCreate, boolean createIfNotExist) {

    Subject subj = SubjectFinder.findByIdAndSource(id, src, true);
    return internal_findOrCreateBySubject(subj, memberUuidIfCreate, createIfNotExist);
  } 

  /**
   * @param s
   * @param subj
   * @param exceptionIfNotExist
   * @return the member or null if exceptionIfNotExist is false
   * @throws InsufficientPrivilegeException
   * @throws MemberNotFoundException
   */
  public static Member internal_findViewableMemberBySubject(GrouperSession s, Subject subj, boolean exceptionIfNotExist)
    throws  InsufficientPrivilegeException,
            MemberNotFoundException  {
    //note, no need for GrouperSession inverse of control
    Member m = findBySubject(s, subj, exceptionIfNotExist);
    if ( SubjectFinder.internal_getGSA().getId().equals( m.getSubjectSourceId() )) {
      // subject is a group.  is it VIEWable?
      try {
        GroupFinder.findByUuid( s, m.getSubjectId(), true ); // TODO 20070328 this is rather heavy
      }
      catch (GroupNotFoundException eGNF) {
        if (exceptionIfNotExist) {
          throw new MemberNotFoundException( eGNF.getMessage(), eGNF );  
        }
        return null;
      }
    }
    return m;
  } // public static Member internal_findViewableMemberBySubject(s, subj)
  
  /**
   * find a member object and if group, make sure it is readable
   * @param grouperSession
   * @param subject
   * @param exceptionIfNotExist
   * @return the member
   * @throws MemberNotFoundException 
   * @throws InsufficientPrivilegeException 
   */
  public static Member internal_findReadableMemberBySubject(GrouperSession grouperSession, 
      Subject subject, boolean exceptionIfNotExist)
     throws MemberNotFoundException, InsufficientPrivilegeException {
    Member member = findBySubject(grouperSession, subject, exceptionIfNotExist);
    
    if (!exceptionIfNotExist && member == null) {
      return null;
    }
    
    //see if this subject is a group
    if ( SubjectFinder.internal_getGSA().getId().equals( member.getSubjectSourceId() )) {
      Group group = null;
      
      try {
        group = GrouperDAOFactory.getFactory().getGroup().findByUuid(member.getSubjectId(), true);
      } catch (GroupNotFoundException gnfe) {
        if (exceptionIfNotExist) {
          throw new MemberNotFoundException("Cant find (or possibly view) group: " + member.getSubjectId(), gnfe);
        }
        return null;
      }
      
      //see if the session can read the group
      if ( PrivilegeHelper.canRead( grouperSession.internal_getRootSession(), group, grouperSession.getSubject() ) ) {
        return member;
      }
      throw new InsufficientPrivilegeException("Subject: " 
          + grouperSession.getSubject().getId() + " does not have READ privilege on group "
          + group.getName());
    }
    return member;
  }
  
  /**
   * 
   */
  public static void clearInternalMembers() {
    all=null;
    root=null;
  } // public static void clearInternalMembers()

} // public class MemberFinder

