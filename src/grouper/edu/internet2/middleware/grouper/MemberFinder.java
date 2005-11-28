/*
  Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2005 The University Of Chicago

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

/**
 * Find members within the Groups Registry.
 * <p />
 * @author  blair christensen.
 * @version $Id: MemberFinder.java,v 1.3 2005-11-28 18:13:18 blair Exp $
 */
public class MemberFinder implements Serializable {

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
    throws MemberNotFoundException
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
  public static Member getByUuid(GrouperSession s, String uuid)
    throws MemberNotFoundException
  {
    GrouperSession.validate(s);
    try {
      Member  m       = null;
      Session hs      = HibernateHelper.getSession();
      List    members = hs.find(
                          "from Member as m where       "
                          + "m.member_id           = ?  ",
                          uuid,
                          Hibernate.STRING
                        )
                        ;
      if (members.size() == 1) {
        // Member exists
        m = (Member) members.get(0);
        m.setSession(s);
      }
      hs.close();
      return m;
    }
    catch (HibernateException eMNF) {
      throw new MemberNotFoundException(
        "member not found: " + eMNF.getMessage()
      );
    }
  } // public static Member getByUuid(s, uuid)

  
  // Protected Class Methods

  protected static Member findBySubject(Subject subj) 
    throws MemberNotFoundException
  {
    if (subj == null) {
      throw new MemberNotFoundException("invalid subject");
    }
    try {
      Member  m       = null;
      Session hs      = HibernateHelper.getSession();
      List    members = hs.find(
                          "from Member as m where       "
                          + "m.subject_id          = ?  "
                          + "and m.subject_type    = ?  "
                          + "and m.subject_source  = ?",
                          new Object[] { 
                            subj.getId(),
                            subj.getType().getName(),
                            subj.getSource().getId()
                          },
                          new Type[] {
                            Hibernate.STRING,
                            Hibernate.STRING,
                            Hibernate.STRING
                          }
                        )
                        ;
      if (members.size() == 1) {
        // The member already exists
        m = (Member) members.get(0);
      }
      hs.close();
      if (m != null) {
        m.setSubject(subj);
        return m;
      }
      else {
        // Create a new member
        m = Member.addMember(subj);
        return m;
        //return Member.addMember(subj);
      }
    }
    catch (HibernateException e) {
      throw new MemberNotFoundException("member not found: " + e.getMessage());
    }
  } // protected static Member findBySubject(subj)
}

