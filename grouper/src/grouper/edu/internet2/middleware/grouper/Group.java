/*
  Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2006 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper;

import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  java.io.Serializable;
import  java.util.*;
import  net.sf.hibernate.*;
import  org.apache.commons.lang.builder.*;
import  org.apache.commons.lang.time.*;
import  org.apache.commons.logging.*;


/** 
 * A group within the Groups Registry.
 * <p />
 * @author  blair christensen.
 * @version $Id: Group.java,v 1.55.2.10 2006-05-11 16:41:44 blair Exp $
 */
public class Group extends Owner implements Serializable {

  // Private Class Constants
  private static final EventLog EL        = new EventLog();
  private static final String   ERR_AM    = "unable to add member: ";
  private static final String   ERR_DG    = "unable to delete group: ";
  private static final String   ERR_GDL   = "members list does not exist: ";
  private static final String   ERR_G2M   = "could not convert group to member: ";
  private static final String   ERR_G2S   = "could not convert group to subject: ";
  private static final String   ERR_DM    = "unable to delete member: ";
  private static final String   ERR_FNF   = "field not found: ";
  private static final String   ERR_GA    = "attribute not found: ";
  private static final String   ERR_NODE  = "group without displayExtension";
  private static final String   ERR_NODN  = "group without displayName";
  private static final String   ERR_NOE   = "group without extension";
  private static final String   ERR_NON   = "group without name";
  private static final String   ERR_S2G   = "could not convert subject to group: ";
  private static final Log      LOG       = LogFactory.getLog(Group.class);
  private static final String   MSG_AM    = "add member ";
  private static final String   MSG_DG    = "group deleted: ";
  private static final String   MSG_DM    = "revoke member ";


  // Hibernate Properties
  private Set     group_attributes;
  private Set     group_types         = new LinkedHashSet(); 
  private Stem    parent_stem;


  // Transient Instance Variables
  private transient Member          as_member = null;
  private transient Subject         as_subj   = null;
  private transient String          attr_dn   = null;
  private transient String          attr_de   = null;
  private transient String          attr_e    = null;
  private transient String          attr_n    = null;
  private transient Map             attrs     = new HashMap();
  private transient Subject         creator;
  private transient Subject         modifier;
  private transient Set             types     = null;


  // Constructors

  /**
   * Default constructor for Hibernate.
   */
  public Group() {
    // Nothing
  }

  // Return a group with an attached session
  protected Group(
    GrouperSession s, Stem ns, String extn, String displayExtn
  ) 
    throws SchemaException
  {
    this.setSession(s);
    // Attach group type
    Set types = this.getGroup_types();
    types.add( GroupTypeFinder.find("base") );
    this.setGroup_types(types);
    // Set create information
    this._setCreated();
    // Assign UUID
    this.setUuid( GrouperUuid.getUuid() );
    // Set naming information
    Set attributes = new LinkedHashSet();
    this.attr_de  = displayExtn;
    this.attr_dn  = Stem.constructName(ns.getDisplayName(), displayExtn);
    this.attr_e   = extn;
    this.attr_n   = Stem.constructName(ns.getName(), extn);
    attributes.add(
      new Attribute(this, FieldFinder.find("displayExtension"), attr_de )
    );
    attributes.add(
      new Attribute(this, FieldFinder.find("displayName")     , attr_dn )
    );
    attributes.add(
      new Attribute(this, FieldFinder.find("extension")       , attr_e  )
    );
    attributes.add(
      new Attribute(this, FieldFinder.find("name")            , attr_n  )
    );
    this.setGroup_attributes(attributes);
  } // protected Group(s, ns, extn, displayExtn)
 

