/*******************************************************************************
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
 ******************************************************************************/
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

import java.util.Set;

import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectAttribute;
import edu.internet2.middleware.grouper.hibernate.ByHqlStatic;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.ExternalSubjectAttributeDAO;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;


/**
 * Basic Hibernate <code>Group</code> DAO interface.
 */
public class Hib3ExternalSubjectAttributeDAO extends Hib3DAO implements ExternalSubjectAttributeDAO {


  /**
   * @see ExternalSubjectAttributeDAO#delete(ExternalSubjectAttribute)
   */
  public void delete(final ExternalSubjectAttribute externalSubjectAttribute) {

    HibernateSession.byObjectStatic().delete(externalSubjectAttribute);
    
  }

  /**
   * 
   * @param hibernateSession
   */
  public static void reset(HibernateSession hibernateSession) {
    hibernateSession.byHql().createQuery("delete from ExternalSubjectAttribute as theExternalSubjectAttribute")
      .executeUpdate();
  }

  /**
   * @see ExternalSubjectAttributeDAO#saveOrUpdate(ExternalSubjectAttribute)
   * save or update this to the DB.
   */
  public void saveOrUpdate(ExternalSubjectAttribute externalSubjectAttribute) {
    
    HibernateSession.byObjectStatic().saveOrUpdate(externalSubjectAttribute);
    
  }

  /** */
  private static final String KLASS = Hib3ExternalSubjectAttributeDAO.class.getName();


  /**
   * @see ExternalSubjectAttributeDAO#findByUuid(String, boolean, QueryOptions)
   */
  public ExternalSubjectAttribute findByUuid(String uuid,
      boolean exceptionIfNotFound, QueryOptions queryOptions) {
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic()
      .createQuery("select theExternalSubjectAttribute from ExternalSubjectAttribute as theExternalSubjectAttribute where theExternalSubjectAttribute.uuid = :theUuid")
      .setCacheable(true).setCacheRegion(KLASS + ".findByUuid").options(queryOptions);
  
    ExternalSubjectAttribute externalSubjectAttribute = byHqlStatic.setString("theUuid", uuid).uniqueResult(ExternalSubjectAttribute.class);
  
    //handle exceptions out of data access method...
    if (externalSubjectAttribute == null && exceptionIfNotFound) {
      throw new RuntimeException("Cannot find externalSubjectAttribute with uuid: '" + uuid + "'");
    }
    return externalSubjectAttribute;
  }
  
  /**
   * @see ExternalSubjectAttributeDAO#findBySubject(String, QueryOptions)
   */
  public Set<ExternalSubjectAttribute> findBySubject(String subjectUuid,
      QueryOptions queryOptions) {
    
    if (queryOptions == null) {
      queryOptions = new QueryOptions();
    }
    if (queryOptions.getQuerySort() == null) {
      queryOptions.sortAsc(ExternalSubjectAttribute.FIELD_ATTRIBUTE_SYSTEM_NAME);
    }
    
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic()
      .createQuery("select theExternalSubjectAttribute from ExternalSubjectAttribute as theExternalSubjectAttribute where theExternalSubjectAttribute.subjectUuid = :theSubjectUuid")
      .setCacheable(true).setCacheRegion(KLASS + ".findBySubject").options(queryOptions);
  
    Set<ExternalSubjectAttribute> externalSubjectAttributes = byHqlStatic.setString("theSubjectUuid", subjectUuid).listSet(ExternalSubjectAttribute.class);
  
    return externalSubjectAttributes;
  }


} 

