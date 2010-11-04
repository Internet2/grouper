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
