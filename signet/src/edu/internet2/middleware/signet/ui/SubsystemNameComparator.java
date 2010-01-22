/*--
$Id: SubsystemNameComparator.java,v 1.2 2006-02-09 10:33:59 lmcrae Exp $
$Date: 2006-02-09 10:33:59 $

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
