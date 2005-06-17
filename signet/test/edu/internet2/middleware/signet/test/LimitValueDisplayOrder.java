/*--
$Id: LimitValueDisplayOrder.java,v 1.1 2005-06-17 23:24:28 acohen Exp $
$Date: 2005-06-17 23:24:28 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet.test;

import java.util.Comparator;

import edu.internet2.middleware.signet.LimitValue;
import edu.internet2.middleware.signet.ObjectNotFoundException;
import edu.internet2.middleware.signet.SignetRuntimeException;
import edu.internet2.middleware.signet.choice.Choice;
import edu.internet2.middleware.signet.choice.ChoiceNotFoundException;

/**
 * @author Andy Cohen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
class LimitValueDisplayOrder implements Comparator
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
      catch (ObjectNotFoundException onfe)
      {
        throw new SignetRuntimeException(onfe);
      }
      
      Comparator choiceComparator = new ChoiceDisplayOrderComparator();
      comparison = choiceComparator.compare(choice0, choice1);
    }

    return comparison;
  }
}
