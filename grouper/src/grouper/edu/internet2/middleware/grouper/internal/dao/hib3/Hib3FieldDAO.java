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
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.GrouperRuntimeException;
import edu.internet2.middleware.grouper.SchemaException;
import edu.internet2.middleware.grouper.hibernate.ByHqlStatic;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.FieldDAO;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;

/**
 * Basic Hibernate <code>Field</code> DAO interface.
 * @author  blair christensen.
 * @version $Id: Hib3FieldDAO.java,v 1.6 2008-07-09 05:28:18 mchyzer Exp $
 * @since   @HEAD@
 */
public class Hib3FieldDAO extends Hib3DAO implements FieldDAO {

  // PRIVATE CLASS CONSTANTS //
  private static final String KLASS = Hib3FieldDAO.class.getName();

  /** logger */
  private static final Log LOG = LogFactory.getLog(Hib3FieldDAO.class);


  /**
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
   * @since   @HEAD@
   */
  public Set<Field> findAll() 
    throws  GrouperRuntimeException
  {
    return HibernateSession.byHqlStatic()
      .createQuery("from Field order by name asc")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAll").listSet(Field.class);
  } // public Set findAll()

  /** 
   * @since   @HEAD@
   */
  public Set<Field> findAllFieldsByGroupType(String uuid)
    throws  GrouperDAOException
  {
    return HibernateSession.byHqlStatic()
      .createQuery("from Field as f where f.groupTypeUuid = :uuid order by f.name asc")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllFieldsByGroupType")
      .setString("uuid", uuid).listSet(Field.class);
  } 

  /**
   * @since   @HEAD@
   */
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
   * @since   @HEAD@
   */
  public boolean isInUse(Field f) 
    throws  GrouperDAOException,
            SchemaException
  {
    ByHqlStatic qry = HibernateSession.byHqlStatic();
    if      ( f.getType().equals(FieldType.ATTRIBUTE) ) {
      qry.createQuery("from Attribute as a where a.attrName = :name");
    }
    else if ( f.getType().equals(FieldType.LIST) )      {
      qry.createQuery("from Membership as ms where ms.listName = :name");
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

} 

