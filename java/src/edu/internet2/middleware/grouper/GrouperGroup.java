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
 * @version $Id: GrouperGroup.java,v 1.196 2005-03-25 19:50:58 blair Exp $
 */
public class GrouperGroup extends Group {

  /*
   * PROTECTED INSTANCE VARIABLES
   */
  private GrouperSession  s;


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
    String key = Group.findKey(s, stem, extn, type);
    if (key != null) {
      GrouperGroup g = (GrouperGroup) Group.loadByKey(s, key);
      return g;
    }
    return null; 
  }


  /*
   * PUBLIC INSTANCE METHODS
   */

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
    String key = Group.findKey(s, stem, extn, type);
    if (key != null) {
      rv = true;
    }
    return rv;
  }


  /*
   * PROTECTED INSTANCE METHODS
   */

  /*
   * Is this group initialized?
   */
  protected boolean initialized() {
    return this.initialized;
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
   * ALLES IST GEFUCKT
   */


  /*
   * @return List of a group's attributes
   * TODO Why not just use the attrs HashMap?  
   * TODO Why static?
   */
  private static List _attributes(GrouperSession s, GrouperGroup g) {
    String  qry   = "GrouperAttribute.by.key";
    List    vals  = new ArrayList();
    try {
      Query q = s.dbSess().session().getNamedQuery(qry);
      q.setString(0, g.key());
      try {
        vals = q.list();
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
   * Attach attributes to a group.
   */
  private static void _attachAttributes(GrouperSession s, GrouperGroup g) {
    Iterator iter = GrouperGroup._attributes(s, g).iterator();
    while (iter.hasNext()) {
      GrouperAttribute attr = (GrouperAttribute) iter.next();
      g.attribute( attr.field(), attr );
    }
  }

  /**
   * Retrieve a group by public GUID.
   * <p />
   * @param   s           Session to load the group within.
   * @param   id          Group GUID.
   * @return  A {@link GrouperGroup} object.
   */
  public static Group loadByID(GrouperSession s, String id) {
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

  /*
   * Flesh out the group a bit.
   */
  protected void load(GrouperSession s) {
    this.s = s;
    this.initialized = true;
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
    String        qry   = "Group.by.id";
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
          GrouperGroup._attachAttributes(s, g);
          g.type( schema.type() );
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
        this.setModifyTime( this.now() );
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
      if (this._attrDel(attribute)) {
        // Now update this object and save it to persist the opattrs
        this.setModifyTime( this.now() );
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

  private boolean _attrDel(String field) {
    String  qry   = "GrouperAttribute.by.key.and.value";
    boolean rv    = false;
    List    vals  = new ArrayList();
    try {
      Query q = this.s.dbSess().session().getNamedQuery(qry);
      q.setString(0, this.key);
      q.setString(1, field);
      try {
        vals = q.list();
        if (vals.size() == 1) {
          try {
            GrouperAttribute attr = (GrouperAttribute) vals.get(0);
            this.s.dbSess().session().delete(attr);
            rv = true;
          } catch (HibernateException e) {
            throw new RuntimeException(
                        "Error deleting attribute: " + e
                      );
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
        this.setModifyTime( this.now() );
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

