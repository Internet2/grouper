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
 * @version $Id: HibernateSubjectAttribute.java,v 1.5 2006-09-06 19:50:21 blair Exp $
 * @since   1.0
 */
class HibernateSubjectAttribute implements Serializable {

  // PUBLIC CLASS CONSTANTS //
  public static final long serialVersionUID = -4979920855853791786L;


  // HIBERNATE PROPERTIES //
  private String            name;
  private String            searchValue;
  private HibernateSubject  subjectId;
  private String            value;


  // CONSTRUCTORS //
  /**
   * For Hibernate.
   * @since 1.0
   */
  public HibernateSubjectAttribute() {
    super();
  } // public Attribute()

  // @since 1.0
  protected HibernateSubjectAttribute(
    HibernateSubject id, String name, String value, String searchVal
  )
  {
    this.setName(         name      );
    this.setSearchValue(  searchVal );
    this.setSubjectId(    id        );
    this.setValue(        value     );
  } // protected HibernateSubjectAttribute(id, name, value, searchVal)


  // PUBLIC INSTANCE METHODS //
  /**
   * @since 1.0
   */
  public boolean equals(Object other) {
    if ( (this == other ) ) return true;
    if ( !(other instanceof HibernateSubjectAttribute) ) return false;
    HibernateSubjectAttribute castOther = (HibernateSubjectAttribute) other;
    return new EqualsBuilder()
      .append(this.getSubjectId() , castOther.getSubjectId()  )
      .append(this.getName()      , castOther.getName()       )
      .append(this.getValue()     , castOther.getValue()      )
      .isEquals();
  } // public boolean equals(other)

  /**
   * @since 1.0
   */
  public int hashCode() {
    return new HashCodeBuilder()
      .append(getSubjectId())
      .append(getName()     )
      .append(getValue()    )
      .toHashCode();
  } // public int hashCode()


  // GETTERS //
  // @since 1.0
  private String getName() {
    return this.name;
  }
  // @since 1.0
  private String getSearchValue() {
    return this.searchValue;
  }
  // @since 1.0
  private HibernateSubject getSubjectId() {
    return this.subjectId;
  }
  // @since 1.0
  private String getValue() {
    return this.value;
  }


  // SETTERS //
  // @since 1.0
  private void setName(String name) {
    this.name = name;
  }
  // @since 1.0
  private void setSearchValue(String value) {
    this.searchValue = value;
  }
  // @since 1.0
  private void setSubjectId(HibernateSubject subj) {
    this.subjectId = subj;
  }
  // @since 1.0
  private void setValue(String value) {
    this.value = value;
  }

}

