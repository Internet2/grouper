/*--
$Id: BaseTestCase.java,v 1.3 2007-02-24 02:11:32 ddonn Exp $
$Date: 2007-02-24 02:11:32 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet.test;

import org.hibernate.Session;
import org.hibernate.Transaction;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.dbpersist.HibernateDB;
import junit.framework.TestCase;

public class BaseTestCase extends TestCase
{
  protected Signet   signet;
  protected Fixtures fixtures;
  protected HibernateDB hibr;
  protected Session hs;
  protected Transaction tx;
  
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
    
    // Let's use a new Signet session, to make sure we're actually
    // pulling data from the database, and not just referring to in-memory
    // structures.
    signet = new Signet();
    hibr = signet.getPersistentDB();
    hs = hibr.openSession();
  }
  
  protected void tearDown() throws Exception
  {
	hibr.closeSession(hs);
    super.tearDown();
  }
}
