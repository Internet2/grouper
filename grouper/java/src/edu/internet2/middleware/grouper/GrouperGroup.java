/*
 * Copyright (C) 2004-2005 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004-2005 The University Of Chicago
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
import  java.util.*;
import  org.apache.commons.lang.builder.ToStringBuilder;


/** 
 * Class modeling a {@link Grouper} group.
 * <p />
 *
 * @author  blair christensen.
 * @version $Id: GrouperGroup.java,v 1.163 2005-03-07 17:18:22 blair Exp $
 */
public class GrouperGroup {

  /*
   * PRIVATE INSTANCE VARIABLES
   */
  private Map             attributes;
  private String          createTime;
  private String          createSubject;
  private String          createSource;
  private String          id;
  private boolean         initialized   = false; // FIXME UGLY HACK!
  private String          groupComment;
  private String          key;
  private String          modifyTime;
  private String          modifySubject;
  private String          modifySource;
  private GrouperSession  s;
  private String          type;


  /*
   * CONSTRUCTORS
   */
  /**
   * Null-argument constructor for Hibernate.
   */
  public GrouperGroup() {
    this._init();
  }


  /*
   * PUBLIC CLASS METHODS
   */

  /**
   * Create a group.
   * <p />
   * @param   s           Session to create the group within.
   * @param   stem        Stem to create the group within.
   * @param   extension   Extension to assign to the group.
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
   * Create a group.
   * <p />
   * @param   s           Session  to create the group within.
   * @param   stem        Stem to create the group within.
   * @param   extension   Extension to assign to the group.
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
   * Delete a group.
   * <p />
   * @param   s   Session to delete the group within.
   * @param   g   Group to delete.
   * @return  True if the group was deleted.
   */
  public static boolean delete(GrouperSession s, GrouperGroup g) {
    boolean rv = false;
    if (GrouperGroup._canDelete(s, g)) {
      rv = GrouperBackend.groupDelete(s, g);
      Grouper.log().groupDel(rv, s, g);
    }
    return rv;
  }

  /**
   * Format a {@link GrouperGroup} name.
   * <p />
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
      // TODO String delim = Grouper.config("hierarchy.delimiter");
      String delim = Grouper.HIER_DELIM;
      if (extn.indexOf(delim) != -1) {
        // FIXME Throw an exception?  And then test for failure?
        //       Or settle for ye olde null
        Grouper.log().event(
          "Extension `" + extn + "' contains delimiter `" + delim + "'"
        );
        name = null;
      } else {
        name = stem + delim + extn;
      }
    }
    return name;
  }

  /**
   * Retrieve a group by stem and extension.
   * <p />
   * @param   s           Session to load the group within.
   * @param   stem        Stem of the group to load.
   * @param   extension   Extension of the group to load.
   * @return  A {@link GrouperGroup} object.
   */
  public static GrouperGroup load(
                               GrouperSession s, 
                               String stem, String extension
                             )
  {
    return GrouperGroup._loadByStemExtn(
             s, stem, extension, Grouper.DEF_GROUP_TYPE
           );
  }

  /**
   * Retrieve a group by stem and extension.
   * <p />
   * @param   s           Session to load the group within.
   * @param   stem        Stem of the group to load.
   * @param   extension   Extension of the group to load.
   * @param   type        The type of group to load.
   * @return  A {@link GrouperGroup} object.
   */
  public static GrouperGroup load(
                               GrouperSession s, String stem, 
                               String extension, String type
                             )
  {
    return GrouperGroup._loadByStemExtn(s, stem, extension, type);
  }

  /**
   * Retrieve a group by public GUID.
   * <p />
   * @param   s           Session to load the group within.
   * @param   id          Group GUID.
   * @return  A {@link GrouperGroup} object.
   */
  public static GrouperGroup loadByID(
                               GrouperSession s, String id
                             )
  {
    return GrouperGroup._loadByID(
             s, id, Grouper.DEF_GROUP_TYPE
           );
  }

  /**
   * Retrieve a group by public GUID.
   * <p />
   * @param   s           Session to load the group within.
   * @param   id          Group GUID.
   * @param   type        The type of group to retrieve.
   * @return  A {@link GrouperGroup} object.
   */
  public static GrouperGroup loadByID(
                               GrouperSession s, String id, String type
                             )
  {
    return GrouperGroup._loadByID(s, id, type);
  }

