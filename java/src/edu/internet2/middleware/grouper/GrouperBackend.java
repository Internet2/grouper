/* 
 * Copyright (C) 2004 Internet2
 * Copyright (C) 2004 The University Of Chicago
 * All Rights Reserved. 
 *
 * See the LICENSE file in the top-level directory of the 
 * distribution for licensing information.
 */

package edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
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
 * @version $Id: GrouperBackend.java,v 1.23 2004-11-11 18:28:59 blair Exp $
 */
public class GrouperBackend {

  // Hibernate configuration
  private static Configuration   cfg;
  // Hibernate session factory
  private static SessionFactory  factory;
  // Hibernate session
  // TODO Should this really be static?
  private static Session session;

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
   * Add {@link GrouperGroup} to backend store.
   *
   * @param s {@link GrouperSession}
   * @param g {@link GrouperGroup} to add
   */
  protected static void addGroup(GrouperSession s, GrouperGroup g) {
    GrouperBackend._init();
    try {
      Transaction t = session.beginTransaction();

      // The Group object
      session.save(g);

      // FIXME 
      String gt = "base";
      // The Group schema
      GrouperSchema schema = new GrouperSchema(g.key(), gt);
      session.save(schema);

      // The Group attributes
      Map attributes = g.attributes();
      for (Iterator iter = attributes.keySet().iterator(); iter.hasNext();) {
        GrouperAttribute attr = (GrouperAttribute) attributes.get( iter.next() );
        attr.set(g.key(), attr.field(), attr.value());
        session.save(attr);
      }

      // And make the creator a member of the "admins" list
      GrouperMembership mship = new GrouperMembership(); 
      // FIXME Group Key is private.  Fuck!  How to manage?
      //       But wait!  GUID!
      // TODO Don't hardcode "admins"
      mship.set(g.key(), "admins", s.subject().id(), true);
      session.save(mship);

      t.commit();
    } catch (Exception e) {
      // TODO We probably need a rollback in here in case of failure
      //      above.
      System.err.println(e);
      System.exit(1);
    }
  }


  /**
   * Add a new {@link GrouperSession}.
   *
   * @param s Session to add.
   */
  protected static void addSession(GrouperSession s) {
    GrouperBackend._init();
    try {
      Transaction t = session.beginTransaction();
      session.save(s);
      t.commit();
    } catch (Exception e) {
      System.err.println(e);
      System.exit(1);
    }
  }

  /**
   * Query a group's attributes.
   *
   * @param g Group object
   * @return List of a group's attributes.
   */
  protected static List attributes(GrouperGroup g) {
    GrouperBackend._init();
    List attributes = new ArrayList();
    try {
      Query q = session.createQuery(
        "SELECT FROM grouper_attributes " +
        "IN CLASS edu.internet2.middleware.grouper.GrouperAttribute " +
        "WHERE groupKey='" + g.key() + "'"
      );
      attributes = q.list();
    } catch (Exception e) {
      System.err.println(e);
      System.exit(1);
    }
    return attributes;
  }

  /**
   * Cull old sessions.
   */
  protected static void cullSessions() {
    GrouperBackend._init();
    /* XXX Until I find the time to identify a better way of managing
     *     sessions -- which I *know* exists -- be crude about it. */
    java.util.Date now     = new java.util.Date();
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
  }

  /**
   * Valid {@link GrouperField} items.
   *
   * @return List of group fields.
   */
  protected static List groupFields() {
    GrouperBackend._init();
    List fields = new ArrayList();
    try {
      Query q = session.createQuery(
        "SELECT ALL FROM GROUPER_FIELDS " +
        "IN CLASS edu.internet2.middleware.grouper.GrouperField"
      );
      fields = q.list();
    } catch (Exception e) {
      System.err.println(e);
      System.exit(1);
    }
    return fields;
  }

