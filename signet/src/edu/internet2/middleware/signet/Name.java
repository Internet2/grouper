/*--
$Id: Name.java,v 1.3 2005-01-11 20:38:44 acohen Exp $
$Date: 2005-01-11 20:38:44 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

/**
* This interface specifies some common methods that every 
* named Signet entity must provide.
* 
*/
interface Name
{
  /**
   * Gets the descriptive name of this entity.
   * 
   * @return Returns a descriptive name which will appear in UIs and
   * 		documents exposed to users.
   */
  public String getName();
}
