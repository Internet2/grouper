/*
 * $Id: GrouperTest.java,v 1.4 2004-06-02 16:03:11 blair Exp $
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

/*
  XXX Running G.destroy() is causing null pointer exceptions.  At
      the moment I'm not sure if that is due to a bug in my code
      (probably), me misundering how JUnit works (also quite
      probable), or me misunderstanding Java (also quite possible).
  protected void tearDown () {
    // Destroy our Grouper instance
    G.destroy();
  }
*/

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

  /* Instantiate a Grouper session */
  public void testSessionInstantiate() {
    G.initialize();
    GrouperSession s = new GrouperSession(G);

    Class klass     = s.getClass();
    String expKlass = "edu.internet2.middleware.directory.grouper.GrouperSession";

    Assert.assertNotNull(s);
    Assert.assertTrue( expKlass.equals( klass.getName() ) );
  }

  /* Create a grouper session as 'GrouperSystem' */
  /* XXX Or should it be as whomever is listed in the cf file? */
  
  /*
   * TODO Tests To Write
   * - Session creation as 'member.system'
   * - Session creation/destruction as 'member.system' 
   * - Ending a session that hasn't been started
   * - Session double destruction
   * - Session creation as memberID ???
   * - Session creation as subjectID ???
   * - TODO And all the ones I haven't thought of or written down yet
   */

}

