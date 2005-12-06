/*--
$Id: HistoryDateComparatorDescending.java,v 1.1 2005-12-06 22:34:51 acohen Exp $
$Date: 2005-12-06 22:34:51 $

Copyright 2005 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet.ui;

import java.util.Comparator;
import java.util.Date;

import edu.internet2.middleware.signet.History;

public class HistoryDateComparatorDescending implements Comparator
{
  /**
   * Compares Subsystems by their names.
   */
  public HistoryDateComparatorDescending()
  {
    super();
  }

  /* (non-Javadoc)
   * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
   */
  public int compare(Object o1, Object o2)
  {
    if (o1 instanceof History
      && o2 instanceof History)
    {
      Date date1 = ((History)o1).getDate();
      Date date2 = ((History)o2).getDate();
      
      // We'll do this comparison "backwards", to get a descending sort.
      return date2.compareTo(date1);
    }
    
    return 0;
  }
}
