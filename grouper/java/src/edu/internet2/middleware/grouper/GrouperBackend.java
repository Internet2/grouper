/*
 * Copyright (C) 2004 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004 The University Of Chicago
 * All Rights Reserved. 
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *  * Neither the name of the University of Chicago nor the names
 *    of its contributors nor the University Corporation for Advanced
 *   Internet Development, Inc. may be used to endorse or promote
 *   products derived from this software without explicit prior
 *   written permission.
 *
 * You are under no obligation whatsoever to provide any enhancements
 * to the University of Chicago, its contributors, or the University
 * Corporation for Advanced Internet Development, Inc.  If you choose
 * to provide your enhancements, or if you choose to otherwise publish
 * or distribute your enhancements, in source code form without
 * contemporaneously requiring end users to enter into a separate
 * written license agreement for such enhancements, then you thereby
 * grant the University of Chicago, its contributors, and the University
 * Corporation for Advanced Internet Development, Inc. a non-exclusive,
 * royalty-free, perpetual license to install, use, modify, prepare
 * derivative works, incorporate into the software or other computer
 * software, distribute, and sublicense your enhancements or derivative
 * works thereof, in binary and source code form.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND WITH ALL FAULTS.  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT ARE DISCLAIMED AND the
 * entire risk of satisfactory quality, performance, accuracy, and effort
 * is with LICENSEE. IN NO EVENT SHALL THE COPYRIGHT OWNER, CONTRIBUTORS,
 * OR THE UNIVERSITY CORPORATION FOR ADVANCED INTERNET DEVELOPMENT, INC.
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OR DISTRIBUTION OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.grouper.backend.*;
import  edu.internet2.middleware.subject.*;
import  java.io.*;
import  java.sql.*;
import  java.util.*;
import  net.sf.hibernate.*;
import  net.sf.hibernate.cfg.*;
import  org.doomdark.uuid.UUIDGenerator;


/** 
 * Internal class providing more direct access to the groups registry
 * for queries and updates.
 * <p />
 *
 * @author  blair christensen.
 * @version $Id: GrouperBackend.java,v 1.140 2004-12-09 14:09:56 blair Exp $
 */
public class GrouperBackend {

  /*
   * FIXME This entire class needs to be gutted and refactored.  I
   *       won't complain if all of this code were to disappear.
   *       In fact, I'd probably celebrate.  
   *       
   *       Unfortunately, I don't have time to refactor this before the
   *       release of 0.5.  Alas.
   */

  /*
   * PROTECTED CONSTANTS
   */
  protected static final String VAL_NOTNULL = "**NOTNULL**";  // FIXME
  protected static final String VAL_NULL    = "**NULL**";     // FIXME


  /* 
   * PRIVATE CLASS VARIABLES
   */
  private static Configuration   cfg;     // Hibernate configuration
  private static SessionFactory  factory; // Hibernate session factory


  /*
   * PROTECTED CLASS METHODS 
   */

  /**
   * Store a {@link GrouperAttribute} in the registry.
   * <p />
   *
   * @param   key     {@link GrouperGroup} key.
   * @param   field   {@link GrouperField} field.
   * @param   value   {@link GrouperField} value.
   * @return  The added {@link GrouperAttribute} object.
   */
  protected static GrouperAttribute attrAdd(
                                            String key, String field, 
                                            String value
                                           ) 
  {
    Session session = GrouperBackend._init();
    GrouperAttribute attr = GrouperBackend._attrAdd(
                              session, key, field, value
                            ); 
    GrouperBackend._hibernateSessionClose(session);
    return attr;
  }

  /**
   * Delete a {@link GrouperAttribute} from the registry.
   * <p />
   *
   * @param   key     {@link GrouperGroup} key.
   * @param   field   {@link GrouperField} field.
   * @return  The added {@link GrouperAttribute} object.
   */
  protected static boolean attrDel(
                                   String key, String field
                                  ) 
  {
    boolean rv = false;
    Session session = GrouperBackend._init();
    rv = GrouperBackend._attrDel(session, key, field);
    GrouperBackend._hibernateSessionClose(session);
    return rv;
  }

  /**
   * Query for all of a group's attributes.
   * <p />
   *
   * @param g Group object
   * @return List of a {@link GrouperAttribute} objects.
   */
  protected static List attributes(GrouperGroup g) {
    Session session = GrouperBackend._init();
    List vals = GBQuery.kv(
                  session, "GrouperAttribute", "groupKey", g.key()
                );
    GrouperBackend._hibernateSessionClose(session);
    return vals;
  }

  // TODO
  /**
   * Query for attributes with a field of <i>extension</i> and 
   * the specified value.
   * <p />
   * 
   * @param   s     Perform query within this {@link
   *   GrouperSession}.
   * @param   extn  Query for extensions of this type.
   * @return  List of {@link GrouperAttribute} objects.
   */
  protected static List extensions(GrouperSession s, String extn) {
    Session session     = GrouperBackend._init();
    List    extensions  = GrouperBackend._extensions(session, extn);
    GrouperBackend._hibernateSessionClose(session);
    return extensions;
  }

