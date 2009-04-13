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
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.ByHqlStatic;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.FieldDAO;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Basic Hibernate <code>Field</code> DAO interface.
 * @author  blair christensen.
 * @version $Id: Hib3FieldDAO.java,v 1.14 2009-04-13 20:24:29 mchyzer Exp $
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
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAll").listSet(Field.class);
  } // public Set findAll()

  /** 
   * @param uuid 
   * @return set of fields
   * @throws GrouperDAOException 
   * @since   @HEAD@
   * @deprecated use the FieldFinder method instead
   */
  @Deprecated
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
    if      ( f.getType().equals(FieldType.ATTRIBUTE) ) {
      qry.createQuery("select a from Attribute as a, Field as field where field.name = :name and field.uuid = a.fieldId");
    }
    else if ( f.getType().equals(FieldType.LIST) )      {
      qry.createQuery("select ms from Membership as ms, Field as field where field.name = :name and field.uuid = ms.fieldId");
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
    
    //do this in its own tx so we can be sure it is done and move on to refreshing cache
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_NEW, 
        AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
        HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
        hibernateSession.byObject().saveOrUpdate(field);    
        return null;
      }

    });
    
    FieldFinder.clearCache();
  }

} 

