/*--
$Id: PrivilegeTest.java,v 1.2 2005-07-08 03:03:58 acohen Exp $
$Date: 2005-07-08 03:03:58 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet.test;

import java.util.Set;
import java.util.StringTokenizer;

import edu.internet2.middleware.signet.LimitValue;
import edu.internet2.middleware.signet.ObjectNotFoundException;
import edu.internet2.middleware.signet.Privilege;
import edu.internet2.middleware.signet.PrivilegedSubject;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.subject.Subject;

import junit.framework.TestCase;

/**
 * @author acohen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class PrivilegeTest extends TestCase
{
  private Signet		signet;
  private Fixtures	fixtures;
  
  public static void main(String[] args)
  {
    junit.textui.TestRunner.run(PrivilegeTest.class);
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
    signet.commit();
    signet.close();
    
    // Let's use a new Signet session, to make sure we're actually
    // pulling data from the database, and not just referring to in-memory
    // structures.
    signet = new Signet();
    signet.beginTransaction();
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
   * Constructor for LimitTest.
   * @param name
   */
  public PrivilegeTest(String name)
  {
    super(name);
  }

  public final void testGetLimitValues()
  throws
    ObjectNotFoundException
  {
    for (int subjectIndex = 0;
         subjectIndex < Constants.MAX_SUBJECTS;
         subjectIndex++)
    {
      Subject subject
        = signet.getSubject(
            Signet.DEFAULT_SUBJECT_TYPE_ID, fixtures.makeSubjectId(subjectIndex));
      
      PrivilegedSubject pSubject = signet.getPrivilegedSubject(subject);
      Set privileges = pSubject.getPrivileges();
      
      // Here's a picture of the Assignments which this test expects to find:
      //
      // Subject 0
      //   Function 0
      //     Permission 0
      //       Limit 0
      //         limit-value: 0
      //    Function 2
      //      Permission 2
      //        Limit 0
      //          limit-value: 0
      //        Limit 1
      //          limit-value: 0
      //        Limit 2
      //          limit-value: 0
      //  Subject 1
      //    Function 1
      //      Permission 1
      //        Limit 0
      //          limit-value: 0
      //        Limit 1
      //          limit-value: 1
      //  Subject 2
      //    Function 2
      //      Permission 2
      //        Limit 0
      //          limit-value: 0
      //        Limit 1
      //          limit-value: 1
      //        Limit 2
      //          limit-value: 2
      
      // subject 0 should have 2 Privileges. All others should have just 1.
      if (subjectIndex == 0)
      {
        assertEquals(2, privileges.size());
      }
      else
      {
        assertEquals(1, privileges.size());
      }
      
      Privilege privilege = (Privilege)(privileges.toArray()[0]);

      LimitValue[] limitValuesArray
        = Common.getLimitValuesInDisplayOrder(privilege);
      assertEquals
        (this.fixtures.expectedLimitValuesCount
            (subjectIndex, privilege.getPermission()),
         limitValuesArray.length);
      
      for (int i = 0; i < limitValuesArray.length; i++)
      {
        LimitValue limitValue = limitValuesArray[i];

        assertEquals
          (signet
            .getSubsystem(Constants.SUBSYSTEM_ID)
              .getLimit(limitValue.getLimit().getId()),
           limitValue.getLimit());
      
        assertEquals
          (fixtures.makeChoiceValue
              (subjectIndex, limitNumber(limitValue.getLimit().getName())),
           limitValue.getValue());
      }
    }
  }

  public final void testGetPermission()
  throws
    ObjectNotFoundException
  {
    for (int subjectIndex = 0;
         subjectIndex < Constants.MAX_SUBJECTS;
         subjectIndex++)
    {
      Subject subject
        = signet.getSubject(
            Signet.DEFAULT_SUBJECT_TYPE_ID, fixtures.makeSubjectId(subjectIndex));
      
      PrivilegedSubject pSubject = signet.getPrivilegedSubject(subject);
      Set privileges = pSubject.getPrivileges();
      
      // Here's a picture of the Assignments which this test expects to find:
      //
      // Subject 0
      //   Function 0
      //     Permission 0
      //   Function 2
      //     Permission 2
      //  Subject 1
      //    Function 1
      //      Permission 1
      //  Subject 2
      //    Function 2
      //      Permission 2
      
      // subject 0 should have 2 Privileges. All others should have just 1.
      if (subjectIndex == 0)
      {
        assertEquals(2, privileges.size());
      }
      else
      {
        assertEquals(1, privileges.size());
      }
      
      Privilege privilege = (Privilege)(privileges.toArray()[0]);
      
      if (subjectIndex > 0)
      {
        assertEquals
          (privilege.getPermission().getId(),
           fixtures.makePermissionId(subjectIndex));
      }
    }
  }
  
  private int limitNumber(String limitName)
  {
    StringTokenizer tokenizer
    	= new StringTokenizer(limitName, Constants.DELIMITER);
    String prefix = tokenizer.nextToken();
    int number = (new Integer(tokenizer.nextToken())).intValue();
    return number;
  }
}