  /**
   * Add {@link GrouperGroup} to backend store.
   *
   * @param s {@link GrouperSession}
   * @param g {@link GrouperGroup} to add
   */
  protected static boolean groupAdd(GrouperSession s, GrouperGroup g) {
    boolean rv      = false;
    Session session = GrouperBackend._init();
    GrouperAttribute stem = (GrouperAttribute) g.attribute("stem");
    if (GrouperBackend._stemLookup(s, session, (String) stem.value())) {
      try {
        Transaction t = session.beginTransaction();

        // The Group object
        session.save(g);

        // The Group schema
        GrouperSchema schema = new GrouperSchema( g.key(), g.type() );
        session.save(schema);

        // The Group attributes
        Map       attributes  = g.attributes();
        Iterator  iter        = attributes.keySet().iterator();
        while (iter.hasNext()) {
          // FIXME WTF?
          GrouperAttribute attr = (GrouperAttribute) attributes.get( iter.next() );
          // TODO Error checking, anyone? 
          GrouperBackend._attrAdd(
                                  session, g.key(), 
                                  attr.field(), attr.value()
                                 );
        }
        
        /*
         * I need to commit the group to the groups registry before
         * granting the ADMIN privs as the act of granting, especially if
         * using the default access privilege implementation, may need to
         * load the group from the groups registry.  If it hasn't been
         * committed, that will obviously fail and Java will go BOOM!
         *
         * Of course, this may make rolling back even the granting fails
         * even more interesting.
         */
        t.commit();

        // We need a root session for for bootstrap privilege granting
        Subject         rootS = GrouperSubject.load(
                                  Grouper.config("member.system"),
                                  Grouper.DEF_SUBJ_TYPE
                                );
        GrouperSession  roots = GrouperSession.start(rootS);

        // And grant ADMIN privilege to the list creator
        Grouper.log().backend("Converting subject " + s);
        GrouperMember m       = GrouperMember.load( s.subject() );
        boolean       granted = false;
        if (m != null) { // FIXME Bah
          Grouper.log().backend("Converted to member " + m);
          // NS_TYPE groups get `STEM', not `ADMIN'
          if (g.type().equals(Grouper.NS_TYPE)) {
            if (Grouper.naming().grant(roots, g, m, "STEM") == true) {
              Grouper.log().backend("Granted STEM to " + m);
              t.commit(); // XXX Is this commit necessary?
              granted = true;
              rv      = true;
            } else {
              Grouper.log().backend("Unable to grant STEM to " + m);
            }
          } else {
            // For all other group types default to `ADMIN'
            if (Grouper.access().grant(roots, g, m, Grouper.PRIV_ADMIN)) {
              Grouper.log().backend("Granted ADMIN to " + m);
              t.commit(); // XXX Is this commit necessary?
              granted = true;
              rv      = true;
            } else {
              Grouper.log().backend("Unable to grant ADMIN to " + m);
            }
          }
        } else {
          Grouper.log().backend("Unable to convert to member");
        }
        // Close root session
        roots.stop();
        if (granted == false) {
          /*
           * TODO Rollback?  Exception?  The rollback would also need to
           *      rollback the granting of the ADMIN privilege.  Or at
           *      least try to.
           */
          System.err.println("Unable to create group " + g);
          System.exit(1);
        }
      } catch (Exception e) {
        // TODO We probably need a rollback in here in case of failure
        //      above.
        System.err.println(e);
        System.exit(1);
      }
    } else { 
      System.err.println("STEM " + stem.value() + " DOES NOT EXIST!");
      Grouper.log().event(
        "Unable to add group as stem=`" + stem.value() + "' does not exist."
      );
    }
    GrouperBackend._hibernateSessionClose(session);
    return rv;
  }

  /**
   * Delete a {@link GrouperGroup} from the registry.
   * <p />
   *
   * @param   s   Session to delete group within.
   * @param   g   Group to delete.
   * @return  True if the group was deleted.
   */
  protected static boolean groupDelete(GrouperSession s, GrouperGroup g) {
    boolean rv = false;
    /*
     * TODO I envision problems when people start creating group types
     *      with other custom fields...
     */
    // Does the group have members?
    List members = g.listVals(s);
    // Is the group a member?
    Subject       asSubj  = GrouperSubject.load(g.id(), "group");
    GrouperMember asMem   = GrouperMember.load(asSubj);
    List memberOf = asMem.listVals(s);
    if ( (members.size() != 0) || (memberOf.size() != 0) ) {
      if (members.size() != 0) {
        Grouper.log().event(
          "ERROR: Unable to delete group as it still has members"
        );
      }
      if (memberOf.size() != 0) {
        Grouper.log().event(
          "ERROR: Unable to delete group as it is a member of other groups"
        );
      }
      return rv;
    }
    Session session = GrouperBackend._init();
    try {
      Transaction t = session.beginTransaction();

      // Revoke all privileges
      Grouper.access().revoke(s, g, "ADMIN");
      Grouper.access().revoke(s, g, "OPTIN");
      Grouper.access().revoke(s, g, "OPTOUT");
      Grouper.access().revoke(s, g, "READ");
      Grouper.access().revoke(s, g, "UPDATE");
      Grouper.access().revoke(s, g, "VIEW");

      Grouper.naming().revoke(s, g, Grouper.PRIV_CREATE);
      Grouper.naming().revoke(s, g, Grouper.PRIV_STEM);

      /*
       * FIXME Remove effected memberships created by this group.
       *       Although, will the above take care of this for me?
       */
      
      // Remove attributes
      Iterator attrIter = GBQuery.kv(
                            session, "GrouperAttribute",
                            "groupKey", g.key()
                          ).iterator();
      while (attrIter.hasNext()) {
        GrouperAttribute ga = (GrouperAttribute) attrIter.next();
        session.delete(ga);
      }
                
      // Remove schema
      Iterator schemaIter = GBQuery.kv(
                              session, "GrouperSchema",
                              "groupKey", g.key()
                            ).iterator();
      while (schemaIter.hasNext()) {
        GrouperSchema gs = (GrouperSchema) schemaIter.next();
        session.delete(gs);
      }

      // Remove group
      session.delete(g);

      // Commit
      t.commit();
      rv = true;
    } catch (HibernateException e) {
      // TODO We probably need a rollback in here in case of failure
      //      above.
      System.err.println(e);
      System.exit(1);
    }
    GrouperBackend._hibernateSessionClose(session);
    return rv;
  }

  /**
   * Valid {@link GrouperField} items.
   *
   * @return List of group fields.
   */
  protected static List groupFields() {
    Session session = GrouperBackend._init();
    List    vals    = GBQuery.all(session, "GrouperField");
    GrouperBackend._hibernateSessionClose(session);
    return vals;
  }

  /**
   * Retrieve {@link GrouperGroup} from backend store.
   */
  protected static GrouperGroup groupLoad(
                                  GrouperSession s, String stem,
                                  String extn, String type
                                ) 
  {
    Session session = GrouperBackend._init();
    // TODO G+H Session validation 
    // TODO Priv validation
    GrouperGroup g = GrouperBackend._groupLoad(
                       s, session, stem, extn, type
                     );
    GrouperBackend._hibernateSessionClose(session);
    return g;
  }

  // FIXME Refactor.  Mercilesssly.
  protected static GrouperGroup groupLoadByID(
                                  GrouperSession s, String id, 
                                  String type
                                ) 
  {
    GrouperGroup g = GrouperBackend._groupLoadByID(id);
    return g;
  }

  // FIXME Refactor.  Mercilesssly.
  protected static GrouperGroup groupLoadByKey(String key) {
    /*
     * While most private class methods take a Session as an argument,
     * this method does not because to do so would possibly cause
     * non-uniqueness issues.
     */
    Session       session = GrouperBackend._init();
    GrouperGroup  g       = new GrouperGroup();

    if (key != null) {
      try {
        // Attempt to load a stored group into the current object
        session.load(g, key);
  
        // Its schema
        GrouperSchema schema = GrouperBackend._groupSchema(session, g);
        if (
            (schema != null)                                &&
            (GrouperBackend._groupAttachAttrs(session, g))  &&
            (g.type( schema.type() ) )
           )
        {
          // TODO Nothing?
        } else {
          System.err.println("Unable to properly load group");
          System.exit(1);
        }
      } catch (Exception e) {
        // TODO Rollback if load fails?  Unset this.exists?
        System.err.println(e);
        System.exit(1);
      }
    }
    GrouperBackend._hibernateSessionClose(session);
    return g;
  }

