/*
  Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2006 The University Of Chicago

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
import  java.util.*;
import  net.sf.hibernate.*;

/**
 * Find members within the Groups Registry.
 * <p/>
 * @author  blair christensen.
 * @version $Id: MemberFinder.java,v 1.20 2006-08-30 14:39:22 blair Exp $
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
    Member m = findBySubject(subj);
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
    try {
      Member  m       = null;
      Session hs      = HibernateHelper.getSession();
      Query   qry     = hs.createQuery(
        "from Member as m where m.member_id = :uuid"
      );
      qry.setCacheable(true);
      qry.setCacheRegion(GrouperConfig.QCR_MF_FBU);
      qry.setString("uuid", uuid);
      List    members = qry.list();
      if (members.size() == 1) {
        m = (Member) members.get(0);
        m.setSession(s);
      }
      hs.close();
      if (m == null) {
        throw new MemberNotFoundException("matching members: " + members.size());
      }
      return m;
    }
    catch (HibernateException eMNF) {
      throw new MemberNotFoundException(
        "member not found: " + eMNF.getMessage(), eMNF
      );
    }
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
    try {
      Session hs  = HibernateHelper.getSession();
      Query   qry = hs.createQuery(
        "from Member as m where "
        + "     m.subject_id      = :sid "  
        + "and  m.subject_type    = :type "
        + "and  m.subject_source  = :source"
      );
      qry.setCacheable(true);
      qry.setCacheRegion(GrouperConfig.QCR_MF_FBS);
      qry.setString("sid",    subj.getId()            );
      qry.setString("type",   subj.getType().getName());
      qry.setString("source", subj.getSource().getId());
      Member  m   = (Member) qry.uniqueResult();
      hs.close();
      if (m != null) {
        m.setSubject(subj);
        return m; // Return existing *Member*
      }
      else {
        // Create a new member
        m = Member.addMember(subj); // Create new *Member*
        return m;  
      }
    }
    catch (HibernateException eH) {
      String msg = E.MEMBER_NEITHER_FOUND_NOR_CREATED + eH.getMessage();
      ErrorLog.error(MemberFinder.class, msg);
      throw new MemberNotFoundException(msg, eH);
    }
  } // protected static Member findBySubject(subj)

} // public class MemberFinder

