package edu.internet2.middleware.grouper.externalSubjects;

import java.util.Set;

import edu.internet2.middleware.grouper.internal.dao.QueryOptions;

/**
 * implement this to change how external subjects are stored
 * @author mchyzer
 */
public interface ExternalSubjectStorable {

  /**
   * find all external subjects which have a disabled date which are not disabled
   * @return the set of subjects
   */
  public Set<ExternalSubject> findAllDisabledMismatch();

  /**
   * find all external subjects
   * @return the set of subjects
   */
  public Set<ExternalSubject> findAll();

  /**
   * find an external subject by identifier
   * @param identifier
   * @param exceptionIfNotFound
   * @param queryOptions 
   * @return the external subject or null or exception
   */
  ExternalSubject findByIdentifier(String identifier, boolean exceptionIfNotFound, QueryOptions queryOptions);
  
  /**
   * delete an external subject and all its attributes
   * @param externalSubject 
   */
  void delete(ExternalSubject externalSubject);

  /**
   * insert or update an external subject to the DB
   * @param externalSubject
   */
  void saveOrUpdate( ExternalSubject externalSubject );

}
