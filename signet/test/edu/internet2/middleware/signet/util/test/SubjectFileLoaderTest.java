/*--
$Id: SubjectFileLoaderTest.java,v 1.1 2005-04-19 18:20:47 acohen Exp $
$Date: 2005-04-19 18:20:47 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet.util.test;

import java.sql.SQLException;

import javax.naming.OperationNotSupportedException;

import net.sf.hibernate.HibernateException;
import edu.internet2.middleware.signet.ObjectNotFoundException;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.test.Constants;
import edu.internet2.middleware.signet.test.Fixtures;
import edu.internet2.middleware.signet.util.SubjectFileLoader;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectType;
import junit.framework.TestCase;

/**
 * @author Andy Cohen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SubjectFileLoaderTest extends TestCase
{
  private SubjectFileLoader  subjectManager;
  private Signet          signet;
  
  public static void main(String[] args)
  {
    junit.textui.TestRunner.run(SubjectFileLoaderTest.class);
  }

  /*
   * @see TestCase#setUp()
   */
  protected void setUp() throws Exception
  {
    super.setUp();
    subjectManager = new SubjectFileLoader();
    signet = new Signet();
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
  public SubjectFileLoaderTest(String arg0)
  {
    super(arg0);
  }
  
  public final void testNewSubject()
  throws
    ObjectNotFoundException,
    OperationNotSupportedException
  {
    SubjectType subjectType = this.signet.getSubjectType("signet");
    subjectManager.newSubject
      (subjectType,
       "testNewSubject_id",
       "testNewSubject_name",
       "testNewSubject_description",
       "testNewSubject_displayId");
  }
  
  public final void testNewAttribute()
  throws
    HibernateException,
    SQLException,
    ObjectNotFoundException
  {
    Subject subject = this.signet.getSubject("testNewSubject_id");
    subjectManager.newAttribute
      (subject,
       "testNewSubject_attrName",
       1,
       "testNewSubject_attrVal",
       "testNewSubject_attrSearchVal");
  }

//  public final void testDeleteAll()
//  throws HibernateException, SQLException
//  {
//    subjectManager.deleteAll();
//  }
}
