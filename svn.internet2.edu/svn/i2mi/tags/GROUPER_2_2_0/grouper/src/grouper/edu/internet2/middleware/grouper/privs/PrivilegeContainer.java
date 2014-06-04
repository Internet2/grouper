/*******************************************************************************
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
 ******************************************************************************/
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
  public String getPrivilegeName();
  
  /**
   * get the membership type this involves.  Note, if there is an immediate
   * and effective, then put immediate so it is obvious that it can be unassigned
   * @return if immediate
   */
  public PrivilegeAssignType getPrivilegeAssignType();

  /**
   * @param privilege1 the privilege to set
   */
  public void setPrivilegeName(String privilege1);

  /**
   * @param privilegeAssignType1 the privilegeAssignType to set
   */
  public void setPrivilegeAssignType(PrivilegeAssignType privilegeAssignType1);
  
}
