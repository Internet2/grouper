/*
 * $Id: GrouperTest.java,v 1.7 2004-06-02 22:17:06 blair Exp $
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

  /* Start a session as SubjectID "member.system", 1 argument method */
  public void testSessionStartAsMemberSystemOneArgMethod() {
    G.initialize();
    GrouperSession s= new GrouperSession(G);
    try {
      s.start( G.config("member.system") );
    } catch(Exception e) {
      Assert.fail("Exception thrown when starting session");
    }
  }
  
  /* Start a session as SubjectID "member.system", 2 argument method */
  public void testSessionStartAsMemberSystemTwoArgMethod() {
    G.initialize();
    GrouperSession s= new GrouperSession(G);
    try {
      s.start( G.config("member.system"), true );
    } catch(Exception e) {
      Assert.fail("Exception thrown when starting session");
    }
  }
  
  /* Start and end a session as SubjectID "member.system", 1 argument method */
  public void testSessionStartEndAsMemberSystemOneArgMethod() {
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
      Assert.fail("Exception thrown when ending session");
    }
  }
  
  /* Start and end a session as SubjectID "member.system", 2 argument method */
  public void testSessionStartEndAsMemberSystemTwoArgMethod() {
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
      Assert.fail("Exception thrown when ending session");
    }
  }

  /* Attempt to end a session that hasn't been started */
  public void testSessionEndWithoutStart() {
    G.initialize();
    GrouperSession s = new GrouperSession(G);
    try {
      // XXX This may fail if we start throwing exceptions. 
      s.end();
    } catch(Exception e) {
      Assert.fail("Exception thrown when ending unstarted session");
    }
  }

  /*
   * TODO Tests To Write
   * - Session creation as memberID ???
   * - Session creation as subjectID ???
   * - TODO And all the ones I haven't thought of or written down yet
   */

}

