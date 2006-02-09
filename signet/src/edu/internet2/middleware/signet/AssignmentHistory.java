/*--
$Id: AssignmentHistory.java,v 1.8 2006-02-09 10:17:52 lmcrae Exp $
$Date: 2006-02-09 10:17:52 $

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

import edu.internet2.middleware.signet.tree.TreeNode;

/**
 * @author Andy Cohen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface AssignmentHistory extends History
{
  /**
   * Returns the scope of the (@link Assignment} described by this historical
   * record, at the time this record was created.
   * 
   * @return the scope of the (@link Assignment} described by this historical
   * record, at the time this record was created.
   */
  public TreeNode getScope();
  
  
  /**
   * Returns the <code>Function</code> associated with the {@link Assignment}
   * described by this historical record, at the time this record was created.
   * 
   * @return the <code>Function</code> associated with the {@link Assignment}
   * described by this historical record, at the time this record was created.
   */
  public Function getFunction();
  
  
  /**
   * Returns the grantability of the {@link Assignment} described by this
   * historical record, at the time this record was created.
   * 
   * @return the grantability of the {@link Assignment} described by this
   * historical record, at the time this record was created.
   */
  public boolean canGrant();

  
  /**
   * Returns the useability of the {@link Assignment} described by this
   * historical record, at the time this record was created.
   * 
   * @return the useability of the {@link Assignment} described by this
   * historical record, at the time this record was created.
   */
  public boolean canUse();
  
  
  /**
   * Returns the {@link LimitValue}s of the {@link Assignment} described by this
   * historical record, at the time this record was created.
   * 
   * @return the {@link LimitValue}s of the {@link Assignment} described by this
   * historical record, at the time this record was created.
   */
  public Set getLimitValues();
}
