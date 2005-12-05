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
import  edu.internet2.middleware.subject.provider.*;
import  java.util.*;
import  net.sf.hibernate.*;
import  net.sf.hibernate.type.*;
import  org.apache.commons.logging.*;


/**
 * Find memberships within the Groups Registry.
 * <p />
 * @author  blair christensen.
 * @version $Id: MembershipFinder.java,v 1.14 2005-12-05 21:40:02 blair Exp $
 */
public class MembershipFinder {

  // Private Class Constants
  private static final Log LOG = LogFactory.getLog(MembershipFinder.class);


  // Public Class Methods

  /**
   * Return the effective membership if it exists.
   * <p/>
   * <pre class="eg">
   * </pre>
   * @param   s     Get membership within this session context.
   * @param   g     Effective membership has this group.
   * @param   subj  Effective membership has this subject.
   * @param   f     Effective membership has this list.
   * @param   via   Effective membership has this via group.
   * @param   depth Effective membership has this depth.
   * @return  A {@link Membership} object
   * @throws  MembershipNotFoundException 
   */
  public static Membership findEffectiveMembership(
    GrouperSession s, Group g, Subject subj, Field f, Group via, int depth
  )
    throws MembershipNotFoundException
  {
    // TODO Filter
    GrouperSession.validate(s);
    try {
      Member      m   = MemberFinder.findBySubject(s, subj);
      Membership  ms  = findEffectiveMembership(
        g.getUuid(), m.getId(), f, via.getUuid(), depth
      );
      ms.setSession(s);
      return ms;
    }
    catch (MemberNotFoundException eMNF) {
      throw new MembershipNotFoundException(eMNF.getMessage());
    }
  } // public static Membership findEffectiveMembership(s, g, subj, f, via, depth)

  /**
   * Return the immediate membership if it exists.
   * <p/>
   * <pre class="eg">
   * </pre>
   * @param   s     Get membership within this session context.
   * @param   g     Immediate membership has this group.
   * @param   subj  Immediate membership has this subject.
   * @param   f     Immediate membership has this list.
   * @return  A {@link Membership} object
   * @throws  MembershipNotFoundException 
   */
  public static Membership findImmediateMembership(
    GrouperSession s, Group g, Subject subj, Field f
  )
    throws MembershipNotFoundException
  {
    // TODO Filter
    GrouperSession.validate(s);
    try {
      Member      m   = MemberFinder.findBySubject(s, subj);
      Membership  ms  = findImmediateMembership(g.getUuid(), m, f);
      ms.setSession(s);
      return ms;
    }
    catch (MemberNotFoundException eMNF) {
      throw new MembershipNotFoundException(eMNF.getMessage());
    }
  } // public static Membership findImmediateMembership(s, g, m, f)


  // Protected Class Methods

  // @return  Set of matching members for a {@link Group}
  protected static Set findEffectiveMembers(GrouperSession s, Group g, Field f) {
    Set       members = new LinkedHashSet();
    Iterator  iter    = findEffectiveMemberships(s, g, f).iterator();
    while (iter.hasNext()) {
      Membership ms = (Membership) iter.next();
      try {
        members.add(ms.getMember());
      }
      catch (MemberNotFoundException eMNF) {
        // Ignore
      }
    }
    return members;
  } // protected static Set findEffectiveMembers(s, g, f)

  // @return  Set of effective memberships for a group
  protected static Set findEffectiveMemberships(
    GrouperSession s, Group g, Field f
  )
  {
    // TODO Switch to criteria queries?
    Set     mships  = new LinkedHashSet();
    String  msg     = "group hasEffectiveMemberships '" + g.getName() 
      + "'/'" + f.getName() + "'";
    try {
      Session hs  = HibernateHelper.getSession();
      List    l   = hs.find(
        "from Membership as ms where  "
        + "ms.owner_id        = ?     "
        + "and ms.field.name  = ?     " 
        + "and ms.field.type  = ?     "
        + "and ms.depth       > 0     ", 
        new Object[] {
          g.getUuid(), f.getName(), f.getType().toString()
        },
        new Type[] {
          Hibernate.STRING, Hibernate.STRING, Hibernate.STRING
        }
        );
      hs.close();
      mships = _filterMemberships(s, g, f, l, msg);
    }
    catch (HibernateException eH) {
      // TODO Is a RE appropriate here?
      throw new RuntimeException(
        "error finding effective memberships: " + eH.getMessage()
      );  
    }
    return mships;
  } // protected static Set findEffectiveMemberships(s, g, f)

