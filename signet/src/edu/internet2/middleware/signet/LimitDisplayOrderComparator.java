/*--
$Id: LimitDisplayOrderComparator.java,v 1.1 2005-07-12 23:13:26 acohen Exp $
$Date: 2005-07-12 23:13:26 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet;

import java.util.Comparator;

/**
 * @author Andy Cohen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class LimitDisplayOrderComparator implements Comparator
{

  /* (non-Javadoc)
   * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
   */
  public int compare(Object o1, Object o2)
  {
    return
      ((Limit)o1).getDisplayOrder() - ((Limit)o2).getDisplayOrder();
  }
}
