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


/** 
 * Class modeling a {@link Grouper} group.
 * <p />
 *
 * @author  blair christensen.
 * @version $Id: GrouperGroup.java,v 1.207 2005-04-13 18:15:11 blair Exp $
 */
public class GrouperGroup extends Group {

  /*
   * PRIVATE INSTANCE VARIABLES
   */
  private Map             attributes = new HashMap();
  private String          createTime;
  private String          createSubject;
  private String          createSource;
  private String          id;
  private boolean         initialized = false; 
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
    // Nothing
  }

  /*
   * Instantiate a group object
   */
  private GrouperGroup(
            GrouperSession s, String stem, String extn, String type
          )
  {
    this.s    = s; 
    this.type = type;
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

    this.setCreated();
  }


  /* 
   * PUBLIC CLASS METHODS
   */

  /**
   * Create a group.
   * <p />
   * @param   s     Session to create the group within.
   * @param   stem  Stem to create the group within.
   * @param   extn  Extension to assign to the group.
   * @return  A {@link GrouperGroup} object.
   */ 
  public static GrouperGroup create(
                               GrouperSession s, String stem, String extn
                             )
  {
    return GrouperGroup.create(s, stem, extn, Grouper.DEF_GROUP_TYPE);
  }

  /**
   * Create a group.
   * <p />
   * @param   s     Session to create the group within.
   * @param   stem  Stem to create the group within.
   * @param   extn  Extension to assign to the group.
   * @param   type  Type of group to create.
   * @return  A {@link GrouperGroup} object.
   */ 
  public static GrouperGroup create(
                               GrouperSession s, String stem, 
                               String extn, String type
                             )
  {
    if (type.equals(Grouper.NS_TYPE)) {
      throw new RuntimeException("Use GrouperStem for namespaces");
    }
    GrouperGroup g;
    if (!GrouperStem.exists(s, stem)) {
      throw new RuntimeException("Parent stem does not exist");
    }
    if (GrouperGroup.exists(s, stem, extn, type)) {
      throw new RuntimeException("Group already exists");
    }
    Group.subjectCanCreateAtRoot(s, stem);
    Group.subjectCanCreateGroup(s, stem);
    try {
      s.dbSess().txStart();
      g = new GrouperGroup(s, stem, extn, type);
      s.dbSess().session().save(g);
      g.grantAdminUponCreate();
      g.initialized = true;
      s.dbSess().txCommit();
      Grouper.log().groupAdd(s, g, Group.groupName(stem, extn), g.type());
    } catch (HibernateException e) {
      s.dbSess().txRollback();
      throw new RuntimeException("Error saving stem: " + e);
    } 
    return g;
  }

  /**
   * Retrieve a group by stem and extension.
   * <p />
   * @param   s     Session to load the group within.
   * @param   stem  Stem of the group to load.
   * @param   extn  Extension of the group to load.
   * @return  A {@link GrouperGroup} object.
   */
  public static GrouperGroup load(
                               GrouperSession s, String stem, String extn
                             )
  {
    return GrouperGroup.load(s, stem, extn, Grouper.DEF_GROUP_TYPE);
  }

  /**
   * Retrieve a group by stem, extension and type.
   * <p />
   * @param   s     Session to load the group within.
   * @param   stem  Stem of the group to load.
   * @param   extn  Extension of the group to load.
   * @param   type  The type of group to load.
   * @return  A {@link GrouperGroup} object.
   */
  public static GrouperGroup load(
                               GrouperSession s, String stem, 
                               String extn, String type
                             )
  {
    if (type.equals(Grouper.NS_TYPE)) {
      throw new RuntimeException("Use GrouperStem for namespaces");
    }
    String key = Group.findKeyByStemExtnType(s, stem, extn, type);
    if (key != null) {
      return (GrouperGroup) Group.loadByKey(s, key);
    }
    return null; 
  }

  /**
   * Retrieve a group by id.
   * <p />
   * @param   s           Session to load the group within.
   * @param   id          Group ID.
   * @return  A {@link Group} object.
   */
  public static Group loadByID(GrouperSession s, String id) {
    // TODO Should I check for NS_TYPE in returned group?
    return Group.loadByID(s, id);
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
    return GrouperGroup.loadByName(s, name, Grouper.DEF_GROUP_TYPE);
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
    if (type.equals(Grouper.NS_TYPE)) {
      throw new RuntimeException("Use GrouperStem for namespaces");
    }
    return (GrouperGroup) Group.loadByNameAndType(s, name, type);
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
    return (GrouperAttribute) this.attributes.get(attribute.toLowerCase());
  }

  /**
   * Set an attribute value.
   * <p />
   * If <i>value</i> is <i>null</i>, the attribute will be deleted.
   * <p /> 
   * @param   attribute   Set this attribute.
   * @param   value       To this value.
   */
  public void attribute(String attribute, String value) {
    this.attribute(this.s, this, attribute, value);
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
   * Retrieve group's public id.
   * <p />
   * @return Public id.
   */
  public String id() {
    return this.getGroupID();
  }

  /**
   * Add member to this group's default list.
   * <p />
   * @param m   Add this member.
   */
  public void listAddVal(GrouperMember m) {
    this.listAddVal(m, Grouper.DEF_LIST_TYPE);
  }

  /**
   * Add member to this group's specified list.
   * <p />
   * @param m     Add this member.
   * @param list  To this list.
   */
  public void listAddVal(GrouperMember m, String list) {
    this.listAddVal(this.s, this, m, list);
  }

  /**
   * Delete member from this group's default list.
   * <p />
   * @param m   Delete this member.
   */
  public void listDelVal(GrouperMember m) {
    this.listDelVal(m, Grouper.DEF_LIST_TYPE);
  }

  /**
   * Delete member from this group's specified list.
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
    return this.listVals(this.s, this, list);
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
   * Retrieve {@link GrouperMember} object for this 
   * {@link GrouperGroup}.
   * </p>
   * @return {@link GrouperMember} object
   */
  public GrouperMember toMember() {
    GrouperMember m = null;
    GrouperSession.validate(this.s);
    // FIXME Make sure I set this when loading as well...
    if (this.initialized == true) {
      m = GrouperMember.load(
            this.s, this.getGroupID(), "group"
          );
      if (m == null) {
        throw new RuntimeException("Error converting group to member");
      }
    } else {
      m = GrouperMember.create(s, this.getGroupID(), "group");
    }
    return m;
  }

  /*
   * Retrieve group type.
   * <p />
   * @return String group type.
   */
  public String type() {
    return this.type;
  }


  /*
   * PROTECTED CLASS METHODS
   */

  /**
   * Check for group existence.
   * <p />
   * @param s     Act within this session.
   * @param stem  Group stem.
   * @param extn  Group extension.
   * @param type  Group type.
   * @return true if group exists.
   */
  protected static boolean exists(
                             GrouperSession s, String stem, 
                             String extn, String type
                           )
  {
    boolean rv = false;
    String key = Group.findKeyByStemExtnType(s, stem, extn, type);
    if (key != null) {
      rv = true;
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
    GrouperAttribute.delete(this.s, attr);
    this.attributes.remove(attr.field());
  }

  /*
   * Is this group initialized?
   */
  protected boolean initialized() {
    return this.initialized;
  }

  /*
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
   * Flesh out the group a bit.
   */
  protected void load(GrouperSession s) {
    this.s = s;
    this.attributes   = GrouperAttribute.attributes(s, this);
    GrouperSchema gs  = GrouperSchema.load(s, this.key);
    if (gs != null) {
      this.type = gs.type();
    } else {
      throw new RuntimeException("Unable to attach type to group");
    }
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
      throw new RuntimeException("Error updating group: " + e);
    }
  }


  /*
   * PRIVATE INSTANCE METHODS
   */

  /*
  /*
   * Grant ADMIN to the group's creator upon creation.
   */
  private void grantAdminUponCreate() {
    // We need a root session
    Subject root = GrouperSubject.load(
                     Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE
                   );
    GrouperSession  rs  = GrouperSession.start(root);
    // Subject that is creating group
    GrouperMember   m   = GrouperMember.load(this.s.subject() );
    boolean rv = rs.access().grant(rs, this, m, Grouper.PRIV_ADMIN);
    rs.stop();
    if (!rv) {
      throw new RuntimeException("Error granting ADMIN to " + m);  
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

