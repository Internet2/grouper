/*--
$Id: ChoiceSetAdapterImpl.java,v 1.3 2005-03-07 18:55:43 acohen Exp $
$Date: 2005-03-07 18:55:43 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

import edu.internet2.middleware.signet.choice.ChoiceSet;
import edu.internet2.middleware.signet.choice.ChoiceSetAdapter;
import edu.internet2.middleware.signet.choice.ChoiceSetNotFound;

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
    throws ChoiceSetNotFound
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
