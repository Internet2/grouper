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
import edu.internet2.middleware.grouper.exception.GrouperRuntimeException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.subj.LazySubject;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;

/**
 * Find members within the Groups Registry.
 * <p/>
 * @author  blair christensen.
 * @version $Id: MemberFinder.java,v 1.57 2008-11-11 22:08:33 mchyzer Exp $
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
   * @throws  GrouperRuntimeException
   */
  public static Set findAll(GrouperSession s)
    throws  GrouperRuntimeException
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
   * @throws  GrouperRuntimeException
   */
  public static Set findAll(GrouperSession s, Source source)
    throws  GrouperRuntimeException
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
   * Convert a {@link Subject} to a {@link Member}.
   * <pre class="eg">
   * // Convert a subject to a Member object, create if not exist
   *   Member m = MemberFinder.findBySubject(s, subj);
   * </pre>
   * @param   s   Find {@link Member} within this session context.
   * @param   subj  {@link Subject} to convert.
   * @return  A {@link Member} object.
   */
  public static Member findBySubject(GrouperSession s, Subject subj) {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(s);
    Member m = internal_findBySubject(subj, true);
    return m;
  } // public static Member findBySubject(s, subj)

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
   */
  public static Member findByUuid(GrouperSession s, String uuid)
    throws MemberNotFoundException
  {
    GrouperSession.validate(s);
    //note, no need for GrouperSession inverse of control
    Member m = GrouperDAOFactory.getFactory().getMember().findByUuid(uuid);
    return m;
  } // public static Member findByUuid(s, uuid)

  
  // public CLASS METHODS //

  // @since   1.2.0
  public static Member internal_findAllMember() 
    throws  GrouperRuntimeException
  {
	  if(all !=null) return all;
    all= MemberFinder.internal_findBySubject( SubjectFinder.findAllSubject(), true); 
    return all;
  } // public static Member internal_findAllMember()

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(MemberFinder.class);

  // @since   1.2.0
  public static Member internal_findRootMember() 
    throws  GrouperRuntimeException
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
      return GrouperDAOFactory.getFactory().getMember().findBySubject(id, src, type);
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
  } // public static Member internal_findOrCreateBySubject(id, src, type)

  // @since   1.2.0
  public static Member internal_findViewableMemberBySubject(GrouperSession s, Subject subj)
    throws  InsufficientPrivilegeException,
            MemberNotFoundException
  {
    //note, no need for GrouperSession inverse of control
    Member m = findBySubject(s, subj);
    if ( SubjectFinder.internal_getGSA().getId().equals( m.getSubjectSourceId() )) {
      // subject is a group.  is it VIEWable?
      try {
        GroupFinder.findByUuid( s, m.getSubjectId() ); // TODO 20070328 this is rather heavy
      }
      catch (GroupNotFoundException eGNF) {
        throw new MemberNotFoundException( eGNF.getMessage(), eGNF );  
      }
    }
    return m;
  } // public static Member internal_findViewableMemberBySubject(s, subj)
  
  // @since   1.2.1
  public static void clearInternalMembers()
    
  {
    all=null;
    root=null;
    
  } // public static void clearInternalMembers()

} // public class MemberFinder

