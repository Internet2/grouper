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
import  edu.internet2.middleware.subject.*;
import  junit.framework.*;
import  org.apache.commons.logging.*;

/**
 * Test using the wrong field type in various operations.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestWrongFieldType.java,v 1.7 2007-08-24 14:18:16 blair Exp $
 */
public class TestWrongFieldType extends TestCase {

  // Private Class Constants
  private static final Log        LOG   = LogFactory.getLog(TestWrongFieldType.class); 


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
      Field f = FieldFinder.find("name");
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

