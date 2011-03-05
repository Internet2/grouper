/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.privs;



/**
 * bean that holds info about the privilege assignment
 */
public interface PrivilegeContainer {

  /**
   * get the privilege this involves
   * @return privilege
   */
  public Privilege getPrivilege();
  
  /**
   * get the membership type this involves.  Note, if there is an immediate
   * and effective, then put immediate so it is obvious that it can be unassigned
   * @return if immediate
   */
  public PrivilegeAssignType getPrivilegeAssignType();
  
}
