/**
 * Copyright 2014 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectType;
import edu.internet2.middleware.subject.provider.SubjectImpl;

/** 
 * A {@link Subject} local to the Groups Registry.
 * 
 * <p><b>NOTE: THIS CLASS IS NOT CONSIDERED STABLE AND MAY CHANGE IN FUTURE RELEASES.</b></p>
 * @author  blair christensen.
 * @version $Id: RegistrySubject.java,v 1.19 2009-09-02 05:57:26 mchyzer Exp $
 * @since   1.2.0
 */
@SuppressWarnings("serial")
public class RegistrySubject extends GrouperAPI implements Subject {

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#clone()
   */
  @Override
  public RegistrySubject clone() {
    throw new RuntimeException("Clone not supported");
  }

  /**
   * wrap a subjectImpl
   */
  private SubjectImpl subject = new SubjectImpl(null, null, null, null, null);

  /**
   * Add a {@link Subject} to a {@link Source} within the Groups Registry.
   * <p>Subjects may only be added within a root-like session.</p>
   * <pre class="eg">
   * try {
   *   RegistrySubject subj = RegistrySubject.add(s, "subject id", "person", "name");
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
   * @return  The created {@link RegistrySubject}.
   * @throws  GrouperException
   * @throws  InsufficientPrivilegeException
   * @since   1.2.0
   */
  public static RegistrySubject add(GrouperSession s, String id, String type, String name)
      throws  GrouperException,
              InsufficientPrivilegeException
  {
    //note, no need for GrouperSession inverse of control
    if ( !PrivilegeHelper.isRoot(s) ) {
      throw new InsufficientPrivilegeException(E.ROOTLIKE_TO_ADD_HSUBJ);
    }    
    try {
      GrouperDAOFactory.getFactory().getRegistrySubject().find(id, type, true);
      throw new GrouperException(E.SUBJ_ALREADY_EXISTS + id + "/" + type + "/" + name);
    }
    catch (SubjectNotFoundException eSNF) {
      RegistrySubject subj  = new RegistrySubject();
      subj.setId(id);
      subj.setName(name);
      subj.setTypeString(type);
      GrouperDAOFactory.getFactory().getRegistrySubject().create(subj);
      return subj;
    }
  } // public static RegistrySubject add(s, id, type, name)


  /**
   * Delete existing {@link RegistrySubject}.
   * <pre>
   * try {
   *   rSubj.delete(s);
   * }
   * catch (GrouperException eG) {
   *   // failed to delete this RegistrySubject
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // not privileged to delete this RegistrySubject
   * }
   * </pre>
   * @param   s   Delete <i>RegistrySubject</i> within this <i>GrouperSession</i> context.
   * @throws  GrouperException  if <i>RegistrySubject</i> cannot be deleted.
   * @throws  IllegalStateException if <i>GrouperSession</i> is null.
   * @throws  InsufficientPrivilegeException if not privileged to delete <i>RegistrySubject</i>s.
   * @since   1.2.0
   */
  public void delete(GrouperSession s) 
    throws  GrouperException,
            IllegalStateException,
            InsufficientPrivilegeException
  {
    //note, no need for GrouperSession inverse of control
    if (s == null) {
      throw new IllegalStateException("null session");
    }
    if ( !PrivilegeHelper.isRoot(s) ) {
      throw new InsufficientPrivilegeException("must be root-like to delete RegistrySubjects");
    }    
    try {
      GrouperDAOFactory.getFactory().getRegistrySubject().delete( this );
    }
    catch (GrouperDAOException eDAO) {
      throw new GrouperException( eDAO.getMessage(), eDAO );
    }
  }
  
  /**
   * Return the value of the specified attribute.
   * 
   * @param name 
   * @return attribute value
   */
  public String getAttributeValue(String name) {
    return this.subject.getAttributeValue(name);
  } // public String getAttributevalue(name)

  /**
   * Return the values for the specified attribute.
   * 
   * @param name 
   * @return attributes
   */
  public Set<String> getAttributeValues(String name) {
    return this.subject.getAttributeValues(name);
  }

