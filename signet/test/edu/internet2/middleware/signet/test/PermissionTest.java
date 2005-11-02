/*--
$Id: PermissionTest.java,v 1.5 2005-11-02 17:54:17 acohen Exp $
$Date: 2005-11-02 17:54:17 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet.test;

import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.internet2.middleware.signet.Function;
import edu.internet2.middleware.signet.Limit;
import edu.internet2.middleware.signet.ObjectNotFoundException;
import edu.internet2.middleware.signet.Permission;
import edu.internet2.middleware.signet.Signet;

import junit.framework.TestCase;

/**
 * @author acohen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class PermissionTest extends TestCase
{
  private Signet		signet;
  private Fixtures	fixtures;
  
  public static void main(String[] args)
  {
    junit.textui.TestRunner.run(PermissionTest.class);
  }

  /*
   * @see TestCase#setUp()
   */
  protected void setUp() throws Exception
  {
    super.setUp();
    
    signet = new Signet();
    signet.beginTransaction();
    fixtures = new Fixtures(signet);
    signet.commit();
    signet.close();
    
    // Let's use a new Signet session, to make sure we're actually
    // pulling data from the database, and not just referring to in-memory
    // structures.
    signet = new Signet();
  }

  /*
   * @see TestCase#tearDown()
   */
  protected void tearDown() throws Exception
  {
    super.tearDown();
    signet.close();
  }

  /**
   * Constructor for LimitTest.
   * @param name
   */
  public PermissionTest(String name)
  {
    super(name);
  }

  public final void testGetLimitsArray()
  throws
  	ObjectNotFoundException
  {
    for (int permissionIndex = 0;
		 		 permissionIndex < Constants.MAX_PERMISSIONS;
		 		 permissionIndex++)
    {
      Permission permission
      	= signet
      			.getSubsystem(Constants.SUBSYSTEM_ID)
      				.getPermission
      					(fixtures.makePermissionId(permissionIndex));

      // Permission 0 contains limit 0, Permission 1 contains Limits 0 and 1,
      // and so forth.
      Set limits = permission.getLimits();
      assertEquals(permissionIndex + 1, limits.size());
      
      SortedSet sortedLimits = new TreeSet(new LimitDisplayOrderComparator());
      sortedLimits.addAll(limits);
      Iterator sortedLimitsIterator = sortedLimits.iterator();
      int limitNumber = 0;
      while (sortedLimitsIterator.hasNext())
      {
        Limit limit = (Limit)(sortedLimitsIterator.next());
        assertEquals
        	(limit,
        	 signet
      	 	   .getSubsystem(Constants.SUBSYSTEM_ID)
      	 		   .getLimit(fixtures.makeLimitId(limitNumber)));
        limitNumber++;
      }
    }
  }

  public final void testGetFunctionsArray()
  throws
  	ObjectNotFoundException
  {
    for (int permissionIndex = 0;
		 		 permissionIndex < Constants.MAX_PERMISSIONS;
		 		 permissionIndex++)
    {
      Permission permission
      	= signet
      			.getSubsystem(Constants.SUBSYSTEM_ID)
      				.getPermission
      					(fixtures.makePermissionId(permissionIndex));

      // Permission 0 is associated with Function 0, Permission 1 is
      // associated with Function 1, and so forth.
      Set functions = permission.getFunctions();
      assertEquals(1, functions.size());

      assertEquals
      	(Common.getSingleSetMember(functions),
      	 signet
      	 	.getSubsystem(Constants.SUBSYSTEM_ID)
      	 		.getFunction(fixtures.makeFunctionId(permissionIndex)));
    }
  }
}
