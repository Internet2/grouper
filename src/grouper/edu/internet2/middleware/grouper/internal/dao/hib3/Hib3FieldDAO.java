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

package edu.internet2.middleware.grouper.internal.dao.hib3;
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
import  org.hibernate.*;

/**
 * Basic Hibernate <code>Field</code> DAO interface.
 * <p><b>WARNING: THIS IS AN ALPHA INTERFACE THAT MAY CHANGE AT ANY TIME.</b></p>
 * @author  blair christensen.
 * @version $Id: Hib3FieldDAO.java,v 1.2 2008-02-17 08:44:42 mchyzer Exp $
 * @since   @HEAD@
 */
public class Hib3FieldDAO extends Hib3DAO implements FieldDAO {

  // PRIVATE CLASS CONSTANTS //
  private static final String KLASS = Hib3FieldDAO.class.getName();


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
   * @since   @HEAD@
   */
  public boolean existsByName(String name) 
    throws  GrouperDAOException
  {
    try {
      Session hs  = Hib3DAO.getSession();
      Query   qry = hs.createQuery("select f.id from Hib3FieldDAO f where f.name = :name");
      qry.setString("name", name);
      boolean rv  = false;
      if ( qry.uniqueResult() != null ) {
        rv = true; 
      }
      hs.close();
      return rv;
    }
    catch (HibernateException eH) {
      ErrorLog.fatal( Hib3FieldDAO.class, eH.getMessage() );
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } // public boolean existsByName(name)
  
  /**
   * @since   @HEAD@
   */
  public Set findAll() 
    throws  GrouperRuntimeException
  {
    Set fields = new LinkedHashSet();
    try {
      Session hs  = Hib3DAO.getSession();
      Query   qry = hs.createQuery("from Hib3FieldDAO order by name asc");
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
   * @since   @HEAD@
   */
  public Set findAllFieldsByGroupType(String uuid)
    throws  GrouperDAOException
  {
    Set fields = new LinkedHashSet();
    try {
      Session hs  = Hib3DAO.getSession();
      Query   qry = hs.createQuery("from Hib3FieldDAO as f where f.groupTypeUuid = :uuid order by f.name asc");
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
   * @since   @HEAD@
   */
  public Set findAllByType(FieldType type) 
    throws  GrouperDAOException
  {
    Set fields = new LinkedHashSet();
    try {
      Session hs  = Hib3DAO.getSession();
      Query   qry = hs.createQuery("from Hib3FieldDAO where type = :type order by name asc");
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
   * @since   @HEAD@
   */
  public String getGroupTypeUuid() {
    return this.groupTypeUUID;
  }

  /**
   * @since   @HEAD@
   */
  public String getId() {
    return this.id;
  }

  /**
   * @since   @HEAD@
   */
  public boolean getIsNullable() {
    return this.isNullable;
  }

  /**
   * @since   @HEAD@
   */
  public String getName() {
    return this.name;
  }

  /**
   * @since   @HEAD@
   */
  public String getReadPrivilege() {
    return this.readPrivilege;
  }

  /** 
   * @since   @HEAD@
   */
  public String getType() {
    return this.type;
  }

  /**
   * @since   @HEAD@
   */
  public String getUuid() {
    return this.uuid;
  }

  /** 
   * @since   @HEAD@
   */
  public String getWritePrivilege() {
    return this.writePrivilege;
  }

  /**
   * @since   @HEAD@
   */
  public boolean isInUse(Field f) 
    throws  GrouperDAOException,
            SchemaException
  {
    try {
      Session hs  = Hib3DAO.getSession();
      Query   qry = null;
      if      ( f.getType().equals(FieldType.ATTRIBUTE) ) {
        qry = hs.createQuery("from Hib3AttributeDAO as a where a.attrName = :name");
      }
      else if ( f.getType().equals(FieldType.LIST) )      {
        qry = hs.createQuery("from Hib3MembershipDAO as ms where ms.listName = :name");
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
   * @since   @HEAD@
   */
  public FieldDAO setGroupTypeUuid(String groupTypeUUID) {
    this.groupTypeUUID = groupTypeUUID;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public FieldDAO setId(String id) {
    this.id = id;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public FieldDAO setIsNullable(boolean isNullable) {
    this.isNullable = isNullable;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public FieldDAO setName(String name) {
    this.name = name;
    return this;
  }

  /** 
   * @since   @HEAD@
   */
  public FieldDAO setReadPrivilege(String readPrivilege) {
    this.readPrivilege = readPrivilege;
    return this;
  }

  /** 
   * @since   @HEAD@
   */
  public FieldDAO setType(String type) {
    this.type = type;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public FieldDAO setUuid(String uuid) {
    this.uuid = uuid;
    return this;
  }

  /** 
   * @since   @HEAD@
   */
  public FieldDAO setWritePrivilege(String writePrivilege) {
    this.writePrivilege = writePrivilege;
    return this;
  }

} 

