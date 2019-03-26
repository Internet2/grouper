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

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.subj.cache.SubjectSourceCache;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;
import edu.internet2.middleware.subject.SubjectType;
import edu.internet2.middleware.subject.provider.SubjectImpl;

/** 
 * A {@link Subject} local to the Groups Registry.
 * <p/>
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
      throws  GrouperException, InsufficientPrivilegeException {
    if (GrouperConfig.retrieveConfig().propertyValueBoolean("create.attributes.when.creating.registry.subjects", true)) {
      return add(s, id, type, name, "name." + id, "id." + id, "description." + id, id + "@somewhere.someSchool.edu");
    }
    return add(s, id, type, name, null, null, null, null);
  } // public static RegistrySubject add(s, id, type, name)

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
   * @param nameAttributeValue 
   * @param loginid 
   * @param description 
   * @param email 
   * @return  The created {@link RegistrySubject}.
   * @throws  GrouperException
   * @throws  InsufficientPrivilegeException
   * @since   2.4.0
   */
  public static RegistrySubject add(GrouperSession s, String id, String type, String name, String nameAttributeValue, String loginid, String description, String email)
      throws  GrouperException, InsufficientPrivilegeException {
    //note, no need for GrouperSession inverse of control
    if ( !PrivilegeHelper.isRoot(s) ) {
      throw new InsufficientPrivilegeException(E.ROOTLIKE_TO_ADD_HSUBJ);
    }    
    try {
      GrouperDAOFactory.getFactory().getRegistrySubject().find(id, true);
      throw new GrouperException(E.SUBJ_ALREADY_EXISTS + id + "/" + name);
    }
    catch (SubjectNotFoundException eSNF) {
      RegistrySubject subj  = new RegistrySubject();
      subj.setId(id);
      subj.setName(name);
      subj.setTypeString(type);
      
      if (!StringUtils.isBlank(name)) {
        subj.getAttributes(false).put("name", GrouperUtil.toSet(nameAttributeValue));
      }
      
      if (!StringUtils.isBlank(loginid)) {
        subj.getAttributes(false).put("loginid", GrouperUtil.toSet(loginid));
      }

      if (!StringUtils.isBlank(description)) {
        subj.getAttributes(false).put("description", GrouperUtil.toSet(description));
      }
      
      if (!StringUtils.isBlank(email)) {
        subj.getAttributes(false).put("email", GrouperUtil.toSet(email));
      }
      
      GrouperDAOFactory.getFactory().getRegistrySubject().create(subj);
      
      try {
        SubjectSourceCache.clearCache();

        SubjectFinder.findById(id, true);
      } catch (SubjectNotFoundException snfe) {
        if (!GrouperConfig.retrieveConfig().propertyValueBoolean("allow.registry.subjects.without.resolution", false)) {
          throw new RuntimeException("Error: your RegistrySubject was not found after creation: '" + id + "', you need a source (e.g. the Grouper jdbc source) to resolve registry subjects!");
        }
      } catch (SubjectNotUniqueException snue) {
        // this is ok
      }
      
      return subj;
    }
  } // public static RegistrySubject add(s, id, type, name)


  /**
   * Add or update a {@link Subject} to a {@link Source} within the Groups Registry.
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
   * @param loginid 
   * @param description 
   * @param email 
   * @return  The created {@link RegistrySubject}.
   * @throws  GrouperException
   * @throws  InsufficientPrivilegeException
   * @since   2.4.0
   */
  public static RegistrySubject addOrUpdate(GrouperSession s, String id, String type, String name, String nameAttributeValue, String loginid, String description, String email)
      throws  GrouperException, InsufficientPrivilegeException {
    //note, no need for GrouperSession inverse of control
    if ( !PrivilegeHelper.isRoot(s) ) {
      throw new InsufficientPrivilegeException(E.ROOTLIKE_TO_ADD_HSUBJ);
    }    
    RegistrySubject registrySubject = GrouperDAOFactory.getFactory().getRegistrySubject().find(id, false);
    if (registrySubject == null) {
      return add(s, id, type, name, nameAttributeValue, loginid, description, email);
    } 
    if (!StringUtils.equals(type, registrySubject.getTypeString())) {
      registrySubject.setTypeString(type);
    }
    if (!StringUtils.equals(name, registrySubject.getName())) {
      registrySubject.setName(name);
    }
    addOrUpdateOrDeleteAttribute(registrySubject, id, "loginid", loginid);
    addOrUpdateOrDeleteAttribute(registrySubject, id, "name", nameAttributeValue);
    addOrUpdateOrDeleteAttribute(registrySubject, id, "description", description);
    addOrUpdateOrDeleteAttribute(registrySubject, id, "email", email);
    return registrySubject;
  }

  /**
   * 
   * @param id
   * @param exceptionIfNotFound
   * @return the subject or null
   */
  public static RegistrySubject find(String id, boolean exceptionIfNotFound) {
    
    RegistrySubject registrySubject = GrouperDAOFactory.getFactory().getRegistrySubject().find(id, false);  
    if (registrySubject == null) {
      if (exceptionIfNotFound) {
        throw new RuntimeException("Registry subject not found '" + id + "'");
      }
      return null;
    }

    String subjectSourceId = GrouperConfig.retrieveConfig().propertyValueString("registrySubjectSourceId", "jdbc");

    Subject theSubject = SubjectFinder.findByIdAndSource(id, subjectSourceId, false);

    if (theSubject == null) {
      try {
        theSubject = SubjectFinder.findById(id, false);
      } catch (SubjectNotUniqueException snue) {
        throw new RuntimeException("Error: your RegistrySubject was not found after creation: '" + id + "', you need to set the RegistyrSubject source id in grouper.properties: registrySubjectSourceId", snue);
      }
      if (theSubject == null) {
        throw new RuntimeException("Error: your RegistrySubject was not found after creation: '" + id + "', you need a source (e.g. the Grouper jdbc source) to resolve registry subjects!");
      }
    }

    registrySubject.subject = (SubjectImpl)theSubject;
    return registrySubject;
  }

  /**
   * @param registrySubject
   * @param subjectId
   * @param attributeName
   * @param attributeValue
   */
  public static void addOrUpdateOrDeleteAttribute(RegistrySubject registrySubject, String subjectId, String attributeName, String attributeValue) {

    RegistrySubjectAttribute registrySubjectAttribute = GrouperDAOFactory.getFactory().getRegistrySubjectAttribute().find(subjectId, attributeName, false);
    if (StringUtils.isBlank(attributeValue) &&  registrySubjectAttribute == null) {
      
      registrySubject.getAttributes(false).remove(attributeName);
      // we good
      
      
    } else if (registrySubjectAttribute != null && StringUtils.equals(attributeValue, registrySubjectAttribute.getValue())) {
      
      registrySubject.getAttributes(false).put(attributeName, GrouperUtil.toSet(attributeValue) );
      // we good
      
      
    } else if (StringUtils.isBlank(attributeValue)) {

      registrySubject.getAttributes(false).remove(attributeName);
      GrouperDAOFactory.getFactory().getRegistrySubjectAttribute().delete(registrySubjectAttribute);
      
    } else {
      
      registrySubject.getAttributes(false).put(attributeName, GrouperUtil.toSet(attributeValue) );
      registrySubjectAttribute.setValue(attributeValue);
      registrySubjectAttribute.setSearchValue(attributeValue.toLowerCase());
      GrouperDAOFactory.getFactory().getRegistrySubjectAttribute().update(registrySubjectAttribute);
      
    }
  }

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
      
      for (RegistrySubjectAttribute registrySubjectAttribute : GrouperDAOFactory.getFactory().getRegistrySubjectAttribute().findByRegistrySubjectId(this.getId())) {
        GrouperDAOFactory.getFactory().getRegistrySubjectAttribute().delete( registrySubjectAttribute );
      }
      
      GrouperDAOFactory.getFactory().getRegistrySubject().delete( this );
    }
    catch (GrouperDAOException eDAO) {
      throw new GrouperException( eDAO.getMessage(), eDAO );
    }
  }
  
  /**
   * Return the value of the specified attribute.
   * <p/>
   * @param name 
   * @return attribute value
   */
  public String getAttributeValue(String name) {
    return this.subject.getAttributeValue(name);
  } // public String getAttributevalue(name)

  /**
   * Return the values for the specified attribute.
   * <p/>
   * @param name 
   * @return attributes
   */
  public Set<String> getAttributeValues(String name) {
    return this.subject.getAttributeValues(name);
  }

  /**
   * Return this subject's description.
   * <p/>
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
   * <p/>
   * @return id
   */
  public String getId() {
    return this.subject.getId();
  }

  /**
   * Return the subject's name.
   * <p/>
   * @return the name
   */
  public String getName() {
    return this.subject.getName();
  } 

  /**
   * Return the source.
   * <p/>
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

