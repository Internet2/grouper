/**
 * Copyright 2014 Internet2
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
package edu.internet2.middleware.grouper.externalSubjects;

import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @see ExternalSubjectAttributeStorable
 * @author mchyzer
 *
 */
public class ExternalSubjectAttributeStorageController {

  /**
   * store the implementation, lazy load
   */
  private static ExternalSubjectAttributeStorable externalSubjectAttributeStorable = null;
  
  /**
   * 
   * @return the external subject storable
   */
  private static ExternalSubjectAttributeStorable externalSubjectAttributeStorable() {
    if (externalSubjectAttributeStorable == null) {

      //externalSubjects.storage.ExternalSubjectAttributeStorable.class = edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectAttributeDbStorage
      String externalSubjectAttributeStorableClassName = StringUtils.defaultIfEmpty(
          GrouperConfig.retrieveConfig().propertyValueString("externalSubjects.storage.ExternalSubjectAttributeStorable.class"), ExternalSubjectAttributeDbStorage.class.getName());
      Class<ExternalSubjectAttributeStorable> externalSubjectAttributeStorableClass = GrouperUtil.forName(externalSubjectAttributeStorableClassName);
      externalSubjectAttributeStorable = GrouperUtil.newInstance(externalSubjectAttributeStorableClass);
      
    }
    return externalSubjectAttributeStorable;
  }

  /**
   * @see ExternalSubjectAttributeDAO#delete(ExternalSubjectAttribute)
   * @param externalSubjectAttribute
   */
  public static void delete(ExternalSubjectAttribute externalSubjectAttribute) {
    externalSubjectAttributeStorable().delete(externalSubjectAttribute);
  }

  /**
   * @see ExternalSubjectAttributeDAO#findBySubject(String, QueryOptions)
   * @param subjectUuid
   * @param queryOptions
   * @return attributes
   */
  public static Set<ExternalSubjectAttribute> findBySubject(String subjectUuid,
      QueryOptions queryOptions) {
    return externalSubjectAttributeStorable().findBySubject(subjectUuid, queryOptions);
  }

  /**
   * @see ExternalSubjectAttributeDAO#findByUuid(String, boolean, QueryOptions)
   * @param uuid
   * @param exceptionIfNotFound
   * @param queryOptions
   * @return attribute
   */
  public static ExternalSubjectAttribute findByUuid(String uuid, boolean exceptionIfNotFound,
      QueryOptions queryOptions) {
    return externalSubjectAttributeStorable().findByUuid(uuid, exceptionIfNotFound, queryOptions);
  }

  /**
   * @see ExternalSubjectAttributeDAO#saveOrUpdate(ExternalSubjectAttribute)
   * @param externalSubjectAttribute
   */
  public static void saveOrUpdate(ExternalSubjectAttribute externalSubjectAttribute) {
    externalSubjectAttributeStorable().saveOrUpdate(externalSubjectAttribute);
  }

}
