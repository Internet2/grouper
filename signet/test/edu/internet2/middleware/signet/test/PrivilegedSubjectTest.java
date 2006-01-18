/*--
$Id: PrivilegedSubjectTest.java,v 1.18 2006-01-18 17:11:59 acohen Exp $
$Date: 2006-01-18 17:11:59 $

Copyright 2004 Internet2 and Stanford University.  All Rights Reserved.
Licensed under the Signet License, Version 1,
see doc/license.txt in this distribution.
*/
package edu.internet2.middleware.signet.test;

import java.util.HashSet;
import java.util.Set;

import edu.internet2.middleware.signet.Assignment;
import edu.internet2.middleware.signet.Function;
import edu.internet2.middleware.signet.Limit;
import edu.internet2.middleware.signet.LimitValue;
import edu.internet2.middleware.signet.ObjectNotFoundException;
import edu.internet2.middleware.signet.PrivilegedSubject;
import edu.internet2.middleware.signet.Proxy;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.SignetAuthorityException;
import edu.internet2.middleware.signet.Status;
import edu.internet2.middleware.signet.Subsystem;
import edu.internet2.middleware.signet.tree.Tree;
import edu.internet2.middleware.signet.tree.TreeNode;
import edu.internet2.middleware.subject.Subject;
import junit.framework.TestCase;

public class PrivilegedSubjectTest extends TestCase
{

  private Signet		signet;
  private Fixtures	fixtures;
  
