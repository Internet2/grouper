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
import  java.util.*;
import  org.apache.commons.lang.builder.ToStringBuilder;


/** 
 * Class representing a {@link Grouper} group.
 * <p />
 *
 * @author  blair christensen.
 * @version $Id: GrouperGroup.java,v 1.107 2004-12-03 03:58:17 blair Exp $
 */
public class GrouperGroup {

  /*
   * PRIVATE INSTANCE VARIABLES
   */
  private String id;
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
   * @param   extension   Extension to assign to group.
   * @return  A {@link GrouperGroup} object.
   */ 
  public static GrouperGroup create(
                                    GrouperSession s, String stem, 
                                    String extension
                                   )
  {
    return GrouperGroup._create(s, stem, extension, Grouper.DEF_GROUP_TYPE);
  }

  /**
   * Class method to create a group.
   *
   * @param   s           Session to create the group within.
   * @param   stem        Stem to create the group within.
   * @param   extension   Extension to assign to group.
   * @param   type        Type of group to create.
   * @return  A {@link GrouperGroup} object.
   */ 
  public static GrouperGroup create(
                                    GrouperSession s, String stem, 
                                    String extension, String type
                                   )
  {
    return GrouperGroup._create(s, stem, extension, type);
  }

  /** 
   * Class method to delete a {@link GrouperGroup}.
   * <p />
   * TODO Version that takes a stem and extension?  And type?
   *
   * @param   s   Session to delete the group within.
   * @param   g   Group to delete.
   * @return  Boolean true if group was deleted, false otherwise.
   */
  public static boolean delete(GrouperSession s, GrouperGroup g) {
    return GrouperBackend.groupDelete(s, g);
  }

  /**
   * Class method to retrieve a group from the groups registry.
   * <p />
   *
   * @param   s           Session to load the group within.
   * @param   stem        Stem of the group to load.
   * @param   extension   Extension of the group to load.
   * @return  A {@link GrouperGroup} object.
   */
  public static GrouperGroup lookup(
                                    GrouperSession s, 
                                    String stem, String extension
                                   )
  {
    return GrouperGroup._lookupByStemExtn(
                                         s, stem, extension, 
                                         Grouper.DEF_GROUP_TYPE
                                        );
  }

  /**
   * TODO
   */
  protected static GrouperGroup lookupByKey(
                                            GrouperSession s, String key, 
                                            String type
                                           ) 
  {
    return GrouperGroup._lookupByKey(s, key, type);
  }

