/* Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
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

package edu.internet2.middleware.grouper.internal.dao.hibernate;
import  edu.internet2.middleware.grouper.ErrorLog;
import  edu.internet2.middleware.grouper.Field;
import  edu.internet2.middleware.grouper.FieldType;
import  edu.internet2.middleware.grouper.GrouperRuntimeException;
import  edu.internet2.middleware.grouper.SchemaException;
import  edu.internet2.middleware.grouper.internal.dao.FieldDAO;
import  edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import  edu.internet2.middleware.grouper.internal.dto.FieldDTO;
import  java.util.Iterator;
import  java.util.LinkedHashSet;
import  java.util.Set;
import  net.sf.hibernate.*;

/**
 * Basic Hibernate <code>Field</code> DAO interface.
 * <p><b>WARNING: THIS IS AN ALPHA INTERFACE THAT MAY CHANGE AT ANY TIME.</b></p>
 * @author  blair christensen.
 * @version $Id: HibernateFieldDAO.java,v 1.7 2008-02-17 08:44:42 mchyzer Exp $
 * @since   1.2.0
 */
public class HibernateFieldDAO extends HibernateDAO implements FieldDAO {

  // PRIVATE CLASS CONSTANTS //
  private static final String KLASS = HibernateFieldDAO.class.getName();


  // PRIVATE INSTANCE VARIABLES //
  private String  groupTypeUUID;
  private String  id;
  private boolean isNullable;
  private String  name;
  private String  readPrivilege;
  private String  type;
  private String  uuid;
  private String  writePrivilege;


  // PUBliC INSTANCE METHODS //

  /**
   * @since   1.2.0
   */
  public boolean existsByName(String name) 
    throws  GrouperDAOException
  {
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("select f.id from HibernateFieldDAO f where f.name = :name");
      qry.setString("name", name);
      boolean rv  = false;
      if ( qry.uniqueResult() != null ) {
        rv = true; 
      }
      hs.close();
      return rv;
    }
    catch (HibernateException eH) {
      ErrorLog.fatal( HibernateFieldDAO.class, eH.getMessage() );
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } // public boolean existsByName(name)
  
  /**
   * @since   1.2.0
   */
  public Set findAll() 
    throws  GrouperRuntimeException
  {
    Set fields = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from HibernateFieldDAO order by name asc");
      qry.setCacheable(false);
      qry.setCacheRegion(KLASS + ".FindAll");
      Iterator it = qry.list().iterator();
      while (it.hasNext()) {
        fields.add( (FieldDTO) FieldDTO.getDTO( (FieldDAO) it.next() ) );
      }
      hs.close();  
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return fields;
  } // public Set findAll()

  /** 
   * @since   1.2.0
   */
  public Set findAllFieldsByGroupType(String uuid)
    throws  GrouperDAOException
  {
    Set fields = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from HibernateFieldDAO as f where f.groupTypeUuid = :uuid order by f.name asc");
      qry.setCacheable(false);
      qry.setCacheRegion(KLASS + ".FindAllFieldsByGroupType");
      qry.setString("uuid", uuid);
      //TODO CH 20080217: replace with query.list() and see if p6spy generates fewer queries
      Iterator it = qry.iterate();
      while (it.hasNext()) {
        fields.add( (FieldDTO) FieldDTO.getDTO( (FieldDAO) it.next() ) );
      }
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return fields;
  } 

  /**
   * @since   1.2.0
   */
  public Set findAllByType(FieldType type) 
    throws  GrouperDAOException
  {
    Set fields = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from HibernateFieldDAO where type = :type order by name asc");
      qry.setCacheable(false);
      qry.setCacheRegion(KLASS + ".FindAllByType");
      qry.setString( "type", type.toString() );
      Iterator it = qry.list().iterator();
      while (it.hasNext()) {
        fields.add( (FieldDTO) FieldDTO.getDTO( (FieldDAO) it.next() ) );
      }
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return fields;
  } // public Set fieldAllByType(type)

  /** 
   * @since   1.2.0
   */
  public String getGroupTypeUuid() {
    return this.groupTypeUUID;
  }

  /**
   * @since   1.2.0
   */
  public String getId() {
    return this.id;
  }

  /**
   * @since   1.2.0
   */
  public boolean getIsNullable() {
    return this.isNullable;
  }

  /**
   * @since   1.2.0
   */
  public String getName() {
    return this.name;
  }

  /**
   * @since   1.2.0
   */
  public String getReadPrivilege() {
    return this.readPrivilege;
  }

  /** 
   * @since   1.2.0
   */
  public String getType() {
    return this.type;
  }

  /**
   * @since   1.2.0
   */
  public String getUuid() {
    return this.uuid;
  }

  /** 
   * @since   1.2.0
   */
  public String getWritePrivilege() {
    return this.writePrivilege;
  }

  /**
   * @since   1.2.0
   */
  public boolean isInUse(Field f) 
    throws  GrouperDAOException,
            SchemaException
  {
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = null;
      if      ( f.getType().equals(FieldType.ATTRIBUTE) ) {
        qry = hs.createQuery("from HibernateAttributeDAO as a where a.attrName = :name");
      }
      else if ( f.getType().equals(FieldType.LIST) )      {
        qry = hs.createQuery("from HibernateMembershipDAO as ms where ms.listName = :name");
      }
      else {
        throw new SchemaException( f.getType().toString() );
      }
      qry.setCacheable(false);
      qry.setString("name", f.getName() );
      if (qry.list().size() > 0) {
        return true;
      }
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return false;
  } // public boolean isInUse(f)

  /** 
   * @since   1.2.0
   */
  public FieldDAO setGroupTypeUuid(String groupTypeUUID) {
    this.groupTypeUUID = groupTypeUUID;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public FieldDAO setId(String id) {
    this.id = id;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public FieldDAO setIsNullable(boolean isNullable) {
    this.isNullable = isNullable;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public FieldDAO setName(String name) {
    this.name = name;
    return this;
  }

  /** 
   * @since   1.2.0
   */
  public FieldDAO setReadPrivilege(String readPrivilege) {
    this.readPrivilege = readPrivilege;
    return this;
  }

  /** 
   * @since   1.2.0
   */
  public FieldDAO setType(String type) {
    this.type = type;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public FieldDAO setUuid(String uuid) {
    this.uuid = uuid;
    return this;
  }

  /** 
   * @since    1.2.0
   */
  public FieldDAO setWritePrivilege(String writePrivilege) {
    this.writePrivilege = writePrivilege;
    return this;
  }

} 

