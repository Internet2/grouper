/*--
  $Id: HelpText.java,v 1.1 2004-12-09 20:49:07 mnguyen Exp $
  $Date: 2004-12-09 20:49:07 $
  
  Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
  Licensed under the Signet License, Version 1,
  see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

interface HelpText
{
  /**
   * Sets the help-text associated with this entity.
   * 
   * @param helpText
   */
    public void setHelpText(String helpText);
    
    /**
     * Gets the help-text associated with this entity.
     * This method should really be non-public.
     * 
     * @return the help-text associated with this entity.
     */
    public String getHelpText();
}
