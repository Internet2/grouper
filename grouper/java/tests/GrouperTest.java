/*
 * $Id: GrouperTest.java,v 1.3 2004-05-24 21:36:07 blair Exp $
 */

import edu.internet2.middleware.directory.grouper.*;
import junit.framework.*;

public class GrouperTest extends TestCase {

  public Grouper        G;

  public GrouperTest(String name) {
    super(name);
  }

  public void testSession () {
    G = new Grouper();
    G.initialize();
    G.destroy();
    // XXX Assert?
  }

}

