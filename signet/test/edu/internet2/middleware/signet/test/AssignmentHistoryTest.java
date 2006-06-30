/*--
$Id: AssignmentHistoryTest.java,v 1.3 2006-06-30 02:04:41 ddonn Exp $
$Date: 2006-06-30 02:04:41 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet.test;

import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import edu.internet2.middleware.signet.Assignment;
import edu.internet2.middleware.signet.AssignmentHistory;
import edu.internet2.middleware.signet.Function;
import edu.internet2.middleware.signet.LimitValue;
import edu.internet2.middleware.signet.ObjectNotFoundException;
import edu.internet2.middleware.signet.PrivilegedSubject;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.Status;
import edu.internet2.middleware.signet.Subsystem;
import edu.internet2.middleware.signet.tree.TreeNode;
import edu.internet2.middleware.subject.Subject;

import junit.framework.TestCase;

public class AssignmentHistoryTest extends TestCase
{
  private Signet		signet;
  private Fixtures	fixtures;
  
  public static void main(String[] args)
  {
    junit.textui.TestRunner.run(AssignmentHistoryTest.class);
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
  public AssignmentHistoryTest(String name)
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
      Set assignmentHistoryRecords = assignment.getHistory();
      AssignmentHistory assignmentHistory
        = (AssignmentHistory)
            (Common.getSingleSetMember(assignmentHistoryRecords));

      LimitValue[] limitValuesArray
        = Common.getLimitValuesInDisplayOrder(assignmentHistory);
      assertEquals
        (this.fixtures.expectedLimitValuesCount
            (subjectIndex, assignment.getFunction()),
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
        Set assignmentHistoryRecords = assignment.getHistory();
        AssignmentHistory assignmentHistory
          = (AssignmentHistory)
              Common.getSingleSetMember(assignmentHistoryRecords);
        
        TreeNode scope = assignmentHistory.getScope();
        assertNotNull(scope);
        assertEquals(Common.getRootNode(signet), scope);
      }
    }
  }

  public final void testGetFunction()
  throws ObjectNotFoundException
  {
    // subject0 has assignments for functions 0 and 2.
    // subject1 has an assignment for function 1.
    // subject2 has an assignment for function 2.
    correlateSubjectAndFunction(0, 0);
    correlateSubjectAndFunction(0, 2);
    correlateSubjectAndFunction(1, 1);
    correlateSubjectAndFunction(2, 2);
  }
  
  private void correlateSubjectAndFunction
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
    
    // Let's see if this PrivilegedSubject has at least one AssignmentHistory
    // record for the specified Function.
    AssignmentHistory matchingAssignmentHistoryRecord = null;
    Set assignments = pSubject.getAssignmentsReceived();
    assignments = Common.filterAssignments(assignments, Status.ACTIVE);
    Iterator assignmentsIterator = assignments.iterator();
    while (assignmentsIterator.hasNext())
    {
      Assignment assignment = (Assignment)(assignmentsIterator.next());
      Set assignmentHistorySet = assignment.getHistory();
      assertEquals(1, assignmentHistorySet.size());
      
      Iterator assignmentHistoryIterator = assignmentHistorySet.iterator();
      while (assignmentHistoryIterator.hasNext())
      {
        AssignmentHistory assignmentHistoryRecord
          = (AssignmentHistory)(assignmentHistoryIterator.next());
        
        if (assignmentHistoryRecord.getFunction().equals(function))
        {
          matchingAssignmentHistoryRecord = assignmentHistoryRecord;
        }
      }
    }
    
    assertNotNull(matchingAssignmentHistoryRecord);
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
        Set assignmentHistorySet = assignment.getHistory();
        assertEquals(1, assignmentHistorySet.size());
        
        Iterator assignmentHistoryIterator = assignmentHistorySet.iterator();
        while (assignmentHistoryIterator.hasNext())
        {
          AssignmentHistory assignmentHistoryRecord
            = (AssignmentHistory)(assignmentHistoryIterator.next());
          boolean canGrant = assignmentHistoryRecord.canGrant();
          assertEquals(Constants.ASSIGNMENT_CANGRANT, canGrant);
        }
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
        
        Set assignmentHistorySet = assignment.getHistory();
        assertEquals(1, assignmentHistorySet.size());
        
        Iterator assignmentHistoryIterator = assignmentHistorySet.iterator();
        while (assignmentHistoryIterator.hasNext())
        {
          AssignmentHistory assignmentHistoryRecord
            = (AssignmentHistory)(assignmentHistoryIterator.next());
          boolean canUse = assignmentHistoryRecord.canUse();
          assertEquals(Constants.ASSIGNMENT_CANUSE, canUse);
        }
      }
    }
  }

  public final void testGetEffectiveDate()
  throws ObjectNotFoundException
  { 
    int totalAssignmentHistoryRecordsFound = 0;
    
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
        
        Set assignmentHistorySet = assignment.getHistory();
        Iterator assignmentHistoryIterator = assignmentHistorySet.iterator();
        while (assignmentHistoryIterator.hasNext())
        {
          AssignmentHistory assignmentHistory
            = (AssignmentHistory)(assignmentHistoryIterator.next());
          
          totalAssignmentHistoryRecordsFound++;
          
          Date effectiveDate = assignmentHistory.getEffectiveDate();

          assertNotNull(effectiveDate);
          assertEquals(Constants.YESTERDAY, effectiveDate);
        }
      }
    }
    
    assertTrue
      ("We expect to encounter at least one AssignmentHistory record.",
       totalAssignmentHistoryRecordsFound > 0);
  }
  
  private int limitNumber(String limitName)
  {
    StringTokenizer tokenizer
    	= new StringTokenizer(limitName, Constants.DELIMITER);
    /* String prefix = */ tokenizer.nextToken();
    int number = (new Integer(tokenizer.nextToken())).intValue();
    return number;
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
        
        Set assignmentHistorySet = assignment.getHistory();
        Iterator assignmentHistoryIterator = assignmentHistorySet.iterator();
        while (assignmentHistoryIterator.hasNext())
        {
          AssignmentHistory assignmentHistory
            = (AssignmentHistory)(assignmentHistoryIterator.next());

          PrivilegedSubject grantee = assignmentHistory.getGrantee();
          assertEquals(pSubject, grantee);
        }
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
        
        Set assignmentHistorySet = assignment.getHistory();
        Iterator assignmentHistoryIterator = assignmentHistorySet.iterator();
        while (assignmentHistoryIterator.hasNext())
        {
          AssignmentHistory assignmentHistory
            = (AssignmentHistory)(assignmentHistoryIterator.next());

          PrivilegedSubject grantor = assignmentHistory.getGrantor();
          assertEquals(pSubject, grantor);
        }
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
    boolean revokerFound = false;
    
    PrivilegedSubject expectedRevoker = Common.getPrivilegedSubject(signet, 0);
    PrivilegedSubject grantee = Common.getPrivilegedSubject(signet, 2);
    Set assignments = grantee.getAssignmentsReceived();
    assignments = Common.filterAssignments(assignments, Status.INACTIVE);
    Assignment revokedAssignment
      = (Assignment)(Common.getSingleSetMember(assignments));
    
    Set assignmentHistorySet = revokedAssignment.getHistory();
    Iterator assignmentHistoryIterator = assignmentHistorySet.iterator();
    while (assignmentHistoryIterator.hasNext())
    {
      AssignmentHistory assignmentHistoryRecord
        = (AssignmentHistory)(assignmentHistoryIterator.next());
      
      PrivilegedSubject actualRevoker = assignmentHistoryRecord.getRevoker();
      if ((actualRevoker != null) && (actualRevoker.equals(expectedRevoker)))
      {
        revokerFound = true;
      }
    }
    
    assertTrue(revokerFound);
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
    AssignmentHistory matchingAssignmentHistoryRecord = null;
    Set assignments = pGrantee.getAssignmentsReceived();
    Iterator assignmentsIterator = assignments.iterator();
    while (assignmentsIterator.hasNext())
    {
      Assignment assignment = (Assignment)(assignmentsIterator.next());

      Set assignmentHistorySet = assignment.getHistory();
      Iterator assignmentHistoryIterator = assignmentHistorySet.iterator();
      while (assignmentHistoryIterator.hasNext())
      {
        AssignmentHistory assignmentHistoryRecord
          = (AssignmentHistory)(assignmentHistoryIterator.next());
          
        if ((assignmentHistoryRecord.getProxySubject() != null)
            && assignmentHistoryRecord.getProxySubject()
                .equals(pProxySubject))
        {
          matchingAssignmentHistoryRecord = assignmentHistoryRecord;
        }
      }
    }
    
    assertNotNull(matchingAssignmentHistoryRecord);
  }
}
