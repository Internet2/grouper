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
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;
import junit.textui.TestRunner;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.GrouperSourceAdapter;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.helper.T;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;

/**
 * Test {@link SubjectFinder.findByIdentifier()} with {@link GrouperSourceAdapter}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestSubjectFinder.java,v 1.2 2009-03-21 13:35:50 mchyzer Exp $
 */
public class TestSubjectFinder extends GrouperTest {

  // Private Class Constants
  private static final Log LOG = GrouperUtil.getLog(TestSubjectFinder.class);


  // Private Class Variables
  private GrouperSession  s;
  private Stem            edu, root;
  private Group           i2;

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    //TestRunner.run(TestSubjectFinder.class);
    TestRunner.run(new TestSubjectFinder("testSearchGood"));
  }
  
  public TestSubjectFinder(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    super.setUp();
    s     = SessionHelper.getRootSession();
    root  = StemHelper.findRootStem(s);
    edu   = StemHelper.addChildStem(root, "edu", "educational");
    i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testFindByIdentifierGoodIdBadType2() {
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
    Assert.assertEquals("11 attributes", 11, attrs.size());
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

  public void testFindByIdGoodId() {
    LOG.info("testFindByIdGoodId");
    SubjectTestHelper.getSubjectById(i2.getUuid());
    Assert.assertTrue("found subject", true);
  } // public void testFindByIdGoodId()

  public void testFindByIdGoodIdBadType2() {
    LOG.info("testFindByIdGoodIdBadType");
    SubjectTestHelper.getSubjectByBadIdType(i2.getUuid(), "person");
    Assert.assertTrue("failed to find bad subject", true);
  } // public void testFindByIdGoodIdBadType()

  public void testFindByIdGoodIdGoodType() {
    LOG.info("testFindByIdGoodIdGoodType");
    LOG.debug("testFindByIdGoodIdGoodType.0");
    Subject subj = SubjectTestHelper.getSubjectByIdType(i2.getUuid(), "group");
    LOG.debug("testFindByIdGoodIdGoodType.1");
    Assert.assertTrue("found subject", true);
    LOG.debug("testFindByIdGoodIdGoodType.2");
    Map attrs = subj.getAttributes();
    LOG.debug("testFindByIdGoodIdGoodType.3");
    Assert.assertEquals("10 attributes", 11, attrs.size());
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

  // Tests
  
  public void testFindByIdBadId() {
    SubjectTestHelper.getSubjectByBadId("i do not exist");
    Assert.assertTrue("failed to find bad subject", true);
  } // public void testFindByIdBadId()

  // Tests
  
  public void testFindByIdentifierBadId() {
    SubjectTestHelper.getSubjectByBadId("i do not exist");
    Assert.assertTrue("failed to find bad subject", true);
  } // public void testFindByIdentifierBadId()

  public void testFindByIdentifierGoodIdBadType() {
    SubjectTestHelper.getSubjectByBadIdType(SubjectTestHelper.SUBJ_ROOT, "person");
    Assert.assertTrue("failed to find bad subject", true);
  } // public void testFindByIdentifierGoodIdBadType()

  public void testFindByIdentifierGoodIdGoodTypeRoot() {
    SubjectTestHelper.getSubjectByIdentifierType(
      SubjectTestHelper.SUBJ_ROOT, "application"
    );
    Assert.assertTrue("found root subject", true);
  } // public void testFindByIdentifierGoodIdGoodTypeRoot()

  public void testFindByIdentifierGoodIdRoot() {
    SubjectTestHelper.getSubjectByIdentifier(SubjectTestHelper.SUBJ_ROOT);
    Assert.assertTrue("found root subject", true);
  } // public void testFindByIdentifierGoodIdRoot()

  public void testFindByIdGoodIdAll() {
    SubjectTestHelper.getSubjectById(SubjectTestHelper.SUBJ_ALL);
    Assert.assertTrue("found all subject", true);
  } // public void testFindByIdGoodIdAll()

  public void testFindByIdGoodIdBadType() {
    SubjectTestHelper.getSubjectByBadIdType(SubjectTestHelper.SUBJ_ROOT, "person");
    Assert.assertTrue("failed to find bad subject", true);
  } // public void testFindByIdGoodIdBadType()

  public void testFindByIdGoodIdGoodTypeAll() {
    SubjectTestHelper.getSubjectByIdType(
      SubjectTestHelper.SUBJ_ALL, "application"
    );
    Assert.assertTrue("found all subject", true);
  } // public void testFindByIdGoodIdGoodTypeAll()

  public void testFindByIdGoodIdGoodTypeRoot() {
    SubjectTestHelper.getSubjectByIdType(
      SubjectTestHelper.SUBJ_ROOT, "application"
    );
    Assert.assertTrue("found root subject", true);
  } // public void testFindByIdGoodIdGoodTypeRoot()

  public void testFindByIdGoodIdRoot() {
    SubjectTestHelper.getSubjectById(SubjectTestHelper.SUBJ_ROOT);
    Assert.assertTrue("found root subject", true);
  } // public void testFindByIdGoodIdRoot()

  // Tests
  
  public void testSearchBad() {
    LOG.info("testSearchBad");
    Set subjs = SubjectFinder.findAll("i do not exist");
    Assert.assertTrue("subjs == 0", subjs.size() == 0);
  } // public void testSearchBad()

  // Tests
  
  public void testSearchBadSearch() {
    LOG.info("testSearchBadSearch");
    Set subjs = SubjectFinder.findAll("i do not exist");
    Assert.assertTrue("subjs == 0", subjs.size() == 0);
  } // public void testSearchBadSearch()

  public void testSearchGood() {
    LOG.info("testSearchGood");
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemHelper.findRootStem(s);
    Stem            edu   = StemHelper.addChildStem(root, "edu", "educational");
    StemHelper.addChildGroup(edu, "i2", "internet2");
    StemHelper.addChildGroup(edu, "uofc", "uchicago");
    Stem            com   = StemHelper.addChildStem(root, "com", "commercial");
    StemHelper.addChildGroup(com, "dc", "devclue");
    Set             subjs = SubjectFinder.findAll("educational");
    Assert.assertTrue("subjs == 2", subjs.size() == 2);
  } // public void testSearchGood()

  public void testSearchGoodAllId() {
    LOG.info("testSearchGoodAllId");
    Set subjs = SubjectFinder.findAll(SubjectTestHelper.SUBJA.getId());
    Assert.assertTrue("subjs == 1", subjs.size() == 1);
  } // public void testSearchGoodAllId()

  public void testSearchGoodAllIdentifier() {
    LOG.info("testSearchGoodAllIdentifier");
    Set subjs = SubjectFinder.findAll(SubjectTestHelper.SUBJA.getName());
    Assert.assertTrue("subjs == 1", subjs.size() == 1);
  } // public void testSearchGoodAllIdentifier()

  public void testSearchGoodRootId() {
    LOG.info("testSearchGoodRootId");
    Set subjs = SubjectFinder.findAll(SubjectTestHelper.SUBJR.getId());
    Assert.assertTrue("subjs == 1", subjs.size() == 1);
  } // public void testSearchGoodRootId()

  public void testSearchGoodRootIdentifier() {
    LOG.info("testSearchGoodRootIdentifier");
    Set subjs = SubjectFinder.findAll(SubjectTestHelper.SUBJR.getName());
    Assert.assertTrue("subjs == 1", subjs.size() == 1);
  } // public void testSearchGoodRootIdentifier()

  // Tests
  
  public void testFinderBadSubject() {
    SubjectTestHelper.getSubjectByBadId("i do not exist");
    Assert.assertTrue("failed to get bad subject", true);
  } // public void testFinderBadSubject()

  public void testFinderBadSubjectByIdentifier() {
    String id = "i do not exist";
    try { 
      Subject subj = SubjectFinder.findByIdentifier(id, true);
      Assert.fail("found bad subject: " + subj);
    } 
    catch (SubjectNotFoundException e) {
      Assert.assertTrue("failed to find bad subject", true);
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFinderBadSubjectByIdentifier()

  public void testFinderBadSubjectByIdentifierWithBadType() {
    String  id    = "i do not exist";
    String  type  = "person";
    try { 
      Subject subj = SubjectFinder.findByIdentifier(id, type, true);
      Assert.fail("found bad subject: " + subj);
    } 
    catch (SubjectNotFoundException e) {
      Assert.assertTrue("failed to find bad subject", true);
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFinderBadSubjectByIdentifierWithBadType()

  public void testFinderBadSubjectByIdentifierWithGoodType() {
    String  id    = "i do not exist";
    String  type  = "application";
    try { 
      Subject subj = SubjectFinder.findByIdentifier(id, type, true);
      Assert.fail("found bad subject: " + subj);
    } 
    catch (SubjectNotFoundException e) {
      Assert.assertTrue("failed to find bad subject", true);
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFinderBadSubjectByIdentifierWithGoodType()

  public void testFinderBadSubjectWithType() {
    SubjectTestHelper.getSubjectByBadIdType("i do not exist", "person");
    Assert.assertTrue("failed to get bad subject", true);
  } // public void testFinderBadSubjectWithType()

  public void testFinderGrouperSystemSubject() {
    String id = "GrouperSystem";
    try { 
      Subject subj = SubjectFinder.findById(id, true);
      Assert.assertTrue("found subject: " + id, true);
      Assert.assertNotNull("subj !null", subj);
      Assert.assertTrue(
        "subj instanceof Subject",
        subj instanceof Subject
      );
      Assert.assertTrue(
        "subj instanceof InternalSubject",
        subj instanceof InternalSubject
      );
      Assert.assertTrue("subj id", subj.getId().equals(id));
      //Configurable names means we should not assert this any longer
      //Assert.assertTrue("subj name", subj.getName().equals(id));
      Assert.assertTrue(
        "subj type",
        subj.getType().getName().equals("application")
      );
    } 
    catch (SubjectNotFoundException e) {
      Assert.fail("failed to find subject: " + id);
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFinderGrouperSystemSubject()

  public void testFinderGrouperSystemSubjectByIdentifier() {
    String id = "GrouperSystem";
    try { 
      Subject subj = SubjectFinder.findByIdentifier(id, true);
      Assert.assertTrue("found subject: " + id, true);
      Assert.assertNotNull("subj !null", subj);
      Assert.assertTrue(
        "subj instanceof Subject",
        subj instanceof Subject
      );
      Assert.assertTrue(
        "subj instanceof InternalSubject",
        subj instanceof InternalSubject
      );
      Assert.assertTrue("subj id", subj.getId().equals(id));
      //Configurable names means we should no longer make assertion
      //Assert.assertTrue("subj name", subj.getName().equals(id));
      Assert.assertTrue(
        "subj type",
        subj.getType().getName().equals("application")
      );
    } 
    catch (SubjectNotFoundException e) {
      Assert.fail("failed to find subject: " + id);
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFinderGrouperSystemSubjectByIdentifier()

  public void testFinderGrouperSystemSubjectByIdentifierWithBadType() {
    String  id    = "GrouperSystem";
    String  type  = "person";
    try { 
      Subject subj = SubjectFinder.findByIdentifier(id, type, true);
      Assert.fail("found good subject with bad type: " + subj);
    } 
    catch (SubjectNotFoundException e) {
      Assert.assertTrue("failed to find good subject with bad type", true);
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFinderGrouperSystemSubjectByIdentifierWithBadType()

  public void testFinderGrouperSystemSubjectByIdentifierWithGoodType() {
    String  id    = "GrouperSystem";
    String  type  = "application";
    try { 
      Subject subj = SubjectFinder.findByIdentifier(id, type, true);
      Assert.assertTrue("found subject: " + id, true);
      Assert.assertNotNull("subj !null", subj);
      Assert.assertTrue(
        "subj instanceof Subject",
        subj instanceof Subject
      );
      Assert.assertTrue(
        "subj instanceof InternalSubject",
        subj instanceof InternalSubject
      );
      Assert.assertTrue("subj id", subj.getId().equals(id));
      //Configurable names means we should no longer make assertion
      //Assert.assertTrue("subj name", subj.getName().equals(id));
      Assert.assertTrue(
        "subj type",
        subj.getType().getName().equals(type)
      );
    } 
    catch (SubjectNotFoundException e) {
      Assert.fail("failed to find subject: " + id);
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFinderGrouperSystemSubjectByIdentifierWithGoodType()

  public void testFinderGrouperSystemSubjectWithBadType() {
    String  id    = "GrouperSystem";
    String  type  = "person";
    try { 
      Subject subj = SubjectFinder.findById(id, type, true);
      Assert.fail("found good subject with bad type: " + subj);
    } 
    catch (SubjectNotFoundException e) {
      Assert.assertTrue("failed to find good subject with bad type", true);
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFinderGrouperSystemSubjectWithBadType()

  public void testFinderGrouperSystemSubjectWithGoodType() {
    String  id    = "GrouperSystem";
    String  type  = "application";
    try { 
      Subject subj = SubjectFinder.findById(id, type, true);
      Assert.assertTrue("found subject: " + id, true);
      Assert.assertNotNull("subj !null", subj);
      Assert.assertTrue(
        "subj instanceof Subject",
        subj instanceof Subject
      );
      Assert.assertTrue(
        "subj instanceof InternalSubject",
        subj instanceof InternalSubject
      );
      Assert.assertTrue("subj id", subj.getId().equals(id));
      //Configurable names means we should no longer make assertion
      //Assert.assertTrue("subj name", subj.getName().equals(id));
      Assert.assertTrue(
        "subj type",
        subj.getType().getName().equals(type)
      );
    } 
    catch (SubjectNotFoundException e) {
      Assert.fail("failed to find subject: " + id);
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFinderGrouperSystemSubjectWithGoodType()

}

