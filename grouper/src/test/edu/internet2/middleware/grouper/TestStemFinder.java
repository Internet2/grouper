/*******************************************************************************
 * Copyright 2012 Internet2
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
 ******************************************************************************/
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
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import junit.textui.TestRunner;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.R;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Test {@link Stem}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestStemFinder.java,v 1.9 2009-03-20 19:56:40 mchyzer Exp $
 */
public class TestStemFinder extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new TestStemFinder("testFindByAttributeDefName"));
  }
  
  // Private Class Constants
  private static final Log LOG = GrouperUtil.getLog(TestStemFinder.class);


  public TestStemFinder(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.internal_resetRegistryAndAddTestSubjects();
    GrouperTest.initGroupsAndAttributes();

  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  /**
   * 
   */
  public void testFindByIdIndex() {
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemHelper.findRootStem(s);
    Stem            edu   = StemHelper.addChildStem(root, "edu", "educational");

    Stem found = StemFinder.findByIdIndex(edu.getIdIndex(), true, null);
    
    assertEquals(found.getName(), edu.getName());
    
    found = StemFinder.findByIdIndex(12345656L, false, null);
    
    assertNull(found);
    
    try {
      StemFinder.findByIdIndex(12345678L, true, null);
      fail("shouldnt get here");
    } catch (StemNotFoundException gnfe) {
      //good
    }
    
    
  } // public void testFindByIdIndex()


  // Tests

  /**
   * 
   */
  public void testFindByName() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignName("stem").assignCreateParentStemsIfNotExist(true).save();
    stem.addAlternateName("alternate");
    stem.store();

    Stem stem1 = new StemSave(grouperSession).assignName("stem1").assignCreateParentStemsIfNotExist(true).save();
    stem1.addAlternateName("alternate1");
    stem1.store();

    assertNotNull(StemFinder.findByName(grouperSession, "stem", false));
    assertNotNull(StemFinder.findByName(grouperSession, "alternate", false));
    
    Stem foundStem = new StemFinder().assignFindByUuidOrName(true).assignScope(stem.getName()).findStem();
    
    assertNotNull(foundStem);
    assertEquals(stem.getName(), foundStem.getName());
    
    
    foundStem = new StemFinder().assignFindByUuidOrName(true).assignScope(stem.getUuid()).findStem();
    
    assertNotNull(foundStem);
    assertEquals(stem.getName(), foundStem.getName());
    
    foundStem = new StemFinder().assignFindByUuidOrName(true).assignScope("alternate").findStem();
    
    assertNotNull(foundStem);
    assertEquals(stem.getName(), foundStem.getName());
    
  }
  
  /**
   * 
   */
  public void testFindByCurrentName() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignName("stem").assignCreateParentStemsIfNotExist(true).save();
    stem.addAlternateName("alternate");
    stem.store();
    
    assertNotNull(StemFinder.findByCurrentName(grouperSession, "stem", false, null));
    assertNull(StemFinder.findByCurrentName(grouperSession, "alternate", false, null));
  }
  
  /**
   * 
   */
  public void testFindByAlternateName() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignName("stem").assignCreateParentStemsIfNotExist(true).save();
    stem.addAlternateName("alternate");
    stem.store();
    
    assertNull(StemFinder.findByAlternateName(grouperSession, "stem", false, null));
    assertNotNull(StemFinder.findByAlternateName(grouperSession, "alternate", false, null));
  }
  
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

  // TESTS //  
  
  public void testFindAllByApproximateDisplayExtension_whenUpperCaseInRegistry() {
    try {
      LOG.info("testFindAllByApproximateDisplayExtension_whenUpperCaseInRegistry");
      R       r     = R.getContext("i2mi");
      Stem    i2mi  = r.getStem("i2mi");
      Stem    child = i2mi.addChildStem("lower", "UPPER");
  
      assertEquals(
        "stems found by displayExtension",
        1, 
        GrouperDAOFactory.getFactory().getStem().findAllByApproximateDisplayExtension( child.getDisplayExtension() ).size()
      );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testFindAllByApproximateDisplayExtension_whenUpperCaseInRegistry

  // TESTS //  
  
  public void testFindAllByApproximateDisplayName_whenUpperCaseInRegistry() {
    try {
      LOG.info("testFindAllByApproximateDisplayName_whenUpperCaseInRegistry");
      R       r     = R.getContext("i2mi");
      Stem    i2mi  = r.getStem("i2mi");
      Stem    child = i2mi.addChildStem("lower", "UPPER");
  
      assertEquals(
        "stems found by displayName",
        1, 
        GrouperDAOFactory.getFactory().getStem().findAllByApproximateDisplayName( child.getDisplayName() ).size()
      );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testFindAllByApproximateDisplayName_whenUpperCaseInRegistry

  // TESTS //  
  
  public void testFindAllByApproximateExtension_whenUpperCaseInRegistry() {
    try {
      LOG.info("testFindAllByApproximateExtension_whenUpperCaseInRegistry");
      R       r     = R.getContext("i2mi");
      Stem    i2mi  = r.getStem("i2mi");
      Stem    child = i2mi.addChildStem("UPPER", "lower");
  
      assertEquals(
        "stems found by extension",
        1, 
        GrouperDAOFactory.getFactory().getStem().findAllByApproximateExtension( child.getExtension() ).size()
      );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testFindAllByApproximateExtension_whenUpperCaseInRegistry

  /**
   * 
   */
  public void testFindByAttributeDefName() {

    GrouperSession grouperSession = GrouperSession.startRootSession();

    AttributeDef attributeDef = new AttributeDefSave(grouperSession).assignAttributeDefNameToEdit("test:attrDef")
        .assignAttributeDefType(AttributeDefType.attr).assignCreateParentStemsIfNotExist(true)
        .assignToStem(true)
        .assignValueType(AttributeDefValueType.string).save();

    AttributeDefName attributeDefName = new AttributeDefNameSave(grouperSession, attributeDef)
      .assignAttributeDefNameNameToEdit("test:attrDefName").assignCreateParentStemsIfNotExist(true).save();

    Stem stem0 = new StemSave(grouperSession).assignName("test:stem0").assignCreateParentStemsIfNotExist(true).save();
    Stem stem1 = new StemSave(grouperSession).assignName("test:stem1").assignCreateParentStemsIfNotExist(true).save();
    Stem stem2 = new StemSave(grouperSession).assignName("test:stem2").assignCreateParentStemsIfNotExist(true).save();
    Stem stem3 = new StemSave(grouperSession).assignName("test:stem3").assignCreateParentStemsIfNotExist(true).save();
    Stem stem4 = new StemSave(grouperSession).assignName("test:stem4").assignCreateParentStemsIfNotExist(true).save();

    stem0.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "abc");
    stem1.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "xyz");
    stem3.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "abc");
    stem4.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "abc");

    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_READ, false);
    
    stem0.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ATTR_READ);
    stem1.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ATTR_READ);
    stem2.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ATTR_READ);
    stem3.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ATTR_READ);
    
    stem0.grantPriv(SubjectTestHelper.SUBJ2, NamingPrivilege.STEM_ATTR_READ);
    stem1.grantPriv(SubjectTestHelper.SUBJ2, NamingPrivilege.STEM_ATTR_READ);
    stem2.grantPriv(SubjectTestHelper.SUBJ2, NamingPrivilege.STEM_ATTR_READ);
    stem3.grantPriv(SubjectTestHelper.SUBJ2, NamingPrivilege.STEM_ATTR_READ);
    stem4.grantPriv(SubjectTestHelper.SUBJ2, NamingPrivilege.STEM_ATTR_READ);
    
    //subj0 can read most of both
    //subj1 can read the attr
    //subj2 can read the group attrs
    
    GrouperSession.stopQuietly(grouperSession);

    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    List<Stem> stems = new ArrayList<Stem>(new StemFinder().assignNameOfAttributeDefName(attributeDefName.getName())
        .assignPrivileges(NamingPrivilege.ATTRIBUTE_READ_PRIVILEGES).findStems());
    
    assertEquals(3, GrouperUtil.length(stems));
    assertEquals("test:stem0", stems.get(0).getName());
    assertEquals("test:stem1", stems.get(1).getName());
    assertEquals("test:stem3", stems.get(2).getName());
    
    stems = new ArrayList<Stem>(new StemFinder().assignNameOfAttributeDefName(attributeDefName.getName())
        .assignPrivileges(NamingPrivilege.ATTRIBUTE_READ_PRIVILEGES).assignAttributeValue("abc").findStems());
    
    assertEquals(2, GrouperUtil.length(stems));
    assertEquals("test:stem0", stems.get(0).getName());
    assertEquals("test:stem3", stems.get(1).getName());
    
    GrouperSession.stopQuietly(grouperSession);
    

    // #####################
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);

    stems = new ArrayList<Stem>(new StemFinder().assignNameOfAttributeDefName(attributeDefName.getName())
        .assignPrivileges(NamingPrivilege.ATTRIBUTE_READ_PRIVILEGES).findStems());
    
    assertEquals(0, GrouperUtil.length(stems));
    
    stems = new ArrayList<Stem>(new StemFinder().assignNameOfAttributeDefName(attributeDefName.getName())
        .assignPrivileges(AccessPrivilege.ATTRIBUTE_READ_PRIVILEGES).assignAttributeValue("abc").findStems());
    
    assertEquals(0, GrouperUtil.length(stems));
    
    GrouperSession.stopQuietly(grouperSession);
    
    // #####################
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ2);

    try {
      stems = new ArrayList<Stem>(new StemFinder().assignNameOfAttributeDefName(attributeDefName.getName())
          .assignPrivileges(AccessPrivilege.ATTRIBUTE_READ_PRIVILEGES).findStems());
      fail("Cant find attribute");
    } catch (Exception e) {
      //good
    }
    
    stems = new ArrayList<Stem>(new StemFinder().assignIdOfAttributeDefName(attributeDefName.getId())
        .assignPrivileges(AccessPrivilege.ATTRIBUTE_READ_PRIVILEGES).findStems());
    
    assertEquals(0, GrouperUtil.length(stems));
    
    stems = new ArrayList<Stem>(new StemFinder().assignIdOfAttributeDefName(attributeDefName.getId())
        .assignPrivileges(AccessPrivilege.ATTRIBUTE_READ_PRIVILEGES).assignAttributeValue("abc").findStems());
    
    assertEquals(0, GrouperUtil.length(stems));

    
    GrouperSession.stopQuietly(grouperSession);
    
    
  }
  
  // TESTS //  
  
  public void testFindAllByApproximateName_whenUpperCaseInRegistry() {
    try {
      LOG.info("testFindAllByApproximateName_whenUpperCaseInRegistry");
      R       r     = R.getContext("i2mi");
      Stem    i2mi  = r.getStem("i2mi");
      Stem    child = i2mi.addChildStem("UPPER", "lower");
  
      assertEquals(
        "stems found by name",
        1, 
        GrouperDAOFactory.getFactory().getStem().findAllByApproximateName( child.getName() ).size()
      );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testFindAllByApproximateName_whenUpperCaseInRegistry

}