  /**
   * Retrieve a group by name.
   * <p />
   * @param   s           Session to load the group within.
   * @param   name        Name of group.
   * @return  A {@link GrouperGroup} object.
   */
  public static GrouperGroup loadByName(
                               GrouperSession s, String name
                             )
  {
    return GrouperGroup._loadByName(
             s, name, Grouper.DEF_GROUP_TYPE
           );
  }

  /**
   * Retrieve a group by name.
   * <p />
   * @param   s           Session to load the group within.
   * @param   name        Name of group.
   * @param   type        The type of group to retrieve.
   * @return  A {@link GrouperGroup} object.
   */
  public static GrouperGroup loadByName(
                               GrouperSession s, String name, String type
                             )
  {
    return GrouperGroup._loadByName(s, name, type);
  }


  /*
   * PUBLIC INSTANCE METHODS
   */

  /**
   * Retrieve a group attribute.
   * <p />
   * @param   s         Retrieve attribute using this session.
   * @param   attribute The attribute to retrieve.
   * @return  A {@link GrouperAttribute} object.
   */
  public GrouperAttribute attribute(String attribute) {
    return (GrouperAttribute) attributes.get(attribute);
  }

  /**
   * Set the value of a group attribute.
   * <p />
   * If <i>value</i> is <i>null</i>, the attribute will be deleted.
   * 
   * @param   s           Set attribute using this session.
   * @param   attribute   Set this attribute.
   * @param   value       Set attribute to this value.
   * @return  True if the attribute was set.
   */
  public boolean attribute(String attribute, String value) {
    boolean rv = false;
    // Attempt to validate whether the attribute is allowed
    if (this._validateAttribute(attribute)) {
      // FIXME We don't handle renames yet -- if ever?
      // FIXME If I actually paid any attention to the contents
      //       of `grouper_field', I wouldn't need to do some of
      //       this...
      if ( 
          (this.initialized == true) && 
           (
            (attribute.equals("displayName")) ||
            (attribute.equals("name"))        ||
            (attribute.equals("stem"))        ||
            (attribute.equals("extension"))
           )
         ) 
      {
        Grouper.log().groupAttrNoMod(attribute);
      } else {
        // TODO Validate?
        GrouperAttribute cur = (GrouperAttribute) attributes.get(attribute);
        // For logging
        if        (value == null) {
          // Delete an existing attribute
          rv = this._attributeDelete(attribute);
        } else if (cur == null) {
          // Add a new attribute value
          rv = this._attributeAdd(attribute, value);
        } else {
          // Update attribute value
          rv = this._attributeUpdate(attribute, value);
        }
      }
    }
    return rv;
  }

  /** 
   * Retrieve all of this group's attributes.
   * <p />
   * @return  A map of {@link GrouperAttribute} objects.
   */
  public Map attributes() {
    return this.attributes;
  }

  /**
   * Retrieve the <i>createSource</i> operational attribute value.
   * <p  />
   * This attribute is not currently used.
   *
   * @return <i>createSource</i> value.
   */
  public String createSource() {
    return this.getCreateSource();
  }

  /**
   * Retrieve the <i>createSubject</i> operational attribute value.
   * <p  />
   * @return A {@link Subject} object.
   */
  public Subject createSubject() {
    return this._returnSubjectObject(this.getCreateSubject()); 
  }

  /**
   * Retrieve the <i>createTime</i> operational attribute value.
   * <p  />
   * @return <i>createTime</i> as a {@link Date} object.
   */
  public Date createTime() {
    // TODO Refactor out commonality with `modifyTime'
    Date d = null;
    String since = this.getCreateTime();
    if (since != null) {
      d = new Date(Long.parseLong(since));
    }
    return d;
  }

  /**
   * Retrieve group's public GUID.
   * <p />
   * @return Public GUID.
   */
  public String id() {
    return this.getGroupID();
  }

  /**
   * Retrieve group's type.
   * <p />
   * @return Type of group.
   */
  public String type() {
    return this.type;
  }

