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

import  edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import  edu.internet2.middleware.subject.*;

/**
 * Find members within the Groups Registry.
 * <p/>
 * @author  blair christensen.
 * @version $Id: MemberFinder.java,v 1.52 2008-06-25 05:46:05 mchyzer Exp $
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
   * // Convert a subject to a Member object
   * try {
   *   Member m = MemberFinder.findBySubject(s, subj);
   * }
   * catch (MemberNotFoundException e) {
   *   // Member not found
   * }
   * </pre>
   * @param   s   Find {@link Member} within this session context.
   * @param   subj  {@link Subject} to convert.
   * @return  A {@link Member} object.
   * @throws  MemberNotFoundException
   */
  public static Member findBySubject(GrouperSession s, Subject subj)
    throws  MemberNotFoundException
  {
    //note, no need for GrouperSession inverse of control
    GrouperSession.validate(s);
    Member m = internal_findBySubject(subj);
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

  
  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static Member internal_findAllMember() 
    throws  GrouperRuntimeException
  {
	  if(all !=null) return all;
    try {
      all= MemberFinder.internal_findBySubject( SubjectFinder.findAllSubject() ); 
      return all;
    }
    catch (MemberNotFoundException eMNF) {
      String msg = E.MEMBERF_FINDALLMEMBER + eMNF.getMessage();
      ErrorLog.fatal(MemberFinder.class, msg);
      throw new GrouperRuntimeException(msg, eMNF);
    }
  } // protected static Member internal_findAllMember()

  // @since   1.2.0
  protected static Member internal_findRootMember() 
    throws  GrouperRuntimeException
  {
	if(root != null) return root;
    try {
      root= MemberFinder.internal_findBySubject( SubjectFinder.findRootSubject() ); 
      return root;
    }
    catch (MemberNotFoundException eShouldNeverHappen) {
      String msg = "unable to fetch GrouperSystem as member: " + eShouldNeverHappen.getMessage();
      ErrorLog.fatal(MemberFinder.class, msg);
      throw new GrouperRuntimeException(msg, eShouldNeverHappen);
    }
  } 

  // @since   1.2.0
  protected static Member internal_findBySubject(Subject subj) 
    throws  MemberNotFoundException
  {
    if (subj == null) {
      throw new MemberNotFoundException();
    }
    Member m = internal_findOrCreateBySubject( subj.getId(), subj.getSource().getId(), subj.getType().getName() ) ;
    return m;
  } // protected static Member internal_findBySubject(subj)

  // @since   1.2.0
  protected static Member internal_findOrCreateBySubject(String id, String src, String type) {
    try {
      return GrouperDAOFactory.getFactory().getMember().findBySubject(id, src, type);
    }
    catch (MemberNotFoundException eMNF) {
      Member _m = new Member();
      _m.setSubjectIdDb(id);
      _m.setSubjectSourceIdDb(src);
      _m.setSubjectTypeId(type);
      _m.setUuid( GrouperUuid.getUuid() );
      
      GrouperDAOFactory.getFactory().getMember().create(_m);
      return _m;
    }
  } // protected static Member internal_findOrCreateBySubject(id, src, type)

  // @since   1.2.0
  protected static Member internal_findViewableMemberBySubject(GrouperSession s, Subject subj)
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
  } // protected static Member internal_findViewableMemberBySubject(s, subj)
  
  // @since   1.2.1
  protected static void clearInternalMembers()
    
  {
    all=null;
    root=null;
    
  } // protected static void clearInternalMembers()

} // public class MemberFinder

