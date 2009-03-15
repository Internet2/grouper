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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
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
 * @version $Id: MemberFinder.java,v 1.60 2009-03-15 06:37:21 mchyzer Exp $
 */
public class MemberFinder {
	
	//Added by Gary Brown 2007-11-02
	//GouperAll / GrouperSystem ought not to change...
	private static Member all;
	private static Member root;

  // PUBLIC CLASS METHODS //
	
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
   * Find all members by source.
   * <pre class="eg">
   * Set members = MemberFinder.findAll(s, source);
   * </pre>
   * @param   s       Find all members within this session context.
   * @param   source  Find all members with this source.
   * @return  {@link Set} of {@link Member} objects.
   * @throws  GrouperException
   */
  public static Set findAll(GrouperSession s, Source source)
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
    Member m = internal_findBySubject(subj, createIfNotExist && !(subj instanceof UnresolvableSubject));
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

  
  public static Member internal_findAllMember() 
    throws  GrouperException
  {
	  if(all !=null) return all;
    all= MemberFinder.internal_findBySubject( SubjectFinder.findAllSubject(), true); 
    return all;
  } // public static Member internal_findAllMember()

  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(MemberFinder.class);

  // @since   1.2.0
  public static Member internal_findRootMember() 
    throws  GrouperException
  {
	if(root != null) return root;
    root= MemberFinder.internal_findBySubject( SubjectFinder.findRootSubject(), true ); 
    return root;
  }
  
  /**
   * find a member, perhaps create a new one if not there
   * @param subj
   * @param createIfNotExist 
   * @return the member
   */
  public static Member internal_findBySubject(Subject subj, boolean createIfNotExist) {
    if (subj == null) {
      throw new NullPointerException("Subject is null");
    }
    
    String sourceId = null;
    if (subj instanceof LazySubject) {
      sourceId = ((LazySubject)subj).getSourceId();
    } else {
      sourceId = subj.getSource().getId();
    }
    Member m = internal_findOrCreateBySubject( subj.getId(), sourceId, subj.getType().getName(), createIfNotExist ) ;
    
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
    return internal_findOrCreateBySubject(id, src, type, true);
  }
  
  /**
   * find a member 
   * @param id
   * @param src
   * @param type
   * @param createIfNotExist 
   * @return the member or null
   */
  private static Member internal_findOrCreateBySubject(String id, String src, String type, boolean createIfNotExist) {
    try {
      return GrouperDAOFactory.getFactory().getMember().findBySubject(id, src, type, true);
    }
    catch (MemberNotFoundException eMNF) {
      if (createIfNotExist) {
        Member _m = new Member();
        _m.setSubjectIdDb(id);
        _m.setSubjectSourceIdDb(src);
        _m.setSubjectTypeId(type);
        _m.setUuid( GrouperUuid.getUuid() );
        
        GrouperDAOFactory.getFactory().getMember().create(_m);
        return _m;
      }
      return null;
    }
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
  
  // @since   1.2.1
  public static void clearInternalMembers()
    
  {
    all=null;
    root=null;
    
  } // public static void clearInternalMembers()

} // public class MemberFinder