  public static void main(String[] args)
  {
    junit.textui.TestRunner.run(PrivilegedSubjectTest.class);
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
   * Constructor for PrivilegedSubjectTest.
   * @param name
   */
  public PrivilegedSubjectTest(String name)
  {
    super(name);
  }
  
  public final void testCanEdit()
  throws ObjectNotFoundException
  {
    // Test the editability of an Assignment.
    
    Subject subject0 = signet.getSubject(
    		Signet.DEFAULT_SUBJECT_TYPE_ID, Common.makeSubjectId(0));
    PrivilegedSubject pSubject0 = signet.getPrivilegedSubject(subject0);
    
    Subject subject2 = signet.getSubject(
    		Signet.DEFAULT_SUBJECT_TYPE_ID, Common.makeSubjectId(2));
    PrivilegedSubject pSubject2 = signet.getPrivilegedSubject(subject2);
    
    Set assignmentsForSubject2 = pSubject2.getAssignmentsReceived();
    assignmentsForSubject2
      = Common.filterAssignments
          (assignmentsForSubject2, Constants.STATUS_ACTIVE_OR_PENDING);
    
    Assignment assignmentForSubject2
    	= (Assignment)(Common.getSingleSetMember(assignmentsForSubject2));
    
    assertFalse(pSubject0.canEdit(assignmentForSubject2).getAnswer());
    
    // Test the editability of a Proxy.
    
    Subject subject1
      = signet.getSubject
          (Signet.DEFAULT_SUBJECT_TYPE_ID, Common.makeSubjectId(1));
    PrivilegedSubject pSubject1 = signet.getPrivilegedSubject(subject1);
    
    Set proxiesTo0
      = pSubject0.getProxiesReceived();
    Set proxiesFrom2to0 = Common.filterProxiesByGrantor(proxiesTo0, pSubject2);
    
    Proxy proxyFrom2to0 = (Proxy)(Common.getSingleSetMember(proxiesFrom2to0));
    
    assertFalse(pSubject1.canEdit(proxyFrom2to0).getAnswer());
  }

  public final void testGetGrantableChoices()
  throws
  	ObjectNotFoundException
  {
    for (int subjectIndex = 0;
		 		 subjectIndex < Constants.MAX_SUBJECTS;
		 		 subjectIndex++)
    {
      Subject subject
      	= signet.getSubject(
      			Signet.DEFAULT_SUBJECT_TYPE_ID, Common.makeSubjectId(subjectIndex));
      
      PrivilegedSubject pSubject = signet.getPrivilegedSubject(subject);
      
      // Here's a picture of the Assignments and Limit-values which this test
      // expects to find:
      //
      // Subject 0
      //   TreeNode L_0_S_0_ID
      //   Function 0
      //     Permission 0
      //       Limit 0
      //         limit-value: 0
      //  Subject 1
      //    TreeNode L_0_S_0_ID
      //    Function 1
      //      Permission 1
      //        Limit 0
      //          limit-value: 0
      //        Limit 1
      //          limit-value: 1
      //  Subject 2
      //    TreeNode L_0_S_0_ID
      //    Function 2
      //      Permission 2
      //        Limit 0
      //          limit-value: 0
      //        Limit 1
      //          limit-value: 1
      //        Limit 2
      //          limit-value: 2
      
      Subsystem subsystem = signet.getSubsystem(Constants.SUBSYSTEM_ID);
      Function function
        = subsystem.getFunction(fixtures.makeFunctionId(subjectIndex));
      Tree tree = signet.getTree(Constants.TREE_ID);
      TreeNode treeNode = fixtures.getRoot(tree);
      Limit limit = subsystem.getLimit(fixtures.makeLimitId(subjectIndex));
      Set grantableChoices
      	= pSubject.getGrantableChoices(function, treeNode, limit);
      
      assertEquals(1, grantableChoices.size());
    }
  }
  
  public final void testGrantActingAs()
  throws
    SignetAuthorityException,
    ObjectNotFoundException
  {
    // We'll attempt to have subject 1 grant a privilege to subject 2, while
    // "acting as" subject 0.
    
    Subject subject0
      = signet.getSubject
          (Signet.DEFAULT_SUBJECT_TYPE_ID,
           Common.makeSubjectId(0));
    Subject subject1
      = signet.getSubject
          (Signet.DEFAULT_SUBJECT_TYPE_ID,
           Common.makeSubjectId(1));
    Subject subject2
      = signet.getSubject
          (Signet.DEFAULT_SUBJECT_TYPE_ID,
           Common.makeSubjectId(2));
    
    PrivilegedSubject pSubject0 = signet.getPrivilegedSubject(subject0);
    PrivilegedSubject pSubject1 = signet.getPrivilegedSubject(subject1);
    PrivilegedSubject pSubject2 = signet.getPrivilegedSubject(subject2);
    
    Assignment oldAssignment
      = (Assignment)
          (Common.getFirstSetMember
            (pSubject0.getAssignmentsReceived()));
    Set oldLimitValues = oldAssignment.getLimitValues();
    
    pSubject1.setActingAs(pSubject0);
    
    Assignment newAssignment
      = pSubject1.grant
          (pSubject2, // grantee
           oldAssignment.getScope(),
           oldAssignment.getFunction(),
           oldLimitValues,
           false, // canUse
           true, // canGrant
           Constants.TODAY,  // effective immediately
           null);       // no expiration date
    
    pSubject1.setActingAs(null);
    
    assertNotNull(newAssignment);
    assertEquals(pSubject0, newAssignment.getGrantor());
    assertEquals(pSubject1, newAssignment.getProxy());
    assertEquals(pSubject2, newAssignment.getGrantee());
  }
  
  public final void testGrant()
  throws
  	SignetAuthorityException,
  	ObjectNotFoundException
  {
    // We'll attempt to have subject 2 grant a privilege to subject 0.
    
    Subject subject0 = signet.getSubject(
    		Signet.DEFAULT_SUBJECT_TYPE_ID, Common.makeSubjectId(0));
    Subject subject2 = signet.getSubject(
    		Signet.DEFAULT_SUBJECT_TYPE_ID, Common.makeSubjectId(2));
    
    PrivilegedSubject pSubject0 = signet.getPrivilegedSubject(subject0);
    PrivilegedSubject pSubject2 = signet.getPrivilegedSubject(subject2);
    
    Set assignmentsForSubject2 = pSubject2.getAssignmentsReceived();
    assignmentsForSubject2
      = Common.filterAssignments
          (assignmentsForSubject2, Constants.STATUS_ACTIVE_OR_PENDING);
    
    Assignment oldAssignment
    	= (Assignment)
    			(Common.getSingleSetMember(assignmentsForSubject2));
    Set oldLimitValues = oldAssignment.getLimitValues();
    
    Assignment newAssignment
    	= pSubject2.grant
    			(pSubject0,
    			 oldAssignment.getScope(),
    			 oldAssignment.getFunction(),
    			 oldLimitValues,
    			 false, // canUse
    			 true, // canGrant
           Constants.TODAY,  // effective immediately
           null);       // no expiration date
    
    assertNotNull(newAssignment);
  }
  
  public final void testGrantProxy()
  throws
    ObjectNotFoundException,
    SignetAuthorityException
  {
    // We'll attempt to have subject 2 grant a proxy to subject 0.
    
    Subject subject0 = signet.getSubject(
        Signet.DEFAULT_SUBJECT_TYPE_ID, Common.makeSubjectId(0));
    Subject subject2 = signet.getSubject(
        Signet.DEFAULT_SUBJECT_TYPE_ID, Common.makeSubjectId(2));
    
    PrivilegedSubject pSubject0 = signet.getPrivilegedSubject(subject0);
    PrivilegedSubject pSubject2 = signet.getPrivilegedSubject(subject2);
    
    Subsystem subsystem0 = signet.getSubsystem(Constants.SUBSYSTEM_ID);
    
    Proxy newProxy
      = pSubject2.grantProxy
          (pSubject0,
           subsystem0,
           Constants.PROXY_CANUSE,
           Constants.PROXY_CANEXTEND,
           Constants.YESTERDAY,
           null);       // no expiration date
    
    assertNotNull(newProxy);
  }
  
  public final void testGetProxiesGranted()
  throws
    ObjectNotFoundException
  {
    for (int i = 0; i < Constants.MAX_SUBJECTS; i++)
    {
      // Each Subject has granted a Proxy to the next Subject, except that the
      // last Subject has granted a Proxy to the first Subject.
      int grantorNumber = i;
      int granteeNumber = (i == (Constants.MAX_SUBJECTS-1) ? 0 : i+1);
      
      PrivilegedSubject grantor
        = signet.getPrivilegedSubject
            (signet.getSubject
              (Signet.DEFAULT_SUBJECT_TYPE_ID,
               Common.makeSubjectId(grantorNumber)));
      
      PrivilegedSubject grantee
        = signet.getPrivilegedSubject
            (signet.getSubject
              (Signet.DEFAULT_SUBJECT_TYPE_ID,
               Common.makeSubjectId(granteeNumber)));
      
      Set proxiesGranted = grantor.getProxiesGranted();
      proxiesGranted = Common.filterProxies(proxiesGranted, Status.ACTIVE);
      assertEquals(1, proxiesGranted.size());
      
      Proxy proxyGranted = (Proxy)(Common.getSingleSetMember(proxiesGranted));
      assertEquals(Status.ACTIVE, proxyGranted.getStatus());
      assertEquals(Constants.YESTERDAY, proxyGranted.getEffectiveDate());
      assertEquals(Constants.TOMORROW, proxyGranted.getExpirationDate());
      assertEquals(grantor, proxyGranted.getGrantor());
      assertEquals(grantee, proxyGranted.getGrantee());
      assertNull(proxyGranted.getRevoker());
    }
  }
  
  public final void testGetProxiesReceived()
  throws
    ObjectNotFoundException
  {
    for (int i = 0; i < Constants.MAX_SUBJECTS; i++)
    {
      // Each Subject has granted a Proxy to the next Subject, except that the
      // last Subject has granted a Proxy to the first Subject.
      int grantorNumber = i;
      int granteeNumber = (i == (Constants.MAX_SUBJECTS-1) ? 0 : i+1);
      
      PrivilegedSubject grantor
        = signet.getPrivilegedSubject
            (signet.getSubject
              (Signet.DEFAULT_SUBJECT_TYPE_ID,
               Common.makeSubjectId(grantorNumber)));
      
      PrivilegedSubject grantee
        = signet.getPrivilegedSubject
            (signet.getSubject
              (Signet.DEFAULT_SUBJECT_TYPE_ID,
               Common.makeSubjectId(granteeNumber)));
      
      Set proxiesReceived = grantee.getProxiesReceived();
      proxiesReceived = Common.filterProxies(proxiesReceived, Status.ACTIVE);
      assertEquals(1, proxiesReceived.size());
      
      Proxy proxyReceived = (Proxy)(Common.getSingleSetMember(proxiesReceived));
      assertEquals(Status.ACTIVE, proxyReceived.getStatus());
      assertEquals(Constants.YESTERDAY, proxyReceived.getEffectiveDate());
      assertEquals(Constants.TOMORROW, proxyReceived.getExpirationDate());
      assertEquals(grantor, proxyReceived.getGrantor());
      assertEquals(grantee, proxyReceived.getGrantee());
      assertNull(proxyReceived.getRevoker());
    }
  }

  public final void testGetPrivileges()
  throws
    ObjectNotFoundException
  {
    for (int subjectIndex = 0;
         subjectIndex < Constants.MAX_SUBJECTS;
         subjectIndex++)
    {
      Subject subject
        = signet.getSubject(
            Signet.DEFAULT_SUBJECT_TYPE_ID, Common.makeSubjectId(subjectIndex));
      
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
    }
  }
  
//  private int limitNumber(String limitName)
//  {
//    StringTokenizer tokenizer
//    	= new StringTokenizer(limitName, Constants.DELIMITER);
//    String prefix = tokenizer.nextToken();
//    int number = (new Integer(tokenizer.nextToken())).intValue();
//    return number;
//  }
  
  int expectedLimitValuesCount(int subjectNumber)
  {
    return subjectNumber + 1;
  }
  
  public final void testGrantProxyActingAs()
  throws
    SignetAuthorityException,
    ObjectNotFoundException
  {
    // We'll attempt to have subject 1 grant a Proxy to subject 2, while
    // "acting as" subject 0.
    
    Subject subject0
      = signet.getSubject
          (Signet.DEFAULT_SUBJECT_TYPE_ID,
           Common.makeSubjectId(0));
    Subject subject1
      = signet.getSubject
          (Signet.DEFAULT_SUBJECT_TYPE_ID,
           Common.makeSubjectId(1));
    Subject subject2
      = signet.getSubject
          (Signet.DEFAULT_SUBJECT_TYPE_ID,
           Common.makeSubjectId(2));
    
    PrivilegedSubject pSubject0 = signet.getPrivilegedSubject(subject0);
    PrivilegedSubject pSubject1 = signet.getPrivilegedSubject(subject1);
    PrivilegedSubject pSubject2 = signet.getPrivilegedSubject(subject2);

    Subsystem subsystem = signet.getSubsystem(Constants.SUBSYSTEM_ID);
    
    pSubject1.setActingAs(pSubject0);
    
    Proxy newProxyFrom1to2
      = pSubject1.grantProxy
          (pSubject2,           // grantee
           subsystem,
           false,               // canUse
           true,                // canGrant
           Constants.YESTERDAY, // effective immediately
           null);               // no expiration date
    
    pSubject1.setActingAs(null);
    
    assertNotNull(newProxyFrom1to2);
  }
  
  public final void testReconcile()
  throws
    ObjectNotFoundException,
    SignetAuthorityException
  {
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
    
    Subject subject0
      = signet.getSubject
          (Signet.DEFAULT_SUBJECT_TYPE_ID,
           Common.makeSubjectId(0));
    PrivilegedSubject pSubject0 = signet.getPrivilegedSubject(subject0);
  
    Subject subject1
      = signet.getSubject
          (Signet.DEFAULT_SUBJECT_TYPE_ID,
           Common.makeSubjectId(1));
    PrivilegedSubject pSubject1 = signet.getPrivilegedSubject(subject1);
    
    // Initially, three of those Grantables should be expected to change Status
    // the day after tomorrow.
    Set changedGrantables = pSubject0.reconcile(Constants.DAY_AFTER_TOMORROW);
    assertEquals(3, changedGrantables.size());
    
    // If we run reconcile() again, there should be no more changes.
    changedGrantables = pSubject0.reconcile(Constants.DAY_AFTER_TOMORROW);
    assertEquals(0, changedGrantables.size());
    
    // Let's grant a new Assignment which should become active at some point
    // in the future:
    Tree tree = signet.getTree(Constants.TREE_ID);
    TreeNode treeNode = fixtures.getRoot(tree);
    Subsystem subsystem = signet.getSubsystem(Constants.SUBSYSTEM_ID);
    Function function1
      = subsystem.getFunction(fixtures.makeFunctionId(1));
    Limit[] limitsInDisplayOrder
    = Common.getLimitsInDisplayOrder(function1.getLimits());
  
    Set limitValues = new HashSet();
    for (int i = 0; i < 2; i++)
    {
      Limit limit = limitsInDisplayOrder[i];
      String value = "CHOICE_" + i + "_VALUE";
      LimitValue limitValue = new LimitValue(limit, value);
      limitValues.add(limitValue);
    }

    Assignment assignmentFrom1to0
      = pSubject1.grant
          (pSubject0,
           treeNode,
           function1,
           limitValues,
           true,
           true,
           Constants.TOMORROW,
           Constants.NEXT_WEEK);
    
    changedGrantables = pSubject0.reconcile(Constants.DAY_AFTER_TOMORROW);
    assertEquals(1, changedGrantables.size());
  }
}
