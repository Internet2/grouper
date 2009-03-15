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
import junit.textui.TestRunner;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author  blair christensen.
 * @version $Id: TestGroupType7.java,v 1.6 2009-03-15 06:37:22 mchyzer Exp $
 */
public class TestGroupType7 extends TestCase {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new TestGroupType7("testFailToDeleteWhenSystemType"));
  }
  
  // Private Static Class Constants
  private static final Log LOG = GrouperUtil.getLog(TestGroupType7.class);

  public TestGroupType7(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testFailToDeleteWhenSystemType() {
    LOG.info("testFailToDeleteWhenSystemType");
    try {
      R               r       = R.populateRegistry(0, 0, 0);
      GroupType       base    = GroupTypeFinder.find("base", true);
      try {
        base.delete(r.rs);
        Assert.fail("deleted system type");
      }
      catch (SchemaException eS) {
        assertTrue(GrouperUtil.getFullStackTrace(eS), eS.getMessage().contains(E.GROUPTYPE_NODELSYS));
      } 
      finally {
        r.rs.stop();
      }
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailToDeleteWhenSystemType()

}

