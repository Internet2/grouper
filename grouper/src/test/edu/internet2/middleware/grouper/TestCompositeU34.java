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
 * @version $Id: TestCompositeU34.java,v 1.1.2.1 2006-05-22 18:59:20 blair Exp $
 */
public class TestCompositeU34 extends TestCase {

  // Private Static Class Constants
  private static final Log LOG = LogFactory.getLog(TestCompositeU34.class);

  public TestCompositeU34(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testDelUnionWithChildAndCompositeChildAndParent() {
    LOG.info("testDelUnionWithChildAndCompositeChildAndParent");
    try {
      R       r     = R.populateRegistry(1, 7, 3);
      Group   a     = r.getGroup("a", "a");
      Group   b     = r.getGroup("a", "b");
      Group   c     = r.getGroup("a", "c");
      Group   d     = r.getGroup("a", "d");
      Group   e     = r.getGroup("a", "e");
      Subject eSubj = e.toSubject();
      Group   f     = r.getGroup("a", "f");
      Subject subjA = r.getSubject("a");
      Subject subjB = r.getSubject("b");
      Subject subjC = r.getSubject("c");
  
      a.addMember(subjA);
      b.addMember(subjB);
      c.addMember(subjC);
      d.addCompositeMember(CompositeType.UNION, a, b);
      e.addCompositeMember(CompositeType.UNION, c, d);
      f.addMember(eSubj);
      e.deleteCompositeMember();

      Assert.assertTrue(  "a isComposite"   , a.isComposite()   );
      Assert.assertFalse( "a !hasComposite" , a.hasComposite()  );
      T.amount("a members", 1, a.getMembers().size());
      Assert.assertTrue("a has subjA", a.hasMember(subjA));

      Assert.assertTrue(  "b isComposite"   , b.isComposite()   );
      Assert.assertFalse( "b !hasComposite" , b.hasComposite()  );
      T.amount("b members", 1, b.getMembers().size());
      Assert.assertTrue("b has subjB", b.hasMember(subjB));

      Assert.assertFalse( "c !isComposite"  , c.isComposite()   );
      Assert.assertFalse( "c !hasComposite" , c.hasComposite()  );
      T.amount("c members", 1, c.getMembers().size());
      Assert.assertTrue("c has subjC", c.hasMember(subjC));

      Assert.assertFalse( "d !isComposite"  , d.isComposite()   );
      Assert.assertTrue(  "d hasComposite"  , d.hasComposite()  );
      T.amount("d members", 2, d.getMembers().size());
      Assert.assertTrue("d has subjA", d.hasMember(subjA));
      Assert.assertTrue("d has subjB", d.hasMember(subjB));

      Assert.assertFalse( "e !isComposite"  , e.isComposite()   );
      Assert.assertFalse( "e !hasComposite" , e.hasComposite()  );
      T.amount("e members", 0, e.getMembers().size());

      Assert.assertFalse( "f !isComposite"  , f.isComposite()   );
      Assert.assertFalse( "e !hasComposite" , f.hasComposite()  );
      T.amount("f members", 1, f.getMembers().size());
      Assert.assertTrue("f has eSubj", f.hasMember(eSubj));

      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testDelUnionWithChildAndCompositeChildAndParent()

}

