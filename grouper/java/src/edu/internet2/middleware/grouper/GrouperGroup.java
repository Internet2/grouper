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
import  java.util.*;


/** 
 * {@link Grouper} group class.
 *
 * @author  blair christensen.
 * @version $Id: GrouperGroup.java,v 1.84 2004-11-23 19:28:47 blair Exp $
 */
public class GrouperGroup {

  /*
   * PRIVATE INSTANCE VARIABLES
   */
  private String key;
  private String type;
  // Operational attributes and information
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
  private GrouperSession  grprSession;
  // Does the group exist?
  private boolean  exists;


  /*
   * CONSTRUCTORS
   */

  /**
   * Create a new object representing a {@link Grouper} group.
   * <p>
   * TODO Document further
   */
  public GrouperGroup() {
    this._init();
  }


  /*
   * PUBLIC CLASS METHODS
   */

  /**
   * Class method to create a group.
   *
   * @param   s           Session to create the group within.
   * @param   stem        Stem to create the group within.
   * @param   descriptor  Descriptor to assign to group.
   * @return  A {@link GrouperGroup} object.
   */ 
  public static GrouperGroup create(
                                    GrouperSession s, String stem, 
                                    String descriptor
                                   )
  {
    return GrouperGroup._create(s, stem, descriptor, Grouper.DEF_GROUP_TYPE);
  }

  /**
   * Class method to create a group.
   *
   * @param   s           Session to create the group within.
   * @param   stem        Stem to create the group within.
   * @param   descriptor  Descriptor to assign to group.
   * @param   type        Type of group to create.
   * @return  A {@link GrouperGroup} object.
   */ 
  public static GrouperGroup create(
                                    GrouperSession s, String stem, 
                                    String descriptor, String type
                                   )
  {
    return GrouperGroup._create(s, stem, descriptor, type);
  }

  /**
   * Class method to retrieve a group from the groups registry.
   * <p />
   *
   * @param   s           Session to load the group within.
   * @param   stem        Stem of the group to load.
   * @param   descriptor  Descriptor of the group to load.
   * @return  A {@link GrouperGroup} object.
   */
  public static GrouperGroup load(
                                  GrouperSession s, 
                                  String stem, String descriptor
                                 )
  {
    return GrouperGroup._load(s, stem, descriptor, Grouper.DEF_GROUP_TYPE);
  }

  /**
   * Class method to retrieve a group from the groups registry.
   * <p />
   *
   * @param   s           Session to load the group within.
   * @param   stem        Stem of the group to load.
   * @param   descriptor  Descriptor of the group to load.
   * @param   type        Type of group to load.
   * @return  A {@link GrouperGroup} object.
   */
  public static GrouperGroup load(
                                  GrouperSession s, String stem, 
                                  String descriptor, String type
                                 )
  {
    return GrouperGroup._load(s, stem, descriptor, type);
  }


  /*
   * PUBLIC INSTANCE METHODS
   */

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
   * Set a group attribute.
   * 
   * @param attribute Attribute to set.
   * @param value     Value of attribute.
   */
  public void attribute(String attribute, String value) {
    GrouperAttribute attr = new GrouperAttribute();

    // Attempt to validate whether the attribute is allowed
    if (this._validateAttribute(attribute)) {
      // Setup the attribute, add it to the stash.
      // TODO Require a valid (?) key?
      attr.set(this.key, attribute, value);
      attributes.put(attribute, attr);
    }
  }

  /** 
   * Get all group attributes.
   *
   * @return  A map of all group attributes.
   */
  public Map attributes() {
    return this.attributes;
  }

