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
 * Test {@link Stem.addChildGroup()}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestStemAddChildGroup.java,v 1.9 2006-02-21 17:11:33 blair Exp $
 */
public class TestStemAddChildGroup extends TestCase {

  // Private Class Constants
  private static final Log LOG = LogFactory.getLog(TestStemAddChildGroup.class);

  public TestStemAddChildGroup(String name) {
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

  public void testAddChildGroupAtRootFail() {
    Stem  root  = StemHelper.findRootStem(
      SessionHelper.getRootSession()
    );
    StemHelper.addChildGroupFail(root, "i2", "internet2");
  } // public void testAddChildGroupAtRootFail()

  public void testAddChildGroup() {
    Stem  root  = StemHelper.findRootStem(
      SessionHelper.getRootSession()
    );
    Stem  edu   = StemHelper.addChildStem(root, "edu", "education");
    Group i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
  } // public void testAddChildGroup()

  public void testAddDuplicateChildGroup() {
    Stem  root  = StemHelper.findRootStem(
      SessionHelper.getRootSession()
    );
    Stem  edu   = StemHelper.addChildStem(root, "edu", "education");
    Group i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    StemHelper.addChildGroupFail(edu, "i2", "internet2");
  } // public void testAddDuplicateChildGroup()

}

