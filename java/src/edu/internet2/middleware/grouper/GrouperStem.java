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


import  edu.internet2.middleware.subject.*;

import  java.util.*;
import  net.sf.hibernate.*;


/** 
 * Class modeling a {@link Grouper} stem.
 * <p />
 *
 * @author  blair christensen.
 * @version $Id: GrouperStem.java,v 1.50 2005-07-14 03:05:42 blair Exp $
 */
public class GrouperStem extends Group {

  /*
   * PRIVATE INSTANCE VARIABLES
   */
  private Map             attributes = new HashMap();
  private String          createSource;
  private String          createSubject;
  private String          createTime;
  private String          groupComment;
  private String          id;
  private boolean         initialized = false;
  private String          key; 
  private String          modifySource;
  private String          modifySubject;
  private String          modifyTime;
  private GrouperSession  s;
  private String          type;


  /*
   * CONSTRUCTORS
   */

  /**
   * Null-argument constructor for Hibernate.
   */
  public GrouperStem() {
    // Nothing
  }

  /* 
   * Use when creating a new stem.
   */
  private GrouperStem(GrouperSession s, String stem, String extn) {
    this.s    = s; 
    this.type = Grouper.NS_TYPE;
    this.setGroupKey( new GrouperUUID().toString() );
    this.setGroupID(  new GrouperUUID().toString() );

    GrouperSchema.save(s, this);

    this.attributeAdd(
      new GrouperAttribute(this.getGroupKey(), "stem", stem)
    );
    this.attributeAdd(
      new GrouperAttribute(this.getGroupKey(), "extension", extn)
    );
    this.attributeAdd(
      new GrouperAttribute(
        this.getGroupKey(), "name", Group.groupName(stem, extn)
      )
    );
    this.attributeAdd(
      new GrouperAttribute(
        this.getGroupKey(), "displayExtension", extn
      )
    );
    // TODO Is this right?
    this.attributeAdd(
      new GrouperAttribute(
        this.getGroupKey(), "displayName", Group.displayName(stem, extn)
      )
    );

    this.setCreated();
  }


  /*
   * PUBLIC CLASS METHODS
   */

  /**
   * Create a namespace..
   * <p />
   * @param   s     Session to create the namespace within.
   * @param   stem  Stem to create the namespace within.
   * @param   extn  Extension to assign to the namespace.
   * @return  A {@link GrouperGroup} object.
   */ 
  public static GrouperStem create(
                              GrouperSession s, String stem, String extn
                            )
  {
    Group.invalidStemOrExtn(stem, extn);
    GrouperStem ns;
    if (!GrouperStem.exists(s, stem)) {
      throw new RuntimeException("Parent stem does not exist");
    }
    if (GrouperStem.exists(s, Group.groupName(stem, extn))) {
      throw new RuntimeException("Stem already exists");
    }
    Group.subjectCanCreateAtRoot(s, stem);
    try {
      Group.subjectCanCreateStem(s, stem);
      try {
        s.dbSess().txStart();
        ns = new GrouperStem(s, stem, extn);
        s.dbSess().session().save(ns);
        ns.grantStemUponCreate(); 
        ns.initialized = true;
        s.dbSess().txCommit();
        Grouper.log().stemAdd(s, ns, Group.groupName(stem, extn), ns.type());
      } catch (HibernateException e) {
        s.dbSess().txRollback();
        throw new RuntimeException(
          "Error saving stem: " + e.getMessage()
        );
      } 
    } catch (InsufficientPrivilegeException e) {
      // TODO Is this the right message?
      throw new RuntimeException(
        "Error creating stem: " + e.getMessage()
      );
    }
    return ns;
  }

  /**
   * Retrieve a group by stem and extension.
   * <p />
   * @param   s           Session to load the group within.
   * @param   stem        Stem of the group to load.
   * @param   extension   Extension of the group to load.
   * @return  A {@link GrouperGroup} object.
   */
  public static GrouperStem load(
    GrouperSession s, String stem, String extension
  )
  {
    Group.invalidStemOrExtn(stem, extension);
    String key = Group.findKeyByStemExtnType(s, stem, extension, Grouper.NS_TYPE);
    if (key != null) {
      try {
        GrouperStem ns = (GrouperStem) Group.loadByKey(s, key);
        return ns;
      } catch (InsufficientPrivilegeException e) {
        return null; // FIXME HATE!!!
      }
    }
    return null; 
  }

