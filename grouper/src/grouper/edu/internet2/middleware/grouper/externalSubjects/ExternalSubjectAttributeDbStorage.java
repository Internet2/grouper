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
