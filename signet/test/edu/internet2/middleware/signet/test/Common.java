/*--
$Id: Common.java,v 1.10 2005-09-23 18:22:05 acohen Exp $
$Date: 2005-09-23 18:22:05 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet.test;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import edu.internet2.middleware.signet.Assignment;
import edu.internet2.middleware.signet.Limit;
import edu.internet2.middleware.signet.LimitValue;
import edu.internet2.middleware.signet.Privilege;
import edu.internet2.middleware.signet.PrivilegedSubject;
import edu.internet2.middleware.signet.SignetAuthorityException;

import junit.framework.TestCase;

/**
 * @author Andy Cohen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Common extends TestCase
{
  public static Object getSingleSetMember(Set set)
  {
    assertEquals(1, set.size());

    Object obj = null;
    Iterator setIterator = set.iterator();
    while (setIterator.hasNext())
    {
      obj = setIterator.next();
    }
    
    return obj;
  }
  
  public static Object getFirstSetMember(Set set)
  {
    assertTrue(0 < set.size());

    Object obj = null;
    Iterator setIterator = set.iterator();
    setIterator.hasNext();
    obj = setIterator.next();
    
    return obj;
  }
  
  static LimitValue[] getLimitValuesArray(Assignment assignment)
  {
    LimitValue limitValuesArray[] = new LimitValue[0];

    return
      (LimitValue[])(assignment.getLimitValues().toArray(limitValuesArray));
  }

  static LimitValue[] getLimitValuesInDisplayOrder
    (Assignment assignment)
  {
    LimitValue[] limitValues = getLimitValuesArray(assignment);
    Arrays.sort(limitValues);
    
    return limitValues;
  }

  static LimitValue[] getLimitValuesInDisplayOrder
    (Privilege privilege)
  {
    LimitValue[] limitValues = new LimitValue[0];
    limitValues
      = (LimitValue[])(privilege.getLimitValues().toArray(limitValues));
    Arrays.sort(limitValues);
    return limitValues;
  }
  
  static Limit[] getLimitsInDisplayOrder(Set limits)
  {
    Limit[] limitsArray = new Limit[0];
    limitsArray = (Limit[])(limits.toArray(limitsArray));
    
    if (limitsArray.length > 0)
    {
      Arrays.sort(limitsArray);
    }
    
    return limitsArray;
  }
  
  static Date getDate(int daysOffset)
  {
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.DATE, daysOffset);
    return calendar.getTime();
  }

  /**
   * @param assignment
   * @return
   * @throws SignetAuthorityException
   */
  public static PrivilegedSubject getOriginalGrantor(Assignment assignment)
  throws SignetAuthorityException
  {
    PrivilegedSubject originalGrantor;
    
    if (assignment.getProxy() == null)
    {
      originalGrantor = assignment.getGrantor();
    }
    else
    {
      originalGrantor = assignment.getProxy();
      originalGrantor.setActingAs(assignment.getGrantor());
    }
    
    return originalGrantor;
  }
}
