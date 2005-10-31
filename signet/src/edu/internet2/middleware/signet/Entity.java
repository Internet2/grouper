/*--
$Id: Entity.java,v 1.6 2005-10-31 22:45:28 acohen Exp $
$Date: 2005-10-31 22:45:28 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

import java.util.Date;

import edu.internet2.middleware.signet.Status;

/**
* This interface specifies some common methods that every 
* Signet entity must provide.
* 
*/
interface Entity
{  
  /**
   * Saves the current state of this Signet entity, and any Signet entities
   * that it refers to.
   *
   */
  public void save();

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

  /**
   * Gets the <code>Status</code> of this entity. {@link Assignment}s and
   * {@link Proxy}s may have the status <code>ACTIVE</code>,
   * <code>INACTIVE</code>, or <code>PENDING</code>, but all other entities may
   * only have the status <code>ACTIVE</code> or <code>INACTIVE</code>.
   * 
   * @return Returns the status of this entity.
   */
  public Status getStatus();
  
  /**
   * Gets the date and time this entity was first created.
   * 
   * @return Returns the date and time this entity was first created.
   */
  public Date getCreateDatetime();
}
