/* 
 * Copyright (C) 2004 Internet2
 * Copyright (C) 2004 The University Of Chicago
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
 * @version $Id: GrouperGroup.java,v 1.38 2004-09-19 03:10:41 blair Exp $
 */
public class GrouperGroup {

  // Operational attributes and information
  private String groupKey;
  private String groupType;
  // TODO Stuff into a map?
  private String createTime;
  private String createSubject;
  private String createSource;
  private String modifyTime;
  private String modifySubject;
  private String modifySource;
  private String comment;

  // Grouper attributes (fields)
  private Map  attributes;

  // Grouper Session
  private GrouperSession grprSession;
  // Hibernate Session
  private Session        session;

  // Does the group exist?
  private boolean  exists;

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
    groupType     = null; // TODO Don't hardcode this
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
    GrouperAttribute attr = new GrouperAttribute();

    // Attempt to validate whether the attribute is allowed
    if (this._validateAttribute(attribute)) {
      // Setup the attribute, add it to the stash, and then attempt to
      // load the hibernated group if appropriate.
      attr.set(this.groupKey, attribute, value);
      attributes.put(attribute, attr);
      this._autoload();
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
  public boolean create() {
    // FIXME Damn this is ugly.

    // Set some of the operational attributes
    // TODO Most, if not all, of the operational attributes should be
    //      handled by Hibernate interceptors.  A task for another day.
    java.util.Date now = new java.util.Date();
    this.setCreateTime( Long.toString(now.getTime()) );
    this.setCreateSubject( this.grprSession.whoAmI() );

    // Verify that we have everything we need to create a group
    // and that this subject is privileged to create this group.
    if (this._validateCreate()) {
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
        for (Iterator iter = attributes.keySet().iterator(); iter.hasNext();) {
          GrouperAttribute attr = (GrouperAttribute) attributes.get( iter.next() );
          attr.set(this.groupKey, attr.field(), attr.value());
          session.save(attr);
        }

        // And make the creator a member of the "admins" list
        GrouperMembership mship = new GrouperMembership(); 
        // FIXME No, no, no.  
        mship.set(this.groupKey, "admins", this.grprSession.whoAmI(), true);
        session.save(mship);

        t.commit();
        return true;
      } catch (Exception e) {
        // TODO We probably need a rollback in here in case of failure
        //      above.
        System.err.println(e);
        System.exit(1);
      }
    } 
    System.err.println("Invalid group type: " + this.groupType);
    return false;
  }

  /**
   * Does this {@link Grouper} group exist?
   *
   * @return Boolean true if the group exists, false otherwise.
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
   * An ugly hack to attempt to autoload an object from the
   * persistent store.  If we know the `stem' and the 
   * `descriptor' and the group is not currently known to exist,
   * attempt to autoload it.
   */
  private void _autoload() {
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
   * Validate whether an attribute is valid for the current group type.
   *
   * @return Boolean true if attribute is valid for type or we are
   * unable to valid the attribute at this type, false otherwise.
   */
  private boolean _validateAttribute(String attribute) {
    boolean rv = false;
    if (this.groupType != null) { // FIXME I can do better than this.
      // We have a group type.  Now what?
      if (grprSession.groupField(this.groupType, attribute) == true) {
        // Our attribute passes muster.
        rv = true;
      }
    } else {
      // We don't know the group type so we can't validate.  Shrug our
      // shoulders and say "good enough" for now.
      rv = true;
    }
    return rv;
  }

  /*
   * Validate whether all attributes are valid for the current group
   * type.
   *
   * @return Boolean true if the attributes are valid for the group
   * type, false otherwise.
   */
  private boolean _validateAttributes() {
    for (Iterator iter = attributes.keySet().iterator(); iter.hasNext();) {
      GrouperAttribute attr = (GrouperAttribute) attributes.get( iter.next() );
      // TODO I should (possibly) revalidate the attributes due to
      //      lack of a group type -- or a changed group type.  Add
      //      some sort of dirty flag to trigger|!trigger this from
      //      occurring.
      if ( !this._validateAttribute( attr.field()) ) {
        return false;
      }
    }
    return true;
  }
 
  /*
   * Validate whether a group can be created.
   *
   * @return Boolean true if the group is valid to be created,
   * false otherwise.
   */
  private boolean _validateCreate() {
    if (
        // Do we have a valid group type?
        (grprSession.groupType(this.groupType) == true) &&
        // And a stem?
        (attributes.containsKey("stem")) &&
        // And a descriptor?
        (attributes.containsKey("descriptor")) && 
        // And do the stem and descriptor already exist?
        (this.exist() == false) && 
        // And are the group attributes valid?
        (this._validateAttributes()) 
        // TODO Member Object for the admin of the group
        // TODO CREATE priv for stem
       )
    {
      return true;
    }
    return false;
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

