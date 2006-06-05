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
import  java.io.Serializable;
import  java.util.*;
import  net.sf.hibernate.*;
import  net.sf.hibernate.type.*;
import  org.apache.commons.logging.*;


/**
 * Find members within the Groups Registry.
 * <p />
 * @author  blair christensen.
 * @version $Id: MemberFinder.java,v 1.12 2006-06-05 19:54:40 blair Exp $
 */
public class MemberFinder implements Serializable {

  // Private Class Constants
  private static final String ERR_FAM = "unable to find ALL subject as member: ";
  private static final Log    LOG     = LogFactory.getLog(MemberFinder.class);


  // Public Class Methods

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
      qry.setCacheable(GrouperConfig.QRY_MF_FBU);
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

  
  // Protected Class Methods

  protected static Member findAllMember() {
    try {
      return MemberFinder.findBySubject(SubjectFinder.findAllSubject()); 
    }
    catch (MemberNotFoundException eMNF) {
      String err = ERR_FAM + eMNF.getMessage();
      LOG.fatal(err);
      throw new RuntimeException(err, eMNF);
    }
  } // protected static Member findAllMember()

  protected static Member findBySubject(Subject subj) 
    throws  MemberNotFoundException
  {
    String msg = "findBySubject";
    LOG.debug(msg);
    if (subj == null) {
      String err = msg + " null subject";
      LOG.debug(err);
      throw new MemberNotFoundException(err);
    }
    try {
      Member  m       = null;
      Session hs      = HibernateHelper.getSession();
      Query   qry     = hs.createQuery(
        "from Member as m where "
        + "     m.subject_id      = :sid "  
        + "and  m.subject_type    = :type "
        + "and  m.subject_source  = :source"
      );
      qry.setCacheable(GrouperConfig.QRY_MF_FBS);
      qry.setCacheRegion(GrouperConfig.QCR_MF_FBS);
      qry.setString("sid",    subj.getId()            );
      qry.setString("type",   subj.getType().getName());
      qry.setString("source", subj.getSource().getId());
      List    members = qry.list();
      LOG.debug(msg + " found: " + members.size());
      if (members.size() == 1) {
        // The member already exists
        m = (Member) members.get(0);
      }
      hs.close();
      if (m != null) {
        m.setSubject(subj);
        LOG.debug(msg + " found existing member: " + m);
        return m;
      }
      else {
        // Create a new member
        m = Member.addMember(subj);
        LOG.debug(msg + " created new member: " + m);
        return m;
      }
    }
    catch (HibernateException eH) {
      String err = msg + " member not found nor created: " + eH.getMessage();
      LOG.error(err);
      throw new MemberNotFoundException(err, eH);
    }
  } // protected static Member findBySubject(subj)
}

