/*--
$Id: PrivilegedSubjectTest.java,v 1.3 2005-04-08 00:47:59 acohen Exp $
$Date: 2005-04-08 00:47:59 $

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
import edu.internet2.middleware.signet.PrivilegedSubject;
import edu.internet2.middleware.signet.Signet;
import edu.internet2.middleware.signet.SignetAuthorityException;
import edu.internet2.middleware.signet.Subsystem;
import edu.internet2.middleware.signet.tree.Tree;
import edu.internet2.middleware.signet.tree.TreeNode;
import edu.internet2.middleware.subject.Subject;
import junit.framework.TestCase;

/**
 * @author Andy Cohen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
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
    Subject subject0 = signet.getSubject(fixtures.makeSubjectId(0));
    PrivilegedSubject pSubject0 = signet.getPrivilegedSubject(subject0);
    
    Subject subject2 = signet.getSubject(fixtures.makeSubjectId(2));
    PrivilegedSubject pSubject2 = signet.getPrivilegedSubject(subject2);
    
    Set assignmentsForSubject2
    	= pSubject2.getAssignmentsReceived(null, null, null);
    
    Assignment assignmentForSubject2
    	= (Assignment)(Common.getSingleSetMember(assignmentsForSubject2));
    
    assertFalse(pSubject0.canEdit(assignmentForSubject2));
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
      	= signet
      			.getSubject(fixtures.makeSubjectId(subjectIndex));
      
      PrivilegedSubject pSubject = signet.getPrivilegedSubject(subject);
      
      // Here's a picture of the Assignments and Limit-values which this test
      // expects to find:
      //
      // Subject 0
      //   TreeNode TREENODE_LEVEL_0_SIBLINGNUMBER_0_ID
      //   Function 0
      //     Permission 0
      //       Limit 0
      //         limit-value: 0
      //  Subject 1
      //    TreeNode TREENODE_LEVEL_0_SIBLINGNUMBER_0_ID
      //    Function 1
      //      Permission 1
      //        Limit 0
      //          limit-value: 0
      //        Limit 1
      //          limit-value: 1
      //  Subject 2
      //    TreeNode TREENODE_LEVEL_0_SIBLINGNUMBER_0_ID
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
  
  public final void testGrant()
  throws
  	SignetAuthorityException,
  	ObjectNotFoundException
  {
    // We'll attempt to have subject 2 grant a privilege to subject 0.
    
    Subject subject0 = signet.getSubject(fixtures.makeSubjectId(0));
    Subject subject2 = signet.getSubject(fixtures.makeSubjectId(2));
    
    PrivilegedSubject pSubject0 = signet.getPrivilegedSubject(subject0);
    PrivilegedSubject pSubject2 = signet.getPrivilegedSubject(subject2);
    
    Assignment oldAssignment
    	= (Assignment)
    			(Common.getSingleSetMember
    			  (pSubject2.getAssignmentsReceived
    			    (null, null, null)));
    Set oldLimitValues = new HashSet();
    for (int i = 0; i < oldAssignment.getLimitValuesArray().length; i++)
    {
      oldLimitValues.add(oldAssignment.getLimitValuesArray()[i]);
    }
    
    Assignment newAssignment
    	= pSubject2.grant
    			(pSubject0,
    			 oldAssignment.getScope(),
    			 oldAssignment.getFunction(),
    			 oldLimitValues,
    			 false,
    			 false);
    
    assertNotNull(newAssignment);
  }
  
  private int limitNumber(String limitName)
  {
    StringTokenizer tokenizer
    	= new StringTokenizer(limitName, Constants.DELIMITER);
    String prefix = tokenizer.nextToken();
    int number = (new Integer(tokenizer.nextToken())).intValue();
    return number;
  }
  
  int expectedLimitValuesCount(int subjectNumber)
  {
    return subjectNumber + 1;
  }
}
