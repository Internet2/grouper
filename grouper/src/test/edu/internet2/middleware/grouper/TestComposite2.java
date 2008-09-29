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
import junit.framework.TestCase;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author  blair christensen.
 * @version $Id: TestComposite2.java,v 1.5 2008-09-29 03:38:27 mchyzer Exp $
 */
public class TestComposite2 extends TestCase {

  // Private Static Class Constants
  private static final Log LOG = GrouperUtil.getLog(TestComposite2.class);

  public TestComposite2(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testCompositeGetType() {
    LOG.info("testCompositeGetType");
    try {
      R       r     = R.populateRegistry(1, 3, 0);
      Group   gA    = r.getGroup("a", "a");
      Group   gB    = r.getGroup("a", "b");
      Group   gC    = r.getGroup("a", "c");
      gA.addCompositeMember(CompositeType.UNION, gB, gC);

      Composite     c     = CompositeFinder.findAsOwner(gA);
      CompositeType type  = c.getType();
      Assert.assertNotNull("type !null", type);
      Assert.assertTrue("type instanceof CompositeType", type instanceof CompositeType);
      Assert.assertEquals("right type", CompositeType.UNION, type);

      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testCompositeGetType()

}

