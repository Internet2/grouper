/*--
$Id: AssignmentTest.java,v 1.25 2006-06-30 02:04:41 ddonn Exp $
$Date: 2006-06-30 02:04:41 $

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
import edu.internet2.middleware.signet.LimitValue;
import edu.internet2.middleware.signet.ObjectNotFoundException;
import edu.internet2.middleware.signet.PrivilegedSubject;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.SignetAuthorityException;
import edu.internet2.middleware.signet.Status;
import edu.internet2.middleware.signet.Subsystem;
import edu.internet2.middleware.signet.tree.TreeNode;
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
    signet.getPersistentDB().beginTransaction();
    fixtures = new Fixtures(signet);
    signet.getPersistentDB().commit();
    signet.getPersistentDB().close();
    
    // Let's use a new Signet session, to make sure we're actually
    // pulling data from the database, and not just referring to in-memory
    // structures.
    signet = new Signet();
    signet.getPersistentDB().beginTransaction();
  }

  /*
   * @see TestCase#tearDown()
   */
  protected void tearDown() throws Exception
  {
    super.tearDown();
    signet.getPersistentDB().commit();
    signet.getPersistentDB().close();
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
        = signet.getSubjectSources().getSubject
            (Signet.DEFAULT_SUBJECT_TYPE_ID,
             Common.makeSubjectId(subjectIndex));
      
      PrivilegedSubject pSubject = signet.getSubjectSources().getPrivilegedSubject(subject);
      
      Set assignmentsReceived = pSubject.getAssignmentsReceived();
      assignmentsReceived
        = Common.filterAssignments
            (assignmentsReceived, Constants.STATUS_ACTIVE_OR_PENDING);
      assignmentsReceived
        = Common.filterAssignments
            (assignmentsReceived, signet.getPersistentDB().getSubsystem(Constants.SUBSYSTEM_ID));
      
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
        = signet.getSubjectSources().getSubject
            (Signet.DEFAULT_SUBJECT_TYPE_ID,
             Common.makeSubjectId(subjectIndex));
 
      PrivilegedSubject pSubject = signet.getSubjectSources().getPrivilegedSubject(subject);
      
      Set assignmentsReceived = pSubject.getAssignmentsReceived();
      assignmentsReceived
        = Common.filterAssignments
            (assignmentsReceived, signet.getPersistentDB().getSubsystem(Constants.SUBSYSTEM_ID));
      
      
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
                 Constants.TODAY,  // EffectiveDate and expirationDate are not
                 Constants.TOMORROW); // considered when finding duplicates.
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
      	= signet.getSubjectSources().getSubject(
      			Signet.DEFAULT_SUBJECT_TYPE_ID, Common.makeSubjectId(subjectIndex));
      
      PrivilegedSubject pSubject = signet.getSubjectSources().getPrivilegedSubject(subject);
      
      Set assignmentsReceived = pSubject.getAssignmentsReceived();
      assignmentsReceived
        = Common.filterAssignments(assignmentsReceived, Status.ACTIVE);
      assignmentsReceived
        = Common.filterAssignments
            (assignmentsReceived, signet.getPersistentDB().getSubsystem(Constants.SUBSYSTEM_ID));
      
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
        	(signet.getPersistentDB()
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
        = signet.getSubjectSources().getSubject(
            Signet.DEFAULT_SUBJECT_TYPE_ID, Common.makeSubjectId(subjectIndex));
      
      PrivilegedSubject pSubject = signet.getSubjectSources().getPrivilegedSubject(subject);
      
      Set assignmentsReceived = pSubject.getAssignmentsReceived();
      assignmentsReceived
        = Common.filterAssignments(assignmentsReceived, Status.ACTIVE);
      assignmentsReceived
        = Common.filterAssignments
            (assignmentsReceived, signet.getPersistentDB().getSubsystem(Constants.SUBSYSTEM_ID));

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
        = signet.getSubjectSources().getSubject(
            Signet.DEFAULT_SUBJECT_TYPE_ID, Common.makeSubjectId(subjectIndex));
      
      PrivilegedSubject pSubject = signet.getSubjectSources().getPrivilegedSubject(subject);
      
      Set assignmentsReceived = pSubject.getAssignmentsReceived();
      assignmentsReceived
        = Common.filterAssignments(assignmentsReceived, Status.ACTIVE);
      assignmentsReceived
        = Common.filterAssignments
            (assignmentsReceived, signet.getPersistentDB().getSubsystem(Constants.SUBSYSTEM_ID));

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
        = signet.getSubjectSources().getSubject
            (Signet.DEFAULT_SUBJECT_TYPE_ID,
             Common.makeSubjectId(subjectIndex));
      
      PrivilegedSubject pSubject = signet.getSubjectSources().getPrivilegedSubject(subject);
      
      Set assignmentsReceived = pSubject.getAssignmentsReceived();
      assignmentsReceived
        = Common.filterAssignments(assignmentsReceived, Status.ACTIVE);
      assignmentsReceived
        = Common.filterAssignments
            (assignmentsReceived, signet.getPersistentDB().getSubsystem(Constants.SUBSYSTEM_ID));

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
      = signet.getSubjectSources().getSubject
          (Signet.DEFAULT_SUBJECT_TYPE_ID, Common.makeSubjectId(0));
    PrivilegedSubject pSubject0 = signet.getSubjectSources().getPrivilegedSubject(subject0);
    
    // Get any one of subject0's Assignments - we don't care which one.
    Set assignmentsReceived = pSubject0.getAssignmentsReceived();
    Iterator assignmentsReceivedIterator = assignmentsReceived.iterator();
    while (assignmentsReceivedIterator.hasNext())
    {
      assignment = (Assignment)(assignmentsReceivedIterator.next());
    }
    
    assertNotNull(assignment);
    assertNotSame(Status.INACTIVE, assignment.getStatus());
    
    Date lastWeek  = Common.getDate(-7);
    Date nextWeek = Common.getDate(7);
    
    PrivilegedSubject grantor = Common.getOriginalGrantor(assignment);
    
    assignment.setEffectiveDate(grantor, Constants.YESTERDAY);
    assignment.setExpirationDate(grantor, Constants.TOMORROW);
    assignment.evaluate();
    assertEquals(Status.ACTIVE, assignment.getStatus());
    
    assignment.setEffectiveDate(grantor, Constants.TOMORROW);
    assignment.setExpirationDate(grantor, nextWeek);
    assignment.evaluate();
    assertEquals(Status.PENDING, assignment.getStatus());
    
    assignment.setEffectiveDate(grantor, lastWeek);
    assignment.setExpirationDate(grantor, Constants.YESTERDAY);
    assignment.evaluate();
    assertEquals(Status.INACTIVE, assignment.getStatus());
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
        = signet.getSubjectSources().getSubject
            (Signet.DEFAULT_SUBJECT_TYPE_ID,
             Common.makeSubjectId(subjectIndex));
      
      PrivilegedSubject pSubject = signet.getSubjectSources().getPrivilegedSubject(subject);
      
      Set assignmentsReceived = pSubject.getAssignmentsReceived();
      assignmentsReceived
        = Common.filterAssignments(assignmentsReceived, Status.ACTIVE);
      assignmentsReceived
        = Common.filterAssignments
            (assignmentsReceived, signet.getPersistentDB().getSubsystem(Constants.SUBSYSTEM_ID));

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
        = signet.getSubjectSources().getSubject
            (Signet.DEFAULT_SUBJECT_TYPE_ID,
             Common.makeSubjectId(subjectIndex));
      
      PrivilegedSubject pSubject = signet.getSubjectSources().getPrivilegedSubject(subject);
      
      Set assignmentsReceived = pSubject.getAssignmentsReceived();
      assignmentsReceived
        = Common.filterAssignments(assignmentsReceived, Status.ACTIVE);
      assignmentsReceived
        = Common.filterAssignments
            (assignmentsReceived, signet.getPersistentDB().getSubsystem(Constants.SUBSYSTEM_ID));

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

  public final void testGetHistory()
  throws ObjectNotFoundException
  { 
    for (int subjectIndex = 0;
         subjectIndex < Constants.MAX_SUBJECTS;
         subjectIndex++)
    {
      Subject subject
        = signet.getSubjectSources().getSubject
            (Signet.DEFAULT_SUBJECT_TYPE_ID,
             Common.makeSubjectId(subjectIndex));
      
      PrivilegedSubject pSubject = signet.getSubjectSources().getPrivilegedSubject(subject);
      
      Set assignmentsReceived = pSubject.getAssignmentsReceived();
      assignmentsReceived
        = Common.filterAssignments(assignmentsReceived, Status.ACTIVE);
      assignmentsReceived
        = Common.filterAssignments
            (assignmentsReceived, signet.getPersistentDB().getSubsystem(Constants.SUBSYSTEM_ID));

      // Every single Assignment should have a single History record.
      Iterator assignmentsReceivedIterator = assignmentsReceived.iterator();
      while (assignmentsReceivedIterator.hasNext())
      {
        Assignment assignment
          = (Assignment)(assignmentsReceivedIterator.next());
        
        Set historySet = assignment.getHistory();
        assertNotNull(historySet);
        assertEquals(1, historySet.size());
      }
    }
  }

  public final void testGetEffectiveDate()
  throws ObjectNotFoundException
  { 
    int totalAssignmentsFound = 0;
    
    for (int subjectIndex = 0;
         subjectIndex < Constants.MAX_SUBJECTS;
         subjectIndex++)
    {
      Subject subject
        = signet.getSubjectSources().getSubject
            (Signet.DEFAULT_SUBJECT_TYPE_ID,
             Common.makeSubjectId(subjectIndex));
      
      PrivilegedSubject pSubject = signet.getSubjectSources().getPrivilegedSubject(subject);
      
      Set assignmentsReceived = pSubject.getAssignmentsReceived();
      assignmentsReceived
        = Common.filterAssignments(assignmentsReceived, Status.ACTIVE);
      assignmentsReceived
        = Common.filterAssignments
            (assignmentsReceived, signet.getPersistentDB().getSubsystem(Constants.SUBSYSTEM_ID));

      Iterator assignmentsReceivedIterator = assignmentsReceived.iterator();
      while (assignmentsReceivedIterator.hasNext())
      {
        Assignment assignment
          = (Assignment)(assignmentsReceivedIterator.next());
        totalAssignmentsFound++;
        
        Date effectiveDate = assignment.getEffectiveDate();

        assertNotNull(effectiveDate);
        assertEquals(Constants.YESTERDAY, effectiveDate);
      }
    }
    
    assertTrue
      ("We expect to encounter at least one Assignment.",
       totalAssignmentsFound > 0);
  }

  public final void testGetScope()
  throws ObjectNotFoundException
  { 
    for (int subjectIndex = 0;
         subjectIndex < Constants.MAX_SUBJECTS;
         subjectIndex++)
    {
      Subject subject
        = signet.getSubjectSources().getSubject
            (Signet.DEFAULT_SUBJECT_TYPE_ID,
             Common.makeSubjectId(subjectIndex));
      
      PrivilegedSubject pSubject = signet.getSubjectSources().getPrivilegedSubject(subject);
      
      Set assignmentsReceived = pSubject.getAssignmentsReceived();
      assignmentsReceived
        = Common.filterAssignments(assignmentsReceived, Status.ACTIVE);
      assignmentsReceived
        = Common.filterAssignments
            (assignmentsReceived, signet.getPersistentDB().getSubsystem(Constants.SUBSYSTEM_ID));

      // Every single Assignment should have the same scope.
      Iterator assignmentsReceivedIterator = assignmentsReceived.iterator();
      while (assignmentsReceivedIterator.hasNext())
      {
        Assignment assignment
          = (Assignment)(assignmentsReceivedIterator.next());
        
        TreeNode scope = assignment.getScope();
        assertNotNull(scope);
        assertEquals(Common.getRootNode(signet), scope);
      }
    }
  }
  
  public final void testCanGrant()
  throws ObjectNotFoundException
  {
    for (int subjectIndex = 0;
         subjectIndex < Constants.MAX_SUBJECTS;
         subjectIndex++)
    {
      Subject subject
       = signet.getSubjectSources().getSubject
           (Signet.DEFAULT_SUBJECT_TYPE_ID,
            Common.makeSubjectId(subjectIndex));
 
      PrivilegedSubject pSubject = signet.getSubjectSources().getPrivilegedSubject(subject);
 
      Set assignmentsReceived = pSubject.getAssignmentsReceived();
      assignmentsReceived
       = Common.filterAssignments(assignmentsReceived, Status.ACTIVE);
      assignmentsReceived
       = Common.filterAssignments
           (assignmentsReceived, signet.getPersistentDB().getSubsystem(Constants.SUBSYSTEM_ID));

      Iterator assignmentsReceivedIterator = assignmentsReceived.iterator();
      while (assignmentsReceivedIterator.hasNext())
      {
        Assignment assignment
          = (Assignment)(assignmentsReceivedIterator.next());
   
        boolean canGrant = assignment.canGrant();
        assertEquals(Constants.ASSIGNMENT_CANGRANT, canGrant);
      }
    }
  }
  
  public final void testCanUse()
  throws ObjectNotFoundException
  {
    for (int subjectIndex = 0;
    subjectIndex < Constants.MAX_SUBJECTS;
    subjectIndex++)
    {
      Subject subject
       = signet.getSubjectSources().getSubject
           (Signet.DEFAULT_SUBJECT_TYPE_ID,
            Common.makeSubjectId(subjectIndex));

      PrivilegedSubject pSubject = signet.getSubjectSources().getPrivilegedSubject(subject);

      Set assignmentsReceived = pSubject.getAssignmentsReceived();
      assignmentsReceived
        = Common.filterAssignments(assignmentsReceived, Status.ACTIVE);
      assignmentsReceived
        = Common.filterAssignments
            (assignmentsReceived, signet.getPersistentDB().getSubsystem(Constants.SUBSYSTEM_ID));

      Iterator assignmentsReceivedIterator = assignmentsReceived.iterator();
      while (assignmentsReceivedIterator.hasNext())
      {
        Assignment assignment
          = (Assignment)(assignmentsReceivedIterator.next());

        boolean canUse = assignment.canUse();
        assertEquals(Constants.ASSIGNMENT_CANUSE, canUse);
      }
    }
  }
  
  public final void testGetGrantee()
  throws ObjectNotFoundException
  {
    for (int subjectIndex = 0;
    subjectIndex < Constants.MAX_SUBJECTS;
    subjectIndex++)
    {
      Subject subject
       = signet.getSubjectSources().getSubject
           (Signet.DEFAULT_SUBJECT_TYPE_ID,
            Common.makeSubjectId(subjectIndex));

      PrivilegedSubject pSubject = signet.getSubjectSources().getPrivilegedSubject(subject);

      Set assignmentsReceived = pSubject.getAssignmentsReceived();

      Iterator assignmentsReceivedIterator = assignmentsReceived.iterator();
      while (assignmentsReceivedIterator.hasNext())
      {
        Assignment assignment
          = (Assignment)(assignmentsReceivedIterator.next());

        PrivilegedSubject grantee = assignment.getGrantee();
        assertEquals(pSubject, grantee);
      }
    }
  }
  
  public final void testGetGrantor()
  throws ObjectNotFoundException
  {
    for (int subjectIndex = 0;
    subjectIndex < Constants.MAX_SUBJECTS;
    subjectIndex++)
    {
      Subject subject
       = signet.getSubjectSources().getSubject
           (Signet.DEFAULT_SUBJECT_TYPE_ID,
            Common.makeSubjectId(subjectIndex));

      PrivilegedSubject pSubject = signet.getSubjectSources().getPrivilegedSubject(subject);

      Set assignmentsGranted = pSubject.getAssignmentsGranted();

      Iterator assignmentsGrantedIterator = assignmentsGranted.iterator();
      while (assignmentsGrantedIterator.hasNext())
      {
        Assignment assignment
          = (Assignment)(assignmentsGrantedIterator.next());

        PrivilegedSubject grantor = assignment.getGrantor();
        assertEquals(pSubject, grantor);
      }
    }
  }
  
  public final void testGetProxySubject()
  throws ObjectNotFoundException
  {
    // subject1000 was proxy for assignments granted to subject0, subject1,
    // and subject2.
    correlateGranteeAndProxySubject(0, 1000);
    correlateGranteeAndProxySubject(1, 1000);
    correlateGranteeAndProxySubject(2, 1000);
  }
  
  public final void testGetRevoker()
  throws ObjectNotFoundException
  {
    // Subject0 has revoked an Assignment held by subject2.
    
    PrivilegedSubject expectedRevoker = Common.getPrivilegedSubject(signet, 0);
    PrivilegedSubject grantee = Common.getPrivilegedSubject(signet, 2);
    Set assignments = grantee.getAssignmentsReceived();
    assignments = Common.filterAssignments(assignments, Status.INACTIVE);
    Assignment revokedAssignment
      = (Assignment)(Common.getSingleSetMember(assignments));
    
    PrivilegedSubject actualRevoker = revokedAssignment.getRevoker();
    assertNotNull(actualRevoker);
    assertEquals(expectedRevoker, actualRevoker);
  }

  public final void testGetFunction()
  throws ObjectNotFoundException
  {
    // subject0 has assignments for functions 0 and 2.
    // subject1 has an assignment for function 1.
    // subject2 has an assignment for function 2.
    correlateGranteeAndFunction(0, 0);
    correlateGranteeAndFunction(0, 2);
    correlateGranteeAndFunction(1, 1);
    correlateGranteeAndFunction(2, 2);
  }
  
  private void correlateGranteeAndFunction
    (int subjectIndex,
     int functionIndex)
  throws ObjectNotFoundException
  {
    Subject subject
      = signet.getSubjectSources().getSubject
          (Signet.DEFAULT_SUBJECT_TYPE_ID,
           Common.makeSubjectId(subjectIndex));
  
    PrivilegedSubject pSubject = signet.getSubjectSources().getPrivilegedSubject(subject);
    
    String functionId = fixtures.makeFunctionId(functionIndex);
    Subsystem subsystem = signet.getPersistentDB().getSubsystem(Constants.SUBSYSTEM_ID);
    Function function = Common.getFunction(subsystem, functionId);
    
    // Let's see if this PrivilegedSubject has at least one Assignment for
    // the specified Function.
    Assignment matchingAssignment = null;
    Set assignments = pSubject.getAssignmentsReceived();
    Iterator assignmentsIterator = assignments.iterator();
    while (assignmentsIterator.hasNext())
    {
      Assignment assignment = (Assignment)(assignmentsIterator.next());
      if (assignment.getFunction().equals(function))
      {
        matchingAssignment = assignment;
      }
    }
    
    assertNotNull(matchingAssignment);
  }
  
  private int limitNumber(String limitName)
  {
    StringTokenizer tokenizer
    	= new StringTokenizer(limitName, Constants.DELIMITER);
    /* String prefix = */ tokenizer.nextToken();
    int number = (new Integer(tokenizer.nextToken())).intValue();
    return number;
  }
  
  private void correlateGranteeAndProxySubject
    (int    granteeIndex,
     int    proxySubjectIndex)
  throws ObjectNotFoundException
  {
    Subject grantee
      = signet.getSubjectSources().getSubject
          (Signet.DEFAULT_SUBJECT_TYPE_ID,
           Common.makeSubjectId(granteeIndex));
  
    PrivilegedSubject pGrantee = signet.getSubjectSources().getPrivilegedSubject(grantee);

    Subject proxySubject
      = signet.getSubjectSources().getSubject
          (Signet.DEFAULT_SUBJECT_TYPE_ID,
           Common.makeSubjectId(proxySubjectIndex));
  
    PrivilegedSubject pProxySubject = signet.getSubjectSources().getPrivilegedSubject(proxySubject);
    
    // Let's see if this grantee has at least one Assignment proxied by this
    // proxySubject.
    Assignment matchingAssignment = null;
    Set assignments = pGrantee.getAssignmentsReceived();
    Iterator assignmentsIterator = assignments.iterator();
    while (assignmentsIterator.hasNext())
    {
      Assignment assignment = (Assignment)(assignmentsIterator.next());
      if ((assignment.getProxy() != null)
          && assignment.getProxy().equals(pProxySubject))
      {
        matchingAssignment = assignment;
      }
    }
    
    assertNotNull(matchingAssignment);
  }
}
