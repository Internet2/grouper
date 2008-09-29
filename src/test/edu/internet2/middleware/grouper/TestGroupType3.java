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
import java.util.Set;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author  blair christensen.
 * @version $Id: TestGroupType3.java,v 1.6 2008-09-29 03:38:27 mchyzer Exp $
 */
public class TestGroupType3 extends TestCase {

  // Private Static Class Constants
  private static final Log LOG = GrouperUtil.getLog(TestGroupType3.class);

  public TestGroupType3(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testFindAllAssignableTypesAfterAddition() {
    LOG.info("testFindAllAssignableTypesAfterAddition");
    Set types = GroupTypeFinder.findAllAssignable();    
    T.amount("assignable group types before addition", 0, types.size());
    GrouperSession s = null;
    try {
      String    name  = "test";
      s               = SessionHelper.getRootSession();
      GroupType type  = GroupType.createType(s, name);
      Assert.assertTrue("added type: " + type, true);
      types = GroupTypeFinder.findAllAssignable();
      T.amount("assignable group types after addition", 1, types.size());
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
    finally {
      SessionHelper.stop(s);
    }
  } // public void testFindAllAssignableTypesAfterAddition()

}