  /**
   * Retrieve a namespace by id.
   * <p />
   * @param   s           Session to load the namespace within.
   * @param   id          Namespace ID.
   * @return  A {@link GrouperStem} object.
   */
  public static GrouperStem loadByID(GrouperSession s, String id) {
    try {
      return (GrouperStem) Group._loadByID(s, id);
    } catch (InsufficientPrivilegeException e) {
      return null; // FIXME HATE!!!
    }
  }

  /**
   * Retrieve a namespace by name.
   * <p />
   * @param   s           Session to load the namespace within.
   * @param   name        Name of namespace.
   * @return  A {@link GrouperStem} object.
   */
  public static GrouperStem loadByName(
    GrouperSession s, String name
  )
  {
    try {
      return (GrouperStem) Group.loadByNameAndType(s, name, Grouper.NS_TYPE);
    } catch (InsufficientPrivilegeException e) {
      return null; // FIXME HATE!!!
    }
  }


  /*
   * PUBLIC INSTANCE METHODS
   */

  /**
   * Retrieve the specified attribute.
   * <p />
   * @param   attribute The attribute to retrieve.
   * @return  A {@link GrouperAttribute} object.
   */
  public GrouperAttribute attribute(String attribute) {
    // TODO Throw exception if invalid attribute for this type?
    GrouperAttribute attr = new NullGrouperAttribute(
                                  this.getGroupKey(), attribute
                                );
    if (this.attributes.containsKey(attribute)) {
      attr = (GrouperAttribute) this.attributes.get(attribute);
    }
    return attr;
  }

  /**
   * Set an attribute value.
   * <p />
   * If <i>value</i> is <i>null</i> or <i>""</i>, the attribute
   * will be deleted.  
   * <p /> 
   * @param   attribute   Set this attribute.
   * @param   value       To this value.
   */
  public void attribute(String attribute, String value) {
    this.attribute(this.s, this, attribute, value);
  }

  /** 
   * Retrieve all of this namespace's attributes.
   * <p />
   * @return  A map of {@link GrouperAttribute} objects.
   */
  public Map attributes() {
    return this.attributes;
  }

  /**
   * Retrieve the <i>createSource</i> value.
   * <p  />
   * This attribute is not currently used.
   * @return <i>createSource</i> value.
   */
  public String createSource() {
    return this.getCreateSource();
  }

  /**
   * Retrieve the <i>createSubject</i> value.
   * <p  />
   * @return A {@link Subject} object.
   */
  public Subject createSubject() {
    return GrouperMember.toSubject(this.s, this.getCreateSubject());
  }

  /**
   * Retrieve the <i>createTime</i> value.
   * <p  />
   * @return <i>createTime</i> as a {@link Date} object.
   */
  public Date createTime() {
    return this.string2date(this.getCreateTime());
  }

  /**
   * Retrieve list of groups that are <b>immediate</b> children
   * of this stem.
   * <p />
   * @return  List of {@link GrouperGroup} objects.
   */
  public List groups() {
    // TODO Shares a lot of common code with stems()
    String  qry   = "Group.key.child.group.of.stem";
    List    vals  = new ArrayList();
    try {
      Query q = s.dbSess().session().getNamedQuery(qry);
      q.setString(0, this.name());
      try {
        Iterator iter = q.list().iterator();
        while (iter.hasNext()) {
          try {
            String key = (String) iter.next();
            GrouperGroup s = (GrouperGroup) Group.loadByKey(this.s, key);
            vals.add(s);
          } catch (InsufficientPrivilegeException e) {
            // Ignore
          }
        }
      } catch (HibernateException e) {
        throw new RuntimeException(
          "Error retrieving results for " + qry + ": " + e.getMessage()
        );
      }
    } catch (HibernateException e) {
      throw new RuntimeException(
        "Unable to get query " + qry + ": " + e.getMessage()
      );
    }
    return vals;
  }

  /**
   * Check whether a group has a specific member.
   * <p />
   * @param   m   Check whether m is a member of this group.
   * @return  boolean true if it is a member
   */
  public boolean hasMember(GrouperMember m) {
    return this.hasMember(m, Grouper.DEF_LIST_TYPE);
  }

  /**
   * Check whether a group has a specific member.
   * <p />
   * @param   m     Check whether m is a member of this group.
   * @param   list  check membership in this list.
   * @return  boolean true if it is a member
   */
  public boolean hasMember(GrouperMember m, String list) {
    return this.hasMember(this.s, m, list);
  }

