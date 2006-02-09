/*--
$Id: GrantableReportComparator.java,v 1.2 2006-02-09 10:31:36 lmcrae Exp $
$Date: 2006-02-09 10:31:36 $

Copyright 2006 Internet2, Stanford University

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
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
