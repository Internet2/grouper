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
package edu.internet2.middleware.grouper.externalSubjects;

import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.internal.dao.ExternalSubjectDAO;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * this finds the storable implementation and calls methods
 * @author mchyzer
 *
 */
public class ExternalSubjectStorageController {

  /**
   * store the implementation, lazy load
   */
  private static ExternalSubjectStorable externalSubjectStorable = null;
  
  /**
   * 
   * @return the external subject storable
   */
  private static ExternalSubjectStorable externalSubjectStorable() {
    if (externalSubjectStorable == null) {

      //externalSubjects.storage.ExternalSubjectStorable.class = edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectDbStorage
      String externalSubjectStorableClassName = StringUtils.defaultIfEmpty(
          GrouperConfig.getProperty("externalSubjects.storage.ExternalSubjectStorable.class"), ExternalSubjectDbStorage.class.getName());
      Class<ExternalSubjectStorable> externalSubjectStorableClass = GrouperUtil.forName(externalSubjectStorableClassName);
      externalSubjectStorable = GrouperUtil.newInstance(externalSubjectStorableClass);
      
    }
    return externalSubjectStorable;
  }
  
  /**
   * 
   * @param externalSubject 
   * @see ExternalSubjectDAO#delete(ExternalSubject)
   * 
   */
  public static void delete(ExternalSubject externalSubject) {
    
    externalSubjectStorable().delete(externalSubject);
    
  }

  /**
   * @see ExternalSubjectDAO#findAll()
   * @return subjects
   */
  public static Set<ExternalSubject> findAll() {
    return externalSubjectStorable().findAll();
  }

  /**
   * @see ExternalSubjectDAO#findAllDisabledMismatch()
   * @return external subjects with mismatches
   */
  public static Set<ExternalSubject> findAllDisabledMismatch() {
    return externalSubjectStorable().findAllDisabledMismatch();
  }

  /**
   * @see ExternalSubjectDAO#findByIdentifier(String, boolean, QueryOptions)
   * @param identifier
   * @param exceptionIfNotFound
   * @param queryOptions
   * @return
   */
  public static ExternalSubject findByIdentifier(String identifier, boolean exceptionIfNotFound,
      QueryOptions queryOptions) {
    return externalSubjectStorable().findByIdentifier(identifier, exceptionIfNotFound, queryOptions);
  }

  /**
   * @see ExternalSubjectDAO#saveOrUpdate(ExternalSubject)
   * @param externalSubject
   */
  public static void saveOrUpdate(ExternalSubject externalSubject) {
    externalSubjectStorable().saveOrUpdate(externalSubject);
    
  }

}
