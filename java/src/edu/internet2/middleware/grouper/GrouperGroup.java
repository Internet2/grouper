/* 
 * Copyright (C) 2004 TODO
 * All Rights Reserved. 
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted only as authorized by the Academic
 * Free License version 2.1.
 *
 * A copy of this license is available in the file LICENSE in the
 * top-level directory of the distribution or, alternatively, at
 * <http://www.opensource.org/licenses/afl-2.1.php>
 */

package edu.internet2.middleware.grouper;

import  java.sql.*;
import  java.util.*;
import  net.sf.hibernate.*;
import  org.doomdark.uuid.UUIDGenerator;


/** 
 * {@link Grouper} group class.
 *
 * @author  blair christensen.
 * @version $Id: GrouperGroup.java,v 1.33 2004-09-08 19:26:55 blair Exp $
 */
public class GrouperGroup {

  // Operational attributes and information
  private static String groupKey;
  private static int    groupType;
  // TODO Stuff into a map?
  private static String createTime;
  private static String createSubject;
  private static String createSource;
  private static String modifyTime;
  private static String modifySubject;
  private static String modifySource;
  private static String comment;

  // Grouper attributes (fields)
  private static Map  attributes;

  // Grouper Session
  private static GrouperSession grprSession;
  // Hibernate Session
  private static Session        session;

  // Does the group exist?
  private static boolean  exists;

  /**
   * Create a new object representing a single {@link Grouper} group.
   * <p>
   * TODO Document further
   */
  public GrouperGroup() {
    attributes    = new HashMap();
    comment       = null;
    createTime    = null;
    createSubject = null;
    createSource  = null;
    exists        = false;
    groupKey      = null;
    groupType     = 1;    // TODO Don't hardcode this
    grprSession   = null;
    modifyTime    = null;
    modifySubject = null;
    modifySource  = null;
    session       = null;
  }

  public String toString() {
    GrouperAttribute stem = (GrouperAttribute) attributes.get("stem");
    GrouperAttribute desc = (GrouperAttribute) attributes.get("descriptor");
    return this.getClass()  + ":" +
           this.groupKey    + ":" + 
           stem.value()     + ":" +
           desc.value(); 
  }

  /**
   * Set a group attribute.
   * 
   * @param attribute Attribute to set.
   * @param value     Value of attribute.
   */
  public void attribute(String attribute, String value) {
    // TODO Attribute validation
    GrouperAttribute attr = new GrouperAttribute();
    attr.set(this.groupKey, attribute, value);
    attributes.put(attribute, attr);

    // An ugly hack to attempt to autoload an object from the
    // persistent store.  If we know the `stem' and the 
    // `descriptor' and the group is not currently known to exist,
    // attempt to autoload it.
    if ( 
        (this.exists == false)         &&     // The group is not
                                              // marked as existing,
        attributes.containsKey("stem") &&     // But it has a stem...
        attributes.containsKey("descriptor")  // And a descriptor
       )
    {
      // Now run the exist() method.  If successful, load the 
      // hibernating group.
      if (this.exist() == true) {
        this._load(this.groupKey);
      }
    }
  }

  /**
   * Get a group attribute.
   *
   * @param   attribute The attribute to get.
   * @return  A {@link GrouperAttribute} object.
   */
  public GrouperAttribute attribute(String attribute) {
    return (GrouperAttribute) attributes.get(attribute);
  }

