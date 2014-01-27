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
/*
 * @author mchyzer
 * $Id: GrouperPrivilege.java,v 1.1 2008-10-23 04:48:57 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.privs;

import java.util.Collection;

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
