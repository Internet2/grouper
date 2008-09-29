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
import java.util.Map;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * Test {@link SubjectFinder.findByIdentifier()} with {@link GrouperSourceAdapter}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestSuFiGrSoAdFindByIdfr.java,v 1.11 2008-09-29 03:38:27 mchyzer Exp $
 */
public class TestSuFiGrSoAdFindByIdfr extends TestCase {

  // Private Class Constants
  private static final Log LOG = GrouperUtil.getLog(TestSuFiGrSoAdFindByIdfr.class);


  // Private Class Variables
  private GrouperSession  s;
  private Stem            edu, root;
  private Group           i2;


  public TestSuFiGrSoAdFindByIdfr(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.internal_resetRegistryAndAddTestSubjects();
    s     = SessionHelper.getRootSession();
    root  = StemHelper.findRootStem(s);
    edu   = StemHelper.addChildStem(root, "edu", "educational");
    i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  // Tests

  public void testFindByIdentifierBadId() {
    LOG.info("testFindByIdentifierBadId");
    SubjectTestHelper.getSubjectByBadId("i do not exist");
    Assert.assertTrue("failed to find bad subject", true);
  } // public void testFindByIdentifierBadId()

  public void testFindByIdentifierGoodIdBadType() {
    LOG.info("testFindByIdentifierGoodIdBadType");
    SubjectTestHelper.getSubjectByBadIdType(i2.getName(), "person");
    Assert.assertTrue("failed to find bad subject", true);
  } // public void testFindByIdentifierGoodIdBadType()

  public void testFindByIdentifierGoodId() {
    LOG.info("testFindByIdentifierGoodId");
    SubjectTestHelper.getSubjectByIdentifier(i2.getName());
    Assert.assertTrue("found subject", true);
  } // public void testFindByIdentifierGoodId()

  public void testFindByIdentifierGoodIdGoodType() {
    LOG.info("testFindByIdentifierGoodIdGoodType");
    Subject subj = SubjectTestHelper.getSubjectByIdentifierType(i2.getName(), "group");
    Assert.assertTrue("found subject", true);
    Map attrs = subj.getAttributes();
    Assert.assertTrue("10 attributes", attrs.size() == 10);
    // createSubjectId
    String attr = "createSubjectId";
    String val  = "GrouperSystem";
    Assert.assertTrue(
      "attr => " + attr, subj.getAttributeValue(attr).equals(val)
    );
    Assert.assertTrue(
      "attrs => " + attr, attrs.get(attr).equals(val)
    );
    // createSubjectType
    attr = "createSubjectType";
    val  = "application";
    Assert.assertTrue(
      "attr => " + attr, subj.getAttributeValue(attr).equals(val)
    );
    Assert.assertTrue(
      "attrs => " + attr, attrs.get(attr).equals(val)
    );
    // createTime
    attr = "createTime";
    Assert.assertNotNull(
      "attr => " + attr, subj.getAttributeValue(attr)
    );
    Assert.assertNotNull(
      "attrs => " + attr, attrs.get(attr)
    );
    // displayExtension
    attr = "displayExtension";
    val  = "internet2";
    Assert.assertTrue(
      "attr => " + attr, subj.getAttributeValue(attr).equals(val)
    );
    Assert.assertTrue(
      "attrs => " + attr, attrs.get(attr).equals(val)
    );
    // displayName
    attr = "displayName";
    val  = "educational:internet2";
    Assert.assertTrue(
      "attr => " + attr, subj.getAttributeValue(attr).equals(val)
    );
    Assert.assertTrue(
      "attrs => " + attr, attrs.get(attr).equals(val)
    );
    // extension
    attr = "extension";
    val  = "i2";
    Assert.assertTrue(
      "attr => " + attr, subj.getAttributeValue(attr).equals(val)
    );
    Assert.assertTrue(
      "attrs => " + attr, attrs.get(attr).equals(val)
    );
    // name
    attr = "name";
    val  = "edu:i2";
    Assert.assertTrue(
      "attr => " + attr, subj.getAttributeValue(attr).equals(val)
    );
    Assert.assertTrue(
      "attrs => " + attr, attrs.get(attr).equals(val)
    );
  } // public void testFindByIdentifierGoodIdGoodType()

}

