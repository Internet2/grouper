/*--
$Id: HistoryDateComparatorDescending.java,v 1.3 2006-05-08 18:33:29 ddonn Exp $
$Date: 2006-05-08 18:33:29 $

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

import java.sql.Timestamp;
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
    if (o1 instanceof History && o2 instanceof History)
    {
      Date date1 = homogenizeDate(((History)o1).getDate());
      Date date2 = homogenizeDate(((History)o2).getDate());
      
      // We'll do this comparison "backwards", to get a descending sort.
      return date2.compareTo(date1);
    }
    
    return 0;
  }


  /** Some of the Dates get converted to Timestamp objects when stored in the DB.
   * This causes compare() to choke when trying to compare a Date to a Timestamp.
   */
  private java.util.Date homogenizeDate(Date inDate)
  {
	  if (inDate instanceof Timestamp)
	  {
		  Date outDate = new Date(inDate.getTime());
		  return (outDate);
	  }
	  else
		  return (inDate);
  }


}