  /**
   * Retrieve list values of the default list type for this group.
   * <p />
   * @return  List of {@link GrouperList} objects.
   */
  public List listVals() {
    return _listVals(this, Grouper.DEF_LIST_TYPE);
  }

  /**
   * Retrieve list values of the specified type for this group.
   * <p />
   * @param   list  Return this list type.
   * @return  List of {@link GrouperList} objects.
   */
  public List listVals(String list) {
    return _listVals(this, list);
  }

  /**
   * Retrieve effective list values of the default list type for this
   * group.
   * <p />
   * @return  List of {@link GrouperList} objects.
   */
  public List listEffVals() {
    return _listEffVals(this, Grouper.DEF_LIST_TYPE);
  }

  /**
   * Retrieve effective list values of the specified type for this
   * group.
   * <p />
   * @param   list  Return this list type.
   * @return  List of {@link GrouperList} objects.
   */
  public List listEffVals(String list) {
    return _listEffVals(this, list);
  }

  /**
   * Retrieve immediate list values of the default list type for this
   * group.
   * <p />
   * @return  List of {@link GrouperList} objects.
   */
  public List listImmVals() {
    return _listImmVals(this, Grouper.DEF_LIST_TYPE);
  }

  /**
   * Retrieve immediate list values of the specified type for this
   * group.
   * <p />
   * @param   list  Return this list type.
   * @return  List of {@link GrouperList} objects.
   */
  public List listImmVals(String list) {
    return _listImmVals(this, list);
  }

  /**
   * Add a list value of the default list type.
   * <p />
   * @param   m     Add this member.
   * @return  True if the list value was added.
   */
  public boolean listAddVal(GrouperMember m) {
    /* 
     * TODO Add a variant that takes a GrouperGroup instead of a
     * GrouperMember?
     */
    boolean rv = false;
    if (GrouperGroup._canModListVal(this, Grouper.DEF_LIST_TYPE)) {
      rv = this._listAddVal(m, Grouper.DEF_LIST_TYPE);
      Grouper.log().groupListAdd(rv, this.s, this, m);
    }
    return rv;
  }

  /**
   * Add a list value of the specified list type.
   * <p />
   * @param   m     Add this member.
   * @param   list  Add member to this list type.
   * @return  True if the list value was added.
   */
  public boolean listAddVal(GrouperMember m, String list) {
    /* 
     * TODO Add a variant that takes a GrouperGroup instead of a
     * GrouperMember?
     */
    boolean rv = false;
    if (GrouperGroup._canModListVal(this, list)) {
      rv = this._listAddVal(m, list);
      Grouper.log().groupListAdd(rv, this.s, this, m);
    }
    return rv;
  }

  /**
   * Delete a list value of the default list type.
   * <p />
   * @param   m     Delete this member.
   * @return  True if the list value was deleted.
   */
  public boolean listDelVal(GrouperMember m) {
    /* 
     * TODO Add a variant that takes a GrouperGroup instead of a
     * GrouperMember?
     */
    boolean rv = false;
    // TODO Refactor into _listDelVal
    if (GrouperGroup._canModListVal(this, Grouper.DEF_LIST_TYPE)) {
      rv = this._listDelVal(m, Grouper.DEF_LIST_TYPE);
    }  
    Grouper.log().groupListDel(rv, this.s, this, m);
    return rv;
  }

  /**
   * Delete a list value of the specified list type.
   * <p />
   * @param   m     Delete this member.
   * @param   list  Delete member from this list type.
   * @return  True if the list value was deleted.
   */
  public boolean listDelVal(GrouperMember m, String list) {
    /* 
     * TODO Add a variant that takes a GrouperGroup instead of a
     * GrouperMember?
     */
    boolean rv = false;
    // TODO Refactor into _listDelVal
    if (GrouperGroup._canModListVal(this, list)) {
      rv = this._listDelVal(m, list);
    }  
    Grouper.log().groupListDel(rv, this.s, this, m);
    return rv;
  }

  /**
   * Retrieve the <i>modifySource</i> operational attribute value.
   * <p  />
   * This attribute is not currently used.
   *
   * @return <i>modifySource</i> value.
   */
  public String modifySource() {
    return this.getModifySource();
  }

