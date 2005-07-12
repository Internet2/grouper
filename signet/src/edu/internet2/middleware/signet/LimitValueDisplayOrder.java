/*--
$Id: LimitValueDisplayOrder.java,v 1.4 2005-07-12 23:13:26 acohen Exp $
$Date: 2005-07-12 23:13:26 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

import java.util.Comparator;

import edu.internet2.middleware.signet.LimitValue;
import edu.internet2.middleware.signet.SignetRuntimeException;
import edu.internet2.middleware.signet.choice.Choice;
import edu.internet2.middleware.signet.choice.ChoiceDisplayOrderComparator;
import edu.internet2.middleware.signet.choice.ChoiceNotFoundException;

/**
 * @author Andy Cohen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class LimitValueDisplayOrder implements Comparator
{

  /* (non-Javadoc)
   * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
   */
  public int compare(Object arg0, Object arg1)
  {
    LimitValue limitValue0 = (LimitValue)arg0;
    LimitValue limitValue1 = (LimitValue)arg1;
    
    int comparison
    	= limitValue0.getLimit().getDisplayOrder()
    	  - limitValue1.getLimit().getDisplayOrder();
    
    if (comparison == 0)
    {
      Choice choice0;
      Choice choice1;
      
      try
      {
        choice0
        	= limitValue0
        			.getLimit()
        				.getChoiceSet()
        					.getChoiceByValue
        						(limitValue0.getValue());
        choice1
      		= limitValue1
      			.getLimit()
      				.getChoiceSet()
      					.getChoiceByValue
      						(limitValue1.getValue());
      }
      catch (ChoiceNotFoundException cnfe)
      {
        throw new SignetRuntimeException(cnfe);
      }
      
      Comparator choiceComparator = new ChoiceDisplayOrderComparator();
      comparison = choiceComparator.compare(choice0, choice1);
    }

    return comparison;
  }
}
