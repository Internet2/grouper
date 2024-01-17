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

package edu.internet2.middleware.grouper;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.hibernate.type.StringType;

import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.GrouperHibernateConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.exception.GrantPrivilegeException;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.RevokePrivilegeAlreadyRevokedException;
import edu.internet2.middleware.grouper.exception.StemAddException;
import edu.internet2.middleware.grouper.exception.StemDeleteException;
import edu.internet2.middleware.grouper.exception.StemModifyException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.helper.GroupHelper;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.PrivHelper;
import edu.internet2.middleware.grouper.helper.R;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.helper.T;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.QuerySort;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.misc.SaveResultType;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.validator.NamingValidator;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import junit.framework.Assert;
import junit.textui.TestRunner;

/**
 * Test {@link Stem}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestStem.java,v 1.34 2009-12-07 07:31:09 mchyzer Exp $
 */
public class TestStem extends GrouperTest {

  /** Private Class Constants */
  private static final Log LOG = GrouperUtil.getLog(TestStem.class);

  /**
   * Method main.
   * @param args String[]
   */
  public static void main(String[] args) {
    TestRunner.run(new TestStem("testObliterateAndPointInTimeComposite"));
    //TestRunner.run(TestStem.class);
    //stemObliterate2setup();
    //loadLotsOfData(3, 3, 3, 3, 3);
  }

  /**
   * 
   * @param name
   */
  public TestStem(String name) {
    super(name);
  }
  
