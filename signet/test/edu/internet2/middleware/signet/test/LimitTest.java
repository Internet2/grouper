/*--
$Id: LimitTest.java,v 1.5 2005-02-25 19:37:03 acohen Exp $
$Date: 2005-02-25 19:37:03 $

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
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
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
    signet.beginTransaction();
    fixtures = new Fixtures(signet);
  }

  /*
   * @see TestCase#tearDown()
   */
  protected void tearDown() throws Exception
  {
    super.tearDown();
    
    signet.commit();
    signet.close();
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
      	= signet
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
      	= signet
      			.getSubsystem(Constants.SUBSYSTEM_ID)
      				.getLimit
      					(fixtures.makeLimitId(limitIndex));

      // Limit 0 contains ChoiceSet 0, Limit 1 contains ChoiceSet 1,
      // and so forth.
      ChoiceSet choiceSet = limit.getChoiceSet();
      assertNotNull(choiceSet);
      assertEquals
      	(choiceSet,
      	 signet.getChoiceSet(fixtures.makeChoiceSetId(limitIndex)));
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
      	= signet
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
				= signet
						.getSubsystem(Constants.SUBSYSTEM_ID)
							.getLimit
								(fixtures.makeLimitId(limitIndex));

    	String helpText = limit.getHelpText();
    	assertEquals(helpText, fixtures.makeLimitHelpText(limitIndex));
    }
  }
}
