/*--
$Id: PermissionTest.java,v 1.3 2005-02-25 19:37:03 acohen Exp $
$Date: 2005-02-25 19:37:03 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet.test;

import javax.naming.OperationNotSupportedException;

import edu.internet2.middleware.signet.Function;
import edu.internet2.middleware.signet.Limit;
import edu.internet2.middleware.signet.ObjectNotFoundException;
import edu.internet2.middleware.signet.Permission;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.choice.ChoiceSet;

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

      // Permission 0 contains limit 0, Permission 1 contains Limit 1,
      // and so forth.
      Limit[] limits = permission.getLimitsArray();
      assertEquals(1, limits.length);
      assertEquals
      	(limits[0],
      	 signet
      	 	.getSubsystem(Constants.SUBSYSTEM_ID)
      	 		.getLimit(fixtures.makeLimitId(permissionIndex)));
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
      Function[] functions = permission.getFunctionsArray();
      assertEquals(1, functions.length);
      assertEquals
      	(functions[0],
      	 signet
      	 	.getSubsystem(Constants.SUBSYSTEM_ID)
      	 		.getFunction(fixtures.makeFunctionId(permissionIndex)));
    }
  }
}
