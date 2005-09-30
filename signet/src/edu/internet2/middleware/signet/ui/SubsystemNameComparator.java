/*--
$Id: SubsystemNameComparator.java,v 1.1 2005-09-30 22:38:56 acohen Exp $
$Date: 2005-09-30 22:38:56 $

Copyright 2005 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet.ui;

import java.util.Comparator;
import edu.internet2.middleware.signet.Subsystem;

public class SubsystemNameComparator implements Comparator
{
  /**
   * Compares Subsystems by their names.
   */
  public SubsystemNameComparator()
  {
    super();
  }

  /* (non-Javadoc)
   * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
   */
  public int compare(Object o1, Object o2)
  {
    if (o1 instanceof Subsystem
      && o2 instanceof Subsystem)
    {
      String name1 = ((Subsystem)o1).getName().toLowerCase();
      String name2 = ((Subsystem)o2).getName().toLowerCase();
      return name1.compareTo(name2);
    }
    
    return 0;
  }
}
