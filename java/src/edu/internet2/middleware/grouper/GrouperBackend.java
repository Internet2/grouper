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
import  edu.internet2.middleware.subject.*;
import  java.io.*;
import  java.sql.*;
import  java.util.*;
import  net.sf.hibernate.*;
import  net.sf.hibernate.cfg.*;
import  org.apache.log4j.*;
import  org.doomdark.uuid.UUIDGenerator;


/** 
 * Internal class providing more direct access to the {@link Grouper} registry
 * for queries and updates.
 * <p />
 * All methods are class methods and most are restricted to use within
 * {@link Grouper}.
 *
 * @author  blair christensen.
 * @version $Id: GrouperBackend.java,v 1.116 2004-12-05 23:31:41 blair Exp $
 */
public class GrouperBackend {

  /*
   * PRIVATE CONSTANTS
   */
  private static final String VAL_NOTNULL = "**NOTNULL**";  // FIXME
  private static final String VAL_NULL    = "**NULL**";     // FIXME
  private static final Logger LOGGER      = 
    Logger.getLogger(GrouperBackend.class.getName());
  private static final Logger LOGGER_Q    = 
    Logger.getLogger(GrouperBackend.class.getName() + ".QUERY");


  /* 
   * PRIVATE CLASS VARIABLES
   */
  private static Configuration   cfg;     // Hibernate configuration
  private static SessionFactory  factory; // Hibernate session factory


  /*
   * CONSTRUCTORS
   */
  protected GrouperBackend() {
    // Provided only for the benefit of finding the Grouper.hbm.xml
    // files.  And yes, there *has* to be a better way.
  }


  /*
   * PUBLIC CLASS METHODS
   */

