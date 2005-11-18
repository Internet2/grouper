/*--
$Id: GrantableReportComparator.java,v 1.1 2005-11-18 23:56:32 acohen Exp $
$Date: 2005-11-18 23:56:32 $

Copyright 2005 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet.ui;

import java.util.Comparator;

import edu.internet2.middleware.signet.Assignment;
import edu.internet2.middleware.signet.PrivilegedSubject;
import edu.internet2.middleware.signet.Proxy;

public class GrantableReportComparator implements Comparator
{
	/**
	 * Compares Subjects by their names.
	 */
	public GrantableReportComparator()
  {
		super();
	}

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object o1, Object o2)
  {
    // Assignments always sort before Proxies.
    if ((o1 instanceof Assignment) && (o2 instanceof Proxy))
    {
      return 1;
    }
    else if ((o1 instanceof Proxy) && (o2 instanceof Assignment))
    {
      return -1;
    }
    else if (o1 instanceof Proxy)
    {
      return ((Proxy)o1).compareTo(o2);
    }
    else
    {
      return ((Assignment)o1).compareTo(o2);
    }
	}

}
