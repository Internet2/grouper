/*--
$Id: SubjectManagerTest.java,v 1.1 2005-04-19 10:11:29 acohen Exp $
$Date: 2005-04-19 10:11:29 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet.util.test;

import java.sql.SQLException;

import net.sf.hibernate.HibernateException;
import edu.internet2.middleware.signet.util.SubjectManager;
import junit.framework.TestCase;

/**
 * @author Andy Cohen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SubjectManagerTest extends TestCase
{
  private SubjectManager subjectManager;
  
  public static void main(String[] args)
  {
    junit.textui.TestRunner.run(SubjectManagerTest.class);
  }

  /*
   * @see TestCase#setUp()
   */
  protected void setUp() throws Exception
  {
    super.setUp();
    subjectManager = new SubjectManager();
  }

  /*
   * @see TestCase#tearDown()
   */
  protected void tearDown() throws Exception
  {
    super.tearDown();
    subjectManager.commit();
  }

  /**
   * Constructor for SubjectManagerTest.
   * @param arg0
   */
  public SubjectManagerTest(String arg0)
  {
    super(arg0);
  }
  
  public final void testNewAttribute() throws HibernateException, SQLException
  {
    subjectManager.newAttribute
      ("signet", "SUBJECT_0_ID", "name1", 1, "value1", "searchValue1");
  }

  public final void testDeleteAll()
  throws HibernateException, SQLException
  {
    subjectManager.deleteAll();
  }
}
