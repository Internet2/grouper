/* 
 * Copyright (C) 2004 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004 The University Of Chicago
 * All Rights Reserved. 
 *
 * See the LICENSE file in the top-level directory of the 
 * distribution for licensing information.
 */

package edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  java.io.*;
import  java.sql.*;
import  java.util.*;
import  net.sf.hibernate.*;
import  net.sf.hibernate.cfg.*;
import  org.doomdark.uuid.UUIDGenerator;

/** 
 * {@link Grouper} class providing access to backend (queries, adds,
 * deletes, modifies, etc.).
 * <p>
 * All methods are static class methods.
 *
 * @author  blair christensen.
 * @version $Id: GrouperBackend.java,v 1.62 2004-11-23 19:43:26 blair Exp $
 */
public class GrouperBackend {

  // Hibernate configuration
  private static Configuration   cfg;
  // Hibernate session factory
  private static SessionFactory  factory;

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
   * Generate UUID.
   *
   * @return A UUID
   */
  public static String uuid() {
    return UUIDGenerator.getInstance().generateRandomBasedUUID().toString();
  }


  /*
   * PROTECTED CLASS METHODS 
   */

  /**
   * Add a new {@link GrouperSession}.
   *
   * @param s Session to add.
   */
  protected static void addSession(GrouperSession s) {
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
   * Query a group's attributes.
   *
   * @param g Group object
   * @return List of a group's attributes.
   */
  protected static List attributes(GrouperGroup g) {
    Session session     = GrouperBackend._init();
    List    attributes = new ArrayList();
    try {
      Query q = session.createQuery(
        "SELECT FROM grouper_attribute " +
        "IN CLASS edu.internet2.middleware.grouper.GrouperAttribute " +
        "WHERE groupKey='" + g.key() + "'"
      );
      attributes = q.list();
    } catch (Exception e) {
      System.err.println(e);
      System.exit(1);
    }
    GrouperBackend._hibernateSessionClose(session);
    return attributes;
  }

  /**
   * Cull old sessions.
   */
  protected static void cullSessions() {
    Session         session = GrouperBackend._init();
    /* XXX Until I find the time to identify a better way of managing
     *     sessions -- which I *know* exists -- be crude about it. */
    java.util.Date  now     = new java.util.Date();
    long nowTime = now.getTime();
    long tooOld  = nowTime - 360000;

    try {
      session.delete(
        "FROM grouper_session " +
        "IN CLASS edu.internet2.middleware.grouper.GrouperSession " +
        "WHERE startTime > " + nowTime
      );
    } catch (Exception e) {
      System.err.println(e);
      System.exit(1);
    }
    GrouperBackend._hibernateSessionClose(session);
  }

  // TODO
  protected static List descriptors(GrouperSession s, String descriptor) {
    Session session     = GrouperBackend._init();
    List    descriptors = GrouperBackend._descriptors(session, descriptor);
    GrouperBackend._hibernateSessionClose(session);
    return descriptors;
  }

  /**
   * Add {@link GrouperGroup} to backend store.
   *
   * @param s {@link GrouperSession}
   * @param g {@link GrouperGroup} to add
   */
  protected static void groupAdd(GrouperSession s, GrouperGroup g) {
    Session session = GrouperBackend._init();
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
        GrouperAttribute attr = (GrouperAttribute) attributes.get( iter.next() );
        attr.set(g.key(), attr.field(), attr.value());
        session.save(attr);
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
      GrouperMember m = GrouperMember.lookup( s.subject() );
      if (
          (m != null) && // FIXME Bah
          (GrouperPrivilege.grant(s, g, m, "ADMIN") == true)
         )
      {
        t.commit(); // XXX Is this commit necessary?
      } else {
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
    GrouperBackend._hibernateSessionClose(session);
  }

  /**
   * Valid {@link GrouperField} items.
   *
   * @return List of group fields.
   */
  protected static List groupFields() {
    Session session = GrouperBackend._init();
    List    fields  = new ArrayList();
    try {
      Query q = session.createQuery(
        "SELECT ALL FROM GROUPER_FIELD " +
        "IN CLASS edu.internet2.middleware.grouper.GrouperField"
      );
      fields = q.list();
    } catch (Exception e) {
      System.err.println(e);
      System.exit(1);
    }
    GrouperBackend._hibernateSessionClose(session);
    return fields;
  }

  /**
   * Retrieve {@link GrouperGroup} from backend store.
   */
  protected static GrouperGroup groupLoad(
                                          GrouperSession s, String stem,
                                          String descriptor, String type
                                         ) 
  {
    Session session = GrouperBackend._init();
    // TODO G+H Session validation 
    // TODO Priv validation
    GrouperGroup g = GrouperBackend._groupLoad(session, stem, descriptor, type);
    GrouperBackend._hibernateSessionClose(session);
    return g;
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
    // TODO  Reorder params.  Sessions should always come first?  Check
    //       with other methods as well.
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
          memberOfBase = GrouperBackend._groupLoad( m.id() );
        }

        // Grab immediate list data to update
        List imms = GrouperBackend._listVals(
                                             session, memberOfBase,
                                             list, "immediate"
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
            GrouperMember mem = GrouperBackend._member( gl.memberKey() );
            GrouperBackend._listAddVal(
                                       session, via.group(),
                                       mem, list, memberOfBase
                                      );
          }
        }

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

        // Update immediate list data
        GrouperBackend._listDelVal(session, g, m, list, null);

        GrouperGroup memberOfBase = g;
        // Is this member a group?
        if (m.typeID().equals("group")) {
          memberOfBase = GrouperBackend._groupLoad( m.id() );
        }

        // Grab immediate list data to update
        List imms = GrouperBackend._listVals(
                                             session, memberOfBase,
                                             list, "immediate"
                                            );

        // Update effective list data
        Iterator effIter = GrouperBackend._memberOf(
                             session, memberOfBase, list
                           ).iterator();
        while (effIter.hasNext()) {
          GrouperVia via = (GrouperVia) effIter.next();
          GrouperBackend._listDelVal(
                                     session, via.group(),
                                     via.member(), list, via.via()
                                    );
          Iterator immsIter = imms.iterator();
          while(immsIter.hasNext()) {
            GrouperList   gl  = (GrouperList) immsIter.next();
            GrouperMember mem = GrouperBackend._member( gl.memberKey() );
            GrouperBackend._listDelVal(
                                       session, via.group(),
                                       mem, list, memberOfBase
                                      );
          }
        }

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
   * Return list data from the backend store.
   *
   * @param s     Return list data within this session context.
   * @param list  Return this list type.
   */
  protected static List listVals(GrouperSession s, GrouperGroup g, String list) {
    Session session = GrouperBackend._init();
    List    members = new ArrayList();
    // FIXME Better validation efforts, please.
    // TODO  Refactor to a method
    if (
        g.getClass().getName().equals("edu.internet2.middleware.grouper.GrouperGroup")   &&
        s.getClass().getName().equals("edu.internet2.middleware.grouper.GrouperSession") &&
        Grouper.groupField(g.type(), list)
       ) 
    {
      // TODO Verify that the subject has privilege to retrieve this list data
      members = GrouperBackend._listVals(session, g, list, null);
    }
    GrouperBackend._hibernateSessionClose(session);
    return members;
  }

  /**
   * Return all group memberships of the specified type for the 
   * specified {@link GrouperMember}.
   *
   * @param   s       Session to query within.
   * @param   list    Type of list membership to query on.
   * @return  List of {@link GrouperGroup} objects.
   */
  protected static List listVals(GrouperSession s, GrouperMember m, String list) {
    Session session = GrouperBackend._init();
    List    groups  = new ArrayList();
    // FIXME Better validation efforts, please.
    // TODO  Refactor to a method
    if (
        m.getClass().getName().equals("edu.internet2.middleware.grouper.GrouperMember")  &&
        s.getClass().getName().equals("edu.internet2.middleware.grouper.GrouperSession") 
       ) 
    {
      // TODO Verify that the subject has privilege to retrieve this list data
      groups = GrouperBackend._listVals(session, m, list, null);
    }
    GrouperBackend._hibernateSessionClose(session);
    return groups;
  }

  protected static List listEffVals(GrouperSession s, GrouperGroup g, String list) {
    Session session = GrouperBackend._init();
    List    members = new ArrayList();
    // FIXME Better validation efforts, please.
    // TODO  Refactor to a method
    if (
        g.getClass().getName().equals("edu.internet2.middleware.grouper.GrouperGroup")   &&
        s.getClass().getName().equals("edu.internet2.middleware.grouper.GrouperSession") &&
        Grouper.groupField(g.type(), list)
       ) 
    {
      // TODO Verify that the subject has privilege to retrieve this list data
      members = GrouperBackend._listVals(session, g, list, "effective");
    }
    GrouperBackend._hibernateSessionClose(session);
    return members;
  }

  /**
   * Return all effective group memberships of the specified type
   * for the specified {@link GrouperMember}.
   *
   * @param   s       Session to query within.
   * @param   list    Type of list membership to query on.
   * @return  List of {@link GrouperGroup} objects.
   */
  protected static List listEffVals(GrouperSession s, GrouperMember m, String list) {
    Session session = GrouperBackend._init();
    List    groups  = new ArrayList();
    // FIXME Better validation efforts, please.
    // TODO  Refactor to a method
    if (
        m.getClass().getName().equals("edu.internet2.middleware.grouper.GrouperMember")  &&
        s.getClass().getName().equals("edu.internet2.middleware.grouper.GrouperSession") 
       ) 
    {
      // TODO Verify that the subject has privilege to retrieve this list data
      groups = GrouperBackend._listVals(session, m, list, "effective");
    }
    GrouperBackend._hibernateSessionClose(session);
    return groups;
  }

  protected static List listImmVals(GrouperSession s, GrouperGroup g, String list) {
    Session session = GrouperBackend._init();
    List    members = new ArrayList();
    // FIXME Better validation efforts, please.
    // TODO  Refactor to a method
    if (
        g.getClass().getName().equals("edu.internet2.middleware.grouper.GrouperGroup")   &&
        s.getClass().getName().equals("edu.internet2.middleware.grouper.GrouperSession") &&
        Grouper.groupField(g.type(), list)
       ) 
    {
      // TODO Verify that the subject has privilege to retrieve this list data
      members = GrouperBackend._listVals(session, g, list, "immediate");
    }
    GrouperBackend._hibernateSessionClose(session);
    return members;
  }

  /**
   * Return all immediate group memberships of the specified type
   * for the specified {@link GrouperMember}.
   *
   * @param   s       Session to query within.
   * @param   list    Type of list membership to query on.
   * @return  List of {@link GrouperGroup} objects.
   */
  protected static List listImmVals(GrouperSession s, GrouperMember m, String list) {
    Session session = GrouperBackend._init();
    List    groups  = new ArrayList();
    // FIXME Better validation efforts, please.
    // TODO  Refactor to a method
    if (
        m.getClass().getName().equals("edu.internet2.middleware.grouper.GrouperMember")  &&
        s.getClass().getName().equals("edu.internet2.middleware.grouper.GrouperSession") 
       ) 
    {
      // TODO Verify that the subject has privilege to retrieve this list data
      groups = GrouperBackend._listVals(session, m, list, "immediate");
    }
    GrouperBackend._hibernateSessionClose(session);
    return groups;
  }

  /**
   * Valid {@link GrouperTypeDef} items.
   *
   * @return List of group type definitions.
   */
  protected static List groupTypeDefs() {
    Session session   = GrouperBackend._init();
    List    typeDefs  = new ArrayList();
    try {
      Query q = session.createQuery(
        "SELECT ALL FROM GROUPER_GROUPTYPEDEFS " +
        "IN CLASS edu.internet2.middleware.grouper.GrouperTypeDef"
        );
      typeDefs = q.list();
    } catch (Exception e) {
      System.err.println(e);
      System.exit(1);
    }
    GrouperBackend._hibernateSessionClose(session);
    return typeDefs;
  }

  /**
   * Valid {@link GrouperType} items.
   *
   * @return List of group types.
   */
  protected static List groupTypes() {
    Session session = GrouperBackend._init();
    List    types   = new ArrayList();
    try {
      Query q = session.createQuery(
        "SELECT ALL FROM GROUPER_GROUPTYPES " +
        "IN CLASS edu.internet2.middleware.grouper.GrouperType"
        );
      types = q.list();
    } catch (Exception e) {
      System.err.println(e);
      System.exit(1);
    }
    GrouperBackend._hibernateSessionClose(session);
    return types;
  }

  /**
   * Query for a single {@link GrouperMember} by member id and type.
   *
   * @return  {@link GrouperMember} object or null.
   */
  protected static GrouperMember member(String id, String typeID) {
    Session       session = GrouperBackend._init();
    GrouperMember m       = null;
    try {
      Query q = session.createQuery(
        "SELECT ALL FROM GROUPER_MEMBER "     +
        "IN CLASS edu.internet2.middleware.grouper.GrouperMember " +
        "WHERE "                              +
        "subjectID='"     + id      + "' "    + 
        "AND "                                +
        "subjectTypeID='" + typeID  + "'"
      );
      if (q.list().size() == 1) {
        // We only want *one* member.
        m = (GrouperMember) q.list().get(0);
      }
      // TODO Throw an exception?
    } catch (Exception e) {
      System.err.println(e);
      System.exit(1);
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
        ( member.id()     != null ) &&
        ( member.typeID() != null )
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
      return GrouperBackend.member( member.id(), member.typeID() );
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
    List    schemas = new ArrayList();
    try {
      Query q = session.createQuery(
        "SELECT FROM grouper_schema " +
        "IN CLASS edu.internet2.middleware.grouper.GrouperSchema " +
        "WHERE groupKey='" + g.key() + "'"
      );
      schemas = q.list();
    } catch (Exception e) {
      System.err.println(e);
      System.exit(1);
    }
    GrouperBackend._hibernateSessionClose(session);
    return schemas;
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
    try {
      Query q = session.createQuery(
        "SELECT ALL FROM GROUPER_GROUP "     +
        "IN CLASS edu.internet2.middleware.grouper.GrouperGroup " +
        "WHERE "                              +
        "groupKey='"      + id      + "' "
      );
      // We only want *one* subject.
      if (q.list().size() == 1) {
        // Now fetch the group object 
        GrouperGroup g = (GrouperGroup) q.list().get(0);
        // ... And fully populate it (explicitly) since I'm not (yet)
        // making full use of everything Hibernate has to offer.
        // TODO Is this necessary?
        g = GrouperBackend._groupLoad(g.key());
        // ... And convert it to a subject object
        subj = new SubjectImpl(id, typeID);
        GrouperBackend._hibernateSessionClose(session);
        return subj;
      }
      // TODO Throw an exception?
    } catch (Exception e) {
      System.err.println(e);
      System.exit(1);
    }
    GrouperBackend._hibernateSessionClose(session);
    return subj;
  }

  /**
   * Query for a single {@link Subject} of the type "person" using the
   * internal subject store.
   *
   * @return  {@link Subject} object or null.
   */
  protected static Subject subjectLookupTypePerson(String id, String typeID) {
    Session session = GrouperBackend._init();
    Subject subj    = null;
    try {
      Query q = session.createQuery(
        "SELECT ALL FROM GROUPER_SUBJECT "     +
        "IN CLASS edu.internet2.middleware.grouper.SubjectImpl " +
        "WHERE "                              +
        "subjectID='"     + id      + "' "    + 
        "AND "                                +
        "subjectTypeID='" + typeID  + "'"
      );
      if (q.list().size() == 1) {
        // We only want *one* subject.
        subj = (Subject) q.list().get(0);
      }
      // TODO Throw an exception?
    } catch (Exception e) {
      System.err.println(e);
      System.exit(1);
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
    List    types   = new ArrayList();
    try {
      Query q = session.createQuery(
        "SELECT ALL FROM grouper_subjectType " +
        "IN CLASS edu.internet2.middleware.grouper.SubjectTypeImpl"
        );
      types = q.list();
    } catch (Exception e) {
      System.err.println(e);
      System.exit(1);
    }
    GrouperBackend._hibernateSessionClose(session);
    return types;
  }


  /*
   * PRIVATE CLASS METHODS
   */

  private static List _descriptors(Session session, String descriptor) {
    List    descriptors = new ArrayList();
    try {
      Query q = session.createQuery(
        "SELECT FROM grouper_attribute " +
        "IN CLASS edu.internet2.middleware.grouper.GrouperAttribute " +
        "WHERE " +
        "groupField='descriptor' " + 
        "AND " +
        "groupFieldValue='" + descriptor + "'"
      );
      descriptors = q.list();
    } catch (Exception e) {
      System.err.println(e);
      System.exit(1);
    }
    return descriptors;
  }

  //private static boolean _groupExists(Session session, String stem, String descriptor) {
  //}

  // FIXME Refactor.  Mercilesssly.
  // TODO  Take group type into account.
  private static GrouperGroup _groupLoad(String key) {
    /*
     * While most private class methods take a Session as an argument,
     * this method does not because to do so would possibly cause
     * non-uniqueness issues.
     */
    Session       session = GrouperBackend._init();
    GrouperGroup  g       = new GrouperGroup();

    try {
      // Attempt to load a stored group into the current object
      Transaction tx = session.beginTransaction();
      session.load(g, key);
  
      // Its schema
      if ( GrouperBackend._groupLoadSchema(session, g) == true ) {
        // And its attributes
        GrouperBackend._groupLoadAttributes(session, g, key);

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

  // FIXME Refactor.  Mercilesssly.
  private static GrouperGroup _groupLoad(
                                         Session session, String stem, 
                                         String descriptor, String type
                                        ) 
  {
    GrouperGroup  g   = new GrouperGroup();
    String        key = null;

    // TODO Please.  Make this better.  Please, please, please.
    //      For whatever reason, SQL and quality code are evading
    //      me this week.
    List descriptors = GrouperBackend._descriptors(session, descriptor);
    if (descriptors.size() > 0) {
      // We found one or more potential descriptors.  Now look
      // for matching stems.
      List stems = GrouperBackend._stems(session, stem);
      if (stems.size() > 0) {
        // We have potential stems and potential descriptors.
        // Now see if we have the *right* stem and the *right*
        // descriptor.
        Iterator iterDesc = descriptors.iterator();
        while (iterDesc.hasNext()) {
          GrouperAttribute possDesc = (GrouperAttribute) iterDesc.next();
          Iterator iterStem = stems.iterator();
          while (iterStem.hasNext()) {
            GrouperAttribute possStem = (GrouperAttribute) iterStem.next();
            if (
                descriptor.equals( possDesc.value() )   &&
                stem.equals(       possStem.value() )   &&
                possDesc.key().equals( possStem.key() )
               )
            {
              // We have found an appropriate stem and descriptor
              // with matching keys.  We exist!
              try {
                Query q = session.createQuery(
                  "SELECT ALL FROM GROUPER_GROUP " +
                  "IN CLASS edu.internet2.middleware.grouper.GrouperGroup " +
                  "WHERE "                          +
                  "groupKey='" + possDesc.key()     + "'"
                );
                if (q.list().size() == 1) {
                  // We may have a group to restore.  Now check to see
                  // if a group of the proper type exists.
                  Query schemaQuery = session.createQuery(
                    "FROM GrouperSchema AS schema"              +
                    " WHERE "                                   +
                    "schema.groupKey='" + possDesc.key() + "'"  +
                    " AND "                                     +
                    "schema.groupType='"  + type + "'"
                  );
                  if (schemaQuery.list().size() == 1) {
                    // We have a group to restore.  
                    key = possDesc.key();
                    break;
                  }
                }
              } catch (Exception e) {
                System.err.println(e);
                System.exit(1);
              }
            }
          }
        }
      }
    }
    if (key != null) {
      g = GrouperBackend._groupLoad(key);
    }
    // TODO Here I return a dummy object while elsewhere, and with
    //      other classes, I return null.  Standardize.  I *probably*
    //      should return null.
    return g;
  }

  private static void _groupLoadAttributes(Session session, GrouperGroup g, String key) {
    // TODO Do I even need `key' passed in?
    Iterator iter = GrouperBackend.attributes(g).iterator();
    while (iter.hasNext()) {
      GrouperAttribute attr = (GrouperAttribute) iter.next();
      g.attribute( attr.field(), attr.value() );
    }
  }

  // TODO This is becoming really misnamed
  private static boolean _groupLoadSchema(Session session, GrouperGroup g) {
    boolean rv = false;
    try {
      Query q = session.createQuery(
        "FROM GrouperSchema AS schema"          +
        " WHERE "                               +
        "schema.groupKey='"   + g.key()   + "'"
      );
      if (q.list().size() == 1) {
        GrouperSchema schema = (GrouperSchema) q.list().get(0);
        // TODO Attach this to the group object.
        rv = true;
      } else {
        System.err.println("Found " + q.list().size() + 
                           " schema definitions.");
      }
    } catch (HibernateException e) {
      System.err.println(e);
      System.exit(1);
    }
    return rv;
  }

  private static void _hibernateSessionClose(Session session) {
    try {
      // TODO This is excessive.  Either perform dirty check, have a
      //      close method that doesn't flush(), or something.
      session.flush();
      try {
        // TODO This is excessive.  Either perform dirty check, have a
        //      close method that doesn't commit(), or something.
        session.connection().commit();
      } catch (SQLException e) {
        System.err.println("SQL Commit Exception");
        System.exit(1);
      }
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
    // TODO Refactor overlap with ._listValExist()
    // TODO Have GrouperList call this and its kin?
    Session     session = GrouperBackend._init();
    GrouperList gl      = null;
    String      via_txt = null;
    if (via != null) {
      via_txt = "'" + via.key() + "'";
    }
    try {
      Query q = session.createQuery(
        "FROM GrouperList AS gl"              +
        " WHERE "                             +
        "gl.id.groupKey='"   + g.key() + "'"  +
        " AND "                               +
        "gl.id.memberKey='"  + m.key() + "'"  +
        " AND "                               +
        "gl.groupField='"    + list    + "'"  +
        " AND "                               +
        "gl.via=" + via_txt
      );   
      if (q.list().size() == 1) {
        gl = (GrouperList) q.list().get(0);
      } 
    } catch (HibernateException e) {
      System.err.println(e);
      System.exit(1);
    }
    GrouperBackend._hibernateSessionClose(session);
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
    // XXX System.err.println("_LISTDELVAL G " + g);
    // XXX System.err.println("_LISTDELVAL M " + m);
    // XXX System.err.println("_LISTDELVAL T " + list);
    if (via != null) {
      // XXX System.err.println("_LISTDELVAL V " + via);
    } else {
      // XXX System.err.println("_LISTDELVAL V null");
    }

    // Confirm that the data exists
    if (GrouperBackend._listValExist(g, m, list, via) == true) {
      GrouperList gl = GrouperBackend._listVal(g, m, list, via);
      try {
        // Delete it
        session.delete(gl); 
        session.flush(); // XXX
      } catch (HibernateException e) {
        System.err.println(e);
        System.exit(1);
      }
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
    /*
     * While most private class methods take a Session as an argument,
     * this method does not because to do so would possibly cause
     * non-uniqueness issues.
     */
    // TODO Refactor overlap with ._listVal()
    Session session = GrouperBackend._init();
    boolean rv      = false;
    String  via_txt = null;
    if (via != null) {
      via_txt = "'" + via.key() + "'";
    }
    try {
      Query q = session.createQuery(
        "FROM GrouperList AS gl"              +
        " WHERE "                             +
        "gl.id.groupKey='"   + g.key() + "'"  +
        " AND "                               +
        "gl.id.memberKey='"  + m.key() + "'"  +
        " AND "                               +
        "gl.groupField='"    + list    + "'"  +
        " AND "                               +
        "gl.via=" + via_txt
      );   
      if (q.list().size() == 1) {
        rv = true;
      } 
    } catch (HibernateException e) {
      System.err.println(e);
      System.exit(1);
    }
    GrouperBackend._hibernateSessionClose(session);
    return rv;
  }

                      
  // TODO REFACTOR!          
  private static List _listVals(Session session, GrouperMember m, String list, String via) {
    List groups = new ArrayList();
    try {
      // Well isn't this an ugly hack...
      String via_txt = "";
      if (via != null) {
        if        ( via.equals("effective") ) {
          via_txt = " AND gl.via IS NOT NULL";
        } else if ( via.equals("immediate") ) {
          via_txt = " AND gl.via IS NULL";
        } // TODO else ...
      }
      // Query away!
      Query q = session.createQuery(
        "FROM GrouperList AS gl"              +
        " WHERE "                             +
        "gl.memberKey='"     + m.key() + "'"  +
        " AND "                               +
        "gl.groupField='"    + list    + "'"  +
        via_txt
      );   
      // XXX System.err.println("QUERY " + q.getQueryString());
      // TODO Behave different depending upon the size?
      groups = q.list();
    } catch (Exception e) {
      System.err.println(e);
      System.exit(1);
    }
    return groups;
  }
  // TODO REFACTOR!          
  private static List _listVals(Session session, GrouperGroup g, String list, String via) {
    List vals = new ArrayList();
    try {
      // Well isn't this an ugly hack...
      String via_txt = "";
      if (via != null) {
        if        ( via.equals("effective") ) {
          via_txt = " AND gl.via IS NOT NULL";
        } else if ( via.equals("immediate") ) {
          via_txt = " AND gl.via IS NULL";
        } // TODO else ...
      }
      // Query away!
      Query q = session.createQuery(
        "FROM GrouperList AS gl"              +
        " WHERE "                             +
        "gl.groupKey='"      + g.key() + "'"  +
        " AND "                               +
        "gl.groupField='"    + list    + "'"  +
        via_txt
      );   
      // XXX System.err.println("QUERY " + q.getQueryString());
      // TODO Behave different depending upon the size?
      vals = q.list();
    } catch (Exception e) {
      System.err.println(e);
      System.exit(1);
    }
    return vals;
  }
  // TODO REFACTOR!          
  private static List _listVals(Session session, GrouperGroup g, GrouperMember m, String list, String via) {
    List vals = new ArrayList();
    try {
      // Well isn't this an ugly hack...
      String list_txt = " AND gl.groupField";
      if (list == null) {
        list_txt = list_txt + " IS NOT NULL";
      } else {
        list_txt = list_txt + "='" + list + "'";
      }
      String via_txt  = "";
      if (via != null) {
        if        ( via.equals("effective") ) {
          via_txt = " AND gl.via IS NOT NULL";
        } else if ( via.equals("immediate") ) {
          via_txt = " AND gl.via IS NULL";
        } // TODO else ...
      }
      // Query away!
      Query q = session.createQuery(
        "FROM GrouperList AS gl"              +
        " WHERE "                             +
        "gl.groupKey='"      + g.key() + "'"  +
        " AND "                               +
        "gl.memberKey='"     + m.key() + "'"  +
        list_txt                              +
        via_txt
      );   
      System.err.println("QUERY " + q.getQueryString());
      // TODO Behave different depending upon the size?
      vals = q.list();
    } catch (Exception e) {
      System.err.println(e);
      System.exit(1);
    }
    return vals;
  }
   
  /*
   * Query for a single {@link GrouperMember} by memberKey.
   *
   * @return  {@link GrouperMember} object or null.
   */
  private static GrouperMember _member(String key) {
    Session session = GrouperBackend._init();
    try {
      GrouperMember m = new GrouperMember();
      session.load(m, key);
      GrouperBackend._hibernateSessionClose(session);
      return m;
    } catch (Exception e) {
      System.err.println(e);
      System.exit(1);
    }
    GrouperBackend._hibernateSessionClose(session);
    return null;
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
    GrouperMember member  = GrouperMember.lookup( g.key(), "group");
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
    List  vals    = new ArrayList();
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
        GrouperMember m = GrouperMember.lookup( g.key(), "group" );
        Query q = session.createQuery(
          "FROM GrouperList AS gl"              +
          " WHERE "                             +
          "gl.id.memberKey='"  + m.key() + "'"  +
          " AND "                               +
          "gl.groupField='"    + list    + "'"  +
          " AND "                               +
          "gl.via=null"      
        );   
        vals = q.list();
        Iterator iter = vals.iterator();
        while (iter.hasNext()) {
          GrouperList   gl  = (GrouperList) iter.next();
          GrouperGroup  grp = GrouperBackend._groupLoad( gl.groupKey() );
          groups.add(grp);
        }
      } catch (Exception e) {
        System.err.println(e);
        System.exit(1);
      }
    }
    return groups;
  }
   
  private static List _stems(Session session, String stem) {
    List    stems   = new ArrayList();
    try {
      Query q = session.createQuery(
        "SELECT FROM grouper_attribute " +
        "IN CLASS edu.internet2.middleware.grouper.GrouperAttribute " +
        "WHERE " +
        "groupField='stem' " + 
        "AND " +
        "groupFieldValue='" + stem + "'"
      );
      stems = q.list();
    } catch (Exception e) {
      System.err.println(e);
      System.exit(1);
    }
    return stems;
  }
  
}
 