  /* (!javadoc)
   * Load a group by name.
   */
  protected static GrouperGroup groupLoadByName(
                     GrouperSession s, String name, String type
                   ) 
  {
    Session       session = GrouperBackend._init();
    GrouperGroup g = GrouperBackend._groupLoadByName(
                      s, session, name, type
                     );
    GrouperBackend._hibernateSessionClose(session);
    return g;
  }

  /**
   * TODO Does this actually work?
   */
  protected static boolean groupUpdate(GrouperSession s, GrouperGroup g) {
    boolean rv      = false;
    Session session = GrouperBackend._init();
    try {
      session.update(g);
      rv = true;
    } catch (HibernateException e) {
      rv = false; 
      Grouper.log().backend("Unable to update group " + g);
    }
    GrouperBackend._hibernateSessionClose(session);
    return rv;
  }

  /**
   * Verify whether the specified group, member, list, and via
   * combination exists within the groups registry.
   *
   * @param   s     Verify data within this session context.
   * @param   g     Verify data for this group.
   * @param   m     Verify data for this member
   * @param   list  Verify data for this list type.
   * @return  True if the value combination exists.
   */
  protected static boolean listVal(
                                   GrouperSession s, GrouperGroup g,
                                   GrouperMember m, String list
                                  ) 
  {
    // TODO Basic input data validation
    Session session = GrouperBackend._init();
    boolean rv      = false;
    rv = GrouperBackend._listValExist(g, m, list, null); 
    GrouperBackend._hibernateSessionClose(session);
    return rv;
  }

  /**
   * Add new list data to the backend store.
   *
   * @param g     Add member to this {@link GrouperGroup}.
   * @param s     Add member within this session context.
   * @param m     Add this member.
   * @param list  Add member to this list.
   */
  protected static boolean listAddVal(
                             GrouperSession s, GrouperGroup g, 
                             GrouperMember m, String list
                           ) 
  {
    Session session = GrouperBackend._init();
    boolean rv      = false;
    // FIXME Better validation efforts, please.
    // TODO  Refactor to a method
    // TODO  Refactor commonality with listDelVal
    if (
        g.getClass().getName().equals("edu.internet2.middleware.grouper.GrouperGroup")   &&
        s.getClass().getName().equals("edu.internet2.middleware.grouper.GrouperSession") &&
        m.getClass().getName().equals("edu.internet2.middleware.grouper.GrouperMember")  &&
        Grouper.groupField(g.type(), list)
       ) 
    {
      // Verify that the value doesn't already exist
      List vals = GBQuery.grouperList(
                    session, g.key(), m.key(), Grouper.DEF_LIST_TYPE, 
                    Grouper.MEM_IMM
                  );
      // Doesn't exist
      if (vals.size() == 0) {
        session.clear(); // FIXME
        try {
          Transaction t = session.beginTransaction();

          // Update immediate list data first
          GrouperBackend._listAddVal(session, g, m, list, null);

          // TODO Refactor to a method
          /* 
           * Find where `g' is a member and make `m' an effective
           * member, via `g', of those groups.
           */
          GrouperMember mg = GrouperMember.load(g.id(), "group");
          List groups = new ArrayList();
          Iterator mgA = GrouperBackend.listVals(s, mg, list).iterator();
          while (mgA.hasNext()) {
            GrouperList glA = (GrouperList) mgA.next();
            GrouperBackend._listAddVal(
              session, glA.group(), m, list, g
            );
            groups.add(glA.group());
          }

          // TODO Refactor to a method
          /*
           * If `m' is a group, convert it from a member to a group
	         * object.  Then, query to find its list members and then add
	         * those members to all groups where `m' is a member.
           */
          if (m.typeID().equals("group")) {
            groups.add(g);
            GrouperGroup gm = GrouperBackend._groupLoadByID(m.subjectID());
            // Find all members of `m'
            Iterator mems   = GrouperBackend.listVals(s, gm, list).iterator();
            while (mems.hasNext()) {
              GrouperList gl = (GrouperList) mems.next();
              // If we have a via, use that, otherwise use `m' 
              GrouperGroup v = gl.via();
              if (v == null) {
                v = gm;
              }
              /*
               * Add all members of `m' to all groups that `m' and `g'
               * belong to
               */
              Iterator iter = groups.iterator();
              while (iter.hasNext()) {
                GrouperGroup group = (GrouperGroup) iter.next();
                GrouperBackend._listAddVal(
                  session, group, gl.member(), list, v
                );
              }
            }
          }

          // Update modify information
          session.update(g);
          // Commit it
          t.commit();
          rv = true;
        } catch (Exception e) {
          // TODO We probably need a rollback in here in case of failure
          //      above.
          System.err.println(e);
          System.exit(1);
        }
      }
    } 
    GrouperBackend._hibernateSessionClose(session);
    return rv;
  }


  /**
   * Remove list data from the backend store.
   *
   * @param g     Remove member from this {@link GrouperGroup}.
   * @param s     Remove member within this session context.
   * @param m     Remove this member.
   * @param list  Remove member from this list.
   */
  protected static boolean listDelVal(GrouperSession s, GrouperGroup g, GrouperMember m, String list) {
    Session session = GrouperBackend._init();
    boolean rv      = false;
    // FIXME Better validation efforts, please.
    // TODO  Refactor to a method
    if (
        g.getClass().getName().equals("edu.internet2.middleware.grouper.GrouperGroup")   &&
        s.getClass().getName().equals("edu.internet2.middleware.grouper.GrouperSession") &&
        m.getClass().getName().equals("edu.internet2.middleware.grouper.GrouperMember")  &&
        Grouper.groupField(g.type(), list)
       ) 
    {
      try {
        Transaction t = session.beginTransaction();

        // Update immediate list data for `m' first
        GrouperBackend._listDelVal(session, g, m, list, null);

        // TODO Refactor to a method
        /* 
         * Find where `m' is a member via `g' and remove those
         * effective memberships.
         */
        Iterator iterViaG = GBQuery.grouperList(
                              session, GrouperBackend.VAL_NOTNULL,
                              m.key(), list, g.key()
                            ).iterator();
        List groups = new ArrayList();
        while (iterViaG.hasNext()) {
          GrouperList   gl  = (GrouperList) iterViaG.next();
          GrouperGroup  grp = gl.group();
          GrouperMember mem = gl.member();
          if ( (grp != null) && (mem != null) ) {
            GrouperBackend._listDelVal(session, grp, mem, list, g);
            groups.add(grp);
          }
        }

        // TODO Refactor to a method
        /*
        * If `m' is a group, convert it from a member to a group
	      * object.  Then, query to find its list members and then delete
	      * those members to all groups where `m' is a member.
        */
        if (m.typeID().equals("group")) {
          groups.add(g);
          GrouperGroup gm = GrouperBackend._groupLoadByID(m.subjectID());
          // Find all members of `m'
          Iterator mems   = GrouperBackend.listVals(s, gm, list).iterator();
          while (mems.hasNext()) {
            GrouperList gl = (GrouperList) mems.next();
            // If we have a via, use that, otherwise use `m' 
            GrouperGroup v = gl.via();
            if (v == null) {
              v = gm;
            }
            /*
             * Delete all members of `m' from all groups that `m' and `g'
             * belong to
             */
            Iterator iter = groups.iterator();
            while (iter.hasNext()) {
              GrouperGroup group = (GrouperGroup) iter.next();
              GrouperBackend._listDelVal(
                session, group, gl.member(), list, v
              );
            }
          }
        }

        // Update modify information
        session.update(g);

        // Commit it
        t.commit();

        rv = true;
      } catch (HibernateException e) {
        System.err.println(e);
        System.exit(1);
      }
    } 
    GrouperBackend._hibernateSessionClose(session);
    return rv;
  }

