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
import junit.textui.TestRunner;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author  blair christensen.
 * @version $Id: TestGroupType8.java,v 1.7 2009-03-15 06:37:22 mchyzer Exp $
 */
public class TestGroupType8 extends GrouperTest {

  // Private Static Class Constants
  private static final Log LOG = GrouperUtil.getLog(TestGroupType8.class);

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new TestGroupType8("testFailToDeleteWhenInUse"));
  }
  
  public TestGroupType8(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testFailToDeleteWhenInUse() {
    LOG.info("testFailToDeleteWhenInUse");
    try {
      R               r       = R.populateRegistry(1, 1, 0);
      GroupType       custom  = GroupType.createType(r.rs, "custom");
      Group           gA      = r.getGroup("a", "a");
      gA.addType(custom);
      try {
        custom.delete(r.rs);
        Assert.fail("deleted in use type");
      }
      catch (SchemaException eS) {
        assertContains(
          "OK: failed to delete in use type",
          eS.getMessage(), 
          E.GROUPTYPE_DELINUSE
        );
      } 
      finally {
        r.rs.stop();
      }
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailToDeleteWhenInUse()

}