  /**
   * Create a {@link Grouper} group.
   */ 
  public void create() {
    // FIXME Damn this is ugly.

    // Set some of the operational attributes
    // TODO Most, if not all, of the operational attributes should be
    //      handled by Hibernate interceptors.  A task for another day.
    java.util.Date now = new java.util.Date();
    this.setCreateTime( Long.toString(now.getTime()) );
    this.setCreateSubject( this.grprSession.whoAmI() );

    // And now attempt to add the group to the store
    try {
      Transaction t = session.beginTransaction();

      // Generate the UUID (groupKey)
      org.doomdark.uuid.UUID uuid = UUIDGenerator.getInstance().generateRandomBasedUUID();
      this.groupKey = uuid.toString();

      // The Group object
      session.save(this);

      // The Group schema
      GrouperSchema schema = new GrouperSchema();
      schema.set(this.groupKey, this.groupType);
      session.save(schema);

      // The Group attributes
      Iterator iter = attributes.keySet().iterator();
      while (iter.hasNext()) {
        GrouperAttribute attr = (GrouperAttribute) attributes.get( iter.next() );
        attr.set(this.groupKey, attr.field(), attr.value());
        session.save(attr);
      }

      // And make the creator a member of the "admins" list
      GrouperMembership mship = new GrouperMembership(); 
      mship.set(this.groupKey, "admins", this.grprSession.whoAmI(), true);
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
   * Does this {@link Grouper} group exist?
   *
   * @return Boolean true if the group exists, otherwise false.
   */
  public boolean exist() {
    if (this.exists == true) {
      return true;
    } else {
      if (attributes.containsKey("stem") && 
          attributes.containsKey("descriptor")) 
      {
        GrouperAttribute stem = (GrouperAttribute) attributes.get("stem");
        GrouperAttribute desc = (GrouperAttribute) attributes.get("descriptor");
        // TODO Please.  Make this better.  Please, please, please.
        //      For whatever reason, SQL and quality code are evading
        //      me this week.
        try {
          String query = "SELECT FROM grouper_attributes " +
                         "IN CLASS edu.internet2.middleware.grouper.GrouperAttribute " +
                         "WHERE " +
                         "groupField='descriptor' " + 
                         "AND " +
                         "groupFieldValue='" + desc.value() + "'";
          List descs = this.session.find(query);
          if (descs.size() > 0) {
            // We found one or more potential descriptors.  Now look
            // for matching stems.
            query = "SELECT FROM grouper_attributes " +
                    "IN CLASS edu.internet2.middleware.grouper.GrouperAttribute " +
                    "WHERE " +
                    "groupField='stem' " + 
                    "AND " +
                    "groupFieldValue='" + stem.value() + "'";
            List stems = this.session.find(query);
  
            if (stems.size() > 0) {
              // We have potential stems and potential descriptors.
              // Now see if we have the *right* stem and the *right*
              // descriptor.
              for (Iterator iterDesc = descs.iterator(); iterDesc.hasNext();) {
                GrouperAttribute possDesc = (GrouperAttribute) iterDesc.next();
                for (Iterator iterStem = stems.iterator(); iterStem.hasNext();) {
                  GrouperAttribute possStem = (GrouperAttribute) iterStem.next();
                  if (desc.value().equals( possDesc.value() ) &&
                      stem.value().equals( possStem.value() ) &&
                      possDesc.key().equals( possStem.key() ))
                  {
                    // We have found an appropriate stem and descriptor
                    // with matching keys.  We exist!

                    // Set groupKey
                    this.groupKey = possDesc.key();
                    
                    // And now acknowledge our existence
                    return true;
                  }
                }
              }
            }
          } 
        } catch (Exception e) {
          System.err.println(e);
          System.exit(1); 
        }
      }
    }
    return false;
  }

  /** 
   * Attach a Grouper session to this object.
   */
  public void session(GrouperSession s) {
    this.grprSession = s;
    this.session = this.grprSession.session();
  }

  /**
   * Return the current Grouper session.
   *
   * @return Returns the Grouper session.
   */
  public GrouperSession session() {
    // TODO Return an exception if !defined?
    return this.grprSession;
  }

  /*
   * PUBLIC METHODS ABOVE, PRIVATE METHODS BELOW
   */

  /*
   * Load group from hibernated state.
   */
  private void _load(String key) {
    Transaction   tx  = null;
    try {
      // Attempt to load a stored group into the current object
      tx = session.beginTransaction();
      session.load(this, key);
      
      // Its schema
      String schemaQuery = "SELECT FROM grouper_schema " +
                           "IN CLASS " +
                           "edu.internet2.middleware.grouper.GrouperSchema " +
                           "WHERE groupKey='" + key + "'";
      List schemas = session.find(schemaQuery);
      if (schemas.size() == 1) {
        GrouperSchema schema = (GrouperSchema) schemas.get(0);
      } else {
        System.err.println("Found " + schemas.size() + 
                           " schema definitions.");
        System.exit(1);
      }

      // And its attributes
      String attrQuery = "SELECT FROM grouper_attributes " +
                         "IN CLASS " +
                         "edu.internet2.middleware.grouper.GrouperAttribute " +
                         "WHERE groupKey='" + key + "'";
      List attrs = session.find(attrQuery);
      for (Iterator attrIter = attrs.iterator(); attrIter.hasNext();) {
        GrouperAttribute attr = (GrouperAttribute) attrIter.next();
        attributes.put( attr.field(), attr );
      }

      tx.commit();

      // Mark that it exists
      this.exists = true;
    } catch (Exception e) {
      // TODO Rollback if load fails?  Unset this.exists?
      System.err.println(e);
      System.exit(1);
    }
  }

  /*
   * Below for Hibernate
   */

  private String getGroupKey() {
    return this.groupKey;
  }

  private void setGroupKey(String groupKey) {
    this.groupKey = groupKey;
  }

  private String getCreateTime() {
    return this.createTime;
  }
 
  private void setCreateTime(String createTime) {
    this.createTime = createTime;
  }
 
  private String getCreateSubject() {
    return this.createSubject;
  }
 
  private void setCreateSubject(String createSubject) {
    this.createSubject = createSubject;
  }
 
  private String getCreateSource() {
    return this.createSource;
  }
 
  private void setCreateSource(String createSource) {
    this.createSource = createSource;
  }
 
  private String getModifyTime() {
    return this.modifyTime;
  }
 
  private void setModifyTime(String modifyTime) {
    this.modifyTime = modifyTime;
  }
 
  private String getModifySubject() {
    return this.modifySubject;
  }
 
  private void setModifySubject(String modifySubject) {
    this.modifySubject = modifySubject;
  }
 
  private String getModifySource() {
    return this.modifySource;
  }
 
  private void setModifySource(String modifySource) {
    this.modifySource = modifySource;
  }

  private String getComment() {
    return this.comment;
  }

  private void setComment(String comment) {
    this.comment = comment;
  } 

}

