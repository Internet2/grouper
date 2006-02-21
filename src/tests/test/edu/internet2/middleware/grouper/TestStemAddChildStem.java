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

package test.edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  junit.framework.*;
import  org.apache.commons.logging.*;


/**
 * Test {@link Stem.addChildStem()}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestStemAddChildStem.java,v 1.8 2006-02-21 17:11:33 blair Exp $
 */
public class TestStemAddChildStem extends TestCase {

  // Private Class Constants
  private static final Log LOG = LogFactory.getLog(TestStemAddChildStem.class);

  public TestStemAddChildStem(String name) {
    super(name);
  }

  protected void setUp () {
    RegistryReset.resetRegistryAndAddTestSubjects();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
    GrouperSession.waitForAllTx();
  }

  // Tests

  public void testAddChildStemAtRoot() {
    Stem  root  = StemHelper.findRootStem(
      SessionHelper.getRootSession()
    );
    Stem  edu   = StemHelper.addChildStem(root, "edu", "education");
  } // public void testAddChildStemAtRoot()

  public void testAddChildStem() {
    Stem  root  = StemHelper.findRootStem(
      SessionHelper.getRootSession()
    );
    Stem  edu   = StemHelper.addChildStem(root, "edu", "education");
    Stem  uofc  = StemHelper.addChildStem(edu, "uofc", "uchicago");
  } // public void testAddChildStem()

  public void testAddDuplicateChildStem() {
    Stem  root  = StemHelper.findRootStem(
      SessionHelper.getRootSession()
    );
    Stem  edu   = StemHelper.addChildStem(root, "edu", "education");
    Stem  uofc  = StemHelper.addChildStem(edu, "uofc", "uchicago");
    StemHelper.addChildStemFail(edu, "uofc", "uchicago");
  } // public void testAddDuplicateChildStem()

}

