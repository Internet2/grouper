/*--
$Id: AssignmentTest.java,v 1.10 2005-06-17 23:24:28 acohen Exp $
$Date: 2005-06-17 23:24:28 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet.test;

import java.util.Date;
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
  
  public final void testFindDuplicates()
  throws
    SignetAuthorityException,
    ObjectNotFoundException
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
      
      Iterator assignmentsReceivedIterator = assignmentsReceived.iterator();
      while (assignmentsReceivedIterator.hasNext())
      {
        Assignment assignmentReceived
          = (Assignment)(assignmentsReceivedIterator.next());
        
        // At this point, there should be no duplicate Assignments.
        assertEquals(0, assignmentReceived.findDuplicates().size());
        
        // Let's make a duplicate.
        Assignment duplicateAssignment
          = assignmentReceived
              .getGrantor()
                .grant
                  (assignmentReceived.getGrantee(),
                   assignmentReceived.getScope(),
                   assignmentReceived.getFunction(),
                   assignmentReceived.getLimitValues(),
                   assignmentReceived.isGrantable(),
                   assignmentReceived.isGrantOnly(),
                   new Date(),  // EffectiveDate and expirationDate are not
                   new Date()); // considered when finding duplicates.
        signet.save(duplicateAssignment);
        
        // At this point, there shoule be exactly one duplicate Assignment.
        assertEquals(1, assignmentReceived.findDuplicates().size());
      }
    }
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

      LimitValue[] limitValuesArray
        = Common.getLimitValuesInDisplayOrder(assignment);
      assertEquals
        (this.fixtures.expectedLimitValuesCount(subjectIndex, assignment.getFunction()),
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

  public final void testSetLimitValues()
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

      // Alter every single LimitValue for every received Assignment.
      Iterator assignmentsReceivedIterator = assignmentsReceived.iterator();
      while (assignmentsReceivedIterator.hasNext())
      {
        Assignment assignment
          = (Assignment)(assignmentsReceivedIterator.next());

        Set originalLimitValues = assignment.getLimitValues();
        Set newLimitValues = new HashSet();
        Iterator originalLimitValuesIterator = originalLimitValues.iterator();
        while (originalLimitValuesIterator.hasNext())
        {
          LimitValue originalLimitValue
            = (LimitValue)(originalLimitValuesIterator.next());
          LimitValue newLimitValue
            = new LimitValue
                (originalLimitValue.getLimit(),
                 originalLimitValue.getValue() + Constants.CHANGED_SUFFIX);
          newLimitValues.add(newLimitValue);
        }
        
        // Update the Assignment with the altered LimitValues.
        assignment.setLimitValues(newLimitValues);
        signet.save(assignment);
      }
      
      // Examine every single altered LimitValue for every received Assignment,
      // and set them back to their original values.
      assignmentsReceivedIterator = assignmentsReceived.iterator();
      while (assignmentsReceivedIterator.hasNext())
      {
        Assignment assignment
          = (Assignment)(assignmentsReceivedIterator.next());

        Set alteredLimitValues = assignment.getLimitValues();
        Set originalLimitValues = new HashSet();
        Iterator alteredLimitValuesIterator = alteredLimitValues.iterator();
        while (alteredLimitValuesIterator.hasNext())
        {
          LimitValue alteredLimitValue
            = (LimitValue)(alteredLimitValuesIterator.next());
          
          assertTrue
            ("Altered Limit-values are expected to end with the String '"
              + Constants.CHANGED_SUFFIX
              + "'.",
             alteredLimitValue.getValue().endsWith(Constants.CHANGED_SUFFIX));

          // Trim that CHANGED_SUFFIX back off the LimitValue.
          LimitValue originalLimitValue
            = new LimitValue
                (alteredLimitValue.getLimit(),
                 alteredLimitValue.getValue().substring
                   (0,
                    alteredLimitValue.getValue().length()
                    - Constants.CHANGED_SUFFIX.length()));
          originalLimitValues.add(originalLimitValue);
        }
        
        // Update the Assignment with the restored original LimitValues.
        assignment.setLimitValues(originalLimitValues);
        signet.save(assignment);
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
