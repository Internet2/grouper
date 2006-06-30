/*--
$Id: BaseTestCase.java,v 1.2 2006-06-30 02:04:41 ddonn Exp $
$Date: 2006-06-30 02:04:41 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet.test;

import edu.internet2.middleware.signet.Signet;
import junit.framework.TestCase;

public class BaseTestCase extends TestCase
{
  protected Signet   signet;
  protected Fixtures fixtures;
  
  BaseTestCase()
  {
    super();
  }

  BaseTestCase(String name)
  {
    super(name);
  }

  protected void setUp() throws Exception
  {
    super.setUp();
    
    signet = new Signet();
    fixtures = new Fixtures(signet);
    signet.getPersistentDB().close();
    
    // Let's use a new Signet session, to make sure we're actually
    // pulling data from the database, and not just referring to in-memory
    // structures.
    signet = new Signet();
  }
  
  protected void tearDown() throws Exception
  {
    super.tearDown();
    signet.getPersistentDB().close();
  }
}
