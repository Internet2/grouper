/*
 * @author mchyzer
 * $Id: GrouperPrivilege.java,v 1.1 2008-10-23 04:48:57 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.privs;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.subject.Subject;

/**
 * combines AccessPrivilege and NamingPrivilege
 */
public interface GrouperPrivilege {

  /** get the object this privilege is assigned to (e.g. group or stem object) 
   * @return the group or stem
   */
  public GrouperAPI getGrouperApi();
  
  /**
   * Get name of implementation class for this privilege type. (e.g. from grouper.properties)
   * @return the name
   */
  public String getImplementationName();
  
  /**
   * get type of privilege (e.g. access or naming)
   * @return the type
   */
  public String getType();
  
  /**
   * Get name of privilege.
   * @return  Name of privilege.
   */
  public String getName();

  /**
   * Get subject which was granted privilege on this object.
   * @return  {@link Subject} that was granted privilege.
   */
  public Subject getOwner();

  /**
   * Get subject which has this privilege.
   * @return  {@link Subject} that has this privilege.
   */
  public Subject getSubject();

  /**
   * Returns true if privilege can be revoked.
   * @return  Boolean true if privilege can be revoked.
   */
  public boolean isRevokable();
  
  /**
   * if we are caching subject objects, then set it here...  do not change the subject here
   * @param subject
   */
  public void internalSetSubject(Subject subject);
}
