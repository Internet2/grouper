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
 * @version $Id: MemberFinder.java,v 1.29 2007-01-08 16:43:56 blair Exp $
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
    Member m = findBySubject(subj);
    m.internal_setSession(s);
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
    Member m = HibernateMemberDAO.findByUuid(uuid);
    if (m == null) {
      throw new MemberNotFoundException("member not found");
    }
    m.internal_setSession(s);
    return m;
  } // public static Member findByUuid(s, uuid)

  
  // PROTECTED CLASS METHODS //

  protected static Member findAllMember() 
    throws  GrouperRuntimeException
  {
    try {
      return MemberFinder.findBySubject(SubjectFinder.findAllSubject()); 
    }
    catch (MemberNotFoundException eMNF) {
      String msg = E.MEMBERF_FINDALLMEMBER + eMNF.getMessage();
      ErrorLog.fatal(MemberFinder.class, msg);
      throw new GrouperRuntimeException(msg, eMNF);
    }
  } // protected static Member findAllMember()

  protected static Member findBySubject(Subject subj) 
    throws  MemberNotFoundException
  {
    if (subj == null) {
      throw new MemberNotFoundException();
    }
    Member m = internal_findBySubject( subj.getId(), subj.getSource().getId(), subj.getType().getName() );
    m.setSubject(subj);
    return m;
  } // protected static Member findBySubject(subj)

  // @since   1.2.0
  protected static Member internal_findBySubject(String id, String src, String type) 
    throws  MemberNotFoundException
  {
    // @session false
    Member m = HibernateMemberDAO.findBySubject(id, src, type);
    if (m == null) {
      return Member.internal_addMember(id, src, type); // Create new *Member*
    }
    return m; // Return existing *Member*
  } // protected static Member internal_findBySubject(id, src, type)

} // public class MemberFinder

