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
import  java.util.*;
import  net.sf.hibernate.*;
import  org.apache.commons.lang.builder.*;


/** 
 * Hibernate representation of the JDBC Subject table.
 * <p />
 * @author  blair christensen.
 * @version $Id: HibernateSubject.java,v 1.2 2006-05-23 19:10:23 blair Exp $
 */
class HibernateSubject implements Serializable {

  // TODO Move to different package?

  // Hibernate Properties
  private Set     attributes      = new LinkedHashSet();
  private String  name;
  private String  subjectID;
  private String  subjectTypeID;


  // Constructors
  public HibernateSubject() {
    super();
  }
  protected HibernateSubject(
    String subjectID, String subjectTypeID, String name
  )
  {
    this.setAttributes(     new LinkedHashSet() );
    this.setName(           name                );
    this.setSubjectID(      subjectID           );
    this.setSubjectTypeID(  subjectTypeID       );
  } // protected HibernateSubject(subjectID, subjectTypeID, name)


  // Public Instance Methods //
  public String toString() {
    return new ToStringBuilder(this)
      .append("id"    , this.getSubjectID()     )
      .append("type"  , this.getSubjectTypeID() )
      .append("name"  , this.getName()          )
      .toString();
  } // public String toString()


  // Getters //
  private Set getAttributes() {
    return this.attributes;
  }
  private String getName() {
    return this.name;
  }
  private String getSubjectID() {
    return this.subjectID;
  }
  private String getSubjectTypeID() {
    return this.subjectTypeID;
  }


  // Setters //
  private void setAttributes(Set attrs) {
    this.attributes = attrs;
  }
  private void setName(String name) {
    this.name = name;
  }
  private void setSubjectID(String id) {
    this.subjectID = id;
  }
  private void setSubjectTypeID(String id) {
    this.subjectTypeID = id;
  }
    
}

