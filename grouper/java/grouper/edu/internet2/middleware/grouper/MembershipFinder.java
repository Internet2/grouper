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

import  java.util.*;
import  net.sf.hibernate.*;
import  net.sf.hibernate.type.*;


/**
 * Find memberships within the Groups Registry.
 * <p />
 * @author  blair christensen.
 * @version $Id: MembershipFinder.java,v 1.1.2.12 2005-11-09 23:20:03 blair Exp $
 */
class MembershipFinder {

  // Protected Class Methods

  // @return  Set of matching memberships
  protected static Set findEffectiveMemberships(
    Group g, Member m, String field
  )
  {
    // TODO Switch to criteria queries?
    Set mships = new HashSet();
    try {
      Session hs = HibernateHelper.getSession();
      mships.addAll(
        hs.find(
          "from Membership as ms where  "
          + "ms.group_id      = ?       "
          + "and ms.member_id = ?       "
          + "and ms.list_id   = ?       "
          + "and ms.depth     > 0       ", 
          new Object[] {
            g.getUuid(), m.getUuid(), field
          },
          new Type[] {
            Hibernate.STRING, Hibernate.STRING, Hibernate.STRING
          }
        )
      );
      hs.close();
    }
    catch (HibernateException e) {
      // TODO Is a RE appropriate here?
      throw new RuntimeException(
        "error checking membership: " + e.getMessage()
      );  
    }
    return mships;
  } // protected static Set findEffectiveMemberships(g, m, field)

  // @return  Set of matching memberships for a {@link Group}
  protected static Set findMemberships(GrouperSession s, Group g, String field) {
    // TODO Switch to criteria queries?
    Set mships = new HashSet();
    try {
      Session hs  = HibernateHelper.getSession();
      List    l   = hs.find(
                      "from Membership as ms where  "
                      + "ms.group_id      = ?       "
                      + "and ms.list_id   = ?       ",
                      new Object[] {
                        g.getUuid(), field
                      },
                      new Type[] {
                        Hibernate.STRING, Hibernate.STRING
                      }
                    )
                    ;
      hs.close();
      mships.addAll(Membership.setSession(s, l));
    }
    catch (HibernateException e) {
      // TODO Is a RE appropriate here?
      throw new RuntimeException(
        "error checking membership: " + e.getMessage()
      );  
    }
    return mships;
  } // protected static Set findMemberships(s, g, field)

  // @return  Set of matching memberships for a {@link Member}
  protected static Set findMemberships(Member m, String field) {
    // TODO Switch to criteria queries?
    Set mships = new HashSet();
    try {
      Session hs = HibernateHelper.getSession();
      mships.addAll(
        hs.find(
          "from Membership as ms where  "
          + "ms.member_id = ?           "
          + "and ms.list_id   = ?       ",
          new Object[] {
            m.getUuid(), field
          },
          new Type[] {
            Hibernate.STRING, Hibernate.STRING
          }
        )
      );
      hs.close();
    }
    catch (HibernateException e) {
      // TODO Is a RE appropriate here?
      throw new RuntimeException(
        "error checking membership: " + e.getMessage()
      );  
    }
    return mships;
  } // protected static Set findMemberships(m, field)

  // @return  Set of matching memberships
  protected static Set findMemberships(Group g, Member m, String field) {
    // TODO Switch to criteria queries?
    Set mships = new HashSet();
    try {
      Session hs = HibernateHelper.getSession();
      mships.addAll(
        hs.find(
          "from Membership as ms where "
          + "ms.group_id      = ?      "
          + "and ms.member_id = ?      "
          + "and ms.list_id   = ?      ",
          new Object[] {
            g.getUuid(), m.getUuid(), field
          },
          new Type[] {
            Hibernate.STRING, Hibernate.STRING, Hibernate.STRING
          }
        )
      );
      hs.close();
    }
    catch (HibernateException e) {
      // TODO Is a RE appropriate here?
      throw new RuntimeException(
        "error checking membership: " + e.getMessage()
      );  
    }
    return mships;
  } // protected static Set findMemberships(g, m, field)

  // @return  {@link Membership} object
  protected static Membership getEffectiveMembership(
    String gid, String mid, String field, String vid, int depth
  )
    throws MembershipNotFoundException
  {
    // TODO Switch to criteria queries?
    List mships = new ArrayList();
    try {
      Session hs = HibernateHelper.getSession();
      mships.addAll(
        hs.find(
          "from Membership as ms where  "
          + "ms.group_id      = ?       "
          + "and ms.member_id = ?       "
          + "and ms.list_id   = ?       "
          + "and ms.via_id    = ?       "
          + "and ms.depth     = ?       ", 
          new Object[] {
            gid, mid, field, 
            vid, new Integer(depth)
          },
          new Type[] {
            Hibernate.STRING, Hibernate.STRING, Hibernate.STRING,
            Hibernate.STRING, Hibernate.INTEGER
          }
        )
      );
      hs.close();
    }
    catch (HibernateException e) {
      // TODO Is a RE appropriate here?
      throw new RuntimeException(
        "error checking membership: " + e.getMessage()
      );  
    }
    if (mships.size() == 1) {
      Membership ms = (Membership) mships.get(0);
      return ms;
    }
    throw new MembershipNotFoundException(
      "effective membership not found"
    );
  } // protected static Membership getEffectiveMembership(gid, mid, field, vid, depth)

  // @return  {@link Membership} object
  protected static Membership getImmediateMembership(
    Group g, Member m, String field
  )
    throws MembershipNotFoundException
  {
    Set mships = findMemberships(g, m, field);
    if (mships.size() == 1) {
      return (Membership) new ArrayList(mships).get(0);
      //List l = new ArrayList(mships);
      //return (Membership) l.get(0);
    }
    throw new MembershipNotFoundException("membership not found");
  } // protected static Membership getImmediateMembership(g, m, field)

}

