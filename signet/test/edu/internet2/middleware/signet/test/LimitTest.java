/*--
$Id: LimitTest.java,v 1.6 2006-06-30 02:04:41 ddonn Exp $
$Date: 2006-06-30 02:04:41 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet.test;

import javax.naming.OperationNotSupportedException;

import edu.internet2.middleware.signet.Limit;
import edu.internet2.middleware.signet.ObjectNotFoundException;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.choice.ChoiceSet;

import junit.framework.TestCase;

/**
 * @author acohen
 *
 */
public class LimitTest extends TestCase
{
  private Signet		signet;
  private Fixtures	fixtures;
  
  public static void main(String[] args)
  {
    junit.textui.TestRunner.run(LimitTest.class);
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
  }

  /*
   * @see TestCase#tearDown()
   */
  protected void tearDown() throws Exception
  {
    super.tearDown();
    
    signet.getPersistentDB().commit();
    signet.getPersistentDB().close();
  }

  /**
   * Constructor for LimitTest.
   * @param name
   */
  public LimitTest(String name)
  {
    super(name);
  }

  public final void testGetId()
  throws ObjectNotFoundException
  {
    for (int limitIndex = 0;
		 limitIndex < Constants.MAX_LIMITS;
		 limitIndex++)
    {
      Limit limit
      	= signet.getPersistentDB()
      			.getSubsystem(Constants.SUBSYSTEM_ID)
      				.getLimit
      					(fixtures.makeLimitId(limitIndex));
 
      String id = limit.getId();
      assertEquals(id, fixtures.makeLimitId(limitIndex));
    }
  }

  public final void testGetChoiceSet()
  throws
  	OperationNotSupportedException,
	ObjectNotFoundException
  {
    for (int limitIndex = 0;
		 limitIndex < Constants.MAX_LIMITS;
		 limitIndex++)
    {
      Limit limit
      	= signet.getPersistentDB()
      			.getSubsystem(Constants.SUBSYSTEM_ID)
      				.getLimit
      					(fixtures.makeLimitId(limitIndex));

      // Limit 0 contains ChoiceSet 0, Limit 1 contains ChoiceSet 1,
      // and so forth.
      ChoiceSet choiceSet = limit.getChoiceSet();
      assertNotNull(choiceSet);
      assertEquals
      	(choiceSet,
      	 signet.getPersistentDB().getChoiceSet(fixtures.makeChoiceSetId(limitIndex)));
    }
  }

  public final void testGetName()
  throws ObjectNotFoundException
  {
    for (int limitIndex = 0;
		 limitIndex < Constants.MAX_LIMITS;
		 limitIndex++)
    {
      Limit limit
      	= signet.getPersistentDB()
      			.getSubsystem(Constants.SUBSYSTEM_ID)
      				.getLimit
      					(fixtures.makeLimitId(limitIndex));
 
      String name = limit.getName();
      assertEquals(name, fixtures.makeLimitName(limitIndex));
    }
  }

  public final void testGetHelpText()
  throws ObjectNotFoundException
  {
    for (int limitIndex = 0;
    		 limitIndex < Constants.MAX_LIMITS;
    		 limitIndex++)
    {
    	Limit limit
				= signet.getPersistentDB()
						.getSubsystem(Constants.SUBSYSTEM_ID)
							.getLimit
								(fixtures.makeLimitId(limitIndex));

    	String helpText = limit.getHelpText();
    	assertEquals(helpText, fixtures.makeLimitHelpText(limitIndex));
    }
  }
}
