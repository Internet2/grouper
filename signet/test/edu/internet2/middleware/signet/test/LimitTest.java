/*
 * Created on Jan 13, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.internet2.middleware.signet.test;

import edu.internet2.middleware.signet.Limit;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.ValueType;

import junit.framework.TestCase;

/**
 * @author acohen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class LimitTest extends TestCase
{
  Signet		signet;
  Limit			limit;
  
  private static final String LIMIT_NAME
  	= "This is the limit name";
  private static final ValueType LIMIT_VALUETYPE
  	= ValueType.NUMERIC;
  private static final String LIMIT_ID
  	= "This is the limit ID";
  private static final String LIMIT_TYPE
  	= "This is the limit type";
  private static final String LIMIT_TYPE_ID
  	= "This is the limit type ID";
  private static final String LIMIT_HELPTEXT
  	= "This is the limit helptext";
  
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
    limit
    	= signet.newLimit
    			(LIMIT_NAME,
    			 LIMIT_VALUETYPE,
    			 LIMIT_ID,
    			 LIMIT_TYPE,
    			 LIMIT_TYPE_ID,
    			 LIMIT_HELPTEXT);
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
  public LimitTest(String name)
  {
    super(name);
  }

  public final void testGetLimitId()
  {
    assertEquals(LIMIT_ID, limit.getLimitId());
  }

  public final void testGetLimitType()
  {
    assertEquals(LIMIT_TYPE, limit.getLimitType());
  }

  public final void testGetLimitTypeId()
  {
    assertEquals(LIMIT_TYPE_ID, limit.getLimitTypeId());
  }

  public final void testGetName()
  {
    assertEquals(LIMIT_NAME, limit.getName());
  }

  public final void testGetHelpText()
  {
    assertEquals(LIMIT_HELPTEXT, limit.getHelpText());
  }

  public final void testGetValueType()
  {
    assertEquals(LIMIT_VALUETYPE, limit.getValueType());
  }

}
