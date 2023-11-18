/**
 * Copyright 2014 Internet2
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
