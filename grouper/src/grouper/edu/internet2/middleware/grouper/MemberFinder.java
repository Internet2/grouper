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
import  edu.internet2.middleware.subject.*;

/**
 * Find members within the Groups Registry.
 * <p/>
 * @author  blair christensen.
 * @version $Id: MemberFinder.java,v 1.40 2007-04-05 14:28:28 blair Exp $
 */
public class MemberFinder {

  // PUBLIC CLASS METHODS //

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
    GrouperSession.validate(s);
    Member m = internal_findBySubject(subj);
    m.setSession(s);
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
    Member m = new Member();
    m.setDTO( GrouperDAOFactory.getFactory().getMember().findByUuid(uuid) );
    m.setSession(s);
    return m;
  } // public static Member findByUuid(s, uuid)

  
  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static Member internal_findAllMember() 
    throws  GrouperRuntimeException
  {
    try {
      return MemberFinder.internal_findBySubject( SubjectFinder.findAllSubject() ); 
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
    try {
      return MemberFinder.internal_findBySubject( SubjectFinder.findRootSubject() ); 
    }
    catch (MemberNotFoundException eShouldNeverHappen) {
      String msg = "unable to fetch GrouperSystem as member: " + eShouldNeverHappen.getMessage();
      ErrorLog.fatal(MemberFinder.class, msg);
      throw new GrouperRuntimeException(msg, eShouldNeverHappen);
    }
  } // protected static Member internal_findRootMember()

  // @since   1.2.0
  protected static Member internal_findBySubject(Subject subj) 
    throws  MemberNotFoundException
  {
    if (subj == null) {
      throw new MemberNotFoundException();
    }
    Member m = new Member();
    m.setDTO( internal_findOrCreateBySubject( subj.getId(), subj.getSource().getId(), subj.getType().getName() ) );
    return m;
  } // protected static Member internal_findBySubject(subj)

  // @since   1.2.0
  protected static MemberDTO internal_findOrCreateBySubject(String id, String src, String type) {
    try {
      return GrouperDAOFactory.getFactory().getMember().findBySubject(id, src, type);
    }
    catch (MemberNotFoundException eMNF) {
      MemberDTO _m = new MemberDTO()
        .setSubjectId(id)
        .setSubjectSourceId(src)
        .setSubjectTypeId(type)
        .setUuid( GrouperUuid.internal_getUuid() )
        ;
      return _m.setId( GrouperDAOFactory.getFactory().getMember().create(_m) );
    }
  } // protected static MemberDTO internal_findOrCreateBySubject(id, src, type)

  // @since   1.2.0
  protected static Member internal_findViewableMemberBySubject(GrouperSession s, Subject subj)
    throws  InsufficientPrivilegeException,
            MemberNotFoundException
  {
    Member m = findBySubject(s, subj);
    if ( SubjectFinder.internal_getGSA().getId().equals( m.getDTO().getSubjectSourceId() ) ) {
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

} // public class MemberFinder

