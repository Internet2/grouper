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

/** 
 * Hibernate representation of the JDBC Subject table.
 * @author  blair christensen.
 * @version $Id: HibernateSubject.java,v 1.3 2006-05-26 17:15:13 blair Exp $
 * @since   1.0
 */
public class HibernateSubject implements Serializable {

  // HIBERNATE PROPERTIES //
  private Set     attributes      = new LinkedHashSet();
  private String  name;
  private String  subjectID;
  private String  subjectTypeID;


  // CONSTRUCTORS //
  /**
   * For Hibernate.
   */
  public HibernateSubject() {
    super();
  } // public HibernateSubject()

  protected HibernateSubject(
    String subjectID, String subjectTypeID, String name
  )
  {
    this.setAttributes(     new LinkedHashSet() );
    this.setName(           name                );
    this.setSubjectID(      subjectID           );
    this.setSubjectTypeID(  subjectTypeID       );
  } // protected HibernateSubject(subjectID, subjectTypeID, name)


  // PUBLIC CLASS METHODS //

  /**
   * Add a {@link Subject} to the <i>JDBC Subject</i> table.
   * <pre class="eg">
   * try {
   *   Subject subj = HibernateSubject.add("id", "person", "name");
   * }
   * catch (HibernateException eH) {
   *   // unable to add subject
   * }
   * </pre>
   * @param   id    The subject id to assign to the subject.
   * @param   type  The subject type to assign to the subject.
   * @param   name  The name to assign to the subject.
   * @return  The created {@link Subject}.
   * @throws  HibernateException
   * @since   1.0
   */
  public static HibernateSubject add(String id, String type, String name) 
    throws  HibernateException  // TODO Throw something more user-friendly
  {
    try {
      HibernateSubject subj = HibernateSubjectFinder.find(id, type);
      throw new HibernateException(
        "subject already exists: " + id + "/" + type + "/" + name
      );
    }
    catch (SubjectNotFoundException eSNF) {
      Session           hs    = HibernateHelper.getSession();
      Transaction       tx    = hs.beginTransaction();
      HibernateSubject  subj  = new HibernateSubject(id, type, name);
      hs.save(subj);
      tx.commit();
      hs.close();
      return subj;
    }
  } // public static HibernateSubject add(id, type, name)


  // PUBLIC INSTANCE METHODS //
  public String toString() {
    return new ToStringBuilder(this)
      .append("id"    , this.getSubjectID()     )
      .append("type"  , this.getSubjectTypeID() )
      .append("name"  , this.getName()          )
      .toString();
  } // public String toString()


  // GETTERS //
  private Set getAttributes() {
    return this.attributes;
  }
  public String getName() {
    return this.name;
  }
  public String getSubjectID() {
    return this.subjectID;
  }
  public String getSubjectTypeID() {
    return this.subjectTypeID;
  }


  // SETTERS //
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

