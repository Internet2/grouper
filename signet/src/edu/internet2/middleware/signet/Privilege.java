/*--
$Id: Privilege.java,v 1.4 2006-02-09 10:23:08 lmcrae Exp $
$Date: 2006-02-09 10:23:08 $
 
Copyright 2006 Internet2, Stanford University

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package edu.internet2.middleware.signet;

import java.util.Set;

/**
 * This interface represents a single {@link Permission} held by a single
 * {@link PrivilegedSubject}, along with its {@link Limit}s.
 */

public interface Privilege
{
  /**
   * Get the Permission that lies at the core of this Privilege.
   * @return the Permission that lies at the core of this Privilege.
   */
  Permission getPermission();
  
  /**
   * Get the LimitValues associated with this Privilege. Limits affect the
   * extent to which this Privilege can be exercised, e.g. a dollar amount
   * and/or a set of classrooms.
   * 
   * Note that Conditions are never included in a Privilege.
   * 
   * @return the LimitValues associated with this Privilege.
   */
  Set getLimitValues();
  
  /**
   * Gets the scope (usually an organization) of this Privilege.
   * 
   * @return the scope (usually an organization) of this Privilege.
   */
  public edu.internet2.middleware.signet.tree.TreeNode getScope();
}
