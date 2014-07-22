/**
 * Copyright 2012 Internet2
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