  /**
   * Query for memberships in the specified list.
   * <p />
   *
   * @param   s     Perform query within this session.
   * @param   list  Query on this list type.
   * @return  List of {@link GrouperList} objects.
   */
  protected static List listVals(GrouperSession s, String list) {
    Session session = GrouperBackend._init();
    List    vals    = new ArrayList();
    // FIXME Better validation efforts, please.
    // TODO  Refactor to a method
    if (s.getClass().getName().equals("edu.internet2.middleware.grouper.GrouperSession"))
    {
      // TODO Verify that the subject has privilege to retrieve this list data
      vals = GrouperBackend._listVals(
                                      session, null, null, 
                                      list, Grouper.MEM_ALL
                                     );
    }
    GrouperBackend._hibernateSessionClose(session);
    return vals;
  }

  /**
   * Query for memberships of the specified list in the specified
   * group.
   * <p />
   *
   * @param   s     Perform query within this session.
   * @param   g     Query on this {@link GrouperGroup}.
   * @param   list  Query on this list type.
   * @return  List of {@link GrouperList} objects.
   */
  protected static List listVals(GrouperSession s, GrouperGroup g, String list) {
    Session session = GrouperBackend._init();
    List    vals    = new ArrayList();
    // FIXME Better validation efforts, please.
    // TODO  Refactor to a method
    if (
        g.getClass().getName().equals("edu.internet2.middleware.grouper.GrouperGroup")   &&
        s.getClass().getName().equals("edu.internet2.middleware.grouper.GrouperSession") &&
        Grouper.groupField(g.type(), list)
       ) 
    {
      // TODO Verify that the subject has privilege to retrieve this list data
      vals = GrouperBackend._listVals(
                                      session, g, null, 
                                      list, Grouper.MEM_ALL
                                     );
    }
    GrouperBackend._hibernateSessionClose(session);
    return vals;
  }

  /**
   * Query for memberships of the specified list for the specified
   * member.
   * <p />
   *
   * @param   s     Perform query within this session.
   * @param   m     Query on this {@link GrouperMember}.
   * @param   list  Query on this list type.
   * @return  List of {@link GrouperList} objects.
   */
  protected static List listVals(GrouperSession s, GrouperMember m, String list) {
    Session session = GrouperBackend._init();
    List    vals    = new ArrayList();
    // FIXME Better validation efforts, please.
    // TODO  Refactor to a method
    if (
        m.getClass().getName().equals("edu.internet2.middleware.grouper.GrouperMember")  &&
        s.getClass().getName().equals("edu.internet2.middleware.grouper.GrouperSession") 
       ) 
    {
      // TODO Verify that the subject has privilege to retrieve this list data
      vals = GrouperBackend._listVals(
                                      session, null, m, 
                                      list, Grouper.MEM_ALL
                                     );
    }
    GrouperBackend._hibernateSessionClose(session);
    return vals;
  }

  /**
   * Query for effective memberships in the specified list.
   * <p />
   *
   * @param   s     Perform query within this session.
   * @param   list  Query on this list type.
   * @return  List of {@link GrouperList} objects.
   */
  protected static List listEffVals(GrouperSession s, String list) {
    Session session = GrouperBackend._init();
    List    vals    = new ArrayList();
    // FIXME Better validation efforts, please.
    // TODO  Refactor to a method
    if (s.getClass().getName().equals("edu.internet2.middleware.grouper.GrouperSession"))
    {
      // TODO Verify that the subject has privilege to retrieve this list data
      vals = GrouperBackend._listVals(
                                      session, null, null, 
                                      list, Grouper.MEM_EFF
                                     );
    }
    GrouperBackend._hibernateSessionClose(session);
    return vals;
  }

  /**
   * Query for effective memberships of the specified list in the
   * specified group.
   * <p />
   *
   * @param   s     Perform query within this session.
   * @param   g     Query on this group.
   * @param   list  Query on this list type.
   * @return  List of {@link GrouperList} objects.
   */
  protected static List listEffVals(GrouperSession s, GrouperGroup g, String list) {
    Session session = GrouperBackend._init();
    List    vals    = new ArrayList();
    // FIXME Better validation efforts, please.
    // TODO  Refactor to a method
    if (
        g.getClass().getName().equals("edu.internet2.middleware.grouper.GrouperGroup")   &&
        s.getClass().getName().equals("edu.internet2.middleware.grouper.GrouperSession") &&
        Grouper.groupField(g.type(), list)
       ) 
    {
      // TODO Verify that the subject has privilege to retrieve this list data
      vals = GrouperBackend._listVals(
                                      session, g, null, 
                                      list, Grouper.MEM_EFF
                                     );
    }
    GrouperBackend._hibernateSessionClose(session);
    return vals;
  }

  /**
   * Query for effective memberships for the specified member in the
   * specified list.
   * <p />
   *
   * @param   s     Perform query within this session.
   * @param   m     Query on this {@link GrouperMember}.
   * @param   list  Query on this list type.
   * @return  List of {@link GrouperList} objects.
   */
  protected static List listEffVals(GrouperSession s, GrouperMember m, String list) {
    Session session = GrouperBackend._init();
    List    vals    = new ArrayList();
    // FIXME Better validation efforts, please.
    // TODO  Refactor to a method
    if (
        m.getClass().getName().equals("edu.internet2.middleware.grouper.GrouperMember")  &&
        s.getClass().getName().equals("edu.internet2.middleware.grouper.GrouperSession") 
       ) 
    {
      // TODO Verify that the subject has privilege to retrieve this list data
      vals = GrouperBackend._listVals(
                                      session, null, m, 
                                      list, Grouper.MEM_EFF
                                     );
    }
    GrouperBackend._hibernateSessionClose(session);
    return vals;
  }

