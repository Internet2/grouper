/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

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
import junit.framework.Assert;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author  blair christensen.
 * @version $Id: TestCompositeC9.java,v 1.6 2008-09-29 03:38:27 mchyzer Exp $
 */
public class TestCompositeC9 extends GrouperTest {

  // Private Static Class Constants
  private static final Log LOG = GrouperUtil.getLog(TestCompositeC9.class);

  public TestCompositeC9(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testAddUnionWithNoChildrenAndNoParents() {
    LOG.info("testAddUnionWithNoChildrenAndNoParents");
    try {
      R     r = R.populateRegistry(1, 3, 0);
      Group a = r.getGroup("a", "a");
      Group b = r.getGroup("a", "b");
      Group c = r.getGroup("a", "c");
      a.addCompositeMember(CompositeType.COMPLEMENT, b, c);
      Assert.assertTrue(  "a hasComposite"  , a.hasComposite()  );
      Assert.assertFalse( "b !hasComposite" , b.hasComposite()  );
      Assert.assertFalse( "c !hasComposite" , c.hasComposite()  );
      Assert.assertFalse( "a !isComposite"  , a.isComposite()   );
      Assert.assertTrue(  "b isComposite"   , b.isComposite()   );
      Assert.assertTrue(  "c isComposite"   , c.isComposite()   );
      T.amount("a members", 0, a.getMembers().size());
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testAddUnionWithNoChildrenAndNoParents()

}

