/**
 * Copyright 2014 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

package edu.internet2.middleware.grouper.subj;
import junit.framework.Assert;
import junit.textui.TestRunner;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.helper.GroupHelper;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * Test {@link GrouperSubject} class.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestGrouperSubject.java,v 1.2 2009-09-02 05:57:26 mchyzer Exp $
 */
public class TestGrouperSubject extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(TestGrouperSubject.class);
  }
  
  // Private Class Constants
  private static final Log LOG = GrouperUtil.getLog(TestGrouperSubject.class);


  // Private Class Variables
  private static Stem           edu;
  private static Group          i2;
  private static Stem           root;
  private static GrouperSession s;
  private static Subject        subjI2;
  private static Subject        subjUofc;
  private static Group          uofc;


  public TestGrouperSubject(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    super.setUp();
    RegistryReset.internal_resetRegistryAndAddTestSubjects();
    GrouperTest.initGroupsAndAttributes();

    s         = SessionHelper.getRootSession();
    root      = StemHelper.findRootStem(s);
    edu       = StemHelper.addChildStem(root, "edu", "education");
    i2        = StemHelper.addChildGroup(edu, "i2", "internet2");
    uofc      = StemHelper.addChildGroup(edu, "uofc", "uchicago");
    uofc.setDescription("a description");
    uofc.store();
    subjI2    = SubjectTestHelper.getSubjectById(i2.getUuid());
    subjUofc  = SubjectTestHelper.getSubjectById(uofc.getUuid());
    GroupHelper.addMember(i2, subjUofc, "members");
  }

  protected void tearDown () {
    LOG.debug("tearDown");
    super.tearDown();
  }

  // Tests

  public void testGetDescription() {
    LOG.info("testGetDescription");
    Assert.assertTrue(
      "i2 has empty description", subjI2.getDescription().equals("")
    );
    Assert.assertEquals(
      "uofc has set description", "a description", subjUofc.getDescription());
  } // public void testGetDescription()

  public void testGetAttributeValues() {
    LOG.info("testGetAttributeValues");
    Assert.assertTrue(
      "i2 has no multivalued attributes",
      subjI2.getAttributeValues("members") == null
    );
    Assert.assertTrue(
      "uofc has no multivalued attributes",
      subjUofc.getAttributeValues("members") == null
    );
  } // public void testGetAttributeValues()

}