  /**
   * Query for immediate memberships in the specified list.
   * <p />
   *
   * @param   s     Perform query within this session.
   * @param   list  Query on this list type.
   * @return  List of {@link GrouperList} objects.
   */
  protected static List listImmVals(GrouperSession s, String list) {
    Session session = GrouperBackend._init();
    List    vals    = new ArrayList();
    // FIXME Better validation efforts, please.
    // TODO  Refactor to a method
    if (s.getClass().getName().equals("edu.internet2.middleware.grouper.GrouperSession")) 
    {
      // TODO Verify that the subject has privilege to retrieve this list data
      vals = GrouperBackend._listVals(
                                      session, null, null, 
                                      list, Grouper.MEM_IMM
                                     );
    }
    GrouperBackend._hibernateSessionClose(session);
    return vals;
  }
  
  /**
   * Query for immediate memberships in the specified list.
   * <p />
   *
   * @param   s     Perform query within this session.
   * @param   g     Query on this {@link GrouperGroup}.
   * @param   list  Query on this list type.
   * @return  List of {@link GrouperList} objects.
   */
  protected static List listImmVals(GrouperSession s, GrouperGroup g, String list) {
    Session session = GrouperBackend._init();
    List    vals    = new ArrayList();
    // FIXME Better validation efforts, please.
    // TODO  Refactor to a method
    if (
        g.getClass().getName().equals("edu.internet2.middleware.grouper.GrouperGroup")   &&
        s.getClass().getName().equals("edu.internet2.middleware.grouper.GrouperSession") &&
        Grouper.groupField(g.type(), list)
       ) 
    {
      // TODO Verify that the subject has privilege to retrieve this list data
      vals = GrouperBackend._listVals(
                                      session, g, null, 
                                      list, Grouper.MEM_IMM
                                     );
    }
    GrouperBackend._hibernateSessionClose(session);
    return vals;
  }

  /**
   * Query for immediate memberships for the specified member in the
   * specified list.
   * <p />
   *
   * @param   s     Perform query within this session.
   * @param   m     Query on this member.
   * @param   list  Query on this list type.
   * @return  List of {@link GrouperList} objects.
   */
  protected static List listImmVals(GrouperSession s, GrouperMember m, String list) {
    Session session = GrouperBackend._init();
    List    vals    = new ArrayList();
    // FIXME Better validation efforts, please.
    // TODO  Refactor to a method
    if (
        m.getClass().getName().equals("edu.internet2.middleware.grouper.GrouperMember")  &&
        s.getClass().getName().equals("edu.internet2.middleware.grouper.GrouperSession") 
       ) 
    {
      // TODO Verify that the subject has privilege to retrieve this list data
      vals = GrouperBackend._listVals(
                                      session, null, m, 
                                      list, Grouper.MEM_IMM
                                     );
    }
    GrouperBackend._hibernateSessionClose(session);
    return vals;
  }

  /**
   * Valid {@link GrouperTypeDef} items.
   *
   * @return List of group type definitions.
   */
  protected static List groupTypeDefs() {
    Session session = GrouperBackend._init();
    List    vals    = GBQuery.all(session, "GrouperTypeDef");
    GrouperBackend._hibernateSessionClose(session);
    return vals;
  }

  /**
   * Query for all groups of the specified type.
   * <p />
   *
   * @param   s     Perform query within this session.
   * @param   type  Query on this {@link GrouperGroup} type.
   * @return  List of {@link GrouperGroup} objects.
   */
  protected static List groupType(GrouperSession s, String type) {
    Session   session = GrouperBackend._init();
    List      vals    = new ArrayList();
    Iterator  iter    = GBQuery.kv(
                          session, "GrouperSchema", "groupType", type
                        ).iterator();
    while (iter.hasNext()) {
      GrouperSchema gs = (GrouperSchema) iter.next();
      // TODO What a hack
      GrouperGroup g = GrouperGroup.loadByKey(s, gs.key(), type);
      if (g != null) {
        vals.add(g);
      }
    }
    GrouperBackend._hibernateSessionClose(session);
    return vals;
  }

  /**
   * Query for groups created after the specified time.
   * <p />
   *
   * @param   d   A {@link java.util.Date} object.
   * @return  List of {@link GrouperGroup} objects.
   */
  protected static List groupCreatedAfter(java.util.Date d) {
    Session   session = GrouperBackend._init();
    List      vals    = new ArrayList();
    Iterator  iter    = GBQuery.kvgt(
                          session, "GrouperGroup",
                          "createTime", Long.toString(d.getTime())
                        ).iterator();
    vals = GrouperBackend._iterGroup(iter);
    GrouperBackend._hibernateSessionClose(session);
    return vals;
  }

  /**
   * Query for groups created before the specified time.
   * <p />
   *
   * @param   d   A {@link java.util.Date} object.
   * @return  List of {@link GrouperGroup} objects.
   */
  protected static List groupCreatedBefore(java.util.Date d) {
    Session   session = GrouperBackend._init();
    List      vals    = new ArrayList();
    Iterator  iter    = GBQuery.kvlt(
                          session, "GrouperGroup",
                          "createTime", Long.toString(d.getTime())
                        ).iterator();
    vals = GrouperBackend._iterGroup(iter);
    GrouperBackend._hibernateSessionClose(session);
    return vals;
  }

  /**
   * Query for groups modified after the specified time.
   * <p />
   *
   * @param   d   A {@link java.util.Date} object.
   * @return  List of {@link GrouperGroup} objects.
   */
  protected static List groupModifiedAfter(java.util.Date d) {
    Session   session = GrouperBackend._init();
    List      vals    = new ArrayList();
    Iterator  iter    = GBQuery.kvgt(
                          session, "GrouperGroup",
                          "modifyTime", Long.toString(d.getTime())
                        ).iterator();
    vals = GrouperBackend._iterGroup(iter);
    GrouperBackend._hibernateSessionClose(session);
    return vals;
  }

  /**
   * Query for groups modified before the specified time.
   * <p />
   *
   * @param   d   A {@link java.util.Date} object.
   * @return  List of {@link GrouperGroup} objects.
   */
  protected static List groupModifiedBefore(java.util.Date d) {
    Session   session = GrouperBackend._init();
    List      vals    = new ArrayList();
    Iterator  iter    = GBQuery.kvlt(
                          session, "GrouperGroup",
                          "modifyTime", Long.toString(d.getTime())
                        ).iterator();
    vals = GrouperBackend._iterGroup(iter);
    GrouperBackend._hibernateSessionClose(session);
    return vals;
  }

  /**
   * Retrieve all valid {@link GrouperGroup} types.
   * <p />
   *
   * @return List of {@link GrouperType} objects.
   */
  protected static List groupTypes() {
    Session session = GrouperBackend._init();
    List    vals    = GBQuery.all(session, "GrouperType");
    GrouperBackend._hibernateSessionClose(session);
    return vals;
  }