  /**
   * Return this subject's description.
   * 
   * @return description
   */
  public String getDescription() {
    //if theres a description, use it
//    String attributeDescription = this.getAttributeValue("description");
//    if (!StringUtils.isBlank(attributeDescription)) {
//      return attributeDescription;
//    }
    return this.subject.getDescription();
  } 

  /**
   * Return the subject id.
   * 
   * @return id
   */
  public String getId() {
    return this.subject.getId();
  }

  /**
   * Return the subject's name.
   * 
   * @return the name
   */
  public String getName() {
    return this.subject.getName();
  } 

  /**
   * Return the source.
   * 
   * <p><b>NOTE:</b> The current implementation is very crude and inefficient.  It
   * attempts to query for the subject to identify the source.</p>
   * @return the source
   * @throws  IllegalStateException if source cannot be returned.
   * @since   1.2.0
   */
  public Source getSource() 
    throws  IllegalStateException {
    return this.subject.getSource();
  } 

  /**
   * Return this subject's {@link SubjectType}.
   * @return  type
   */
  public SubjectType getType() {
    return this.subject.getType();
  }


  /**
   * @return type string
   */
  public String getTypeString() {
    return this.subject.getTypeName();
  }



  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    return this.subject.equals(obj);
  }


  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return this.subject.hashCode();
  }

  /**
   * @param id 
   */
  public void setId(String id) {
    this.subject.setId(id);
  }

  /**
   * 
   * @param name
   */
  public void setName(String name) {
    this.subject.setName(name);
  }



  /**
   * @param type 
   */
  public void setTypeString(String type) {
    this.subject.setTypeName(type);
  }
  
  /**
   * 
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return this.subject.toString();
  }


  /**
   * @see edu.internet2.middleware.subject.Subject#getAttributeValueOrCommaSeparated(java.lang.String)
   */
  public String getAttributeValueOrCommaSeparated(String attributeName) {
    return this.subject.getAttributeValueOrCommaSeparated(attributeName);
  }


  /**
   * @see edu.internet2.middleware.subject.Subject#getAttributeValueSingleValued(java.lang.String)
   */
  public String getAttributeValueSingleValued(String attributeName) {
    return this.subject.getAttributeValueSingleValued(attributeName);
  }


  /**
   * @see edu.internet2.middleware.subject.Subject#getSourceId()
   */
  public String getSourceId() {
    return this.subject.getSourceId();
  }


  /**
   * @see edu.internet2.middleware.subject.Subject#getTypeName()
   */
  public String getTypeName() {
    return this.subject.getTypeName();
  }


  /**
   * @see edu.internet2.middleware.subject.Subject#getAttributes()
   */
  public Map<String, Set<String>> getAttributes() {
    return this.subject.getAttributes();
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getAttributeValue(java.lang.String, boolean)
   */
  public String getAttributeValue(String attributeName, boolean excludeInternalAttributes) {
    return this.subject.getAttributeValue(attributeName, excludeInternalAttributes);
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getAttributeValueOrCommaSeparated(java.lang.String, boolean)
   */
  public String getAttributeValueOrCommaSeparated(String attributeName, boolean excludeInternalAttributes) {
    return this.subject.getAttributeValueOrCommaSeparated(attributeName, excludeInternalAttributes);
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getAttributeValueSingleValued(java.lang.String, boolean)
   */
  public String getAttributeValueSingleValued(String attributeName, boolean excludeInternalAttributes) {
    return this.subject.getAttributeValueSingleValued(attributeName, excludeInternalAttributes);
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getAttributeValues(java.lang.String, boolean)
   */
  public Set<String> getAttributeValues(String attributeName, boolean excludeInternalAttributes) {
    return this.subject.getAttributeValues(attributeName, excludeInternalAttributes);
  }

  /**
   * @see edu.internet2.middleware.subject.Subject#getAttributes(boolean)
   */
  public Map<String, Set<String>> getAttributes(boolean excludeInternalAttributes) {
    return this.subject.getAttributes(excludeInternalAttributes);
  } 
  
}

