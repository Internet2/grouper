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
/**
 * 
 */
package edu.internet2.middleware.grouper.externalSubjects;

import java.util.Set;

import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;


/**
 * @author mchyzer
 *
 */
public class ExternalSubjectDbStorage implements ExternalSubjectStorable {

  /** 
   * @see edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectStorable#delete(edu.internet2.middleware.grouper.externalSubjects.ExternalSubject)
   */
  public void delete(ExternalSubject externalSubject) {
    GrouperDAOFactory.getFactory().getExternalSubject().delete(externalSubject);
  }

  /**
   * @see edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectStorable#findAll()
   */
  public Set<ExternalSubject> findAll() {
    return GrouperDAOFactory.getFactory().getExternalSubject().findAll();
  }

  /**
   * @see edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectStorable#findAllDisabledMismatch()
   */
  public Set<ExternalSubject> findAllDisabledMismatch() {
    return GrouperDAOFactory.getFactory().getExternalSubject().findAllDisabledMismatch();
  }

  /**
   * @see edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectStorable#findByIdentifier(java.lang.String, boolean, edu.internet2.middleware.grouper.internal.dao.QueryOptions)
   */
  public ExternalSubject findByIdentifier(String identifier, boolean exceptionIfNotFound,
      QueryOptions queryOptions) {
    return GrouperDAOFactory.getFactory().getExternalSubject().findByIdentifier(identifier, exceptionIfNotFound, queryOptions);

  }

  /**
   * @see edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectStorable#saveOrUpdate(edu.internet2.middleware.grouper.externalSubjects.ExternalSubject)
   */
  public void saveOrUpdate(ExternalSubject externalSubject) {
    GrouperDAOFactory.getFactory().getExternalSubject().saveOrUpdate( externalSubject );
  }

}
