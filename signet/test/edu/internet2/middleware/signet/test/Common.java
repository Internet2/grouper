/*--
$Id: Common.java,v 1.3 2005-07-01 01:51:33 acohen Exp $
$Date: 2005-07-01 01:51:33 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet.test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import edu.internet2.middleware.signet.Assignment;
import edu.internet2.middleware.signet.LimitValue;

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
    Arrays.sort(limitValues, new LimitValueDisplayOrder());
    return limitValues;
  }
}
