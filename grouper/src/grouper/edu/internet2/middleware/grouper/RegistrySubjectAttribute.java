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
import java.io.Serializable;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotUniqueException;
import edu.internet2.middleware.subject.provider.SubjectImpl;

/** 
 * Hibernate representation of the JDBC <code>SubjectAttribute</code> table.
 * @author  blair christensen.
 * @version $Id: RegistrySubjectAttribute.java,v 1.2 2008-10-21 03:51:03 mchyzer Exp $
 * @since   @HEAD@
 */
public class RegistrySubjectAttribute implements Serializable {

  // PUBLIC CLASS CONSTANTS //
  public static final long serialVersionUID = -4979920855853791786L;


  // HIBERNATE PROPERTIES //
  private String name;
  private String searchValue;
  private String subjectId;
  private String value;


  // CONSTRUCTORS //

  /**
   * For Hibernate.
   * @since   @HEAD@
   */
  public RegistrySubjectAttribute() {
    super();
  }

  // @since   @HEAD@
  protected RegistrySubjectAttribute(
    String id, String name, String value, String searchVal
  )
  {
    this.setName(         name      );
    this.setSearchValue(  searchVal );
    this.setSubjectId(    id        );
    this.setValue(        value     );
  } 


  // PUBLIC INSTANCE METHODS //

  /**
   * @since   @HEAD@
   */
  public boolean equals(Object other) {
    if ( (this == other ) ) return true;
    if ( !(other instanceof RegistrySubjectAttribute) ) return false;
    RegistrySubjectAttribute castOther = (RegistrySubjectAttribute) other;
    return new EqualsBuilder()
      .append(this.getSubjectId() , castOther.getSubjectId()  )
      .append(this.getName()      , castOther.getName()       )
      .append(this.getValue()     , castOther.getValue()      )
      .isEquals();
  } 

  /**
   * @since   @HEAD@
   */
  public int hashCode() {
    return new HashCodeBuilder()
      .append(getSubjectId())
      .append(getName()     )
      .append(getValue()    )
      .toHashCode();
  }

  
  // PRIVATE INSTANCE METHODS //
  
  // @since   @HEAD@
  public String getName() {
    return this.name;
  }
  // @since   @HEAD@
  public String getSearchValue() {
    return this.searchValue;
  }
  // @since   @HEAD@
  public String getSubjectId() {
    return this.subjectId;
  }
  // @since   @HEAD@
  public String getValue() {
    return this.value;
  }
  // @since   @HEAD@
  public RegistrySubjectAttribute setName(String name) {
    this.name = name;
    return this;
  }
  // @since   @HEAD@
  public RegistrySubjectAttribute setSearchValue(String value) {
    this.searchValue = value;
    return this;
  }
  // @since   @HEAD@
  public RegistrySubjectAttribute setSubjectId(String subjectId) {
    this.subjectId = subjectId;
    return this;
  }
  // @since   @HEAD@
  public RegistrySubjectAttribute setValue(String value) {
    this.value = value;
    return this;
  }

  /**
   * Delete existing {@link RegistrySubjectAttribute}.
   * <pre>
   * try {
   *   rSubjAttr.delete(s);
   * }
   * catch (GrouperException eG) {
   *   // failed to delete this RegistrySubject
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // not privileged to delete this RegistrySubject
   * }
   * </pre>
   * @throws  GrouperException  if <i>RegistrySubjectAttribute</i> cannot be deleted.
   * @throws  IllegalStateException if <i>GrouperSession</i> is null.
   * @throws  InsufficientPrivilegeException if not privileged to delete <i>RegistrySubject</i>s.
   * @since   2.4.0
   */
  public void delete() 
    throws  GrouperException,
            IllegalStateException,
            InsufficientPrivilegeException
  {
    //note, no need for GrouperSession inverse of control
    GrouperSession s = GrouperSession.staticGrouperSession();
    if (s == null) {
      throw new IllegalStateException("null session");
    }
    if ( !PrivilegeHelper.isRoot(s) ) {
      throw new InsufficientPrivilegeException("must be root-like to delete RegistrySubjectAttributes");
    }    
    try {
      GrouperDAOFactory.getFactory().getRegistrySubjectAttribute().delete( this );
    }
    catch (GrouperDAOException eDAO) {
      throw new GrouperException( eDAO.getMessage(), eDAO );
    }
  }

  /**
   * Delete existing {@link RegistrySubjectAttribute}.
   * <pre>
   * try {
   *   rSubjAttr.delete(s);
   * }
   * catch (GrouperException eG) {
   *   // failed to delete this RegistrySubject
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // not privileged to delete this RegistrySubject
   * }
   * </pre>
   * @throws  GrouperException  if <i>RegistrySubjectAttribute</i> cannot be deleted.
   * @throws  IllegalStateException if <i>GrouperSession</i> is null.
   * @throws  InsufficientPrivilegeException if not privileged to delete <i>RegistrySubject</i>s.
   * @since   2.4.0
   */
  public void store() 
    throws  GrouperException,
            IllegalStateException,
            InsufficientPrivilegeException
  {
    //note, no need for GrouperSession inverse of control
    GrouperSession s = GrouperSession.staticGrouperSession();
    if (s == null) {
      throw new IllegalStateException("null session");
    }
    if ( !PrivilegeHelper.isRoot(s) ) {
      throw new InsufficientPrivilegeException("must be root-like to delete RegistrySubjectAttributes");
    }    
    if (StringUtils.isBlank(this.subjectId)) {
      throw new RuntimeException("needs subjectId");
    }
    if (StringUtils.isBlank(this.name)) {
      throw new RuntimeException("needs attribute name");
    }
    RegistrySubjectAttribute existing = find(this.subjectId, this.name, false);
    if (existing == null) {
      GrouperDAOFactory.getFactory().getRegistrySubjectAttribute().create(existing);
    } else {
      GrouperDAOFactory.getFactory().getRegistrySubjectAttribute().update(existing);
    }
  }

  /**
   * @param subjectId 
   * @param attributeName 
   * @param exceptionIfNotFound
   * @return the attribute or null
   */
  public static RegistrySubjectAttribute find(String subjectId, String attributeName, boolean exceptionIfNotFound) {
    
    RegistrySubjectAttribute registrySubjectAttribute = GrouperDAOFactory.getFactory().getRegistrySubjectAttribute().find(subjectId, attributeName, false);  
    if (registrySubjectAttribute == null) {
      if (exceptionIfNotFound) {
        throw new RuntimeException("Registry subject attribute not found '" + subjectId + "', '" + attributeName + "'");
      }
      return null;
    }
    return registrySubjectAttribute;
  }

  /**
   * Add or update registry subject attribute
   * @param subjectId 
   * @param attributeName 
   * @param value 
   * @return  The created {@link RegistrySubjectAttribute}.
   * @since   2.4.0
   */
  public static RegistrySubjectAttribute addOrUpdate(String subjectId, String attributeName, String value) {
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    //note, no need for GrouperSession inverse of control
    if ( !PrivilegeHelper.isRoot(grouperSession) ) {
      throw new InsufficientPrivilegeException(E.ROOTLIKE_TO_ADD_HSUBJ);
    }    
    RegistrySubjectAttribute registrySubjectAttribute = new RegistrySubjectAttribute();
    registrySubjectAttribute.setSubjectId(subjectId);
    registrySubjectAttribute.setName(attributeName);
    registrySubjectAttribute.setValue(value);
    registrySubjectAttribute.setSearchValue(value == null ? null : value.toLowerCase());
    registrySubjectAttribute.store();
    return registrySubjectAttribute;
  }


}

