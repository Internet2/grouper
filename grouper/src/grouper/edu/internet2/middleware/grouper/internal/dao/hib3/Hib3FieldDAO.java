/**
 * Copyright 2012 Internet2
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
import java.util.Set;

import org.apache.commons.logging.Log;
import org.hibernate.HibernateException;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.hibernate.ByHqlStatic;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.FieldDAO;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Basic Hibernate <code>Field</code> DAO interface.
 * @author  blair christensen.
 * @version $Id: Hib3FieldDAO.java,v 1.16 2009-11-17 02:52:29 mchyzer Exp $
 * @since   @HEAD@
 */
public class Hib3FieldDAO extends Hib3DAO implements FieldDAO {

  /** */
  private static final String KLASS = Hib3FieldDAO.class.getName();

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(Hib3FieldDAO.class);

  /**
   * @param name 
   * @return if exists
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public boolean existsByName(String name) 
    throws  GrouperDAOException
  {
    Object id = null;
    
    try {
      id = HibernateSession.byHqlStatic()
      .createQuery("select f.id from Field f where f.name = :name")
      .setString("name", name).uniqueResult(Object.class);
    } catch (GrouperDAOException gde) {
      Throwable throwable = gde.getCause();
      //CH 20080218 this was legacy error handling
      if (throwable instanceof HibernateException) {
        LOG.fatal( throwable.getMessage() );
      }
      throw gde;
    }
    boolean rv  = false;
    if ( id != null ) {
      rv = true; 
    }
    return rv;
  } // public boolean existsByName(name)
  
  /**
   * @return set of fields
   * @throws GrouperException 
   * @since   @HEAD@
   */
  public Set<Field> findAll() 
    throws  GrouperException
  {
    return HibernateSession.byHqlStatic()
      .createQuery("from Field order by name asc")
      .setCacheable(true)
      .setCacheRegion(KLASS + ".FindAll").listSet(Field.class);
  } // public Set findAll()

  /**
   * @param type 
   * @return set of fields
   * @throws GrouperDAOException 
   * @since   @HEAD@
   * @deprecated use the FieldFinder instead
   */
  @Deprecated
  public Set<Field> findAllByType(FieldType type) 
    throws  GrouperDAOException
  {
    return HibernateSession.byHqlStatic()
       .createQuery("from Field where type = :type order by name asc")
       .setCacheable(false)
       .setCacheRegion(KLASS + ".FindAllByType")
       .setString( "type", type.toString() ).listSet(Field.class);
  } // public Set fieldAllByType(type)

  /**
   * @param f 
   * @return if in use
   * @throws GrouperDAOException 
   * @throws SchemaException 
   * @since   @HEAD@
   */
  public boolean isInUse(Field f) 
    throws  GrouperDAOException,
            SchemaException
  {
    ByHqlStatic qry = HibernateSession.byHqlStatic();
    if ( f.getType().equals(FieldType.LIST) )      {
      qry.createQuery("select ms from MembershipEntry as ms, Field as field where field.name = :name and field.uuid = ms.fieldId");
    } else {
      throw new SchemaException( f.getType().toString() );
    }
    qry.setCacheable(false);
    qry.setString("name", f.getName() );
    if (qry.list(Object.class).size() > 0) {
      return true;
    }
    return false;
  } // public boolean isInUse(f)

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.FieldDAO#createOrUpdate(edu.internet2.middleware.grouper.Field)
   */
  public void createOrUpdate(final Field field) {
    HibernateSession.byObjectStatic().saveOrUpdate(field);
    FieldFinder.clearCache();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.FieldDAO#findByUuidOrName(java.lang.String, java.lang.String, boolean)
   */
  public Field findByUuidOrName(String uuid, String name, 
      boolean exceptionIfNull) throws GrouperDAOException {
    return findByUuidOrName(uuid, name, exceptionIfNull, null);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.FieldDAO#findByUuidOrName(java.lang.String, java.lang.String, boolean, QueryOptions)
   */
  public Field findByUuidOrName(String uuid, String name,
      boolean exceptionIfNull, QueryOptions queryOptions) throws GrouperDAOException {
    try {
      Field field = HibernateSession.byHqlStatic()
        .createQuery("from Field as theField where theField.uuid = :uuid or theField.name = :name")
        .setCacheable(true)
        .setCacheRegion(KLASS + ".FindByUuidOrName")
        .options(queryOptions)
        .setString("uuid", uuid)
        .setString("name", name)
        .uniqueResult(Field.class);
      if (field == null && exceptionIfNull) {
        throw new RuntimeException("Can't find field by uuid: '" + uuid + "' or name '" + name + "'");
      }
      return field;
    }
    catch (GrouperDAOException e) {
      String error = "Problem find field by uuid: '" 
        + uuid + "' or name '" + name + "', " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.FieldDAO#saveUpdateProperties(edu.internet2.middleware.grouper.Field)
   */
  public void saveUpdateProperties(Field field) {
    //run an update statement since the business methods affect these properties
    HibernateSession.byHqlStatic().createQuery("update Field " +
        "set hibernateVersionNumber = :theHibernateVersionNumber, " +
        "contextId = :theContextId " +
        "where uuid = :theUuid")
        .setLong("theHibernateVersionNumber", field.getHibernateVersionNumber())
        .setString("theContextId", field.getContextId())
        .setString("theUuid", field.getUuid())
        .executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.FieldDAO#update(edu.internet2.middleware.grouper.Field)
   */
  public void update(Field field) throws GrouperDAOException {
    HibernateSession.byObjectStatic().update(field);  
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.FieldDAO#delete(edu.internet2.middleware.grouper.Field)
   */
  public void delete(Field field) {
    HibernateSession.byObjectStatic().delete(field);  
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.FieldDAO#delete(java.util.Set)
   */
  public void delete(Set<Field> fields) {
    HibernateSession.byObjectStatic().delete(fields);  
  }
  
  /**
   *
   * @param hibernateSession
   * @throws HibernateException
   */
  protected static void reset(HibernateSession hibernateSession)
    throws  HibernateException {

    //delete custom lists
    hibernateSession.byHql().createQuery("delete from Field field where typeString='list' and name <> 'members'").executeUpdate();   
  }
} 