  /**
   * Retrieve the <i>modifySubject</i> operational attribute value.
   * <p  />
   * @return A {@link Subject} object.
   */
  public Subject modifySubject() {
    return this._returnSubjectObject(this.getModifySubject()); 
  }

  /**
   * Retrieve the <i>modifyTime</i> operational attribute.
   * <p  />
   * @return <i>modifyTime</i> as a {@link Date} object.
   */
  public Date modifyTime() {
    // TODO Refactor out commonality with `createTime'
    Date d = null;
    String since = this.getModifyTime();
    if (since != null) {
      d = new Date(Long.parseLong(since));
    }
    return d;
  }

  /**
   * Retrieve the <i>name</i> attribute.
   * <p>
   * This is a convenience method.  The value can also be retrieved
   * using the <i>attribute()</i> method.
   *
   * @return  Name of group.
   */
  public String name() {
    return this.attribute("name").value();
  }

  /**
   * Return a string representation of this object.
   * <p />
   * @return String representation of this object.
   */
  public String toString() {
    // TODO GrouperAttribute stem = (GrouperAttribute) this.attributes.get("stem");
    // TODO GrouperAttribute extn = (GrouperAttribute) this.attributes.get("extn");
    return new ToStringBuilder(this)          .
      append("type"     , this.type()       ) .
      append("id"       , this.getGroupID() ) .
      // TODO append("stem"     , stemVal           ) .
      // TODO append("extension", extnVal           ) .
      toString();
  }


  /*
   * PROTECTED INSTANCE METHODS
   */

  // FIXME Does this method I can *really* clean up the public version?
  protected void attribute(String attr, GrouperAttribute value) {
    if (attr != null) {
      if (value != null) {
        attributes.put(attr, value);
      } else {
        attributes.remove(attr);
      }
    }
  }

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

  /* (!javadoc)
   * Retrieve a group by key.
   */
  protected static GrouperGroup loadByKey(
                                  GrouperSession s, String key, 
                                  String type
                                ) 
  {
    return GrouperGroup._loadByKey(s, key, type);
  }

  /**
   * Set {@link GrouperGroup} type.
   * <p />
   * 
   * @param   type  Set group to this type.
   * @return  True if the type was set.
   */
  protected boolean type(String type) {
    boolean rv = false;
    if (type != null) {
      this.type = type;
      rv = true;
    }
    return rv;
  }


  /*
   * PRIVATE CLASS METHODS
   */

  /* (!javadoc)
   * Does the current subject have permission to create 
   * groups within the specified stem.
   */
  private static boolean _canCreate(
                           GrouperSession s, String stem, String type
                         ) 
  {
    boolean rv = false;
    if (GrouperBackend.sessionValid(s)) {
      // We are adding a top-level namespace.
      if (stem.equals(Grouper.NS_ROOT)) {
        // And only member.system can do so in this release
        if (s.subject().getId().equals(Grouper.config("member.system"))) {
          rv = true;
        }
      } else {
        GrouperGroup ns = GrouperBackend.groupLoadByName(
                            s, stem, Grouper.NS_TYPE
                          );
        if (ns != null) {
          if (type.equals("naming")) {
            // If a naming group, does the subject have STEM on `stem'?
            rv = Grouper.naming().has(s, ns, Grouper.PRIV_STEM);
          } else {
            // Otherwise, does the subject have `CREATE' on `stem'?
            rv = Grouper.naming().has(s, ns, Grouper.PRIV_CREATE);
          }
        }
      }
    }
    return rv;
  }

  /* (!javadoc)
   * Does the current subject have permission to delete the group?
   */
  private static boolean _canDelete(GrouperSession s, GrouperGroup g) {
    boolean rv = false;
    if (GrouperBackend.sessionValid(s)) {
      // FIXME Support for multiple list types
      if ( (s != null) && (g != null) ) {
        if (Grouper.access().has(s, g, Grouper.PRIV_ADMIN)) {
          rv = true;
        }
      }
    }
    return rv;
  }

  /* (!javadoc)
   * Does the current subject have permission to modify attrs on the
   * specified group?
   */
  private static boolean _canModAttr(GrouperGroup g) {
    boolean rv = false;
    if (GrouperBackend.sessionValid(g.s)) {
      if (g != null) {
        if (Grouper.access().has(g.s, g, Grouper.PRIV_ADMIN)) {
          rv = true;
        }
      }
    }
    return rv;
  }