  /**
   * Query for a single {@link GrouperMember} by key.
   *
   * @return  {@link GrouperMember} object or null.
   */
  protected static GrouperMember member(String key) {
    Session       session = GrouperBackend._init();
    GrouperMember m       = new GrouperMember();
    if (key != null) {
      try {
        session.load(m, key);
      } catch (ObjectNotFoundException e) {
        // No proper member to return
        m = null;
      } catch (HibernateException e) {
        System.err.println(e);
        System.exit(1);
      }
    } else {
      m = null;
    }
    GrouperBackend._hibernateSessionClose(session);
    return m;
  }

  /**
   * Query for a single {@link GrouperMember} by subject id and type.
   *
   * @return  {@link GrouperMember} object or null.
   */
  protected static GrouperMember member(
                                        String subjectID, 
                                        String subjectTypeID
                                       ) 
  {
    Session       session = GrouperBackend._init();
    GrouperMember m       = null;
    List          vals    = GBQuery.kvkv(
                              session, "GrouperMember", "subjectID", 
                              subjectID, "subjectTypeID", subjectTypeID
                            );
    // We only want one
    if (vals.size() == 1) {
      m = (GrouperMember) vals.get(0);
    }
    GrouperBackend._hibernateSessionClose(session);
    return m;
  }

  /**
   * Add a {@link GrouperMember} to backend store.
   *
   * @param   member  {@link GrouperMember} object to store.
   * @return  {@link GrouperMember} object.
   */
  protected static GrouperMember memberAdd(GrouperMember member) {
    // TODO Should I have session/security restrictions in place?
    Session session = GrouperBackend._init();
    if ( 
        ( member.memberID()   != null) &&
        ( member.subjectID()  != null) &&
        ( member.typeID()     != null)
       ) 
    {
      try {
        Transaction t = session.beginTransaction();

        // Save it
        session.save(member);
      
        // Commit it
        t.commit();
      } catch (Exception e) {
        // TODO We probably need a rollback in here in case of failure
        //      above.
        System.err.println(e);
        System.exit(1);
      }
      GrouperBackend._hibernateSessionClose(session);
      return GrouperBackend.member(member.subjectID(), member.typeID());
    }
    GrouperBackend._hibernateSessionClose(session);
    return null;
  }

  /**
   * Query a group's schema.
   *
   * @param g Group object
   * @return List of a group's schema
   */
  protected static List schemas(GrouperGroup g) {
    Session session = GrouperBackend._init();
    List    vals    = GBQuery.kv(
                        session, "GrouperSchema", "groupKey", g.key()
                      );
    GrouperBackend._hibernateSessionClose(session);
    return vals;
  }

  /**
   * Add a new {@link GrouperSession}.
   *
   * @param s Session to add.
   */
  protected static void sessionAdd(GrouperSession s) {
    Session session = GrouperBackend._init();
    try {
      Transaction t = session.beginTransaction();
      session.save(s);
      t.commit();
    } catch (Exception e) {
      System.err.println(e);
      System.exit(1);
    }
    GrouperBackend._hibernateSessionClose(session);
  }

  /**
   * Delete a {@link GrouperSession}.
   * <p />
   *
   * @param   s   Session to delete.
   * @return  True is fhe session was deleted.
   */
  protected static boolean sessionDel(GrouperSession s) {
    boolean rv = false;
    Session session = GrouperBackend._init();
    try {
      Transaction t = session.beginTransaction();
      session.delete(s);
      t.commit();
      rv = true;
    } catch (Exception e) {
      System.err.println(e);
      System.exit(1);
    }
    GrouperBackend._hibernateSessionClose(session);
    return rv;
  }

  /**
   * Is the {@link GrouperSession} still valid?
   * <p />
   *
   * @param   s   Session to validate. 
   * @return  True if the session is still valid.
   */
  protected static boolean sessionValid(GrouperSession s) {
    boolean rv = false;
    Session session = GrouperBackend._init();
    if (s != null) {
      List vals = GBQuery.kv(
                    session, "GrouperSession", "sessionID", s.id()
                  );
      if (vals.size() == 1) {
        rv = true;
      } else {
        Grouper.log().event("Attempt to use an invalid session");
      }
    }
    GrouperBackend._hibernateSessionClose(session);
    return rv;
  }

  /**
   * Cull old sessions.
   * <p />
   * TODO Go away. 
   */
  protected static void sessionsCull() {
    Session         session = GrouperBackend._init();
    /* XXX Until I find the time to identify a better way of managing
     *     sessions -- which I *know* exists -- be crude about it. */
    java.util.Date  now     = new java.util.Date();
    long nowTime = now.getTime();
    long tooOld  = nowTime - 360000;

    try {
      session.delete(
        "FROM GrouperSession AS gs" +
        " WHERE "                   +
        "gs.startTime > " + nowTime
      );
    } catch (Exception e) {
      System.err.println(e);
      System.exit(1);
    }
    GrouperBackend._hibernateSessionClose(session);
  }

  // TODO
  protected static List stems(GrouperSession s, String stem) {
    Session session = GrouperBackend._init();
    List    stems   = GrouperBackend._stems(session, stem);
    GrouperBackend._hibernateSessionClose(session);
    return stems;
  }

  /**
   * Query for a single {@link Subject} of type "group".
   *
   * @return  {@link Subject} object or null.
   */
  protected static Subject subjectLookupTypeGroup(String id, String typeID) {
    Session session = GrouperBackend._init();
    Subject subj    = null;
    List    vals    = GBQuery.kv(
                        session, "GrouperGroup", "groupID", id
                      );
    // We only want one
    if (vals.size() == 1) {
      /*
       * TODO Do I want/need to fully load the group to validate it
       *      before continuing?  I don't think so, but...
      */
      GrouperGroup g = (GrouperGroup) vals.get(0);
      if (g != null) {
        // ... And convert it to a subject object
        subj = new SubjectImpl(id, typeID);
      } else {
        Grouper.log().backend(
          "subjectLookupTypeGroup() Returned group is null"
        );
      }
    } else {
      Grouper.log().backend(
        "subjectLookupTypeGroup() Found " + vals.size() + 
        " matching groups"
      );
    }
    GrouperBackend._hibernateSessionClose(session);
    return subj;
  }

  /**
   * Query for a single {@link Subject} of the type DEF_SUBJ_TYPE using 
   * the internal subject store.
   *
   * @return  {@link Subject} object or null.
   */
  protected static Subject subjectLookupTypePerson(String id, String typeID) {
    Session session = GrouperBackend._init();
    Subject subj    = null;
    List    vals    = GBQuery.kvkv(
                        session, "SubjectImpl", "subjectID", id,
                        "subjectTypeID", typeID
                      );
    // We only want one
    if (vals.size() == 1) {
      subj = (Subject) vals.get(0);
    }
    GrouperBackend._hibernateSessionClose(session);
    return subj;
  }

