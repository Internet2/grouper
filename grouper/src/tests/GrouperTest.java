/*
 * $Id: GrouperTest.java,v 1.2 2004-03-25 01:40:37 blair Exp $
 */

import edu.internet2.middleware.directory.grouper.*;
import junit.framework.*;

public class GrouperTest extends TestCase {

  private boolean rv;
  private int     sessionID;
  public  Grouper g;

  public GrouperTest(String name) {
    super(name);
  }

  protected void setUp () {
    rv        = false;
    sessionID = -1;
    g         = new Grouper();
  }

  public void testInstantiate () {
    assertNotNull(g);
  }

  public void testSession_start () {
    assertNotNull(g);
    sessionID = g.Session_start("TestUser");
    // XXX assertTrue(sessionID.is_int());
    assertTrue(sessionID != -1);
    assertTrue(sessionID >   0);
  }

  public void testSession_end () {
    assertNotNull(g);
    rv = g.Session_end(sessionID);
    assertFalse(rv);
  }

  public void testSession_start_and_end () {
    assertNotNull(g);
    sessionID = g.Session_start("TestUser");
    //assertTrue(sessionID.is_int());
    assertTrue(sessionID != -1);
    assertTrue(sessionID >   0);
    rv = g.Session_end(sessionID);
    assertTrue(rv);
  }
}

