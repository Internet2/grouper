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
/**
 * 
 */
package edu.internet2.middleware.grouper.externalSubjects;

import java.util.Set;

import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;


/**
 * Grouper built in storage for external subject attributes
 * @author mchyzer
 *
 */
public class ExternalSubjectAttributeDbStorage implements
    ExternalSubjectAttributeStorable {

  /**
   * @see edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectAttributeStorable#delete(edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectAttribute)
   */
  public void delete(ExternalSubjectAttribute externalSubjectAttribute) {
    GrouperDAOFactory.getFactory().getExternalSubjectAttribute().delete(externalSubjectAttribute);
  }

  /**
   * @see edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectAttributeStorable#findBySubject(java.lang.String, edu.internet2.middleware.grouper.internal.dao.QueryOptions)
   */
  public Set<ExternalSubjectAttribute> findBySubject(String subjectUuid,
      QueryOptions queryOptions) {
    return GrouperDAOFactory.getFactory().getExternalSubjectAttribute().findBySubject(subjectUuid, queryOptions);
  }

  /**
   * @see edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectAttributeStorable#findByUuid(java.lang.String, boolean, edu.internet2.middleware.grouper.internal.dao.QueryOptions)
   */
  public ExternalSubjectAttribute findByUuid(String uuid, boolean exceptionIfNotFound,
      QueryOptions queryOptions) {
    return GrouperDAOFactory.getFactory().getExternalSubjectAttribute().findByUuid(uuid, exceptionIfNotFound, queryOptions);
  }

  /**
   * @see edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectAttributeStorable#saveOrUpdate(edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectAttribute)
   */
  public void saveOrUpdate(ExternalSubjectAttribute externalSubjectAttribute) {
    GrouperDAOFactory.getFactory().getExternalSubjectAttribute().saveOrUpdate(externalSubjectAttribute);
  }

}
