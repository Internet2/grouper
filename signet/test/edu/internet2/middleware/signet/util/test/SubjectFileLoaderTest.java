/*--
$Id: SubjectFileLoaderTest.java,v 1.3 2005-06-01 06:13:08 mnguyen Exp $
$Date: 2005-06-01 06:13:08 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet.util.test;

import java.sql.SQLException;

import edu.internet2.middleware.signet.ObjectNotFoundException;
import edu.internet2.middleware.signet.Signet;

import edu.internet2.middleware.signet.util.SubjectFileLoader;

import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectType;
import edu.internet2.middleware.subject.provider.SubjectTypeEnum;
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
    SQLException
  {
    SubjectType subjectType = SubjectTypeEnum.valueOf("person");
    subjectManager.newSubject
      (subjectType,
       "testNewSubject_id",
       "testNewSubject_name",
       "testNewSubject_description",
       "testNewSubject_displayId");
  }
  
  public final void testNewAttribute()
  throws
    SQLException,
    ObjectNotFoundException
  {
  	SubjectType subjectType = SubjectTypeEnum.valueOf("person");
    Subject subject = subjectManager.newSubject
    (subjectType,
    	       "testNewSubject_id",
    	       "testNewSubject_name",
    	       "testNewSubject_description",
    	       "testNewSubject_displayId");
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
