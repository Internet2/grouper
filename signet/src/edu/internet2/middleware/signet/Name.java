/*--
  $Id: Name.java,v 1.1 2004-12-09 20:49:07 mnguyen Exp $
  $Date: 2004-12-09 20:49:07 $
  
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

    /**
     * Sets the descriptive name of this entity.
     * 
     * @param name A descriptive name which will appear in UIs and
     * 		documents exposed to users.
     */
    public void setName(String name);
}