  // @return  Set of effective memberships for a member
  protected static Set findEffectiveMemberships(
    GrouperSession s, Member m, Field f
  )
  {
    // TODO Switch to criteria queries?
    // TODO Filter
    Set mships = new LinkedHashSet();
    try {
      Session hs  = HibernateHelper.getSession();
      List    l   = hs.find(
                      "from Membership as ms where  "
                      + "ms.member_id       = ?     "
                      + "and ms.field.name  = ?     "
                      + "and ms.field.type  = ?     "
                      + "and ms.depth       > 0     ", 
                      new Object[] {
                        m.getId(), f.getName(), f.getType().toString()
                      },
                      new Type[] {
                        Hibernate.STRING, Hibernate.STRING, Hibernate.STRING
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
  } // protected static Set findEffectiveMemberships(s, m, f)

  // @return  Set of matching memberships
  // @return  Set of matching memberships
  // TODO I'm questioning the value of this method
  protected static Set findEffectiveMemberships(
    Group g, Member m, Field f
  )
  {
    // TODO Switch to criteria queries?
    // TODO Filter
    Set mships = new LinkedHashSet();
    try {
      Session hs = HibernateHelper.getSession();
      mships.addAll(
        hs.find(
          "from Membership as ms where  "
          + "ms.owner_id        = ?     "
          + "and ms.member_id   = ?     "
          + "and ms.field.name  = ?     "
          + "and ms.field.type  = ?     "
          + "and ms.depth       > 0     ", 
          new Object[] {
            g.getUuid(), m.getId(), 
            f.getName(), f.getType().toString()
          },
          new Type[] {
            Hibernate.STRING, Hibernate.STRING, 
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
  } // protected static Set findEffectiveMemberships(g, m, f)

  protected static Set findImmediateMembers(GrouperSession s, Group g, Field f) {
    Set       members = new LinkedHashSet();
    Iterator  iter    = findImmediateMemberships(s, g, f).iterator();
    while (iter.hasNext()) {
      Membership ms = (Membership) iter.next();
      try {
        members.add(ms.getMember());
      }
      catch (MemberNotFoundException eMNF) {
        // Ignore
      }
    }
    return members;
  } // protected static Set findImmediateMembers(s, g, f)

  // @return  Set of immediate memberships for a group
  protected static Set findImmediateMemberships(
    GrouperSession s, Group g, Field f
  )
  {
    // TODO Switch to criteria queries?
    Set     mships  = new LinkedHashSet();
    String  msg     = "group hasImmediateMemberships '" + g.getName() 
      + "'/'" + f.getName() + "'";
    try {
      Session hs  = HibernateHelper.getSession();
      List    l   = hs.find(
        "from Membership as ms where  "
        + "ms.owner_id    = ?         "
        + "and ms.field.name  = ?     "
        + "and ms.field.type  = ?     "
        + "and ms.depth   = 0         ",   
        new Object[] {
          g.getUuid(), f.getName(), f.getType().toString()
        },
        new Type[] {
          Hibernate.STRING, Hibernate.STRING, Hibernate.STRING
        }
        );
      hs.close();
      mships = _filterMemberships(s, g, f, l, msg);
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
    GrouperSession s, Member m, Field f
  )
  {
    // TODO Switch to criteria queries?
    // TODO Filter
    Set mships = new LinkedHashSet();
    try {
      Session hs  = HibernateHelper.getSession();
      List    l   = hs.find(
                      "from Membership as ms where  "
                      + "ms.member_id       = ?     "
                      + "and ms.field.name  = ?     "
                      + "and ms.field.type  = ?     "
                      + "and ms.depth       = 0     ",
                      new Object[] {
                        m.getId(), f.getName(), f.getType().toString()
                      },
                      new Type[] {
                        Hibernate.STRING, Hibernate.STRING, Hibernate.STRING
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
  } // protected static Set findImmediateMemberships(s, m, f)

  // @return  Set of matching members for a {@link Group}
  protected static Set findMembers(GrouperSession s, Group g, Field f) {
    Set       members = new LinkedHashSet();
    Iterator  iter    = findMemberships(s, g, f).iterator();
    while (iter.hasNext()) {
      Membership ms = (Membership) iter.next();
      try {
        members.add(ms.getMember());
      }
      catch (MemberNotFoundException eMNF) {
        // Ignore
      }
    }
    return members;
  } // protected static Set findMembers(s, g, f)

  // @return  Set of matching memberships for a {@link Group}
  protected static Set findMemberships(GrouperSession s, Group g, Field f) {
    // TODO Switch to criteria queries?
    Set     mships  = new LinkedHashSet();
    String  msg     = "group hasMemberships '" + g.getName() + "'/'" + f.getName() + "'";
    try {
      Session hs  = HibernateHelper.getSession();
      List    l   = hs.find(
        "from Membership as ms where  "
        + "ms.owner_id        = ?     "
        + "and ms.field.name  = ?     "
        + "and ms.field.type  = ?     ",
        new Object[] {
          g.getUuid(), f.getName(), f.getType().toString()
        },
        new Type[] {
          Hibernate.STRING, Hibernate.STRING, Hibernate.STRING
        }
        );
      hs.close();
      mships = _filterMemberships(s, g, f, l, msg);
    }
    catch (HibernateException e) {
      // TODO Is a RE appropriate here?
      throw new RuntimeException(
        "error checking membership: " + e.getMessage()
      );  
    }
    return mships;
  } // protected static Set findMemberships(s, g, f)

  // @return  Set of matching memberships for a {@link Member}
  protected static Set findMemberships(Member m, Field f) {
    // TODO Switch to criteria queries?
    // TODO Filter
    Set mships = new LinkedHashSet();
    try {
      Session hs = HibernateHelper.getSession();
      mships.addAll(
        hs.find(
          "from Membership as ms where  "
          + "ms.member_id       = ?     "
          + "and ms.field.name  = ?     "
          + "and ms.field.type  = ?     ",
          new Object[] {
            m.getId(), f.getName(), f.getType().toString()
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
  } // protected static Set findMemberships(m, f)

  // @return  Set of matching memberships
  protected static Set findMemberships(String oid, Member m, Field f) {
    // TODO Switch to criteria queries?
    // TODO Filter
    Set mships = new LinkedHashSet();
    try {
      Session hs = HibernateHelper.getSession();
      mships.addAll(
        hs.find(
          "from Membership as ms where  "
          + "ms.owner_id        = ?     "
          + "and ms.member_id   = ?     "
          + "and ms.field.name  = ?     "
          + "and ms.field.type  = ?     ",
          new Object[] {
            oid, m.getId(), 
            f.getName(), f.getType().toString()
          },
          new Type[] {
            Hibernate.STRING, Hibernate.STRING, 
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
  } // protected static Set findMemberships(oid, m, f)

  // @return  Set of matching immediate subjects
  protected static Set findImmediateSubjects(GrouperSession s, String oid, Field f) {
    // TODO Switch to criteria queries?
    // TODO Filter
    Set subjs = new LinkedHashSet();
    try {
      Session   hs    = HibernateHelper.getSession();
      Iterator  iter  = hs.find(
        "from Membership as ms where  "
        + "ms.owner_id        = ?     "
        + "and ms.field.name  = ?     "
        + "and ms.field.type  = ?     "
        + "and ms.depth       = 0     ",
        new Object[] {
          oid, f.getName(), f.getType().toString()
        },
        new Type[] {
          Hibernate.STRING, Hibernate.STRING, Hibernate.STRING
        }
      ).iterator();
      hs.close();
      while (iter.hasNext()) {
        Membership ms = (Membership) iter.next();
        ms.setSession(s);
        try {
          subjs.add(ms.getMember().getSubject());
        }
        catch (MemberNotFoundException eMNF) {
          // TODO Ignore?
        }
        catch (SubjectNotFoundException eSNF) {
          // TODO Ignore?
        }
      }
    }
    catch (HibernateException e) {
      // TODO Is a RE appropriate here?
      throw new RuntimeException(
        "error finding subjects: " + e.getMessage()
      );  
    }
    return subjs;
  } // protected static Set findImmediateSubjects(s, oid, f)

  // @return  Set of matching subjects
  protected static Set findSubjects(GrouperSession s, String oid, Field f) {
    // TODO Switch to criteria queries?
    // TODO Filter
    Set subjs = new LinkedHashSet();
    try {
      Session   hs    = HibernateHelper.getSession();
      Iterator  iter  = hs.find(
        "from Membership as ms where  "
        + "ms.owner_id        = ?     "
        + "and ms.field.name  = ?     "
        + "and ms.field.type  = ?     ",
        new Object[] {
          oid, f.getName(), f.getType().toString()
        },
        new Type[] {
          Hibernate.STRING, Hibernate.STRING, Hibernate.STRING
        }
      ).iterator();
      hs.close();
      while (iter.hasNext()) {
        Membership ms = (Membership) iter.next();
        ms.setSession(s);
        try {
          subjs.add(ms.getMember().getSubject());
        }
        catch (MemberNotFoundException eMNF) {
          // TODO Ignore?
        }
        catch (SubjectNotFoundException eSNF) {
          // TODO Ignore?
        }
      }
    }
    catch (HibernateException e) {
      // TODO Is a RE appropriate here?
      throw new RuntimeException(
        "error finding subjects: " + e.getMessage()
      );  
    }
    return subjs;
  } // protected static Set findSubjects(s, oid, f)

  // @return  {@link Membership} object
  protected static Membership findEffectiveMembership(
    String gid, String mid, Field f, String vid, int depth
  )
    throws MembershipNotFoundException
  {
    // TODO Switch to criteria queries?
    // TODO Filter
    List mships = new ArrayList();
    try {
      Session hs  = HibernateHelper.getSession();
      mships.addAll(
        hs.find(
          "from Membership as ms where  "
          + "ms.owner_id        = ?     "
          + "and ms.member_id   = ?     "
          + "and ms.field.name  = ?     "
          + "and ms.field.type  = ?     "
          + "and ms.via_id      = ?     "
          + "and ms.depth       = ?     ", 
          new Object[] {
            gid, mid, f.getName(), f.getType().toString(), 
            vid, new Integer(depth)
          },
          new Type[] {
            Hibernate.STRING, Hibernate.STRING, Hibernate.STRING,
            Hibernate.STRING, Hibernate.STRING, Hibernate.INTEGER
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
      "effective membership not found: " + gid + "/" + mid + "/" 
      + f.getName() + "/" + vid + "/" + depth
    );
  } // protected static Membership findEffectiveMembership(gid, mid, field, vid, depth)

  // @return  {@link Membership} object
  protected static Membership findImmediateMembership(String oid, Member m, Field f)
    throws MembershipNotFoundException
  {
    // TODO Filter
    Set mships = findMemberships(oid, m, f);
    if (mships.size() == 1) {
      return (Membership) new ArrayList(mships).get(0);
    }
    throw new MembershipNotFoundException("membership not found");
  } // protected static Membership findImmediateMembership(oid, m, f)

  protected static Membership findImmediateMembership(
    GrouperSession s, String oid, Member m, Field f
  )
    throws MembershipNotFoundException
  {
    // TODO Filter
    Membership ms = findImmediateMembership(oid, m, f);
    ms.setSession(s);
    return ms;
  } // protected static Membership findImmediateMembership(s, oid, m, f)


  // Private Class Methods
  private static Set _filterMemberships(
    GrouperSession s, Group g, Field f, List l, String msg
  ) 
  {
    Set       mships  = new LinkedHashSet();
    Iterator iter     = l.iterator();
    GrouperLog.debug(LOG, s, msg + " unfiltered: " + l.size());
    while (iter.hasNext()) {
      Membership  ms  = (Membership) iter.next();
      ms.setSession(s);
      try {
        String      who = msg + " has '" + ms.getMember() 
          + "'/'" + ms.getList().getName() + "'/" + ms.getDepth() + ": ";
        try {
          PrivilegeResolver.getInstance().canPrivDispatch(
            s, g, s.getSubject(), f.getReadPriv());
          mships.add(ms);
          GrouperLog.debug(LOG, s, who + "visible");
        }
        catch (Exception e) {
          GrouperLog.debug(LOG, s, who + "not visible");
        }
      }
      catch (MemberNotFoundException eMNF) {
        GrouperLog.debug(LOG, s, msg + " could not resolve member: " + ms);
      } 
    }
    GrouperLog.debug(LOG, s, msg + " filtered: " + mships.size());
    return mships;
  } // private static Set _filterMemberships(s, g, f, l, msg)

}

