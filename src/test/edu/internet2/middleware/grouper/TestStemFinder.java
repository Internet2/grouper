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
import  junit.framework.*;
import  org.apache.commons.logging.*;

/**
 * Test {@link Stem}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestStemFinder.java,v 1.6 2007-01-08 16:43:56 blair Exp $
 */
public class TestStemFinder extends TestCase {

  // Private Class Constants
  private static final Log LOG = LogFactory.getLog(TestStemFinder.class);


  public TestStemFinder(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.internal_resetRegistryAndAddTestSubjects();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }


  // Tests

  public void testFindRootStem() {
    LOG.info("testFindRootStem");
    StemHelper.findRootStem(
      SessionHelper.getRootSession()
    );
  } // public void testFindRootStem()

  public void testFindRootByName() {
    LOG.info("testFindRootByName");
    try {
      GrouperSession  s   = SessionHelper.getRootSession();
      Stem            frs = StemHelper.findRootStem(s);
      Stem            fbn = StemHelper.findByName(s, "");
      Assert.assertTrue("frs == fbn", frs.equals(fbn));
      s.stop();
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  } // public void testFindRootByName()

}