  /**
   * Does this {@link Grouper} group exist?
   *
   * @return Boolean true if the group exists, false otherwise.
   */
  public boolean exists() {
    if (this.exists == true) {
      // We are already marked as existing.  Assume that our status
      // hasn't changed.
      return this.exists;
    } else {
      // Otherwise attempt to find and load the group from the
      // persistent store.
      if (this.attributes.containsKey("stem")) {
        // We need a stem
        if (this.attributes.containsKey("descriptor")) {
          // And a descriptor
          if (this.grprSession != null) {
            /* TODO This method, in particular this check, is proving
             *      to be nothing but trouble.  At the least call out
             *      to various GB methods to determine existence.
             */
            // And a session to load a group
            // FIXME Provide a method of confirming a group's existence
            //       that doesn't rely upon loading a group and checking for
            //       the presence of a `key'.
            GrouperGroup g = GrouperGroup.load(
                                               this.grprSession,
                                               this.attribute("stem").value(),
                                               this.attribute("descriptor").value()
                                              );
            // Does the returned GrouperGroup object contain a group
            // key?  If so, the group is considered to exist.
            //if (g.key() != null) {
            if ( ( g != null) && ( g.key() != null ) ) {
              this.exists = true;
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  /**
   * Return group's unique key (UUID).
   * <p>
   * FIXME Do I really want to this to be public information?  Scale
   *       back if at all possible.
   *
   * @return Group key
   */
  public String key() {
    return this.getGroupKey();
  }

  /**
   * Return group's type.
   *
   * @return Group type
   */
  public String type() {
    return this.type;
  }

  /**
   * Return list values of the default list type for this 
   * {@link GrouperGroup}.
   * <p />
   *
   * @param   s     Return list data within this session context.
   * @return  List of effective {@link GrouperList} objects.
   */
  public List listVals(GrouperSession s) {
    return GrouperBackend.listVals(s, this, Grouper.DEF_LIST_TYPE);
  }

  /**
   * Return list values of the specified type for this
   * {@link GrouperGroup}.
   * <p />
   *
   * @param   s     Return list data within this session context.
   * @param   list  Return this list type.
   * @return  List of effective {@link GrouperList} objects.
   */
  public List listVals(GrouperSession s, String list) {
    return GrouperBackend.listVals(s, this, list);
  }

  /**
   * Return effective list values of the default list type for this 
   * {@link GrouperGroup}.
   * <p />
   *
   * @param   s     Return list data within this session context.
   * @return  List of effective {@link GrouperList} objects.
   */
  public List listEffVals(GrouperSession s) {
    return GrouperBackend.listEffVals(s, this, Grouper.DEF_LIST_TYPE);
  }

  /**
   * Return effective list values of the specified type for this
   * {@link GrouperGroup}.
   * <p />
   *
   * @param   s     Return list data within this session context.
   * @param   list  Return this list type.
   * @return  List of effective {@link GrouperList} objects.
   */
  public List listEffVals(GrouperSession s, String list) {
    return GrouperBackend.listEffVals(s, this, list);
  }

  /**
   * Return immediate list values of the default list type for this 
   * {@link GrouperGroup}.
   * <p />
   *
   * @param   s     Return list data within this session context.
   * @return  List of effective {@link GrouperList} objects.
   */
  public List listImmVals(GrouperSession s) {
    return GrouperBackend.listImmVals(s, this, Grouper.DEF_LIST_TYPE);
  }

  /**
   * Return immediate list values of the specified type for this
   * {@link GrouperGroup}.
   * <p />
   *
   * @param   s     Return list data within this session context.
   * @param   list  Return this list type.
   * @return  List of effective {@link GrouperList} objects.
   */
  public List listImmVals(GrouperSession s, String list) {
    return GrouperBackend.listImmVals(s, this, list);
  }

  /**
   * Add a {@link GrouperMember} to the default list type.
   * <p />
   * TODO Test
   * TODO Make a variant that takes a GrouperGroup instead of a
   *      GrouperMember?
   *
   * @param   s     Add member within this session context.
   * @param   m     Add this member.
   * @return  Boolean true if successful, false otherwise.
   */
  public boolean listAddVal(GrouperSession s, GrouperMember m) {
    return GrouperBackend.listAddVal(s, this, m, Grouper.DEF_LIST_TYPE);
  }

  /**
   * Add a {@link GrouperMember} to the specified list.
   * <p />
   * TODO Test
   * TODO Make a variant that takes a GrouperGroup instead of a
   *      GrouperMember?
   *
   * @param   s     Add member within this session context.
   * @param   m     Add this member.
   * @param   list  Add member to this list.
   * @return  Boolean true if successful, false otherwise.
   */
  public boolean listAddVal(GrouperSession s, GrouperMember m, String list) {
    return GrouperBackend.listAddVal(s, this, m, list);
  }

  /**
   * Delete a {@link GrouperMember} from default list type.
   * <p />
   * TODO Test
   * TODO Make a variant that takes a GrouperGroup instead of a
   *      GrouperMember?
   *
   * @param   s     Delete member within this session context.
   * @param   m     Delete this member.
   * @return  Boolean true if successful, false otherwise.
   */
  public boolean listDelVal(GrouperSession s, GrouperMember m) {
    return GrouperBackend.listDelVal(s, this, m, Grouper.DEF_LIST_TYPE);
  }

  /**
   * Delete a {@link GrouperMember} from the specified list.
   * <p />
   * TODO Test
   * TODO Make a variant that takes a GrouperGroup instead of a
   *      GrouperMember?
   *
   * @param   s     Delete member within this session context.
   * @param   m     Delete this member.
   * @param   list  Delete member from this list.
   * @return  Boolean true if successful, false otherwise.
   */
  public boolean listDelVal(GrouperSession s, GrouperMember m, String list) {
    return GrouperBackend.listDelVal(s, this, m, list);
  }

  /**
   * Returns a string representation of the {@link GrouperGroup}
   * object.
   *
   * @return  A string representation of the object.
   */
  public String toString() {
    // TODO This should probably return UUID, not key
    // TODO Switch to toString builder...
    return this.getClass().getName()              + ":" +
           this.key                               + ":" + 
           this.attribute("stem").value()         + ":" +
           this.attribute("descriptor").value();
  }


  /*
   * PRIVATE CLASS METHODS
   */

  /*
   * Retrieve a group from the groups registry
   */
  private static GrouperGroup _load(
                                    GrouperSession s, String stem, 
                                    String descriptor, String type
                                   )
  {
    GrouperGroup g = GrouperBackend.groupLoad(s, stem, descriptor, type);
    // Attach session
    g.grprSession = s;
    // Attach type  
    // FIXME Grr....
    g.type = type;
    return g;
  }


  /*
   * PRIVATE INSTANCE METHODS
   */

  /*
   * Initialize aspects of the group before creating it.
   *
   * @param   s           Session to create the group within.
   * @param   stem        Stem of the group to be created.
   * @param   descriptor  Descriptor of group to be created.
   * @param   type        Type of group to be created.
   * @return  A {@link GrouperGroup} object.
   */
  private static GrouperGroup _create(
                                    GrouperSession s, String stem, 
                                    String descriptor, String type
                                   )
  {
    // TODO Can I move all|most of this to GrouperBackend?
    GrouperGroup g = new GrouperGroup();

    // Attach session
    g.grprSession  = s;

    // Generate the UUID (key)
    g.setGroupKey( GrouperBackend.uuid() );

    g.attribute("stem", stem);
    g.attribute("descriptor", descriptor);
    g.type = type;

    // Set some of the operational attributes
    /*
     * TODO Most, if not all, of the operational attributes should be
     *      handled by Hibernate interceptors.  A task for another day.
     */
    // TODO Is this in UTC?
    java.util.Date now = new java.util.Date();
    g.setCreateTime( Long.toString(now.getTime()) );
    g.setCreateSubject( s.subject().getId() );

    // Verify that we have everything we need to create a group
    // and that this subject is privileged to create this group.
    if (g._validateCreate()) {
      // And now attempt to add the group to the store
      GrouperBackend.groupAdd(s, g);
      g.exists = true;
    }
    return g;
  }

  /*
   * Initialize instance variables
   */
  private void _init() { 
    this.attributes     = new HashMap();
    this.comment        = null;
    this.createTime     = null;
    this.createSubject  = null;
    this.createSource   = null;
    this.exists         = false;
    this.key            = null;
    this.grprSession    = null;
    this.modifyTime     = null;
    this.modifySubject  = null;
    this.modifySource   = null;
    this.type           = null; // FIXME Is this right?
  }

  /*
   * Validate whether an attribute is valid for the current group type.
   *
   * @return Boolean true if attribute is valid for type or we are
   * unable to valid the attribute at this type, false otherwise.
   */
  private boolean _validateAttribute(String attribute) {
    boolean rv = false;
    if (this.type != null) { // FIXME I can do better than this.
      // We have a group type.  Now what?
      if (Grouper.groupField(this.type, attribute) == true) {
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
    Iterator iter = attributes.keySet().iterator();
    while (iter.hasNext()) {
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
        (Grouper.groupType(this.type) == true) &&
        // And a stem?
        (attributes.containsKey("stem"))            &&
        // And a descriptor?
        (attributes.containsKey("descriptor"))      && 
        // And do the stem and descriptor already exist?
        (this.exists() == false)                    && 
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
   * HIBERNATE
   */

  private String getGroupKey() {
    return this.key;
  }

  private void setGroupKey(String key) {
    this.key = key;
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

