/*--
$Id: Entity.java,v 1.7 2005-11-01 00:07:33 acohen Exp $
$Date: 2005-11-01 00:07:33 $

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
