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
import  java.io.Serializable;
import  java.sql.*;
import  java.util.*;
import  net.sf.hibernate.*;
import  org.apache.commons.lang.builder.*;
import  org.apache.commons.lang.time.*;

/** 
 * Schema specification for a Group type.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GroupType.java,v 1.20 2006-07-03 17:18:48 blair Exp $
 */
public class GroupType implements Serializable {

  // PUBLIC CLASS CONSTANTS //
  public static final long serialVersionUID = 8214760621248803096L;


  // HIBERNATE PROPERTIES //
  private boolean assignable    = true;
  private Member  creator_id;
  private long    create_time;
  private Set     fields        = new LinkedHashSet();
  private String  id;
  private boolean internal      = false;
  private String  name;


  // CONSTRUCTORS //
  public GroupType() {
    super();
  } // public GroupType()

  protected GroupType(String name, Set fields, boolean assignable, boolean internal) {
    this.setName(name);
    this.setFields(fields); 
    this.setAssignable(assignable);
    this.setInternal(internal);
  } // protected GroupType(name, fields, assignable, internal)


  // PUBLIC CLASS METHODS //

  /*
   * Create a new {@link GroupType}.  
   * <p/>
   * Create a new custom group type that can be assigned to existing or
   * new groups.  If the type already exists, a {@link SchemaException}
   * will be thrown.  If the subject is not root-like, an 
   * {@link InsufficientPrivilegeException} will be thrown.
   * <pre class="eg">
   * try {
   *   GroupType type = GroupType.createType(s, "my custom type");
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Subject not privileged to add group types.
   * }
   * catch (SchemaException eS) {
   *   // Type not created
   * }
   * </pre>
   * @param   s     Create type within this session context.
   * @param   name  Create type with this name.
   * @return  New {@link GroupType}.
   * @throws  InsufficientPrivilegeException
   * @throws  SchemaException
   */
  public static GroupType createType(GrouperSession s, String name) 
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    GroupType type  = null;
    StopWatch sw    = new StopWatch();
    sw.start();
    if (!PrivilegeResolver.getInstance().isRoot(s.getSubject())) {
      String msg = E.GROUPTYPE_NOADD;
      ErrorLog.error(GroupType.class, msg);
      throw new InsufficientPrivilegeException(msg);
    }
    try {
      type = GroupTypeFinder.find(name);  
      // type already exists.  
    }
    catch (SchemaException eS) {
      // Ignore
    } 
    if (type != null) {
      String msg = E.GROUPTYPE_EXISTS + name;
      ErrorLog.error(GroupType.class, msg);
      throw new SchemaException(msg);
    }
    try {
      type = new GroupType(name, new HashSet(), true, false);
      type.setCreator_id(   s.getMember()                   );
      type.setCreate_time(  new java.util.Date().getTime()  );
      HibernateHelper.save(type);
      sw.stop();
      EventLog.info(s, M.GROUPTYPE_ADD + U.q(type.toString()), sw);
      return type;
    }
    catch (HibernateException eH) {
      String msg = E.GROUPTYPE_ADD + name + ": " + eH.getMessage();
      ErrorLog.error(GroupType.class, msg);
      throw new SchemaException(msg, eH);
    }
  } // public static GroupType createType(s, name)


  // PUBLIC INSTANCE METHODS //

  /**
   * Add a custom attribute {@link Field} to a custom {@link GroupType}.
   * try {
   *   Field myAttr = type.addAttribute(
   *     "my attribute", AccessPrivilege.VIEW, AccessPrivilege.UPDATE, false
   *   );
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to add attribute
   * }
   * catch (SchemaException eS) {
   *   // Invalid schema
   * }
   * </pre>
   * @param   s         Add attribute within this session context.
   * @param   name      Name of attribute.
   * @param   read      {@link Privilege} required to write to this {@link Field}.
   * @param   write     {@link Privilege} required to write to this {@link Field}.
   * @param   required  Is this attribute required.
   * @throws  InsufficientPrivilegeException
   * @throws  SchemaException
   */
  public Field addAttribute(
    GrouperSession s, String name, Privilege read, Privilege write, boolean required
  )
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    return this._addField(s, name, FieldType.ATTRIBUTE, read, write, required);
  } // public Field addAttribute(s, name, read, write, required)

  /**
   * Add a custom list {@link Field} to a custom {@link GroupType}.
   * try {
   *   Field myList = type.addList(
   *     "my list", AccessPrivilege.VIEW, AccessPrivilege.UPDATE
   *   );
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to add list
   * }
   * catch (SchemaException eS) {
   *   // Invalid schema
   * }
   * </pre>
   * @param   s         Add list within this session context.
   * @param   name      Name of list.
   * @param   read      {@link Privilege} required to write to this {@link Field}.
   * @param   write     {@link Privilege} required to write to this {@link Field}.
   * @throws  InsufficientPrivilegeException
   * @throws  SchemaException
   */
  public Field addList(
    GrouperSession s, String name, Privilege read, Privilege write
  )
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    return this._addField(s, name, FieldType.LIST, read, write, false);
  } // public Field addList(s, name, read, write)

  /*
   * Delete a custom {@link GroupType} definition.
   * <p/>
   * <pre class="eg">
   * try {
   *   aGroupType.delete(s);
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Subject not privileged to delete group type.
   * }
   * catch (SchemaException eS) {
   *   // Type could not be deleted
   * }
   * </pre>
   * @param   s     Delete type within this session context.
   * @throws  InsufficientPrivilegeException
   * @throws  SchemaException
   * @since   1.0
   */
  public void delete(GrouperSession s) 
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    StopWatch sw = new StopWatch();
    sw.start();
    if (isSystemType(this)) {
      String msg = E.GROUPTYPE_NODELSYS + this.getName();
      ErrorLog.error(GroupType.class, msg);
      throw new SchemaException(msg);
    } 
    if (!PrivilegeResolver.getInstance().isRoot(s.getSubject())) {
      String msg = E.GROUPTYPE_NODEL;
      ErrorLog.error(GroupType.class, msg);
      throw new InsufficientPrivilegeException(msg);
    }
    try {
      // TODO I **really** should not need to drop down to JDBC for this
      //      And, of course, I can't even iterator through this type's
      //      fields to check if they are in use - because it might not
      //      have any fields!
      Session           hs  = HibernateHelper.getSession();
      Connection        c   = hs.connection();
      PreparedStatement st  = c.prepareStatement(
        "select count(ggt.type_id) from grouper_groups_types ggt where ggt.type_id = ?"
      );
      st.setString(1, this.getId());
      ResultSet         rs  = st.executeQuery();
      if (rs.next()) {
        int size = rs.getInt(1);
        if (size > 0) {
          String msg = E.GROUPTYPE_DELINUSE;
          ErrorLog.error(GroupType.class, msg);
          throw new SchemaException(msg);
        }
      }
      hs.close();

      // Now delete the type
      String typeName = this.getName(); // For logging purposes
      HibernateHelper.delete(this);
      sw.stop();
      EventLog.info(s, M.GROUPTYPE_DEL + U.q(typeName), sw);
      // TODO Now update the cached types + fields
      GroupTypeFinder.updateKnownTypes();
      FieldFinder.updateKnownFields();
    }
    catch (HibernateException eH) {
      String msg = E.GROUPTYPE_DEL + eH.getMessage();
      ErrorLog.error(GroupType.class, msg);
      throw new SchemaException(msg, eH);
    }
    catch (SQLException eSQL) {
      String msg = E.GROUPTYPE_DEL + eSQL.getMessage();
      ErrorLog.error(GroupType.class, msg);
      throw new SchemaException(msg, eSQL);
    }
  } // public void delete(s)

  /**
   * Delete a custom {@link Field} from a custom {@link GroupType}.
   * <p/>
   * Delete a field from this group type.  If the field does not exist
   * in this type a {@link SchemaException} will be thrown.  If the
   * subject is not root-like, an {@link InsufficientPrivilegeException}
   * will be thrown.
   * <pre class="eg">
   * try {
   *   type.deleteField("my field");
   *   );
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to delete field
   * }
   * catch (SchemaException eS) {
   *   // Invalid schema
   * }
   * </pre>
   * @param   s         Delete field within this session context.
   * @param   name      Name of field to delete.
   * @throws  InsufficientPrivilegeException
   * @throws  SchemaException
   */
  public void deleteField(GrouperSession s, String name)
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    // TODO I should still move more of this to _Validator_
    StopWatch sw  = new StopWatch();
    sw.start();
    Field     f   = FieldFinder.find(name);  
    Validator.canDeleteFieldFromType(s, this, f);
    if (f.inUse()) {
      String msg = E.GROUPTYPE_FIELDNODELINUSE + name;
      ErrorLog.error(GroupType.class, msg);
      throw new SchemaException(msg);
    }
    // With validation complete, delete the field
    try {
      Set fields = this.getFields();
      if (fields.remove(f)) {
        this.setFields(fields);
        HibernateHelper.save(this);
        sw.stop();
        EventLog.info(
          s,
          M.GROUPTYPE_DELFIELD + U.q(f.getName()) + " type=" + U.q(this.getName()),
          sw
        );
      }
      else {
        String msg = E.GROUPTYPE_FIELDNODELMISS;
        ErrorLog.error(GroupType.class, msg);
        throw new SchemaException(msg);
      }
    }
    catch (HibernateException eH) {
      String msg = E.GROUPTYPE_FIELDDEL + eH.getMessage();
      ErrorLog.error(GroupType.class, msg);
      throw new SchemaException(msg, eH);
    }
  } // public void deleteField(s, name)

  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof GroupType)) {
      return false;
    }
    GroupType otherType = (GroupType) other;
    return new EqualsBuilder()
      .append(this.getName()  , otherType.getName())
      .isEquals();
  } // public boolean equals(other)

  public int hashCode() {
    return new HashCodeBuilder()
      .append(getName())
      .toHashCode();
  } // public int hashCode()

  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
      .append("name",   this.getName()  )
      .toString();
  } // public String toString()


  // Protected Class Methods
  // When retrieving groups via the parent stem we need to manually
  // initialize the types - which means we then need to manually
  // initialize the fields..
  protected static void initializeGroupType(GroupType type) 
    throws  HibernateException
  {
    Session hs = HibernateHelper.getSession();
    hs.load(type, type.getId());
    Hibernate.initialize( type.getFields() );
    hs.close();
  } // protected void initializeGroupType()

  protected static boolean isSystemType(GroupType type) {
    String name = type.getName();
    if ( (name.equals("base")) || (name.equals("naming")) ) {
      return true;
    }
    return false;
  } // protected static boolean isSystemType(type)


  // PRIVATE INSTANCE METHODSs //
  private Field _addField(
      GrouperSession s, String name, FieldType type, Privilege read, 
      Privilege write, boolean required
  )
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    Field     f   = null;
    StopWatch sw  = new StopWatch();
    sw.start();
    Validator.canAddFieldToType(s, this, name, type, read, write);
    try {
      boolean nullable = true;
      if (required == true) {
        nullable = false;
      }
      Set fields = this.getFields();
      f = new Field(name, type, read, write, nullable);
      fields.add(f);
      this.setFields(fields);
      HibernateHelper.save(this);
      sw.stop();
      EventLog.info(
        s, 
        M.GROUPTYPE_ADDFIELD + U.q(f.getName()) + " ftype=" + U.q(type.toString()) 
        + " gtype=" + U.q(this.getName()),
        sw
      );
      return f;
    }
    catch (HibernateException eS) {
      String msg = E.GROUPTYPE_FIELDADD + name + ": " + eS.getMessage();
      ErrorLog.error(GroupType.class, msg);
      throw new SchemaException(msg, eS);
    }
  } // private void _addField(s, name, type, read, write, required)


  // GETTERS //
  protected boolean getAssignable() {
    return this.assignable;
  }
  private long getCreate_time() {
    return this.create_time;
  }
  private Member getCreator_id() {
    return this.creator_id;
  }
  /**
   * Get group fields for this group type.
   * @return  A set of {@link Field} objects.
   */
  public Set getFields() {
    return this.fields;
  } // public Set getFields()
  private String getId() {
    return this.id;
  } // private String getId()
  protected boolean getInternal() {
    return this.internal;
  }
  /**
   * Get group type name.
   * @return  group type name.
   */
  public String getName() {
    return this.name;
  } // public String getName()


  // SETTERS //
  private void setAssignable(boolean assignable) {
    this.assignable = assignable;
  }
  private void setCreate_time(long time) {
    this.create_time = time;
  }
  private void setCreator_id(Member m) {
    this.creator_id = m;
  }
  protected void setFields(Set fields) {
    Field     f;
    Iterator  iter  = fields.iterator();
    while (iter.hasNext()) {
      f = (Field) iter.next();
      f.setGroup_type(this); 
    }
    this.fields = fields;
  } // protected void setFields(fields)
  private void setId(String id) {
    this.id = id;
  } // private void setId()
  private void setInternal(boolean internal) {
    this.internal = internal;
  }
  private void setName(String name) {
    this.name = name;
  } // private void setName(name)

}
