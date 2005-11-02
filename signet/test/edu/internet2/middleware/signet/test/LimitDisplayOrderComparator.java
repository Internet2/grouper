/*--
$Id: LimitDisplayOrderComparator.java,v 1.1 2005-11-02 17:54:17 acohen Exp $
$Date: 2005-11-02 17:54:17 $

Copyright 2005 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet.test;

import java.util.Comparator;
import edu.internet2.middleware.signet.Limit;

public class LimitDisplayOrderComparator implements Comparator
{
  /**
   * Compares Limits by their display-order values.
   */
  public LimitDisplayOrderComparator()
  {
    super();
  }

  /* (non-Javadoc)
   * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
   */
  public int compare(Object o1, Object o2)
  {
    if (o1 instanceof Limit
        && o2 instanceof Limit)
    {
      int order1 = ((Limit)o1).getDisplayOrder();
      int order2 = ((Limit)o2).getDisplayOrder();
      
      return (order1 - order2);
    }
    return 0;
  }

}
