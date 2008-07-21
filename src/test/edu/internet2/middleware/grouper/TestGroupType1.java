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
import  java.util.*;
import  junit.framework.*;
import  org.apache.commons.logging.*;

import edu.internet2.middleware.grouper.registry.RegistryReset;

/**
 * @author  blair christensen.
 * @version $Id: TestGroupType1.java,v 1.5 2008-07-21 04:43:57 mchyzer Exp $
 */
public class TestGroupType1 extends TestCase {

  // Private Static Class Constants
  private static final Log LOG = LogFactory.getLog(TestGroupType1.class);

  public TestGroupType1(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testFindAllTypesAfterAddition() {
    LOG.info("testFindAllTypesAfterAddition");
    Set types = GroupTypeFinder.findAll();    
    T.amount("public group types before addition", 1, types.size());
    GrouperSession s = null;
    try {
      String    name  = "test";
      s               = SessionHelper.getRootSession();
      GroupType type  = GroupType.createType(s, name);
      Assert.assertTrue("added type: " + type, true);
      types = GroupTypeFinder.findAll();
      T.amount("public group types after addition", 2, types.size());
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
    finally {
      SessionHelper.stop(s);
    }
  } // public void testFindAllTypesAfterAddition()

}