  /**
   * 
   */
  public static void stemObliterate2setup() {
    
    // test stem has objects and privs (subj0[ADMIN], subj1[ADMIN])
    // test:someGroupA has a member: subj9, and privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someAttrDefA has privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someAttrDefNameA has effective privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someGroupB has a member: subj9, and privs (subj1[ADMIN])
    // test:someAttrDefB has privs (subj1[ADMIN])
    // test:someAttrDefNameB has effective privs (subj1[ADMIN])

    // test:anotherFolder (subj0[ADMIN], subj1[ADMIN])
    // test:anotherFolder:someGroup3 has a member: subj9, and privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:anotherFolder:someGroup4 has a member: subj9, and privs (subj1[ADMIN])
    // test:anotherFolder:attributeDef3 has privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:anotherFolder:attributeDefName3 has effective privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:anotherFolder:attributeDef4 has privs (subj1[ADMIN])
    // test:anotherFolder:attributeDefName4 has effective privs (subj1[ADMIN])

    // test:anotherFolder2 (subj1[ADMIN])

    // test:anotherFolder3 (subj1[ADMIN])
    // test:anotherFolder3:someFolder5 (subj1[ADMIN])
    // test:anotherFolder3:someFolder6

    // test:anotherFolder4

    // test:anotherFolder7 (subj1[ADMIN])
    // test:anotherFolder7:someFolder8 (subj1[ADMIN])
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    // test stem, subject 1 and 2 have admin
    StemSave stemSave = new StemSave(grouperSession).assignName("test")
        .assignCreateParentStemsIfNotExist(true).assignDisplayName("test");
    Stem stem = stemSave.save();
    stem.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ADMIN, false);
    stem.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.STEM_ADMIN, false);

    // some group A, subject 0 and 1 have admin
    GroupSave groupSave = new GroupSave(grouperSession)
      .assignName("test:someGroupA")
      .assignCreateParentStemsIfNotExist(true)
      .assignTypeOfGroup(TypeOfGroup.group);
    Group group = groupSave.save();
    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);
    group.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN, false);
    group.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.UPDATE, false);
    group.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.READ, false);
    group.addMember(SubjectTestHelper.SUBJ9, false);
    
    //some attribute A, subject 0 and 1 have admin
    AttributeDefSave attributeDefSave = new AttributeDefSave(grouperSession)
      .assignName("test:someAttrDefA")
      .assignCreateParentStemsIfNotExist(true).assignToStem(true)
      .assignAttributeDefType(AttributeDefType.attr).assignMultiAssignable(false)
      .assignMultiValued(false).assignValueType(AttributeDefValueType.marker);
    AttributeDef attributeDef = attributeDefSave.save();
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_ADMIN, false);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_ADMIN, false);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ2, AttributeDefPrivilege.ATTR_UPDATE, false);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ2, AttributeDefPrivilege.ATTR_READ, false);

    AttributeDefNameSave attributeDefNameSave = new AttributeDefNameSave(
      grouperSession, attributeDef).assignName("test:someAttrDefNameA")
      .assignCreateParentStemsIfNotExist(true);
    AttributeDefName attributeDefName = attributeDefNameSave.save();

    // some group B, subject 2 has admin
    groupSave = new GroupSave(grouperSession)
      .assignName("test:someGroupB")
      .assignCreateParentStemsIfNotExist(true)
      .assignTypeOfGroup(TypeOfGroup.group);
    group = groupSave.save();
    group.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN, false);
    group.addMember(SubjectTestHelper.SUBJ9, false);

    attributeDefSave = new AttributeDefSave(grouperSession)
      .assignName("test:someAttrDefB")
      .assignCreateParentStemsIfNotExist(true).assignToStem(true)
      .assignAttributeDefType(AttributeDefType.attr).assignMultiAssignable(false)
      .assignMultiValued(false).assignValueType(AttributeDefValueType.marker);
    attributeDef = attributeDefSave.save();
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_ADMIN, false);

    attributeDefNameSave = new AttributeDefNameSave(
        grouperSession, attributeDef).assignName("test:someAttrDefNameB")
        .assignCreateParentStemsIfNotExist(true);
    attributeDefName = attributeDefNameSave.save();

    // another folder, test.subject.0 and 1 has admin
    stemSave = new StemSave(grouperSession).assignName("test:anotherFolder")
        .assignCreateParentStemsIfNotExist(true);
    stem = stemSave.save();
    stem.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ADMIN, false);
    stem.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.STEM_ADMIN, false);

    // another folder2, test.subject.0 does not have admin, 1 does
    stemSave = new StemSave(grouperSession).assignName("test:anotherFolder2")
        .assignCreateParentStemsIfNotExist(true);
    stem = stemSave.save();
    stem.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.STEM_ADMIN, false);

    // another folder2, test.subject.0 does not have admin, 1 does
    stemSave = new StemSave(grouperSession).assignName("test:anotherFolder3")
        .assignCreateParentStemsIfNotExist(true);
    stem = stemSave.save();
    stem.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.STEM_ADMIN, false);

    stemSave = new StemSave(grouperSession).assignName("test:anotherFolder4")
        .assignCreateParentStemsIfNotExist(true);
    stem = stemSave.save();
    stem.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.STEM_ADMIN, false);

    stemSave = new StemSave(grouperSession).assignName("test:anotherFolder3:someFolder5")
        .assignCreateParentStemsIfNotExist(true);
    stem = stemSave.save();
    stem.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.STEM_ADMIN, false);
    
    stemSave = new StemSave(grouperSession).assignName("test:anotherFolder3:someFolder6")
        .assignCreateParentStemsIfNotExist(true);
    stem = stemSave.save();
    stem.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.STEM_ADMIN, false);
    
    stemSave = new StemSave(grouperSession).assignName("test:anotherFolder7")
        .assignCreateParentStemsIfNotExist(true);
    stem = stemSave.save();
    stem.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.STEM_ADMIN, false);

    stemSave = new StemSave(grouperSession).assignName("test:anotherFolder7:someFolder8")
        .assignCreateParentStemsIfNotExist(true);
    stem = stemSave.save();
    stem.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.STEM_ADMIN, false);
    

    // somegroup3, both have admin
    groupSave = new GroupSave(grouperSession)
        .assignName("test:anotherFolder:someGroup3")
        .assignCreateParentStemsIfNotExist(true)
        .assignDisplayName("test:anotherFolder:someGroup3")
        .assignTypeOfGroup(TypeOfGroup.group);
    group = groupSave.save();
    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);
    group.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN, false);
    group.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.UPDATE, false);
    group.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.READ, false);
    group.addMember(SubjectTestHelper.SUBJ9, false);

    // somegroup4, 1 has admin
    groupSave = new GroupSave(grouperSession)
        .assignName("test:anotherFolder:someGroup4")
        .assignCreateParentStemsIfNotExist(true)
        .assignTypeOfGroup(TypeOfGroup.group);
    group = groupSave.save();
    group.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN, false);
    group.addMember(SubjectTestHelper.SUBJ9, false);

    attributeDefSave = new AttributeDefSave(grouperSession)
      .assignName("test:anotherFolder:attributeDef3")
      .assignCreateParentStemsIfNotExist(true).assignToStem(true)
      .assignAttributeDefType(AttributeDefType.attr).assignMultiAssignable(false)
      .assignMultiValued(false).assignValueType(AttributeDefValueType.marker);
    attributeDef = attributeDefSave.save();
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_ADMIN, false);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_ADMIN, false);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ2, AttributeDefPrivilege.ATTR_UPDATE, false);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ2, AttributeDefPrivilege.ATTR_READ, false);
  
    attributeDefNameSave = new AttributeDefNameSave(
      grouperSession, attributeDef).assignName("test:anotherFolder:attributeDefName3")
      .assignCreateParentStemsIfNotExist(true);
    attributeDefName = attributeDefNameSave.save();

    attributeDefSave = new AttributeDefSave(grouperSession)
      .assignName("test:anotherFolder:attributeDef4")
      .assignCreateParentStemsIfNotExist(true).assignToStem(true)
      .assignAttributeDefType(AttributeDefType.attr).assignMultiAssignable(false)
      .assignMultiValued(false).assignValueType(AttributeDefValueType.marker);
    attributeDef = attributeDefSave.save();
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_ADMIN, false);

    attributeDefNameSave = new AttributeDefNameSave(
      grouperSession, attributeDef).assignName("test:anotherFolder:attributeDefName4")
      .assignCreateParentStemsIfNotExist(true);
    attributeDefName = attributeDefNameSave.save();


  }

  public void testStemSaveReplaceAllSettingsFalse() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    // test stem, subject 1 and 2 have admin
    StemSave stemSave = new StemSave(grouperSession).assignName("test")
        .assignCreateParentStemsIfNotExist(true).assignDisplayName("test")
        .assignDescription("testDescription");
    Stem stem = stemSave.save();
    
    assertEquals("testDescription", stem.getDescription());
    
    stemSave = new StemSave(grouperSession).assignUuid(stem.getId())
        .assignDisplayExtension("test1")
        .assignAlternateName("newAlternateName")
        .assignReplaceAllSettings(false);
    stem = stemSave.save();
    
    assertEquals("testDescription", stem.getDescription());
    assertEquals("test1", stem.getDisplayExtension());
    assertEquals("newAlternateName", stem.getAlternateName());
    
  }
  
  public void testStemDelete() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    // test stem, subject 1 and 2 have admin
    StemSave stemSave = new StemSave(grouperSession).assignName("test")
        .assignCreateParentStemsIfNotExist(true).assignDisplayName("test")
        .assignDescription("testDescription");
    Stem stem = stemSave.save();
    
    assertEquals("testDescription", stem.getDescription());
    
    stemSave = new StemSave(grouperSession).assignUuid(stem.getId())
        .assignSaveMode(SaveMode.DELETE);
    stem = stemSave.save();
    
    assertEquals(SaveResultType.DELETE, stemSave.getSaveResultType());
    
  }
  
  /**
   * 
   */
  public void testStemObliterate2Group0() {


    // test stem has objects and privs (subj0[ADMIN], subj1[ADMIN])
    // test:someGroupA has a member: subj9, and privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someAttrDefA has privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someAttrDefNameA has effective privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someGroupB has privs (subj1[ADMIN])
    // test:someAttrDefB has privs (subj1[ADMIN])
    // test:someAttrDefNameB has effective privs (subj1[ADMIN])

    // test:anotherFolder (subj0[ADMIN], subj1[ADMIN])
    // test:anotherFolder:someGroup3 has a member: subj9, and privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:anotherFolder:someGroup4 has privs (subj1[ADMIN])

    // test:anotherFolder2 (subj1[ADMIN])

    // test:anotherFolder3 (subj1[ADMIN])
    // test:anotherFolder3:someFolder5 (subj1[ADMIN])
    // test:anotherFolder3:someFolder6

    // test:anotherFolder4

    // test:anotherFolder7 (subj1[ADMIN])
    // test:anotherFolder7:someFolder8 (subj1[ADMIN])

    stemObliterate2setup();

    // subj7 can do nothing
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ7);

    Stem testStem = StemFinder.findByName(grouperSession, "test", true);

    assertFalse(testStem.isCanObliterate());

    int count = testStem.deleteGroups(false, false, Scope.ONE).size();
    assertEquals(0, count);
  }
  
  /**
   * 
   */
  public void testStemObliterate2Group1() {


    // test stem has objects and privs (subj0[ADMIN], subj1[ADMIN])
    // test:someGroupA has a member: subj9, and privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someAttrDefA has privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someAttrDefNameA has effective privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someGroupB has privs (subj1[ADMIN])
    // test:someAttrDefB has privs (subj1[ADMIN])
    // test:someAttrDefNameB has effective privs (subj1[ADMIN])

    // test:anotherFolder (subj0[ADMIN], subj1[ADMIN])
    // test:anotherFolder:someGroup3 has a member: subj9, and privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:anotherFolder:someGroup4 has privs (subj1[ADMIN])

    // test:anotherFolder2 (subj1[ADMIN])

    // test:anotherFolder3 (subj1[ADMIN])
    // test:anotherFolder3:someFolder5 (subj1[ADMIN])
    // test:anotherFolder3:someFolder6

    // test:anotherFolder4

    // test:anotherFolder7 (subj1[ADMIN])
    // test:anotherFolder7:someFolder8 (subj1[ADMIN])

    stemObliterate2setup();

    // subj7 can do nothing
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ2);

    Stem testStem = StemFinder.findByName(grouperSession, "test", true);

    int count = testStem.deleteGroups(false, false, Scope.ONE).size();
    assertEquals(0, count);
  }
  
  /**
   * 
   */
  public void testStemObliterate2Group2() {


    // test stem has objects and privs (subj0[ADMIN], subj1[ADMIN])
    // test:someGroupA has a member: subj9, and privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someAttrDefA has privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someAttrDefNameA has effective privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someGroupB has privs (subj1[ADMIN])
    // test:someAttrDefB has privs (subj1[ADMIN])
    // test:someAttrDefNameB has effective privs (subj1[ADMIN])

    // test:anotherFolder (subj0[ADMIN], subj1[ADMIN])
    // test:anotherFolder:someGroup3 has a member: subj9, and privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:anotherFolder:someGroup4 has privs (subj1[ADMIN])

    // test:anotherFolder2 (subj1[ADMIN])

    // test:anotherFolder3 (subj1[ADMIN])
    // test:anotherFolder3:someFolder5 (subj1[ADMIN])
    // test:anotherFolder3:someFolder6

    // test:anotherFolder4

    // test:anotherFolder7 (subj1[ADMIN])
    // test:anotherFolder7:someFolder8 (subj1[ADMIN])

    stemObliterate2setup();

    // subj1 can ddelete 2 groups
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);

    Group group = GroupFinder.findByName(grouperSession, "test:someGroupA", true);
    assertNotNull(group);
    group = GroupFinder.findByName(grouperSession, "test:someGroupB", true);
    assertNotNull(group);
    
    Stem testStem = StemFinder.findByName(grouperSession, "test", true);

    int count = testStem.deleteGroups(false, false, Scope.ONE).size();
    assertEquals(2, count);

    GrouperSession.stopQuietly(grouperSession);
    
    grouperSession = GrouperSession.startRootSession();
    
    group = GroupFinder.findByName(grouperSession, "test:someGroupA", false);
    assertNull(group);
    group = GroupFinder.findByName(grouperSession, "test:someGroupB", false);
    assertNull(group);
  }
  
  /**
   * 
   */
  public void testStemObliterate2Group3() {


    // test stem has objects and privs (subj0[ADMIN], subj1[ADMIN])
    // test:someGroupA has a member: subj9, and privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someAttrDefA has privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someAttrDefNameA has effective privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someGroupB has privs (subj1[ADMIN])
    // test:someAttrDefB has privs (subj1[ADMIN])
    // test:someAttrDefNameB has effective privs (subj1[ADMIN])

    // test:anotherFolder (subj0[ADMIN], subj1[ADMIN])
    // test:anotherFolder:someGroup3 has a member: subj9, and privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:anotherFolder:someGroup4 has privs (subj1[ADMIN])

    // test:anotherFolder2 (subj1[ADMIN])

    // test:anotherFolder3 (subj1[ADMIN])
    // test:anotherFolder3:someFolder5 (subj1[ADMIN])
    // test:anotherFolder3:someFolder6

    // test:anotherFolder4

    // test:anotherFolder7 (subj1[ADMIN])
    // test:anotherFolder7:someFolder8 (subj1[ADMIN])

    stemObliterate2setup();

    // subj7 can do nothing
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);

    Group group = GroupFinder.findByName(grouperSession, "test:someGroupA", true);
    assertNotNull(group);
    
    Stem testStem = StemFinder.findByName(grouperSession, "test", true);

    int count = testStem.deleteGroups(false, false, Scope.ONE).size();
    assertEquals(1, count);

    GrouperSession.stopQuietly(grouperSession);
    
    grouperSession = GrouperSession.startRootSession();
    
    group = GroupFinder.findByName(grouperSession, "test:someGroupA", false);
    assertNull(group);
  }
  
  /**
   * 
   */
  public void testStemObliterate2GroupMemberships4() {


    // test stem has objects and privs (subj0[ADMIN], subj1[ADMIN])
    // test:someGroupA has a member: subj9, and privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someAttrDefA has privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someAttrDefNameA has effective privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someGroupB has a member: subj9, and privs (subj1[ADMIN])
    // test:someAttrDefB has privs (subj1[ADMIN])
    // test:someAttrDefNameB has effective privs (subj1[ADMIN])

    // test:anotherFolder (subj0[ADMIN], subj1[ADMIN])
    // test:anotherFolder:someGroup3 has a member: subj9, and privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:anotherFolder:someGroup4 has a member: subj9, and privs (subj1[ADMIN])

    // test:anotherFolder2 (subj1[ADMIN])

    // test:anotherFolder3 (subj1[ADMIN])
    // test:anotherFolder3:someFolder5 (subj1[ADMIN])
    // test:anotherFolder3:someFolder6

    // test:anotherFolder4

    // test:anotherFolder7 (subj1[ADMIN])
    // test:anotherFolder7:someFolder8 (subj1[ADMIN])

    stemObliterate2setup();

    // subj0 can delete two groups
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);

    Group group = GroupFinder.findByName(grouperSession, "test:someGroupA", true);
    assertNotNull(group);
    assertTrue(group.hasMember(SubjectTestHelper.SUBJ9));
    
    group = GroupFinder.findByName(grouperSession, "test:someGroupB", true);
    assertNotNull(group);
    assertTrue(group.hasMember(SubjectTestHelper.SUBJ9));

    group = GroupFinder.findByName(grouperSession, "test:anotherFolder:someGroup3", true);
    assertNotNull(group);
    assertTrue(group.hasMember(SubjectTestHelper.SUBJ9));

    group = GroupFinder.findByName(grouperSession, "test:anotherFolder:someGroup4", true);
    assertNotNull(group);
    assertTrue(group.hasMember(SubjectTestHelper.SUBJ9));

    Stem testStem = StemFinder.findByName(grouperSession, "test", true);

    int count = testStem.deleteGroupMemberships(false, false, Scope.SUB).size();
    assertEquals(2, count);

    GrouperSession.stopQuietly(grouperSession);
    
    grouperSession = GrouperSession.startRootSession();
    
    group = GroupFinder.findByName(grouperSession, "test:someGroupA", false);
    assertNotNull(group);
    assertFalse(group.hasMember(SubjectTestHelper.SUBJ9));

    group = GroupFinder.findByName(grouperSession, "test:someGroupB", false);
    assertNotNull(group);
    assertTrue(group.hasMember(SubjectTestHelper.SUBJ9));

    group = GroupFinder.findByName(grouperSession, "test:anotherFolder:someGroup3", false);
    assertNotNull(group);
    assertFalse(group.hasMember(SubjectTestHelper.SUBJ9));

    group = GroupFinder.findByName(grouperSession, "test:anotherFolder:someGroup4", false);
    assertNotNull(group);
    assertTrue(group.hasMember(SubjectTestHelper.SUBJ9));
}
  
  public void testGetAllStemsSplitScopeSecure() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      QueryOptions queryOptions = new QueryOptions().paging(100, 1, true).sortAsc("name");
      GrouperDAOFactory.getFactory().getStem().getAllStemsSplitScopeSecure("ed", grouperSession, grouperSession.getSubject(), 
          GrouperUtil.toSet(NamingPrivilege.CREATE), queryOptions);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
  
  /**
   * make sure obliterate works
   */
  public void testObliterate() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignName("test").save();
    new StemSave(grouperSession).assignName("test:sub1").save();
    new StemSave(grouperSession).assignName("test:sub2").save();
    new StemSave(grouperSession).assignName("test:sub1:sub12").save();
    new StemSave(grouperSession).assignName("test:sub2:sub22").save();
    
    new GroupSave(grouperSession).assignName("test:testGroup").save();
    new GroupSave(grouperSession).assignName("test:testGroup2").save();
    new GroupSave(grouperSession).assignName("test:testGroup3").save();
    new GroupSave(grouperSession).assignName("test:sub1:testGroup").save();
    new GroupSave(grouperSession).assignName("test:sub1:testGroup2").save();
    new GroupSave(grouperSession).assignName("test:sub1:testGroup3").save();
    new GroupSave(grouperSession).assignName("test:sub2:testGroup").save();
    new GroupSave(grouperSession).assignName("test:sub2:testGroup2").save();
    new GroupSave(grouperSession).assignName("test:sub2:testGroup3").save();
    new GroupSave(grouperSession).assignName("test:sub1:sub12:testGroup").save();
    new GroupSave(grouperSession).assignName("test:sub1:sub12:testGroup2").save();
    new GroupSave(grouperSession).assignName("test:sub1:sub12:testGroup3").save();
    new GroupSave(grouperSession).assignName("test:sub2:sub22:testGroup").save();
    new GroupSave(grouperSession).assignName("test:sub2:sub22:testGroup2").save();
    new GroupSave(grouperSession).assignName("test:sub2:sub22:testGroup3").save();

    AttributeDef testTestAttributeDef = new AttributeDefSave(grouperSession).assignName("test:testAttributeDef").save();
    AttributeDef testTest2AttributeDef = new AttributeDefSave(grouperSession).assignName("test:testAttributeDef2").save();
    AttributeDef testTest3AttributeDef = new AttributeDefSave(grouperSession).assignName("test:testAttributeDef3").save();
    AttributeDef testTestSub1AttributeDef = new AttributeDefSave(grouperSession).assignName("test:sub1:testAttributeDef").save();
    AttributeDef testTestSub12AttributeDef = new AttributeDefSave(grouperSession).assignName("test:sub1:testAttributeDef2").save();
    AttributeDef testTestSub13AttributeDef = new AttributeDefSave(grouperSession).assignName("test:sub1:testAttributeDef3").save();
    AttributeDef testTestSub2AttributeDef = new AttributeDefSave(grouperSession).assignName("test:sub2:testAttributeDef").save();
    AttributeDef testTestSub22AttributeDef = new AttributeDefSave(grouperSession).assignName("test:sub2:testAttributeDef2").save();
    AttributeDef testTestSub23AttributeDef = new AttributeDefSave(grouperSession).assignName("test:sub2:testAttributeDef3").save();
    AttributeDef testTestSub112AttributeDef = new AttributeDefSave(grouperSession).assignName("test:sub1:sub12:testAttributeDef").save();
    AttributeDef testTestSub122AttributeDef = new AttributeDefSave(grouperSession).assignName("test:sub1:sub12:testAttributeDef2").save();
    AttributeDef testTestSub123AttributeDef = new AttributeDefSave(grouperSession).assignName("test:sub1:sub12:testAttributeDef3").save();
    AttributeDef testTestSub222AttributeDef = new AttributeDefSave(grouperSession).assignName("test:sub2:sub22:testAttributeDef").save();
    AttributeDef testTestSub2222AttributeDef = new AttributeDefSave(grouperSession).assignName("test:sub2:sub22:testAttributeDef2").save();
    AttributeDef testTestSub2223AttributeDef = new AttributeDefSave(grouperSession).assignName("test:sub2:sub22:testAttributeDef3").save();

    new AttributeDefNameSave(grouperSession, testTestAttributeDef).assignName("test:testAttributeDefName").save();
    new AttributeDefNameSave(grouperSession, testTest2AttributeDef).assignName("test:testAttributeDefName2").save();
    new AttributeDefNameSave(grouperSession, testTest3AttributeDef).assignName("test:testAttributeDefName3").save();
    new AttributeDefNameSave(grouperSession, testTestSub1AttributeDef).assignName("test:testSub1AttributeDefName").save();
    new AttributeDefNameSave(grouperSession, testTestSub12AttributeDef).assignName("test:testSub12AttributeDefName").save();
    new AttributeDefNameSave(grouperSession, testTestSub13AttributeDef).assignName("test:testSub13AttributeDefName").save();
    new AttributeDefNameSave(grouperSession, testTestSub2AttributeDef).assignName("test:testSub2AttributeDefName").save();
    new AttributeDefNameSave(grouperSession, testTestSub22AttributeDef).assignName("test:testSub22AttributeDefName").save();
    new AttributeDefNameSave(grouperSession, testTestSub23AttributeDef).assignName("test:testSub23AttributeDefName").save();
    new AttributeDefNameSave(grouperSession, testTestSub112AttributeDef).assignName("test:testSub112AttributeDefName").save();
    new AttributeDefNameSave(grouperSession, testTestSub122AttributeDef).assignName("test:testSub122AttributeDefName").save();
    new AttributeDefNameSave(grouperSession, testTestSub123AttributeDef).assignName("test:testSub123AttributeDefName").save();
    new AttributeDefNameSave(grouperSession, testTestSub222AttributeDef).assignName("test:testSub222AttributeDefName").save();
    new AttributeDefNameSave(grouperSession, testTestSub2222AttributeDef).assignName("test:testSub2222AttributeDefName").save();
    new AttributeDefNameSave(grouperSession, testTestSub2223AttributeDef).assignName("test:testSub2223AttributeDefName").save();

    stem.obliterate(true, true);
    
    stem = StemFinder.findByName(grouperSession, "test", true);
    
    assertNotNull(stem);
    
    stem.obliterate(false, false);
    
    stem = StemFinder.findByName(grouperSession, "test", false, new QueryOptions().secondLevelCache(false));
    
    assertNull(stem);
    
  }
  
  /**
   * make sure obliterate works
   */
  public void testObliterateLots() {

    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignName("test").save();
    loadLotsOfData(true, true, true, true, 2, 2, 2, 1, 2, 1, 1, 2, 3, 4);
    stem.obliterate(true, false);
    
    stem = StemFinder.findByName(grouperSession, "test", false, new QueryOptions().secondLevelCache(false));
    
    assertNull(stem);
    
  }

  /**
   * load lots of data
   * @param createGroups 
   * @param createStems 
   * @param createAttributeDefs 
   * @param createAttributeNames 
   * @param sizeLevel0
   * @param sizeLevel1
   * @param sizeLevel2
   * @param sizeLevel3
   * @param sizeLevel4
   * @param sizeLevel5 
   * @param sizeLevel6 
   * @param sizeLevel7 
   * @param sizeLevel8 
   * @param sizeLevel9 
   */
  public static void loadLotsOfData(final boolean createGroups, final boolean createStems, final boolean createAttributeDefs, final boolean createAttributeNames,
      final int sizeLevel0, final int sizeLevel1, final int sizeLevel2, final int sizeLevel3, final int sizeLevel4, 
      final int sizeLevel5, final int sizeLevel6, final int sizeLevel7, final int sizeLevel8, final int sizeLevel9) {
    
    final int[] objectsCreated = new int[]{0};
    final boolean[] done = new boolean[]{false};
    new Thread(new Runnable() {

      public void run() {
        int objectCount = createGroups ? 1 : 0;
        objectCount += createStems ? 1 : 0;
        objectCount += createAttributeDefs ? 1 : 0;
        objectCount += createAttributeNames ? 1 : 0;
        
        while (true) {
          for (int i=0;i<15;i++) {
            GrouperUtil.sleep(1000);
            if (done[0]) {
              return;
            }
          }
          System.out.println("done creating " + objectsCreated[0] + " of " + (objectCount * sizeLevel0 * sizeLevel1 * sizeLevel2 * sizeLevel3 * sizeLevel4
              * sizeLevel5 * sizeLevel6 * sizeLevel7 * sizeLevel8 * sizeLevel9) );
        }
      }
      
    }).start();

    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

        char[] level0 = createCharArray(sizeLevel0, 'a');
        char[] level1 = createCharArray(sizeLevel1, 'f');
        char[] level2 = createCharArray(sizeLevel2, 'k');
        char[] level3 = createCharArray(sizeLevel3, 'p');
        char[] level4 = createCharArray(sizeLevel4, 'u');
        char[] level5 = createCharArray(sizeLevel5, 'A');
        char[] level6 = createCharArray(sizeLevel6, 'F');
        char[] level7 = createCharArray(sizeLevel7, 'K');
        char[] level8 = createCharArray(sizeLevel8, 'P');
        char[] level9 = createCharArray(sizeLevel9, 'U');

        AttributeDef attributeDef = null;
        if (createAttributeNames && !createAttributeDefs) {
          attributeDef = new AttributeDefSave(grouperSession).assignCreateParentStemsIfNotExist(false).assignName("test:testAttributeDef").save();
        }
        
        for (char theChar0 : level0) {
          for (char theChar1 : level1) {
            for (char theChar2 : level2) {
              for (char theChar3 : level3) {
                for (char theChar4 : level4) {
                  for (char theChar5 : level5) {
                    for (char theChar6 : level6) {
                      for (char theChar7 : level7) {
                        for (char theChar8 : level8) {
                          for (char theChar9 : level9) {
                  
                            final String stemName = "test:" + theChar0 + ":" + theChar1 + ":" + theChar2 + ":" + theChar3 + ":" + theChar4
                                + ":" + theChar5 + ":" + theChar6 + ":" + theChar7 + ":" + theChar8 + ":" + theChar9;
                            
                            if (createStems) {
                              new StemSave(grouperSession).assignName(stemName)
                                .assignCreateParentStemsIfNotExist(true).save();
                              objectsCreated[0]++;
                            }
                            if (createGroups) {
                              new GroupSave(grouperSession).assignName(stemName + ":testGroup")
                                .assignCreateParentStemsIfNotExist(true).save();
                              objectsCreated[0]++;
                            }
                            AttributeDef testTestAttributeDef = null;
                            if (createAttributeDefs) {
                              testTestAttributeDef = new AttributeDefSave(grouperSession).assignName(stemName + ":testAttributeDef").save();
                              objectsCreated[0]++;
                            } else {
                              testTestAttributeDef = attributeDef;
                            }
                            if (createAttributeNames) {
                              new AttributeDefNameSave(grouperSession, testTestAttributeDef).assignName(stemName + ":testAttributeDefName").save();
                              objectsCreated[0]++;
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }      

        
        return null;
      }
    });

    done[0] = true;
  }
  
  /**
   * make a char array.  e.g. if passed in 5 and 'e', then return: ['e', 'f', 'g', 'h', 'i']
   * @param size
   * @param startingChar
   * @return the array
   */
  public static char[] createCharArray(int size, char startingChar) {
    if (size<=0 || size>26) {
      throw new RuntimeException("Why is size less than or equal to 0 or greater than 26? " + size);
    }
    char[] result = new char[size];
    for (int i=0;i<size;i++) {
      char nextChar = (char)(startingChar + i);
      if (startingChar >= 'a' && startingChar <= 'z' && nextChar > 'z') {
        nextChar = 'a';
      }
      if (startingChar >= 'A' && startingChar <= 'Z' && nextChar > 'Z') {
        nextChar = 'A';
      }
      result[i] = nextChar;
    }
    return result;
  }
  
  /**
   * 
   */
  public void testFindParentStems() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group group1 = new GroupSave(grouperSession)
      .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignName("a:b:c:d")
      .assignCreateParentStemsIfNotExist(true).save();
    Group group2 = new GroupSave(grouperSession)
      .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignName("a:d:r")
      .assignCreateParentStemsIfNotExist(true).save();
    Set<Stem> stems = GrouperDAOFactory.getFactory().getStem()
      .findParentsByGroups(GrouperUtil.toSet(group1, group2));
    Stem stem1 = StemFinder.findByName(grouperSession, ":", true);
    Stem stem2 = StemFinder.findByName(grouperSession, "a", true);
    Stem stem3 = StemFinder.findByName(grouperSession, "a:b", true);
    Stem stem4 = StemFinder.findByName(grouperSession, "a:b:c", true);
    Stem stem5 = StemFinder.findByName(grouperSession, "a:d", true);
    assertEquals(5, stems.size());
    assertTrue(stems.contains(stem1));
    assertTrue(stems.contains(stem2));
    assertTrue(stems.contains(stem3));
    assertTrue(stems.contains(stem4));
    assertTrue(stems.contains(stem5));
  }
  
  public void testRoot() {
    LOG.info("testRoot");
    Stem  rootA = StemHelper.findRootStem(
      SessionHelper.getRootSession()
    );
    Stem  rootB = StemHelper.findRootStem(
      SessionHelper.getRootSession()
    );
    Assert.assertEquals("root == root", rootA, rootB);
  } // public void testRoot()

  public void testRootAsNonRoot() {
    LOG.info("testRootAsNonRoot");
    Stem  rootA = StemHelper.findRootStem(
      SessionHelper.getSession(SubjectTestHelper.SUBJ0_ID)
    );
    Stem  rootB = StemHelper.findRootStem(
      SessionHelper.getSession(SubjectTestHelper.SUBJ0_ID)
    );
    Assert.assertEquals("root == root", rootA, rootB);
  } // public void testRootAsNonRoot()

  public void testStemModifyAttributesAfterDisablingMembership() {    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("stems.updateLastMembershipTime", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.updateLastMembershipTime", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.updateLastImmediateMembershipTime", "true");

    try {
      R       r     = R.populateRegistry(1, 3, 3);
      Group   gA    = r.getGroup("a", "a");
      Group   gB    = r.getGroup("a", "b");
      Group   gC    = r.getGroup("a", "c");
      Stem    nsA   = r.getStem("a");
      
      nsA.grantPriv(gA.toSubject(), NamingPrivilege.STEM);
      gA.addMember(gB.toSubject());
      gB.addMember(gC.toSubject());
      
      GrouperUtil.sleep(100);
      long    pre   = new java.util.Date().getTime();
      GrouperUtil.sleep(100);

      Membership ms = GrouperDAOFactory.getFactory().getMembership().findByStemOwnerAndMemberAndFieldAndType(
          nsA.getUuid(), gA.toMember().getUuid(), FieldFinder.find(Field.FIELD_NAME_STEMMERS, true), MembershipType.IMMEDIATE.getTypeString(), true, true);
      ms.setEnabled(false);
      GrouperDAOFactory.getFactory().getMembership().update(ms);
      
      // load group in new session so we don't (potentially) get stale data
      GrouperSession s = GrouperSession.start(SubjectFinder.findRootSubject());
      nsA = StemFinder.findByName(s, nsA.getName(), true);
      gA = GroupFinder.findByUuid(s, gA.getUuid(), true, new QueryOptions().secondLevelCache(false));
      gB = GroupFinder.findByUuid(s, gB.getUuid(), true, new QueryOptions().secondLevelCache(false));
      gC = GroupFinder.findByUuid(s, gC.getUuid(), true, new QueryOptions().secondLevelCache(false));

      assertTrue("nsA getLastMembershipChange > pre", nsA.getLastMembershipChange().getTime() > pre);
      assertTrue("gA getLastMembershipChange < pre", gA.getLastMembershipChange().getTime() < pre);
      assertTrue("gB getLastMembershipChange < pre", gB.getLastMembershipChange().getTime() < pre);
      assertTrue("gC getLastMembershipChange < pre", gC.getLastMembershipChange().getTime() < pre);

      s.stop();
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } 
  
  public void testStemModifyAttributesAfterEnablingMembership() {    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("stems.updateLastMembershipTime", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.updateLastMembershipTime", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.updateLastImmediateMembershipTime", "true");

    try {
      R       r     = R.populateRegistry(1, 3, 3);
      Group   gA    = r.getGroup("a", "a");
      Group   gB    = r.getGroup("a", "b");
      Group   gC    = r.getGroup("a", "c");
      Stem    nsA   = r.getStem("a");
      
      nsA.grantPriv(gA.toSubject(), NamingPrivilege.STEM);
      gA.addMember(gB.toSubject());
      gB.addMember(gC.toSubject());
      
      Membership ms = GrouperDAOFactory.getFactory().getMembership().findByStemOwnerAndMemberAndFieldAndType(
          nsA.getUuid(), gA.toMember().getUuid(), FieldFinder.find(Field.FIELD_NAME_STEMMERS, true), MembershipType.IMMEDIATE.getTypeString(), true, true);
      ms.setEnabled(false);
      GrouperDAOFactory.getFactory().getMembership().update(ms);
      
      GrouperUtil.sleep(100);
      long    pre   = new java.util.Date().getTime();
      GrouperUtil.sleep(100);

      ms.setEnabled(true);
      GrouperDAOFactory.getFactory().getMembership().update(ms);
      
      // load group in new session so we don't (potentially) get stale data
      GrouperSession s = GrouperSession.start(SubjectFinder.findRootSubject());
      nsA = StemFinder.findByName(s, nsA.getName(), true);
      gA = GroupFinder.findByUuid(s, gA.getUuid(), true, new QueryOptions().secondLevelCache(false));
      gB = GroupFinder.findByUuid(s, gB.getUuid(), true, new QueryOptions().secondLevelCache(false));
      gC = GroupFinder.findByUuid(s, gC.getUuid(), true, new QueryOptions().secondLevelCache(false));

      assertTrue("nsA getLastMembershipChange > pre", nsA.getLastMembershipChange().getTime() > pre);
      assertTrue("gA getLastMembershipChange < pre", gA.getLastMembershipChange().getTime() < pre);
      assertTrue("gB getLastMembershipChange < pre", gB.getLastMembershipChange().getTime() < pre);
      assertTrue("gC getLastMembershipChange < pre", gC.getLastMembershipChange().getTime() < pre);

      s.stop();
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } 
  
  public void testStemModifyAttributesAfterDisablingMembership2() {    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("stems.updateLastMembershipTime", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.updateLastMembershipTime", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.updateLastImmediateMembershipTime", "true");

    try {
      R       r     = R.populateRegistry(1, 3, 3);
      Group   gA    = r.getGroup("a", "a");
      Group   gB    = r.getGroup("a", "b");
      Group   gC    = r.getGroup("a", "c");
      Stem    nsA   = r.getStem("a");
      
      nsA.grantPriv(gA.toSubject(), NamingPrivilege.STEM);
      gA.addMember(gB.toSubject());
      gB.addMember(gC.toSubject());
      
      GrouperUtil.sleep(100);
      long    pre   = new java.util.Date().getTime();
      GrouperUtil.sleep(100);

      Membership ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
          gA.getUuid(), gB.toMember().getUuid(), Group.getDefaultList(), MembershipType.IMMEDIATE.getTypeString(), true, true);
      ms.setEnabled(false);
      GrouperDAOFactory.getFactory().getMembership().update(ms);
      
      // load group in new session so we don't (potentially) get stale data
      GrouperSession s = GrouperSession.start(SubjectFinder.findRootSubject());
      nsA = StemFinder.findByName(s, nsA.getName(), true);
      gA = GroupFinder.findByUuid(s, gA.getUuid(), true, new QueryOptions().secondLevelCache(false));
      gB = GroupFinder.findByUuid(s, gB.getUuid(), true, new QueryOptions().secondLevelCache(false));
      gC = GroupFinder.findByUuid(s, gC.getUuid(), true, new QueryOptions().secondLevelCache(false));

      assertTrue("nsA getLastMembershipChange > pre", nsA.getLastMembershipChange().getTime() > pre);
      assertTrue("gA getLastMembershipChange > pre", gA.getLastMembershipChange().getTime() > pre);
      assertTrue("gB getLastMembershipChange < pre", gB.getLastMembershipChange().getTime() < pre);
      assertTrue("gC getLastMembershipChange < pre", gC.getLastMembershipChange().getTime() < pre);

      s.stop();
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } 
  
  public void testStemModifyAttributesAfterEnablingMembership2() {    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("stems.updateLastMembershipTime", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.updateLastMembershipTime", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.updateLastImmediateMembershipTime", "true");

    try {
      R       r     = R.populateRegistry(1, 3, 3);
      Group   gA    = r.getGroup("a", "a");
      Group   gB    = r.getGroup("a", "b");
      Group   gC    = r.getGroup("a", "c");
      Stem    nsA   = r.getStem("a");
      
      nsA.grantPriv(gA.toSubject(), NamingPrivilege.STEM);
      gA.addMember(gB.toSubject());
      gB.addMember(gC.toSubject());
      
      Membership ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
          gA.getUuid(), gB.toMember().getUuid(), Group.getDefaultList(), MembershipType.IMMEDIATE.getTypeString(), true, true);
      ms.setEnabled(false);
      GrouperDAOFactory.getFactory().getMembership().update(ms);
      
      GrouperUtil.sleep(100);
      long    pre   = new java.util.Date().getTime();
      GrouperUtil.sleep(100);
      
      ms.setEnabled(true);
      GrouperDAOFactory.getFactory().getMembership().update(ms);
      
      // load group in new session so we don't (potentially) get stale data
      GrouperSession s = GrouperSession.start(SubjectFinder.findRootSubject());
      nsA = StemFinder.findByName(s, nsA.getName(), true);
      gA = GroupFinder.findByUuid(s, gA.getUuid(), true, new QueryOptions().secondLevelCache(false));
      gB = GroupFinder.findByUuid(s, gB.getUuid(), true, new QueryOptions().secondLevelCache(false));
      gC = GroupFinder.findByUuid(s, gC.getUuid(), true, new QueryOptions().secondLevelCache(false));

      assertTrue("nsA getLastMembershipChange > pre", nsA.getLastMembershipChange().getTime() > pre);
      assertTrue("gA getLastMembershipChange > pre", gA.getLastMembershipChange().getTime() > pre);
      assertTrue("gB getLastMembershipChange < pre", gB.getLastMembershipChange().getTime() < pre);
      assertTrue("gC getLastMembershipChange < pre", gC.getLastMembershipChange().getTime() < pre);

      s.stop();
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } 
  
  public void testStemModifyAttributesAfterDisablingMembership3() {    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("stems.updateLastMembershipTime", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.updateLastMembershipTime", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.updateLastImmediateMembershipTime", "true");

    try {
      R       r     = R.populateRegistry(1, 3, 3);
      Group   gA    = r.getGroup("a", "a");
      Group   gB    = r.getGroup("a", "b");
      Group   gC    = r.getGroup("a", "c");
      Stem    nsA   = r.getStem("a");
      
      nsA.grantPriv(gA.toSubject(), NamingPrivilege.STEM);
      gA.addMember(gB.toSubject());
      gB.addMember(gC.toSubject());
      
      GrouperUtil.sleep(100);
      long    pre   = new java.util.Date().getTime();
      GrouperUtil.sleep(100);

      Membership ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
          gB.getUuid(), gC.toMember().getUuid(), Group.getDefaultList(), MembershipType.IMMEDIATE.getTypeString(), true, true);
      ms.setEnabled(false);
      GrouperDAOFactory.getFactory().getMembership().update(ms);
      
      // load group in new session so we don't (potentially) get stale data
      GrouperSession s = GrouperSession.start(SubjectFinder.findRootSubject());
      nsA = StemFinder.findByName(s, nsA.getName(), true);
      gA = GroupFinder.findByUuid(s, gA.getUuid(), true, new QueryOptions().secondLevelCache(false));
      gB = GroupFinder.findByUuid(s, gB.getUuid(), true, new QueryOptions().secondLevelCache(false));
      gC = GroupFinder.findByUuid(s, gC.getUuid(), true, new QueryOptions().secondLevelCache(false));

      assertTrue("nsA getLastMembershipChange > pre", nsA.getLastMembershipChange().getTime() > pre);
      assertTrue("gA getLastMembershipChange > pre", gA.getLastMembershipChange().getTime() > pre);
      assertTrue("gB getLastMembershipChange > pre", gB.getLastMembershipChange().getTime() > pre);
      assertTrue("gC getLastMembershipChange < pre", gC.getLastMembershipChange().getTime() < pre);

      s.stop();
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } 
  
  public void testStemModifyAttributesAfterEnablingMembership3() {    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("stems.updateLastMembershipTime", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.updateLastMembershipTime", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.updateLastImmediateMembershipTime", "true");

    try {
      R       r     = R.populateRegistry(1, 3, 3);
      Group   gA    = r.getGroup("a", "a");
      Group   gB    = r.getGroup("a", "b");
      Group   gC    = r.getGroup("a", "c");
      Stem    nsA   = r.getStem("a");
      
      nsA.grantPriv(gA.toSubject(), NamingPrivilege.STEM);
      gA.addMember(gB.toSubject());
      gB.addMember(gC.toSubject());
      
      Membership ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
          gB.getUuid(), gC.toMember().getUuid(), Group.getDefaultList(), MembershipType.IMMEDIATE.getTypeString(), true, true);
      ms.setEnabled(false);
      GrouperDAOFactory.getFactory().getMembership().update(ms);
      
      GrouperUtil.sleep(100);
      long    pre   = new java.util.Date().getTime();
      GrouperUtil.sleep(100);

      ms.setEnabled(true);
      GrouperDAOFactory.getFactory().getMembership().update(ms);
      
      // load group in new session so we don't (potentially) get stale data
      GrouperSession s = GrouperSession.start(SubjectFinder.findRootSubject());
      nsA = StemFinder.findByName(s, nsA.getName(), true);
      gA = GroupFinder.findByUuid(s, gA.getUuid(), true, new QueryOptions().secondLevelCache(false));
      gB = GroupFinder.findByUuid(s, gB.getUuid(), true, new QueryOptions().secondLevelCache(false));
      gC = GroupFinder.findByUuid(s, gC.getUuid(), true, new QueryOptions().secondLevelCache(false));

      assertTrue("nsA getLastMembershipChange > pre", nsA.getLastMembershipChange().getTime() > pre);
      assertTrue("gA getLastMembershipChange > pre", gA.getLastMembershipChange().getTime() > pre);
      assertTrue("gB getLastMembershipChange > pre", gB.getLastMembershipChange().getTime() > pre);
      assertTrue("gC getLastMembershipChange < pre", gC.getLastMembershipChange().getTime() < pre);

      s.stop();
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } 
  
  public void testGetParentStemAtRoot() {
    LOG.info("testGetParentStemAtRoot");
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemHelper.findRootStem(s);
    try {
      root.getParentStem();
      Assert.fail("root stem has parent");
    }
    catch (StemNotFoundException eSNF) {
      Assert.assertTrue("root stem has no parent", true);
    }
  } // public void testGetParentStemAtRoot()

  public void testGetParentStem() {
    LOG.info("testGetParentStem");
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemHelper.findRootStem(s);
    Stem            edu   = StemHelper.addChildStem(root, "edu", "education");
    try {
      Stem parent = edu.getParentStem();
      Assert.assertTrue("stem has parent", true);
      Assert.assertTrue("parent == root", parent.equals(root));
      Assert.assertTrue(
        "root has STEM on parent", parent.hasStem(s.getSubject())
      );
      
      Assert.assertTrue(
          "root has STEM_ADMIN on parent", parent.hasStemAdmin(s.getSubject())
        );
    }
    catch (StemNotFoundException eSNF) {
      Assert.fail("stem has no parent: " + eSNF.getMessage());
    }
  } // public void testGetParentStem()

  public void testGetChildStems() {
    LOG.info("testGetChildStems");
    try {
      R         r         = R.populateRegistry(4, 0, 0);
      Set       children  = r.ns.getChildStems();
      T.amount( "child stems", 4, children.size() );
      Stem      child;
      Iterator  it        = children.iterator();
      while (it.hasNext()) {
        child = (Stem) it.next();
        assertTrue( "child stem has right parent", child.getParentStem().equals(r.ns) );
      }
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testGetChildStems()

  /**
   * 
   */
  public void testGetChildGroups2() {
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "false");

    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem testStem = new StemSave(grouperSession).assignName("test").save();
    Stem testSubStem = new StemSave(grouperSession).assignName("test:sub").save();
    Stem rootStem = StemFinder.findRootStem(grouperSession);
    
    Group testGroup = new GroupSave(grouperSession).assignName("test:group").save();
    Group testRole = new GroupSave(grouperSession).assignName("test:role").assignTypeOfGroup(TypeOfGroup.role).save();
    Group testEntity = new GroupSave(grouperSession).assignName("test:entity").assignTypeOfGroup(TypeOfGroup.entity).save();
    
    Group testGroup2 = new GroupSave(grouperSession).assignName("test:sub:group2").save();
    Group testRole2 = new GroupSave(grouperSession).assignName("test:sub:role2").assignTypeOfGroup(TypeOfGroup.role).save();
    Group testEntity2 = new GroupSave(grouperSession).assignName("test:sub:entity2").assignTypeOfGroup(TypeOfGroup.entity).save();
    
    testGroup.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ);
    testGroup.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN);
    
    testRole.grantPriv(SubjectTestHelper.SUBJ3, AccessPrivilege.UPDATE);
    testEntity.grantPriv(SubjectTestHelper.SUBJ4, AccessPrivilege.VIEW);

    testGroup2.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.READ);
    testRole2.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.ADMIN);
    testEntity2.grantPriv(SubjectTestHelper.SUBJ3, AccessPrivilege.ADMIN);
 
    QueryOptions queryOptions = new QueryOptions();
    queryOptions.sort(QuerySort.asc("theGroup.nameDb"));
    Set<Group> groups = rootStem.getChildGroups(Scope.ONE, AccessPrivilege.VIEW_PRIVILEGES, queryOptions, null);
    assertEquals(0, GrouperUtil.length(groups));
    
    groups = rootStem.getChildGroups(Scope.SUB, AccessPrivilege.VIEW_PRIVILEGES, queryOptions, null);
    assertGroupSetsAndOrder(GrouperUtil.toSet(testEntity, testGroup, testRole, testEntity2, testGroup2, testRole2), groups);
    
    groups = testStem.getChildGroups(Scope.SUB, AccessPrivilege.VIEW_PRIVILEGES, queryOptions, null);
    assertGroupSetsAndOrder(GrouperUtil.toSet(testEntity, testGroup, testRole, testEntity2, testGroup2, testRole2), groups);
    
    groups = testStem.getChildGroups(Scope.ONE, AccessPrivilege.VIEW_PRIVILEGES, queryOptions, null);
    assertGroupSetsAndOrder(GrouperUtil.toSet(testEntity, testGroup, testRole), groups);
    
    groups = rootStem.getChildGroups(Scope.SUB, AccessPrivilege.VIEW_PRIVILEGES, queryOptions, GrouperUtil.toSet(TypeOfGroup.group));
    assertGroupSetsAndOrder(GrouperUtil.toSet(testGroup, testGroup2), groups);
    
    groups = testStem.getChildGroups(Scope.SUB, AccessPrivilege.VIEW_PRIVILEGES, queryOptions, GrouperUtil.toSet(TypeOfGroup.role, TypeOfGroup.entity));
    assertGroupSetsAndOrder(GrouperUtil.toSet(testEntity, testRole, testEntity2, testRole2), groups);
    
    groups = testStem.getChildGroups(Scope.ONE, AccessPrivilege.VIEW_PRIVILEGES, queryOptions, GrouperUtil.toSet(TypeOfGroup.entity));
    assertGroupSetsAndOrder(GrouperUtil.toSet(testEntity), groups);

    groups = GrouperDAOFactory.getFactory().getGroup().findAllByApproximateNameSecure(
        "tes", "test:%", queryOptions, null);
    assertGroupSetsAndOrder(GrouperUtil.toSet(testEntity, testGroup, testRole, testEntity2, testGroup2, testRole2), groups);

    groups = GrouperDAOFactory.getFactory().getGroup().findAllByApproximateNameSecure(
        "tes", "test:%", queryOptions, GrouperUtil.toSet(TypeOfGroup.role, TypeOfGroup.entity));
    assertGroupSetsAndOrder(GrouperUtil.toSet(testEntity, testRole, testEntity2, testRole2), groups);

    groups = GrouperDAOFactory.getFactory().getGroup().findAllByApproximateNameSecure(
        "abc", "test:%", queryOptions, GrouperUtil.toSet(TypeOfGroup.role, TypeOfGroup.entity));
    assertGroupSetsAndOrder(null, groups);

    GrouperSession.stopQuietly(grouperSession);
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ3);

    groups = rootStem.getChildGroups(Scope.SUB, AccessPrivilege.VIEW_PRIVILEGES, queryOptions, null);
    assertGroupSetsAndOrder(GrouperUtil.toSet(testRole, testEntity2), groups);
    
    groups = testStem.getChildGroups(Scope.SUB, AccessPrivilege.VIEW_PRIVILEGES, queryOptions, null);
    assertGroupSetsAndOrder(GrouperUtil.toSet(testRole, testEntity2), groups);
    
    groups = testStem.getChildGroups(Scope.ONE, AccessPrivilege.VIEW_PRIVILEGES, queryOptions, null);
    assertGroupSetsAndOrder(GrouperUtil.toSet(testRole), groups);
    
    groups = rootStem.getChildGroups(Scope.SUB, AccessPrivilege.VIEW_PRIVILEGES, queryOptions, GrouperUtil.toSet(TypeOfGroup.group));
    assertGroupSetsAndOrder(null, groups);
    
    groups = testStem.getChildGroups(Scope.SUB, AccessPrivilege.VIEW_PRIVILEGES, queryOptions, GrouperUtil.toSet(TypeOfGroup.role, TypeOfGroup.entity));
    assertGroupSetsAndOrder(GrouperUtil.toSet(testRole, testEntity2), groups);
    
    groups = testStem.getChildGroups(Scope.ONE, AccessPrivilege.VIEW_PRIVILEGES, queryOptions, GrouperUtil.toSet(TypeOfGroup.entity));
    assertGroupSetsAndOrder(null, groups);
    
    groups = GrouperDAOFactory.getFactory().getGroup().findAllByApproximateNameSecure(
        "tes", "test:%", queryOptions, null);
    assertGroupSetsAndOrder(GrouperUtil.toSet(testRole, testEntity2), groups);

    groups = GrouperDAOFactory.getFactory().getGroup().findAllByApproximateNameSecure(
        "tes", "test:%", queryOptions, GrouperUtil.toSet(TypeOfGroup.group, TypeOfGroup.entity));
    assertGroupSetsAndOrder(GrouperUtil.toSet(testEntity2), groups);

    groups = GrouperDAOFactory.getFactory().getGroup().findAllByApproximateNameSecure(
        "abc", "test:%", queryOptions, GrouperUtil.toSet(TypeOfGroup.role, TypeOfGroup.entity));
    assertGroupSetsAndOrder(null, groups);

    
    GrouperSession.stopQuietly(grouperSession);
    
  }
  
  /**
   * 
   */
  public void testGetChildGroups() {
    LOG.info("testGetChildGroups");
    try {
      R         r         = R.populateRegistry(1, 4, 0);
      Stem      nsA       = r.getStem("a");
      Set       children  = nsA.getChildGroups();
      T.amount( "child groups", 4, children.size() );
      Group     child;
      Iterator  it        = children.iterator();
      while (it.hasNext()) {
        child = (Group) it.next();
        assertTrue( "child group has right parent", child.getParentStem().equals(nsA) );
      }
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testGetChildGroups()

  public void testPropagateDisplayExtensionChangeAsRoot() {
    LOG.info("testPropagateExtensionChangeAsRoot");
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemHelper.findRootStem(s);
    Stem            edu   = StemHelper.addChildStem(root, "edu", "education");
    Stem            i2    = StemHelper.addChildStem(edu, "i2", "internet2");
    Stem            uofc  = StemHelper.addChildStem(edu, "uofc", "uchicago");
    Group           bsd   = StemHelper.addChildGroup(uofc, "bsd", "biological sciences division");
    Group           psd   = StemHelper.addChildGroup(uofc, "psd", "physical sciences division");

    String exp = "education";
    Assert.assertTrue("edu displayExtn" , edu.getDisplayExtension().equals(exp));
    Assert.assertTrue("edu displayName" , edu.getDisplayName().equals(exp));
    exp = edu.getDisplayName() + ":internet2";
    Assert.assertTrue("i2 displayName", i2.getDisplayName().equals(exp));
    exp = edu.getDisplayName() + ":uchicago";
    Assert.assertTrue("uofc displayName", uofc.getDisplayName().equals(exp));
    exp = uofc.getDisplayName() + ":biological sciences division";
    Assert.assertTrue("bsd displayName" , bsd.getDisplayName().equals(exp));
    exp = uofc.getDisplayName() + ":physical sciences division";
    Assert.assertTrue("psd displayName" , psd.getDisplayName().equals(exp));
   
    // Now rename
    exp = "higher ed";
    try {
      edu.setDisplayExtension(exp);
      edu.store();
      Assert.assertTrue(
        "mod'd edu displayExtension (" + edu.getDisplayExtension() + ")", 
        edu.getDisplayExtension().equals(exp)
      );
      Assert.assertTrue(
        "mod'd edu displayName (" + edu.getDisplayName() + ")", 
        edu.getDisplayName().equals(exp)
      );
    }
    catch (Exception e) {
      Assert.fail("unable to change stem displayName: " + e.getMessage());
    }
    
    // Now retrieve the children and check them 
    Stem i2R = StemHelper.findByName(s, i2.getName());
    exp = edu.getDisplayName() + ":internet2";
    Assert.assertTrue(
      "mod'd i2 displayName=(" + i2R.getDisplayName() + ") (" + exp + ")", 
      i2R.getDisplayName().equals(exp)
    );

    exp = edu.getDisplayName() + ":uchicago";
    Stem  uofcR = StemHelper.findByName(s, uofc.getName());
    Assert.assertTrue(
      "mod'd uofc displayName=(" + uofcR.getDisplayName() + ") (" + exp + ")", 
      uofcR.getDisplayName().equals(exp)
    );
    exp = uofcR.getDisplayName() + ":biological sciences division";
    Group bsdR  = GroupHelper.findByName(s, bsd.getName());
    Assert.assertTrue(
      "mod'd bsd edu displayName=(" + bsdR.getDisplayName() + ") (" + exp + ")", 
      bsdR.getDisplayName().equals(exp)
    );
    exp = uofcR.getDisplayName() + ":physical sciences division";
    Group psdR  = GroupHelper.findByName(s, psd.getName());
    Assert.assertTrue(
      "mod'd psd edu displayName=(" + psdR.getDisplayName() + ") (" + exp + ")", 
      psdR.getDisplayName().equals(exp)
    );

  } // public void testPropagateExtensionChangeAsRoot()

  public void testPropagateDisplayExtensionChangeAsNonRoot() {
    LOG.info("testPropagateExtensionChangeAsNonRoot");
    try {
      // Create stems + groups as root
      GrouperSession  s     = SessionHelper.getRootSession();
      Stem            root  = StemHelper.findRootStem(s);
      Stem            edu   = StemHelper.addChildStem(root, "edu", "education");
      Stem            i2    = StemHelper.addChildStem(edu, "i2", "internet2");
      Stem            uofc  = StemHelper.addChildStem(edu, "uofc", "uchicago");
      Group           bsd   = StemHelper.addChildGroup(uofc, "bsd", "biological sciences division");
      Group           psd   = StemHelper.addChildGroup(uofc, "psd", "physical sciences division");
      // Grant subj0 STEM on edu
      PrivHelper.grantPriv(s, edu, SubjectTestHelper.SUBJ0, NamingPrivilege.STEM);
      // And revoke VIEW + READ from ALL on one of the child groups
      PrivHelper.revokePriv(s, psd, SubjectTestHelper.SUBJA, AccessPrivilege.VIEW);
      PrivHelper.revokePriv(s, psd, SubjectTestHelper.SUBJA, AccessPrivilege.READ);
   
      // Now rename as subj0
      GrouperSession  nrs   = SessionHelper.getSession(SubjectTestHelper.SUBJ0_ID);
      Stem            eduNR = StemHelper.findByName(nrs, edu.getName());
   
      // Now rename
      String exp = "higher ed";
      try {
        eduNR.setDisplayExtension(exp);
        eduNR.store();
        Assert.assertTrue(
        "mod'd edu displayExtension (" + eduNR.getDisplayExtension() + ")", 
        eduNR.getDisplayExtension().equals(exp)
        );
        Assert.assertTrue(
          "mod'd edu displayName (" + edu.getDisplayName() + ")", 
          eduNR.getDisplayName().equals(exp)
        );
      }
      catch (Exception e) {
        Assert.fail("unable to change stem displayName: " + e.getMessage());
      }
    
      // Now retrieve the children and check them 
      Stem i2R = StemHelper.findByName(nrs, i2.getName());
      exp = eduNR.getDisplayName() + ":internet2";
      Assert.assertTrue(
        "mod'd i2 displayName=(" + i2R.getDisplayName() + ") (" + exp + ")", 
        i2R.getDisplayName().equals(exp)
      );

      exp = eduNR.getDisplayName() + ":uchicago";
      Stem  uofcR = StemHelper.findByName(nrs, uofc.getName());
      Assert.assertTrue(
        "mod'd uofc displayName=(" + uofcR.getDisplayName() + ") (" + exp + ")", 
        uofcR.getDisplayName().equals(exp)
      );
      exp = uofcR.getDisplayName() + ":biological sciences division";
      Group bsdR  = GroupHelper.findByName(nrs, bsd.getName());
      Assert.assertTrue(
        "mod'd bsd edu displayName=(" + bsdR.getDisplayName() + ") (" + exp + ")", 
        bsdR.getDisplayName().equals(exp)
      );

      // Check this one as root as subj0 doesn't have READ or VIEW
      exp = uofcR.getDisplayName() + ":physical sciences division";
      Group psdR  = GroupHelper.findByName(s, psd.getName());
      Assert.assertTrue(
        "mod'd psd edu displayName=(" + psdR.getDisplayName() + ") (" + exp + ")", 
        psdR.getDisplayName().equals(exp)
      );
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testPropagateExtensionChangeAsNonRoot()

  public void testChildStemsAndGroupsLazyInitialization() {
    LOG.info("testChildStemsAndGroupsLazyInitialization");
    try {
      String          edu   = "edu";
      String          uofc  = "uofc";
      String          bsd   = "bsd";

      GrouperSession  s     = GrouperSession.startRootSession();
      Subject         subj  = SubjectFinder.findById("GrouperSystem", true);
      Stem            root  = StemFinder.findRootStem(s);
      Stem            ns0   = root.addChildStem(edu, edu);
      Stem            ns1   = ns0.addChildStem(uofc, uofc);
      ns1.addChildGroup(bsd, bsd);
      s.stop();

      s = GrouperSession.start(subj);
      Stem  a         = StemFinder.findByName(s, edu, true);
      Set   children  = a.getChildStems();
      Assert.assertTrue("has child stems", children.size() > 0);
      Iterator iter = children.iterator();
      while (iter.hasNext()) {
        Stem child  = (Stem) iter.next();
        Set  stems  = child.getChildStems();
        Assert.assertTrue("child has no child stems", stems.size() == 0);
        Set  groups = child.getChildGroups();
        Assert.assertTrue("child has child groups", groups.size() == 1);
        Iterator gIter = groups.iterator();
        while (gIter.hasNext()) {
          Group g = (Group) gIter.next();
          Assert.assertNotNull("group name", g.getName());
        }
      }
      s.stop();

      Assert.assertTrue("no exceptions", true);
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testChildStemsAndGroupsLazyInitialization() 

  public void testParentChildStemsAndGroupsLazyInitialization() throws Exception {
    LOG.info("testParentChildStemsAndGroupsLazyInitialization");

    String          eduStemName   = "edu";
    String          uofcStemName  = "uofc";
    String          uofcGroupBsdName   = "bsd";

    GrouperSession  grouperSession     = GrouperSession.startRootSession();
    Subject         subj  = SubjectFinder.findById("GrouperSystem", true);

    Stem            rootStem  = StemFinder.findRootStem(grouperSession);
    Stem            eduStem   = rootStem.addChildStem(eduStemName, eduStemName);
    Stem            uofcStem   = eduStem.addChildStem(uofcStemName, uofcStemName);
    uofcStem.addChildGroup(uofcGroupBsdName, uofcGroupBsdName);
    grouperSession.stop();

    grouperSession = GrouperSession.start(subj);
    Stem  eduStem2         = StemFinder.findByName(grouperSession, eduStemName, true);
    Stem  rootStem2    = eduStem2.getParentStem();
    Set   rootStemChildrenStems  = rootStem2.getChildStems();
    Assert.assertTrue("root stem has child stems: " + rootStemChildrenStems.size(), rootStemChildrenStems.size() >= 1);
    Iterator rootStemChildrenStemsIter = rootStemChildrenStems.iterator();
    while (rootStemChildrenStemsIter.hasNext()) {
      
      Stem rootStemChildStem  = (Stem) rootStemChildrenStemsIter.next();
      
      if (!StringUtils.equals(rootStemChildStem.getName(), eduStemName) 
          && !(StringUtils.equals(rootStemChildStem.getName(), uofcStemName))) {
        //maybe another stem exists...
        continue;
      }
      
      Set  rootStemGrandchildStems  = rootStemChildStem.getChildStems();
      assertEquals(
        "root child " + rootStemChildStem.getName() + " shoudl have 1 child stem", 1, rootStemGrandchildStems.size());
      Iterator grandchildStemIter = rootStemGrandchildStems.iterator();
      while (grandchildStemIter.hasNext()) {
        Stem grandchildStem = (Stem) grandchildStemIter.next();
        Assert.assertEquals(
          "grandchild " + grandchildStem.getName() + " has no child stems",
            0, grandchildStem.getChildStems().size()
          );
          Assert.assertTrue(
            "grandchild " + grandchildStem.getName() + " has child groups",
          grandchildStem.getChildGroups().size() == 1
        );
        Iterator gIter = grandchildStem.getChildGroups().iterator();
        while (gIter.hasNext()) {
          Group g = (Group) gIter.next();
          Assert.assertNotNull("group name", g.getName());
        }
      }
      Set  groups = rootStemChildStem.getChildGroups();
      Assert.assertTrue(
        "child of parent has no child groups", groups.size() == 0
      );
    }
    grouperSession.stop();

    Assert.assertTrue("no exceptions", true);
  } 

  public void testAddChildStemWithBadExtnOrDisplayExtn() {
    LOG.info("testAddChildStemWithBadExtnOrDisplayExtn");
    try {
      GrouperSession  s     = SessionHelper.getRootSession();
      Stem            root  = StemFinder.findRootStem(s);
      try {
        root.addChildStem(null, "test");
        Assert.fail("added stem with null extn");
      }
      catch (StemAddException eSA) {
        Assert.assertTrue("null extn", true);
      }
      try {
        root.addChildStem("", "test");
        Assert.fail("added stem with empty extn");
      }
      catch (StemAddException eSA) {
        Assert.assertTrue("empty extn", true);
      }
      try {
        root.addChildStem("a:test", "test");
        Assert.fail("added stem with colon-containing extn");
      }
      catch (StemAddException eSA) {
        Assert.assertTrue("colon-containing extn", true);
      }
      try {
        root.addChildStem("test", null);
        Assert.fail("added stem with null displayExtn");
      }
      catch (StemAddException eSA) {
        Assert.assertTrue("null displayExtn", true);
      }
      try {
        root.addChildStem("test", "");
        Assert.fail("added stem with empty displayextn");
      }
      catch (StemAddException eSA) {
        Assert.assertTrue("empty displayExtn", true);
      }
      try {
        root.addChildStem("test", "a:test");
        Assert.fail("added stem with colon-containing displayExtn");
      }
      catch (StemAddException eSA) {
        Assert.assertTrue("colon-containing displayExtn", true);
      }
      s.stop();
    }
    catch (Exception e) {
      Assert.fail(e.getMessage() + ", " + ExceptionUtils.getFullStackTrace(e));
    }
  } // public void testAddChildStemWithBadExtnOrDisplayExtn()

  public void testSetBadStemDisplayExtension() {
    LOG.info("testSetBadStemDisplayExtension");
    try {
      GrouperSession  s     = SessionHelper.getRootSession();
      Stem            root  = StemFinder.findRootStem(s);
      Stem            edu   = root.addChildStem("edu", "edu");
      try {
        edu.setDisplayExtension(null);
        edu.store();
        Assert.fail("set null displayExtn");
      }
      catch (StemModifyException eSA) {
        Assert.assertTrue("null displayExtn", true);
      }
      try {
        edu.setDisplayExtension("");
        edu.store();
        Assert.fail("set empty displayExtn");
      }
      catch (StemModifyException eSA) {
        Assert.assertTrue("empty displayExtn", true);
      }
      try {
        edu.setDisplayExtension("a:test");
        edu.store();
        Assert.fail("set colon-containing displayExtn");
      }
      catch (StemModifyException eSA) {
        Assert.assertTrue("colon-containing displayExtn", true);
      }
      s.stop();
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  } // public void testSetBadStemDisplayExtension()

  /**
   * test delete
   */
  public void testDeleteEmptyStem() {
    LOG.info("testDeleteEmptyStem");
    try {
      R r = R.populateRegistry(0, 0, 0);
      r.ns.delete();
      Assert.assertTrue("deleted stem", true);
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testDeleteEmptyStem()

  /**
   * 
   */
  public void testDeleteWithAttrPrivs() {
    Subject subj0 = SubjectTestHelper.SUBJ0;
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignName("test").save();
    
    stem.grantPriv(subj0, NamingPrivilege.STEM_ATTR_READ);
    stem.grantPriv(subj0, NamingPrivilege.STEM_ATTR_UPDATE);
    
    stem.delete();
  }
  
  public void testDeleteEmptyStemInNewSession() {
    LOG.info("testDeleteEmptyStemInNewSession");
    try {
      R       r     = R.populateRegistry(0, 0, 0);
      String  name  = r.ns.getName();
      Subject subj  = r.rs.getSubject();
      r.rs.stop();
  
      // Now reload and delete
      GrouperSession  s   = GrouperSession.start(subj);
      Stem            ns  = StemFinder.findByName(s, name, true);
      ns.delete();
      Assert.assertTrue("no lazy initialization error", true);
      s.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testDeleteEmptyStemInNewSession()

  public void testFailToDeleteRootStem() {
    LOG.info("testFailToDeleteRootStem");
    try {
      R r = R.populateRegistry(0, 0, 0);
      r.root.delete();
      r.rs.stop();
      Assert.fail("deleted root stem");
    }
    catch (StemDeleteException eSD) {
      Assert.assertTrue("OK: failed to delete root stem", true);
    }
    catch (Exception e) {
      Assert.fail("unexpected exception: " + e.getMessage());
    }
  } // public void testFailToDeleteRootStem()

  public void testFailToDeleteStemWithChildGroups() {
    LOG.info("testFailToDeleteStemWithChildGroups");
    try {
      R r = R.populateRegistry(1, 1, 0);
      Stem ns = r.getStem("a");
      ns.delete();
      Assert.fail("deleted stem with child groups");
      r.rs.stop();
    }
    catch (StemDeleteException eSD) {
      Assert.assertTrue("OK: failed to delete stem with child groups", true);
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailToDeleteStemWithChildGroups()

  public void testFailToDeleteStemWithChildStems() {
    LOG.info("testFailToDeleteStemWithChildStems");
    try {
      R r = R.populateRegistry(1, 0, 0);
      r.ns.delete();
      Assert.fail("deleted stem with child stems");
      r.rs.stop();
    }
    catch (StemDeleteException eSD) {
      Assert.assertTrue("OK: failed to delete stem with child stems", true);
    }
    catch (Exception e) {
      Assert.fail("unexpected exception: " + e.getMessage());
    }
  } // public void testFailToDeleteStemWithChildStems()

  public void testFailToDeleteStemWithoutSTEM() {
    LOG.info("testFailToDeleteStemWithoutSTEM");
    try {
      R               r     = R.populateRegistry(1, 0, 1);
      Subject         subj  = r.getSubject("a");
      GrouperSession  s     = GrouperSession.start(subj);
      r.ns.delete();
      s.stop();
      r.rs.stop();
      Assert.fail("deleted stem without STEM");
    }
    catch (InsufficientPrivilegeException eIP) {
      Assert.assertTrue("OK: failed to delete stem without STEM", true);
    }
    catch (Exception e) {
      Assert.fail("unexpected exception: " + e.getMessage());
    }
  } // public void testFailToDeleteStemWithoutSTEM()

  public void testGetCreateAttrs() {
    LOG.info("testGetCreateAttrs");
    try {
      GrouperSession  s     = SessionHelper.getRootSession();
      Stem            root  = StemHelper.findRootStem(s);
      Stem            edu   = root.addChildStem("edu", "education");
      try {
        Subject creator = edu.getCreateSubject();
        Assert.assertNotNull("creator !null", creator);
        Assert.assertTrue("creator", creator.equals(s.getSubject()));
      }
      catch (SubjectNotFoundException eSNF) {
        Assert.fail("no create subject");
      }
      Date  d       = edu.getCreateTime();
      Assert.assertNotNull("create time !null", d);
      Assert.assertTrue("create time instanceof Date", d instanceof Date);
      long  create  = d.getTime();
      long  epoch   = new Date(0).getTime();
      Assert.assertFalse(
        "create[" + create + "] != epoch[" + epoch + "]",
        create == epoch
      );
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testGetCreateAttrs()

  public void testGetModifyAttrsModified() {
    LOG.info("testGetModifyAttrsModified");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("stems.updateLastMembershipTime", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.updateLastMembershipTime", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.updateLastImmediateMembershipTime", "true");

    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemHelper.findRootStem(s);
    Stem            edu   = StemHelper.addChildStem(root, "edu", "education");
    StemHelper.addChildGroup(edu, "i2", "internet2");
    edu = StemFinder.findByName(s, edu.getName(), true);
  
    try {
      Subject modifier = edu.getModifySubject();
      Assert.fail("no exception thrown");
    }
    catch (SubjectNotFoundException eSNF) {
    }
    
    Date  d       = edu.getModifyTime();
    Assert.assertTrue("modify time null", d.getTime() == 0);
    
    edu.grantPriv(SubjectFinder.findRootSubject(), NamingPrivilege.STEM_ADMIN);
    edu = StemFinder.findByName(s, edu.getName(), true, new QueryOptions().secondLevelCache(false));
    
    d = edu.getLastMembershipChange();
    
    Assert.assertNotNull("last membership change !null: " + d, d);
    Assert.assertTrue("last membership change instanceof Date", d instanceof Date);
    
    long  modify  = d.getTime();
    long  epoch   = new Date(0).getTime();
    Assert.assertFalse(
      "modify[" + modify + "] != epoch[" + epoch + "]",
      modify == epoch
    );
  } // public void testGetModifyAttrsModified()

  public void testGetModifyAttrsNotModified() {

    LOG.info("testGetModifyAttrsNotModified");

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("stems.updateLastMembershipTime", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.updateLastMembershipTime", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.updateLastImmediateMembershipTime", "true");

    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemHelper.findRootStem(s);
    Stem            edu   = StemHelper.addChildStem(root, "edu", "education");
    edu = StemFinder.findByName(s, edu.getName(), true);
    
    try {
      Subject modifier = edu.getModifySubject();
      Assert.fail("no exception thrown");
    }
    catch (SubjectNotFoundException eSNF) {
    }
    Date  d       = edu.getModifyTime();
    Assert.assertTrue("modify time null", d.getTime() == 0);
    
    edu.grantPriv(SubjectFinder.findRootSubject(), NamingPrivilege.STEM_ADMIN);
    edu = StemFinder.findByName(s, edu.getName(), true, new QueryOptions().secondLevelCache(false));

    d = edu.getLastMembershipChange();
    Assert.assertNotNull("last membership change !null: " + d, d);
    Assert.assertTrue("last membership change instanceof Date", d instanceof Date);
    long  modify  = d.getTime();
    long  epoch   = new Date(0).getTime();
    Assert.assertFalse(
      "modify[" + modify + "] != epoch[" + epoch + "]",
      modify == epoch
    );
  } // public void testGetModifyAttrsNotModified()

  // BUGFIX:GCODE:10
  public void testGetPrivsStemmersAndCreatorsAsNonRoot() {
    LOG.info("testGetPrivsStemmersAndCreatorsAsNonRoot");
    try {
      final R r = R.populateRegistry(0, 0, 1);
      final Subject subjA = r.getSubject("a");
      GrouperSession s = GrouperSession.start(subjA);
  
  
      T.amount("privs before grant"   , 0, r.ns.getPrivs(subjA).size());
      T.amount("stemmers before grant", 0, r.ns.getStemmers().size()  );
      T.amount("stemAdmins before grant", 0, r.ns.getStemAdmins().size()  );
      T.amount("creators before grant", 0, r.ns.getCreators().size()  );
  
      GrouperSession.callbackGrouperSession(r.rs, new GrouperSessionHandler() {
  
        public Object callback(GrouperSession grouperSession)
            throws GrouperSessionException {
          try {
            r.ns.grantPriv(subjA, NamingPrivilege.STEM);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
          return null;
        }
        
      });
  
      T.amount("privs after grant"    , 1, r.ns.getPrivs(subjA).size());
      T.amount("stemmers after grant" , 1, r.ns.getStemmers().size()  );
      T.amount("stemAdmins after grant" , 1, r.ns.getStemAdmins().size()  );
      T.amount("creators after grant" , 0, r.ns.getCreators().size()  );
  
      s.stop();
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testGetPrivsStemmersAndCreatorsAsNonRoot()

  public void testGetPrivsStemmersAndCreatorsAsRoot() {
    LOG.info("testGetPrivsStemmersAndCreatorsAsRoot");
    try {
      R       r     = R.populateRegistry(0, 0, 1);
      Subject subjA = r.getSubject("a");
      T.amount("privs before grant"   , 0, r.ns.getPrivs(subjA).size());
      T.amount("stemmers before grant", 0, r.ns.getStemmers().size()  );
      T.amount("stemAdmins before grant", 0, r.ns.getStemAdmins().size()  );
      T.amount("creators before grant", 0, r.ns.getCreators().size()  );
      r.ns.grantPriv(subjA, NamingPrivilege.STEM);
      T.amount("privs after grant"    , 1, r.ns.getPrivs(subjA).size());
      T.amount("stemmers after grant" , 1, r.ns.getStemmers().size()  );
      T.amount("stemAdmins after grant" , 1, r.ns.getStemAdmins().size()  );
      T.amount("creators after grant" , 0, r.ns.getCreators().size()  );
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testGetPrivsStemmersAndCreatorsAsRoot()

  
  public void testStemSaveRunAsRoot() {
    
    Stem stem = new StemSave().assignRunAsRoot(true).assignName("test78:test79")
      .assignCreateParentStemsIfNotExist(true).save();
    
    GrouperSession rootSession = SessionHelper.getRootSession();
    Stem foundStem = StemFinder.findByName(rootSession, "test78:test79", true);
    
    assertTrue(foundStem != null);
    
  }
  
  /**
   * test static save stem
   * @throws Exception if problem
   */
  public void testStaticSaveStem() throws Exception {
    
    R.populateRegistry(1, 2, 0);
    
    String displayExtension = "testing123 display";
    GrouperSession rootSession = SessionHelper.getRootSession();
    String stemDescription = "description";
    try {
      String stemNameNotExist = "whatever123:whatever:testing123";
      
      GrouperTest.deleteAllStemsIfExists(rootSession, stemNameNotExist);
      
      Stem.saveStem(rootSession, stemNameNotExist,null,  stemNameNotExist, 
          displayExtension, stemDescription, 
          SaveMode.UPDATE, false);
      fail("this should fail, since stem doesnt exist");
    } catch (StemNotFoundException e) {
      //good, caught an exception
      //e.printStackTrace();
    }
    
    /////////////////////////////////
    String stemName = "i2:a:testing123";
    GrouperTest.deleteStemIfExists(rootSession, stemName);
    
    //////////////////////////////////
    //this should insert
    Stem createdStem = Stem.saveStem(rootSession, null, null, 
        stemName,displayExtension, stemDescription, 
        SaveMode.INSERT, false);
    
    //now retrieve
    Stem foundStem = StemFinder.findByName(rootSession, stemName, true);
    
    assertEquals(stemName, createdStem.getName());
    assertEquals(stemName, foundStem.getName());
    
    assertEquals(displayExtension, createdStem.getDisplayExtension());
    assertEquals(displayExtension, foundStem.getDisplayExtension());
    
    assertEquals(stemDescription, createdStem.getDescription());
    assertEquals(stemDescription, foundStem.getDescription());
    
    ///////////////////////////////////
    //this should update by uuid
    createdStem = Stem.saveStem(rootSession, stemName, createdStem.getUuid(),
        stemName, displayExtension, stemDescription + "1", 
         SaveMode.INSERT_OR_UPDATE, false);
    assertEquals("this should update by uuid", stemDescription + "1", createdStem.getDescription());
    
    //this should update by name
    createdStem = Stem.saveStem(rootSession, stemName, null, stemName, 
        displayExtension, stemDescription + "2", 
        SaveMode.UPDATE, false);
    assertEquals("this should update by name", stemDescription + "2", createdStem.getDescription());
    
    /////////////////////////////////////
    //create a stem that creates a bunch of stems
    String stemsNotExist = "whatever123:heythere:another";
    //lets also delete those stems
    GrouperTest.deleteAllStemsIfExists(rootSession, stemsNotExist);
    createdStem = Stem.saveStem(rootSession, stemsNotExist, null, 
        stemsNotExist, displayExtension, stemDescription, 
        SaveMode.INSERT_OR_UPDATE, true);
    
    assertEquals(stemDescription, createdStem.getDescription());
    //clean up
    GrouperTest.deleteAllStemsIfExists(rootSession, stemsNotExist);
    
    rootSession.stop();
    
  }

  public void testStemModifyAttributesAfterUpdatingAttributes() {
    LOG.info("testStemModifyAttributesAfterUpdatingAttributes");
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("stems.updateLastMembershipTime", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.updateLastMembershipTime", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.updateLastImmediateMembershipTime", "true");

    try {
      R       r     = R.populateRegistry(1, 1, 1);
      Stem    nsA   = r.getStem("a");
      
      Thread.sleep(1);

      nsA.grantPriv(SubjectFinder.findRootSubject(), NamingPrivilege.STEM_ADMIN);
      nsA = StemFinder.findByName(GrouperSession.staticGrouperSession(), nsA.getName(), true, new QueryOptions().secondLevelCache(false));

      long    orig  = nsA.getModifyTime().getTime();
      long    pre   = new java.util.Date().getTime();
      Thread.sleep(1);
      nsA.setDescription("test");
      nsA.store();
      Thread.sleep(1);
      nsA = StemFinder.findByName(GrouperSession.staticGrouperSession(), nsA.getName(), true, new QueryOptions().secondLevelCache(false));
      long    mtime = nsA.getModifyTime().getTime();


      long    mtime_mem = nsA.getLastMembershipChange().getTime();
  
      assertTrue( "nsA modify time updated (" + mtime + " > " + orig + ")", mtime > orig );
      assertTrue( "nsA last membership time < pre (" + mtime_mem + " < " + pre + ")", mtime_mem < pre );
      
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  }

  public void testStemModifyAttributesUpdatedAfterGrantingEffectivePriv() {
    LOG.info("testStemModifyAttributesUpdatedAfterGrantingEffectivePriv");
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("stems.updateLastMembershipTime", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.updateLastMembershipTime", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.updateLastImmediateMembershipTime", "true");

    try {
      R       r     = R.populateRegistry(1, 1, 1);
      Stem    nsA   = r.getStem("a");
      Subject subjA = r.getSubject("a");
      Group   gA    = r.getGroup("a", "a");
  
      nsA.grantPriv(gA.toSubject(), NamingPrivilege.STEM);
      
      Thread.sleep(1);
      long    orig  = nsA.getModifyTime().getTime();
      long    pre   = new java.util.Date().getTime();
      Thread.sleep(1);
      gA.addMember(subjA);
      Thread.sleep(1);
      long    post  = new java.util.Date().getTime();
      nsA = StemFinder.findByName(r.rs, nsA.getName(), true);
      long    mtime = nsA.getModifyTime().getTime();
      long    mtime_mem = nsA.getLastMembershipChange().getTime();
  
      assertTrue( "nsA modify time not updated (" + mtime + " == " + orig + ")", mtime == orig );
      assertTrue( "nsA last membership time >= pre (" + mtime_mem + " >= " + pre + ")", mtime_mem >= pre );
      assertTrue( "nsA last membership time <= post (" + mtime_mem + " <= " + post + ")", mtime_mem <= post );
      
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  }

  // NAMING PRIVS //
  
  // @since   1.2.0
  public void testStemModifyAttributesUpdatedAfterGrantingImmediatePriv() {
    
    LOG.info("testStemModifyAttributesUpdatedAfterGrantingImmediatePriv");

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("stems.updateLastMembershipTime", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.updateLastMembershipTime", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.updateLastImmediateMembershipTime", "true");

    try {
      R       r     = R.populateRegistry(1, 0, 1);
      Stem    nsA   = r.getStem("a");
      Subject subjA = r.getSubject("a");
  
      long    orig  = nsA.getModifyTime().getTime();
      long    pre   = new java.util.Date().getTime();
      assertTrue(nsA.grantPriv(subjA, NamingPrivilege.STEM, true));
      long    post  = new java.util.Date().getTime();
      nsA = StemFinder.findByName(r.rs, nsA.getName(), true);
      long    mtime = nsA.getModifyTime().getTime();
      long    mtime_mem = nsA.getLastMembershipChange().getTime();
  
      assertTrue( "nsA modify time not updated (" + mtime + " == " + orig + ")", mtime == orig );
      assertTrue( "nsA last membership time >= pre (" + mtime_mem + " >= " + pre + ")", mtime_mem >= pre );
      assertTrue( "nsA last membership time <= post (" + mtime_mem + " <= " + post + ")", mtime_mem <= post );
  
      try {
        nsA.grantPriv(subjA, NamingPrivilege.STEM);
        fail("Should throw already exists exception");
      } catch (GrantPrivilegeException gpe) {
        
      }
  
      assertFalse(nsA.grantPriv(subjA, NamingPrivilege.STEM, false));
      
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  }

  public void testStemModifyAttributesUpdatedAfterRevokingEffectivePriv() {
    LOG.info("testStemModifyAttributesUpdatedAfterRevokingEffectivePriv");

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("stems.updateLastMembershipTime", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.updateLastMembershipTime", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.updateLastImmediateMembershipTime", "true");

    try {
      R       r     = R.populateRegistry(1, 1, 1);
      Stem    nsA   = r.getStem("a");
      Subject subjA = r.getSubject("a");
      Group   gA    = r.getGroup("a", "a");
  
      nsA.grantPriv(gA.toSubject(), NamingPrivilege.STEM);
      gA.addMember(subjA);
      
      Thread.sleep(1);
      long    orig  = nsA.getModifyTime().getTime();
      long    pre   = new java.util.Date().getTime();
      Thread.sleep(1);
      gA.deleteMember(subjA);
      Thread.sleep(1);
      long    post  = new java.util.Date().getTime();
      nsA = StemFinder.findByName(r.rs, nsA.getName(), true);
      long    mtime = nsA.getModifyTime().getTime();
      long    mtime_mem = nsA.getLastMembershipChange().getTime();
  
      assertTrue( "nsA modify time not updated (" + mtime + " == " + orig + ")", mtime == orig );
      assertTrue( "nsA last membership time >= pre (" + mtime_mem + " >= " + pre + ")", mtime_mem >= pre );
      assertTrue( "nsA last membership time <= post (" + mtime_mem + " <= " + post + ")", mtime_mem <= post );
      
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  }

  // @since   1.2.0
  public void testStemModifyAttributesUpdatedAfterRevokingImmediatePriv() {
    LOG.info("testStemModifyAttributesUpdatedAfterRevokingImmediatePriv");

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("stems.updateLastMembershipTime", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.updateLastMembershipTime", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.updateLastImmediateMembershipTime", "true");

    try {
      R       r     = R.populateRegistry(1, 0, 1);
      Stem    nsA   = r.getStem("a");
      Subject subjA = r.getSubject("a");
      nsA.grantPriv(subjA, NamingPrivilege.STEM);
  
      long    orig  = nsA.getModifyTime().getTime();
      long    pre   = new java.util.Date().getTime();
      Thread.sleep(1); // TODO 20070430 hack!
      nsA.revokePriv(subjA, NamingPrivilege.STEM);
  
      //try again
      try {
        nsA.revokePriv(subjA, NamingPrivilege.STEM);
        fail("Problem revoking priv");
      } catch (RevokePrivilegeAlreadyRevokedException rpare) {
        //good
      }
  
      nsA.revokePriv(subjA, NamingPrivilege.STEM, false);
      nsA = StemFinder.findByName(r.rs, nsA.getName(), true);
  
      Thread.sleep(1); // TODO 20070430 hack!
      long    post  = new java.util.Date().getTime();
      long    mtime = nsA.getModifyTime().getTime();
      long    mtime_mem = nsA.getLastMembershipChange().getTime();
  
      assertTrue( "nsA modify time not updated (" + mtime + " == " + orig + ")", mtime == orig );
      assertTrue( "nsA membership change time >= pre (" + mtime_mem + " >= " + pre + ")", mtime_mem >= pre );
      assertTrue( "nsA membership change time <= post (" + mtime_mem + " <= " + post + ")", mtime_mem <= post );
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  }

  public void testAddChildGroup() {
    Stem  root  = StemHelper.findRootStem(
      SessionHelper.getRootSession()
    );
    Stem  edu   = StemHelper.addChildStem(root, "edu", "education");
    StemHelper.addChildGroup(edu, "i2", "internet2");
  } // public void testAddChildGroup()

  // Tests
  
  public void testAddChildGroupAtRootFail() {
    Stem  root  = StemHelper.findRootStem(
      SessionHelper.getRootSession()
    );
    StemHelper.addChildGroupFail(root, "i2", "internet2");
  } // public void testAddChildGroupAtRootFail()

  public void testAddChildStem() {
    Stem  root  = StemHelper.findRootStem(
      SessionHelper.getRootSession()
    );
    Stem  edu   = StemHelper.addChildStem(root, "edu", "education");
    StemHelper.addChildStem(edu, "uofc", "uchicago");
  } // public void testAddChildStem()

  public void testAddChildStemAtRoot() {
    Stem  root  = StemHelper.findRootStem(
      SessionHelper.getRootSession()
    );
    StemHelper.addChildStem(root, "edu", "education");
  } // public void testAddChildStemAtRoot()

  public void testAddDuplicateChildGroup() {
    Stem  root  = StemHelper.findRootStem(
      SessionHelper.getRootSession()
    );
    Stem  edu   = StemHelper.addChildStem(root, "edu", "education");
    StemHelper.addChildGroup(edu, "i2", "internet2");
    StemHelper.addChildGroupFail(edu, "i2", "internet2");
  } // public void testAddDuplicateChildGroup()

  public void testAddDuplicateChildStem() {
    Stem  root  = StemHelper.findRootStem(
      SessionHelper.getRootSession()
    );
    Stem  edu   = StemHelper.addChildStem(root, "edu", "education");
    StemHelper.addChildStem(edu, "uofc", "uchicago");
    StemHelper.addChildStemFail(edu, "uofc", "uchicago");
  } // public void testAddDuplicateChildStem()

  public void testSetExtension_EmptyValue() {
    try {
      LOG.info("testSetExtension_EmptyValue");
      Stem ns = new Stem();
      ns.setExtension(GrouperConfig.EMPTY_STRING);
      ns.store();
      fail("should have thrown StemModifyException");
    }
    catch (StemModifyException eNSM) {
      assertTrue(true);
      assertEquals( NamingValidator.E_WS, eNSM.getMessage() );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testSetExtension_EmptyValue()

  // TESTS //  
  
  public void testSetExtension_NullValue() {
    try {
      LOG.info("testSetExtension_NullValue");
      Stem ns = new Stem();
      ns.setExtension(null);
      ns.store();
      fail("should have thrown StemModifyException");
    }
    catch (StemModifyException eNSM) {
      assertTrue(true);
      assertEquals( E.ATTR_NULL, eNSM.getMessage() );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testSetExtension_NullValue()

  public void testSetExtension_ValueContainsColon() {
    try {
      LOG.info("testSetExtension_ValueContainsColon");
      Stem ns = new Stem();
      ns.setExtension("co:on");
      ns.store();
      fail("should have thrown StemModifyException");
    }
    catch (StemModifyException eNSM) {
      assertTrue(true);
      assertEquals( E.ATTR_COLON, eNSM.getMessage() );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testSetExtension_ValueContainsColon()

  public void testSetExtension_WhitespaceOnlyValue() {
    try {
      LOG.info("testSetExtension_WhitespaceOnlyValue");
      Stem ns = new Stem();
      ns.setExtension(" ");
      ns.store();
      fail("should have thrown StemModifyException");
    }
    catch (StemModifyException eNSM) {
      assertTrue(true);
      assertEquals( NamingValidator.E_WS, eNSM.getMessage() );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  }

  // TESTS //  
  
  public void testIsRootStem_DoNotThrowNullPointerException() {
    try {
      LOG.info("testIsRootStem_DoNotThrowNullPointerException");
      Stem ns = new Stem();
      assertFalse( ns.isRootStem() );
    }
    catch (NullPointerException eNP) {
      fail( "threw NullPointerException: " + eNP.getMessage() );
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testSetExtension_NullValue()

  // @source  Gary Brown, 20051221, <B96A40BBB6DC736573C06C6D@cse-gwb.cse.bris.ac.uk>
  // @status  fixed
  public void testSetStemDisplayName() {
    LOG.info("testSetStemDisplayName");
    // Setup
    Subject subj0 = SubjectTestHelper.SUBJ0;
    try {
      GrouperSession  s     = SessionHelper.getRootSession();
      Stem            root  = StemFinder.findRootStem(s);
      Stem            qsuob = root.addChildStem("qsuob", "qsuob");
      qsuob.grantPriv(subj0, NamingPrivilege.STEM);
      Stem            cs    = qsuob.addChildStem("cs", "child stem");
      // These weren't explicitly listed in the test report but I can't
      // replicate unless I have at least two groups.
      qsuob.addChildGroup("cg", "child group");
      cs.addChildGroup("gcg", "grandchild group");
      s.stop();
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
    // Test
    try {
      GrouperSession  nrs   = GrouperSession.start(subj0);
      Stem            qsuob = StemFinder.findByName(nrs, "qsuob", true);
      String          de    = "QS University of Bristol";
      qsuob.setDisplayExtension(de);
      qsuob.store();
      String          val   = qsuob.getDisplayExtension();
      Assert.assertTrue("updated displayExtn: " + val, de.equals(val));
      val                   = qsuob.getDisplayName();
      Assert.assertTrue("updated displayName: " + val, de.equals(val));
      nrs.stop();
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  } // public void testStemDisplayName()

  // @source  Gary Brown, 20051212, <04A762113806B3F6EDBFD2F8@cse-gwb.cse.bris.ac.uk>>
  // @status  fixed
  public void testChildStemsLazyInitializationException() {
    LOG.info("testChildStemsLazyInitializationException");
    try {
      GrouperSession  s     = GrouperSession.startRootSession();
      Subject         subj  = SubjectFinder.findById("GrouperSystem", true);

      Stem  root  = StemFinder.findRootStem(s);
      root.addChildStem("qsuob", "qsuob");
      s.stop();
  
      s = GrouperSession.start(subj);
      Stem  a         = StemFinder.findByName(s,"qsuob", true);
      a.getChildStems();
      s.stop();
  
      Assert.assertTrue("no exceptions", true);
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  } // public void testChildStemsLazyInitializationException() 


  /**
   * NOTE, THIS ASSUMES CACHING IS ENABLED IN THE GROUPER HIBERNATE PROPERTIES
   * @throws Exception
   */
  public void testCache() throws Exception {
    
    if (!GrouperHibernateConfig.retrieveConfig().propertyValueBoolean("hibernate.cache.use_query_cache", false)) {
      System.out.println("Not testing query cache since hibernate.cache.use_query_cache is false!");
      return;
    }
    
    LOG.info("testSetBadStemDisplayExtension");
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemFinder.findRootStem(s);
    Stem            edu   = root.addChildStem("edu", "edu");
    
    //get once
    Stem edu2 = StemFinder.findByUuid(s, edu.getUuid(), true);

    //update it
    HibernateSession.bySqlStatic().executeSql(
        "update grouper_stems set extension = 'abc' where id = ?", HibUtils.listObject(edu.getUuid()), 
        HibUtils.listType(StringType.INSTANCE));

    //NOTE: make sure this is set in grouper.hibernate.properties: hibernate.cache.use_query_cache = true  
    //NOTE: make sure the ehcache.xml is like the example in regards to Stem caching
    
    //get again
    Stem edu3 = StemFinder.findByUuid(s, edu.getUuid(), true);
    
    assertEquals("edu", edu3.getExtension());
    
    //wait until timeout
    GrouperUtil.sleep(12000);
    
    //should have updated by now
    edu2 = StemFinder.findByUuid(s, edu.getUuid(), true);
    assertEquals("abc", edu2.getExtension());
    
    edu.setExtension("abc2");
    edu.store();
    
    //hibernate should know to update it
    edu2 = StemFinder.findByUuid(s, edu.getUuid(), true);
    assertEquals("abc2", edu2.getExtension());
    
    s.stop();
  } // public void testSetBadStemDisplayExtension()

  @Override
  protected void setupConfigs() {
    super.setupConfigs();
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("stem.validateExtensionByDefault", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("group.validateExtensionByDefault", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDef.validateExtensionByDefault", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefName.validateExtensionByDefault", "false");
  }

  /**
   * test new performance method for child memberships
   * @throws Exception 
   */
  public void testGetChildMembershipGroups() throws Exception {
    
    GrouperSession aSession;
    R r = R.populateRegistry(0, 0, 1);
    final Subject subject = r.getSubject("a");
    
    aSession = GrouperSession.start(subject);
    final Stem[] topNew = new Stem[1];
    final Group[] group1 = new Group[1];
    
    final GrouperSession rootSession = aSession.internal_getRootSession();
    
    GrouperSession.callbackGrouperSession(rootSession, new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        try {
          topNew[0] = StemFinder.findRootStem(grouperSession).addChildStem("top new", "top new display name");
          group1[0] = topNew[0].addChildGroup("test1", "test1");
          
        } catch (Exception e) {
          throw new RuntimeException("Problem", e);
        }
        return null;
      }
      
    });

    Set<Group> groups = topNew[0].getChildMembershipGroups(Scope.ONE, AccessPrivilege.READ_PRIVILEGES, null);
    assertEquals(0, groups.size());
    groups = topNew[0].getChildMembershipGroups(Scope.ONE, AccessPrivilege.VIEW_PRIVILEGES, null);
    assertEquals(0, groups.size());
    groups = topNew[0].getChildMembershipGroups(Scope.ONE, AccessPrivilege.MANAGE_PRIVILEGES, null);
    assertEquals(0, groups.size());    

    //add a membership (grouper all should see
    GrouperSession.callbackGrouperSession(rootSession, new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        try {
          group1[0].addMember(subject);
        } catch (Exception e) {
          throw new RuntimeException("Problem", e);
        }
        return null;
      }
      
    });

    groups = topNew[0].getChildMembershipGroups(Scope.ONE, AccessPrivilege.READ_PRIVILEGES, null);
    assertEquals(1, groups.size());
    assertEquals("top new:test1", groups.iterator().next().getName());
    groups = topNew[0].getChildMembershipGroups(Scope.ONE, AccessPrivilege.VIEW_PRIVILEGES, null);
    assertEquals(1, groups.size());
    assertEquals("top new:test1", groups.iterator().next().getName());
    groups = topNew[0].getChildMembershipGroups(Scope.ONE, AccessPrivilege.MANAGE_PRIVILEGES, null);
    assertEquals(0, groups.size());    

    //remove grouper all, should not see
    GrouperSession.callbackGrouperSession(rootSession, new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        try {
          group1[0].revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
          group1[0].revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
        } catch (Exception e) {
          throw new RuntimeException("Problem", e);
        }
        return null;
      }
      
    });

    groups = topNew[0].getChildMembershipGroups(Scope.ONE, AccessPrivilege.READ_PRIVILEGES, null);
    assertEquals(0, groups.size());
    groups = topNew[0].getChildMembershipGroups(Scope.ONE, AccessPrivilege.VIEW_PRIVILEGES, null);
    assertEquals(0, groups.size());
    groups = topNew[0].getChildMembershipGroups(Scope.ONE, AccessPrivilege.MANAGE_PRIVILEGES, null);
    assertEquals(0, groups.size()); 
    
    //remove membership, add a priv
    GrouperSession.callbackGrouperSession(rootSession, new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        try {
          group1[0].deleteMember(subject);
          group1[0].grantPriv(subject, AccessPrivilege.READ);
        } catch (Exception e) {
          throw new RuntimeException("Problem", e);
        }
        return null;
      }
      
    });

    groups = topNew[0].getChildMembershipGroups(Scope.ONE, AccessPrivilege.READ_PRIVILEGES, null);
    assertEquals(0, groups.size());
    groups = topNew[0].getChildMembershipGroups(Scope.ONE, AccessPrivilege.VIEW_PRIVILEGES, null);
    assertEquals(0, groups.size());
    groups = topNew[0].getChildMembershipGroups(Scope.ONE, AccessPrivilege.MANAGE_PRIVILEGES, null);
    assertEquals(0, groups.size());    

    //add membership
    GrouperSession.callbackGrouperSession(rootSession, new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        try {
          group1[0].addMember(subject);
        } catch (Exception e) {
          throw new RuntimeException("Problem", e);
        }
        return null;
      }
      
    });

    groups = topNew[0].getChildMembershipGroups(Scope.ONE, AccessPrivilege.READ_PRIVILEGES, null);
    assertEquals(1, groups.size());
    assertEquals("top new:test1", groups.iterator().next().getName());

    groups = topNew[0].getChildMembershipGroups(Scope.ONE, AccessPrivilege.VIEW_PRIVILEGES, null);
    assertEquals(1, groups.size());
    assertEquals("top new:test1", groups.iterator().next().getName());
    
    groups = topNew[0].getChildMembershipGroups(Scope.ONE, AccessPrivilege.MANAGE_PRIVILEGES, null);
    assertEquals(0, groups.size());    
    
    
    //remove read, add list
    GrouperSession.callbackGrouperSession(rootSession, new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        try {
          group1[0].revokePriv(subject, AccessPrivilege.READ);
          group1[0].grantPriv(subject, AccessPrivilege.VIEW);
        } catch (Exception e) {
          throw new RuntimeException("Problem", e);
        }
        return null;
      }
      
    });
    
    groups = topNew[0].getChildMembershipGroups(Scope.ONE, AccessPrivilege.READ_PRIVILEGES, null);
    assertEquals(0, groups.size());

    groups = topNew[0].getChildMembershipGroups(Scope.ONE, AccessPrivilege.VIEW_PRIVILEGES, null);
    assertEquals(1, groups.size());
    assertEquals("top new:test1", groups.iterator().next().getName());
    
    groups = topNew[0].getChildMembershipGroups(Scope.ONE, AccessPrivilege.MANAGE_PRIVILEGES, null);
    assertEquals(0, groups.size());    
    
    //remove list, add update
    GrouperSession.callbackGrouperSession(rootSession, new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        try {
          group1[0].revokePriv(subject, AccessPrivilege.VIEW);
          group1[0].grantPriv(subject, AccessPrivilege.UPDATE);
        } catch (Exception e) {
          throw new RuntimeException("Problem", e);
        }
        return null;
      }
      
    });
    
    groups = topNew[0].getChildMembershipGroups(Scope.ONE, AccessPrivilege.READ_PRIVILEGES, null);
    assertEquals(0, groups.size());

    groups = topNew[0].getChildMembershipGroups(Scope.ONE, AccessPrivilege.VIEW_PRIVILEGES, null);
    assertEquals(1, groups.size());
    assertEquals("top new:test1", groups.iterator().next().getName());
    
    groups = topNew[0].getChildMembershipGroups(Scope.ONE, AccessPrivilege.MANAGE_PRIVILEGES, null);
    assertEquals(1, groups.size());
    assertEquals("top new:test1", groups.iterator().next().getName());
    
  }
  
  /**
   * make an example stem for testing
   * @return an example stem
   */
  public static Stem exampleStem() {
    Stem stem = new Stem();
    stem.setAlternateNameDb("alternateName");
    stem.setContextId("contextId");
    stem.setCreateTimeLong(5L);
    stem.setCreatorUuid("creatorId");
    stem.setDescription("description");
    stem.setDisplayExtensionDb("displayExtension");
    stem.setDisplayNameDb("displayName");
    stem.setExtensionDb("extension");
    stem.setHibernateVersionNumber(3L);
    stem.setIdIndex(12345L);
    stem.setLastMembershipChangeDb(4L);
    stem.setModifierUuid("modifierId");
    stem.setModifyTimeLong(6L);
    stem.setName("name");
    stem.setParentUuid("parentUuid");
    stem.setUuid("uuid");
    
    return stem;
  }
  
  /**
   * make an example stem for testing
   * @return an example stem
   */
  public static Stem exampleStemDb() {
    Stem stem = new StemSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignStemNameToEdit("stemTest").assignName("stemTest")
      .assignDescription("description").save();
    return stem;
  }

  
  /**
   * make an example stem for testing
   * @return an example stem
   */
  public static Stem exampleRetrieveStemDb() {
    Stem stem = StemFinder.findByName(GrouperSession.staticGrouperSession(), "stemTest", true);
    return stem;
  }

  
  /**
   * make sure update properties are detected correctly
   */
  public void testXmlInsert() {
    
    GrouperSession.startRootSession();
    
    Stem stemOriginal = new StemSave(GrouperSession.staticGrouperSession()).assignStemNameToEdit("stemInsert").assignName("stemInsert").save();
    stemOriginal = StemFinder.findByUuid(GrouperSession.staticGrouperSession(), stemOriginal.getUuid(), true, new QueryOptions().secondLevelCache(false));
    Stem stemCopy = StemFinder.findByUuid(GrouperSession.staticGrouperSession(), stemOriginal.getUuid(), true, new QueryOptions().secondLevelCache(false));
    Stem stemCopy2 = StemFinder.findByUuid(GrouperSession.staticGrouperSession(), stemOriginal.getUuid(), true, new QueryOptions().secondLevelCache(false));
    stemCopy.delete();
    
    //lets insert the original
    stemCopy2.xmlSaveBusinessProperties(null);
    
    System.out.println(stemCopy2.getLastMembershipChange());
    
    stemCopy2.xmlSaveUpdateProperties();

    //refresh from DB
    stemCopy = StemFinder.findByUuid(GrouperSession.staticGrouperSession(), stemOriginal.getUuid(), true, new QueryOptions().secondLevelCache(false));
    
    assertFalse(stemCopy == stemOriginal);
    assertFalse(stemCopy.xmlDifferentBusinessProperties(stemOriginal));
    assertFalse(stemCopy.xmlDifferentUpdateProperties(stemOriginal));
    
  }
  
  /**
   * make sure update properties are detected correctly
   */
  public void testXmlDifferentUpdateProperties() {
    
    @SuppressWarnings("unused")
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem stem = null;
    Stem exampleStem = null;

    //lets do an insert
    
    
    //TEST UPDATE PROPERTIES
    {
      stem = exampleStemDb();
      exampleStem = stem.clone();
      
      stem.setContextId("abc");
      
      assertFalse(stem.xmlDifferentBusinessProperties(exampleStem));
      assertTrue(stem.xmlDifferentUpdateProperties(exampleStem));

      stem.setContextId(exampleStem.getContextId());
      stem.xmlSaveUpdateProperties();

      stem = exampleRetrieveStemDb();
      
      assertFalse(stem.xmlDifferentBusinessProperties(exampleStem));
      assertFalse(stem.xmlDifferentUpdateProperties(exampleStem));
      
    }
    
    {
      stem = exampleStemDb();
      exampleStem = stem.clone();

      stem.setCreateTimeLong(99);
      
      assertFalse(stem.xmlDifferentBusinessProperties(exampleStem));
      assertTrue(stem.xmlDifferentUpdateProperties(exampleStem));

      stem.setCreateTimeLong(exampleStem.getCreateTimeLong());
      stem.xmlSaveUpdateProperties();
      
      stem = exampleRetrieveStemDb();
      
      assertFalse(stem.xmlDifferentBusinessProperties(exampleStem));
      assertFalse(stem.xmlDifferentUpdateProperties(exampleStem));
    }
    
    {
      stem = exampleStemDb();
      exampleStem = stem.clone();

      stem.setCreatorUuid("abc");
      
      assertFalse(stem.xmlDifferentBusinessProperties(exampleStem));
      assertTrue(stem.xmlDifferentUpdateProperties(exampleStem));

      stem.setCreatorUuid(exampleStem.getCreatorUuid());
      stem.xmlSaveUpdateProperties();
      
      stem = exampleRetrieveStemDb();
      
      assertFalse(stem.xmlDifferentBusinessProperties(exampleStem));
      assertFalse(stem.xmlDifferentUpdateProperties(exampleStem));
    }
    
    {
      stem = exampleStemDb();
      exampleStem = stem.clone();
      
      stem.setModifierUuid("abc");
      
      assertFalse(stem.xmlDifferentBusinessProperties(exampleStem));
      assertTrue(stem.xmlDifferentUpdateProperties(exampleStem));

      stem.setModifierUuid(exampleStem.getModifierUuid());
      stem.xmlSaveUpdateProperties();
      
      stem = exampleRetrieveStemDb();
      
      assertFalse(stem.xmlDifferentBusinessProperties(exampleStem));
      assertFalse(stem.xmlDifferentUpdateProperties(exampleStem));
    }
    
    {
      stem = exampleStemDb();
      exampleStem = stem.clone();

      stem.setModifyTimeLong(99);
      
      assertFalse(stem.xmlDifferentBusinessProperties(exampleStem));
      assertTrue(stem.xmlDifferentUpdateProperties(exampleStem));

      stem.setModifyTimeLong(exampleStem.getModifyTimeLong());
      stem.xmlSaveUpdateProperties();
      
      stem = exampleRetrieveStemDb();
      
      assertFalse(stem.xmlDifferentBusinessProperties(exampleStem));
      assertFalse(stem.xmlDifferentUpdateProperties(exampleStem));

    }

    {
      stem = exampleStemDb();
      exampleStem = stem.clone();

      stem.setHibernateVersionNumber(99L);
      
      assertFalse(stem.xmlDifferentBusinessProperties(exampleStem));
      assertTrue(stem.xmlDifferentUpdateProperties(exampleStem));

      stem.setHibernateVersionNumber(exampleStem.getHibernateVersionNumber());
      stem.xmlSaveUpdateProperties();
      
      stem = exampleRetrieveStemDb();
      
      assertFalse(stem.xmlDifferentBusinessProperties(exampleStem));
      assertFalse(stem.xmlDifferentUpdateProperties(exampleStem));
    }
    //TEST BUSINESS PROPERTIES
    
    {
      stem = exampleStemDb();
      exampleStem = stem.clone();

      stem.setDescriptionDb("abc");
      
      assertTrue(stem.xmlDifferentBusinessProperties(exampleStem));
      assertFalse(stem.xmlDifferentUpdateProperties(exampleStem));

      stem.setDescriptionDb(exampleStem.getDescriptionDb());
      stem.xmlSaveBusinessProperties(exampleStem.clone());
      stem.xmlSaveUpdateProperties();
      
      stem = exampleRetrieveStemDb();
      
      assertFalse(stem.xmlDifferentBusinessProperties(exampleStem));
      assertFalse(stem.xmlDifferentUpdateProperties(exampleStem));
    
    }
    
    {
      stem = exampleStemDb();
      exampleStem = stem.clone();

      stem.setDisplayExtensionDb("abc");
      
      assertTrue(stem.xmlDifferentBusinessProperties(exampleStem));
      assertFalse(stem.xmlDifferentUpdateProperties(exampleStem));

      stem.setDisplayExtensionDb(exampleStem.getDisplayExtensionDb());
      stem.xmlSaveBusinessProperties(exampleStem.clone());
      stem.xmlSaveUpdateProperties();
      
      stem = exampleRetrieveStemDb();
      
      assertFalse(stem.xmlDifferentBusinessProperties(exampleStem));
      assertFalse(stem.xmlDifferentUpdateProperties(exampleStem));
    
    }
    
    {
      stem = exampleStemDb();
      exampleStem = stem.clone();

      stem.setDisplayNameDb("abc");
      
      assertTrue(stem.xmlDifferentBusinessProperties(exampleStem));
      assertFalse(stem.xmlDifferentUpdateProperties(exampleStem));

      stem.setDisplayNameDb(exampleStem.getDisplayNameDb());
      stem.xmlSaveBusinessProperties(exampleStem.clone());
      stem.xmlSaveUpdateProperties();
      
      stem = exampleRetrieveStemDb();
      
      assertFalse(stem.xmlDifferentBusinessProperties(exampleStem));
      assertFalse(stem.xmlDifferentUpdateProperties(exampleStem));
    
    }
    
    {
      stem = exampleStemDb();
      exampleStem = stem.clone();

      stem.setExtensionDb("abc");
      
      assertTrue(stem.xmlDifferentBusinessProperties(exampleStem));
      assertFalse(stem.xmlDifferentUpdateProperties(exampleStem));

      stem.setExtensionDb(exampleStem.getExtensionDb());
      stem.xmlSaveBusinessProperties(exampleStem.clone());
      stem.xmlSaveUpdateProperties();
      
      stem = exampleRetrieveStemDb();
      
      assertFalse(stem.xmlDifferentBusinessProperties(exampleStem));
      assertFalse(stem.xmlDifferentUpdateProperties(exampleStem));
    
    }
    
    {
      stem = exampleStemDb();
      exampleStem = stem.clone();

      stem.setNameDb("abc");
      
      assertTrue(stem.xmlDifferentBusinessProperties(exampleStem));
      assertFalse(stem.xmlDifferentUpdateProperties(exampleStem));

      stem.setNameDb(exampleStem.getNameDb());
      stem.xmlSaveBusinessProperties(exampleStem.clone());
      stem.xmlSaveUpdateProperties();
      
      stem = exampleRetrieveStemDb();
      
      assertFalse(stem.xmlDifferentBusinessProperties(exampleStem));
      assertFalse(stem.xmlDifferentUpdateProperties(exampleStem));
    
    }
    
    {
      stem = exampleStemDb();
      exampleStem = stem.clone();

      stem.setParentUuid("abc");
      
      assertTrue(stem.xmlDifferentBusinessProperties(exampleStem));
      assertFalse(stem.xmlDifferentUpdateProperties(exampleStem));

      stem.setParentUuid(exampleStem.getParentUuid());
      stem.xmlSaveBusinessProperties(exampleStem.clone());
      stem.xmlSaveUpdateProperties();
      
      stem = exampleRetrieveStemDb();
      
      assertFalse(stem.xmlDifferentBusinessProperties(exampleStem));
      assertFalse(stem.xmlDifferentUpdateProperties(exampleStem));
    
    }

    {
      stem = exampleStemDb();
      exampleStem = stem.clone();

      stem.setUuid("abc");
      
      assertTrue(stem.xmlDifferentBusinessProperties(exampleStem));
      assertFalse(stem.xmlDifferentUpdateProperties(exampleStem));

      stem.setUuid(exampleStem.getUuid());
      stem.xmlSaveBusinessProperties(exampleStem.clone());
      stem.xmlSaveUpdateProperties();
      
      stem = exampleRetrieveStemDb();
      
      assertFalse(stem.xmlDifferentBusinessProperties(exampleStem));
      assertFalse(stem.xmlDifferentUpdateProperties(exampleStem));
    
    }
    
    {
      stem = exampleStemDb();
      exampleStem = stem.clone();

      stem.setAlternateNameDb("abc");
      
      assertTrue(stem.xmlDifferentBusinessProperties(exampleStem));
      assertFalse(stem.xmlDifferentUpdateProperties(exampleStem));

      stem.setAlternateNameDb(exampleStem.getAlternateNameDb());
      stem.xmlSaveBusinessProperties(exampleStem.clone());
      stem.xmlSaveUpdateProperties();
      
      stem = exampleRetrieveStemDb();
      
      assertFalse(stem.xmlDifferentBusinessProperties(exampleStem));
      assertFalse(stem.xmlDifferentUpdateProperties(exampleStem));
    
    }
  }

  /**
   * make sure obliterate works
   */
  public void testObliterateAndPointInTime() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignName("test").save();
    new StemSave(grouperSession).assignName("test:sub1").save();
    new StemSave(grouperSession).assignName("test:sub2").save();
    new StemSave(grouperSession).assignName("test:sub1:sub12").save();
    new StemSave(grouperSession).assignName("test:sub2:sub22").save();
    
    new GroupSave(grouperSession).assignName("test:testGroup").save();
    new GroupSave(grouperSession).assignName("test:testGroup2").save();
    new GroupSave(grouperSession).assignName("test:testGroup3").save();
    new GroupSave(grouperSession).assignName("test:sub1:testGroup").save();
    new GroupSave(grouperSession).assignName("test:sub1:testGroup2").save();
    new GroupSave(grouperSession).assignName("test:sub1:testGroup3").save();
    new GroupSave(grouperSession).assignName("test:sub2:testGroup").save();
    new GroupSave(grouperSession).assignName("test:sub2:testGroup2").save();
    new GroupSave(grouperSession).assignName("test:sub2:testGroup3").save();
    new GroupSave(grouperSession).assignName("test:sub1:sub12:testGroup").save();
    new GroupSave(grouperSession).assignName("test:sub1:sub12:testGroup2").save();
    new GroupSave(grouperSession).assignName("test:sub1:sub12:testGroup3").save();
    new GroupSave(grouperSession).assignName("test:sub2:sub22:testGroup").save();
    new GroupSave(grouperSession).assignName("test:sub2:sub22:testGroup2").save();
    new GroupSave(grouperSession).assignName("test:sub2:sub22:testGroup3").save();
  
    AttributeDef testTestAttributeDef = new AttributeDefSave(grouperSession).assignName("test:testAttributeDef").save();
    AttributeDef testTest2AttributeDef = new AttributeDefSave(grouperSession).assignName("test:testAttributeDef2").save();
    AttributeDef testTest3AttributeDef = new AttributeDefSave(grouperSession).assignName("test:testAttributeDef3").save();
    AttributeDef testTestSub1AttributeDef = new AttributeDefSave(grouperSession).assignName("test:sub1:testAttributeDef").save();
    AttributeDef testTestSub12AttributeDef = new AttributeDefSave(grouperSession).assignName("test:sub1:testAttributeDef2").save();
    AttributeDef testTestSub13AttributeDef = new AttributeDefSave(grouperSession).assignName("test:sub1:testAttributeDef3").save();
    AttributeDef testTestSub2AttributeDef = new AttributeDefSave(grouperSession).assignName("test:sub2:testAttributeDef").save();
    AttributeDef testTestSub22AttributeDef = new AttributeDefSave(grouperSession).assignName("test:sub2:testAttributeDef2").save();
    AttributeDef testTestSub23AttributeDef = new AttributeDefSave(grouperSession).assignName("test:sub2:testAttributeDef3").save();
    AttributeDef testTestSub112AttributeDef = new AttributeDefSave(grouperSession).assignName("test:sub1:sub12:testAttributeDef").save();
    AttributeDef testTestSub122AttributeDef = new AttributeDefSave(grouperSession).assignName("test:sub1:sub12:testAttributeDef2").save();
    AttributeDef testTestSub123AttributeDef = new AttributeDefSave(grouperSession).assignName("test:sub1:sub12:testAttributeDef3").save();
    AttributeDef testTestSub222AttributeDef = new AttributeDefSave(grouperSession).assignName("test:sub2:sub22:testAttributeDef").save();
    AttributeDef testTestSub2222AttributeDef = new AttributeDefSave(grouperSession).assignName("test:sub2:sub22:testAttributeDef2").save();
    AttributeDef testTestSub2223AttributeDef = new AttributeDefSave(grouperSession).assignName("test:sub2:sub22:testAttributeDef3").save();
  
    new AttributeDefNameSave(grouperSession, testTestAttributeDef).assignName("test:testAttributeDefName").save();
    new AttributeDefNameSave(grouperSession, testTest2AttributeDef).assignName("test:testAttributeDefName2").save();
    new AttributeDefNameSave(grouperSession, testTest3AttributeDef).assignName("test:testAttributeDefName3").save();
    new AttributeDefNameSave(grouperSession, testTestSub1AttributeDef).assignName("test:testSub1AttributeDefName").save();
    new AttributeDefNameSave(grouperSession, testTestSub12AttributeDef).assignName("test:testSub12AttributeDefName").save();
    new AttributeDefNameSave(grouperSession, testTestSub13AttributeDef).assignName("test:testSub13AttributeDefName").save();
    new AttributeDefNameSave(grouperSession, testTestSub2AttributeDef).assignName("test:testSub2AttributeDefName").save();
    new AttributeDefNameSave(grouperSession, testTestSub22AttributeDef).assignName("test:testSub22AttributeDefName").save();
    new AttributeDefNameSave(grouperSession, testTestSub23AttributeDef).assignName("test:testSub23AttributeDefName").save();
    new AttributeDefNameSave(grouperSession, testTestSub112AttributeDef).assignName("test:testSub112AttributeDefName").save();
    new AttributeDefNameSave(grouperSession, testTestSub122AttributeDef).assignName("test:testSub122AttributeDefName").save();
    new AttributeDefNameSave(grouperSession, testTestSub123AttributeDef).assignName("test:testSub123AttributeDefName").save();
    new AttributeDefNameSave(grouperSession, testTestSub222AttributeDef).assignName("test:testSub222AttributeDefName").save();
    new AttributeDefNameSave(grouperSession, testTestSub2222AttributeDef).assignName("test:testSub2222AttributeDefName").save();
    new AttributeDefNameSave(grouperSession, testTestSub2223AttributeDef).assignName("test:testSub2223AttributeDefName").save();
  
    try {
      Stem.testingRunChangeLogSooner = true;
      
      stem.obliterate(true, true, true);
      
      stem = StemFinder.findByName(grouperSession, "test", true);
      
      assertNotNull(stem);
      
      stem.obliterate(false, false, true);
      
      stem = StemFinder.findByName(grouperSession, "test", false);
      
      assertNull(stem);
      
      ChangeLogTempToEntity.convertRecords();
      
      assertEquals(0, GrouperUtil.length(GrouperDAOFactory.getFactory().getPITStem().findByName("test", false)));
    } finally {
      Stem.testingRunChangeLogSooner = false;
      
    }
    
  }

  /**
   * make sure obliterate works
   */
  public void testObliterateAndPointInTimeComposite() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    runCompositeMembershipChangeLogConsumer();

    Stem stem = new StemSave(grouperSession).assignName("test").save();
    new StemSave(grouperSession).assignName("test:sub1").save();
    new StemSave(grouperSession).assignName("test:sub2").save();
    new StemSave(grouperSession).assignName("test:sub1:sub12").save();
    new StemSave(grouperSession).assignName("test:sub2:sub22").save();

    Group test2Group = new GroupSave(grouperSession).assignName("test2:testGroup").assignCreateParentStemsIfNotExist(true).save();
    Group test2Group2 = new GroupSave(grouperSession).assignName("test2:testGroup2").save();
    Group test2Group3 = new GroupSave(grouperSession).assignName("test2:testGroup3").save();
    Group test2Group4 = new GroupSave(grouperSession).assignName("test2:testGroup4").save();

    Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
    Group testGroup2 = new GroupSave(grouperSession).assignName("test:testGroup2").save();
    
    testGroup.addCompositeMember(CompositeType.INTERSECTION, test2Group, test2Group2);
    test2Group3.addCompositeMember(CompositeType.INTERSECTION, testGroup2, test2Group4);
    
    test2Group.addMember(SubjectTestHelper.SUBJ1);
    test2Group2.addMember(SubjectTestHelper.SUBJ1);
    
    new GroupSave(grouperSession).assignName("test:testGroup3").save();
    new GroupSave(grouperSession).assignName("test:sub1:testGroup").save();
    new GroupSave(grouperSession).assignName("test:sub1:testGroup2").save();
    new GroupSave(grouperSession).assignName("test:sub1:testGroup3").save();
    new GroupSave(grouperSession).assignName("test:sub2:testGroup").save();
    new GroupSave(grouperSession).assignName("test:sub2:testGroup2").save();
    new GroupSave(grouperSession).assignName("test:sub2:testGroup3").save();
    new GroupSave(grouperSession).assignName("test:sub1:sub12:testGroup").save();
    new GroupSave(grouperSession).assignName("test:sub1:sub12:testGroup2").save();
    new GroupSave(grouperSession).assignName("test:sub1:sub12:testGroup3").save();
    new GroupSave(grouperSession).assignName("test:sub2:sub22:testGroup").save();
    new GroupSave(grouperSession).assignName("test:sub2:sub22:testGroup2").save();
    new GroupSave(grouperSession).assignName("test:sub2:sub22:testGroup3").save();
  
    AttributeDef testTestAttributeDef = new AttributeDefSave(grouperSession).assignName("test:testAttributeDef").save();
    AttributeDef testTest2AttributeDef = new AttributeDefSave(grouperSession).assignName("test:testAttributeDef2").save();
    AttributeDef testTest3AttributeDef = new AttributeDefSave(grouperSession).assignName("test:testAttributeDef3").save();
    AttributeDef testTestSub1AttributeDef = new AttributeDefSave(grouperSession).assignName("test:sub1:testAttributeDef").save();
    AttributeDef testTestSub12AttributeDef = new AttributeDefSave(grouperSession).assignName("test:sub1:testAttributeDef2").save();
    AttributeDef testTestSub13AttributeDef = new AttributeDefSave(grouperSession).assignName("test:sub1:testAttributeDef3").save();
    AttributeDef testTestSub2AttributeDef = new AttributeDefSave(grouperSession).assignName("test:sub2:testAttributeDef").save();
    AttributeDef testTestSub22AttributeDef = new AttributeDefSave(grouperSession).assignName("test:sub2:testAttributeDef2").save();
    AttributeDef testTestSub23AttributeDef = new AttributeDefSave(grouperSession).assignName("test:sub2:testAttributeDef3").save();
    AttributeDef testTestSub112AttributeDef = new AttributeDefSave(grouperSession).assignName("test:sub1:sub12:testAttributeDef").save();
    AttributeDef testTestSub122AttributeDef = new AttributeDefSave(grouperSession).assignName("test:sub1:sub12:testAttributeDef2").save();
    AttributeDef testTestSub123AttributeDef = new AttributeDefSave(grouperSession).assignName("test:sub1:sub12:testAttributeDef3").save();
    AttributeDef testTestSub222AttributeDef = new AttributeDefSave(grouperSession).assignName("test:sub2:sub22:testAttributeDef").save();
    AttributeDef testTestSub2222AttributeDef = new AttributeDefSave(grouperSession).assignName("test:sub2:sub22:testAttributeDef2").save();
    AttributeDef testTestSub2223AttributeDef = new AttributeDefSave(grouperSession).assignName("test:sub2:sub22:testAttributeDef3").save();
  
    new AttributeDefNameSave(grouperSession, testTestAttributeDef).assignName("test:testAttributeDefName").save();
    new AttributeDefNameSave(grouperSession, testTest2AttributeDef).assignName("test:testAttributeDefName2").save();
    new AttributeDefNameSave(grouperSession, testTest3AttributeDef).assignName("test:testAttributeDefName3").save();
    new AttributeDefNameSave(grouperSession, testTestSub1AttributeDef).assignName("test:testSub1AttributeDefName").save();
    new AttributeDefNameSave(grouperSession, testTestSub12AttributeDef).assignName("test:testSub12AttributeDefName").save();
    new AttributeDefNameSave(grouperSession, testTestSub13AttributeDef).assignName("test:testSub13AttributeDefName").save();
    new AttributeDefNameSave(grouperSession, testTestSub2AttributeDef).assignName("test:testSub2AttributeDefName").save();
    new AttributeDefNameSave(grouperSession, testTestSub22AttributeDef).assignName("test:testSub22AttributeDefName").save();
    new AttributeDefNameSave(grouperSession, testTestSub23AttributeDef).assignName("test:testSub23AttributeDefName").save();
    new AttributeDefNameSave(grouperSession, testTestSub112AttributeDef).assignName("test:testSub112AttributeDefName").save();
    new AttributeDefNameSave(grouperSession, testTestSub122AttributeDef).assignName("test:testSub122AttributeDefName").save();
    new AttributeDefNameSave(grouperSession, testTestSub123AttributeDef).assignName("test:testSub123AttributeDefName").save();
    new AttributeDefNameSave(grouperSession, testTestSub222AttributeDef).assignName("test:testSub222AttributeDefName").save();
    new AttributeDefNameSave(grouperSession, testTestSub2222AttributeDef).assignName("test:testSub2222AttributeDefName").save();
    new AttributeDefNameSave(grouperSession, testTestSub2223AttributeDef).assignName("test:testSub2223AttributeDefName").save();
  
    runCompositeMembershipChangeLogConsumer();

    try {
      Stem.testingRunChangeLogSooner = true;
      
      stem.obliterate(true, true, true);
      
      stem = StemFinder.findByName(grouperSession, "test", true);
      
      assertNotNull(stem);
      
      stem.obliterate(false, false, true);
      
      stem = StemFinder.findByName(grouperSession, "test", false);
      
      assertNull(stem);
      
      ChangeLogTempToEntity.convertRecords();
      
      assertEquals(0, GrouperUtil.length(GrouperDAOFactory.getFactory().getPITStem().findByName("test", false)));
    } finally {
      Stem.testingRunChangeLogSooner = false;
      
    }
    
  }

  /**
   * 
   */
  public void testStemObliterate2Stem0() {
  
    // test stem has objects and privs (subj0[ADMIN], subj1[ADMIN])
    // test:someGroupA has a member: subj9, and privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someAttrDefA has privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someAttrDefNameA has effective privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someGroupB has privs (subj1[ADMIN])
    // test:someAttrDefB has privs (subj1[ADMIN])
    // test:someAttrDefNameB has effective privs (subj1[ADMIN])

    // test:anotherFolder (subj0[ADMIN], subj1[ADMIN])
    // test:anotherFolder:someGroup3 has a member: subj9, and privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:anotherFolder:someGroup4 has privs (subj1[ADMIN])

    // test:anotherFolder2 (subj1[ADMIN])

    // test:anotherFolder3 (subj1[ADMIN])
    // test:anotherFolder3:someFolder5 (subj1[ADMIN])
    // test:anotherFolder3:someFolder6

    // test:anotherFolder4

    // test:anotherFolder7 (subj1[ADMIN])
    // test:anotherFolder7:someFolder8 (subj1[ADMIN])
  
    stemObliterate2setup();
  
    // subj7 can delete no folders
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ7);
  
    Stem stem = StemFinder.findByName(grouperSession, "test", true);
    assertNotNull(stem);
    stem = StemFinder.findByName(grouperSession, "test:anotherFolder", true);
    assertNotNull(stem);
    stem = StemFinder.findByName(grouperSession, "test:anotherFolder2", true);
    assertNotNull(stem);
    stem = StemFinder.findByName(grouperSession, "test:anotherFolder3", true);
    assertNotNull(stem);
    stem = StemFinder.findByName(grouperSession, "test:anotherFolder3:someFolder5", true);
    assertNotNull(stem);
    
    Stem testStem = StemFinder.findByName(grouperSession, "test", true);
  
    int count = testStem.deleteEmptyStems(false, false, Scope.SUB).size();
    assertEquals(0, count);
  
    GrouperSession.stopQuietly(grouperSession);
    
    grouperSession = GrouperSession.startRootSession();
    
    stem = StemFinder.findByName(grouperSession, "test", false);
    assertNotNull(stem);
    stem = StemFinder.findByName(grouperSession, "test:anotherFolder", false);
    assertNotNull(stem);
    stem = StemFinder.findByName(grouperSession, "test:anotherFolder2", false);
    assertNotNull(stem);
    stem = StemFinder.findByName(grouperSession, "test:anotherFolder3", false);
    assertNotNull(stem);
    stem = StemFinder.findByName(grouperSession, "test:anotherFolder3:someFolder5", false);
    assertNotNull(stem);
  }
  /**
   * 
   */
  public void testStemObliterate2Stem1() {
  
    // test stem has objects and privs (subj0[ADMIN], subj1[ADMIN])
    // test:someGroupA has a member: subj9, and privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someAttrDefA has privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someAttrDefNameA has effective privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someGroupB has privs (subj1[ADMIN])
    // test:someAttrDefB has privs (subj1[ADMIN])
    // test:someAttrDefNameB has effective privs (subj1[ADMIN])

    // test:anotherFolder (subj0[ADMIN], subj1[ADMIN])
    // test:anotherFolder:someGroup3 has a member: subj9, and privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:anotherFolder:someGroup4 has privs (subj1[ADMIN])

    // test:anotherFolder2 (subj1[ADMIN])

    // test:anotherFolder3 (subj1[ADMIN])
    // test:anotherFolder3:someFolder5 (subj1[ADMIN])
    // test:anotherFolder3:someFolder6

    // test:anotherFolder4

    // test:anotherFolder7 (subj1[ADMIN])
    // test:anotherFolder7:someFolder8 (subj1[ADMIN])
  
    stemObliterate2setup();

    // subj7 can delete no folders
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);

    Stem stem = StemFinder.findByName(grouperSession, "test", true);
    assertNotNull(stem);
    stem = StemFinder.findByName(grouperSession, "test:anotherFolder", true);
    assertNotNull(stem);
    stem = StemFinder.findByName(grouperSession, "test:anotherFolder2", true);
    assertNotNull(stem);
    stem = StemFinder.findByName(grouperSession, "test:anotherFolder3", true);
    assertNotNull(stem);
    stem = StemFinder.findByName(grouperSession, "test:anotherFolder3:someFolder5", true);
    assertNotNull(stem);
    stem = StemFinder.findByName(grouperSession, "test:anotherFolder3:someFolder6", true);
    assertNotNull(stem);
    stem = StemFinder.findByName(grouperSession, "test:anotherFolder4", true);
    assertNotNull(stem);
    stem = StemFinder.findByName(grouperSession, "test:anotherFolder7", true);
    assertNotNull(stem);
    stem = StemFinder.findByName(grouperSession, "test:anotherFolder7:someFolder8", true);
    assertNotNull(stem);

    Stem testStem = StemFinder.findByName(grouperSession, "test", true);

    int count = testStem.deleteEmptyStems(false, false, Scope.ONE).size();
    assertEquals(2, count); // testAnotherFolder2 and testAnotherFolder4

    GrouperSession.stopQuietly(grouperSession);
    
    grouperSession = GrouperSession.startRootSession();
    
    stem = StemFinder.findByName(grouperSession, "test", false);
    assertNotNull(stem);
    stem = StemFinder.findByName(grouperSession, "test:anotherFolder", false);
    assertNotNull(stem);
    stem = StemFinder.findByName(grouperSession, "test:anotherFolder2", false);
    assertNull(stem);
    stem = StemFinder.findByName(grouperSession, "test:anotherFolder3", false);
    assertNotNull(stem);
    stem = StemFinder.findByName(grouperSession, "test:anotherFolder3:someFolder5", false);
    assertNotNull(stem);
    stem = StemFinder.findByName(grouperSession, "test:anotherFolder3:someFolder6", false);
    assertNotNull(stem);
    stem = StemFinder.findByName(grouperSession, "test:anotherFolder4", false);
    assertNull(stem);
    stem = StemFinder.findByName(grouperSession, "test:anotherFolder7", false);
    assertNotNull(stem);
    stem = StemFinder.findByName(grouperSession, "test:anotherFolder7:someFolder8", false);
    assertNotNull(stem);
  }

  /**
   * 
   */
  public void testStemObliterate2Stem2() {
  
    // test stem has objects and privs (subj0[ADMIN], subj1[ADMIN])
    // test:someGroupA has a member: subj9, and privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someAttrDefA has privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someAttrDefNameA has effective privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someGroupB has privs (subj1[ADMIN])
    // test:someAttrDefB has privs (subj1[ADMIN])
    // test:someAttrDefNameB has effective privs (subj1[ADMIN])
  
    // test:anotherFolder (subj0[ADMIN], subj1[ADMIN])
    // test:anotherFolder:someGroup3 has a member: subj9, and privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:anotherFolder:someGroup4 has privs (subj1[ADMIN])
  
    // test:anotherFolder2 (subj1[ADMIN])
  
    // test:anotherFolder3 (subj1[ADMIN])
    // test:anotherFolder3:someFolder5 (subj1[ADMIN])
    // test:anotherFolder3:someFolder6
  
    // test:anotherFolder4
  
    // test:anotherFolder7 (subj1[ADMIN])
    // test:anotherFolder7:someFolder8 (subj1[ADMIN])
  
    stemObliterate2setup();
  
    // subj7 can delete no folders
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
  
    Stem stem = StemFinder.findByName(grouperSession, "test", true);
    assertNotNull(stem);
    stem = StemFinder.findByName(grouperSession, "test:anotherFolder", true);
    assertNotNull(stem);
    stem = StemFinder.findByName(grouperSession, "test:anotherFolder2", true);
    assertNotNull(stem);
    stem = StemFinder.findByName(grouperSession, "test:anotherFolder3", true);
    assertNotNull(stem);
    stem = StemFinder.findByName(grouperSession, "test:anotherFolder3:someFolder5", true);
    assertNotNull(stem);
    stem = StemFinder.findByName(grouperSession, "test:anotherFolder3:someFolder6", true);
    assertNotNull(stem);
    stem = StemFinder.findByName(grouperSession, "test:anotherFolder4", true);
    assertNotNull(stem);
    stem = StemFinder.findByName(grouperSession, "test:anotherFolder7", true);
    assertNotNull(stem);
    stem = StemFinder.findByName(grouperSession, "test:anotherFolder7:someFolder8", true);
    assertNotNull(stem);
  
    Stem testStem = StemFinder.findByName(grouperSession, "test", true);

    int count = testStem.deleteEmptyStems(false, false, Scope.SUB).size();
    assertEquals(7, count); 

    GrouperSession.stopQuietly(grouperSession);
    
    grouperSession = GrouperSession.startRootSession();
    
    stem = StemFinder.findByName(grouperSession, "test", false);
    assertNotNull(stem);
    stem = StemFinder.findByName(grouperSession, "test:anotherFolder", false);
    assertNotNull(stem);
    stem = StemFinder.findByName(grouperSession, "test:anotherFolder2", false);
    assertNull(stem);
    stem = StemFinder.findByName(grouperSession, "test:anotherFolder3", false);
    assertNull(stem);
    stem = StemFinder.findByName(grouperSession, "test:anotherFolder3:someFolder5", false);
    assertNull(stem);
    stem = StemFinder.findByName(grouperSession, "test:anotherFolder3:someFolder6", false);
    assertNull(stem);
    stem = StemFinder.findByName(grouperSession, "test:anotherFolder4", false);
    assertNull(stem);
    stem = StemFinder.findByName(grouperSession, "test:anotherFolder7", false);
    assertNull(stem);
    stem = StemFinder.findByName(grouperSession, "test:anotherFolder7:someFolder8", false);
    assertNull(stem);
  }

  /**
   * 
   */
  public void testStemObliterate2GroupMemberships0() {
  
  
    // test stem has objects and privs (subj0[ADMIN], subj1[ADMIN])
    // test:someGroupA has a member: subj9, and privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someAttrDefA has privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someAttrDefNameA has effective privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someGroupB has a member: subj9, and privs (subj1[ADMIN])
    // test:someAttrDefB has privs (subj1[ADMIN])
    // test:someAttrDefNameB has effective privs (subj1[ADMIN])

    // test:anotherFolder (subj0[ADMIN], subj1[ADMIN])
    // test:anotherFolder:someGroup3 has a member: subj9, and privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:anotherFolder:someGroup4 has a member: subj9, and privs (subj1[ADMIN])

    // test:anotherFolder2 (subj1[ADMIN])

    // test:anotherFolder3 (subj1[ADMIN])
    // test:anotherFolder3:someFolder5 (subj1[ADMIN])
    // test:anotherFolder3:someFolder6

    // test:anotherFolder4

    // test:anotherFolder7 (subj1[ADMIN])
    // test:anotherFolder7:someFolder8 (subj1[ADMIN])
  
    stemObliterate2setup();
  
    // subj7 can do nothing
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ7);
  
    Stem testStem = StemFinder.findByName(grouperSession, "test", true);
  
    int count = testStem.deleteGroupMemberships(false, false, Scope.ONE).size();
    assertEquals(0, count);
  }

  /**
   * 
   */
  public void testStemObliterate2GroupMemberships1() {
  
  
    // test stem has objects and privs (subj0[ADMIN], subj1[ADMIN])
    // test:someGroupA has a member: subj9, and privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someAttrDefA has privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someAttrDefNameA has effective privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someGroupB has a member: subj9, and privs (subj1[ADMIN])
    // test:someAttrDefB has privs (subj1[ADMIN])
    // test:someAttrDefNameB has effective privs (subj1[ADMIN])

    // test:anotherFolder (subj0[ADMIN], subj1[ADMIN])
    // test:anotherFolder:someGroup3 has a member: subj9, and privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:anotherFolder:someGroup4 has a member: subj9, and privs (subj1[ADMIN])

    // test:anotherFolder2 (subj1[ADMIN])

    // test:anotherFolder3 (subj1[ADMIN])
    // test:anotherFolder3:someFolder5 (subj1[ADMIN])
    // test:anotherFolder3:someFolder6

    // test:anotherFolder4

    // test:anotherFolder7 (subj1[ADMIN])
    // test:anotherFolder7:someFolder8 (subj1[ADMIN])
  
    stemObliterate2setup();
  
    // subj2 can update
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ2);

    Group group = GroupFinder.findByName(grouperSession, "test:someGroupA", false);
    assertNotNull(group);

    assertTrue(group.hasMember(SubjectTestHelper.SUBJ9));

    group = GroupFinder.findByName(grouperSession, "test:someGroupB", false);
    assertNotNull(group);

    assertTrue(group.hasMember(SubjectTestHelper.SUBJ9));

    Stem testStem = StemFinder.findByName(grouperSession, "test", true);
  
    int count = testStem.deleteGroupMemberships(false, false, Scope.ONE).size();
    assertEquals(1, count);
    
    GrouperSession.stopQuietly(grouperSession);
    
    grouperSession = GrouperSession.startRootSession();
    
    group = GroupFinder.findByName(grouperSession, "test:someGroupA", false);
    assertNotNull(group);

    assertFalse(group.hasMember(SubjectTestHelper.SUBJ9));
    
    group = GroupFinder.findByName(grouperSession, "test:someGroupB", false);
    assertNotNull(group);

    assertTrue(group.hasMember(SubjectTestHelper.SUBJ9));
    
  }

  /**
   * 
   */
  public void testStemObliterate2GroupMemberships2() {
  
  
    // test stem has objects and privs (subj0[ADMIN], subj1[ADMIN])
    // test:someGroupA has a member: subj9, and privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someAttrDefA has privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someAttrDefNameA has effective privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someGroupB has a member: subj9, and privs (subj1[ADMIN])
    // test:someAttrDefB has privs (subj1[ADMIN])
    // test:someAttrDefNameB has effective privs (subj1[ADMIN])

    // test:anotherFolder (subj0[ADMIN], subj1[ADMIN])
    // test:anotherFolder:someGroup3 has a member: subj9, and privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:anotherFolder:someGroup4 has a member: subj9, and privs (subj1[ADMIN])

    // test:anotherFolder2 (subj1[ADMIN])

    // test:anotherFolder3 (subj1[ADMIN])
    // test:anotherFolder3:someFolder5 (subj1[ADMIN])
    // test:anotherFolder3:someFolder6

    // test:anotherFolder4

    // test:anotherFolder7 (subj1[ADMIN])
    // test:anotherFolder7:someFolder8 (subj1[ADMIN])
  
    stemObliterate2setup();
  
    // subj1 can ddelete 2 groups
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
  
    Group group = GroupFinder.findByName(grouperSession, "test:someGroupA", true);
    assertNotNull(group);
    assertTrue(group.hasMember(SubjectTestHelper.SUBJ9));

    group = GroupFinder.findByName(grouperSession, "test:someGroupB", true);
    assertNotNull(group);
    assertTrue(group.hasMember(SubjectTestHelper.SUBJ9));


    Stem testStem = StemFinder.findByName(grouperSession, "test", true);
  
    int count = testStem.deleteGroupMemberships(false, false, Scope.ONE).size();
    assertEquals(2, count);
  
    GrouperSession.stopQuietly(grouperSession);
    
    grouperSession = GrouperSession.startRootSession();
    
    group = GroupFinder.findByName(grouperSession, "test:someGroupA", false);
    assertNotNull(group);
    assertFalse(group.hasMember(SubjectTestHelper.SUBJ9));

    group = GroupFinder.findByName(grouperSession, "test:someGroupB", false);
    assertNotNull(group);
    assertFalse(group.hasMember(SubjectTestHelper.SUBJ9));
  }

  /**
   * 
   */
  public void testStemObliterate2GroupMemberships3() {
  
  
    // test stem has objects and privs (subj0[ADMIN], subj1[ADMIN])
    // test:someGroupA has a member: subj9, and privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someAttrDefA has privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someAttrDefNameA has effective privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someGroupB has a member: subj9, and privs (subj1[ADMIN])
    // test:someAttrDefB has privs (subj1[ADMIN])
    // test:someAttrDefNameB has effective privs (subj1[ADMIN])

    // test:anotherFolder (subj0[ADMIN], subj1[ADMIN])
    // test:anotherFolder:someGroup3 has a member: subj9, and privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:anotherFolder:someGroup4 has a member: subj9, and privs (subj1[ADMIN])

    // test:anotherFolder2 (subj1[ADMIN])

    // test:anotherFolder3 (subj1[ADMIN])
    // test:anotherFolder3:someFolder5 (subj1[ADMIN])
    // test:anotherFolder3:someFolder6

    // test:anotherFolder4

    // test:anotherFolder7 (subj1[ADMIN])
    // test:anotherFolder7:someFolder8 (subj1[ADMIN])

    stemObliterate2setup();

    // subj7 can do nothing
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);

    Group group = GroupFinder.findByName(grouperSession, "test:someGroupA", true);
    assertNotNull(group);
    assertTrue(group.hasMember(SubjectTestHelper.SUBJ9));


    Stem testStem = StemFinder.findByName(grouperSession, "test", true);
  
    int count = testStem.deleteGroupMemberships(false, false, Scope.ONE).size();
    assertEquals(1, count);
  
    GrouperSession.stopQuietly(grouperSession);
    
    grouperSession = GrouperSession.startRootSession();
    
    group = GroupFinder.findByName(grouperSession, "test:someGroupA", false);
    assertNotNull(group);
    assertFalse(group.hasMember(SubjectTestHelper.SUBJ9));

    group = GroupFinder.findByName(grouperSession, "test:someGroupB", false);
    assertNotNull(group);
    assertTrue(group.hasMember(SubjectTestHelper.SUBJ9));
}

  /**
   * 
   */
  public void testStemObliterate2Group4() {
  
  
    // test stem has objects and privs (subj0[ADMIN], subj1[ADMIN])
    // test:someGroupA has a member: subj9, and privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someAttrDefA has privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someAttrDefNameA has effective privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someGroupB has privs (subj1[ADMIN])
    // test:someAttrDefB has privs (subj1[ADMIN])
    // test:someAttrDefNameB has effective privs (subj1[ADMIN])
  
    // test:anotherFolder (subj0[ADMIN], subj1[ADMIN])
    // test:anotherFolder:someGroup3 has a member: subj9, and privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:anotherFolder:someGroup4 has privs (subj1[ADMIN])
  
    // test:anotherFolder2 (subj1[ADMIN])
  
    // test:anotherFolder3 (subj1[ADMIN])
    // test:anotherFolder3:someFolder5 (subj1[ADMIN])
    // test:anotherFolder3:someFolder6
  
    // test:anotherFolder4
  
    // test:anotherFolder7 (subj1[ADMIN])
    // test:anotherFolder7:someFolder8 (subj1[ADMIN])
  
    stemObliterate2setup();
  
    // subj0 can delete two groups
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
  
    Group group = GroupFinder.findByName(grouperSession, "test:someGroupA", true);
    assertNotNull(group);
    group = GroupFinder.findByName(grouperSession, "test:anotherFolder:someGroup3", true);
    assertNotNull(group);
    
    Stem testStem = StemFinder.findByName(grouperSession, "test", true);
  
    int count = testStem.deleteGroups(false, false, Scope.SUB).size();
    assertEquals(2, count);
  
    GrouperSession.stopQuietly(grouperSession);
    
    grouperSession = GrouperSession.startRootSession();
    
    group = GroupFinder.findByName(grouperSession, "test:someGroupA", false);
    assertNull(group);
    group = GroupFinder.findByName(grouperSession, "test:anotherFolder:someGroup3", false);
    assertNull(group);
  }

  /**
   * 
   */
  public void testStemObliterate2AttributeDef0() {
  
    // test stem has objects and privs (subj0[ADMIN], subj1[ADMIN])
    // test:someGroupA has a member: subj9, and privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someAttrDefA has privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someAttrDefNameA has effective privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someGroupB has a member: subj9, and privs (subj1[ADMIN])
    // test:someAttrDefB has privs (subj1[ADMIN])
    // test:someAttrDefNameB has effective privs (subj1[ADMIN])

    // test:anotherFolder (subj0[ADMIN], subj1[ADMIN])
    // test:anotherFolder:someGroup3 has a member: subj9, and privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:anotherFolder:someGroup4 has a member: subj9, and privs (subj1[ADMIN])
    // test:anotherFolder:attributeDef3 has privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:anotherFolder:attributeDefName3 has effective privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:anotherFolder:attributeDef4 has privs (subj1[ADMIN])
    // test:anotherFolder:attributeDefName4 has effective privs (subj1[ADMIN])

    // test:anotherFolder2 (subj1[ADMIN])

    // test:anotherFolder3 (subj1[ADMIN])
    // test:anotherFolder3:someFolder5 (subj1[ADMIN])
    // test:anotherFolder3:someFolder6

    // test:anotherFolder4

    // test:anotherFolder7 (subj1[ADMIN])
    // test:anotherFolder7:someFolder8 (subj1[ADMIN])
  
    stemObliterate2setup();
  
    // subj7 can do nothing
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ7);
  
    Stem testStem = StemFinder.findByName(grouperSession, "test", true);
  
    int count = testStem.deleteAttributeDefs(false, false, Scope.ONE).size();
    assertEquals(0, count);
    
    assertFalse(testStem.isCanObliterate());
  }

  /**
   * 
   */
  public void testStemObliterate2AttributeDef1() {
  
    // test stem has objects and privs (subj0[ADMIN], subj1[ADMIN])
    // test:someGroupA has a member: subj9, and privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someAttrDefA has privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someAttrDefNameA has effective privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someGroupB has a member: subj9, and privs (subj1[ADMIN])
    // test:someAttrDefB has privs (subj1[ADMIN])
    // test:someAttrDefNameB has effective privs (subj1[ADMIN])
  
    // test:anotherFolder (subj0[ADMIN], subj1[ADMIN])
    // test:anotherFolder:someGroup3 has a member: subj9, and privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:anotherFolder:someGroup4 has a member: subj9, and privs (subj1[ADMIN])
    // test:anotherFolder:attributeDef3 has privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:anotherFolder:attributeDefName3 has effective privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:anotherFolder:attributeDef4 has privs (subj1[ADMIN])
    // test:anotherFolder:attributeDefName4 has effective privs (subj1[ADMIN])
  
    // test:anotherFolder2 (subj1[ADMIN])
  
    // test:anotherFolder3 (subj1[ADMIN])
    // test:anotherFolder3:someFolder5 (subj1[ADMIN])
    // test:anotherFolder3:someFolder6
  
    // test:anotherFolder4
  
    // test:anotherFolder7 (subj1[ADMIN])
    // test:anotherFolder7:someFolder8 (subj1[ADMIN])
  
    stemObliterate2setup();
  
    // subj7 can do nothing
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ2);
  
    Stem testStem = StemFinder.findByName(grouperSession, "test", true);
  
    int count = testStem.deleteAttributeDefs(false, false, Scope.ONE).size();
    assertEquals(0, count);
    
    assertFalse(testStem.isCanObliterate());

  }

  /**
   * 
   */
  public void testStemObliterate2AttributeDef2() {
  
    // test stem has objects and privs (subj0[ADMIN], subj1[ADMIN])
    // test:someGroupA has a member: subj9, and privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someAttrDefA has privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someAttrDefNameA has effective privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someGroupB has a member: subj9, and privs (subj1[ADMIN])
    // test:someAttrDefB has privs (subj1[ADMIN])
    // test:someAttrDefNameB has effective privs (subj1[ADMIN])

    // test:anotherFolder (subj0[ADMIN], subj1[ADMIN])
    // test:anotherFolder:someGroup3 has a member: subj9, and privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:anotherFolder:someGroup4 has a member: subj9, and privs (subj1[ADMIN])
    // test:anotherFolder:attributeDef3 has privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:anotherFolder:attributeDefName3 has effective privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:anotherFolder:attributeDef4 has privs (subj1[ADMIN])
    // test:anotherFolder:attributeDefName4 has effective privs (subj1[ADMIN])

    // test:anotherFolder2 (subj1[ADMIN])

    // test:anotherFolder3 (subj1[ADMIN])
    // test:anotherFolder3:someFolder5 (subj1[ADMIN])
    // test:anotherFolder3:someFolder6

    // test:anotherFolder4

    // test:anotherFolder7 (subj1[ADMIN])
    // test:anotherFolder7:someFolder8 (subj1[ADMIN])
  
    stemObliterate2setup();
  
    // subj1 can ddelete 2 groups
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
  
    AttributeDef attributeDef = AttributeDefFinder.findByName("test:someAttrDefA", true);
    assertNotNull(attributeDef);
    attributeDef = AttributeDefFinder.findByName("test:someAttrDefB", true);
    assertNotNull(attributeDef);
    
    Stem testStem = StemFinder.findByName(grouperSession, "test", true);

    assertTrue(testStem.isCanObliterate());

    int count = testStem.deleteAttributeDefs(false, false, Scope.ONE).size();
    assertEquals(2, count);
  
    GrouperSession.stopQuietly(grouperSession);
    
    grouperSession = GrouperSession.startRootSession();
    
    attributeDef = AttributeDefFinder.findByName("test:someAttrDefA", false);
    assertNull(attributeDef);
    attributeDef = AttributeDefFinder.findByName("test:someAttrDefB", false);
    assertNull(attributeDef);
  }

  /**
   * 
   */
  public void testStemObliterate2AttributeDef3() {
  
    // test stem has objects and privs (subj0[ADMIN], subj1[ADMIN])
    // test:someGroupA has a member: subj9, and privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someAttrDefA has privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someAttrDefNameA has effective privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someGroupB has a member: subj9, and privs (subj1[ADMIN])
    // test:someAttrDefB has privs (subj1[ADMIN])
    // test:someAttrDefNameB has effective privs (subj1[ADMIN])
  
    // test:anotherFolder (subj0[ADMIN], subj1[ADMIN])
    // test:anotherFolder:someGroup3 has a member: subj9, and privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:anotherFolder:someGroup4 has a member: subj9, and privs (subj1[ADMIN])
    // test:anotherFolder:attributeDef3 has privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:anotherFolder:attributeDefName3 has effective privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:anotherFolder:attributeDef4 has privs (subj1[ADMIN])
    // test:anotherFolder:attributeDefName4 has effective privs (subj1[ADMIN])
  
    // test:anotherFolder2 (subj1[ADMIN])
  
    // test:anotherFolder3 (subj1[ADMIN])
    // test:anotherFolder3:someFolder5 (subj1[ADMIN])
    // test:anotherFolder3:someFolder6
  
    // test:anotherFolder4
  
    // test:anotherFolder7 (subj1[ADMIN])
    // test:anotherFolder7:someFolder8 (subj1[ADMIN])
  
    stemObliterate2setup();

    // subj1 can ddelete 2 groups
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);

    AttributeDef attributeDef = AttributeDefFinder.findByName("test:someAttrDefA", false);
    assertNotNull(attributeDef);

    Stem testStem = StemFinder.findByName(grouperSession, "test", true);

    assertFalse(testStem.isCanObliterate());

    Stem.StemObliterateResults stemObliterateResults = Stem.retrieveObliterateResults();
    assertEquals(2, stemObliterateResults.getStemCount());
    assertEquals(9, stemObliterateResults.getStemCountTotal());
    assertEquals(2, stemObliterateResults.getGroupCount());
    assertEquals(4, stemObliterateResults.getGroupCountTotal());
    assertEquals(2, stemObliterateResults.getAttributeDefCount());
    assertEquals(4, stemObliterateResults.getAttributeDefCountTotal());
    assertEquals(2, stemObliterateResults.getAttributeDefNameCount());
    assertEquals(4, stemObliterateResults.getAttributeDefNameCountTotal());
    
    int count = testStem.deleteAttributeDefs(false, false, Scope.ONE).size();
    assertEquals(1, count);

    GrouperSession.stopQuietly(grouperSession);
    
    grouperSession = GrouperSession.startRootSession();

    assertTrue(testStem.isCanObliterate());

    attributeDef = AttributeDefFinder.findByName("test:someAttrDefA", false);
    assertNull(attributeDef);
    attributeDef = AttributeDefFinder.findByName("test:someAttrDefB", false);
    assertNotNull(attributeDef);
  }

  /**
   * 
   */
  public void testStemObliterate2AttributeDef4() {
  
    // test stem has objects and privs (subj0[ADMIN], subj1[ADMIN])
    // test:someGroupA has a member: subj9, and privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someAttrDefA has privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someAttrDefNameA has effective privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someGroupB has a member: subj9, and privs (subj1[ADMIN])
    // test:someAttrDefB has privs (subj1[ADMIN])
    // test:someAttrDefNameB has effective privs (subj1[ADMIN])
  
    // test:anotherFolder (subj0[ADMIN], subj1[ADMIN])
    // test:anotherFolder:someGroup3 has a member: subj9, and privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:anotherFolder:someGroup4 has a member: subj9, and privs (subj1[ADMIN])
    // test:anotherFolder:attributeDef3 has privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:anotherFolder:attributeDefName3 has effective privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:anotherFolder:attributeDef4 has privs (subj1[ADMIN])
    // test:anotherFolder:attributeDefName4 has effective privs (subj1[ADMIN])
  
    // test:anotherFolder2 (subj1[ADMIN])
  
    // test:anotherFolder3 (subj1[ADMIN])
    // test:anotherFolder3:someFolder5 (subj1[ADMIN])
    // test:anotherFolder3:someFolder6
  
    // test:anotherFolder4
  
    // test:anotherFolder7 (subj1[ADMIN])
    // test:anotherFolder7:someFolder8 (subj1[ADMIN])
  
    stemObliterate2setup();
  
    // subj1 can ddelete 2 groups
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
  
    AttributeDef attributeDef = AttributeDefFinder.findByName("test:someAttrDefA", false);
    assertNotNull(attributeDef);
    attributeDef = AttributeDefFinder.findByName("test:anotherFolder:attributeDef3", false);
    assertNotNull(attributeDef);
  
    Stem testStem = StemFinder.findByName(grouperSession, "test", true);
  
    int count = testStem.deleteAttributeDefs(false, false, Scope.SUB).size();
    assertEquals(2, count);
  
    GrouperSession.stopQuietly(grouperSession);

    grouperSession = GrouperSession.startRootSession();

    attributeDef = AttributeDefFinder.findByName("test:someAttrDefA", false);
    assertNull(attributeDef);
    attributeDef = AttributeDefFinder.findByName("test:anotherFolder:attributeDef3", false);
    assertNull(attributeDef);
  }

  /**
   * 
   */
  public void testStemObliterate2AttributeDefName0() {
  
    // test stem has objects and privs (subj0[ADMIN], subj1[ADMIN])
    // test:someGroupA has a member: subj9, and privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someAttrDefA has privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someAttrDefNameA has effective privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someGroupB has a member: subj9, and privs (subj1[ADMIN])
    // test:someAttrDefB has privs (subj1[ADMIN])
    // test:someAttrDefNameB has effective privs (subj1[ADMIN])
  
    // test:anotherFolder (subj0[ADMIN], subj1[ADMIN])
    // test:anotherFolder:someGroup3 has a member: subj9, and privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:anotherFolder:someGroup4 has a member: subj9, and privs (subj1[ADMIN])
    // test:anotherFolder:attributeDef3 has privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:anotherFolder:attributeDefName3 has effective privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:anotherFolder:attributeDef4 has privs (subj1[ADMIN])
    // test:anotherFolder:attributeDefName4 has effective privs (subj1[ADMIN])
  
    // test:anotherFolder2 (subj1[ADMIN])
  
    // test:anotherFolder3 (subj1[ADMIN])
    // test:anotherFolder3:someFolder5 (subj1[ADMIN])
    // test:anotherFolder3:someFolder6
  
    // test:anotherFolder4
  
    // test:anotherFolder7 (subj1[ADMIN])
    // test:anotherFolder7:someFolder8 (subj1[ADMIN])
  
    stemObliterate2setup();
  
    // subj7 can do nothing
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ7);
  
    Stem testStem = StemFinder.findByName(grouperSession, "test", true);
  
    int count = testStem.deleteAttributeDefNames(false, false, Scope.ONE).size();
    assertEquals(0, count);
    
    assertFalse(testStem.isCanObliterate());

  }

  /**
   * 
   */
  public void testStemObliterate2AttributeDefName1() {
  
    // test stem has objects and privs (subj0[ADMIN], subj1[ADMIN])
    // test:someGroupA has a member: subj9, and privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someAttrDefA has privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someAttrDefNameA has effective privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someGroupB has a member: subj9, and privs (subj1[ADMIN])
    // test:someAttrDefB has privs (subj1[ADMIN])
    // test:someAttrDefNameB has effective privs (subj1[ADMIN])
  
    // test:anotherFolder (subj0[ADMIN], subj1[ADMIN])
    // test:anotherFolder:someGroup3 has a member: subj9, and privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:anotherFolder:someGroup4 has a member: subj9, and privs (subj1[ADMIN])
    // test:anotherFolder:attributeDef3 has privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:anotherFolder:attributeDefName3 has effective privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:anotherFolder:attributeDef4 has privs (subj1[ADMIN])
    // test:anotherFolder:attributeDefName4 has effective privs (subj1[ADMIN])
  
    // test:anotherFolder2 (subj1[ADMIN])
  
    // test:anotherFolder3 (subj1[ADMIN])
    // test:anotherFolder3:someFolder5 (subj1[ADMIN])
    // test:anotherFolder3:someFolder6
  
    // test:anotherFolder4
  
    // test:anotherFolder7 (subj1[ADMIN])
    // test:anotherFolder7:someFolder8 (subj1[ADMIN])
  
    stemObliterate2setup();
  
    // subj7 can do nothing
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ2);
  
    Stem testStem = StemFinder.findByName(grouperSession, "test", true);

    assertFalse(testStem.isCanObliterate());

    int count = testStem.deleteAttributeDefNames(false, false, Scope.ONE).size();
    assertEquals(0, count);
  }

  /**
   * 
   */
  public void testStemObliterate2AttributeDefName2() {
  
    // test stem has objects and privs (subj0[ADMIN], subj1[ADMIN])
    // test:someGroupA has a member: subj9, and privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someAttrDefA has privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someAttrDefNameA has effective privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someGroupB has a member: subj9, and privs (subj1[ADMIN])
    // test:someAttrDefB has privs (subj1[ADMIN])
    // test:someAttrDefNameB has effective privs (subj1[ADMIN])
  
    // test:anotherFolder (subj0[ADMIN], subj1[ADMIN])
    // test:anotherFolder:someGroup3 has a member: subj9, and privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:anotherFolder:someGroup4 has a member: subj9, and privs (subj1[ADMIN])
    // test:anotherFolder:attributeDef3 has privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:anotherFolder:attributeDefName3 has effective privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:anotherFolder:attributeDef4 has privs (subj1[ADMIN])
    // test:anotherFolder:attributeDefName4 has effective privs (subj1[ADMIN])
  
    // test:anotherFolder2 (subj1[ADMIN])
  
    // test:anotherFolder3 (subj1[ADMIN])
    // test:anotherFolder3:someFolder5 (subj1[ADMIN])
    // test:anotherFolder3:someFolder6
  
    // test:anotherFolder4
  
    // test:anotherFolder7 (subj1[ADMIN])
    // test:anotherFolder7:someFolder8 (subj1[ADMIN])
  
    stemObliterate2setup();
  
    // subj1 can ddelete 2 groups
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
  
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName("test:someAttrDefNameA", true);
    assertNotNull(attributeDefName);
    attributeDefName = AttributeDefNameFinder.findByName("test:someAttrDefNameB", true);
    assertNotNull(attributeDefName);
    
    Stem testStem = StemFinder.findByName(grouperSession, "test", true);

    assertTrue(testStem.isCanObliterate());

    int count = testStem.deleteAttributeDefNames(false, false, Scope.ONE).size();
    assertEquals(2, count);
  
    GrouperSession.stopQuietly(grouperSession);
    
    grouperSession = GrouperSession.startRootSession();
    
    attributeDefName = AttributeDefNameFinder.findByName("test:someAttrDefNameA", false);
    assertNull(attributeDefName);
    attributeDefName = AttributeDefNameFinder.findByName("test:someAttrDefNameB", false);
    assertNull(attributeDefName);
  }

  /**
   * 
   */
  public void testStemObliterate2AttributeDefName3() {
  
    // test stem has objects and privs (subj0[ADMIN], subj1[ADMIN])
    // test:someGroupA has a member: subj9, and privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someAttrDefA has privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someAttrDefNameA has effective privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someGroupB has a member: subj9, and privs (subj1[ADMIN])
    // test:someAttrDefB has privs (subj1[ADMIN])
    // test:someAttrDefNameB has effective privs (subj1[ADMIN])
  
    // test:anotherFolder (subj0[ADMIN], subj1[ADMIN])
    // test:anotherFolder:someGroup3 has a member: subj9, and privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:anotherFolder:someGroup4 has a member: subj9, and privs (subj1[ADMIN])
    // test:anotherFolder:attributeDef3 has privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:anotherFolder:attributeDefName3 has effective privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:anotherFolder:attributeDef4 has privs (subj1[ADMIN])
    // test:anotherFolder:attributeDefName4 has effective privs (subj1[ADMIN])
  
    // test:anotherFolder2 (subj1[ADMIN])
  
    // test:anotherFolder3 (subj1[ADMIN])
    // test:anotherFolder3:someFolder5 (subj1[ADMIN])
    // test:anotherFolder3:someFolder6
  
    // test:anotherFolder4
  
    // test:anotherFolder7 (subj1[ADMIN])
    // test:anotherFolder7:someFolder8 (subj1[ADMIN])
  
    stemObliterate2setup();
  
    // subj1 can ddelete 2 groups
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
  
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName("test:someAttrDefNameA", false);
    assertNotNull(attributeDefName);
  
    Stem testStem = StemFinder.findByName(grouperSession, "test", true);
  
    assertFalse(testStem.isCanObliterate());

    int count = testStem.deleteAttributeDefNames(false, false, Scope.ONE).size();
    assertEquals(1, count);
  
    GrouperSession.stopQuietly(grouperSession);
    
    grouperSession = GrouperSession.startRootSession();
    
    attributeDefName = AttributeDefNameFinder.findByName("test:someAttrDefNameA", false);
    assertNull(attributeDefName);
    attributeDefName = AttributeDefNameFinder.findByName("test:someAttrDefNameB", false);
    assertNotNull(attributeDefName);
  }

  /**
   * 
   */
  public void testStemObliterate2AttributeDefName4() {
  
    // test stem has objects and privs (subj0[ADMIN], subj1[ADMIN])
    // test:someGroupA has a member: subj9, and privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someAttrDefA has privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someAttrDefNameA has effective privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:someGroupB has a member: subj9, and privs (subj1[ADMIN])
    // test:someAttrDefB has privs (subj1[ADMIN])
    // test:someAttrDefNameB has effective privs (subj1[ADMIN])
  
    // test:anotherFolder (subj0[ADMIN], subj1[ADMIN])
    // test:anotherFolder:someGroup3 has a member: subj9, and privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:anotherFolder:someGroup4 has a member: subj9, and privs (subj1[ADMIN])
    // test:anotherFolder:attributeDef3 has privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:anotherFolder:attributeDefName3 has effective privs (subj0[ADMIN], subj1[ADMIN], subj2[READ,UPDATE])
    // test:anotherFolder:attributeDef4 has privs (subj1[ADMIN])
    // test:anotherFolder:attributeDefName4 has effective privs (subj1[ADMIN])
  
    // test:anotherFolder2 (subj1[ADMIN])
  
    // test:anotherFolder3 (subj1[ADMIN])
    // test:anotherFolder3:someFolder5 (subj1[ADMIN])
    // test:anotherFolder3:someFolder6
  
    // test:anotherFolder4
  
    // test:anotherFolder7 (subj1[ADMIN])
    // test:anotherFolder7:someFolder8 (subj1[ADMIN])
  
    stemObliterate2setup();
  
    // subj1 can ddelete 2 groups
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
  
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName("test:someAttrDefNameA", false);
    assertNotNull(attributeDefName);
    attributeDefName = AttributeDefNameFinder.findByName("test:anotherFolder:attributeDefName3", false);
    assertNotNull(attributeDefName);
  
    Stem testStem = StemFinder.findByName(grouperSession, "test", true);
  
    int count = testStem.deleteAttributeDefs(false, false, Scope.SUB).size();
    assertEquals(2, count);
  
    GrouperSession.stopQuietly(grouperSession);
  
    grouperSession = GrouperSession.startRootSession();
  
    attributeDefName = AttributeDefNameFinder.findByName("test:someAttrDefNameA", false);
    assertNull(attributeDefName);
    attributeDefName = AttributeDefNameFinder.findByName("test:anotherFolder:attributeDefName3", false);
    assertNull(attributeDefName);
  }
}