  // TODO
  protected static List descriptors(GrouperSession s, String descriptor) {
    GrouperBackend._init();
    List descriptors = new ArrayList();
    try {
      Query q = session.createQuery(
        "SELECT FROM grouper_attributes " +
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

  /**
   * Return list data from the backend store.
   *
   * @param s     Return list data within this session context.
   * @param list  Return this list type.
   */
  protected static List listVals(GrouperGroup g, GrouperSession s, String list) {
    GrouperBackend._init();
    List members = new ArrayList();
    // FIXME Better validation efforts, please.
    // TODO  Refactor to a method
    if (
        g.getClass().getName().equals("edu.internet2.middleware.grouper.GrouperGroup")   &&
        s.getClass().getName().equals("edu.internet2.middleware.grouper.GrouperSession") &&
        Grouper.groupField(g.type(), list)
       ) 
    {
      // TODO Verify that the subject has privilege to retrieve this list data

      try {
        // Query away!
        Query q = session.createQuery(
          "SELECT ALL FROM grouper_lists "  +
          "IN CLASS edu.internet2.middleware.grouper.GrouperMembership " +
          "WHERE "                          +
          "groupKey='"    + g.key() + "' "  +
          "AND "                            +
          "groupField='"  + list + "' "     +
          "AND "                            +
          "via=null"
        );   
        // TODO Behave different depending upon the size?
        members = q.list();
      } catch (Exception e) {
        System.err.println(e);
        System.exit(1);
      }
    }
    return members;
  }
   
  /**
   * Add new list data to the backend store.
   *
   * @param g     Add member to this {@link GrouperGroup}.
   * @param s     Add member within this session context.
   * @param m     Add this member.
   * @param list  Add member to this list.
   */
  protected static boolean listAddVal(GrouperGroup g, GrouperSession s, GrouperMember m, String list) {
    GrouperBackend._init();
    // FIXME Better validation efforts, please.
    // TODO  Refactor to a method
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

        // Instantiate the GrouperMembership object
        GrouperMembership mship = new GrouperMembership(g, m, list);

        // Save it
        session.save(mship);

        // TODO Update effective memberships
  
        // Commit it
        t.commit();
      } catch (Exception e) {
        // TODO We probably need a rollback in here in case of failure
        //      above.
        System.err.println(e);
        System.exit(1);
      }
      return true;
    } 
    return false;
  }

  /**
   * Remove list data from the backend store.
   *
   * @param g     Remove member from this {@link GrouperGroup}.
   * @param s     Remove member within this session context.
   * @param m     Remove this member.
   * @param list  Remove member from this list.
   */
  protected static boolean listDelVal(GrouperGroup g, GrouperSession s, GrouperMember m, String list) {
    GrouperBackend._init();
    // FIXME Better validation efforts, please.
    // TODO  Refactor to a method
    if (
        g.getClass().getName().equals("edu.internet2.middleware.grouper.GrouperGroup")   &&
        s.getClass().getName().equals("edu.internet2.middleware.grouper.GrouperSession") &&
        m.getClass().getName().equals("edu.internet2.middleware.grouper.GrouperMember")  &&
        Grouper.groupField(g.type(), list)
       ) 
    {
      // TODO Verify that the subject has privilege to remove this list data
      // TODO Verify that this data to be removed exists

      try {
        // Load the GrouperMembership object
        Query q = session.createQuery(
          "SELECT ALL FROM grouper_lists "  +
          "IN CLASS edu.internet2.middleware.grouper.GrouperMembership " +
          "WHERE "                          +
          "groupKey='"    + g.key() + "' "  +
          "AND "                            +
          "memberKey='"   + m.key() + "' "  +
          "AND "                            +
          "groupField='"  + list + "' "     +
          "AND "                            +
          "via=null"
        );   
        if (q.list().size() == 1) {
          // There should be only *one* member to remove
          try {
            GrouperMembership mship = (GrouperMembership) q.list().get(0);
            try {
              Transaction t = session.beginTransaction();
              // Delete it
              session.delete(mship); 
              // TODO Update effective memberships
              // Commit it
              t.commit();
            } catch (Exception e) {
              // TODO We probably need a rollback in here in case of failure
              //      above.
              System.err.println(e);
              System.exit(1);
            }
          } catch (Exception e) {
            System.err.println(e);
            System.exit(1);
          }
          return true;
        } else {
          // TODO Raise an exception of some sort?
          return false;
        }
      } catch (Exception e) {
        System.err.println(e);
        System.exit(1);
      }
    }
    return false;
  }

  // TODO
  protected static void loadGroup(GrouperSession s, GrouperGroup g, String key) {
    GrouperBackend._init();
    try {
      // Attempt to load a stored group into the current object
      Transaction tx = session.beginTransaction();
      session.load(g, key);
      
      // Its schema
      List schemas = GrouperBackend.schemas(g);
      if (schemas.size() == 1) {
        GrouperSchema schema = (GrouperSchema) schemas.get(0);
        // TODO Attach this to the group object.
      } else {
        System.err.println("Found " + schemas.size() + 
                           " schema definitions.");
        System.exit(1);
      }

      // And its attributes
      List attrs = GrouperBackend.attributes(g);
      for (Iterator attrIter = attrs.iterator(); attrIter.hasNext();) {
        GrouperAttribute attr = (GrouperAttribute) attrIter.next();
        g.attribute( attr.field(), attr.value() );
      }

      // FIXME Attach s to object?

      tx.commit();
    } catch (Exception e) {
      // TODO Rollback if load fails?  Unset this.exists?
      System.err.println(e);
      System.exit(1);
    }
  }

  // TODO
  protected static List stems(GrouperSession s, String stem) {
    GrouperBackend._init();
    List stems = new ArrayList();
    try {
      Query q = session.createQuery(
        "SELECT FROM grouper_attributes " +
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
  
  /**
   * Valid {@link GrouperTypeDef} items.
   *
   * @return List of group type definitions.
   */
  protected static List groupTypeDefs() {
    GrouperBackend._init();
    List typeDefs = new ArrayList();
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
    return typeDefs;
  }

  /**
   * Valid {@link GrouperType} items.
   *
   * @return List of group types.
   */
  protected static List groupTypes() {
    GrouperBackend._init();
    List types = new ArrayList();
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
    return types;
  }

  /**
   * Query a group's schema.
   *
   * @param g Group object
   * @return List of a group's schema
   */
  protected static List schemas(GrouperGroup g) {
    GrouperBackend._init();
    List schemas = new ArrayList();
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
    return schemas;
  }

  /**
   * Query for a single {@link GrouperMember}.
   *
   * @return  {@link GrouperMember} object or null.
   */
  protected static GrouperMember member(String id, String typeID) {
    GrouperBackend._init();
    GrouperMember m = null;
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
    return m;
  }

  protected static GrouperGroup group(GrouperSession s, 
                                   String stem,
                                   String descriptor) 
  {
    GrouperBackend._init();

    // TODO Please.  Make this better.  Please, please, please.
    //      For whatever reason, SQL and quality code are evading
    //      me this week.
    List descriptors = GrouperBackend.descriptors(s, descriptor);
    if (descriptors.size() > 0) {
      // We found one or more potential descriptors.  Now look
      // for matching stems.
      List stems = GrouperBackend.stems(s, stem);
      if (stems.size() > 0) {
        // We have potential stems and potential descriptors.
        // Now see if we have the *right* stem and the *right*
        // descriptor.
        for (Iterator iterDescs = descriptors.iterator(); iterDescs.hasNext();) {
          GrouperAttribute possDesc = (GrouperAttribute) iterDescs.next();
          for (Iterator iterStem = stems.iterator(); iterStem.hasNext();) {
            GrouperAttribute possStem = (GrouperAttribute) iterStem.next();
            if (
                descriptor.equals( possDesc.value() )   &&
                stem.equals( possStem.value() )         &&
                possDesc.key().equals( possStem.key() )
               )
            {
              // We have found an appropriate stem and descriptor
              // with matching keys.  We exist!

              // Now query for the group with the appropriate key and
              // return it.
              try {
                Query q = session.createQuery(
                  "SELECT ALL FROM GROUPER_GROUP " +
                  "IN CLASS edu.internet2.middleware.grouper.GrouperGroup " +
                  "WHERE "                          +
                  "groupKey='" + possDesc.key()     + "'"
                );
                if (q.list().size() == 1) {
                  return (GrouperGroup) q.list().get(0);
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
    return new GrouperGroup();
  }

  /*
   * PRIVATE INSTANCE METHODS
   */


  /*
   * Initialize static Hibernate session.
   */
  private static void _init() {
    if (cfg == null) {
      try {
      cfg = new Configuration()
        .addFile("conf/Grouper.hbm.xml");
      } catch (Exception e) {
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
    if (session == null) {
      try {
        session = factory.openSession();
      } catch (Exception e) {
        System.err.println(e);
        System.exit(1);
      }
    }
  }

}
 
