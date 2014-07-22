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

package edu.internet2.middleware.grouper.internal.dao.hib3;

import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.externalSubjects.ExternalSubject;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectAttribute;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectAttributeStorageController;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.ByHql;
import edu.internet2.middleware.grouper.hibernate.ByHqlStatic;
import edu.internet2.middleware.grouper.hibernate.ByObject;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.ExternalSubjectDAO;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * Basic Hibernate <code>Group</code> DAO interface.
 */
public class Hib3ExternalSubjectDAO extends Hib3DAO implements ExternalSubjectDAO {

  /**
   * @see ExternalSubjectDAO#findAllDisabledMismatch()
   */
  public Set<ExternalSubject> findAllDisabledMismatch() {
    long now = System.currentTimeMillis();

    StringBuilder sql = new StringBuilder(
        "select es from ExternalSubject as es where  "
          + " (es.enabledDb = 'F' and es.disabledTimeDb is null) "
          + " or (es.enabledDb = 'F' and es.disabledTimeDb > :now) "
          + " or (es.enabledDb = 'T' and es.disabledTimeDb < :now) "
          + " or (es.enabledDb <> 'T' and es.enabledDb <> 'F') "
          + " or (es.enabledDb is null) "
     );

    Set<ExternalSubject> externalSubjects = HibernateSession.byHqlStatic()
      .createQuery(sql.toString())
      .setCacheable(false)
      .setLong( "now",  now )
      .listSet(ExternalSubject.class);
    return externalSubjects;
  }

  /**
   * 
   * @param hibernateSession
   */
  public static void reset(HibernateSession hibernateSession) {
    hibernateSession.byHql().createQuery("delete from ExternalSubject as theExternalSubject")
      .executeUpdate();
  }

  /**
   * @see ExternalSubjectDAO#delete(ExternalSubject)
   */
  public void delete(final ExternalSubject externalSubject) {

    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
        new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {

            HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
            hibernateSession.setCachingEnabled(false);

            ByObject byObject = hibernateSession.byObject();
            
            // delete attributes
            ByHql byHql = hibernateSession.byHql();
            byHql.createQuery("select theExternalSubjectAttribute " +
            		"from ExternalSubjectAttribute as theExternalSubjectAttribute where subject_uuid = :theSubjectUuid");
            byHql.setString("theSubjectUuid", externalSubject.getUuid() );
            List<ExternalSubjectAttribute> externalSubjectAttributes = byHql.list(ExternalSubjectAttribute.class);
            
            for (ExternalSubjectAttribute externalSubjectAttribute : GrouperUtil.nonNull(externalSubjectAttributes)) {
              ExternalSubjectAttributeStorageController.delete(externalSubjectAttribute);
            }
            
            // delete external subject
            byObject.delete(externalSubject);
            return null;
          }
    });
    
  }

  /**
   * @see ExternalSubjectDAO#saveOrUpdate(ExternalSubject)
   * save or update this to the DB.
   */
  public void saveOrUpdate(ExternalSubject externalSubject) {
    
    //TODO update the description and lower search string
    
    HibernateSession.byObjectStatic().saveOrUpdate(externalSubject);
    
  }

  /** */
  private static final String KLASS = Hib3ExternalSubjectDAO.class.getName();


  /**
   * @see ExternalSubjectDAO#findByIdentifier(String, boolean, QueryOptions)
   */
  public ExternalSubject findByIdentifier(String identifier, boolean exceptionIfNotFound, QueryOptions queryOptions) {
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic()
      .createQuery("select theExternalSubject from ExternalSubject as theExternalSubject where theExternalSubject.identifier = :theIdentifier")
      .setCacheable(true).setCacheRegion(KLASS + ".findByIdentifier").options(queryOptions);

    ExternalSubject externalSubject = byHqlStatic.setString("theIdentifier", identifier).uniqueResult(ExternalSubject.class);

    //handle exceptions out of data access method...
    if (externalSubject == null && exceptionIfNotFound) {
      throw new RuntimeException("Cannot find externalSubject with identifier: '" + identifier + "'");
    }
    return externalSubject;

  }

  /**
   * @see ExternalSubjectDAO#findAll()
   */
  public Set<ExternalSubject> findAll() {
    StringBuilder sql = new StringBuilder(
        "select es from ExternalSubject as es "
     );

    Set<ExternalSubject> externalSubjects = HibernateSession.byHqlStatic()
      .createQuery(sql.toString())
      .setCacheable(false)
      .listSet(ExternalSubject.class);
    return externalSubjects;
  }
  
  

} 

