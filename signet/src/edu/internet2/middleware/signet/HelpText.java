/*--
$Id: HelpText.java,v 1.3 2005-01-11 20:38:44 acohen Exp $
$Date: 2005-01-11 20:38:44 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

interface HelpText
{
/**
 * Sets the help-text associated with this entity.
 * This method should probably be non-public.
 * 
 * @param helpText
 */
  public void setHelpText(String helpText);
  
  /**
   * Gets the help-text associated with this entity.
   * 
   * @return the help-text associated with this entity.
   */
  public String getHelpText();
}
