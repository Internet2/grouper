/*--
$Id: NonGrantable.java,v 1.3 2006-02-09 10:22:26 lmcrae Exp $
$Date: 2006-02-09 10:22:26 $

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

/**
 * This interface encapsulates some attributes that are common to
 * all Signet entities except for {@link Assignment} and {@link Proxy}.
 */
interface NonGrantable
extends Entity
{

  /**
   * Sets the {@link Status} of this entity to <code>INACTIVE</code>, and
   * deactivates all dependent objects, including {@link Assignment}s and
   * {@link Proxy}s. For example, calling <code>Function.inactivate()</code>
   * would deactivate the {@link Function} and its <code>Assignment</code>s
   * (because each <code>Assignment</code> is dependent upon a single
   * <code>Function</code>), but not its {@link Permission}s, because each
   * <code>Permission</code> can be associated with multiple
   * <code>Functions</code>. Calling <code>Permission.inactivate()</code> would
   * deactivate that <code>Permission</code>, but would not deactivate any other
   * objects.
   */
  public void inactivate();
}
