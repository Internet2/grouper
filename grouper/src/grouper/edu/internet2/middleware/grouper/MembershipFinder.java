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
 * @version $Id: MembershipFinder.java,v 1.1.2.13 2005-11-10 16:36:18 blair Exp $
 */
public class MembershipFinder {

  // Public Class Methods

  /**
   * Return the effective membership if it exists.
   * <p/>
   * <pre class="eg">
   * </pre>
   * @param   s     Get membership within this session context.
   * @param   g     Effective membership has this group.
   * @param   m     Effective membership has this member.
   * @param   l     Effective membership has this list.
   * @param   via   Effective membership has this via group.
   * @param   depth Effective membership has this depth.
   * @return  A {@link Membership} object
   * @throws  MembershipNotFoundException 
   */
  public static Membership getEffectiveMembership(
    GrouperSession s, Group g, Member m, String l, Group via, int depth
  )
    throws MembershipNotFoundException
  {
    Membership ms = getEffectiveMembership(
      g.getUuid(), m.getUuid(), l, via.getUuid(), depth
    );
    ms.setSession(s);
    return ms;
  } // public static Membership getEffectiveMembership(s, g, m, l, via, depth)

  /**
   * Return the immediate membership if it exists.
   * <p/>
   * <pre class="eg">
   * </pre>
   * @param   s     Get membership within this session context.
   * @param   g     Immediate membership has this group.
   * @param   m     Immediate membership has this member.
   * @param   l     Immediate membership has this list.
   * @return  A {@link Membership} object
   * @throws  MembershipNotFoundException 
   */
  public static Membership getImmediateMembership(
    GrouperSession s, Group g, Member m, String l
  )
    throws MembershipNotFoundException
  {
    Membership ms = getImmediateMembership(g, m, l);
    ms.setSession(s);
    return ms;
  } // public static Membership getImmediateMembership(s, g, m, l)


  // Protected Class Methods

  // @return  Set of effective memberships for a group
  protected static Set findEffectiveMemberships(
    GrouperSession s, Group g, String field
  )
  {
    // TODO Switch to criteria queries?
    Set mships = new LinkedHashSet();
    try {
      Session hs  = HibernateHelper.getSession();
      List    l   = hs.find(
                      "from Membership as ms where  "
                      + "ms.group_id      = ?       "
                      + "and ms.list_id   = ?       "
                      + "and ms.depth     > 0       ", 
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
    catch (HibernateException eH) {
      // TODO Is a RE appropriate here?
      throw new RuntimeException(
        "error finding effective memberships: " + eH.getMessage()
      );  
    }
    return mships;
  } // protected static Set findEffectiveMemberships(s, g, field)

  // @return  Set of effective memberships for a member
  protected static Set findEffectiveMemberships(
    GrouperSession s, Member m, String field
  )
  {
    // TODO Switch to criteria queries?
    Set mships = new LinkedHashSet();
    try {
      Session hs  = HibernateHelper.getSession();
      List    l   = hs.find(
                      "from Membership as ms where  "
                      + "ms.member_id     = ?       "
                      + "and ms.list_id   = ?       "
                      + "and ms.depth     > 0       ", 
                      new Object[] {
                        m.getUuid(), field
                      },
                      new Type[] {
                        Hibernate.STRING, Hibernate.STRING
                      }
                    )
                    ;
      hs.close();
      mships.addAll(Membership.setSession(s, l));
    }
    catch (HibernateException eH) {
      // TODO Is a RE appropriate here?
      throw new RuntimeException(
        "error finding effective memberships: " + eH.getMessage()
      );  
    }
    return mships;
  } // protected static Set findEffectiveMemberships(s, m, field)

  // @return  Set of matching memberships
  // @return  Set of matching memberships
  // TODO I'm questioning the value of this method
  protected static Set findEffectiveMemberships(
    Group g, Member m, String field
  )
  {
    // TODO Switch to criteria queries?
    Set mships = new LinkedHashSet();
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

  // @return  Set of immediate memberships for a group
  protected static Set findImmediateMemberships(
    GrouperSession s, Group g, String field
  )
  {
    // TODO Switch to criteria queries?
    Set mships = new LinkedHashSet();
    try {
      Session hs  = HibernateHelper.getSession();
      List    l   = hs.find(
                      "from Membership as ms where  "
                      + "ms.group_id      = ?       "
                      + "and ms.list_id   = ?       "
                      + "and ms.depth     = 0       ", 
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
    catch (HibernateException eH) {
      // TODO Is a RE appropriate here?
      throw new RuntimeException(
        "error finding immediate memberships: " + eH.getMessage()
      );  
    }
    return mships;
  } // protected static Set findImmediateMemberships(s, g, field)

  // @return  Set of immediate memberships for a member
  protected static Set findImmediateMemberships(
    GrouperSession s, Member m, String field
  )
  {
    // TODO Switch to criteria queries?
    Set mships = new LinkedHashSet();
    try {
      Session hs  = HibernateHelper.getSession();
      List    l   = hs.find(
                      "from Membership as ms where  "
                      + "ms.member_id     = ?       "
                      + "and ms.list_id   = ?       "
                      + "and ms.depth     = 0       ", 
                      new Object[] {
                        m.getUuid(), field
                      },
                      new Type[] {
                        Hibernate.STRING, Hibernate.STRING
                      }
                    )
                    ;
      hs.close();
      mships.addAll(Membership.setSession(s, l));
    }
    catch (HibernateException eH) {
      // TODO Is a RE appropriate here?
      throw new RuntimeException(
        "error finding immediate memberships: " + eH.getMessage()
      );  
    }
    return mships;
  } // protected static Set findImmediateMemberships(s, m, field)

  // @return  Set of effective memberships for a member
  // @return  Set of effective memberships for a member
  // @return  Set of matching members for a {@link Group}
  protected static Set findMembers(GrouperSession s, Group g, String field) {
    // TODO Switch to criteria queries?
    Set members = new LinkedHashSet();
    try {
      Session     hs  = HibernateHelper.getSession();
      Iterator  iter  = hs.find(
                          "select distinct ms.member_id from Membership "
                          + "as ms where          "
                          + "ms.group_id      = ? "
                          + "and ms.list_id   = ? ",
                          new Object[] {
                            g.getUuid(), field
                          },
                          new Type[] {
                            Hibernate.STRING, Hibernate.STRING
                          }
                        )
                        .iterator()
                        ;
      hs.close();
      while (iter.hasNext()) {
        try {
          members.add(
            MemberFinder.getByUuid( s, (String) iter.next() )
          );
        }
        catch (MemberNotFoundException eMNF) {
          // TODO Throw exception?  Ignore?
        }
      }
    }
    catch (HibernateException eH) {
      // TODO Is a RE appropriate here?
      throw new RuntimeException(
        "error finding members: " + eH.getMessage()
      );  
    }
    return members;
  } // protected static Set findMembers(s, g, field)

  // @return  Set of matching memberships for a {@link Group}
  protected static Set findMemberships(GrouperSession s, Group g, String field) {
    // TODO Switch to criteria queries?
    Set mships = new LinkedHashSet();
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
    Set mships = new LinkedHashSet();
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
    Set mships = new LinkedHashSet();
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

