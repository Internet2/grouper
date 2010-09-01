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

package edu.internet2.middleware.grouper.group;
import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.exception.AttributeNotFoundException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * Test using the wrong field type in various operations.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestWrongFieldType.java,v 1.2 2009-03-24 17:12:08 mchyzer Exp $
 */
public class TestWrongFieldType extends TestCase {

  // Private Class Constants
  private static final Log        LOG   = GrouperUtil.getLog(TestWrongFieldType.class); 


  // Private Class Variables
  private static Stem           edu;
  private static Group          i2;
  private static Stem           root;
  private static GrouperSession s;
  private static Subject        subj0;


  public TestWrongFieldType(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.internal_resetRegistryAndAddTestSubjects();
    GrouperTest.initGroupsAndAttributes();
    s     = SessionHelper.getRootSession();
    root  = StemHelper.findRootStem(s);
    edu   = StemHelper.addChildStem(root, "edu", "education");
    i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    subj0 = SubjectTestHelper.SUBJ0;
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }


  // Tests

  public void testGroupAttrs() {
    LOG.info("testGroupAttrs");
    try {
      i2.setAttribute("members", "members");

      Assert.fail("set list-as-attribute");
    }
    catch (AttributeNotFoundException eANF) {
      Assert.assertTrue(eANF.getMessage(), true);
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  } // public void testGroupAttrs()

  public void testGroupLists() {
    LOG.info("testGroupLists");
    try {
      Field f = FieldFinder.find("name", true);
      i2.addMember(subj0, f);
      Assert.fail("set attribute-as-list");
    }
    catch (SchemaException eS) {
      Assert.assertTrue(eS.getMessage(), true);
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  } // public void testGroupLists()

}

