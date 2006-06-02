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
import  edu.internet2.middleware.subject.provider.*;
import  java.util.*;
import  junit.framework.*;
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: TestCompositeC25.java,v 1.1 2006-06-02 18:23:46 blair Exp $
 */
public class TestCompositeC25 extends TestCase {

  // Private Static Class Constants
  private static final Log LOG = LogFactory.getLog(TestCompositeC25.class);

  public TestCompositeC25(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testAddUnionWithOneChildAndCompositeParent() {
    LOG.info("testAddUnionWithOneChildAndCompositeParent");
    try {
      R       r     = R.populateRegistry(1, 5, 1);
      // Feeder Groups
      Group   a     = r.getGroup("a", "a");
      Subject subjA = r.getSubject("a");
      a.addMember(subjA);
      Group   b     = r.getGroup("a", "b");
      // Composite Group
      Group   c     = r.getGroup("a", "c");
      c.addCompositeMember(CompositeType.COMPLEMENT, a, b); // subjA - 
      // Parent Feeder Group
      Group   d     = r.getGroup("a", "d");
      // Parent Group
      Group   e     = r.getGroup("a", "e");
      e.addCompositeMember(CompositeType.COMPLEMENT, c, d); // subjA - 

      // And test
      Assert.assertFalse( "a !hasComposite" , a.hasComposite()  );
      Assert.assertFalse( "b !hasComposite" , b.hasComposite()  );
      Assert.assertTrue(  "c hasComposite"  , c.hasComposite()  );
      Assert.assertFalse( "d !hasComposite" , d.hasComposite()  );
      Assert.assertTrue(  "e hasComposite"  , e.hasComposite()  );

      Assert.assertTrue(  "a isComposite"   , a.isComposite()   );
      Assert.assertTrue(  "b isComposite"   , b.isComposite()   );
      Assert.assertTrue(  "c isComposite"   , c.isComposite()   );
      Assert.assertTrue(  "d isComposite"   , d.isComposite()   );
      Assert.assertFalse( "e !isComposite"  , e.isComposite()   );

      T.amount( "a members", 1, a.getMembers().size() );
      Assert.assertTrue("a has subjA", a.hasMember(subjA));

      T.amount( "b members", 0, b.getMembers().size() );

      T.amount( "c members", 1, c.getMembers().size() );
      Assert.assertTrue("c has subjA", c.hasMember(subjA));

      T.amount( "d members", 0, d.getMembers().size() );

      T.amount( "e members", 1, e.getMembers().size() );
      Assert.assertTrue("e has subjA", e.hasMember(subjA));

      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testAddUnionWithOneChildAndCompositeParent()

}

