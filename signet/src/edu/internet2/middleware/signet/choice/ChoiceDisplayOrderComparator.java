/*--
$Id: ChoiceDisplayOrderComparator.java,v 1.1 2005-07-08 21:54:57 acohen Exp $
$Date: 2005-07-08 21:54:57 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet.choice;

import java.util.Comparator;

import edu.internet2.middleware.signet.choice.Choice;

/**
 * @author Andy Cohen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ChoiceDisplayOrderComparator implements Comparator
{

  /* (non-Javadoc)
   * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
   */
  public int compare(Object arg0, Object arg1)
  {
    Choice choice0 = (Choice)arg0;
    Choice choice1 = (Choice)arg1;

    return (choice0.getDisplayOrder() - choice1.getDisplayOrder());
  }

}