  // Public Class Methods
  /**
   * Retrieve default members {@link Field}.
   * <pre class="eg">
   * Field members = Group.getDefaultList();
   * </pre>
   * @return  The "members" {@link Field}
   */
  public static Field getDefaultList() {
    try {
      return FieldFinder.find(GrouperConfig.LIST);
    }
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String err = ERR_GDL + eS.getMessage();
      LOG.fatal(err);
      throw new RuntimeException(err);
    }
  } // public static Field getDefaultList()

 
  // Public Instance Methods //

  /**
   * Add a composite membership to this group.
   * <pre class="eg">
   * try {
   *   g.addCompositeMember(CompositeType.UNION, leftGroup, rightGroup);
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to add members 
   * }
   * catch (MemberAddException eMA) {
   *   // Unable to add composite membership
   * } 
   * </pre>
   * @param   type  {@link CompositeType}
   * @param   left  {@link Group} that is left factor of of composite membership.
   * @param   right {@link Group} that is right factor of composite membership.
   * @throws  InsufficientPrivilegeException
   * @throws  MemberAddException
   */
  public void addCompositeMember(CompositeType type, Group left, Group right)
    throws  InsufficientPrivilegeException,
            MemberAddException
  {
    try {
      StopWatch sw = new StopWatch();
      sw.start();
      Composite c = new Composite(this.getSession(), this, left, right, type);
      GroupValidator.canAddCompositeMember(this, c);
      Membership.addCompositeMembership(this.getSession(), this, c);
      sw.stop();
      // TODO EL.groupAddMember(this.getSession(), this.getName(), subj, f, sw);
    }
    catch (ModelException eM) {
      throw new MemberAddException(eM.getMessage());
    }
    catch (SchemaException eS) {
      throw new MemberAddException(eS.getMessage());
    }
  } // public void addCompositeMember(type, left, right)

  /**
   * Add a subject to this group.
   * <pre class="eg">
   * try {
   *   g.addMember(subj);
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to add members 
   * }
   * catch (MemberAddException eMA) {
   *   // Unable to add subject
   * } 
   * </pre>
   * @param   subj  Add this {@link Subject}
   * @throws  InsufficientPrivilegeException
   * @throws  MemberAddException
   */
  public void addMember(Subject subj) 
    throws  InsufficientPrivilegeException,
            MemberAddException
  {
    try {
      this.addMember(subj, getDefaultList());
    }
    catch (SchemaException eS) {
      throw new MemberAddException(eS.getMessage());
    }
  } // public void addMember(subj)

  /**
   * Add a subject to this group.
   * <pre class="eg">
   * try {
   *   g.addMember(subj, f);
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to add members 
   * }
   * catch (MemberAddException eMA) {
   *   // Unable to add member
   * } 
   * catch (SchemaException eS) {
   *   // Invalid Field
   * } 
   * </pre>
   * @param   subj  Add this {@link Subject}
   * @param   f     Add subject to this {@link Field}.
   * @throws  InsufficientPrivilegeException
   * @throws  MemberAddException
   * @throws  SchemaException
   */
  public void addMember(Subject subj, Field f)
    throws  InsufficientPrivilegeException,
            MemberAddException,
            SchemaException
  {
    StopWatch sw = new StopWatch();
    sw.start();
    GroupValidator.canAddMember(this, subj, f);
    Membership.addImmediateMembership(this.getSession(), this, subj, f);
    sw.stop();
    EL.groupAddMember(this.getSession(), this.getName(), subj, f, sw);
  } // public void addMember(subj, f)

  /**
   * Add an additional group type.
   * <pre class="eg">
   * try {
   *   GroupType custom = GroupTypeFinder.find("custom type");
   *   g.addType(custom);
   * }
   * catch (GroupModifyException eGM) {
   *   // Unable to add type 
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to add type
   * }
   * catch (SchemaException eS) {
   *   // Cannot add system-maintained types
   * }
   * </pre>
   * @param   type  The {@link GroupType} to add.
   * @throws  GroupModifyException if unable to add type.
   * @throws  InsufficientPrivilegeException if subject not root-like.
   * @throws  SchemaException if attempting to add a system group type.
   */
  public void addType(GroupType type) 
    throws  GroupModifyException,
            InsufficientPrivilegeException,
            SchemaException
  {
    Validator.canAddGroupType(this.getSession(), this, type);
    try {
      StopWatch sw    = new StopWatch();
      Session   hs    = HibernateHelper.getSession();
      Set       types = this.getGroup_types();
      types.add(type);
      this.setGroup_types(types);
      HibernateHelper.save(this);
      hs.close();
      sw.stop();
      EL.groupAddType(this.getSession(), this.getName(), type, sw);
    }
    catch (Exception e) {
      String msg = "unable to add type: " + type + ": " + e.getMessage();
      LOG.error(msg);
      throw new GroupModifyException(msg); 
    }
  } // public void addType(type)

  /**
   * Delete this group from the Groups Registry.
   * <pre class="eg">
   * try {
   *   g.delete();
   * }
   * catch (GroupDeleteException e0) {
   *   // Unable to delete group
   * }
   * catch (InsufficientPrivilegeException e1) {
   *   // Not privileged to delete this group
   * }
   * </pre>
   * @throws  GroupDeleteException
   * @throws  InsufficientPrivilegeException
   */
  public void delete() 
    throws  GroupDeleteException,
            InsufficientPrivilegeException
  {
    // TODO Refactor into smaller components
    StopWatch sw = new StopWatch();
    sw.start();
    GrouperSession.validate(this.getSession());
    String msg = "delete";
    GrouperLog.debug(LOG, this.getSession(), msg);
    try {
      PrivilegeResolver.getInstance().canADMIN(this.getSession(), this, this.getSession().getSubject());
      GrouperLog.debug(LOG, this.getSession(), msg + " canADMIN");
    }
    catch (InsufficientPrivilegeException eIP) {
      String err = msg + " cannot ADMIN: " + eIP.getMessage();
      GrouperLog.debug(LOG, this.getSession(), err);
      throw new InsufficientPrivilegeException(err);
    } 
    try {
      Set deletes = new LinkedHashSet();
      msg += " '" + this.getName() + "'";
      GrouperLog.debug(LOG, this.getSession(), msg);

      // And revoke all access privileges
      GrouperLog.debug(LOG, this.getSession(), msg + " revoking access privs");
      this._revokeAllAccessPrivs(msg);
      GrouperLog.debug(LOG, this.getSession(), msg + " access privs revoked");

      // And delete all memberships - as root
      GrouperLog.debug(
        LOG, this.getSession(), msg + " finding memberships to delete"
      );
      deletes.addAll( 
        Membership.deleteAllFieldType(
          GrouperSessionFinder.getRootSession(), this, FieldType.LIST
        )
      );
      GrouperLog.debug(
        LOG, this.getSession(), msg + " total membership to delete: " + deletes.size()
      );

      // Add the group last for good luck    
      deletes.add(this);

      // Preserve name for logging
      String name = this.getName();

      // And then commit changes to registry
      HibernateHelper.delete(deletes);
      // TODO info
      sw.stop();
      EL.groupDelete(this.getSession(), name, sw);
    }
    catch (InsufficientPrivilegeException eIP) {
      LOG.debug(ERR_DG + eIP.getMessage());
      throw new InsufficientPrivilegeException(eIP.getMessage());
    }
    catch (Exception e) {
      LOG.debug(ERR_DG + e.getMessage());
      throw new GroupDeleteException(e.getMessage());
    }
  } // public void delete()

  /**
   * Delete a group attribute.
   * <pre class="eg">
   * try {
   *   g.deleteAttribute(attribute);
   * }
   * catch (GroupModifyException e0) {
   *   // Unable to modify group
   * }
   * catch (InsufficientPrivilegeException e1) {
   *   // Not privileged to delete this attribute
   * }
   * </pre>
   * @param   attr  Delete this attribute.
   * @throws  AttributeNotFoundException
   * @throws  GroupModifyException
   * @throws  InsufficientPrivilegeException
   */
  public void deleteAttribute(String attr) 
    throws  AttributeNotFoundException,
            GroupModifyException, 
            InsufficientPrivilegeException
  {
    try {
      StopWatch sw = new StopWatch();
      sw.start();
      Field f = FieldFinder.find(attr);
      GroupValidator.canDelAttribute(this, f);

      // TODO I'm not comfortable with this code
      Set       saves   = new LinkedHashSet();
      Set       deletes = new LinkedHashSet();
      Set       attrs   = new LinkedHashSet();
      boolean   found   = false;        // so we know if there was actually anything to delete
      String    val     = new String(); // for logging purposes
      Iterator  iter    = this.getGroup_attributes().iterator();
      while (iter.hasNext()) {
        Attribute a = (Attribute) iter.next();
        if (a.getField().equals(f)) {
          val = a.getValue();
          deletes.add(a); // deleting
          found = true;
        }
        else {
          attrs.add(a); // preserving
        }
      }
      if (found == true) {
        this.setModified();
        this.setGroup_attributes(attrs);
        saves.add(this);
        HibernateHelper.saveAndDelete(saves, deletes);
      }
      else {
        throw new AttributeNotFoundException();
      }
      sw.stop();
      EL.groupDelAttr(this.getSession(), this.getName(), attr, val, sw);
    }
    catch (SchemaException eS) {
      throw new AttributeNotFoundException(eS.getMessage());
    }
    catch (Exception e) {
      throw new GroupModifyException(e.getMessage());
    }
  } // public void deleteAttribute(attr)

  /**
   * Delete a composite membership from this group.
   * <pre class="eg">
   * try {
   *   g.deleteCompositeMember();
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to delete members
   * }
   * catch (MemberDeleteException eMD) {
   *   // Unable to delete composite membership
   * } 
   * </pre>
   * @throws  InsufficientPrivilegeException
   * @throws  MemberDeleteException
   */
  public void deleteCompositeMember()
    throws  InsufficientPrivilegeException,
            MemberDeleteException
  {
    try {
      StopWatch sw  = new StopWatch();
      sw.start();
      GroupValidator.canDelCompositeMember(this);
      Composite c   = CompositeFinder.isOwner(this);
      Membership.delCompositeMembership(this.getSession(), this, c);
      sw.stop();
      // TODO EL.groupAddMember(this.getSession(), this.getName(), subj, f, sw);
    }
    catch (CompositeNotFoundException eCNF) {
      throw new MemberDeleteException(eCNF.getMessage());
    }
    catch (ModelException eM) {
      throw new MemberDeleteException(eM.getMessage());
    }
    catch (SchemaException eS) {
      throw new MemberDeleteException(eS.getMessage());
    }
  } // public void deleteCompositeMember()

  /** 
   * Delete a subject from this group.
   * <pre class="eg">
   * try {
   *   g.deleteMember(subj);
   * } 
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to delete this subject
   * }
   * catch (MemberDeleteException eMD) {
   *   // Unable to delete subject
   * }
   * </pre>
   * @param   subj  Delete this {@link Subject}
   * @throws  InsufficientPrivilegeException
   * @throws  MemberDeleteException
   * @throws  SchemaException
   */
  public void deleteMember(Subject subj)
    throws  InsufficientPrivilegeException,
            MemberDeleteException
  {
    try {
      this.deleteMember(subj, getDefaultList());
    }
    catch (SchemaException eS) {
      throw new MemberDeleteException(eS.getMessage());
    }
  } // public void deleteMember(subj)

  /** 
   * Delete a subject from this group.
   * <pre class="eg">
   * try {
   *   g.deleteMember(m, f);
   * } 
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to delete this subject
   * }
   * catch (MemberDeleteException eMD) {
   *   // Unable to delete subject
   * }
   * </pre>
   * @param   subj  Delete this {@link Subject}.
   * @param   f     Delete subject from this {@link Field}.
   * @throws  InsufficientPrivilegeException
   * @throws  MemberDeleteException
   * @throws  SchemaException
   */
  public void deleteMember(Subject subj, Field f) 
    throws  InsufficientPrivilegeException, 
            MemberDeleteException,
            SchemaException
  {
    StopWatch sw = new StopWatch();
    sw.start();
    GroupValidator.canDelMember(this, subj, f);
    Membership.delImmediateMembership(this.getSession(), this, subj, f);
    sw.stop();
    EL.groupDelMember(this.getSession(), this.getName(), subj, f, sw);
  } // public void deleteMember(subj, f)

  /**
   * Delete a group type.
   * <pre class="eg">
   * try {
   *   GroupType custom = GroupTypeFinder.find("custom type");
   *   g.deleteType(custom);
   * }
   * catch (GroupModifyException eGM) {
   *   // Unable to delete type 
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to add type
   * }
   * catch (SchemaException eS) {
   *   // Cannot delete system-maintained types
   * }
   * </pre>
   * @param   type  The {@link GroupType} to add.
   * @throws  GroupModifyException if unable to delete type.
   * @throws  InsufficientPrivilegeException if subject not root-like.
   * @throws  SchemaException if attempting to delete a system group type.
   */
  public void deleteType(GroupType type) 
    throws  GroupModifyException,
            InsufficientPrivilegeException,
            SchemaException
  {
    Validator.canDeleteGroupType(this.getSession(), this, type);
    try {
      StopWatch sw    = new StopWatch();
      Session   hs    = HibernateHelper.getSession();
      Set       types = this.getGroup_types();
      types.remove(type);
      this.setGroup_types(types);
      HibernateHelper.save(this);
      hs.close();
      sw.stop();
      EL.groupDelType(this.getSession(), this.getName(), type, sw);
    }
    catch (Exception e) {
      String msg = "unable to delete type: " + type + ": " + e.getMessage();
      LOG.error(msg);
      throw new GroupModifyException(msg); 
    }
  } // public void deleteType(type)

  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Group)) {
      return false;
    }
    Group otherGroup = (Group) other;
    return new EqualsBuilder()
      .append(this.getCreator_id()  , otherGroup.getCreator_id()  )
      .append(this.getCreate_time() , otherGroup.getCreate_time() )
      .append(this.getUuid()        , otherGroup.getUuid()        )
      .isEquals();
  } // public boolean equals(other)

  /**
   * Get subjects with the ADMIN privilege on this group.
   * <pre class="eg">
   * Set admins = g.getAdmins();
   * </pre>
   * @return  Set of subjects with ADMIN
   */
  public Set getAdmins() {
    try {
      return PrivilegeResolver.getInstance().getSubjectsWithPriv(
        this.getSession(), this, AccessPrivilege.ADMIN
      );
    }
    catch (SchemaException eS) {
      String err = ERR_FNF + AccessPrivilege.ADMIN;
      LOG.fatal(err);
      throw new RuntimeException(err);
    }
  } // public Set getAdmins()

  /**
   * Get attribute value.
   * <pre class="eg">
   * try {
   *   String value = g.getAttribute(attribute);
   * }
   * catch (AttributeNotFoundException e) {
   *   // Group doesn't have attribute
   * }
   * </pre>
   * @param   attr  Get value of this attribute.
   * @return  Attribute value.
   * @throws  AttributeNotFoundException
   */
  public String getAttribute(String attr) 
    throws  AttributeNotFoundException
  {
    String val = new String();
    try {
      Field f = FieldFinder.find(attr);
      if (!f.getType().equals(FieldType.ATTRIBUTE)) {
        String err = "not an attribute: " + attr;
        throw new SchemaException(err);
      }
      if (!this.hasType( f.getGroupType() ) ) {
        String err = "does not have group type: " + f.getGroupType().toString();
        throw new SchemaException(err);
      }
    }
    catch (SchemaException eS) {
      throw new AttributeNotFoundException(eS.getMessage());
    }
    try {
      Map attrs = this._getAttributes();
      if (attrs.containsKey(attr)) {
        Attribute a = (Attribute) attrs.get(attr);
        PrivilegeResolver.getInstance().canPrivDispatch(
          this.getSession(), this, this.getSession().getSubject(), a.getField().getReadPriv()
        );
        val = a.getValue();
      }
    }
    catch (InsufficientPrivilegeException eIP) {
      // Ignore
    }
    catch (SchemaException eS) {
      // Ignore
    }
    return val;
  } // public String getAttribute(attr)

  /**
   * Get all attributes and values.
   * <pre class="eg">
   * Map attributes = g.getAttributes();
   * </pre>
   * @return  A map of attributes and values.
   */
  public Map getAttributes() {
    // TODO Cache results
    Map       filtered  = new HashMap();
    Iterator  iter      = this._getAttributes().values().iterator();
    while (iter.hasNext()) {
      Attribute attr = (Attribute) iter.next();
      try {
        // TODO Does this actually work?
        PrivilegeResolver.getInstance().canPrivDispatch(
          this.getSession(), this, this.getSession().getSubject(), attr.getField().getReadPriv()
        );
        filtered.put( attr.getField().getName(), attr.getValue() );
      }
      catch (Exception e) {
        // Nothing
      }
    }
    return filtered;
  } // public Map getAttributes()

  /**
   * Get (optional and questionable) create source for this group.
   * <pre class="eg">
   * // Get create source
`  * String source = g.getCreateSource();
   * </pre>
   * @return  Create source for this group.
   */
  public String getCreateSource() {
    String source = this.getCreate_source();
    if (source == null) {
      source = new String();
    }
    return source;
  } // public String getCreateSource()
  
  /**
   * Get subject that created this group.
   * <pre class="eg">
   * // Get creator of this group.
   * try {
   *   Subject creator = g.getCreateSubject();
   * }
   * catch (SubjectNotFoundException e) {
   *   // Couldn't find subject
   * }
   * </pre>
   * @return  {@link Subject} that created this group.
   * @throws  SubjectNotFoundException
   */
  public Subject getCreateSubject() 
    throws SubjectNotFoundException
  {
    if (this.creator == null) {
      this.creator = this.getCreator_id().getSubject();
    }
    return this.creator; 
  } // public Subject getCreateSubject() 
  
  /**
   * Get creation time for this group.
   * <pre class="eg">
   * // Get create time.
   * Date created = g.getCreateTime();
   * </pre>
   * @return  {@link Date} that this group was created.
   */
  public Date getCreateTime() {
    return new Date(this.getCreate_time());
  } // public Date getCreateTime()

  /**
   * Get group description.
   * <pre class="eg">
   * String description = g.getDescription();
   * </pre>
   * @return  Group description.
   */
  public String getDescription() {
    try {
      return (String) this.getAttribute("description");
    }
    catch (AttributeNotFoundException eANF) {
      // Lack of a description is acceptable
      return new String();
    }
  } // public String getDescription()

  /**
   * Get group displayExtension.
   * <pre class="eg">
   * String displayExtn = g.getDisplayExtension();
   * </pre>
   * @return  Gruop displayExtension.
   */
  public String getDisplayExtension() {
    GrouperSession.validate(s);
    String msg = "getDisplayExtension";
    if (attr_de == null) {
      try {
        GrouperLog.debug(LOG, this.getSession(), msg + " fetching");
        attr_de = (String) this.getAttribute("displayExtension");
        GrouperLog.debug(LOG, this.getSession(), msg + " fetched");
      }
      catch (AttributeNotFoundException eANF) {
        GrouperLog.fatal(LOG, this.getSession(), ERR_NODE);
        throw new RuntimeException(ERR_NODE);
      }
    }
    return attr_de;
  } // public String getDisplayExtension()

  /**
   * Get group displayName.
   * <pre class="eg">
   * String displayName = g.getDisplayName();
   * </pre>
   * @return  Group displayName.
   */
  public String getDisplayName() {
    GrouperSession.validate(s);
    String msg = "getDisplayName";
    if (attr_dn == null) {
      try {
        GrouperLog.debug(LOG, this.getSession(), msg + " fetching");
        attr_dn = (String) this.getAttribute("displayName");
        GrouperLog.debug(LOG, this.getSession(), msg + " fetched");
      }
      catch (AttributeNotFoundException eANF) {
        GrouperLog.fatal(LOG, this.getSession(), ERR_NODN);
        throw new RuntimeException(ERR_NODN);
      }
    }
    return attr_dn;
  } // public String getDisplayName()

  /**
   * Get effective members of this group.
   * <pre class="eg">
   * Set effectives = g.getEffectiveMembers();
   * </pre>
   * @return  A set of {@link Member} objects.
   */
  public Set getEffectiveMembers() {
    try {
      return this.getEffectiveMembers(getDefaultList());
    }
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String err = ERR_GDL + eS.getMessage();
      LOG.fatal(err);
      throw new RuntimeException(err);
    }
  } // public Set getEffectiveMembership()


  /**
   * Get effective members of this group.
   * <pre class="eg">
   * Set effectives = g.getEffectiveMembers(f);
   * </pre>
   * @param   f Get members in this list field.
   * @return  A set of {@link Member} objects.
   * @throws  SchemaException
   */
  public Set getEffectiveMembers(Field f) 
    throws  SchemaException
  {
    return MembershipFinder.findEffectiveMembers(this.getSession(), this, f);
  }  // public Set getEffectiveMembers(f)

  /**
   * Get effective memberships of this group.
   * <pre class="eg">
   * Set effectives = g.getEffectiveMemberships();
   * </pre>
   * @return  A set of {@link Membership} objects.
   */
  public Set getEffectiveMemberships() {
    try {
      return this.getEffectiveMemberships(getDefaultList());
    }
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String err = ERR_GDL + eS.getMessage();
      LOG.fatal(err);
      throw new RuntimeException(err);
    }
  } // public Set getEffectiveMembership()

  /**
   * Get memberships of this group.
   * <pre class="eg">
   * Set memberships = g.getMemberships(f);
   * </pre>
   * @param   f Get memberships in this list field.
   * @return  A set of {@link Membership} objects.
   * @throws  SchemaException
   */
  public Set getEffectiveMemberships(Field f) 
    throws  SchemaException
  {
    return MembershipFinder.findEffectiveMemberships(this.getSession(), this, f);
  } // public Set getEffectiveMemberships(f)

  /**
   * Get group extension.
   * <pre class="eg">
   * String extension = g.getExtension();
   * </pre>
   * @return  Group extension.
   */
  public String getExtension() {
    GrouperSession.validate(s);
    String msg = "getExtension";
    if (attr_e == null) {
      try {
        GrouperLog.debug(LOG, this.getSession(), msg + " fetching");
        attr_e = (String) this.getAttribute("extension");
        GrouperLog.debug(LOG, this.getSession(), msg + " fetched");
      }
      catch (AttributeNotFoundException eANF) {
        GrouperLog.fatal(LOG, this.getSession(), ERR_NOE);
        throw new RuntimeException(ERR_NOE);
      }
    }
    return attr_e;
  } // public String getExtension()
 
  /**
   * Get immediate members of this group.
   * <pre class="eg">
   * Set immediates = g.getImmediateMembers();
   * </pre>
   * @return  A set of {@link Member} objects.
   */
  public Set getImmediateMembers() {
    try {
      return this.getImmediateMembers(getDefaultList());
    }
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String err = ERR_GDL + eS.getMessage();
      LOG.fatal(err);
      throw new RuntimeException(err);
    }
  } // public Set getImmediateMembers()

  /**
   * Get immediate members of this group.
   * <pre class="eg">
   * Set immediates = g.getImmediateMembers(f);
   * </pre>
   * @param   f Get members in this list field.
   * @return  A set of {@link Member} objects.
   * @throws  SchemaException
   */
  public Set getImmediateMembers(Field f) 
    throws  SchemaException
  {
    return MembershipFinder.findImmediateMembers(this.getSession(), this, f);
  } // public Set getImmediateMembers(f)

  /**
   * Get immediate memberships of this group.
   * <pre class="eg">
   * Set immediates = g.getImmediateMemberships();
   * </pre>
   * @return  A set of {@link Membership} objects.
   */
  public Set getImmediateMemberships() {
    try {
      return this.getImmediateMemberships(getDefaultList());
    }
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String err = ERR_GDL + eS.getMessage();
      LOG.fatal(err);
      throw new RuntimeException(err);
    }
  } // public Set getImmediateMemberships()

  /**
   * Get immediate memberships of this group.
   * <pre class="eg">
   * Set immediates = g.getImmediateMemberships(f);
   * </pre>
   * @param   f Get memberships in this list field.
   * @return  A set of {@link Membership} objects.
   * @throws  SchemaException
   */
  public Set getImmediateMemberships(Field f) 
    throws  SchemaException
  {
    GrouperSession.validate(this.getSession());
    return MembershipFinder.findImmediateMemberships(this.getSession(), this, f);
  } // public Set getImmediateMemberships(f)

  /**
   * Get members of this group.
   * <pre class="eg">
   * Set members = g.getMembers();
   * </pre>
   * @return  A set of {@link Member} objects.
   */
  public Set getMembers() {
    try {
      return this.getMembers(getDefaultList());
    }
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String err = ERR_GDL + eS.getMessage();
      LOG.fatal(err);
      throw new RuntimeException(err);
    }
  } // public Set getMembers()

  /**
   * Get members of this group.
   * <pre class="eg">
   * Set members = g.getMembers(f);
   * </pre>
   * @param   f Get members in this list field.
   * @return  A set of {@link Member} objects.
   * @throws  SchemaException
   */
  public Set getMembers(Field f) 
    throws  SchemaException
  {
    return MembershipFinder.findMembers(this.getSession(), this, f);
  } // public Set getMembers(f)

  /**
   * Get memberships of this group.
   * <pre class="eg">
   * Set memberships = g.getMemberships();
   * </pre>
   * @return  A set of {@link Membership} objects.
   */
  public Set getMemberships() {
    try {
      return this.getMemberships(getDefaultList());
    }
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String err = ERR_GDL + eS.getMessage();
      LOG.fatal(err);
      throw new RuntimeException(err);
    }
  } // public Set getMemberships()

  /**
   * Get memberships of this group.
   * <pre class="eg">
   * Set memberships = g.getMemberships(f);
   * </pre>
   * @param   f Get memberships in this list field.
   * @return  A set of {@link Membership} objects.
   * @throws  SchemaException
   */
  public Set getMemberships(Field f) 
    throws  SchemaException
  {
    return MembershipFinder.findMemberships(this.getSession(), this, f);
  } // public Set getMemberships(f)

  /**
   * Get (optional and questionable) modify source for this group.
   * <pre class="eg">
   * // Get modify source
`  * String source = g.getModifySource();
   * </pre>
   * @return  Modify source for this group.
   */
  public String getModifySource() {
    String source = this.getModify_source();
    if (source == null) {
      source = new String();
    }
    return source;
  } // public String getModifySource()
  
  /**
   * Get subject that last modified this group.
   * <pre class="eg">
   * // Get last modifier of this group.
   * try {
   *   Subject modifier = g.getModifySubject();
   * }
   * catch (SubjectNotFoundException e) {
   *   // Couldn't find subject
   * }
   * </pre>
   * @return  {@link Subject} that last modified this group.
   * @throws  SubjectNotFoundException
   */
  public Subject getModifySubject() 
    throws SubjectNotFoundException
  {
    if (this.modifier == null) {
      Member m = this.getModifier_id();
      if (m == null) {
        throw new SubjectNotFoundException(
          "group has not been modified"
        );
      }
      this.modifier = m.getSubject();
    }
    return this.modifier; 
  } // public Subject getModifySubject()
  
  /**
   * Get last modified time for this group.
   * <pre class="eg">
   * // Get last modified time.
   * Date modified = g.getModifyTime();
   * </pre>
   * @return  {@link Date} that this group was last modified.
   */
  public Date getModifyTime() {
    return new Date(this.getModify_time());
  }

  /**
   * Get group name.
   * <pre class="eg">
   * String name = g.getName();
   * </pre>
   * @return  Group name.
   */
  public String getName() {
    GrouperSession.validate(s);
    String msg = "getName";
    if (attr_n == null) {
      try {
        GrouperLog.debug(LOG, this.getSession(), msg + " fetching");
        attr_n = (String) this.getAttribute("name");
        GrouperLog.debug(LOG, this.getSession(), msg + " fetched");
      }
      catch (AttributeNotFoundException eANF) {
        GrouperLog.fatal(LOG, this.getSession(), ERR_NON);
        throw new RuntimeException(ERR_NON);
      }
    }
    return attr_n;
  } // public String getName()

  /**
   * Get subjects with the OPTIN privilege on this group.
   * <pre class="eg">
   * Set optins = g.getOptins();
   * </pre>
   * @return  Set of subjects with OPTIN
   */
  public Set getOptins() {
    try {
      return PrivilegeResolver.getInstance().getSubjectsWithPriv(
        this.getSession(), this, AccessPrivilege.OPTIN
      );
    } 
    catch (SchemaException eS) { 
      String err = ERR_FNF + AccessPrivilege.OPTIN;
      LOG.fatal(err);
      throw new RuntimeException(err);
    }
  } // public Set getOptins()

  /**
   * Get subjects with the OPTOUT privilege on this group.
   * <pre class="eg">
   * Set admins = g.getOptouts();
   * </pre>
   * @return  Set of subjects with OPTOUT
   */
  public Set getOptouts() {
    try {
      return PrivilegeResolver.getInstance().getSubjectsWithPriv(
        this.getSession(), this, AccessPrivilege.OPTOUT
      );
    } 
    catch (SchemaException eS) { 
      String err = ERR_FNF + AccessPrivilege.OPTOUT;
      LOG.fatal(err);
      throw new RuntimeException(err);
    }
  } // public Set getOptouts()

  /**
   * Get parent stem.
   * <pre class="eg">
   * Stem parent = g.getParentStem();
   * </pre>
   * @return  Parent {@link Stem}.
   */
  public Stem getParentStem() {
    Stem parent = this.getParent_stem();
    parent.setSession(this.getSession());
    return parent;
  } // public Stem getParentStem()

  /**
   * Get privileges that the specified subject has on this group.
   * <pre class="eg">
   * Set privs = g.getPrivs(subj);
   * </pre>
   * @param   subj  Get privileges for this subject.
   * @return  Set of {@link AccessPrivilege} objects.
   */
  public Set getPrivs(Subject subj) {
    return PrivilegeResolver.getInstance().getPrivs(
      this.getSession(), this, subj
    );
  } // public Set getPrivs(subj)


  /**
   * Get subjects with the READ privilege on this group.
   * <pre class="eg">
   * Set readers = g.getReaders();
   * </pre>
   * @return  Set of subjects with READ
   */
  public Set getReaders() {
    try {
      return PrivilegeResolver.getInstance().getSubjectsWithPriv(
        this.getSession(), this, AccessPrivilege.READ
      );
    } 
    catch (SchemaException eS) { 
      String err = ERR_FNF + AccessPrivilege.READ;
      LOG.fatal(err);
      throw new RuntimeException(err);
    }
  } // public Set getReaders()

  /**
   * Get group types for this group.
   * <pre class="eg">
   * Set types = g.getTypes();
   * </pre>
   * @return  Set of group types.
   */
  public Set getTypes() {
    return this.getGroup_types();
  } // public Set getTypes()

  /**
   * Get subjects with the UPDATE privilege on this group.
   * <pre class="eg">
   * Set updaters = g.getUpdaters();
   * </pre>
   * @return  Set of subjects with UPDATE
   */
  public Set getUpdaters() {
    try {
      return PrivilegeResolver.getInstance().getSubjectsWithPriv(
        this.getSession(), this, AccessPrivilege.UPDATE
      );
    } 
    catch (SchemaException eS) { 
      String err = ERR_FNF + AccessPrivilege.UPDATE;
      LOG.fatal(err);
      throw new RuntimeException(err);
    }
  } // public set getUpdateres()

  /**
   * Get subjects with the VIEW privilege on this group.
   * <pre class="eg">
   * Set viewers = g.getViewers();
   * </pre>
   * @return  Set of subjects with VIEW
   */
  public Set getViewers() {
    try {
      return PrivilegeResolver.getInstance().getSubjectsWithPriv(
        this.getSession(), this, AccessPrivilege.VIEW
      );
    } 
    catch (SchemaException eS) { 
      String err = ERR_FNF + AccessPrivilege.VIEW;
      LOG.fatal(err);
      throw new RuntimeException(err);
    }
  } // public Set getViewers()

  /**
   * Grant privilege to a subject on this group.
   * <pre class="eg">
   * try {
   *   g.grantPriv(subj, AccessPrivilege.ADMIN);
   * }
   * catch (GrantPrivilegeException e0) {
   *   // Not privileged to grant this privilege
   * }
   * catch (InsufficientPrivilegeException e1) {
   *   // Unable to grant this privilege
   * }
   * </pre>
   * @param   subj  Grant privilege to this subject.
   * @param   priv  Grant this privilege.
   * @throws  GrantPrivilegeException
   * @throws  InsufficientPrivilegeException
   * @throws  SchemaException
   */
  public void grantPriv(Subject subj, Privilege priv)
    throws  GrantPrivilegeException,
            InsufficientPrivilegeException,
            SchemaException
  {
    StopWatch sw = new StopWatch();
    sw.start();
    PrivilegeResolver.getInstance().grantPriv(
      this.getSession(), this, subj, priv
    );
    sw.stop();
    EL.groupGrantPriv(this.getSession(), this.getName(), subj, priv, sw);
  }  // public void grantPriv(subj, priv)

  /**
   * Check whether the subject has ADMIN on this group.
   * <pre class="eg">
   * if (g.hasAdmin(subj)) {
   *   // Has ADMIN
   * }
   * else {
   *   // Does not have ADMIN
   * }
   * </pre>
   * @param   subj  Check this subject.
   * @return  Boolean true if subject has ADMIN.
   */
  public boolean hasAdmin(Subject subj) {
    return PrivilegeResolver.getInstance().hasPriv(
      this.getSession(), this, subj, AccessPrivilege.ADMIN
    );
  } // public boolean hasAdmin(subj)

  /**
   * Does this {@link Group} have a {@link Composite} membership.
   * <pre class="eg">
   * if (g.hasComposite()) {
   *   // this group has a composite membership
   * }
   * </pre>
   * @return  Boolean true if group has a composite membership.
   */
  public boolean hasComposite() {
    try {
      Composite c = CompositeFinder.isOwner(this);
      return true;
    }
    catch (CompositeNotFoundException eCNF) {
      return false;
    }
  } // public boolean hasComposite()

  /**
   * Check whether the subject is an effective member of this group.
   * <pre class="eg">
   * if (g.hasEffectiveMember(subj)) {
   *   // Subject is an effective member of this group
   * }
   * else {
   *   // Subject is not an effective member of this group
   * } 
   * </pre>
   * @param   subj  Check this subject.
   * @return  Boolean true if subject belongs to this group.
   */
  public boolean hasEffectiveMember(Subject subj) {
    try {
      return this.hasEffectiveMember(subj, getDefaultList());
    } 
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String err = ERR_GDL + eS.getMessage();
      LOG.fatal(err);
      throw new RuntimeException(err);
    }
  } // public boolean hasEffectiveMember(Subject subj)

  /**
   * Check whether the subject is an effective member of this group.
   * <pre class="eg">
   * if (g.hasEffectiveMember(subj, f)) {
   *   // Subject is an effective member of this group
   * }
   * else {
   *   // Subject is not an effective member of this group
   * } 
   * </pre>
   * @param   subj  Check this subject.
   * @param   f     Check for membership in this list field.
   * @return  Boolean true if subject belongs to this group.
   * @throws  SchemaException
   */
  public boolean hasEffectiveMember(Subject subj, Field f) 
    throws  SchemaException
  {
    boolean rv  = false;
    String  msg = "hasEffectiveMember " + SubjectHelper.getPretty(subj) + " '" 
      + f.getName() + "': ";
    try {
      Member m = MemberFinder.findBySubject(this.getSession(), subj);
      rv = m.isEffectiveMember(this, f);
    }
    catch (MemberNotFoundException eMNF) {
      // TODO Fail silently?
      GrouperLog.debug(LOG, this.getSession(), msg + eMNF.getMessage());
    }
    GrouperLog.debug(LOG, this.getSession(), msg + rv);
    return rv;
  } // public boolean hasEffectiveMember(subj, f)

  /**
   * Check whether the subject is an immediate member of this group.
   * <pre class="eg">
   * if (g.hasImmediateMember(subj)) {
   *   // Subject is an immediate member of this group
   * }
   * else {
   *   // Subject is not a immediate member of this group
   * } 
   * </pre>
   * @param   subj  Check this subject.
   * @return  Boolean true if subject belongs to this group.
   */
  public boolean hasImmediateMember(Subject subj) {
    try {
      return this.hasImmediateMember(subj, getDefaultList());
    }
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String err = ERR_GDL + eS.getMessage();
      LOG.fatal(err);
      throw new RuntimeException(err);
    }
  } // public boolean hasImmediateMember(subj)

  /**
   * Check whether the subject is an immediate member of this group.
   * <pre class="eg">
   * if (g.hasImmediateMember(subj, f)) {
   *   // Subject is an immediate member of this group
   * }
   * else {
   *   // Subject is not a immediate member of this group
   * } 
   * </pre>
   * @param   subj  Check this subject.
   * @param   f     Check for membership in this list field.
   * @return  Boolean true if subject belongs to this group.
   * @throws  SchemaException
   */
  public boolean hasImmediateMember(Subject subj, Field f) 
    throws  SchemaException
  {
    boolean rv  = false;
    String  msg = "hasImmediateMember " + SubjectHelper.getPretty(subj) + " '" 
      + f.getName() + "': ";
    try {
      Member m = MemberFinder.findBySubject(this.getSession(), subj);
      rv = m.isImmediateMember(this, f);
    }
    catch (MemberNotFoundException eMNF) {
      // TODO Fail silently?
      GrouperLog.debug(LOG, this.getSession(), msg + eMNF.getMessage());
    }
    GrouperLog.debug(LOG, this.getSession(), msg + rv);
    return rv;
  } // public boolean hasImmediateMember(subj, f)

  public int hashCode() {
    return new HashCodeBuilder()
      .append(this.getCreator_id()  )
      .append(this.getCreate_time() )
      .append(this.getUuid()        )
      .toHashCode();
  }

  /**
   * Check whether the subject has OPTIN on this group.
   * <pre class="eg">
   * if (g.hasOptin(subj)) {
   *   // Has OPTIN
   * }
   * else {
   *   // Does not have OPTIN
   * }
   * </pre>
   * @param   subj  Check this subject.
   * @return  Boolean true if subject has OPTIN.
   */
  public boolean hasOptin(Subject subj) {
    return PrivilegeResolver.getInstance().hasPriv(
      this.getSession(), this, subj, AccessPrivilege.OPTIN
    );
  } // public boolean hasOption(subj)

  /**
   * Check whether the subject has OPTOUT on this group.
   * <pre class="eg">
   * if (g.hasOptout(subj)) {
   *   // has OPTOUT
   * }
   * else {
   *   // Does not have OPTOUT
   * }
   * </pre>
   * @param   subj  Check this subject.
   * @return  Boolean true if subject has OPTOUT.
   */
  public boolean hasOptout(Subject subj) {
    return PrivilegeResolver.getInstance().hasPriv(
      this.getSession(), this, subj, AccessPrivilege.OPTOUT
    );
  } // public boolean hasOptout(subj)

  /**
   * Check whether the subject is a member of this group.
   * <pre class="eg">
   * if (g.hasMember(subj)) {
   *   // Subject is a member of this group
   * }
   * else {
   *   // Subject is not a member of this group
   * } 
   * </pre>
   * @param   subj  Check this subject.
   * @return  Boolean true if subject belongs to this group.
   */
  public boolean hasMember(Subject subj) {
    try {
      return this.hasMember(subj, getDefaultList());
    }
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String err = ERR_GDL + eS.getMessage();
      LOG.fatal(err);
      throw new RuntimeException(err);
    }
  } // public boolean hasMember(subj)

  /**
   * Check whether the subject is a member of this list on this group.
   * <pre class="eg">
   * if (g.hasMember(subj, f)) {
   *   // Subject is a member of this group
   * }
   * else {
   *   // Subject is not a member of this group
   * } 
   * </pre>
   * @param   subj  Check this subject.
   * @param   f     Is subject a member of this list {@link Field}.
   * @return  Boolean true if subject belongs to this group.
   * @throws  SchemaException
   */
  public boolean hasMember(Subject subj, Field f) 
    throws  SchemaException
  {
    // TODO I should probably have _Member_ call _Group_ and not the inverse
    boolean rv  = false;
    String  msg = "hasMember " + SubjectHelper.getPretty(subj) + " '" 
      + f.getName() + "': ";
    try {
      Member m = MemberFinder.findBySubject(this.getSession(), subj);
      rv = m.isMember(this, f);
    }
    catch (MemberNotFoundException eMNF) {
      // TODO Fail silently?
      GrouperLog.debug(LOG, this.getSession(), msg + eMNF.getMessage());
    }
    GrouperLog.debug(LOG, this.getSession(), msg + rv);
    return rv;
  } // public boolean hasMember(subj, f)

  /**
   * Check whether the subject has READ on this group.
   * <pre class="eg">
   * if (g.hasRead(subj)) {
   *   // Has READ
   * }
   * else {
   *   // Does not have READ
   * }
   * </pre>
   * @param   subj  Check this subject.
   * @return  Boolean true if subject has READ.
   */
  public boolean hasRead(Subject subj) {
    return PrivilegeResolver.getInstance().hasPriv(
      this.getSession(), this, subj, AccessPrivilege.READ
    );
  } // public boolean hasRead(subj)

  /**
   * Check whether group has the specified type.
   * <pre class="eg">
   * GroupType custom = GroupTypeFinder.find("custom type");
   * if (g.hasType(custom)) {
   *   // Group has type
   * }
   * </pre>
   * @param   type  The {@link GroupType} to check.
   */
  public boolean hasType(GroupType type) {
    // if (this.getGroup_types().contains(type)) {
    Set types = this.getGroup_types();
    if (types.contains(type)) {
      return true;
    }
    return false;
  }

  /**
   * Check whether the subject has UPDATE on this group.
   * <pre class="eg">
   * if (g.hasUpdate(subj)) {
   *   // Has UPDATE
   * }
   * else {
   *   // Does not have UPDATE
   * }
   * </pre>
   * @param   subj  Check this subject.
   * @return  Boolean true if subject has UPDATE.
   */
  public boolean hasUpdate(Subject subj) {
    return PrivilegeResolver.getInstance().hasPriv(
      this.getSession(), this, subj, AccessPrivilege.UPDATE
    );
  } // public boolean hasUpdate(subj)

  /**
   * Check whether the subject has VIEW on this group.
   * <pre class="eg">
   * if (g.hasView(subj)) {
   *   // Has VIEW
   * }
   * else {
   *   // Does not have VIEW
   * }
   * </pre>
   * @param   subj  Check this member.
   * @return  Boolean true if subject has VIEW.
   */
  public boolean hasView(Subject subj) {
    return PrivilegeResolver.getInstance().hasPriv(
      this.getSession(), this, subj, AccessPrivilege.VIEW
    );
  } // public boolean hasView(subj)

  /**
   * Is this {@link Group} a factor in a {@link Composite} membership.
   * <pre class="eg">
   * if (g.isComposite()) {
   *   // this group is a factor in one-or-more composite memberships.
   * }
   * </pre>
   * @return  Boolean true if group is a factor in a composite membership.
   */
  public boolean isComposite() {
    Set factors = CompositeFinder.isFactor(this);
    if (factors.size() > 0) {
      return true;
    }
    return false;
  } // public boolean isComposite()

  /**
   * Revoke all privileges of the specified type on this group.
   * <pre class="eg">
   * try {
   *   g.revokePriv(AccessPrivilege.OPTIN);
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to revoke this privilege
   * }
   * catch (RevokePrivilegeException eRP) {
   *   // Unable to modify group
   * }
   * </pre>
   * @param   priv  Revoke all instances of this privilege.
   * @throws  InsufficientPrivilegeException
   * @throws  RevokePrivilegeException
   * @throws  SchemaException
   */
  public void revokePriv(Privilege priv)
    throws  InsufficientPrivilegeException,
            RevokePrivilegeException,
            SchemaException
  {
    StopWatch sw = new StopWatch();
    sw.start();
    PrivilegeResolver.getInstance().revokePriv(
      this.getSession(), this, priv
    );
    sw.stop();
    EL.groupRevokePriv(this.getSession(), this.getName(), priv, sw);
  } // public void revokePriv(priv)

  /**
   * Revoke a privilege from the specified subject.
   * <pre class="eg">
   * try {
   *   g.revokePriv(subj, AccessPrivilege.OPTIN);
   * }
   * catch (InsufficientPrivilegeException e1) {
   *   // Not privileged to revoke this privilege
   * }
   * catch (RevokePrivilegeException eRP) {
   *   // Error revoking privilege
   * }
   * </pre>
   * @param   subj  Revoke privilege from this subject.
   * @param   priv  Revoke this privilege.
   * @throws  InsufficientPrivilegeException
   * @throws  RevokePrivilegeException
   * @throws  SchemaException
   */
  public void revokePriv(Subject subj, Privilege priv) 
    throws  InsufficientPrivilegeException,
            RevokePrivilegeException,
            SchemaException
  {
    StopWatch sw = new StopWatch();
    sw.start();
    PrivilegeResolver.getInstance().revokePriv(
      this.getSession(), this, subj, priv
    );
    sw.stop();
    EL.groupRevokePriv(this.getSession(), this.getName(), subj, priv, sw);
  } // public void revokePriv(subj, priv)

  /**
   * Set an attribute value.
   * <pre class="eg">
   * try {
   *   g.attribute(attribute, value);
   * } 
   * catch (AttributeNotFoundException e0) {
   *   // Attribute doesn't exist
   * }
   * catch (GroupModifyException e1) {
   *   // Unable to modify group
   * }
   * catch (InsufficientPrivilegeException e2) {
   *   // Not privileged to modify this attribute
   * }
   * </pre>
   * @param   attr  Set this attribute.
   * @param   value Set to this value.
   * @throws  AttributeNotFoundException
   * @throws  GroupModifyException
   * @throws  InsufficientPrivilegeException
   */
  public void setAttribute(String attr, String value) 
    throws  AttributeNotFoundException, 
            GroupModifyException, 
            InsufficientPrivilegeException
  {
    // TODO s,AttributeNotFoundException,SchemaException,
    try {
      StopWatch sw = new StopWatch();
      sw.start();
      Field f = FieldFinder.find(attr);
      GroupValidator.canSetAttribute(this, f, value);

      // TODO I'm not comfortable with this code
      Set       attrs = new LinkedHashSet();
      boolean   found = false; // So we know if we are adding or updating
      Iterator  iter  = this.getGroup_attributes().iterator();
      while (iter.hasNext()) {
        Attribute a = (Attribute) iter.next();
        if (a.getField().equals(f)) {
          a.setValue(value); // updating
          found = true;
        }
        attrs.add(a);
      }
      if (found == false) {
        attrs.add(new Attribute(this, f, value)); // adding 
      }

      this.setGroup_attributes( this._updateSystemAttrs(f, value, attrs) );
      this.setModified();
      HibernateHelper.save(this);

      sw.stop();
      EL.groupSetAttr(this.getSession(), this.getName(), attr, value, sw);
    }
    catch (SchemaException eS) {
      throw new AttributeNotFoundException(eS.getMessage());
    }
    catch (Exception e) {
      throw new GroupModifyException(e.getMessage());
    }
  } // public void setAttribute(attr, value)

  /**
   * Set group description.
   * <pre class="eg">
   * try {
   *   g.setDescription(value);
   * }
   * catch (GroupModifyException e0) {
   *   // Unable to modify group
   * }
   * catch (InsufficientPrivilegeException e1) {
   *   // Not privileged to modify description
   * }
   * </pre>
   * @param   value   Set description to this value.
   * @throws  GroupModifyException
   * @throws  InsufficientPrivilegeException
   */
  public void setDescription(String value) 
    throws  GroupModifyException, 
            InsufficientPrivilegeException
  {
    try {
      this.setAttribute("description", value);
    }
    catch (AttributeNotFoundException eANF) {
      throw new GroupModifyException(
        "unable to set description: " + eANF.getMessage()
      );
    }
  } // public void setDescription(value)
 
  /**
   * Set group <i>extension</i>.
   * <pre class="eg">
   * try {
   *   g.setExtension(value);
   * }
   * catch (GroupModifyException eGM) {
   *   // Unable to modify group
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to modify group
   * }
   * </pre>
   * @param   value   Set <i>extension</i> to this value.
   * @throws  GroupModifyException
   * @throws  InsufficientPrivilegeException
   */
  public void setExtension(String value) 
    throws  GroupModifyException, 
            InsufficientPrivilegeException
  {
    try {
      this.setAttribute("extension", value);
    }
    catch (AttributeNotFoundException eANF) {
      throw new GroupModifyException(
        "unable to set extension: " + eANF.getMessage()
      );
    }
  } // public void setExtension(value)

  /**
   * Set group displayExtension.
   * <pre class="eg">
   * try {
   *   g.setDisplayExtension(value);
   * }
   * catch (GroupModifyException e0) {
   *   // Unable to modify group
   * }
   * catch (InsufficientPrivilegeException e1) {
   *   // Not privileged to modify displayExtension
   * }
   * </pre>
   * @param   value   Set displayExtension to this value.
   * @throws  GroupModifyException
   * @throws  InsufficientPrivilegeException
   */
  public void setDisplayExtension(String value) 
    throws  GroupModifyException, 
            InsufficientPrivilegeException
  {
    try {
      this.setAttribute("displayExtension", value);
    }
    catch (AttributeNotFoundException eANF) {
      throw new GroupModifyException(
        "unable to set displayExtension: " + eANF.getMessage()
      );
    }
  } // public void setDisplayExtension(value)

  /**
   * Convert this group to a {@link Member} object.
   * <p/>
   * <pre class="eg">
   * Member m = g.toMember();
   * </pre>
   * @return  {@link Group} as a {@link Member}
   */
  public Member toMember() {
    // TODO Does this need to be public?
    GrouperSession.validate(this.getSession());
    String msg = "toMember";
    GrouperLog.debug(LOG, s, msg);
    if (as_member == null) {
      try {
        GrouperLog.debug(LOG, s, msg + " finding group as member");
        as_member = MemberFinder.findBySubject(
          this.getSession(), this.toSubject()
        );
        GrouperLog.debug(LOG, s, msg + " found: " + as_member);
      }  
      catch (MemberNotFoundException eMNF) {
        // If we can't convert a group to a member we have major issues
        // and should probably just give up
        String err = ERR_G2M + eMNF.getMessage();
        GrouperLog.error(LOG, s, err);
        throw new RuntimeException(err);
      }
    }
    return as_member;
  } // public Member toMember()

  /**
   * Convert this group to a {@link Subject} object.
   * <p/>
   * <pre class="eg">
   * Subject subj = g.toSubject();
   * </pre>
   * @return  {@link Group} as a {@link Subject}
   */
  public Subject toSubject() {
    GrouperSession.validate(s);
    String msg = "toSubject";
    GrouperLog.debug(LOG, s, msg);
    if (as_subj == null) {
      try {
        GrouperLog.debug(LOG , s, msg + " find group as subject");
        as_subj = SubjectFinder.findById(
          this.getUuid(), "group", GrouperSourceAdapter.ID
        );
        GrouperLog.debug(LOG, s, msg + " found: " + as_subj);
      }
      catch (Exception e) {
        // If we can't find an existing group as a subject we have
        // major issues and shoudl probably just give up
        String err = ERR_G2S + e.getMessage();
        GrouperLog.error(LOG, s, err);
        throw new RuntimeException(err);
      }
    }
    return as_subj;
  } // public Subject toSubject()

  public String toString() {
    return new ToStringBuilder(this)
      .append("name"        , this.getName()        )
      .append("displayName" , this.getDisplayName() )
      .append("uuid"        , this.getUuid()        )
      .append("creator"     , getCreator_id()       )
      .append("modifier"    , getModifier_id()      )
      .toString();
  } // public String toString()


  // Protected Class Methods
  
  // When retrieving groups via the parent stem we need to manually
  // initialize the attributes and types.
  protected static void initializeGroup(Group g) 
    throws  HibernateException
  {
    Session hs = HibernateHelper.getSession();
    hs.load(g, g.getId());
    Hibernate.initialize( g.getGroup_attributes() );
    hs.close();
  } // protected void initializeGroup()


  // Protected Instance Methods //
  protected void setModified() {
    this.setModifier_id( s.getMember()        );
    this.setModify_time( new Date().getTime() );
  } // private void setModified()

  protected void setDisplayName(String value) {
    Set       attrs = new LinkedHashSet();
    Iterator  iter  = this.getGroup_attributes().iterator();
    while (iter.hasNext()) {
      Attribute a = (Attribute) iter.next();
      if (a.getField().getName().equals("displayName")) {
        a.setValue(value);
        this.attr_dn = value;
      }
      attrs.add(a);
    }
    this.setGroup_attributes(attrs);
  } // protected void setDisplayName(value)


  // Private Instance Methods //
  private Map _getAttributes() {
    Iterator iter = this.getGroup_attributes().iterator();
    while (iter.hasNext()) {
      Attribute attr = (Attribute) iter.next();
      this.attrs.put(attr.getField().getName(), attr);
    }
    return this.attrs;
  } // private Map _getAttributes()

  private void _revokeAllAccessPrivs(String msg) 
    throws  InsufficientPrivilegeException,
            RevokePrivilegeException, 
            SchemaException
  {
    // Proxy as root
    GrouperSession orig = this.getSession();
    this.setSession(GrouperSessionFinder.getRootSession());

    GrouperLog.debug(LOG, orig, msg + " revoking ADMIN");
    this.revokePriv(AccessPrivilege.ADMIN);
    GrouperLog.debug(LOG, orig, msg + " revoked ADMIN");
    GrouperLog.debug(LOG, orig, msg + " revoking OPTIN");
    this.revokePriv(AccessPrivilege.OPTIN);
    GrouperLog.debug(LOG, orig, msg + " revoked OPTIN");
    GrouperLog.debug(LOG, orig, msg + " revoking OPTOUT");
    this.revokePriv(AccessPrivilege.OPTOUT);
    GrouperLog.debug(LOG, orig, msg + " revoked OPTOUT");
    GrouperLog.debug(LOG, orig, msg + " revoking READ");
    this.revokePriv(AccessPrivilege.READ);
    GrouperLog.debug(LOG, orig, msg + " revoked READ");
    GrouperLog.debug(LOG, orig, msg + " revoking UPDATE");
    this.revokePriv(AccessPrivilege.UPDATE);
    GrouperLog.debug(LOG, orig, msg + " revoked UPDATE");
    GrouperLog.debug(LOG, orig, msg + " revoking VIEW");
    this.revokePriv(AccessPrivilege.VIEW);
    GrouperLog.debug(LOG, orig, msg + " revoked VIEW");

    this.setSession(orig);
  } // private void _revokeAllAccessPrivs(msg)

  private void _setCreated() {
    this.setCreator_id( s.getMember()         );
    this.setCreate_time( new Date().getTime() );
  } // private void _setCreated()

  private Set _updateSystemAttrs(Field f, String value, Set attrs) 
    throws  ModelException
  {
    Set updated = new LinkedHashSet();
    if      (f.getName().equals("extension")) {
      Iterator iter = attrs.iterator();
      while (iter.hasNext()) {
        Attribute a = (Attribute) iter.next();
        if (a.getField().getName().equals("name")) {
          AttributeValidator.namingValue(value);
          String newVal = Stem.constructName(
            this.getParentStem().getName(), value
          );
          a.setValue(newVal);
          // Update the cached values
          this.attr_e = value;
          this.attr_n = newVal;
        }
        updated.add(a);
      }
    }
    else if (f.getName().equals("displayExtension")) {
      Iterator iter = attrs.iterator();
      while (iter.hasNext()) {
        Attribute a = (Attribute) iter.next();
        if (a.getField().getName().equals("displayName")) {
          AttributeValidator.namingValue(value);
          String newVal = Stem.constructName(
            this.getParentStem().getDisplayName(), value
          );
          a.setValue(newVal);
          // Update the cached values
          this.attr_de = value;
          this.attr_dn = newVal;
        }
        updated.add(a);
      }
    } else {
      updated = attrs;
    }
    return updated;
  } // private Set _updateSystemAttrs(f, value, attrs


  // Getters //
  private Set getGroup_attributes() {
    return this.group_attributes;
  }
  private Set getGroup_types() {
    // We only want to return the singleton instances of each group
    // type.  This saves from potential catastrophe when saving objects
    // as well as (hopefully) being more efficient.
    if (this.types == null) {
      types         = new LinkedHashSet();
      Iterator iter = this.group_types.iterator();
      while (iter.hasNext()) {
        try {
          GroupType type = (GroupType) iter.next();
          types.add( GroupTypeFinder.find( type.toString() ) );
        }
        catch (SchemaException eS) {
          LOG.error(eS.getMessage()); // TODO
        }
      }
    }
    return this.types;
  }
  private Stem getParent_stem() {
    return this.parent_stem;
  }

  // Setters //
  private void setGroup_attributes(Set group_attributes) {
    this.group_attributes = group_attributes;
  }
  private void setGroup_types(Set types) {
    this.group_types = types;
  }
  protected void setParent_stem(Stem parent_stem) {
    this.parent_stem = parent_stem;
  }

}