  /**
   * Valid {@link SubjectType} items.
   *
   * @return List of subject types.
   */
  protected static List subjectTypes() {
    Session session = GrouperBackend._init();
    List    vals    = GBQuery.all(session, "SubjectTypeImpl");
    GrouperBackend._hibernateSessionClose(session);
    return vals;
  }

  /**
   * Generate UUID using the Doomdark UUID generator.
   * <p />
   *
   * @return A string UUID.
   */
  protected static String uuid() {
    return UUIDGenerator.getInstance().generateRandomBasedUUID().toString();
  }


  /*
   * PRIVATE CLASS METHODS
   */

  /*
   * Hibernate an attribute
   */
  private static GrouperAttribute _attrAdd(
                                    Session session, String key,
                                    String  field,   String value
                                  )
  {
    GrouperAttribute attr = null;
    List vals = GBQuery.grouperAttr(session, key, field);
    if (vals.size() == 0) {
      // We've got a new one.  Store it.
      try {
        attr = new GrouperAttribute(key, field, value);
        session.save(attr);   
      } catch (HibernateException e) {
        attr = null;
        Grouper.log().backend(
          "Unable to store attribute " + field + "=" + value
        );
      }
    } else if (vals.size() == 1) {
      // Attribute already exists.  Check to see if the value has
      // changed.
      attr = (GrouperAttribute) vals.get(0); 
      if (!attr.value().equals(value)) {
        try {
          attr = new GrouperAttribute(key, field, value);
          session.update(attr);
        } catch (HibernateException e) {
          attr = null;
          Grouper.log().backend(
            "Unable to update attribute " + field + "=" + value
          );
        }
      }
    } // TODO else...
    return attr;
  }

  private static boolean _attrDel(
                           Session session, String key, String field
                         ) 
  {
    boolean rv = false;
    List vals = GBQuery.grouperAttr(session, key, field);
    if (vals.size() == 1) {
      try {
        GrouperAttribute attr = (GrouperAttribute) vals.get(0);
        session.delete(attr);
        rv = true;
      } catch (HibernateException e) {
        Grouper.log().backend("Unable to delete attribute " + field);
      }
    }
    return rv;
  }

  private static List _extensions(Session session, String extension) {
    return GBQuery.kvkv(
             session, "GrouperAttribute", "groupField", 
             "extension", "groupFieldValue", extension
           );
  }

  /* (!javadoc)
   * Attach attributes to a group.
   * FIXME Won't calling g.attribute(...) eventually cause the group's
   *       modify attrs to be updated every time this group is loaded?
   *      
   *       But perhaps the `initialized' hack that I added for another
   *       reason will work?
   */
  private static boolean _groupAttachAttrs(Session session, GrouperGroup g) {
    boolean rv = false;
    if (g != null) {
      Iterator iter = GrouperBackend.attributes(g).iterator();
      while (iter.hasNext()) {
        GrouperAttribute attr = (GrouperAttribute) iter.next();
        g.attribute( attr.field(), attr );
        rv = true;
      }
    }
    return rv;
  }

  // FIXME Refactor.  Mercilesssly.
  private static GrouperGroup _groupLoad(
                                GrouperSession s, Session session, String stem, 
                                String extn, String type
                              )
  {
    GrouperGroup g = null;
    if (GrouperBackend._stemLookup(s, session, stem)) {
      String name = GrouperGroup.groupName(stem, extn);
      g = GrouperBackend._groupLoadByName(s, session, name, type);
      // FIXME WTF IS THIS?!?!
      if (g != null) {
      } else {
      }
    }
    return g;
  }

  // FIXME Refactor.  Mercilesssly.
  // TODO  Take group type into account.
  private static GrouperGroup _groupLoadByID(String id) {
    Session session = GrouperBackend._init();
    String  key     = null;
    List    vals    = GBQuery.kv(session, "GrouperGroup", "groupID", id);
    // We only want one
    if (vals.size() == 1) {
      GrouperGroup g = (GrouperGroup) vals.get(0);
      key = g.key();
    }
    GrouperBackend._hibernateSessionClose(session);
    // TODO Verify that key != null or let ByKey() handle that?
    return GrouperBackend.groupLoadByKey(key);
  }

  /* (!javadoc)
   * Load a group by name.
   */
  private static GrouperGroup _groupLoadByName(
                   GrouperSession s, Session session, 
                   String name, String type
                 ) 
  {
    GrouperGroup g = null;
    List names = GBQuery.kvkv(
                   session, "GrouperAttribute", "groupField", "name",
                   "groupFieldValue", name
                 );
    Iterator iter = names.iterator();
    while (iter.hasNext()) {
      GrouperAttribute attr = (GrouperAttribute) iter.next();
      List gs = GBQuery.kvkv(
                  session, "GrouperSchema", "groupKey", attr.key(),
                  "groupType", type
                );
      if (gs.size() == 1) {
        GrouperSchema schema = (GrouperSchema) gs.get(0);
        if (schema.type().equals(type)) {
          // FIXME Isn't this circular?
          GrouperGroup grp = GrouperGroup.loadByKey(
                               s, attr.key(), type
                             );
          if (grp != null) {
            if (grp.type().equals(type)) {
              g = grp; 
            }
          }
        }
      }
    }
    return g;
  }

  /* (!javadoc)
   * Given a {@link GrouperGroup} object, return its matching 
   * {@link GrouperSchema} object.
   * TODO This will need poking when we support multiple types.
   */
  private static GrouperSchema _groupSchema(Session session, GrouperGroup g) {
    GrouperSchema schema = null;
    if (g != null) {
      List    vals  = GBQuery.kv(
                        session, "GrouperSchema", "groupKey", g.key()
                      );
      // TODO For now, we only want one.
      if (vals.size() == 1) {
        schema = (GrouperSchema) vals.get(0);
      }
    }
    return schema;
  }

  private static void _hibernateSessionClose(Session session) {
    try {
      if (session.isDirty() == true) {
        Grouper.log().backend("Flushing dirty Hibernate session");
        session.flush();
      }
      /*
       * FIXME I need to be able to either tell if I am using a session
       *       that has seen updates performed or else have different
       *       versions of this method that can be called.
       *
       *       And of course this *could* just all be premature
       *       optimization.
       */
      try {
        Grouper.log().backend("Calling commit() on Hibernate connection");
        session.connection().commit();
      } catch (SQLException e) {
        System.err.println("SQL Commit Exception:" + e);
        System.exit(1);
      }
      Grouper.log().backend("Closing Hibernate session");
      session.close();
    } catch (HibernateException e) {
      System.err.println(e);
      System.exit(1);
    }      
  }

