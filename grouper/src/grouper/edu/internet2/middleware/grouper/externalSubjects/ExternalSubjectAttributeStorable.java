package edu.internet2.middleware.grouper.externalSubjects;

import java.util.Set;

import edu.internet2.middleware.grouper.internal.dao.QueryOptions;

/**
 * interface to implement to keep external subjects somewhere besides in the Grouper DB
 * @author mchyzer
 */
public interface ExternalSubjectAttributeStorable {
  /**
   * delete an external subject and all its attributes
   * @param externalSubjectAttribute 
   */
  void delete(ExternalSubjectAttribute externalSubjectAttribute);

  /**
   * insert or update an external subject attribute to the DB
   * @param externalSubjectAttribute
   */
  void saveOrUpdate( ExternalSubjectAttribute externalSubjectAttribute );

  /**
   * find an external subject attribute by identifier
   * @param uuid
   * @param exceptionIfNotFound
   * @param queryOptions 
   * @return the external subject or null or exception
   */
  ExternalSubjectAttribute findByUuid(String uuid, boolean exceptionIfNotFound, QueryOptions queryOptions);

  /**
   * find attributes by subject, order by system name
   * @param subjectUuid
   * @param queryOptions 
   * @return the external subject or null or exception
   */
  Set<ExternalSubjectAttribute> findBySubject(String subjectUuid, QueryOptions queryOptions);

}
