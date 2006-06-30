/*--
$Id: FunctionTest.java,v 1.7 2006-06-30 02:04:41 ddonn Exp $
$Date: 2006-06-30 02:04:41 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet.test;

import java.util.Iterator;
import java.util.Set;
import junit.framework.TestCase;
import edu.internet2.middleware.signet.Function;
import edu.internet2.middleware.signet.Limit;
import edu.internet2.middleware.signet.ObjectNotFoundException;
import edu.internet2.middleware.signet.Permission;
import edu.internet2.middleware.signet.Signet;

/**
 * @author acohen
 *
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
    signet.getPersistentDB().beginTransaction();
    fixtures = new Fixtures(signet);
    signet.getPersistentDB().commit();
    signet.getPersistentDB().close();
    
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
    signet.getPersistentDB().close();
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
        = Common.getFunction
            (signet.getPersistentDB().getSubsystem(Constants.SUBSYSTEM_ID),
             fixtures.makeFunctionId(functionIndex));

      // Function 0 contains limit 0, Function 1 contains Limits 0 and 1,
      // and so forth.
      Limit[] sortedLimits
        = Common.getLimitsInDisplayOrder(function.getLimits());
      int limitCount = functionIndex + 1;
      assertEquals(limitCount, sortedLimits.length);
      
      for (int limitIndex = 0; limitIndex < limitCount; limitIndex++)
      {
        assertEquals
        	(Common.getLimitsInDisplayOrder
              (Common.getFunction
                (signet.getPersistentDB().getSubsystem(Constants.SUBSYSTEM_ID), 
        	     fixtures.makeFunctionId(functionIndex))
      	 		  .getLimits())
                    [limitIndex],
      	     sortedLimits[limitIndex]);
      }
    }
  }

  public final void testGetPermissions()
  throws
  	ObjectNotFoundException
  {
    for (int functionIndex = 0;
		 		 functionIndex < Constants.MAX_FUNCTIONS;
		 		 functionIndex++)
    {
      Function function
      	= Common.getFunction
            (signet.getPersistentDB().getSubsystem(Constants.SUBSYSTEM_ID),
             fixtures.makeFunctionId(functionIndex));

      // Function 0 contains Permission 0, Function 1 Permission Limit 1,
      // and so forth.
      Set permissions = function.getPermissions();
      assertEquals(permissions.size(), 1);
      Permission permission = null;
      Iterator permissionsIterator = permissions.iterator();
      while (permissionsIterator.hasNext())
      {
        permission = (Permission)(permissionsIterator.next());
      }
      
      assertEquals
        (fixtures.makePermissionId(functionIndex),
         permission.getId());
    }
  }
}