  /**
   * Retrieve namespace's public id.
   * <p />
   * @return Public id.
   */
  public String id() {
    try {
      this.s.canREAD(this);
      return this.getGroupID();
    } catch (InsufficientPrivilegeException e) {
      return new String();
    }
  }

  /**
   * Add member to this stem's default list.
   * <p />
   * @param m   Add this member.
   */
  public void listAddVal(GrouperMember m) {
    this.listAddVal(m, Grouper.DEF_LIST_TYPE);
  }

  /**
   * Add member to this stem's specified list.
   * <p />
   * @param m     Add this member.
   * @param list  To this list.
   */
  public void listAddVal(GrouperMember m, String list) {
    this.listAddVal(this.s, this, m, list);
  }

  /**
   * Delete member from this stem's default list.
   * <p />
   * @param m   Delete this member.
   */
  public void listDelVal(GrouperMember m) {
    this.listDelVal(m, Grouper.DEF_LIST_TYPE);
  }

  /**
   * Delete member from this stem's specified list.
   * <p />
   * @param m     Delete this member.
   * @param list  From this list.
   */
  public void listDelVal(GrouperMember m, String list) {
    this.listDelVal(this.s, this, m, list);
  }

  /**
   * List members of this stem's default list.
   * <p />
   * @return  List of {@link GrouperList} objects.
   */
  public List listVals() {
    return this.listVals(Grouper.DEF_LIST_TYPE);
  }

  /**
   * List members of this stem's specified list.
   * <p />
   * @param list  Return members of this list.
   * @return  List of {@link GrouperList} objects.
   */
  public List listVals(String list) {
    List      vals  = new ArrayList();
    Iterator iter   = this.listVals(this.s, this, list).iterator();
    while (iter.hasNext()) {
      // Attach the current session to each list value
      GrouperList lv = (GrouperList) iter.next();
      lv.setSession(this.s);
      vals.add(lv);      
    }
    return vals;
  }

  /**
   * Effective members of this stem's default list.
   * <p />
   * @return  List of {@link GrouperList} objects.
   */
  public List listEffVals() {
    return this.listEffVals(Grouper.DEF_LIST_TYPE);
  }

  /**
   * Effective list members of this stem's specified list.
   * <p />
   * @param list  Return effective members of this list.
   * @return  List of {@link GrouperList} objects.
   */
  public List listEffVals(String list) {
    return this.listEffVals(this.s, this, list);
  }

  /**
   * Immediate list members of this stem's default list.
   * <p />
   * @return  List of {@link GrouperList} objects.
   */
  public List listImmVals() {
    return this.listImmVals(Grouper.DEF_LIST_TYPE);
  }

  /**
   * Immediate list members of this stem's specified list.
   * <p />
   * @param list  Return immediate members of this list.
   * @return  List of {@link GrouperList} objects.
   */
  public List listImmVals(String list) {
    return this.listImmVals(this.s, this, list);
  }

  /**
   * Retrieve the <i>modifySource</i> value.
   * <p  />
   * This attribute is not currently used.
   * @return <i>modifySource</i> value.
   */
  public String modifySource() {
    return this.getModifySource();
  }

  /**
   * Retrieve the <i>modifySubject</i> value.
   * <p  />
   * @return A {@link Subject} object.
   */
  public Subject modifySubject() {
    return GrouperMember.toSubject(this.s, this.getModifySubject());
  }

  /**
   * Retrieve the <i>modifyTime</i> value.
   * <p  />
   * @return <i>modifyTime</i> as a {@link Date} object.
   */
  public Date modifyTime() {
    return this.string2date(this.getModifyTime());
  }

  /**
   * Retrieve list of namespaces that are <b>immediate</b> children
   * of this stem.
   * <p />
   * @return  List of {@link GrouperStem} objects.
   */
  public List stems() {
    // TODO Shares a lot of common code with groups()
    String  qry   = "Group.key.child.stem.of.stem";
    List    vals  = new ArrayList();
    try {
      Query q = s.dbSess().session().getNamedQuery(qry);
      q.setString(0, this.name());
      try {
        Iterator iter = q.list().iterator();
        while (iter.hasNext()) {
          try {
            String key = (String) iter.next();
            GrouperStem s = (GrouperStem) Group.loadByKey(this.s, key);
            vals.add(s);
          } catch (InsufficientPrivilegeException e) {
            // Ignore
          }
        }
      } catch (HibernateException e) {
        throw new RuntimeException(
          "Error retrieving results for " + qry + ": " + e.getMessage()
        );
      }
    } catch (HibernateException e) {
      throw new RuntimeException(
        "Unable to get query " + qry + ": " + e.getMessage()
      );
    }
    return vals;
  }

