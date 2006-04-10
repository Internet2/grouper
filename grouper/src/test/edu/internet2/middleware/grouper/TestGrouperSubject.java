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

package edu.internet2.middleware.grouper;


import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  java.io.*;
import  java.util.*;
import  junit.framework.*;
import  org.apache.commons.logging.*;


/**
 * Test {@link GrouperSubject} class.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestGrouperSubject.java,v 1.1.2.1 2006-04-10 19:07:20 blair Exp $
 */
public class TestGrouperSubject extends TestCase {

  // Private Class Constants
  private static final Log LOG = LogFactory.getLog(TestGrouperSubject.class);


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
    RegistryReset.resetRegistryAndAddTestSubjects();
    s         = SessionHelper.getRootSession();
    root      = StemHelper.findRootStem(s);
    edu       = StemHelper.addChildStem(root, "edu", "education");
    i2        = StemHelper.addChildGroup(edu, "i2", "internet2");
    uofc      = StemHelper.addChildGroup(edu, "uofc", "uchicago");
    GroupHelper.setAttr(uofc, "description", "a description");
    subjI2    = SubjectTestHelper.getSubjectById(i2.getUuid());
    subjUofc  = SubjectTestHelper.getSubjectById(uofc.getUuid());
    GroupHelper.addMember(i2, subjUofc, "members");
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  // Tests

  public void testGetDescription() {
    LOG.info("testGetDescription");
    Assert.assertTrue(
      "i2 has empty description", subjI2.getDescription().equals("")
    );
    Assert.assertTrue(
      "uofc has set description", subjUofc.getDescription().equals("a description")
    );
  } // public void testGetDescription()

  public void testGetAttributeValues() {
    LOG.info("testGetAttributeValues");
    Assert.assertTrue(
      "i2 has no multivalued attributes",
      subjI2.getAttributeValues("members").size() == 0
    );
    Assert.assertTrue(
      "uofc has no multivalued attributes",
      subjUofc.getAttributeValues("members").size() == 0
    );
  } // public void testGetAttributeValues()

}

