/*
 * Created on Jan 13, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.internet2.middleware.signet.test;

import java.util.Iterator;
import java.util.Set;

import edu.internet2.middleware.signet.Assignment;
import edu.internet2.middleware.signet.Function;
import edu.internet2.middleware.signet.Limit;
import edu.internet2.middleware.signet.LimitValue;
import edu.internet2.middleware.signet.ObjectNotFoundException;
import edu.internet2.middleware.signet.Permission;
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

  public final void testGetLimitValuesArray()
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
      Set assignmentsReceived
      	= pSubject.getAssignmentsReceived
      			(null, signet.getSubsystem(Constants.SUBSYSTEM_ID));
      
      // Subject 0 is assigned Function 0, which contains Permission 0,
      // which is associated with Limit 0, which has been assigned the
      // value 0. Subject 1 is assigned Function 1, and so forth.
      
      assertEquals(1, assignmentsReceived.size());
      Assignment assignment = (Assignment)(assignmentsReceived.toArray()[0]);

      LimitValue limitValues[] = assignment.getLimitValuesArray();
      assertEquals(1, limitValues.length);
      LimitValue limitValue = limitValues[0];

      assertEquals
      	(signet
      	 	.getSubsystem(Constants.SUBSYSTEM_ID)
      	 		.getLimit(fixtures.makeLimitId(subjectIndex)),
      	 limitValue.getLimit());
      
      assertEquals
      	(fixtures.makeChoiceValue(subjectIndex),
      	 limitValue.getValue());
    }
  }
}