  /**
   * Retrieve stem type.
   * <p />
   * @return String stem type.
   */
  public String type() {
    return this.type;
  }

  /*
   * PROTECTED CLASS METHODS
   */
  
  /*
   * @return true if the stem exists
   */
  protected static boolean exists(GrouperSession s, String stem) {
    boolean rv = false;
    if (stem.equals(Grouper.NS_ROOT)) {
      rv = true;
    } else {
      if (Group.findKeyByNameAndType(s, stem, Grouper.NS_TYPE) != null) {
        rv = true;
      }
    }
    return rv;
  }


  /*
   * PROTECTED INSTANCE METHODS
   */

  /*
   * Add new attribute.
   */
  protected void attributeAdd(GrouperAttribute attr) {
    GrouperAttribute.save(this.s, attr);
    this.attributes.put(attr.field(), attr);
  }

  /*
   * Delete an attribute
   */
  protected void attributeDel(GrouperAttribute attr) {
    attr.delete(this.s);
    this.attributes.remove(attr.field());
  }

  /*
   * Is this group initialized?
   */
  protected boolean initialized() {
    return this.initialized;
  }

  /*
   * Return namespace key.
   * <p />
   * @return Group key of the {@link GrouperGroup}
   */
  protected String key() {
    return this.getGroupKey();
  }

  /*
   * Flesh out the group a bit.
   */
  protected void load(GrouperSession s) {
    this.s = s; 
    this.attributes   = GrouperAttribute.attributes(s, this);
    this.type         = Grouper.NS_TYPE;
    this.initialized  = true;
  }

  /*
   * Set create* attributes.
   */
  protected void setCreated() {
    this.setCreateTime( this.now() );
    GrouperMember m = GrouperMember.load(s, s.subject());
    this.setCreateSubject(m.key());
  }

  /*
   * Set and save modify* attributes.
   */
  protected void setModified() {
    this.setModifyTime( this.now() );
    GrouperMember mem = GrouperMember.load(this.s, this.s.subject());
    this.setModifySubject( mem.key() );
    try {
      this.s.dbSess().session().update(this);
    } catch (HibernateException e) {
      throw new RuntimeException(
        "Error updating group: " + e.getMessage()
      );
    }
  }


  /*
   * PRIVATE INSTANCE METHODS
   */

  /*
   * Grant STEM to the stem's creator upon creation.
   */
  private void grantStemUponCreate() {
    // We need a root session
    GrouperSession  rs  = GrouperSession.getRootSession();
    // Subject that is creating group
    GrouperMember   m   = GrouperMember.load(this.s.subject() );
    boolean rv = rs.naming().grant(rs, this, m, Grouper.PRIV_STEM);
    if (!rv) {
      throw new RuntimeException("Error granting STEM to " + m);  
    } 
  }



  /*
   * HIBERNATE
   */

  protected String getGroupID() {
    return this.id;
  }

  protected void setGroupID(String id) {
    this.id = id;
  }

  protected String getGroupKey() {
    return this.key;
  }

  protected void setGroupKey(String key) {
    this.key = key;
  }

  protected String getCreateTime() {
    return this.createTime;
  }
 
  protected void setCreateTime(String createTime) {
    this.createTime = createTime;
  }
 
  protected String getCreateSubject() {
    return this.createSubject;
  }
 
  protected void setCreateSubject(String createSubject) {
    this.createSubject = createSubject;
  }
 
  protected String getCreateSource() {
    return this.createSource;
  }
 
  protected void setCreateSource(String createSource) {
    this.createSource = createSource;
  }
 
  protected String getModifyTime() {
    return this.modifyTime;
  }
 
  protected void setModifyTime(String modifyTime) {
    this.modifyTime = modifyTime;
  }
 
  protected String getModifySubject() {
    return this.modifySubject;
  }
 
  protected void setModifySubject(String modifySubject) {
    this.modifySubject = modifySubject;
  }
 
  protected String getModifySource() {
    return this.modifySource;
  }
 
  protected void setModifySource(String modifySource) {
    this.modifySource = modifySource;
  }

  protected String getGroupComment() {
    return this.groupComment;
  }

  protected void setGroupComment(String comment) {
    this.groupComment = comment;
  } 

}

