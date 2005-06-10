/*--
$Id: AssignmentTest.java,v 1.9 2005-06-10 23:05:12 acohen Exp $
$Date: 2005-06-10 23:05:12 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet.test;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import edu.internet2.middleware.signet.Assignment;
import edu.internet2.middleware.signet.Function;
import edu.internet2.middleware.signet.Limit;
import edu.internet2.middleware.signet.LimitValue;
import edu.internet2.middleware.signet.ObjectNotFoundException;
import edu.internet2.middleware.signet.Permission;
import edu.internet2.middleware.signet.PrivilegedSubject;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.SignetAuthorityException;
import edu.internet2.middleware.signet.Status;
import edu.internet2.middleware.subject.Subject;

import junit.framework.TestCase;

/**
 * @author acohen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class AssignmentTest extends TestCase
{
  private Signet		signet;
  private Fixtures	fixtures;
  
  public static void main(String[] args)
  {
    junit.textui.TestRunner.run(AssignmentTest.class);
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
  public AssignmentTest(String name)
  {
    super(name);
  }
  
  public final void testRevoke()
  throws
    ObjectNotFoundException,
    SignetAuthorityException
  {
    for (int subjectIndex = 0;
         subjectIndex < Constants.MAX_SUBJECTS;
         subjectIndex++)
    {
      Subject subject
        = signet.getSubject
            (Signet.DEFAULT_SUBJECT_TYPE_ID,
             fixtures.makeSubjectId(subjectIndex));
      
      PrivilegedSubject pSubject = signet.getPrivilegedSubject(subject);
      Set assignmentsReceived
        = pSubject.getAssignmentsReceived
            (null, signet.getSubsystem(Constants.SUBSYSTEM_ID), null);
      Assignment assignment = (Assignment)(assignmentsReceived.toArray()[0]);
      
      assignment.revoke(assignment.getGrantor());
    }
  }

  public final void testGetLimitValuesArray()
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
      Set assignmentsReceived
      	= pSubject.getAssignmentsReceived
      			(Status.ACTIVE, signet.getSubsystem(Constants.SUBSYSTEM_ID), null);
      
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
      
      // subject 0 should have 2 Assignments. All others should have just 1.
      if (subjectIndex == 0)
      {
        assertEquals(2, assignmentsReceived.size());
      }
      else
      {
        assertEquals(1, assignmentsReceived.size());
      }
      Assignment assignment = (Assignment)(assignmentsReceived.toArray()[0]);

      LimitValue limitValues[] = assignment.getLimitValuesArray();
      assertEquals
        (this.fixtures.expectedLimitValuesCount(subjectIndex),
         limitValues.length);
      
      for (int i = 0; i < limitValues.length; i++)
      {
        LimitValue limitValue = limitValues[i];

        assertEquals
        	(signet
        	 	.getSubsystem(Constants.SUBSYSTEM_ID)
        	 		.getLimit(limitValue.getLimit().getId()),
      	   limitValue.getLimit());
      
        assertEquals
      	  (fixtures.makeChoiceValue
      	      (limitNumber(limitValue.getLimit().getName())),
      	   limitValue.getValue());
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
