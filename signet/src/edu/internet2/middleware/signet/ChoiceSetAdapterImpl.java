/*
 * Created on Jan 14, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.internet2.middleware.signet;

import edu.internet2.middleware.signet.choice.ChoiceSet;
import edu.internet2.middleware.signet.choice.ChoiceSetAdapter;
import edu.internet2.middleware.signet.choice.ChoiceSetNotFoundException;

/**
 * @author acohen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

class ChoiceSetAdapterImpl implements ChoiceSetAdapter
{
  Signet signet;
  
  /**
   * Every implementation of ChoiceSetAdapter is required to have
   * a public parameterless constructor.
   */
  public ChoiceSetAdapterImpl()
  {
    super();
    // TODO Auto-generated constructor stub
  }
  
  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.choice.ChoiceSetAdapter#getChoiceSet(java.lang.String)
   */
  public ChoiceSet getChoiceSet(String id)
    throws ChoiceSetNotFoundException
  {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see edu.internet2.middleware.signet.choice.ChoiceSetAdapter#init()
   */
  public void init() throws AdapterUnavailableException
  {
    // TODO Auto-generated method stub
  }

  /**
   * @param signet
   */
  public void setSignet(Signet signet)
  {
    this.signet = signet;
  }

}
