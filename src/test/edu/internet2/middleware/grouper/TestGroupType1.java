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
 * @version $Id: TestGroupType1.java,v 1.7 2008-11-04 07:17:56 mchyzer Exp $
 */
public class TestGroupType1 extends TestCase {

  /**
   * 
   */
  private static final Log LOG = GrouperUtil.getLog(TestGroupType1.class);

  /**
   * 
   * @param name
   */
  public TestGroupType1(String name) {
    super(name);
  }

  /**
   * 
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  /**
   * 
   * @see junit.framework.TestCase#tearDown()
   */
  protected void tearDown () {
    LOG.debug("tearDown");
  }

  /**
   * 
   */
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
  }
  
}

