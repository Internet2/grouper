/*
 * $Id: GrouperTest.java,v 1.6 2004-06-02 22:02:49 blair Exp $
 */

import edu.internet2.middleware.directory.grouper.*;
import java.lang.reflect.*;
import junit.framework.*;

public class GrouperTest extends TestCase {

  public Grouper G;

  public GrouperTest(String name) {
    super(name);
  }

  protected void setUp () {
    // Establish a new Grouper instance
    G = new Grouper();
  }

  protected void tearDown () {
    // Destroy our Grouper instance
    G.destroy();
  }

  /* Instantiate a Grouper instance */
  public void testGrouperInstantiate() {
    Class  klass    = G.getClass();
    String expKlass = "edu.internet2.middleware.directory.grouper.Grouper";

    Assert.assertNotNull(G);
    Assert.assertTrue( expKlass.equals( klass.getName() ) );
  }

  /* Initialize Grouper environment */
  public void testGrouperInitialize() {
    try {
      G.initialize();
    } catch(Exception e) {
      Assert.fail("Exception thrown when initializing Grouper");
    }
  }

  /* Get a runtime configuration setting */
  public void testGetRuntimeConfigSetting() {
    G.initialize();
    
    String expVal = "GrouperSystem";
    Assert.assertTrue( expVal.equals( G.config("member.system") ) );
  }

  /* Instantiate a Grouper session */
  public void testSessionInstantiate() {
    G.initialize();
    GrouperSession s = new GrouperSession(G);

    Class klass     = s.getClass();
    String expKlass = "edu.internet2.middleware.directory.grouper.GrouperSession";

    Assert.assertNotNull(s);
    Assert.assertTrue( expKlass.equals( klass.getName() ) );
  }

  /* Create a session as SubjectID "member.system", 1 argument method */
  public void testSessionCreateAsMemberSystemOneArgMethod() {
    G.initialize();
    GrouperSession s= new GrouperSession(G);
    try {
      s.start( G.config("member.system") );
    } catch(Exception e) {
      Assert.fail("Exception thrown when starting session");
    }
  }
  
  /* Create a session as SubjectID "member.system", 2 argument method */
  public void testSessionCreateAsMemberSystemTwoArgMethod() {
    G.initialize();
    GrouperSession s= new GrouperSession(G);
    try {
      s.start( G.config("member.system"), true );
    } catch(Exception e) {
      Assert.fail("Exception thrown when starting session");
    }
  }
  
  /* Create and end a session as SubjectID "member.system", 1 argument method */
  public void testSessionCreateEndAsMemberSystemOneArgMethod() {
    G.initialize();
    GrouperSession s= new GrouperSession(G);
    try {
      s.start( G.config("member.system") );
    } catch(Exception e) {
      Assert.fail("Exception thrown when starting session");
    }
    try {
      s.end();
    } catch(Exception e) {
      Assert.fail("Exception thrown when ending sessin");
    }
  }
  
  /* Create and end a session as SubjectID "member.system", 2 argument method */
  public void testSessionCreateEndAsMemberSystemTwoArgMethod() {
    G.initialize();
    GrouperSession s= new GrouperSession(G);
    try {
      s.start( G.config("member.system"), true );
    } catch(Exception e) {
      Assert.fail("Exception thrown when starting session");
    }
    try {
      s.end();
    } catch(Exception e) {
      Assert.fail("Exception thrown when ending sessin");
    }
  }
  
  /*
   * TODO Tests To Write
   * - Ending a session that hasn't been started
   * - Session creation as memberID ???
   * - Session creation as subjectID ???
   * - TODO And all the ones I haven't thought of or written down yet
   */

}

