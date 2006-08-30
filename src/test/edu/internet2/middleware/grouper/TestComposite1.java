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
import  junit.framework.*;
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: TestComposite1.java,v 1.2 2006-08-30 18:35:38 blair Exp $
 */
public class TestComposite1 extends TestCase {

  // Private Static Class Constants
  private static final Log LOG = LogFactory.getLog(TestComposite1.class);

  public TestComposite1(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testCompositeFinderFindAsOwner() {
    LOG.info("testCompositeFinderFindAsOwner");
    try {
      R       r     = R.populateRegistry(1, 4, 0);
      Group   gA    = r.getGroup("a", "a");
      Group   gB    = r.getGroup("a", "b");
      Group   gC    = r.getGroup("a", "c");
      gA.addCompositeMember(CompositeType.UNION, gB, gC);
      Group   gD    = r.getGroup("a", "d");
      gD.addCompositeMember(CompositeType.COMPLEMENT, gA, gB);

      // gA
      try {
        Composite c = CompositeFinder.findAsOwner(gA);
        Assert.assertNotNull("c !null for gA", c);
      }
      catch (CompositeNotFoundException eCNF) {
        Assert.fail("FAIL: did not find composite for gA");
      }
      // gB
      try {
        CompositeFinder.findAsOwner(gB);
        Assert.fail("FAIL: found composite for gB");
      }
      catch (CompositeNotFoundException eCNF) {
        Assert.assertTrue("OK: did not find composite for gB", true);
      }
      // gC
      try {
        Composite c = CompositeFinder.findAsOwner(gC);
        Assert.fail("FAIL: found composite for gC");
      }
      catch (CompositeNotFoundException eCNF) {
        Assert.assertTrue("OK: did not find composite for gC", true);
      }
      // gD
      try {
        Composite c = CompositeFinder.findAsOwner(gD);
        Assert.assertNotNull("c !null for gD", c);
      }
      catch (CompositeNotFoundException eCNF) {
        Assert.fail("FAIL: did not find composite for gD");
      }
    
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testCompositeFinderFindAsOwner()

}

