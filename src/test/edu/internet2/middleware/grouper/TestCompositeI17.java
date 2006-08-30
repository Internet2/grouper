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
 * @version $Id: TestCompositeI17.java,v 1.2 2006-08-30 18:35:38 blair Exp $
 */
public class TestCompositeI17 extends TestCase {

  // Private Static Class Constants
  private static final Log LOG = LogFactory.getLog(TestCompositeI17.class);

  public TestCompositeI17(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testAddUnionWithTwoCompositeChildrenAndNoParents() {
    LOG.info("testAddUnionWithTwoCompositeChildrenAndNoParents");
    try {
      R       r     = R.populateRegistry(1, 7, 2);
      // Feeder Groups
      Subject subjA = r.getSubject("a");
      Group   a     = r.getGroup("a", "a");
      a.addMember(subjA);                                       // subjA
      Subject subjB = r.getSubject("b");
      Group   b     = r.getGroup("a", "b");
      b.addMember(subjB);                                       // subjB
      Group   c     = r.getGroup("a", "c");
      c.addMember(subjA);                                       // subjA
      Group   d     = r.getGroup("a", "d");
      d.addMember(subjB);                                       // subjB
      // Feeder Composite Groups
      Group   e     = r.getGroup("a", "e");
      e.addCompositeMember(CompositeType.INTERSECTION, a, b);   // -
      Group   f     = r.getGroup("a", "f");
      f.addCompositeMember(CompositeType.INTERSECTION, c, d);   // -
      // And our ultimate composite group
      Group   g     = r.getGroup("a", "g");
      g.addCompositeMember(CompositeType.INTERSECTION, e, f);   // -
      // And test
      Assert.assertFalse( "a !hasComposite" , a.hasComposite()  );
      Assert.assertFalse( "b !hasComposite" , b.hasComposite()  );
      Assert.assertFalse( "c !hasComposite" , c.hasComposite()  );
      Assert.assertFalse( "d !hasComposite" , d.hasComposite()  );
      Assert.assertTrue(  "e hasComposite"  , e.hasComposite()  );
      Assert.assertTrue(  "f hasComposite"  , f.hasComposite()  );
      Assert.assertTrue(  "g hasComposite"  , g.hasComposite()  );

      Assert.assertTrue(  "a isComposite"   , a.isComposite()   );
      Assert.assertTrue(  "b isComposite"   , b.isComposite()   );
      Assert.assertTrue(  "c isComposite"   , c.isComposite()   );
      Assert.assertTrue(  "d isComposite"   , d.isComposite()   );
      Assert.assertTrue(  "e isComposite"   , e.isComposite()   );
      Assert.assertTrue(  "f isComposite"   , f.isComposite()   );
      Assert.assertFalse( "g !isComposite"  , g.isComposite()   );

      T.amount( "a members", 1, a.getMembers().size() );
      Assert.assertTrue(  "a has subjA",  a.hasMember(subjA)  );
      T.amount( "b members", 1, b.getMembers().size() );
      Assert.assertTrue(  "b has subjB",  b.hasMember(subjB)  );
      T.amount( "c members", 1, c.getMembers().size() );
      Assert.assertTrue(  "c has subjA",  c.hasMember(subjA)  );
      T.amount( "d members", 1, d.getMembers().size() );
      Assert.assertTrue(  "d has subjB",  d.hasMember(subjB)  );
      T.amount( "e members", 0, e.getMembers().size() );
      T.amount( "f members", 0, f.getMembers().size() );
      T.amount( "g members", 0, g.getMembers().size() );

      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testAddUnionWithTwoCompositeChildrenAndNoParents()

}

