/*--
$Id: FunctionTest.java,v 1.3 2005-03-03 18:29:00 acohen Exp $
$Date: 2005-03-03 18:29:00 $

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
public class FunctionTest extends TestCase
{
  private Signet		signet;
  private Fixtures	fixtures;
  
  public static void main(String[] args)
  {
    junit.textui.TestRunner.run(FunctionTest.class);
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
  public FunctionTest(String name)
  {
    super(name);
  }

  public final void testGetLimitsArray()
  throws
  	ObjectNotFoundException
  {
    for (int functionIndex = 0;
		 		 functionIndex < Constants.MAX_FUNCTIONS;
		 		 functionIndex++)
    {
      Function function
      	= signet
      			.getSubsystem(Constants.SUBSYSTEM_ID)
      				.getFunction
      					(fixtures.makeFunctionId(functionIndex));

      // Function 0 contains limit 0, Function 1 contains Limits 0 and 1,
      // and so forth.
      Limit[] limits = function.getLimitsArray();
      int limitCount = functionIndex + 1;
      assertEquals(limitCount, limits.length);
      
      for (int limitIndex = 0; limitIndex < limitCount; limitIndex++)
      {
        assertEquals
        	(signet
        	   .getSubsystem(Constants.SUBSYSTEM_ID)
        	    .getFunction(fixtures.makeFunctionId(functionIndex))
      	 		  	.getLimitsArray()[limitIndex],
      	   limits[limitIndex]);
      }
    }
  }

  public final void testGetPermissionsArray()
  throws
  	ObjectNotFoundException
  {
    for (int functionIndex = 0;
		 		 functionIndex < Constants.MAX_FUNCTIONS;
		 		 functionIndex++)
    {
      Function function
      	= signet
      			.getSubsystem(Constants.SUBSYSTEM_ID)
      				.getFunction
      					(fixtures.makeFunctionId(functionIndex));

      // Function 0 contains Permission 0, Function 1 Permission Limit 1,
      // and so forth.
      Permission[] permissions = function.getPermissionsArray();
      assertEquals(permissions.length, 1);
      assertEquals
      	(signet
      	 	.getSubsystem(Constants.SUBSYSTEM_ID)
      	 		.getFunction(fixtures.makeFunctionId(functionIndex))
      	 		  .getPermissionsArray()[0],
      	 permissions[0]);
    }
  }
}
