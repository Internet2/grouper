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
 * Class modeling a {@link Grouper} stem.
 * <p />
 *
 * @author  blair christensen.
 * @version $Id: GrouperStem.java,v 1.19 2005-03-25 14:37:24 blair Exp $
 */
public class GrouperStem extends Group {

  /*
   * PROTECTED INSTANCE VARIABLES
   */
  protected GrouperSession  s;


  /*
   * PRIVATE INSTANCE VARIABLES
   */
  private HashMap         attributes = new HashMap();
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
    GrouperStem ns;
    if (!GrouperStem.exists(s, stem)) {
      throw new RuntimeException("Parent stem does not exist");
    }
    if (GrouperStem.exists(s, Group.groupName(stem, extn))) {
      throw new RuntimeException("Stem already exists");
    }
    Group.subjectCanCreateAtRoot(s, stem);
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
      throw new RuntimeException("Error saving stem: " + e);
    } 
    return ns;
  }

  /**
   * Delete a namespace.
   * <p />
   * @param s   Delete namespace within this session.
   * @param ns  Delete this namespace.
   */
  public static void delete(GrouperSession s, GrouperStem ns) {
    Group.subjectCanDelete(s, (Group) ns);
    try {
      s.dbSess().txStart();
      ns.revokeAllAccessPrivs();
      ns.revokeAllNamingPrivs();
      GrouperAttribute.delete(s, ns);
      GrouperSchema.delete(s, ns);
      s.dbSess().session().delete(ns);
      s.dbSess().txCommit();
    } catch (HibernateException e) {
      s.dbSess().txRollback();
      throw new RuntimeException("Error deleting group: " + e);
    }
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
                              GrouperSession s, 
                              String stem, String extension
                            )
  {
    String key = Group.findKey(s, stem, extension, Grouper.NS_TYPE);
    if (key != null) {
      GrouperStem ns = (GrouperStem) Group.loadByKey(s, key);
      return ns;
    }
    return null; 
  }


  /*
   * PUBLIC INSTANCE METHODS
   */

  /**
   * Retrieve a group attribute.
   * <p />
   * @param   attribute The attribute to retrieve.
   * @return  A {@link GrouperAttribute} object.
   */
  public GrouperAttribute attribute(String attribute) {
    return (GrouperAttribute) attributes.get(attribute);
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
    if (Group.subjectCanModListVal(this.s, this, list)) {
      GrouperList gl = new GrouperList(this, m, list);
      gl.load(this.s);
      GrouperList.validate(gl);
      if (GrouperList.exists(this.s, gl)) {
        throw new RuntimeException("List value already exists");
      }
      s.dbSess().txStart();
      try {
        this.listAddVal(gl); // Calculate mof and add vals
        if (this.initialized == true) {
          // Only update modify attrs if group is fully loaded
          this.setModified();
        }
        s.dbSess().txCommit(); 
        Grouper.log().groupListAdd(this.s, this, m);
      } catch (RuntimeException e) {
        s.dbSess().txRollback();
        throw new RuntimeException("Error adding list value: " + e);
      }
    }
  }

  /**
   * Delete member from this stem's default list.
   * <p />
   * FIXME NOT IMPLEMENTED
   * <p />
   * @param m   Delete this member
   * @return  true is list value deleted
   */
  public boolean listDelVal(GrouperMember m) {
    return this.listDelVal(m, Grouper.DEF_LIST_TYPE);
  }

  /**
   * Delete member from this stem's specified list.
   * <p />
   * FIXME NOT IMPLEMENTED
   * <p />
   * @param m     Delete this member
   * @param list  From this list     
   * @return  true if list value deleted.
   */
  public boolean listDelVal(GrouperMember m, String list) {
    return false;
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

  /**
   * Retrieve the value of the <i>name</i> attribute.
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
   * Retrieve group's type.
   * <p />
   * @return Type of group.
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
      if (Group.findKeyByName(s, stem, Grouper.NS_TYPE) != null) {
        rv = true;
      }
    }
    return rv;
  }


  /*
   * PROTECTED INSTANCE METHODS
   */

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
    this.initialized = true;
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
   * Add new attribute.
   */
  private void attributeAdd(GrouperAttribute attr) {
    this.attributes.put(attr.field(), attr);
    GrouperAttribute.save(s, attr);
  }

  /*
   * Grant STEM to the stem's creator upon creation.
   */
  private void grantStemUponCreate() {
    // We need a root session
    Subject root = GrouperSubject.load(
                     Grouper.config("member.system"), Grouper.DEF_SUBJ_TYPE
                   );
    GrouperSession  rs  = GrouperSession.start(root);
    // Subject that is creating group
    GrouperMember   m   = GrouperMember.load(this.s.subject() );
    boolean rv = rs.naming().grant(rs, this, m, Grouper.PRIV_STEM);
    rs.stop();
    if (!rv) {
      throw new RuntimeException("Error granting STEM to " + m);  
    } 
  }


  /*
   * Add immediate and effective list values.
   */
  private void listAddVal(GrouperList gl) {
    // Find the list values that we will need to add
    MemberOf mof  = new MemberOf(this.s);
    Iterator iter = mof.memberOf(gl).iterator();
    // Now add the list values
    while (iter.hasNext()) {
      GrouperList lv = (GrouperList) iter.next();
      lv.load(this.s);
      GrouperList.save(this.s, lv);
    }
  }

  /* 
   * Revoke all access privs attached to a group
   */
  private void revokeAllAccessPrivs() {
    /* 
     * TODO This could be prettier, especially if/when there are custom
     *      privs
     */
    if (!(
          this.s.access().revoke(this.s, this, Grouper.PRIV_OPTIN)   &&
          this.s.access().revoke(this.s, this, Grouper.PRIV_OPTOUT)  &&
          this.s.access().revoke(this.s, this, Grouper.PRIV_VIEW)    &&
          this.s.access().revoke(this.s, this, Grouper.PRIV_READ)    &&
          this.s.access().revoke(this.s, this, Grouper.PRIV_UPDATE)  &&
          this.s.access().revoke(this.s, this, Grouper.PRIV_ADMIN)
       ))
    {
      throw new RuntimeException("Error revoking access privileges");
    }
  }

  /* 
   * Revoke all naming privs attached to a group
   */
  private void revokeAllNamingPrivs() {
    // FIXME This is ugly 
    if (!(
          this.s.naming().revoke(this.s, this, Grouper.PRIV_STEM)    &&
          this.s.naming().revoke(this.s, this, Grouper.PRIV_CREATE) 
       ))
    {       
      throw new RuntimeException("Error revoking naming privileges");
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

