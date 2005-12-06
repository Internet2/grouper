/*
  Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2005 The University Of Chicago

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
import  java.util.*;
import  junit.framework.*;
import  org.apache.commons.logging.*;


/**
 * Test {@link SubjectFinder.findById()} with {@link GrouperSourceAdapter}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestSuFiGrSoAdFindById.java,v 1.7 2005-12-06 19:42:19 blair Exp $
 */
public class TestSuFiGrSoAdFindById extends TestCase {

  // Private Class Constants
  private static final Log LOG = LogFactory.getLog(TestSuFiGrSoAdFindById.class);


  // Private Class Variables
  private GrouperSession  s;
  private Stem            edu, root;
  private Group           i2;


  public TestSuFiGrSoAdFindById(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    Db.refreshDb();
    s     = SessionHelper.getRootSession();
    root  = StemHelper.findRootStem(s);
    edu   = StemHelper.addChildStem(root, "edu", "educational");
    i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
  }

  protected void tearDown () {
    // Nothing 
    LOG.debug("tearDown");
  }

  // Tests

  public void testFindByIdBadId() {
    LOG.info("testFindByIdBadId");
    SubjectHelper.getSubjectByBadId(Helper.BAD_SUBJ_ID);
    Assert.assertTrue("failed to find bad subject", true);
  } // public void testFindByIdBadId()

  public void testFindByIdGoodIdBadType() {
    LOG.info("testFindByIdGoodIdBadType");
    SubjectHelper.getSubjectByBadIdType(i2.getUuid(), "person");
    Assert.assertTrue("failed to find bad subject", true);
  } // public void testFindByIdGoodIdBadType()

  public void testFindByIdGoodId() {
    LOG.info("testFindByIdGoodId");
    Subject subj = SubjectHelper.getSubjectById(i2.getUuid());
    Assert.assertTrue("found subject", true);
  } // public void testFindByIdGoodId()

  public void testFindByIdGoodIdGoodType() {
    LOG.info("testFindByIdGoodIdGoodType");
    LOG.debug("testFindByIdGoodIdGoodType.0");
    Subject subj = SubjectHelper.getSubjectByIdType(i2.getUuid(), "group");
    LOG.debug("testFindByIdGoodIdGoodType.1");
    Assert.assertTrue("found subject", true);
    LOG.debug("testFindByIdGoodIdGoodType.2");
    Map attrs = subj.getAttributes();
    LOG.debug("testFindByIdGoodIdGoodType.3");
    // TODO Because modify* attrs are erroneously set
    Assert.assertTrue("10 attributes", attrs.size() == 10);
    LOG.debug("testFindByIdGoodIdGoodType.4");
    // createSubjectId
    String attr = "createSubjectId";
    String val  = "GrouperSystem";
    Assert.assertTrue(
      "attr => " + attr, subj.getAttributeValue(attr).equals(val)
    );
    LOG.debug("testFindByIdGoodIdGoodType.5");
    Assert.assertTrue(
      "attrs => " + attr, attrs.get(attr).equals(val)
    );
    LOG.debug("testFindByIdGoodIdGoodType.6");
    // createSubjectType
    attr = "createSubjectType";
    val  = "application";
    Assert.assertTrue(
      "attr => " + attr, subj.getAttributeValue(attr).equals(val)
    );
    LOG.debug("testFindByIdGoodIdGoodType.7");
    Assert.assertTrue(
      "attrs => " + attr, attrs.get(attr).equals(val)
    );
    LOG.debug("testFindByIdGoodIdGoodType.8");
    // createTime
    attr = "createTime";
    Assert.assertNotNull(
      "attr => " + attr, subj.getAttributeValue(attr)
    );
    LOG.debug("testFindByIdGoodIdGoodType.9");
    Assert.assertNotNull(
      "attrs => " + attr, attrs.get(attr)
    );
    LOG.debug("testFindByIdGoodIdGoodType.10");
    // displayExtension
    attr = "displayExtension";
    val  = "internet2";
    Assert.assertTrue(
      "attr => " + attr, subj.getAttributeValue(attr).equals(val)
    );
    LOG.debug("testFindByIdGoodIdGoodType.11");
    Assert.assertTrue(
      "attrs => " + attr, attrs.get(attr).equals(val)
    );
    LOG.debug("testFindByIdGoodIdGoodType.12");
    // displayName
    attr = "displayName";
    val  = "educational:internet2";
    Assert.assertTrue(
      "attr => " + attr, subj.getAttributeValue(attr).equals(val)
    );
    LOG.debug("testFindByIdGoodIdGoodType.13");
    Assert.assertTrue(
      "attrs => " + attr, attrs.get(attr).equals(val)
    );
    LOG.debug("testFindByIdGoodIdGoodType.14");
    // extension
    attr = "extension";
    val  = "i2";
    Assert.assertTrue(
      "attr => " + attr, subj.getAttributeValue(attr).equals(val)
    );
    LOG.debug("testFindByIdGoodIdGoodType.15");
    Assert.assertTrue(
      "attrs => " + attr, attrs.get(attr).equals(val)
    );
    LOG.debug("testFindByIdGoodIdGoodType.16");
    // name
    attr = "name";
    val  = "edu:i2";
    Assert.assertTrue(
      "attr => " + attr, subj.getAttributeValue(attr).equals(val)
    );
    LOG.debug("testFindByIdGoodIdGoodType.17");
    Assert.assertTrue(
      "attrs => " + attr, attrs.get(attr).equals(val)
    );
    LOG.debug("testFindByIdGoodIdGoodType.18");
  } // public void testFindByIdGoodIdGoodType()

}

