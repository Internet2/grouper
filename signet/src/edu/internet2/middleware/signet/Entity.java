/*--
$Id: Entity.java,v 1.2 2004-12-24 04:15:46 acohen Exp $
$Date: 2004-12-24 04:15:46 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

import java.util.Date;

/**
* This interface specifies some common methods that every 
* Signet entity must provide.
* 
*/
interface Entity
{
  /**
   * Gets the ID of this entity.
   * 
   * @return Returns a short mnemonic id which will appear in XML
   * 		documents and other documents used by analysts.
   */
  public String getId();

  /**
   * Sets the Status of this entity.
   * 
   * @param status The {@link Status} of this Entity.
   */
  public void setStatus(Status status);

  /**
   * Gets the Status of this entity.
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