  /**
   * Class method to retrieve a group from the groups registry.
   * <p />
   *
   * @param   s           Session to load the group within.
   * @param   stem        Stem of the group to load.
   * @param   extension   Extension of the group to load.
   * @param   type        Type of group to load.
   * @return  A {@link GrouperGroup} object.
   */
  public static GrouperGroup lookup(
                                    GrouperSession s, String stem, 
                                    String extension, String type
                                   )
  {
    return GrouperGroup._lookupByStemExtn(s, stem, extension, type);
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

  // TODO
  public String createSource() {
    return this.getCreateSource();
  }
  // TODO
  public String createSubject() {
    // TODO Return `Subject' object?
    return this.getCreateSubject();
  }
  // TODO
  public String createTime() {
    // TODO Return date object?
    return this.getCreateTime();
  }
  // TODO
  public String modifySource() {
    return this.getModifySource();
  }
  // TODO
  public String modifySubject() {
    // TODO Return `Subject' object?
    return this.getModifySubject();
  }
  // TODO
  public String modifyTime() {
    // TODO Return date object?
    return this.getModifyTime();
  }

  /**
   * Set a group attribute.
   * 
   * @param   attribute Attribute to set.
   * @param   value     Value of attribute.
   * @return  Boolean true if attribute added successfully, false
   *   otherwise.
   */
  public boolean attribute(String attribute, String value) {
    boolean rv = false;
    // Attempt to validate whether the attribute is allowed
    if (this._validateAttribute(attribute)) {
      // Setup the attribute, add it to the stash.
      // TODO Validate?
      GrouperAttribute cur = (GrouperAttribute) attributes.get(attribute);
      if (value == null) {
        // Delete an existing attribute
        // FIXME Implement...  this._attrDel(attribute); ???
      } else             {
        // Add a new attribute value
        // FIXME Update modify* opattrs
        GrouperAttribute attr = GrouperBackend.attrAdd(this.key, attribute, value);
        if (
            (attr != null)                                      &&
            (this._attrAdd(attribute, attr))                    //&&
            //(GrouperBackend.groupUpdate(this.grprSession, this))   
           )
        {
          rv = true;
        } else {
          // Revert changes
          // FIXME Revert opattr changes
          if (!this._attrAdd(attribute, cur)) {
            Grouper.LOGGER.warn("Unable to revert failed attribute change!");
            System.exit(1);
          }
          rv = false;
        }
      } 
    }
    return rv;
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
   * Return group ID.
   * <p />
   *
   * @return Group ID of the {@link GrouperGroup}
   */
  public String id() {
    return this.getGroupID();
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
    return this._listAddVal(s, m, Grouper.DEF_LIST_TYPE);
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
    return this._listAddVal(s, m, list);
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
    return this._listDelVal(s, m, Grouper.DEF_LIST_TYPE);
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
    return this._listDelVal(s, m, list);
  }

  /**
   * Return a string representation of the {@link GrouperSchema}
   * object.
   * <p />
   * @return String representation of the object.
   */
  public String toString() {
    return new ToStringBuilder(this)                            .
      append("type"     , this.type()                         ) .
      append("id"       , this.getGroupID()                   ) .
      append("stem"     , this.attribute("stem").value()      ) .
      append("extension", this.attribute("extension").value() ) .
      toString();
  }


  /*
   * PROTECTED INSTANCE METHODS
   */

  /**
   * Return group key.
   * <p >
   * FIXME Can I eventually make this private?
   *
   * @return Group key of the {@link GrouperGroup}
   */
  protected String key() {
    return this.getGroupKey();
  }


  /*
   * PRIVATE CLASS METHODS
   */

  /*
   * Retrieve a group from the groups registry
   */
  private static GrouperGroup _lookupByKey(
                                           GrouperSession s, String key, 
                                           String type
                                          ) 
  {
    GrouperGroup g = GrouperBackend.groupLookupByKey(key);
    if (g != null) {
      // Attach session
      g.grprSession = s;
      // Attach type  
      // FIXME Grr....
      g.type = type;
    }
    return g;
  }

  private static GrouperGroup _lookupByStemExtn(
                                                GrouperSession s, String stem, 
                                                String extension, String type
                                               )
  {
    GrouperGroup g = GrouperBackend.groupLookup(s, stem, extension, type);
    if (g != null) {
      // Attach session
      g.grprSession = s;
      // Attach type  
      // FIXME Grr....
      g.type = type;
    }
    return g;
  }

  /*
   * Return the number of seconds since the epoch.
   */
  private static String _now() {
    // TODO Do I want to store in non-epoch format?
    java.util.Date  now = new java.util.Date();
    return Long.toString(now.getTime());
  }

  /*
   * PRIVATE INSTANCE METHODS
   */

  /*
   * Add an attribute persistently
   */
  private boolean _attrAdd(String attribute, GrouperAttribute attr) {
    boolean rv = false;
    if (attr != null) {
      attributes.put(attribute, attr);
      rv = true;
    } else {
      Grouper.LOGGER.warn("Unable to add attribute " + attribute);
    }
    return rv;
  }

  /*
   * Add an attribute
   */
  private void _attrAdd(String attribute, String value) {
    GrouperAttribute attr = new GrouperAttribute(
                              this.key, attribute, value
                            );
    if (attr != null) {
      attributes.put(attribute, attr);
    } else {
      Grouper.LOGGER.warn("Unable to add attribute " +
                          attribute + "=" + value);
    }
  }

  /*
   * Delete an attribute
   */
  private void _attrDel(String attribute) {
    GrouperAttribute attr = null;
    if (attr != null) {
      attributes.remove(attribute);
    } else {
      Grouper.LOGGER.warn("Unable to remove attribute " + attribute);
    }
  }

  /*
   * Initialize aspects of the group before creating it.
   *
   * @param   s           Session to create the group within.
   * @param   stem        Stem of the group to be created.
   * @param   extension   Extension of group to be created.
   * @param   type        Type of group to be created.
   * @return  A {@link GrouperGroup} object.
   */
  private static GrouperGroup _create(
                                      GrouperSession s, String stem, 
                                      String extension, String type
                                     )
  {
    // TODO Can I move all|most of this to GrouperBackend?
    GrouperGroup g = new GrouperGroup();

    // Attach session
    g.grprSession  = s;

    // Generate the UUIDs
    g.setGroupKey( GrouperBackend.uuid() );
    g.setGroupID(  GrouperBackend.uuid() );

    g._attrAdd("stem", stem);
    g._attrAdd("extension", extension);
    g.type = type;

    // Set some of the operational attributes
    /*
     * TODO Most, if not all, of the operational attributes should be
     *      handled by Hibernate interceptors.  A task for another day.
     */
    g.setCreateTime(    GrouperGroup._now() );
    g.setCreateSubject( s.subject().getId() );

    // Verify that we have everything we need to create a group
    // and that this subject is privileged to create this group.
    if (g._validateCreate()) {
      // And now attempt to add the group to the store
      GrouperBackend.groupAdd(s, g);
    } else {
      // TODO Log
      g = null;
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
    this.key            = null;
    this.grprSession    = null;
    this.modifyTime     = null;
    this.modifySubject  = null;
    this.modifySource   = null;
    this.type           = null; // FIXME Is this right?
  }

  /*
   * Add list value and update modify* attributes.
   */
  private boolean _listAddVal(GrouperSession s, GrouperMember m, String list) {
    boolean rv = false;
    // Set some of the operational attributes
    /*
     * TODO Most, if not all, of the operational attributes should be
     *      handled by Hibernate interceptors.  A task for another day.
     */
    String curModTime = this.getModifyTime();
    String curModSubj = this.getModifySubject();
    this.setModifyTime( GrouperGroup._now() );
    this.setModifySubject( s.subject().getId() );
    if (GrouperBackend.listAddVal(s, this, m, list) == true) {
      rv = true;
    } else {
      // Revert changes
      this.setModifyTime(curModTime);
      this.setModifySubject(curModSubj);
    }
    return rv;
  }

  /*
   * Delete list value and update modify* attributes.
   */
  private boolean _listDelVal(GrouperSession s, GrouperMember m, String list) {
    boolean rv = false;
    // Set some of the operational attributes
    /*
     * TODO Most, if not all, of the operational attributes should be
     *      handled by Hibernate interceptors.  A task for another day.
     */
    String curModTime = this.getModifyTime();
    String curModSubj = this.getModifySubject();
    this.setModifyTime( GrouperGroup._now() );
    this.setModifySubject( s.subject().getId() );
    if (GrouperBackend.listDelVal(s, this, m, list) == true) {
      rv = true;
    } else {
      // Revert changes
      this.setModifyTime(curModTime);
      this.setModifySubject(curModSubj);
    }
    return rv;
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
        // And an extension?
        (attributes.containsKey("extension"))      && 
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

  private String getGroupID() {
    return this.id;
  }

  private void setGroupID(String id) {
    this.id = id;
  }

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

