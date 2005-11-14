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
import  org.apache.commons.lang.builder.*;


/** 
 * Schema specification for Access and Naming privileges.
 * <p/>
 * @author  blair christensen.
 * @version $Id: Privilege.java,v 1.3 2005-11-14 18:35:39 blair Exp $
 */
public class Privilege implements Serializable {

  // Public Class Constants
  public static final String STEM = "stemmers";


  // Hibernate Properties
  private String  id;
  private boolean is_access;
  private boolean is_naming;
  private String  name;
  private Integer version;


  // Constructors

  /**
   * For Hibernate.
   */
  public Privilege() {
    // nothing
  }


  // Public Instance Methods
  public String toString() {
    return new ToStringBuilder(this)
           .append("name",      getName()     )
           .append("is_access", isIs_access() )
           .append("is_naming", isIs_naming() )
           .toString()
           ;
  } // public String toString()

  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Privilege)) {
      return false;
    }
    Privilege otherPriv = (Privilege) other;
    return new EqualsBuilder()
           .append(this.getName(),      otherPriv.getName()     )
           .append(this.isIs_access(),  otherPriv.isIs_access() )
           .append(this.isIs_naming(),  otherPriv.isIs_naming() )
           .isEquals()
           ;
    } // public boolean equals(other)

  public int hashCode() {
    return new HashCodeBuilder()
           .append(getName()      )
           .append(isIs_access()  )
           .append(isIs_naming()  )
           .toHashCode()
           ;
  } // public int hashCode()

  // Hibernate Accessors
  private String getId() {
    return this.id;
  }

  private void setId(String id) {
    this.id = id;
  }

  private String getName() {
    return this.name;
  }

  private void setName(String name) {
    this.name = name;
  }

  private boolean isIs_access() {
    return this.is_access;
  }

  private void setIs_access(boolean is_access) {
    this.is_access = is_access;
  }

  private boolean isIs_naming() {
    return this.is_naming;
  }

  private void setIs_naming(boolean is_naming) {
    this.is_naming = is_naming;
  }

  private Integer getVersion() {
    return this.version;
  }

  private void setVersion(Integer version) {
    this.version = version;
  }

}
