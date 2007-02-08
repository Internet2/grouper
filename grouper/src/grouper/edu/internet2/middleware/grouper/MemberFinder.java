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
 * @version $Id: MemberFinder.java,v 1.31 2007-02-08 16:25:25 blair Exp $
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
    GrouperSessionValidator.internal_validate(s);
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
    GrouperSessionValidator.internal_validate(s);
    MemberDTO dto = HibernateMemberDAO.findByUuid(uuid);
    Member    m   = new Member();
    m.setDTO(dto);
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
    m.setDTO( internal_findBySubject( subj.getId(), subj.getSource().getId(), subj.getType().getName() ) );
    return m;
  } // protected static Member internal_findBySubject(subj)

  // @since   1.2.0
  // TODO 20070123  this should really be renamed to reflect that it will create the member
  //                if it cannot be found
  protected static MemberDTO internal_findBySubject(String id, String src, String type) 
    throws  MemberNotFoundException
  {
    // @session false
    MemberDTO dto = null;
    try {
      dto = HibernateMemberDAO.findBySubject(id, src, type);
    }
    catch (MemberNotFoundException eMNF) {
      dto = new MemberDTO();
      dto.setMemberUuid( GrouperUuid.internal_getUuid() );
      dto.setSubjectId(id);
      dto.setSubjectSourceId(src);
      dto.setSubjectTypeId(type);
      dto.setId( HibernateMemberDAO.create(dto) ); // create new member
    }
    return dto; // return existing member
  } // protected static Member internal_findBySubject(id, src, type)

} // public class MemberFinder

