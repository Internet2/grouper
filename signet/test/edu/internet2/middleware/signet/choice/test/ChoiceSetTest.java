/*--
$Id: ChoiceSetTest.java,v 1.12 2007-02-24 02:11:32 ddonn Exp $
$Date: 2007-02-24 02:11:32 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet.choice.test;

import java.util.Set;
import org.hibernate.Session;
import org.hibernate.Transaction;
import junit.framework.TestCase;
import edu.internet2.middleware.signet.ObjectNotFoundException;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.Subsystem;
import edu.internet2.middleware.signet.choice.ChoiceSet;
import edu.internet2.middleware.signet.choice.ChoiceSetAdapter;
import edu.internet2.middleware.signet.dbpersist.HibernateDB;
import edu.internet2.middleware.signet.test.Constants;
import edu.internet2.middleware.signet.test.Fixtures;

public class ChoiceSetTest extends TestCase
{
  private Signet		signet;
  private Fixtures	fixtures;
  protected HibernateDB hibr;
  protected Session hs;
  protected Transaction tx;

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
    hibr = signet.getPersistentDB();
    hs = hibr.openSession();
    tx = hs.beginTransaction();
    fixtures = new Fixtures(signet);
  }

  /*
   * @see TestCase#tearDown()
   */
  protected void tearDown() throws Exception
  {
    super.tearDown();
    
    tx.commit();
    hibr.closeSession(hs);
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
      ChoiceSet choiceSet = signet.getPersistentDB().getSubsystem(Constants.SUBSYSTEM_ID)
      				.getChoiceSet(fixtures.makeChoiceSetId(choiceSetIndex));
      
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
      	= signet.getPersistentDB()
      			.getSubsystem(Constants.SUBSYSTEM_ID)
      				.getChoiceSet
      					(fixtures.makeChoiceSetId(choiceSetIndex));
 
      Subsystem subsystem = choiceSet.getSubsystem();
      assertEquals(subsystem, signet.getPersistentDB().getSubsystem(Constants.SUBSYSTEM_ID));
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
      	= signet.getPersistentDB()
      			.getSubsystem(Constants.SUBSYSTEM_ID)
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
      	= signet.getPersistentDB()
      			.getSubsystem(Constants.SUBSYSTEM_ID)
      				.getChoiceSet
      					(fixtures.makeChoiceSetId(choiceSetIndex));

      // choiceSet 0 contains choice 0 and choice 0_changed,
      // choiceSet 1 contains choices 0, 0_changed, 1 and 1_changed,
      // and so on.
      Set choices = choiceSet.getChoices();
      assertEquals((choiceSetIndex + 1) * 2, choices.size());
    }
  }
}
