/*--
$Id: HTMLLimitRendererImpl.java,v 1.2 2005-02-25 19:37:03 acohen Exp $
$Date: 2005-02-25 19:37:03 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

import edu.internet2.middleware.signet.choice.ChoiceSet;

/**
 * @author acohen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
class HTMLLimitRendererImpl implements HTMLLimitRenderer
{
  private Signet signet;

  /**
   * 
   */
  public HTMLLimitRendererImpl()
  {
    super();
    // TODO Auto-generated constructor stub
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.HTMLLimitRenderer#render(java.lang.String, edu.internet2.middleware.signet.choice.ChoiceSet)
   */
  public String render(String limitId, ChoiceSet choiceSet)
  {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @param signet
   */
  public void setSignet(Signet signet)
  {
    this.signet = signet;
  }

}
