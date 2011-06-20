/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.privs;

import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 *
 */
public class PrivilegeSubjectContainerImpl implements PrivilegeSubjectContainer {

  /** subject */
  private Subject subject;
  
  /**
   * privilege containers
   */
  private Map<String, PrivilegeContainer> privilegeContainers;
  
  /**
   * @see edu.internet2.middleware.grouper.privs.PrivilegeSubjectContainer#getPrivilegeContainers()
   */
  public Map<String, PrivilegeContainer> getPrivilegeContainers() {
    return this.privilegeContainers;
  }

  /**
   * @see edu.internet2.middleware.grouper.privs.PrivilegeSubjectContainer#getSubject()
   */
  public Subject getSubject() {
    return this.subject;
  }

  
  /**
   * @param subject1 the subject to set
   */
  public void setSubject(Subject subject1) {
    this.subject = subject1;
  }

  
  /**
   * @param privilegeContainers1 the privilegeContainers to set
   */
  public void setPrivilegeContainers(Map<String, PrivilegeContainer> privilegeContainers1) {
    this.privilegeContainers = privilegeContainers1;
  }

  /**
   * @see Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    if (this.subject == null) {
      result.append("Subject: null");
    } else {
      result.append(GrouperUtil.subjectToString(this.subject));
    }
    result.append(": ");
    if (GrouperUtil.length(this.privilegeContainers) == 0) {
      result.append(" no privs");
    } else {
      
      Set<String> privilegeNameSet = this.privilegeContainers.keySet();
      int index = 0;
      for (String privilege: privilegeNameSet) {
        result.append(this.privilegeContainers.get(privilege));
        if (index < privilegeNameSet.size()-1) {
          result.append(", ");
        }
        index++;
      }
    }
    return result.toString();
  }
  
}
