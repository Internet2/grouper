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
import  org.apache.commons.lang.builder.*;


/** 
 * Hibernate representation of the JDBC SubjectAttribute table.
 * <p/>
 * @author  blair christensen.
 * @version $Id: HibernateSubjectAttribute.java,v 1.1.2.1 2006-04-10 17:51:03 blair Exp $
 */
class HibernateSubjectAttribute implements Serializable {

  // TODO Move to different package?

  // Hibernate Properties
  private String            name;
  private String            searchValue;
  private HibernateSubject  subjectID;
  private String            value;


  // Constructors
  public HibernateSubjectAttribute() {
    super();
  } // public Attribute()
  protected HibernateSubjectAttribute(
    HibernateSubject id, String name, String value, String searchVal
  )
  {
    this.name         = name;
    this.searchValue  = searchVal;
    this.subjectID    = id;
    this.value        = value;
  } // protected HibernateSubjectAttribute(id, name, value, searchVal)


  // Public Instance Methods
  public boolean equals(Object other) {
    if ( (this == other ) ) return true;
    if ( !(other instanceof HibernateSubjectAttribute) ) return false;
    HibernateSubjectAttribute castOther = (HibernateSubjectAttribute) other;
    return new EqualsBuilder()
      .append(this.getSubjectID() , castOther.getSubjectID()  )
      .append(this.getName()      , castOther.getName()       )
      .append(this.getValue()     , castOther.getValue()      )
      .isEquals();
  } // public boolean equals(other)

  public int hashCode() {
    return new HashCodeBuilder()
      .append(getSubjectID())
      .append(getName()     )
      .append(getValue()    )
      .toHashCode();
  } // public int hashCode()


  // Getters //
  private String getName() {
    return this.name;
  }
  private String getSearchValue() {
    return this.searchValue;
  }
  private HibernateSubject getSubjectID() {
    return this.subjectID;
  }
  private String getValue() {
    return this.value;
  }


  // Setters //
  private void setName(String name) {
    this.name = name;
  }
  private void setSearchValue(String value) {
    this.searchValue = value;
  }
  private void setSubjectID(HibernateSubject subj) {
    this.subjectID = subj;
  }
  private void setValue(String value) {
    this.value = value;
  }

}

