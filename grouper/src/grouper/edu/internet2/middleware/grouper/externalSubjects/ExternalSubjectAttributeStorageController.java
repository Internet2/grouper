package edu.internet2.middleware.grouper.externalSubjects;

import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.internal.dao.ExternalSubjectAttributeDAO;
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
          GrouperConfig.getProperty("externalSubjects.storage.ExternalSubjectAttributeStorable.class"), ExternalSubjectAttributeDbStorage.class.getName());
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
