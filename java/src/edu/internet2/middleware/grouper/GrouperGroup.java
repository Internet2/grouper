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
 * Class representing a {@link Grouper} group.
 *
 * @author  blair christensen.
 * @version $Id: GrouperGroup.java,v 1.30 2004-08-27 02:33:54 blair Exp $
 */
public class GrouperGroup {

  private Session         session;

  private String groupKey;
  private int groupType;
  private String createTime;
  private String createSubject;
  private String createSource;
  private String modifyTime;
  private String modifySubject;
  private String modifySource;
  private String comment;

  private GrouperSession  intSess;
  private String          name;
  private String          groupID   = null;
  private boolean         exists    = false;
  private Map             attributes;

  /**
   * TODO 
   *
   * Create a new object that represents a single {@link Grouper}
   * group. 
   * <p>
   * <ul>
   *  <li>Caches the group name.</li>
   *  <li>Checks and caches whether group exists.</li>
   *  <li>If group exists, the privileges of the current subject   
   *      on this group will be cached.</li>
   * </ul>
   * 
   * @param   s         Session context.
   * @param   name Name of group.
   */
  public GrouperGroup() {
    session       = null;

    // TODO Merge with 'attributes'?
    groupKey      = null;
    groupType     = 1;    // TODO Don't hardcode this
    createTime    = null;
    createSubject = null;
    createSource  = null;
    modifyTime    = null;
    modifySubject = null;
    modifySource  = null;
    comment       = null;

    attributes    = new HashMap();
    intSess       = null;
    name          = null;
  }

  public String toString() {
    return this.getClass()        + ":" +
           this.groupKey          + ":" + 
           attributes.get("stem") + ":" +
           attributes.get("descriptor");
  }

  public void session(GrouperSession s) {
    this.intSess = s;
    this.session = this.intSess.session();
  }

  public GrouperSession session() {
    // TODO Return an exception if !defined?
    return this.intSess;
  }

  public void attribute(String attribute, String value) {
    // TODO Attribute validation
    /* 
     * We save the transformation into a GrouperAttribute object until
     * later as we need to have a valid groupKey.  And yes, this 
     * can be improved upon.
     */
    attributes.put(attribute, value);
  }

  public String attribute(String attribute) {
    return (String) attributes.get(attribute);
  }

  public boolean exist() {
    // SELECT * FROM grouper_attributes WHERE name=$name
    // XXX The above is no longer correct.

    if (this.exists == true) {
      return true;
    } else {
      if (attributes.containsKey("stem") && 
          attributes.containsKey("descriptor")) 
      {
        String stem = (String) attributes.get("stem");
        String desc = (String) attributes.get("descriptor");
        // TODO Please.  Make this better.  Please, please, please.
        try {
          String query = "SELECT FROM grouper_attributes " +
                         "IN CLASS edu.internet2.middleware.grouper.GrouperAttribute " +
                         "WHERE " +
                         "groupField='descriptor' " + 
                         "AND " +
                         "groupFieldValue='" + desc + "'";
          List descs = this.session.find(query);
          if (descs.size() > 0) {
            // We found one or more potential descriptors.  Now look
            // for matching stems.
            query = "SELECT FROM grouper_attributes " +
                    "IN CLASS edu.internet2.middleware.grouper.GrouperAttribute " +
                    "WHERE " +
                    "groupField='stem' " + 
                    "AND " +
                    "groupFieldValue='" + stem + "'";
            List stems = this.session.find(query);
  
            if (stems.size() > 0) {
              // We have potential stems and potential descriptors.
              // Now see if we have the *right* stem and the *right*
              // descriptor.
              for (Iterator iterDesc = descs.iterator(); iterDesc.hasNext();) {
                GrouperAttribute possDesc = (GrouperAttribute) iterDesc.next();
                for (Iterator iterStem = stems.iterator(); iterStem.hasNext();) {
                  GrouperAttribute possStem = (GrouperAttribute) iterStem.next();
                  if (desc.equals( possDesc.value() ) &&
                      stem.equals( possStem.value() ) &&
                      possDesc.key().equals( possStem.key() ))
                  {
                    // We have found an appropriate stem and descriptor
                    // with matching keys.  We exist!
                    this.exists = true;

                    // Set groupKey
                    this.groupKey = possDesc.key();

                    // And not acknowledge our existence
                    return this.exists;
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

  public void create() {
    // FIXME Damn this is ugly.

    // Set some of the operational attributes
    java.util.Date now = new java.util.Date();
    this.setCreateTime( Long.toString(now.getTime()) );
    this.setCreateSubject( this.intSess.whoAmI() );

    // And now attempt to add the group to the store
    try {
      Transaction t = session.beginTransaction();
      org.doomdark.uuid.UUID uuid = UUIDGenerator.getInstance().generateRandomBasedUUID();
      this.groupKey = uuid.toString();

      // The Group object
      session.save(this);

      // Its schema
      GrouperSchema schema = new GrouperSchema();
      schema.set(this.groupKey, this.groupType);
      session.save(schema);

      // And its attributes
      Iterator iter = attributes.keySet().iterator();
      while (iter.hasNext()) {
        GrouperAttribute attr = new GrouperAttribute();
        String key = (String) iter.next();
        attr.set(this.groupKey, key, (String) attributes.get(key));
        session.save(attr);
      }

      // And make the creator a member of the "admins" list
      GrouperMembership mship = new GrouperMembership(); 
      mship.set(this.groupKey, "admins", this.intSess.whoAmI(), true);
      session.save(mship);

      t.commit();
    } catch (Exception e) {
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

