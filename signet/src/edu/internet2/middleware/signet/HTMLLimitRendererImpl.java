/*
 * Created on Jan 28, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
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
