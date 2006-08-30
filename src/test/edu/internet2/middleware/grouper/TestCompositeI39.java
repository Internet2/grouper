/*
  Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2006 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper;
import  edu.internet2.middleware.subject.*;
import  junit.framework.*;
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: TestCompositeI39.java,v 1.2 2006-08-30 18:35:38 blair Exp $
 */
public class TestCompositeI39 extends TestCase {

  // Private Static Class Constants
  private static final Log LOG = LogFactory.getLog(TestCompositeI39.class);

  public TestCompositeI39(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testAddMemberToChildOfComposite() {
    LOG.info("testAddMemberToChildOfComposite");
    try {
      R       r     = R.populateRegistry(1, 3, 2);
      Group   a     = r.getGroup("a", "a");
      Group   b     = r.getGroup("a", "b");
      Group   c     = r.getGroup("a", "c");
      Subject subjA = r.getSubject("a");
      Subject subjB = r.getSubject("b");
  
      a.addMember(subjA);
      b.addMember(subjB);
      c.addCompositeMember(CompositeType.INTERSECTION, a, b);
      a.addMember(subjB);
  
      Assert.assertTrue(  "a isComposite"   , a.isComposite()   );
      Assert.assertFalse( "a !hasComposite" , a.hasComposite()  );
      T.amount("a members", 2, a.getMembers().size());
      Assert.assertTrue("a has subjA", a.hasMember(subjA));
      Assert.assertTrue("a has subjB", a.hasMember(subjB));

      Assert.assertTrue(  "b isComposite"   , b.isComposite()   );
      Assert.assertFalse( "b !hasComposite" , b.hasComposite()  );
      T.amount("b members", 1, b.getMembers().size());
      Assert.assertTrue("b has subjB", b.hasMember(subjB));

      Assert.assertFalse( "c !isComposite"  , c.isComposite()   );
      Assert.assertTrue(  "c hasComposite"  , c.hasComposite()  );
      T.amount("c members", 1, c.getMembers().size());
      Assert.assertTrue("c has subjB", c.hasMember(subjB));

      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testAddMemberToChildOfComposite()

}

