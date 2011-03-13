/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.privs;

import java.util.Map;

import edu.internet2.middleware.subject.Subject;


/**
 * bean that holds info about the privilege assignment
 */
public interface PrivilegeSubjectContainer {

  /**
   * get the subject this involves
   * @return the subject
   */
  public Subject getSubject();
  
  /**
   * privileges for this subject
   * @return map of privileges, by privilege name
   */
  public Map<String, PrivilegeContainer> getPrivilegeContainers();

  /**
   * set the subject this involves
   * @param theSubject
   */
  public void setSubject(Subject theSubject);
  
  /**
   * privileges for this subject
   * @param thePrivilegeContainers map of privileges, by privilege name
   */
  public void setPrivilegeContainers(Map<String, PrivilegeContainer> thePrivilegeContainers);

}
