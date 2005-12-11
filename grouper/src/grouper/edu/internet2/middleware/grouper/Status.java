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
 * Schema specification for an object status.
 * <p />
 * @author  blair christensen.
 * @version $Id: Status.java,v 1.1 2005-12-11 21:41:53 blair Exp $    
 */
public class Status implements Serializable {

  // Hibernate Properties
  private String    id;
  private String    status_name;
  private long      status_ttl;

    
  // Constructors
    
  // For Hibernate
  public Status() {
    super();
  }

  protected Status(String name, long ttl) {
    this.setStatus_name(name);
    this.setStatus_ttl(ttl);
  } // protected Status(name, ttl)


  // Public Instance Methods
  public boolean equals(Object other) {
    if (this == other) { 
      return true;
    }
    if (!(other instanceof Status)) {
      return false;
    }
    Status otherStatus = (Status) other;
    return new EqualsBuilder()
           .append(this.getStatus_name(),  otherStatus.getStatus_name())
           .append(this.getStatus_ttl() ,  otherStatus.getStatus_ttl() )
           .isEquals();
  } // public boolean equals(other)

  public String getName() {
    return this.getStatus_name();
  } // public String getName()

  public long getTTL() {
    return this.getStatus_ttl();
  } // public long getTTL()

  public int hashCode() {
    return new HashCodeBuilder()
           .append(getStatus_name() )
           .append(getStatus_ttl()  )
           .toHashCode();
  } // public int hashCode()

  public String toString() {
    return new ToStringBuilder(this)
      .append("name"  , this.getStatus_name()  )
      .append("ttl"   , this.getStatus_ttl()  )
      .toString();
  } // public String toString()


  // Hibernate Accessors
  private String getId() {
    return this.id;
  }

  private void setId(String id) {
    this.id = id;
  }

  private String getStatus_name() {
    return this.status_name;
  }

  private void setStatus_name(String status_name) {
    this.status_name = status_name;
  }

  private long getStatus_ttl() {
    return this.status_ttl;
  }

  private void setStatus_ttl(long ttl) {
    this.status_ttl = ttl;
  }

}

