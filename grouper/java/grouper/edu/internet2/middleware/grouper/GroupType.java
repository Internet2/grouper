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
import  org.apache.commons.lang.builder.*;
import  org.apache.commons.logging.*;


/** 
 * Schema specification for a Group type.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GroupType.java,v 1.3 2005-12-09 07:35:38 blair Exp $
 *     
 */
public class GroupType implements Serializable {

  // Private Class Constants
  private static final Log LOG = LogFactory.getLog(GroupType.class);


  // Hibernate Properties
  private Set     fields  = new LinkedHashSet();
  private String  id;
  private String  name;


  // Constructors

  // For Hibernate
  public GroupType() {
    super();
  }

  protected GroupType(String name, Set fields) {
    this.setName(name);
    this.setFields(fields); 
  } // protected GroupType(name, fields)


  // Public Instance Methods
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

}
