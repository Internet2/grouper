/*--
$Id: NonGrantable.java,v 1.1 2005-11-01 00:07:33 acohen Exp $
$Date: 2005-11-01 00:07:33 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

/**
 * This interface encapsulates some attributes that are common to
 * all Signet entities except for {@link Assignment} and {@link Proxy}.
 */
public interface NonGrantable
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
