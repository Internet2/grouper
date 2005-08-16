/*--
$Id: Entity.java,v 1.5 2005-08-16 16:41:08 acohen Exp $
$Date: 2005-08-16 16:41:08 $

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
   * Sets the Status of this entity to INACTIVE, and deactivates all dependent
   * objects, including Assignments. For example, calling Function.inactivate()
   * would deactivate the Function, its Permissions (because each Permissions
   * is dependent upon a single Function), and Assignments (because each
   * Assignment is dependent upon a single Function). Calling
   * Permission.inactivate() would deactivate that Permission, but would not
   * deactivate any other objects.
   */
  public void inactivate();

  /**
   * Gets the Status of this entity. Assignments may have the status ACTIVE,
   * INACTIVE, or PENDING, but all other entities may only have the status
   * ACTIVE or INACTIVE.
   * 
   * @return Returns the status.
   */
  public Status getStatus();
  
  /**
   * Gets the date and time this entity was first created.
   * 
   * @return Returns the date and time this entity was first created.
   */
  public Date getCreateDatetime();
}
