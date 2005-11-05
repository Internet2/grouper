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
 * @version $Id: MemberFinder.java,v 1.1.2.6 2005-11-05 23:43:46 blair Exp $
 */
public class MemberFinder implements Serializable {

  // Public Class Methods

  /**
   * Convert a {@link Group} to a {@link Member}.
   * <pre class="eg">
   * // Convert Group g to a Member object
   * try {
   *   Member m = MemberFinder.findByGroup(s, g);
   * }
   * catch (MemberNotFoundException e) {
   *   // Member not found
   * }
   * </pre>
   * @param   s   Find {@link Member} within this session context.
   * @param   g   {@link Group} to convert.
   * @return  A {@link Member} object.
   * @throws  MemberNotFoundException
   */
  public static Member findByGroup(GrouperSession s, Group g) 
    throws MemberNotFoundException
  {
    throw new RuntimeException("Not implemented");
  }

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
    if (s == null) {
      throw new MemberNotFoundException("invalid session");
    }
    if (subj == null) {
      throw new MemberNotFoundException("invalid subject");
    }
    try {
      Member  m       = null;
      Session hs      = HibernateUtil.getSession();
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
        m = (Member) members.get(0);
        m.setSession(s);
        m.setSubject(subj);
      }
      hs.close();
      if (m != null) {
        return m;
      }
      throw new MemberNotFoundException("member not found");
    }
    catch (HibernateException e) {
      throw new MemberNotFoundException("member not found: " + e.getMessage());
    }
    finally {
    }
  } // public static Member findBySubject(s, subj)

  /**
   * Find a member by UUID.
   * <pre class="eg">
   * // Find a member by uuid.
   * try {
   *   Member m = MemberFind.findByUuid(s, uuid);
   * }
   * catch (MemberNotFoundException e) {
   *   // Member not found
   * }
   * </pre>
   * @param   s   Find {@link Member} within this session context.
   * @param   uuid  Find {@link Member} with this UUID.
   * @return  A {@link Member} object.
   * @throws  MemberNotFoundException
   */
  public static Member findByUuid(GrouperSession s, String uuid)
    throws MemberNotFoundException
  {
    throw new RuntimeException("Not implemented");
  }

  
  // Protected Class Methods

  protected static Member findBySubject(Subject subj) {
    return new Member(subj);  
/*
    try {
      Member m = new Member(subj);
      System.err.println("MEMBER: " + m);
      HibernateUtil.save(m);
      System.err.println("SAVED?: " + m);
      return m;
    }
    catch (Exception e) {
      throw new RuntimeException("FUCK! " + e.getMessage());
    }
*/
  } // protected static Member findBySubject(subj)
}

