/*
 * Created on Jan 13, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.internet2.middleware.signet.choice.test;

import edu.internet2.middleware.signet.ObjectNotFoundException;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.choice.Choice;
import edu.internet2.middleware.signet.choice.ChoiceNotFoundException;
import edu.internet2.middleware.signet.choice.ChoiceSet;
import edu.internet2.middleware.signet.choice.ChoiceSetNotFoundException;
import edu.internet2.middleware.signet.test.Constants;
import edu.internet2.middleware.signet.test.Fixtures;
import junit.framework.TestCase;

/**
 * @author acohen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ChoiceTest extends TestCase
{
  private Signet		signet;
  private Fixtures	fixtures;
  
  public static void main(String[] args)
  {
    junit.textui.TestRunner.run(ChoiceTest.class);
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
   * Constructor for ChoiceTest.
   * @param name
   */
  public ChoiceTest(String name)
  {
    super(name);
  }

  public final void testGetValue()
  throws
  	ObjectNotFoundException,
  	ChoiceNotFoundException
  {
    for (int choiceSetIndex = 0;
    		 choiceSetIndex < Constants.MAX_CHOICE_SETS;
    		 choiceSetIndex++)
    {
      ChoiceSet choiceSet
      	= signet
    				.getSubsystem(Constants.SUBSYSTEM_ID)
    					.getChoiceSet
    						(fixtures.makeChoiceSetId(choiceSetIndex));
    
      // choiceSet 0 has 0 choices, choiceSet 1 has 1, and so on.
      for (int i = 0; i < choiceSetIndex; i++)
      {      
        Choice choice
        	= choiceSet.getChoiceByValue(fixtures.makeChoiceValue(i));
        assertEquals(fixtures.makeChoiceValue(i), choice.getValue());
      }
    }
  }

  public final void testGetDisplayValue()
  throws
  	ObjectNotFoundException,
  	ChoiceNotFoundException
  {
    for (int choiceSetIndex = 0;
		 		 choiceSetIndex < Constants.MAX_CHOICE_SETS;
		 		 choiceSetIndex++)
    {
      ChoiceSet choiceSet
      	= signet
      			.getSubsystem(Constants.SUBSYSTEM_ID)
      				.getChoiceSet
      					(fixtures.makeChoiceSetId(choiceSetIndex));

      // choiceSet 0 has 0 choices, choiceSet 1 has 1, and so on.
      for (int i = 0; i < choiceSetIndex; i++)
      {      
        Choice choice
        	= choiceSet.getChoiceByValue(fixtures.makeChoiceValue(i));
        assertEquals
        	(fixtures.makeChoiceDisplayValue(i),
        	 choice.getDisplayValue());
      }
    }
  }

  public final void testGetDisplayOrder()
  throws
		ObjectNotFoundException,
		ChoiceNotFoundException
  {
    for (int choiceSetIndex = 0;
		 		 choiceSetIndex < Constants.MAX_CHOICE_SETS;
		 		 choiceSetIndex++)
    {
      ChoiceSet choiceSet
      	= signet
      			.getSubsystem(Constants.SUBSYSTEM_ID)
      				.getChoiceSet
      					(fixtures.makeChoiceSetId(choiceSetIndex));

      // choiceSet 0 has 0 choices, choiceSet 1 has 1, and so on.
      for (int i = 0; i < choiceSetIndex; i++)
      {      
        Choice choice
        	= choiceSet.getChoiceByValue(fixtures.makeChoiceValue(i));
        assertEquals
        	(fixtures.makeChoiceDisplayOrder(i),
        	 choice.getDisplayOrder());
      }
    }
  }

  public final void testGetRank()
  throws
  	ObjectNotFoundException,
  	ChoiceNotFoundException
  {
    for (int choiceSetIndex = 0;
				 choiceSetIndex < Constants.MAX_CHOICE_SETS;
				 choiceSetIndex++)
    {
      ChoiceSet choiceSet
      	= signet
      			.getSubsystem(Constants.SUBSYSTEM_ID)
      				.getChoiceSet
      					(fixtures.makeChoiceSetId(choiceSetIndex));

      // choiceSet 0 has 0 choices, choiceSet 1 has 1, and so on.
      for (int i = 0; i < choiceSetIndex; i++)
      {      
        Choice choice
        	= choiceSet.getChoiceByValue(fixtures.makeChoiceValue(i));
        assertEquals
        	(fixtures.makeChoiceRank(i, choiceSetIndex),
        	 choice.getRank());
      }
    }
  }
  
  public final void testGetChoiceSet()
  throws
  	ObjectNotFoundException,
  	ChoiceNotFoundException,
  	ChoiceSetNotFoundException
  {
    for (int choiceSetIndex = 0;
		 choiceSetIndex < Constants.MAX_CHOICE_SETS;
		 choiceSetIndex++)
    {
      ChoiceSet choiceSet
     		= signet
     				.getSubsystem(Constants.SUBSYSTEM_ID)
     					.getChoiceSet
     						(fixtures.makeChoiceSetId(choiceSetIndex));

     // choiceSet 0 has 0 choices, choiceSet 1 has 1, and so on.
     for (int i = 0; i < choiceSetIndex; i++)
     {      
       Choice choice
       	= choiceSet.getChoiceByValue(fixtures.makeChoiceValue(i));
       assertEquals(choiceSet, choice.getChoiceSet());
     }
   }
  }
}
