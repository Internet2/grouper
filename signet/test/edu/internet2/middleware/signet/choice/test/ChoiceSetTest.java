/*
 * Created on Jan 14, 2005
 */
package edu.internet2.middleware.signet.choice.test;

import java.util.Set;

import junit.framework.TestCase;

import edu.internet2.middleware.signet.ObjectNotFoundException;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.Subsystem;
import edu.internet2.middleware.signet.choice.ChoiceSet;
import edu.internet2.middleware.signet.choice.ChoiceSetAdapter;
import edu.internet2.middleware.signet.test.Constants;
import edu.internet2.middleware.signet.test.Fixtures;

public class ChoiceSetTest extends TestCase
{
  private Signet		signet;
  private Fixtures	fixtures;
  
  public static void main(String[] args)
  {
    junit.textui.TestRunner.run(ChoiceSetTest.class);
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
   * Constructor for ChoiceSetTest.
   * @param name
   */
  public ChoiceSetTest(String name)
  {
    super(name);
  }
  
  public final void testGetId()
  throws ObjectNotFoundException
  {
    for (int choiceSetIndex = 0;
    		 choiceSetIndex < Constants.MAX_CHOICE_SETS;
    		 choiceSetIndex++)
    {
      ChoiceSet choiceSet
      	= fixtures
      			.getSubsystem()
      				.getChoiceSet
      					(fixtures.makeChoiceSetId(choiceSetIndex));
      
      String id = choiceSet.getId();
      assertEquals(id, fixtures.makeChoiceSetId(choiceSetIndex));
    }
  }

  public final void testGetSubsystem()
  throws ObjectNotFoundException
  {
    for (int choiceSetIndex = 0;
		 		 choiceSetIndex < Constants.MAX_CHOICE_SETS;
		 		 choiceSetIndex++)
    {
      ChoiceSet choiceSet
      	= fixtures
      			.getSubsystem()
      				.getChoiceSet
      					(fixtures.makeChoiceSetId(choiceSetIndex));
 
      Subsystem subsystem = choiceSet.getSubsystem();
      assertEquals(subsystem, fixtures.getSubsystem());
    }
  }

  public final void testGetChoiceSetAdapter()
  throws ObjectNotFoundException
  {
    for (int choiceSetIndex = 0;
		 		 choiceSetIndex < Constants.MAX_CHOICE_SETS;
		 		 choiceSetIndex++)
    {
      ChoiceSet choiceSet
      	= fixtures
      			.getSubsystem()
      				.getChoiceSet
      					(fixtures.makeChoiceSetId(choiceSetIndex));

      ChoiceSetAdapter adapter = choiceSet.getChoiceSetAdapter();
      assertNotNull(adapter);
    }
  }

  public final void testGetChoices()
  throws ObjectNotFoundException
  {
    for (int choiceSetIndex = 0;
		 		 choiceSetIndex < Constants.MAX_CHOICE_SETS;
		 		 choiceSetIndex++)
    {
      ChoiceSet choiceSet
      	= fixtures
      			.getSubsystem()
      				.getChoiceSet
      					(fixtures.makeChoiceSetId(choiceSetIndex));

      // choiceSet 0 has 0 choices, choiceSet 1 has 1, and so on.
      Set choices = choiceSet.getChoices();
      assertEquals(choiceSetIndex, choices.size());
    }
  }
}
