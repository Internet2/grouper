/*
 * Created on Jan 14, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.internet2.middleware.signet.choice.test;

import junit.framework.TestCase;

import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.choice.ChoiceSet;

/**
 * @author acohen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
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
  {
    //TODO Implement getId().
  }

  public final void testGetSubsystem()
  {
    //TODO Implement getSubsystem().
  }

  public final void testGetChoiceSetAdapter()
  {
    //TODO Implement getChoiceSetAdapter().
  }

  public final void testGetChoices()
  {
    //TODO Implement getChoices().
  }

}
