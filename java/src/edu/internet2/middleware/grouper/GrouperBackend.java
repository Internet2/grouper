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
 * @version $Id: GrouperBackend.java,v 1.11 2004-10-13 18:16:50 blair Exp $
 */
public class GrouperBackend {

  // Hibernate configuration
  private static Configuration   cfg;
  // Hibernate session factory
  private static SessionFactory  factory;
  // Hibernate session
  private static Session session;

  public GrouperBackend() {
    // Nothing
  }

  /**
   * Add {@link GrouperGroup} to backend store.
   *
   * @param s {@link GrouperSession}
   * @param g {@link GrouperGroup} to add
   */
  public static void addGroup(GrouperSession s, GrouperGroup g) {
    try {
      Transaction t = session.beginTransaction();

      // The Group object
      session.save(g);

      // FIXME 
      String gt = "base";
      // The Group schema
      GrouperSchema schema = new GrouperSchema(g.groupKey(), gt);
      session.save(schema);

      // The Group attributes
      Map attributes = g.attributes();
      for (Iterator iter = attributes.keySet().iterator(); iter.hasNext();) {
        GrouperAttribute attr = (GrouperAttribute) attributes.get( iter.next() );
        attr.set(g.groupKey(), attr.field(), attr.value());
        session.save(attr);
      }

      // And make the creator a member of the "admins" list
      GrouperMembership mship = new GrouperMembership(); 
      // FIXME No, no, no.  
      // BDC mship.set(m.groupKey, "admins", m.grprSession.subject().memberID(), true);
      // BDC session.save(mship);

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
  public static void addSession(GrouperSession s) {
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
  public static List attributes(GrouperGroup g) {
    GrouperBackend._init();
    List attributes = new ArrayList();
    try {
      Query q = session.createQuery(
        "SELECT FROM grouper_attributes " +
        "IN CLASS edu.internet2.middleware.grouper.GrouperAttribute " +
        "WHERE groupKey='" + g.groupKey() + "'"
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
  public static void cullSessions() {
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
  public static List groupFields() {
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
  public static List descriptors(GrouperSession s, String descriptor) {
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

  // TODO
  public static void loadGroup(GrouperSession s, GrouperGroup g, String key) {
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
  public static List namespaces(GrouperSession s, String namespace) {
    List namespaces = new ArrayList();
    try {
      Query q = session.createQuery(
        "SELECT FROM grouper_attributes " +
        "IN CLASS edu.internet2.middleware.grouper.GrouperAttribute " +
        "WHERE " +
        "groupField='namespace' " + 
        "AND " +
        "groupFieldValue='" + namespace + "'"
      );
      namespaces = q.list();
    } catch (Exception e) {
      System.err.println(e);
      System.exit(1);
    }
    return namespaces;
  }
  
  /**
   * Valid {@link GrouperTypeDef} items.
   *
   * @return List of group type definitions.
   */
  public static List groupTypeDefs() {
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
  public static List groupTypes() {
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
  public static List schemas(GrouperGroup g) {
    List schemas = new ArrayList();
    try {
      Query q = session.createQuery(
        "SELECT FROM grouper_schema " +
        "IN CLASS edu.internet2.middleware.grouper.GrouperSchema " +
        "WHERE groupKey='" + g.groupKey() + "'"
      );
      schemas = q.list();
    } catch (Exception e) {
      System.err.println(e);
      System.exit(1);
    }
    return schemas;
  }

  /**
   * Generate UUID.
   *
   * @return A UUID
   */
  public static String uuid() {
    //org.doomdark.uuid.UUID uuid = UUIDGenerator.getInstance().generateRandomBasedUUID();
    return UUIDGenerator.getInstance().generateRandomBasedUUID().toString();
  }

  /**
   * Query for a single {@link GrouperMember}.
   *
   * @return  {@link GrouperMember} object or null.
   */
  public static GrouperMember member(String id, String type) {
    GrouperBackend._init();
    GrouperMember m = null;
    try {
      Query q = session.createQuery(
        "SELECT ALL FROM GROUPER_MEMBER " +
        "IN CLASS edu.internet2.middleware.grouper.GrouperMember " +
        "WHERE "                          +
        "memberID='"    + id    + "' "    + 
        "AND "                            +
        "memberType='"  + type  + "'"
      );
      if (q.list().size() == 1) {
        // We only want *one* member.
        m = (GrouperMember) q.list().get(0);
      }
    } catch (Exception e) {
      System.err.println(e);
      System.exit(1);
    }
    return m;
  }

  public static GrouperGroup group(GrouperSession s, String namespace, String descriptor) {
    GrouperBackend._init();

    // TODO Please.  Make this better.  Please, please, please.
    //      For whatever reason, SQL and quality code are evading
    //      me this week.
    List descs = GrouperBackend.descriptors(s, descriptor);
    if (descs.size() > 0) {
      // We found one or more potential descriptors.  Now look
      // for matching namespaces.
      List namespaces = GrouperBackend.namespaces(s, namespace);
      if (namespaces.size() > 0) {
        // We have potential namespaces and potential descriptors.
        // Now see if we have the *right* namespace and the *right*
        // descriptor.
        for (Iterator iterDesc = descs.iterator(); iterDesc.hasNext();) {
          GrouperAttribute possDesc = (GrouperAttribute) iterDesc.next();
          for (Iterator iterNamespace = namespaces.iterator(); iterNamespace.hasNext();) {
            GrouperAttribute possNamespace = (GrouperAttribute) iterNamespace.next();
            if (
                descriptor.equals( possDesc.value() )   &&
                namespace.equals( possNamespace.value() )         &&
                possDesc.key().equals( possNamespace.key() )
               )
            {
              // We have found an appropriate namespace and descriptor
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
   * PUBLIC METHODS ABOVE, PRIVATE METHODS BELOW
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
 