  /* (!javadoc)
   * Does the current subject have permission to modify list vals of
   * the specified type on the specified group?
   */
  private static boolean _canModListVal(GrouperGroup g, String list) {
    boolean rv = false;
    if (GrouperBackend.sessionValid(g.s)) {
      // FIXME Support for multiple list types
      if ( (g != null) && (list != null) ) {
        if (
            (Grouper.access().has(g.s, g, Grouper.PRIV_UPDATE)) ||
            (Grouper.access().has(g.s, g, Grouper.PRIV_ADMIN))
           )
        {
          rv = true;
        }
      }
    }
    return rv;
  }

  /*
   * Retrieve list values.
   */
  private List _listVals(GrouperGroup g, String list) {
    List vals = GrouperBackend.listVals(this.s, g, list);
    return vals;
  }

  /*
   * Retrieve effective list values.
   */
  private List _listEffVals(GrouperGroup g, String list) {
    List vals = GrouperBackend.listEffVals(this.s, g, list);
    return vals;
  }

  /*
   * Retrieve immediate list values.
   */
  private List _listImmVals(GrouperGroup g, String list) {
    List vals = GrouperBackend.listImmVals(this.s, g, list);
    return vals;
  }

  /*
   * Retrieve a group from the groups registry
   */ 
  private static GrouperGroup _loadByID(
                                GrouperSession s, String id, String type
                              ) 
  {
    GrouperGroup g = GrouperBackend.groupLoadByID(s, id, type);
    if (g != null) {
      // Attach type  
      // FIXME Grr....wait.  Is this even needed now that I have // type()?
      g.type = type; 
      g.initialized = true; // FIXME UGLY HACK!
      g.s = s; // Attach GrouperSession
    }
    return g;
  }

  private static GrouperGroup _loadByKey(
                                GrouperSession s, String key, 
                                String type
                              ) 
  {
    GrouperGroup g = GrouperBackend.groupLoadByKey(key);
    if (g != null) {
      // Attach type  
      // FIXME Grr....
      g.type = type;
      g.initialized = true; // FIXME UGLY HACK!
      g.s = s; // Attach GrouperSession
    }
    return g;
  }

  private static GrouperGroup _loadByName(
                                GrouperSession s, String name,
                                String type
                              )
  {
    GrouperGroup g = GrouperBackend.groupLoadByName(s, name, type);
    if (g != null) {
      // Attach type  
      // FIXME Grr....
      g.type = type;
      g.initialized = true; // FIXME UGLY HACK!
      g.s = s; // Attach GrouperSession
    }
    return g;
  }

