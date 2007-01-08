/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

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
import  java.io.Serializable;
import  java.util.*;
import  org.apache.commons.lang.builder.*;

/** 
 * Hibernate representation of the JDBC Subject table.
 * <p/>
 * <p><b>This class is experimental and will change in future Grouper
 * releases.</b></p>
 * @author  blair christensen.
 * @version $Id: HibernateSubject.java,v 1.21 2007-01-08 16:43:56 blair Exp $
 * @since   1.0
 */
public class HibernateSubject implements Serializable {

  // PUBLIC CLASS CONSTANTS //
  public static final long serialVersionUID = -3413627300057683379L;


  // HIBERNATE PROPERTIES //
  private Set     attributes      = new LinkedHashSet();
  private String  name;
  private String  subjectId;
  private String  subjectTypeId;


  // CONSTRUCTORS //

  // @since 1.0
  protected HibernateSubject(
    String subjectId, String subjectTypeId, String name
  )
  {
    this.setAttributes(     new LinkedHashSet() );
    this.setName(           name                );
    this.setSubjectId(      subjectId           );
    this.setSubjectTypeId(  subjectTypeId       );
  } // protected HibernateSubject(subjectId, subjectTypeId, name)

  // Default constructor for Hibernate.
  // @since 1.0
  private HibernateSubject() {
    super();
  } // private HibernateSubject()


  // PUBLIC CLASS METHODS //

  /**
   * Add a {@link Subject} to the <i>JDBC Subject</i> table.
   * <p>Subjects may only be added within a root-like session.</p>
   * <pre class="eg">
   * try {
   *   HibernateSubject hsubj = HibernateSubject.add(s, "id", "person", "name");
   * }
   * catch (GrouperException eG) {
   *   // unable to add subject
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // not privileged to add subject
   * }
   * </pre>
   * @param   s     Create subject within this session context.
   * @param   id    The subject id to assign to the subject.
   * @param   type  The subject type to assign to the subject.
   * @param   name  The name to assign to the subject.
   * @return  The created {@link Subject}.
   * @throws  GrouperException
   * @throws  InsufficientPrivilegeException
   * @since   1.1.0
   */
  public static HibernateSubject add(GrouperSession s, String id, String type, String name) 
    throws  GrouperException,
            InsufficientPrivilegeException
  {
    if (!RootPrivilegeResolver.internal_isRoot(s)) {
      throw new InsufficientPrivilegeException(E.ROOTLIKE_TO_ADD_HSUBJ);
    }    
    return internal_add(id, type, name);
  } // public static HibernateSubject add(s, id, type, name)


  // PUBLIC INSTANCE METHODS //
  /**
   * To mimic {@link Subject}.
   * @return  <i>subject id</i>
   * @since   1.0
   */
  public String getId() {
    return this.getSubjectId();
  } // public String getId()

  /**
   * @since 1.0
   */
  public String toString() {
    return new ToStringBuilder(this)
      .append("id"    , this.getSubjectId()     )
      .append("type"  , this.getSubjectTypeId() )
      .append("name"  , this.getName()          )
      .toString();
  } // public String toString()


  // PROTECTED CLASS METHODS //

  // @since   1.1.0
  protected static HibernateSubject internal_add(String id, String type, String name)
    throws  GrouperException
  {
    try {
      HibernateRegistrySubjectDAO.find(id, type);
      throw new GrouperException(E.SUBJ_ALREADY_EXISTS + id + "/" + type + "/" + name);
    }
    catch (SubjectNotFoundException eSNF) {
      HibernateSubject subj = new HibernateSubject(id, type, name);
      return HibernateRegistrySubjectDAO.create(subj);
    }
  } // protected static HibernateSubject internal_add(id, type, name)


  // GETTERS //
  // @since 1.0
  private Set getAttributes() {
    return this.attributes;
  }
  // @since 1.0
  public String getName() {
    return this.name;
  }
  // @since 1.0
  public String getSubjectId() {
    return this.subjectId;
  }
  // @since 1.0
  public String getSubjectTypeId() {
    return this.subjectTypeId;
  }


  // SETTERS //
  // @since 1.0
  private void setAttributes(Set attrs) {
    this.attributes = attrs;
  }
  // @since 1.0
  private void setName(String name) {
    this.name = name;
  }
  // @since 1.0
  private void setSubjectId(String id) {
    this.subjectId = id;
  }
  // @since 1.0
  private void setSubjectTypeId(String id) {
    this.subjectTypeId = id;
  }
    
}