  /*
   * Initialize Hibernate session
   */
  private static Session _init() {
    if (cfg == null) {
      // TODO Hateful
      GrouperSession tmp = new GrouperSession();
      InputStream    in  = tmp.getClass()
                              .getResourceAsStream("/Grouper.hbm.xml");
      try {
        // conf.load(in);
        cfg = new Configuration()
          .addInputStream(in);
      } catch (MappingException e) {
        System.err.println(e);
        System.exit(1); 
      }
    }
    if (factory == null) {
      try {
        factory = cfg.buildSessionFactory();
      } catch (Exception e) {
        System.err.println(e);
        System.exit(1);
      }
    }
    try {
      return factory.openSession();
    } catch (HibernateException e) {
      System.err.println(e);
      System.exit(1);
    }
    return null;
  }

  /* (!javadoc)
   * Iterate through a list and fully load {@link GrouperGroup}
   * objects.
   */
  private static List _iterGroup(Iterator iter) {
    List vals = new ArrayList();
    while (iter.hasNext()) {
      GrouperGroup g = (GrouperGroup) iter.next();
      if (g != null) {
        g = GrouperBackend.groupLoadByKey(g.key());
        if (g != null) {
          vals.add(g);
        }
      }  
    }
    return vals;
  }

  private static GrouperList _listVal(
                                      GrouperGroup g, GrouperMember m,
                                      String list, GrouperGroup via
                                     )
  {
    /*
     * While most private class methods take a Session as an argument,
     * this method does not because to do so would possibly cause
     * non-uniqueness issues.
     */
    // TODO Have GrouperList call this and its kin?
    Session     session   = GrouperBackend._init();
    GrouperList gl        = null;
    if (g != null) { // TODO 
      List        vals      = new ArrayList();
      String      via_param;
      if (via == null)  {
        via_param = GrouperBackend.VAL_NULL;
      } else {
        via_param = via.key();
      }
      vals = GBQuery.grouperList(
               session, g.key(), m.key(), list, via_param
             );
      // We only want one
      if (vals.size() == 1) {
        gl = (GrouperList) vals.get(0);
      }
      GrouperBackend._hibernateSessionClose(session);
    }
    return gl;
  }

  private static void _listAddVal(
                                  Session session, 
                                  GrouperGroup g, 
                                  GrouperMember m, 
                                  String list, 
                                  GrouperGroup via
                                 ) 
  {
    // XXX System.err.println("_LISTADDVAL G " + g);
    // XXX System.err.println("_LISTADDVAL M " + m);
    // XXX System.err.println("_LISTADDVAL T " + list);
    if (via != null) {
      // XXX System.err.println("_LISTADDVAL V " + via);
    } else {
      // XXX System.err.println("_LISTADDVAL V null");
    }

    // Confirm that list data doesn't already exist
    if (GrouperBackend._listValExist(g, m, list, via) == false) {
      // XXX System.err.println("_lISTADDVAL VALUES DO NOT EXIST");
      // Instantiate the GrouperList object
      GrouperList gl = new GrouperList(g, m, list, via);
      // Save it
      try {
        session.save(gl);
      } catch (HibernateException e) {
        System.err.println(e);
        System.exit(1);
      }
    } else {
      // XXX System.err.println("_LISTADDVAL VALUES EXIST");
    }
  }

  private static void _listDelVal(
                                  Session sess,   // TODO Fix
                                  GrouperGroup g,
                                  GrouperMember m,
                                  String list,
                                  GrouperGroup via
                                 )
  {
    // TODO Resolve non-uniqueness errors generated when I use the
    //      parent session.  Is the addition of list vals immune to
    //      this problem or have I just not triggered it yet?  Or if I
    //      can't, add the disclaimer.
    Session session = GrouperBackend._init(); // XXX
    Grouper.log().backend("_listDelVal() (g) " + g);
    Grouper.log().backend("_listDelVal() (m) " + m);
    Grouper.log().backend("_listDelVal() (t) " + list);
    if (via != null) {
      Grouper.log().backend("_listDelVal() (v) " + via);
    } else {
      Grouper.log().backend("_listDelVal() (v) null");
    }

    // Confirm that the data exists
    if (GrouperBackend._listValExist(g, m, list, via) == true) {
      Grouper.log().backend("_listDelVal() Value exists");
      GrouperList gl = GrouperBackend._listVal(g, m, list, via);
      Grouper.log().backend("_listDelVal() Deleting " + gl);
      try {
        // Delete it
        session.delete(gl); 
        session.flush(); // XXX
        Grouper.log().backend("_listDelVal() deleted");
      } catch (HibernateException e) {
        System.err.println(e);
        System.exit(1);
      }
    } else {
      Grouper.log().backend("_listDelVal() Value doesn't exist");
    }
    GrouperBackend._hibernateSessionClose(session); // XXX
  }

    // TODO Add session disclaimer
  private static boolean _listValExist(
                                       GrouperGroup g, 
                                       GrouperMember m, 
                                       String list, 
                                       GrouperGroup via
                                      ) 
  {
    boolean rv = false;
    GrouperList gl = GrouperBackend._listVal(g, m, list, via);
    if (gl != null) { // FIXME Can I do better than this?
      rv = true;
    }
    return rv;
  }

  // TODO REFACTOR!          
  private static List _listVals(
                                Session session, GrouperGroup g, 
                                GrouperMember m, String list, String via
                               ) 
  {
    // Well isn't this an ugly hack...
    String gkey_param = null;
    if (g != null) {
      gkey_param = g.key();
    } else {
      gkey_param = GrouperBackend.VAL_NOTNULL;
    }
    String mkey_param = null;
    if (m != null) {
      mkey_param = m.key();
    } else {
      mkey_param = GrouperBackend.VAL_NOTNULL;
    }
    String via_param  = null;
    if (!via.equals(Grouper.MEM_ALL)) {
      if        (via.equals(Grouper.MEM_EFF)) {
        via_param = GrouperBackend.VAL_NOTNULL;
      } else if (via.equals(Grouper.MEM_IMM)) {
        via_param = GrouperBackend.VAL_NULL;
      } else {
        System.err.println("Invalid via requirement: " + via);
        System.exit(1);
      }
    }
    return GBQuery.grouperList(
             session, gkey_param, mkey_param, list, via_param
           );
  }
   
  /* (!javadoc)
   * True if the stem exists.
   */
  private static boolean _stemLookup(
                           GrouperSession s, Session session, 
                           String stem
                         ) 
  {
    boolean rv = false;
    if (stem.equals(Grouper.NS_ROOT)) {
      rv = true;
    } else {
      GrouperGroup g = GrouperBackend._groupLoadByName(
                         s, session, stem, Grouper.NS_TYPE
                       );
      if (g != null) {
        rv = true;
      }
    }
    return rv;
  }

  private static List _stems(Session session, String stem) {
    return GBQuery.kvkv(
             session, "GrouperAttribute", "groupField", 
             "stem", "groupFieldValue", stem
           );
  }
  
}
 
