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
import  net.sf.hibernate.*;
import  org.apache.commons.lang.builder.ToStringBuilder;


/** 
 * Class modeling a {@link Grouper} group.
 * <p />
 *
 * @author  blair christensen.
 * @version $Id: GrouperGroup.java,v 1.187 2005-03-23 23:15:48 blair Exp $
 */
public class GrouperGroup extends Group {

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
    if (g._canDelete()) {
      s.dbSess().txStart();
      try {
        // Revoke access privileges
        if (g._privAccessRevokeAll()) {
          // Revoke naming privileges
          if (g._privNamingRevokeAll()) {
            // Delete attributes
            if (g._deleteAttributes()) {
              // Delete schema
              GrouperSchema.delete(s, g);
              // Delete group
              s.dbSess().session().delete(g);
              rv = true;
            }
          }
        }
      } catch (HibernateException e) {
        throw new RuntimeException("Error deleting group: " + e);
      }
    }
    if (rv) {
      s.dbSess().txCommit();
    } else {
      s.dbSess().txRollback();
    }
    Grouper.log().groupDel(rv, s, g);
    return rv;
  }

  /* 
   * Delete all attributes attached to a group
   */
  private boolean _deleteAttributes() {
    boolean rv = false;
    // TODO 
    Iterator iter = GrouperBackend.attributes(this.s, this).iterator();
    while (iter.hasNext()) {
      GrouperAttribute attr = (GrouperAttribute) iter.next();
      try {
        GrouperAttribute.delete(this.s, attr);
        rv = true;
      } catch (RuntimeException e) {
        // TODO Less than ideal
        rv = false;
        break;
      }
    }
    return rv;
  }

  /* 
   * Revoke all access privs attached to a group
   */
  private boolean _privAccessRevokeAll() {
    boolean rv = false;
    /* 
     * TODO This could be prettier, especially if/when there are custom
     *      privs
     */
    if (
        this.s.access().revoke(this.s, this, Grouper.PRIV_OPTIN)   &&
        this.s.access().revoke(this.s, this, Grouper.PRIV_OPTOUT)  &&
        this.s.access().revoke(this.s, this, Grouper.PRIV_VIEW)    &&
        this.s.access().revoke(this.s, this, Grouper.PRIV_READ)    &&
        this.s.access().revoke(this.s, this, Grouper.PRIV_UPDATE)  &&
        this.s.access().revoke(this.s, this, Grouper.PRIV_ADMIN)
       )
    {
      rv = true;
    }
    return rv;
  }

  /* 
   * Revoke all naming privs attached to a group
   */
  protected boolean _privNamingRevokeAll() {
    boolean rv = false;
    // Revoke all privileges
    // FIXME This is ugly 
    if (
        this.s.naming().revoke(this.s, this, Grouper.PRIV_STEM)    &&
        this.s.naming().revoke(this.s, this, Grouper.PRIV_CREATE) 
       )
    {       
      rv = true;
    }
    return rv;
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
        s.dbSess().txStart();
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
        if (rv) {
          s.dbSess().txCommit();
        } else {
          s.dbSess().txRollback();
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
  protected void type(String type) {
    this.type = type;
  }

  /**
   * Retrieve list values of the default list type for this group.
   * <p />
   * @return  List of {@link GrouperList} objects.
   */
  public List listVals() {
    return this._listVals(Grouper.DEF_LIST_TYPE);
  }

  /**
   * Retrieve list values of the specified type for this group.
   * <p />
   * @param   list  Return this list type.
   * @return  List of {@link GrouperList} objects.
   */
  public List listVals(String list) {
    return this._listVals(list);
  }

  /**
   * Retrieve effective list values of the default list type for this
   * group.
   * <p />
   * @return  List of {@link GrouperList} objects.
   */
  public List listEffVals() {
    return this._listEffVals(Grouper.DEF_LIST_TYPE);
  }

  /**
   * Retrieve effective list values of the specified type for this
   * group.
   * <p />
   * @param   list  Return this list type.
   * @return  List of {@link GrouperList} objects.
   */
  public List listEffVals(String list) {
    return this._listEffVals(list);
  }

  /**
   * Retrieve immediate list values of the default list type for this
   * group.
   * <p />
   * @return  List of {@link GrouperList} objects.
   */
  public List listImmVals() {
    return this._listImmVals(Grouper.DEF_LIST_TYPE);
  }

  /**
   * Retrieve immediate list values of the specified type for this
   * group.
   * <p />
   * @param   list  Return this list type.
   * @return  List of {@link GrouperList} objects.
   */
  public List listImmVals(String list) {
    return this._listImmVals(list);
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
    /* 
     * FIXME What a mess.  Did this break again when I started having
     *       GNI go through GG rather than GB for list val changes?
     */
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
    // TODO This isn't right
    String name = null;
    if (this.attribute("name") != null) {
      name = this.attribute("name").value();
    }
    return name;
    //return this.attribute("name").value();
  }

  /**
   * Retrieve {@link GrouperMember} object for this 
   * {@link GrouperGroup}.
   * </p>
   * @return {@link GrouperMember} object
   */
  public GrouperMember toMember() {
    GrouperSession.validate(this.s);
    GrouperMember m = GrouperMember.load(
                        this.s, this.getGroupID(), "group"
                      );
    if (m == null) {
      throw new RuntimeException("Error converting group to member");
    }
    return m;
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
    // We are adding a top-level namespace.
    if (stem.equals(Grouper.NS_ROOT)) {
      // And only member.system can do so in this release
      if (s.subject().getId().equals(Grouper.config("member.system"))) {
        rv = true;
      }
    } else {
      GrouperGroup ns = GrouperGroup._loadByName(
                          s, stem, Grouper.NS_TYPE
                        );
      if (ns != null) {
        if (type.equals("naming")) {
          // If a naming group, does the subject have STEM on `stem'?
          rv = s.naming().has(s, ns, Grouper.PRIV_STEM);
        } else {
          // Otherwise, does the subject have `CREATE' on `stem'?
          rv = s.naming().has(s, ns, Grouper.PRIV_CREATE);
        }
      }
    }
    return rv;
  }

  /* 
   * Does the current subject have permission to delete the group?
   */
  private boolean _canDelete() {
    boolean rv = false;
    // FIXME Support for multiple list types
    if ( (this != null) && (this.s != null) ) {
      if (this.s.access().has(this.s, this, Grouper.PRIV_ADMIN)) {
        rv = true;
        // Convert the group to member to see if it has any mships
        GrouperMember m = this.toMember();
        List valsG = this.listVals(Grouper.DEF_LIST_TYPE);
        List valsM = m.listVals(Grouper.DEF_LIST_TYPE);
        if ( (valsG.size() != 0) || (valsM.size() != 0) ) {
          // TODO Throw exception!
          if (valsG.size() != 0) {
            Grouper.log().event(
              "ERROR: Unable to delete group as it still has members"
            );
          }
          if (valsM.size() != 0) {
            Grouper.log().event(
              "ERROR: Unable to delete group as it is a member of other groups"
            );
          }
        } else {
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
    if (g != null) {
      if (g.s.access().has(g.s, g, Grouper.PRIV_ADMIN)) {
        rv = true;
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
    // FIXME Support for multiple list types
    if ( (g != null) && (list != null) ) {
      if (
          (g.s.access().has(g.s, g, Grouper.PRIV_UPDATE)) ||
          (g.s.access().has(g.s, g, Grouper.PRIV_ADMIN))
         )
      {
        rv = true;
      }
    }
    return rv;
  }

  /*
   * Retrieve list values.
   */
  private List _listVals(String list) {
    String  qry   = "GrouperList.by.group.and.list";
    List    vals  = new ArrayList();
    try {
      Query q = this.s.dbSess().session().getNamedQuery(qry);
      q.setString(0, this.key());
      q.setString(1, list);
      try {
        Iterator iter = q.list().iterator();
        while (iter.hasNext()) {
          // Make the returned items into proper objects
          GrouperList gl = (GrouperList) iter.next();
          gl.load(this.s);
          vals.add(gl);
        }
      } catch (HibernateException e) {
        throw new RuntimeException(
                    "Error retrieving results for " + qry + ": " + e
                  );
      }
    } catch (HibernateException e) {
      throw new RuntimeException(
                  "Unable to get query " + qry + ": " + e
                );
    }
    return vals;
  }

  /*
   * Retrieve effective list values.
   */
  private List _listEffVals(String list) {
    String  qry   = "GrouperList.by.group.and.list.and.is.eff";
    List    vals  = new ArrayList();
    try {
      Query q = this.s.dbSess().session().getNamedQuery(qry);
      q.setString(0, this.key());
      q.setString(1, list);
      try {
        // TODO Argh!
        Iterator iter = q.list().iterator();
        while (iter.hasNext()) {
          // Make the returned items into proper objects
          GrouperList gl = (GrouperList) iter.next();
          gl.load(this.s);
          vals.add(gl);
        }
      } catch (HibernateException e) {
        throw new RuntimeException(
                    "Error retrieving results for " + qry + ": " + e
                  );
      }
    } catch (HibernateException e) {
      throw new RuntimeException(
                  "Unable to get query " + qry + ": " + e
                );
    } 
    return vals;
  }

  /*
   * Retrieve immediate list values.
   */
  private List _listImmVals(String list) {
    String  qry   = "GrouperList.by.group.and.list.and.is.imm";
    List    vals  = new ArrayList();
    try {
      Query q = this.s.dbSess().session().getNamedQuery(qry);
      q.setString(0, this.key());
      q.setString(1, list);
      try {
        // TODO Argh!
        Iterator iter = q.list().iterator();
        while (iter.hasNext()) {
          // Make the returned items into proper objects
          GrouperList gl = (GrouperList) iter.next();
          gl.load(this.s);
          vals.add(gl);
        }
      } catch (HibernateException e) {
        throw new RuntimeException(
                    "Error retrieving results for " + qry + ": " + e
                  );
      }
    } catch (HibernateException e) {
      throw new RuntimeException(
                  "Unable to get query " + qry + ": " + e
                );
    }
    return vals;
  }

  /*
   * Retrieve a group from the groups registry
   */ 
  private static GrouperGroup _loadByID(
                                GrouperSession s, String id,
                                String type
                              ) 
  {
    GrouperGroup  g     = null;
    String        key   = null;
    String        qry   = "GrouperGroup.by.id";
    try {
      Query q = s.dbSess().session().getNamedQuery(qry);
      q.setString(0, id);
      try {
        List vals = q.list();
        if (vals.size() == 1) {
          g = (GrouperGroup) vals.get(0);
          if ( (g != null) && (g.key() != null) ) {
            key = g.key();
            g = GrouperGroup.loadByKey(s, g, key);
            if (g != null) {
              // Attach type  
              g.type = type; 
              g.initialized = true; // FIXME UGLY HACK!
              g.s = s; // Attach GrouperSession
            }
          }
        }
      } catch (HibernateException e) {
        throw new RuntimeException(
                    "Error retrieving results for " + qry + ": " + e
                  );
      }
    } catch (HibernateException e) {
      throw new RuntimeException(
                  "Unable to get query " + qry + ": " + e
                );
    }
    return g;
  }

  protected static GrouperGroup loadByKey(GrouperSession s, String key) {
    return GrouperGroup.loadByKey(s, new GrouperGroup(), key);
  }
  protected static GrouperGroup loadByKey(
                                  GrouperSession s, GrouperGroup g,
                                  String key
                                )
  { 
    GrouperSession.validate(s);
    try {
      // Attempt to load a stored group into the current object
      g = (GrouperGroup) s.dbSess().session().get(GrouperGroup.class, key);
      if (g != null) {
        // Its schema
        GrouperSchema schema = GrouperSchema.load(s, g.key());
        if (schema != null) {
          if (GrouperBackend._groupAttachAttrs(s, g)) {
            g.type( schema.type() );
          } else {
            g = null;
          }
        } else {
          g = null;
        }
      }
    } catch (HibernateException e) {
      // TODO Rollback if load fails?  Unset this.exists?
      throw new RuntimeException("Error loading group: " + e);
    }

    if (g != null) {
      g.initialized = true; // FIXME UGLY HACK!
      g.s = s; // Attach GrouperSession
    }
    return g;
  }

  // TODO Move to _Group_
  protected static GrouperGroup _loadByName(
                                GrouperSession s, String name,
                                String type
                              )
  {
    // FIXME Kill me.  Please.
    GrouperGroup  g           = null;
    String        qryGG       = "GrouperAttribute.by.name";
    String        qryGS       = "GrouperSchema.by.key.and.type";
    boolean       initialized = false;
    try {
      Query qGG = s.dbSess().session().getNamedQuery(qryGG);
      qGG.setString(0, name);
      try {
        List names = qGG.list();
        if (names.size() > 0) {
          Iterator iter = names.iterator();
          while (iter.hasNext()) {
            GrouperAttribute attr = (GrouperAttribute) iter.next();
            try {
              Query qGS = s.dbSess().session().getNamedQuery(qryGS); 
              qGS.setString(0, attr.key());
              qGS.setString(1, type);
              try {
                List gs = qGS.list();
                if (gs.size() == 1) {
                  GrouperSchema schema = (GrouperSchema) gs.get(0);
                  if (schema.type().equals(type)) {
                    g = GrouperGroup.loadByKey(s, g, attr.key());
                    if (g != null) {
                      if (g.type().equals(type)) {
                        initialized = true;
                        //g.type = type;
                        g.initialized = true; // FIXME UGLY HACK!
                        g.s = s; // Attach GrouperSession
                      }
                    }
                  }
                }
              } catch (HibernateException e) {
                throw new RuntimeException(
                            "Error retrieving results for " + 
                            qryGS + ": " + e
                          );
              }
            } catch (HibernateException e) {
              throw new RuntimeException(
                          "Unable to get query " + qryGS + ": " + e
                        );
            }
          }
        }
      } catch (HibernateException e) {
        throw new RuntimeException(
                    "Error retrieving results for " + qryGG + ": " + e
                  );
      }
    } catch (HibernateException e) {
      throw new RuntimeException(
                  "Unable to get query " + qryGG + ": " + e
                );
    }
    if (!initialized) {
      // We failed to load a group.  Null out the object.
      g = null;
    }
    return g;
  }

  private static GrouperGroup _loadByStemExtn(
                                GrouperSession s, String stem, 
                                String extn, String type
                              )
  {
    GrouperGroup g = null;
    if (GrouperStem.exists(s, stem)) {
      String name = GrouperGroup.groupName(stem, extn);
      g = GrouperGroup._loadByName(s, name, type);
      if (g != null) {
        // Attach type  
        // FIXME Grr....
        g.type = type;
        g.initialized = true; // FIXME UGLY HACK!
        g.s = s; // Attach GrouperSession
      }
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
      GrouperAttribute attr = new GrouperAttribute(
                                    this.key, attribute, value
                                  );
      try {
        GrouperAttribute.save(this.s, attr);
        // Now update this object and save it to persist the opattrs
        this.setModifyTime( GrouperGroup._now() );
        GrouperMember mem = GrouperMember.load(this.s, this.s.subject());
        this.setModifySubject( mem.key() );
        this.attributes.put(attribute, attr);
        this.update();
      } catch (RuntimeException e) {
        rv = false;
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

  // TODO Does this actually work?
  // TODO Is this needed?
  protected void update() {
    try {
      this.s.dbSess().session().update(this);
    } catch (HibernateException e) {
      throw new RuntimeException("Error updating group: " + e);
    }
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
      if (GrouperBackend.attrDel(this.s, this.key, attribute)) {
        // Now update this object and save it to persist the opattrs
        this.setModifyTime( GrouperGroup._now() );
        GrouperMember mem = GrouperMember.load(this.s, this.s.subject());
        this.setModifySubject( mem.key() );
        this.attributes.remove(attribute);
        try {
          this.update();
          rv = true;
        } catch (RuntimeException e) {
          rv = false; 
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
      GrouperAttribute attr = new GrouperAttribute(
                                    this.key, attribute, value
                                  );
      try {
        // Now update this object and save it to persist the opattrs
        GrouperAttribute.save(this.s, attr);
        this.setModifyTime( GrouperGroup._now() );
        GrouperMember mem = GrouperMember.load(this.s, this.s.subject());
        this.setModifySubject( mem.key() );
        this.attributes.put(attribute, attr);
        try {
          this.update();
          rv = true;
        } catch (RuntimeException e) {
          rv = false;
        }
      } catch (RuntimeException e) {
        rv = false;
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
    s.dbSess().txStart();
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
          // Merge these two?
          g = new GrouperGroup();
          g.s = s;

          // Generate the UUIDs
          g.setGroupKey( new GrouperUUID().toString() );
          g.setGroupID(  new GrouperUUID().toString() ); 

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
          GrouperMember mem = GrouperMember.load(s, s.subject());
          g.setCreateSubject( mem.key() );

          // Verify that we have everything we need to create a group
          // and that this subject is privileged to create this group.
          if (g._validateCreate()) {
            try {
              s.dbSess().session().save(g);
              // Add schema
              GrouperSchema.save(s, g);
              // Add attributes
              if (g._saveAttributes()) {
                if (g._privGrantUponCreate()) {
                  g.initialized = true; // FIXME UGLY HACK!
                }
              }
            } catch (HibernateException e) {
              throw new RuntimeException("Error saving group: " + g);
            } 
          }
        } 
      }
    } else {
      Grouper.log().event(
        "Subject does not have " + Grouper.PRIV_CREATE + 
        " privileges on this stem"
      );
    }
    if (g != null) {
      if (g.initialized != true) {
  System.err.println("23");
        g = null;
      } else {
        s.dbSess().txCommit();
      }
    } else {
      s.dbSess().txRollback();
    }
    Grouper.log().groupAdd(s, g, name, type);
    return g;
  }

  /*
   * Save group's attributes.
   * TODO Make part of save()?
   */
  private boolean _saveAttributes() {
    boolean rv = false;
    Iterator iter = this.attributes().keySet().iterator();
    while (iter.hasNext()) {
      GrouperAttribute attr = (GrouperAttribute) this.attribute(
                                                   (String) iter.next() 
                                                 );
      try {
        GrouperAttribute.save(
          this.s, new GrouperAttribute(this.key(), attr.field(), attr.value())
        );
      } catch (RuntimeException e) {
        // TODO Less than ideal
        rv = false;
        break;
      }
      rv = true; 
    }
    return rv;
  }

  /* 
   * Grant PRIV_ADMIN to group creator upon creation
   */
  private boolean _privGrantAdminUponCreate(
                    GrouperSession s, GrouperMember m
                  )
  {
    boolean rv = false;
    if (s.access().grant(s, this, m, Grouper.PRIV_ADMIN)) {
      rv = true;
    } else {
      // TODO Exception!
    }
    return rv;
  }

  /* 
   * Grant PRIV_STEM to stem creator upon creation
   */
  protected boolean _privGrantStemUponCreate(
                      GrouperSession s, GrouperMember m
                    )
  {
    boolean rv = false;
    if (s.naming().grant(s, this, m, Grouper.PRIV_STEM)) {
      rv = true;
    } else {
      // TODO Exception!
    }
    return rv;
  }

  /* 
   * Grant appropriate privilege to group|stem creator upon creation
   */
  protected boolean _privGrantUponCreate() {
    GrouperSession.validate(this.s);
    boolean rv = false;
    // We need a root session for for bootstrap privilege granting
    // TODO Replace with Grouper root session?
    Subject root = GrouperSubject.load(
                     Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE
                   );
    GrouperSession rs = GrouperSession.start(root);
    if (rs != null) {
      // Now grant privileges to the group creator
      GrouperMember m = GrouperMember.load(this.s.subject() );
      if (m != null) { // FIXME Bah
        if (this.type().equals(Grouper.NS_TYPE)) {
          if (this._privGrantStemUponCreate(rs, m)) {
            // NS_TYPE groups get PRIV_STEM
            rv = true;
          } 
        } else if (this._privGrantAdminUponCreate(rs, m)) {
          // All other group types get PRIV_ADMIN
          rv = true;
        }
      }
      // Close root session
      rs.stop();
    }
    return rv;
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
    s.dbSess().txStart();
    // Set some of the operational attributes
    /*
     * TODO Most, if not all, of the operational attributes should be
     *      handled by Hibernate interceptors.  A task for another day.
     */
    String curModTime = this.getModifyTime();
    String curModSubj = this.getModifySubject();
    this.setModifyTime( GrouperGroup._now() );
    GrouperMember mem = GrouperMember.load(this.s, this.s.subject());
    this.setModifySubject( mem.key() );
    GrouperList gl = new GrouperList(this, m, list);
    gl.load(this.s); // TODO Necessary?
    GrouperList.validate(gl);
    gl.load(this.s);

    if (GrouperList.exists(s, gl) == false) {
      // The GrouperList objects that we will need to add
      MemberOf mof = new MemberOf(this.s);
      List listVals = mof.memberOf(gl);
          
      // Now add the list values
      // TODO Refactor out to _listAddVal(List vals)
      Iterator iter = listVals.iterator();
      while (iter.hasNext()) {
        GrouperList lv = (GrouperList) iter.next();
        lv.load(this.s); // TODO Is this necessary?
        GrouperList.save(this.s, lv);
      }
      rv = true; // TODO This seems naive
    }
    if (rv) { 
      this.s.dbSess().txCommit();
    } else {
      // Revert changes
      this.s.dbSess().txRollback();
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
    s.dbSess().txStart();
    // Set some of the operational attributes
    /*
     * TODO Most, if not all, of the operational attributes should be
     *      handled by Hibernate interceptors.  A task for another day.
     */
    String curModTime = this.getModifyTime();
    String curModSubj = this.getModifySubject();
    this.setModifyTime( GrouperGroup._now() );
    GrouperMember mem = GrouperMember.load(this.s, this.s.subject());
    this.setModifySubject( mem.key() );

    GrouperList gl = new GrouperList(this, m, list);
    gl.load(this.s); // TODO Necessary?
    GrouperList.validate(gl);
    gl.load(this.s);

    if (GrouperList.exists(this.s, gl)) {
      // The GrouperList objects that we will need to add
      MemberOf mof = new MemberOf(this.s);
      List listVals = mof.memberOf(gl);
          
      // Now add the list values
      // TODO Refactor out to _listAddVal(List vals)
      Iterator iter = listVals.iterator();
      while (iter.hasNext()) {
        GrouperList lv = (GrouperList) iter.next();
        lv.load(this.s); // TODO Is this necessary?
        GrouperList.delete(this.s, lv);
      }
      rv = true; // TODO This seems naive
    }
    if (rv) {
      s.dbSess().txCommit();
    } else {
      // Revert changes
      s.dbSess().txRollback();
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
      GrouperMember mem = GrouperMember.loadByKey(this.s, memberKey);
      if (mem != null) {
        subj = GrouperSubject.load(mem.subjectID(), mem.typeID());
      }
    }
/* TODO Arguably this is correct but right now it is not
    if (subj == null) {
      return new SubjectImpl();
    }
*/
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
    // TODO Break these down into individual error reporting conditions
    if (
        // Do we have a valid group type?
        (Grouper.groupType(this.type) == true) &&
        // And a stem?
        (attributes.containsKey("stem"))       &&
        // And stem exists
        (GrouperStem.exists(this.s, this.attribute("stem").value())) &&
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

