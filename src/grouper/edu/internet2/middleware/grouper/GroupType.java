/*
  Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2005 The University Of Chicago

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
import  java.util.*;
import  net.sf.hibernate.*;
import  org.apache.commons.lang.builder.*;
import  org.apache.commons.lang.time.*;
import  org.apache.commons.logging.*;


/** 
 * Schema specification for a Group type.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GroupType.java,v 1.7 2006-01-25 22:27:09 blair Exp $
 *     
 */
public class GroupType implements Serializable {

  // Private Class Constants
  private static final EventLog EL  = new EventLog();
  private static final Log      LOG = LogFactory.getLog(GroupType.class);


  // Hibernate Properties
  private Member  creator_id;
  private long    create_time;
  private Set     fields        = new LinkedHashSet();
  private String  id;
  private String  name;
  private Status  status;


  // Constructors

  // For Hibernate
  public GroupType() {
    super();
  }

  protected GroupType(String name, Set fields) {
    this.setName(name);
    this.setFields(fields); 
  } // protected GroupType(name, fields)


  // Public Class Methods

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
      String msg = "subject not privileged to add group types";
      LOG.error(msg);
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
      String msg = "type already exists: " + name;
      LOG.error(msg);
      throw new SchemaException(msg);
    }
    try {
      type = new GroupType(name, new HashSet());
      HibernateHelper.save(type);
      sw.stop();
      EL.groupTypeAdd(s, name, sw);
      return type;
    }
    catch (HibernateException eH) {
      String msg = "unable to add type: " + name + ": " + eH.getMessage();
      LOG.error(msg);
      throw new SchemaException(msg);
    }
  } // public static GroupType createType(s, name)

  // Public Instance Methods

  /**
   * Add a custom {@link Field} to a custom {@link GroupType}.
   * <p/>
   * Create a new custom field that can be used with this group type.
   * If the field already exists, is one of the reserved system field
   * types (<i>base</i> and <i>naming</i>), the type is neither an
   * <i>attribute</i> nor a <i>list</i> or if the read and write
   * privileges are not access privileges a {@link SchemaException}
   * will be thrown.  If the subject is not root-like, an 
   * {@link InsufficientPrivilegeException} will be thrown.
   * <pre class="eg">
   * try {
   *   type.addField(
   *     "my field", FieldType.LIST, AccessPrivilege.VIEW, AccessPrivilege.UPDATE, false
   *   );
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to add field
   * }
   * catch (SchemaException eS) {
   *   // Invalid schema
   * }
   * </pre>
   * @param   name      Name of field.
   * @param   type      {@link FieldType} of this {@link Field}.
   * @param   read      {@link Privilege} required to write to this {@link Field}.
   * @param   write     {@link Privilege} required to write to this {@link Field}.
   * @param   required  Is this field required.
   * @throws  InsufficientPrivilegeException
   * @throws  SchemaException
   */
  public void addField(
      GrouperSession s, String name, FieldType type, Privilege read, 
      Privilege write, boolean required
  )
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    Field     f   = null;
    StopWatch sw    = new StopWatch();
    sw.start();
    if (!PrivilegeResolver.getInstance().isRoot(s.getSubject())) {
      String msg = "subject not privileged to add fields";
      LOG.error(msg);
      throw new InsufficientPrivilegeException(msg);
    }
    try {
      f = FieldFinder.find(name);  
      // field already exists.  
    }
    catch (SchemaException eS) {
      // Ignore
    } 
    if (f != null) {
      String msg = "field already exists: " + name;
      LOG.error(msg);
      throw new SchemaException(msg);
    }
    if ( (this.getName().equals("base")) || (this.getName().equals("naming")) ) {
      String msg = "cannot add fields to system group types";
      LOG.error(msg);
      throw new SchemaException(msg);
    }
    if 
    (
      !(
        (type.toString().equals(FieldType.ATTRIBUTE.toString()) ) 
        || 
        (type.toString().equals(FieldType.LIST.toString())      ) 
      )
    )
    {
      String msg = "invalid field type: " + type;
      LOG.error(msg);
      throw new SchemaException(msg);
    }
    if (!Privilege.isAccess(read)) {
      String msg = "read privilege not access privilege: " + read;
      LOG.error(msg);
      throw new SchemaException(msg);
    }
    if (!Privilege.isAccess(write)) {
      String msg = "write privilege not access privilege: " + write;
      LOG.error(msg);
      throw new SchemaException(msg);
    }
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
      EL.groupTypeAddField(s, this.getName(), name, sw);
    }
    catch (HibernateException eS) {
      String msg = "unable to add field: " + name + ": " + eS.getMessage();
      LOG.error(msg);
      throw new SchemaException(msg);
    }
  } // public void addField(s, name, type, read, write, required)

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


  // Hibernate Accessors

  /**
   * Get group fields for this group type.
   * @return  A set of {@link Field} objects.
   */
  public Set getFields() {
    return this.fields;
  } // public Set getFields()

  protected void setFields(Set fields) {
    Iterator iter = fields.iterator();
    while (iter.hasNext()) {
      Field f = (Field) iter.next();
      f.setGroup_type(this); 
    }
    this.fields = fields;
  } // protected void setFields(fields)
  
  private String getId() {
    return this.id;
  } // private String getId()
  
  private void setId(String id) {
    this.id = id;
  } // private void setId()

  /**
   * Get group type name.
   * @return  group type name.
   */
  public String getName() {
    return this.name;
  } // public String getName()

  private void setName(String name) {
    this.name = name;
  } // private void setName(name)

  private Status getStatus() {
    return this.status;
  }
  private void setStatus(Status s) {
    this.status = s;
  }

  private Member getCreator_id() {
    return this.creator_id;
  }
  private void setCreator_id(Member m) {
    this.creator_id = m;
  }
  private long getCreate_time() {
    return this.create_time;
  }
  private void setCreate_time(long time) {
    this.create_time = time;
  }

}