  private static GrouperGroup _loadByStemExtn(
                                GrouperSession s, String stem, 
                                String extn, String type
                              )
  {
    GrouperGroup g = GrouperBackend.groupLoad(s, stem, extn, type);
    if (g != null) {
      // Attach type  
      // FIXME Grr....
      g.type = type;
      g.initialized = true; // FIXME UGLY HACK!
      g.s = s; // Attach GrouperSession
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
   * Add an attribute
   */
  private boolean _attrAdd(String attribute, GrouperAttribute attr) {
    boolean rv = false;
    if (attr != null) {
      attributes.put(attribute, attr);
      rv = true;
    }
    return rv;
  }

  // Add and persist attribute 
  private boolean _attributeAdd(String attribute, String value) {
    boolean rv = false;

    // In case we need to revert
    String curModTime = this.getModifyTime();
    String curModSubj = this.getModifySubject();

    // Verify subject has sufficient privs
    if (this._canModAttr(this)) {
      // Hibernate the attribute
      GrouperAttribute attr = GrouperBackend.attrAdd(
                                this.key, attribute, value
                              );
      if (attr != null) {
        // Now update this object and save it to persist the opattrs
        this.setModifyTime( GrouperGroup._now() );
        GrouperMember mem = GrouperMember.load( this.s.subject() );
        this.setModifySubject( mem.key() );
        this.attributes.put(attribute, attr);
        if (GrouperBackend.groupUpdate(s, this)) {
          rv = true;
        }
      }
    }
    Grouper.log().groupAttrAdd(rv, this.s, this, attribute, value);
    if (rv != true) {
      // Revert modify* attr changes 
      this.setModifyTime(curModTime);
      this.setModifySubject(curModSubj);
    }
    return rv;
  }

  // Delete and persist attribute 
  private boolean _attributeDelete(String attribute) {
    boolean rv = false;
    // In case we need to revert
    GrouperAttribute cur  = (GrouperAttribute) attributes.get(attribute);
    String curModTime     = this.getModifyTime();
    String curModSubj     = this.getModifySubject();

    // Verify subject has sufficient privs
    if (this._canModAttr(this)) {
      if (GrouperBackend.attrDel(this.key, attribute)) {
        // Now update this object and save it to persist the opattrs
        this.setModifyTime( GrouperGroup._now() );
        GrouperMember mem = GrouperMember.load( this.s.subject() );
        this.setModifySubject( mem.key() );
        this.attributes.remove(attribute);
        if (GrouperBackend.groupUpdate(this.s, this)) {
          rv = true;
        }
      }
    }
    Grouper.log().groupAttrDel(rv, this.s, this, attribute);
    if (rv != true) {
      // Revert attribute value change
      this.attributes.put(attribute, cur);
      // Revert modify* attr changes 
      this.setModifyTime(curModTime);
      this.setModifySubject(curModSubj);
    }
    return rv;
  }

  // Update and persist attribute 
  private boolean _attributeUpdate(String attribute, String value) {
    boolean rv = false;
    // In case we need to revert
    GrouperAttribute cur  = (GrouperAttribute) attributes.get(attribute);
    String curModTime     = this.getModifyTime();
    String curModSubj     = this.getModifySubject();

    // Verify subject has sufficient privs
    if (this._canModAttr(this)) {
      // Hibernate the attribute
      GrouperAttribute attr = GrouperBackend.attrAdd(
                                this.key, attribute, value
                              );
      if (attr != null) {
        // Now update this object and save it to persist the opattrs
        this.setModifyTime( GrouperGroup._now() );
        GrouperMember mem = GrouperMember.load( this.s.subject() );
        this.setModifySubject( mem.key() );
        this.attributes.put(attribute, attr);
        if (GrouperBackend.groupUpdate(this.s, this)) {
          rv = true;
        }
      }
    }
    Grouper.log().groupAttrUpdate(rv, this.s, this, attribute, value);
    if (rv != true) {
      // Revert attribute value change
      this.attributes.put(attribute, cur);
      // Revert modify* attr changes 
      this.setModifyTime(curModTime);
      this.setModifySubject(curModSubj);
    }
    return rv;
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
                                      String extn, String type
                                     )
  {
    GrouperGroup g = null;
    String name = GrouperGroup.groupName(stem, extn);
    if (GrouperGroup._canCreate(s, stem, type)) {
      // Check to see if the group already exists.
      g = GrouperGroup._loadByStemExtn(s, stem, extn, type);
      if (g != null) {
        /*
         * TODO Group already exists.  Ideally we'd throw an exception or
         *      something, but, for now...
         */
        Grouper.log().groupAddCannot(s, name, type);
        g = null;
      } else {
        if (name != null) {
          // TODO Can I move all|most of this to GrouperBackend?
          g = new GrouperGroup();
          // Generate the UUIDs
          g.setGroupKey( GrouperBackend.uuid() );
          g.setGroupID(  GrouperBackend.uuid() );

          // Set attributes
          GrouperAttribute stem_attr = new GrouperAttribute(
                                        g.getGroupKey(),
                                        "stem", stem
                                       );
          GrouperAttribute extn_attr = new GrouperAttribute(
                                        g.getGroupKey(),
                                        "extension", extn
                                       );
          GrouperAttribute name_attr = new GrouperAttribute(
                                        g.getGroupKey(),
                                        "name", name
                                       );
          g._attrAdd("stem",      stem_attr);
          g._attrAdd("extension", extn_attr);
          g._attrAdd("name",      name_attr);
          /*
           * TODO Add `displayName' support
           *      Will I run into priv (for fetching of stem's
           *      `displayName' when I add in support for this?
           */

          g.type = type;
          // Set some of the operational attributes
          /*
           * TODO Most, if not all, of the operational attributes should be
           *      handled by Hibernate interceptors.  A task for another day.
           */
          g.setCreateTime(    GrouperGroup._now() );
          GrouperMember mem = GrouperMember.load( s.subject() );
          g.setCreateSubject( mem.key() );

          // Verify that we have everything we need to create a group
          // and that this subject is privileged to create this group.
          if (g._validateCreate()) {
            // And now attempt to add the group to the store
            if (GrouperBackend.groupAdd(s, g)) {
              g.initialized = true; // FIXME UGLY HACK!
              g.s = s;  // Attach the GrouperSession
            } else {
              g = null;  
            }
          }
        } else {
          // TODO Log
          g = null;
        }
      }
    } else {
      Grouper.log().event(
        "Subject does not have " + Grouper.PRIV_CREATE + 
        " privileges on this stem"
      );
    }
    Grouper.log().groupAdd(s, g, name, type);
    return g;
  }

  /*
   * Initialize instance variables
   */
  private void _init() { 
    this.attributes     = new HashMap();
    this.createTime     = null;
    this.createSubject  = null;
    this.createSource   = null;
    this.groupComment   = null;
    this.key            = null;
    this.modifyTime     = null;
    this.modifySubject  = null;
    this.modifySource   = null;
    this.s              = null;
    this.type           = null; // FIXME Is this right?
  }

  /*
   * Add list value and update modify* attributes.
   */
  private boolean _listAddVal(GrouperMember m, String list) {
    boolean rv = false;
    // Set some of the operational attributes
    /*
     * TODO Most, if not all, of the operational attributes should be
     *      handled by Hibernate interceptors.  A task for another day.
     */
    String curModTime = this.getModifyTime();
    String curModSubj = this.getModifySubject();
    this.setModifyTime( GrouperGroup._now() );
    GrouperMember mem = GrouperMember.load( this.s.subject());
    this.setModifySubject( mem.key() );
    if (
        GrouperBackend.listAddVal(
          this.s, new GrouperList(this, m, list, null)
        ) == true
       )
    {
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
  private boolean _listDelVal(GrouperMember m, String list) {
    boolean rv = false;
    // Set some of the operational attributes
    /*
     * TODO Most, if not all, of the operational attributes should be
     *      handled by Hibernate interceptors.  A task for another day.
     */
    String curModTime = this.getModifyTime();
    String curModSubj = this.getModifySubject();
    this.setModifyTime( GrouperGroup._now() );
    GrouperMember mem = GrouperMember.load( this.s.subject());
    this.setModifySubject( mem.key() );
    //if (GrouperBackend.listDelVal(s, this, m, list) == true) {
    if (
        GrouperBackend.listDelVal(
          this.s, new GrouperList(this, m, list, null) 
        ) == true
       )
    {
      rv = true;
    } else {
      // Revert changes
      this.setModifyTime(curModTime);
      this.setModifySubject(curModSubj);
    }
    return rv;
  }

  /* (!javadoc)
   * Try to return an initialized Subject object.  If not, default to
   * an unitialized object.  Used by the (create|modify)Subject opattr
   * methods.
   */
  private Subject _returnSubjectObject(String memberKey) {
    Subject subj = null;
    if (memberKey != null) {
      GrouperMember mem = GrouperBackend.member(memberKey);
      if (mem != null) {
        subj = GrouperSubject.load(mem.subjectID(), mem.typeID());
      }
    }
    if (subj == null) {
      return new SubjectImpl();
    }
    return subj;
  }

  /* (!javadoc)
   * Validate whether an attribute is valid for the current group type.
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

  /* (!javadoc)
   * Validate whether all attributes are valid for the current group
   * type.
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
 
  /* (!javadoc)
   * Validate whether a group can be created.
   */
  private boolean _validateCreate() {
    if (
        // Do we have a valid group type?
        (Grouper.groupType(this.type) == true) &&
        // And a stem?
        (attributes.containsKey("stem"))       &&
        // And an extension?
        (attributes.containsKey("extension"))  && 
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

  private String getGroupComment() {
    return this.groupComment;
  }

  private void setGroupComment(String comment) {
    this.groupComment = comment;
  } 

}