  /**
   * Formats {@link GrouperGroup} name.
   * <p />
   *
   * @param   stem  Stem of the {@link GrouperGroup}.
   * @param   extn  Extension of the {@link GrouperGroup}.
   * @return  String representation of the group <i>stem</i>,
   *   delimiter, and <i>extension</i>.
   */
  public static String groupName(String stem, String extn) {
    String name;
    if (stem.equals(Grouper.NS_ROOT)) {
      name = extn;
    } else {
      String delim = Grouper.config("hierarchy.delimiter");
      if (extn.indexOf(delim) != -1) {
        // FIXME Throw an exception?  And then test for failure?
        //       Or settle for ye olde null
        Grouper.LOGGER.warn(
          "Extension `" + extn + "' contains delimiter `" + delim + "'"
        );
        name = null;
      } else {
        name = stem + delim + extn;
      }
    }
    return name;
  }


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
    List vals = GrouperBackend._queryKV(
                                        session, "GrouperAttribute", 
                                        "groupKey", g.key()
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

        // And grant ADMIN privilege to the list creator
        GrouperBackend.LOGGER.debug("Converting subject " + s);
        GrouperMember m       = GrouperMember.lookup( s.subject() );
        boolean       granted = false;
        if (m != null) { // FIXME Bah
          GrouperBackend.LOGGER.debug("Converted to member " + m);
          // NS_TYPE groups get `STEM', not `ADMIN'
          if (g.type().equals(Grouper.NS_TYPE)) {
            if (Grouper.naming().grant(s, g, m, "STEM") == true) {
              GrouperBackend.LOGGER.debug("Granted STEM to " + m);
              t.commit(); // XXX Is this commit necessary?
              granted = true;
              rv      = true;
            } else {
              GrouperBackend.LOGGER.debug("Unable to grant STEM to " + m);
            }
          } else {
            // For all other group types default to `ADMIN'
            if (Grouper.access().grant(s, g, m, Grouper.PRIV_ADMIN)) {
              GrouperBackend.LOGGER.debug("Granted ADMIN to " + m);
              t.commit(); // XXX Is this commit necessary?
              granted = true;
              rv      = true;
            } else {
              GrouperBackend.LOGGER.debug("Unable to grant ADMIN to " + m);
            }
          }
        } else {
          GrouperBackend.LOGGER.debug("Unable to convert to member");
        }
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
      Grouper.LOGGER.info(
                          "Unable to add group as stem=`" +
                          stem.value() + "' does not exist."
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
   * @return  Boolean true if group was deleted, false otherwise.
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
    Subject       asSubj  = GrouperSubject.lookup(g.id(), "group");
    GrouperMember asMem   = GrouperMember.lookup(asSubj);
    List memberOf = asMem.listVals(s);
    if ( (members.size() != 0) || (memberOf.size() != 0) ) {
      if (members.size() != 0) {
        Grouper.LOGGER.warn(
          "ERROR: Unable to delete group as it still has members"
        );
      }
      if (memberOf.size() != 0) {
        Grouper.LOGGER.warn(
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
      Iterator attrIter = GrouperBackend._queryKV(
                            session, "GrouperAttribute",
                            "groupKey", g.key()
                          ).iterator();
      while (attrIter.hasNext()) {
        GrouperAttribute ga = (GrouperAttribute) attrIter.next();
        session.delete(ga);
      }
                
      // Remove schema
      Iterator schemaIter = GrouperBackend._queryKV(
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
    List    vals    = GrouperBackend._queryAll(session, "GrouperField");
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
  protected static GrouperGroup groupLoadByKey(String key) {
    /*
     * While most private class methods take a Session as an argument,
     * this method does not because to do so would possibly cause
     * non-uniqueness issues.
     */
    Session       session = GrouperBackend._init();
    GrouperGroup  g       = new GrouperGroup();

    // TODO Verify that key != null
    try {
      // Attempt to load a stored group into the current object
      Transaction tx = session.beginTransaction();
      session.load(g, key);
  
      // Its schema
      if ( GrouperBackend._groupHasSchema(session, g) == true ) {
        // And its attributes
        GrouperBackend._groupAttachAttrs(session, g, key);

        // FIXME Attach s to object?

        tx.commit();
      } else {
        System.err.println("Unable to load group schema");
        System.exit(1);
      }
    } catch (Exception e) {
      // TODO Rollback if load fails?  Unset this.exists?
      System.err.println(e);
      System.exit(1);
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
      GrouperBackend.LOGGER.warn("Unable to update group " + g);
    }
    GrouperBackend._hibernateSessionClose(session);
    return rv;
  }

  /**
   * Verify whether the specified group, member, list, and via
   * combination exists within the groups registry.
   *
   * @param s     Verify data within this session context.
   * @param g     Verify data for this group.
   * @param m     Verify data for this member
   * @param list  Verify data for this list type.
   * @return  Boolean true if value combination exists, boolean false
   *   otherwise.
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
  protected static boolean listAddVal(GrouperSession s, GrouperGroup g, GrouperMember m, String list) {
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
      // TODO Verify that the subject has privilege to add this list data
      // TODO Verify that this data does not already exist

      try {
        Transaction t = session.beginTransaction();

        // Update immediate list data
        GrouperBackend._listAddVal(session, g, m, list, null);

        GrouperGroup memberOfBase = g;
        // Is this member a group?
        if (m.typeID().equals("group")) {
          memberOfBase = GrouperBackend._groupLoadByID( m.subjectID() );
        }

        // Grab immediate list data to update
        List imms = GrouperBackend._listVals(
                                             session, memberOfBase, null,
                                             list, Grouper.MEM_IMM
                                            );

        // Update effective list data
        Iterator effIter = GrouperBackend._memberOf(
                            session, memberOfBase, list
                           ).iterator();
        while (effIter.hasNext()) {
          GrouperVia via = (GrouperVia) effIter.next();
          GrouperBackend._listAddVal(
                                     session, via.group(),
                                     via.member(), list, via.via()
                                    );
          Iterator immsIter = imms.iterator();
          while(immsIter.hasNext()) {
            GrouperList   gl  = (GrouperList) immsIter.next();
            GrouperMember mem = (GrouperMember) gl.member();
            if (mem != null) {
              GrouperBackend._listAddVal(
                                         session, via.group(),
                                         mem, list, memberOfBase
                                        );
            } // TODO else...
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

        // TODO Verify that the subject has privilege to remove this list data

        /*
         * When deleting a list value, take the following approach:
         * - Delete (`g', `m', `list', null)
         * - Delete ( * , `m', `list', `g' )
         * - If `m' is a group:
         *   - Delete ( *, * , `list', `m' )
         */

        // Update immediate list data for `m'
        GrouperBackend._listDelVal(session, g, m, list, null);

        // Update effective list data for `m'
        Iterator iterViaG = GrouperBackend._queryGrouperList(
                              session, GrouperBackend.VAL_NOTNULL,
                              GrouperBackend.VAL_NOTNULL, list, g.key()
                            ).iterator();
        while (iterViaG.hasNext()) {
          GrouperList   gl  = (GrouperList) iterViaG.next();
          GrouperGroup  grp = gl.group();
          GrouperMember mem = gl.member();
          if ( (grp != null) && (mem != null) ) {
            GrouperBackend._listDelVal(session, grp, mem, list, g);
          }
        }

        // If `m' is a group, delete effective list data via `m'
        if (m.typeID().equals("group")) {
          GrouperGroup  mAsG        = GrouperBackend._groupLoadByID( 
                                        m.subjectID() 
                                      );
          Iterator      iterViaMAsG = GrouperBackend._queryGrouperList(
                                        session, 
                                        GrouperBackend.VAL_NOTNULL, 
                                        GrouperBackend.VAL_NOTNULL, 
                                        list, mAsG.key()
                                      ).iterator();
          while (iterViaMAsG.hasNext()) {
            GrouperList   gl  = (GrouperList) iterViaMAsG.next();
            GrouperGroup  grp = gl.group();
            GrouperMember mem = gl.member();
            if ( (grp != null) && (mem != null) ) {
              GrouperBackend._listDelVal(session, grp, mem, list, mAsG);
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
    List    vals    = GrouperBackend._queryAll(session, "GrouperTypeDef");
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
    Iterator  iter    = GrouperBackend._queryKV(
                                                session, "GrouperSchema",
                                                "groupType", type
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
   * Retrieve all valid {@link GrouperGroup} types.
   * <p />
   *
   * @return List of {@link GrouperType} objects.
   */
  protected static List groupTypes() {
    Session session = GrouperBackend._init();
    List    vals    = GrouperBackend._queryAll(session, "GrouperType");
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
    List          vals    = GrouperBackend._queryKVKV(
                              session, "GrouperMember",
                              "subjectID", subjectID,
                              "subjectTypeID", subjectTypeID
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
    List    vals    = GrouperBackend._queryKV(
                                              session, "GrouperSchema", 
                                              "groupKey", g.key()
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
    List    vals    = GrouperBackend._queryKV(
                                              session, "GrouperGroup", 
                                              "groupID", id
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
        GrouperBackend.LOGGER.debug(
                                    "subjectLookupTypeGroup() " +
                                    "Returned group is null"
                                   );
      }
    } else {
      GrouperBackend.LOGGER.debug(
                                  "subjectLookupTypeGroup() "  +
                                  "Found " + vals.size()       +
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
    List    vals    = GrouperBackend._queryKVKV(
                        session, "SubjectImpl",
                        "subjectID", id,
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
    List    vals    = GrouperBackend._queryAll(session, "SubjectTypeImpl");
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
    List vals = GrouperBackend._queryGrouperAttr(session, key, field);
    if (vals.size() == 0) {
      // We've got a new one.  Store it.
      try {
        attr = new GrouperAttribute(key, field, value);
        session.save(attr);   
      } catch (HibernateException e) {
        attr = null;
        GrouperBackend.LOGGER.warn(
                                   "Unable to store attribute " +
                                   field + "=" + value
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
          GrouperBackend.LOGGER.warn(
                                     "Unable to update attribute " +
                                     field + "=" + value
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
    List vals = GrouperBackend._queryGrouperAttr(session, key, field);
    if (vals.size() == 1) {
      try {
        GrouperAttribute attr = (GrouperAttribute) vals.get(0);
        session.delete(attr);
        rv = true;
      } catch (HibernateException e) {
        GrouperBackend.LOGGER.warn(
                                   "Unable to delete attribute " + field
                                  );
      }
    }
    return rv;
  }

  private static List _extensions(Session session, String extension) {
    return GrouperBackend._queryKVKV(
                                     session, "GrouperAttribute",
                                     "groupField", "extension",
                                     "groupFieldValue", extension
                                    );
  }

  /* (!javadoc)
   * Attach attributes to a group.
   * FIXME Won't calling g.attribute(...) eventually cause the group's
   *       modify attrs to be updated every time this group is laoded?
   *      
   *       But perhaps the `initialized' hack that I added for another
   *       reason will work?
   */
  private static void _groupAttachAttrs(Session session, GrouperGroup g, String key) {
    // TODO Do I even need `key' passed in?
    Iterator iter = GrouperBackend.attributes(g).iterator();
    while (iter.hasNext()) {
      GrouperAttribute attr = (GrouperAttribute) iter.next();
      g.attribute( attr.field(), attr.value() );
    }
  }

  /*
   * TODO Of what value is this method?  If it would either:
   *      - Take a group type and validate whether this group is of
   *        that type
   *      - Or attached the type to the group object
   *
   *      *Then* this method might have some value.  Right now I'm
   *      dubious.
   */
  private static boolean _groupHasSchema(Session session, GrouperGroup g) {
    boolean rv    = false;
    List    vals  = GrouperBackend._queryKV(
                      session, "GrouperSchema", "groupKey", g.key()
                    );
    // We only want one
    // TODO Attach this to the group object.
    if (vals.size() == 1) {
      rv = true;
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
      String name = GrouperBackend.groupName(stem, extn);
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
    List    vals    = GrouperBackend._queryKV(
                        session, "GrouperGroup",
                        "groupID", id
                      );
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
    List names = GrouperBackend._queryKVKV(
                   session, "GrouperAttribute",
                   "groupField", "name",
                   "groupFieldValue", name
                 );
    Iterator iter = names.iterator();
    while (iter.hasNext()) {
      GrouperAttribute attr = (GrouperAttribute) iter.next();
      List gs = GrouperBackend._queryKVKV(
                  session, "GrouperSchema",
                  "groupKey", attr.key(),
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

  private static void _hibernateSessionClose(Session session) {
    try {
      if (session.isDirty() == true) {
        Grouper.LOGGER.debug("Flushing dirty Hibernate session");
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
        Grouper.LOGGER.debug("Calling commit() on Hibernate connection");
        session.connection().commit();
      } catch (SQLException e) {
        System.err.println("SQL Commit Exception:" + e);
        System.exit(1);
      }
      Grouper.LOGGER.debug("Closing Hibernate session");
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
      GrouperBackend  tmp = new GrouperBackend();
      InputStream     in  = tmp.getClass().getResourceAsStream("Grouper.hbm.xml");
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
      vals = GrouperBackend._queryGrouperList(
                                              session, g.key(), 
                                              m.key(), list, via_param
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
    Grouper.LOGGER.debug("_listDelVal() (g) " + g);
    Grouper.LOGGER.debug("_listDelVal() (m) " + m);
    Grouper.LOGGER.debug("_listDelVal() (t) " + list);
    if (via != null) {
      Grouper.LOGGER.debug("_listDelVal() (v) " + via);
    } else {
      Grouper.LOGGER.debug("_listDelVal() (v) null");
    }

    // Confirm that the data exists
    if (GrouperBackend._listValExist(g, m, list, via) == true) {
      Grouper.LOGGER.debug("_listDelVal() Value exists");
      GrouperList gl = GrouperBackend._listVal(g, m, list, via);
      Grouper.LOGGER.debug("_listDelVal() Deleting " + gl);
      try {
        // Delete it
        session.delete(gl); 
        session.flush(); // XXX
        Grouper.LOGGER.debug("_listDelVal() deleted");
      } catch (HibernateException e) {
        System.err.println(e);
        System.exit(1);
      }
    } else {
      Grouper.LOGGER.debug("_listDelVal() Value doesn't exist");
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
    return GrouperBackend._queryGrouperList(
                                            session, gkey_param, 
                                            mkey_param, list, via_param
                                           );
  }
   
  /*
   * The memberOf algorithim: Grouper's one trick pony
   * <http://middleware.internet2.edu/dir/groups/docs/internet2-mace-dir-groups-best-practices-200210.htm>
   * Section 7.1
   */
  private static Set _memberOf(Session session, GrouperGroup g, String list) {
    Set memberships = new HashSet();
    Set newGroups   = new HashSet();

    // Get initial group memberships for group `g' and assign to
    // `newGroups'.
    newGroups = GrouperBackend._memberOfQuery(session, g, list);
    // For each group in `newGroups', convert to a membership object
    // and assign to `memberships'
    GrouperMember member  = GrouperMember.lookup( g.id(), "group");
    Iterator      immIter = newGroups.iterator();
    while (immIter.hasNext()) {
      GrouperGroup  immediate = (GrouperGroup) immIter.next();
      memberships.add( new GrouperVia(member, immediate, null) );
      // XXX System.err.println("I MEMBER " + g);
      // XXX System.err.println("I OF     " + immediate);
    }
    while (true) {
      // While there are `newGroups'
      if (newGroups.size() > 0) {
        Set       nextGroups  = new HashSet();
        Iterator  newIter     = newGroups.iterator();
        while (newIter.hasNext()) {
          // Lookup group membership for each group in `newGroups'
          GrouperGroup  via       = (GrouperGroup) newIter.next();
          Set           effGroups = GrouperBackend._memberOfQuery(session, via, list);
          Iterator      effIter   = effGroups.iterator();
          while (effIter.hasNext()) {
            GrouperGroup effective = (GrouperGroup) effIter.next();
            // Add to `memberships'
            memberships.add( new GrouperVia(member, effective, via) );
            // TODO I need to update the !group memberships as well
            // Add additional groups to `nextGroups'
            nextGroups.add(effective);
            // XXX System.err.println("E MEMBER " + g);
            // XXX System.err.println("E OF     " + effective);
            // XXX System.err.println("E VIA    " + via);
          }
        }
        // Set `newGroups' to the next set of groups to query
        newGroups = nextGroups;
      } else {
        break;
      }
    }
    return memberships;
  }
  
  /*
   * TODO
   */
  private static Set _memberOfQuery(Session session, GrouperGroup g, String list) {
    //List  vals    = new ArrayList();
    Set   groups  = new HashSet();
    // TODO Better validation efforts, please.
    // TODO Refactor validation to a method?
    // TODO Better check of session
    // TODO Grouper.groupField(g.type(), list)
    if (session != null) {
      // TODO Verify that the subject has privilege to retrieve this list data
      // TODO I should stop relying upon the .key() methods.  RSN.

      try {
        // Query away!
        // Make group a member
        // TODO Or should I just cheat and go straight to the GB method?
        GrouperMember m     = GrouperMember.lookup( g.id(), "group" );
        if (m != null) { // FIXME Bah!
          Iterator      iter  = GrouperBackend._queryGrouperList(
                                  session, GrouperBackend.VAL_NOTNULL, 
                                  m.key(), list, GrouperBackend.VAL_NULL
                                ).iterator();
          while (iter.hasNext()) {
            GrouperList   gl  = (GrouperList) iter.next();
            GrouperGroup  grp = gl.group();
            groups.add(grp);
          }
        }
      } catch (Exception e) {
        System.err.println(e);
        System.exit(1);
      }
    }
    return groups;
  }
   
  /*
   * Return all items in a Hibernate-mapped table.
   */
  private static List _queryAll(Session session, String klass) {
    List vals = new ArrayList();
    try { 
      Query q = session.createQuery("FROM " + klass);
      GrouperBackend.LOGGER_Q.debug(
                                    "_queryAll() " + 
                                    q.getQueryString()
                                   );
      vals    = q.list();
    } catch (HibernateException e) {
      System.err.println(e);
      System.exit(1);
    }
    return vals;
  }

  private static List _queryGrouperAttr(
                                        Session session, String key,
                                        String field
                                       )
  {
    List vals = new ArrayList();
    try {
      Query q = session.createQuery(
        "FROM GrouperAttribute AS ga"   +
        " WHERE "                       +
        "ga.groupKey='"   + key   + "'" +
        " AND "                         +
        "ga.groupField='" + field + "'"
      );          
      GrouperBackend.LOGGER_Q.debug(
                                    "_queryGrouperAttr() " +
                                    q.getQueryString()
                                   );
      vals = q.list();
    } catch (HibernateException e) {
      System.err.println(e);
      System.exit(1);
    }
    return vals;
  }

  /*
   * Return matching items from {@link GrouperList}-mapped table.
   */
  private static List _queryGrouperList(
                                        Session session, String gkey,
                                        String  mkey,    String gfield,
                                        String  via
                                       )
  {
    List    vals        = new ArrayList();
    String  gfield_txt  = GrouperBackend._queryNullOrVal(gfield);
    String  gkey_txt    = GrouperBackend._queryNullOrVal(gkey);
    String  mkey_txt    = GrouperBackend._queryNullOrVal(mkey);
    String  via_txt     = "";
    if (via != null) {
      via_txt = " AND gl.via" + GrouperBackend._queryNullOrVal(via);
    }
    try {
      Query q = session.createQuery(
        "FROM GrouperList AS gl"      +
        " WHERE "                     +
        "gl.groupKey"   + gkey_txt    +
        " AND "                       +
        "gl.memberKey"  + mkey_txt    +
        " AND "                       +
        "gl.groupField" + gfield_txt  +
        via_txt
      );
      GrouperBackend.LOGGER_Q.debug(
                                    "_queryGrouperList() " + 
                                    q.getQueryString()
                                   );
      vals = q.list();
    } catch (HibernateException e) {
      System.err.println(e);
      System.exit(1);
    }
    return vals;
  }

  /*
   * Return all items matching key=value specification.
   */
  private static List _queryKV(
                               Session session, String klass, 
                               String key, String value
                              ) 
  {
    List vals = new ArrayList();
    try {
      Query q = session.createQuery(
        "FROM " + klass + " WHERE " + key + "='" + value + "'"
      );
      GrouperBackend.LOGGER_Q.debug(
                                    "_queryKV() " + 
                                    q.getQueryString()
                                   );
      vals = q.list();
    } catch (HibernateException e) {
      System.err.println(e);
      System.exit(1);
    }
    return vals;
  }

  /*
   * Return all items matching two key=value specifications.
   */
  private static List _queryKVKV(
                                 Session session, String klass, 
                                 String key0, String value0,
                                 String key1, String value1
                                ) 
  {
    List vals = new ArrayList();
    try {
      Query q = session.createQuery(
        "FROM " + klass + " WHERE "           +
        key0    + "='"  + value0  + "' AND "  +
        key1    + "='"  + value1  + "'"
      );
      GrouperBackend.LOGGER_Q.debug(
                                    "_queryKVKV() " + 
                                    q.getQueryString()
                                   );
      vals = q.list();
    } catch (HibernateException e) {
      System.err.println(e);
      System.exit(1);
    }
    return vals;
  }

  /*
   * Return string value suitable for insertion into a query.
   */
  private static String _queryNullOrVal(String val) {
    if        (val == null)                             {
      // FIXME Should I allow this?
      val = " IS NOT NULL";
    } else if (val.equals(GrouperBackend.VAL_NULL))     {
      val = " IS NULL";
    } else if (val.equals(GrouperBackend.VAL_NOTNULL))  {
      val = " IS NOT NULL";
    } else {
      val = "='" + val + "'";
    }
    return val;
  }

  /* (!javadoc)
   * Boolean true if stem exists, false otherwise.
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
    return GrouperBackend._queryKVKV(
                                     session, "GrouperAttribute",
                                     "groupField", "stem", 
                                     "groupFieldValue", stem
                                    );
  }
  
}
 
