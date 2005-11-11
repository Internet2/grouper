/*--
$Id: AssignmentTest.java,v 1.19 2005-11-11 00:24:01 acohen Exp $
$Date: 2005-11-11 00:24:01 $

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
import edu.internet2.middleware.signet.LimitValue;
import edu.internet2.middleware.signet.ObjectNotFoundException;
import edu.internet2.middleware.signet.PrivilegedSubject;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.SignetAuthorityException;
import edu.internet2.middleware.signet.Status;
import edu.internet2.middleware.subject.Subject;

import junit.framework.TestCase;

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
      
      Set assignmentsReceived = pSubject.getAssignmentsReceived();
      assignmentsReceived
        = Common.filterAssignments
            (assignmentsReceived, signet.getSubsystem(Constants.SUBSYSTEM_ID));
      
      Assignment assignment = (Assignment)(assignmentsReceived.toArray()[0]);
      
      PrivilegedSubject revoker;
      
      if (assignment.getProxy() == null)
      {
        revoker = assignment.getGrantor();
      }
      else
      {
        revoker = assignment.getProxy();
        revoker.setActingAs(assignment.getGrantor());
      }
      
      assignment.revoke(revoker);
      assignment.save();
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
      
      Set assignmentsReceived = pSubject.getAssignmentsReceived();
      assignmentsReceived
        = Common.filterAssignments
            (assignmentsReceived, signet.getSubsystem(Constants.SUBSYSTEM_ID));
      
      
      Iterator assignmentsReceivedIterator = assignmentsReceived.iterator();
      while (assignmentsReceivedIterator.hasNext())
      {
        Assignment assignmentReceived
          = (Assignment)(assignmentsReceivedIterator.next());
        
        // At this point, there should be no duplicate Assignments.
        assertEquals(0, assignmentReceived.findDuplicates().size());
        
        // Let's make a duplicate. We need to copy the original Assignment's
        // LimitValues to a new Set, because having two persistable entities
        // pointing to the same persistable Set confuses our database
        // persistence layer.
        Set duplicateLimitValues = new HashSet();
        duplicateLimitValues.addAll(assignmentReceived.getLimitValues());
        Assignment duplicateAssignment
          = Common.getOriginalGrantor(assignmentReceived)
              .grant
                (assignmentReceived.getGrantee(),
                 assignmentReceived.getScope(),
                 assignmentReceived.getFunction(),
                 duplicateLimitValues,
                 assignmentReceived.canUse(),
                 assignmentReceived.canGrant(),
                 new Date(),  // EffectiveDate and expirationDate are not
                 Common.getDate(1)); // considered when finding duplicates.
        duplicateAssignment.save();
        
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
      
      Set assignmentsReceived = pSubject.getAssignmentsReceived();
      assignmentsReceived
        = Common.filterAssignments(assignmentsReceived, Status.ACTIVE);
      assignmentsReceived
        = Common.filterAssignments
            (assignmentsReceived, signet.getSubsystem(Constants.SUBSYSTEM_ID));
      
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
    SignetAuthorityException,
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
      
      Set assignmentsReceived = pSubject.getAssignmentsReceived();
      assignmentsReceived
        = Common.filterAssignments(assignmentsReceived, Status.ACTIVE);
      assignmentsReceived
        = Common.filterAssignments
            (assignmentsReceived, signet.getSubsystem(Constants.SUBSYSTEM_ID));

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
        assignment.setLimitValues
          (Common.getOriginalGrantor(assignment), newLimitValues);
        assignment.save();
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
        assignment.setLimitValues
          (Common.getOriginalGrantor(assignment), originalLimitValues);
        assignment.save();
      }
    }
  }

  public final void testSetEffectiveDate()
  throws
    SignetAuthorityException,
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
      
      Set assignmentsReceived = pSubject.getAssignmentsReceived();
      assignmentsReceived
        = Common.filterAssignments(assignmentsReceived, Status.ACTIVE);
      assignmentsReceived
        = Common.filterAssignments
            (assignmentsReceived, signet.getSubsystem(Constants.SUBSYSTEM_ID));

      // Alter every single effectiveDate for every received Assignment.
      Iterator assignmentsReceivedIterator = assignmentsReceived.iterator();
      while (assignmentsReceivedIterator.hasNext())
      {
        Assignment assignment
          = (Assignment)(assignmentsReceivedIterator.next());

        Date originalEffectiveDate = assignment.getEffectiveDate();
        
        assertEquals
          (Constants.YESTERDAY, originalEffectiveDate);
        
        // Update the Assignment with the altered effectiveDate.
        PrivilegedSubject grantor = Common.getOriginalGrantor(assignment);
        assignment.setEffectiveDate
          (grantor,
           Constants.DAY_BEFORE_YESTERDAY);
        assignment.save();
      }
      
      // Examine every single altered EffectiveDate for every received
      // Assignment, and set them back to their original values.
      assignmentsReceivedIterator = assignmentsReceived.iterator();
      while (assignmentsReceivedIterator.hasNext())
      {
        Assignment assignment
          = (Assignment)(assignmentsReceivedIterator.next());
        
        Date alteredEffectiveDate = assignment.getEffectiveDate();
        
        assertEquals
          (Constants.DAY_BEFORE_YESTERDAY, alteredEffectiveDate);
        
        // Update the Assignment with the restored original effectiveDate.
        PrivilegedSubject grantor = Common.getOriginalGrantor(assignment);
        assignment.setEffectiveDate
          (grantor,
           Constants.YESTERDAY);
        assignment.save();
      }
    }
  }

  public final void testSetExpirationDate()
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
      
      Set assignmentsReceived = pSubject.getAssignmentsReceived();
      assignmentsReceived
        = Common.filterAssignments(assignmentsReceived, Status.ACTIVE);
      assignmentsReceived
        = Common.filterAssignments
            (assignmentsReceived, signet.getSubsystem(Constants.SUBSYSTEM_ID));

      // Alter every single expirationDate for every received Assignment.
      Iterator assignmentsReceivedIterator = assignmentsReceived.iterator();
      while (assignmentsReceivedIterator.hasNext())
      {
        Assignment assignment
          = (Assignment)(assignmentsReceivedIterator.next());

        Date originalExpirationDate = assignment.getExpirationDate();
        
        assertEquals
          (Constants.TOMORROW, originalExpirationDate);
        
//        calendar.setTime(originalExpirationDate);
//        calendar.add(Calendar.WEEK_OF_YEAR, Constants.WEEKS_DIFFERENCE);
//        Date newExpirationDate = calendar.getTime();
        
        // Update the Assignment with the altered expirationDate.
        PrivilegedSubject grantor = Common.getOriginalGrantor(assignment);
        assignment.setExpirationDate
          (grantor,
           Constants.DAY_AFTER_TOMORROW);
        assignment.save();
      }
      
      // Examine every single altered expirationDate for every received
      // Assignment, and set them back to their original values.
      assignmentsReceivedIterator = assignmentsReceived.iterator();
      while (assignmentsReceivedIterator.hasNext())
      {
        Assignment assignment
          = (Assignment)(assignmentsReceivedIterator.next());
        
        Date alteredExpirationDate = assignment.getExpirationDate();
        
        assertEquals
          (Constants.DAY_AFTER_TOMORROW, alteredExpirationDate);
        
        // Update the Assignment with the restored original expirationDate.
        PrivilegedSubject grantor = Common.getOriginalGrantor(assignment);
        assignment.setExpirationDate
          (grantor,
           Constants.TOMORROW);
        assignment.save();
      }
    }
  }
  
  public final void testEvaluate()
  throws
    ObjectNotFoundException,
    SignetAuthorityException
  {
    Assignment assignment = null;
    
    Subject subject0
      = signet.getSubject
          (Signet.DEFAULT_SUBJECT_TYPE_ID, fixtures.makeSubjectId(0));
    PrivilegedSubject pSubject0 = signet.getPrivilegedSubject(subject0);
    
    // Get any one of subject0's Assignments - we don't care which one.
    Set assignmentsReceived = pSubject0.getAssignmentsReceived();
    Iterator assignmentsReceivedIterator = assignmentsReceived.iterator();
    while (assignmentsReceivedIterator.hasNext())
    {
      assignment = (Assignment)(assignmentsReceivedIterator.next());
    }
    
    assertNotNull(assignment);
    
    Date lastWeek  = Common.getDate(-7);
    Date yesterday = Common.getDate(-1);
    Date tomorrow = Common.getDate(1);
    Date nextWeek = Common.getDate(7);
    
    PrivilegedSubject grantor = Common.getOriginalGrantor(assignment);
    
    assignment.setEffectiveDate(grantor, lastWeek);
    assignment.setExpirationDate(grantor, yesterday);
    assertEquals(Status.INACTIVE, assignment.evaluate());
    
    assignment.setEffectiveDate(grantor, yesterday);
    assignment.setExpirationDate(grantor, tomorrow);
    assertEquals(Status.ACTIVE, assignment.evaluate());
    
    assignment.setEffectiveDate(grantor, tomorrow);
    assignment.setExpirationDate(grantor, nextWeek);
    assertEquals(Status.PENDING, assignment.evaluate());
  }

  public final void testSetGrantable()
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
      
      Set assignmentsReceived = pSubject.getAssignmentsReceived();
      assignmentsReceived
        = Common.filterAssignments(assignmentsReceived, Status.ACTIVE);
      assignmentsReceived
        = Common.filterAssignments
            (assignmentsReceived, signet.getSubsystem(Constants.SUBSYSTEM_ID));

      // Alter every single "isGrantable" flag for every received Assignment.
      Iterator assignmentsReceivedIterator = assignmentsReceived.iterator();
      while (assignmentsReceivedIterator.hasNext())
      {
        Assignment assignment
          = (Assignment)(assignmentsReceivedIterator.next());

        boolean originalIsGrantable = assignment.canGrant();
        assertEquals(Constants.ASSIGNMENT_CANGRANT, originalIsGrantable);
        
        // Update the Assignment with the altered isGrantable flag.
        PrivilegedSubject grantor = Common.getOriginalGrantor(assignment);
        assignment.setCanGrant
          (grantor, !originalIsGrantable);
        assignment.save();
      }
      
      // Examine every single altered "isGrantable" flag for every received
      // Assignment, and set them back to their original values.
      assignmentsReceivedIterator = assignmentsReceived.iterator();
      while (assignmentsReceivedIterator.hasNext())
      {
        Assignment assignment
          = (Assignment)(assignmentsReceivedIterator.next());
        
        boolean alteredCanGrant = assignment.canGrant();
        assertEquals(!Constants.ASSIGNMENT_CANGRANT, alteredCanGrant);
        
        // Update the Assignment with the restored original "canGrant" flag.
        PrivilegedSubject grantor = Common.getOriginalGrantor(assignment);
        assignment.setCanGrant
          (grantor,
           Constants.ASSIGNMENT_CANGRANT);
        assignment.save();
      }
    }
  }

  public final void testSetCanUse()
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
      
      Set assignmentsReceived = pSubject.getAssignmentsReceived();
      assignmentsReceived
        = Common.filterAssignments(assignmentsReceived, Status.ACTIVE);
      assignmentsReceived
        = Common.filterAssignments
            (assignmentsReceived, signet.getSubsystem(Constants.SUBSYSTEM_ID));

      // Alter every single "canUse" flag for every received Assignment.
      Iterator assignmentsReceivedIterator = assignmentsReceived.iterator();
      while (assignmentsReceivedIterator.hasNext())
      {
        Assignment assignment
          = (Assignment)(assignmentsReceivedIterator.next());

        boolean originalCanUse = assignment.canUse();
        assertEquals(Constants.ASSIGNMENT_CANUSE, originalCanUse);
        
        // Update the Assignment with the altered canUse flag.
        PrivilegedSubject grantor = Common.getOriginalGrantor(assignment);
        assignment.setCanUse
          (grantor, !originalCanUse);
        assignment.save();
      }
      
      // Examine every single altered "canUse" flag for every received
      // Assignment, and set them back to their original values.
      assignmentsReceivedIterator = assignmentsReceived.iterator();
      while (assignmentsReceivedIterator.hasNext())
      {
        Assignment assignment
          = (Assignment)(assignmentsReceivedIterator.next());
        
        boolean alteredCanUse = assignment.canUse();
        assertEquals(!Constants.ASSIGNMENT_CANUSE, alteredCanUse);
        
        // Update the Assignment with the restored original "canUse" flag.
        PrivilegedSubject grantor = Common.getOriginalGrantor(assignment);
        assignment.setCanUse
          (grantor,
           Constants.ASSIGNMENT_CANUSE);
        assignment.save();
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
