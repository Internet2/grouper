/*
 * Created on Jan 13, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
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

      // Function 0 contains limit 0, Function 1 contains Limit 1,
      // and so forth.
      Limit[] limits = function.getLimitsArray();
      assertEquals(1, limits.length);
      assertEquals
      	(signet
      	 	.getSubsystem(Constants.SUBSYSTEM_ID)
      	 		.getFunction(fixtures.makeFunctionId(functionIndex))
      	 		  .getLimitsArray()[0],
      	 limits[0]);
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
