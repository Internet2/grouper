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
/**
 * 
 */
package edu.internet2.middleware.grouper.rules;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Set;

import junit.textui.TestRunner;

import org.apache.commons.lang.exception.ExceptionUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.SubjectFinder.RestrictSourceForGroup;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderType;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;
import edu.internet2.middleware.grouper.permissions.role.Role;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.util.GrouperEmail;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.SourceManager;
import edu.internet2.middleware.subject.provider.SubjectImpl;


/**
 * test rule api
 * @author mchyzer
 */
public class RuleApiTest extends GrouperTest {

  /**
   * @param name
   */
  public RuleApiTest(String name) {
    super(name);
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new RuleApiTest("testNoNeedForInheritedAdminPrivileges"));
    TestRunner.run(new RuleApiTest("testNoNeedForWheelOrRootPrivileges"));
    TestRunner.run(new RuleApiTest("testInheritAttributeDefPrivilegesRemove"));
    TestRunner.run(new RuleApiTest("testInheritFolderPrivilegesRemove"));
    TestRunner.run(new RuleApiTest("testInheritGroupPrivilegesRemoveWithLikeStringNotMatch"));
    TestRunner.run(new RuleApiTest("testInheritGroupPrivilegesRemoveWithLikeString"));
    TestRunner.run(new RuleApiTest("testInheritGroupPrivilegesRemove"));
    
    
  }

  /**
   * @see GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "false");

  }

  /**
   * 
   */
  public void testReassignAttributeDefPrivilegesIfFromGroup() {
    
    assertTrue(RuleSubjectActAs.allowedToActAs(null, SubjectFinder.findRootSubject(), new SubjectImpl("GrouperSystem", null, null, null, "g:isa")));
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem2 = new StemSave(grouperSession).assignName("stem2").assignCreateParentStemsIfNotExist(true).save();
    Stem stem2sub = new StemSave(grouperSession).assignName("stem2:sub").assignCreateParentStemsIfNotExist(true).save();
    @SuppressWarnings("unused")
    Stem stem2sub2 = new StemSave(grouperSession).assignName("stem2:sub2").assignCreateParentStemsIfNotExist(true).save();
    Stem stem2sub3 = new StemSave(grouperSession).assignName("stem2:sub3").assignCreateParentStemsIfNotExist(true).save();
    Stem stem2sub4 = new StemSave(grouperSession).assignName("stem2:sub4").assignCreateParentStemsIfNotExist(true).save();
    Stem stem2sub5 = new StemSave(grouperSession).assignName("stem2:sub5").assignCreateParentStemsIfNotExist(true).save();
    Stem stem1 = new StemSave(grouperSession).assignName("stem1").assignCreateParentStemsIfNotExist(true).save();
  
    Group stem1admins = new GroupSave(grouperSession).assignName("stem1:admins").assignCreateParentStemsIfNotExist(true).save();
    Group stem1admins2 = new GroupSave(grouperSession).assignName("stem1:admins2").assignCreateParentStemsIfNotExist(true).save();
    Group stem2sub3wheel = new GroupSave(grouperSession).assignName("stem2:sub3wheel").assignCreateParentStemsIfNotExist(true).save();
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.use", "true");
    Group wheelGroup = new GroupSave(grouperSession).assignName("etc:sysadmingroup").assignCreateParentStemsIfNotExist(true).save();
  
    //subject 0 is wheel
    wheelGroup.addMember(SubjectTestHelper.SUBJ0);
    wheelGroup.addMember(stem2sub3wheel.toSubject());
  
    //subject 1 is indirect wheel
    stem2sub3wheel.addMember(SubjectTestHelper.SUBJ1);
    
    //subject 2 is stem1admin
    stem1admins.addMember(SubjectTestHelper.SUBJ2);
    
    //subject 3 is stem1admins2
    stem1admins2.addMember(SubjectTestHelper.SUBJ3);
    
    //subject4 is not in a group
    stem2.grantPriv(SubjectTestHelper.SUBJ4, NamingPrivilege.CREATE);
    
    //subject5 is in stem1admins and stem1admins2
    stem1admins.addMember(SubjectTestHelper.SUBJ5);
    stem1admins2.addMember(SubjectTestHelper.SUBJ5);
  
    //subject6 is in stem1admins and NOT stem1admins2
    stem1admins.addMember(SubjectTestHelper.SUBJ6);
  
    //stem2 is admined by stem1admins
    stem2.grantPriv(stem1admins.toSubject(), NamingPrivilege.CREATE);
  
    //stem2sub is admined by stem1admins2
    stem2sub.grantPriv(stem1admins2.toSubject(), NamingPrivilege.CREATE);
    
    //stem2sub2 is not admined by a group
  
    //stem1 is admined by stem1admin
    stem1.grantPriv(stem1admins.toSubject(), NamingPrivilege.CREATE, false);
  
    //stem2sub3 is admined by stem1admins2
    stem2sub3.grantPriv(stem2sub3wheel.toSubject(), NamingPrivilege.CREATE);
    
    //stem2sub4 is admined by wheel
    stem2sub4.grantPriv(wheelGroup.toSubject(), NamingPrivilege.CREATE);
  
    //stem2sub5 is admined by two groups
    stem2sub5.grantPriv(stem1admins.toSubject(), NamingPrivilege.CREATE);
    stem2sub5.grantPriv(stem1admins2.toSubject(), NamingPrivilege.CREATE);
        
    //add a rule on stem2 saying if you create a group underneath, then remove admin if in another group which has create on stem
    RuleApi.reassignAttributeDefPrivilegesIfFromGroup(SubjectFinder.findRootSubject(), stem2, Stem.Scope.SUB);

    long initialFirings = RuleEngine.ruleFirings;
    GrouperSession.stopQuietly(grouperSession);
    
    AttributeDef stem2testAttributeDef = null;
    AttributeDef stem1testAttributeDef = null;
    AttributeDef stem2subTestAttributeDef = null;
    AttributeDef stem2sub5testAttributeDef = null;
    
    //################################## SUBJ 0 wheel should be removed, nothing added
    initialFirings = RuleEngine.ruleFirings;
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    stem2testAttributeDef = new AttributeDefSave(grouperSession).assignName("stem2:testAttributeDef").assignCreateParentStemsIfNotExist(true).save();
  
    //count rule firings
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
  
    assertFalse(PrivilegeHelper.hasImmediatePrivilege(stem2testAttributeDef, SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_ADMIN));
    assertFalse(PrivilegeHelper.hasImmediatePrivilege(stem2testAttributeDef, wheelGroup.toSubject(), AttributeDefPrivilege.ATTR_ADMIN));
    
    stem2testAttributeDef.delete();
    
    GrouperSession.stopQuietly(grouperSession);
  
    //################################## stem1 no rule, SUBJ 0 wheel should not be removed, nothing added
    initialFirings = RuleEngine.ruleFirings;
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    stem1testAttributeDef = new AttributeDefSave(grouperSession).assignName("stem1:testAttributeDef").assignCreateParentStemsIfNotExist(true).save();
  
    //count rule firings
    assertEquals(initialFirings, RuleEngine.ruleFirings);
  
    assertTrue(PrivilegeHelper.hasImmediatePrivilege(stem1testAttributeDef, SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_ADMIN));
  
    stem1testAttributeDef.delete();
  
    GrouperSession.stopQuietly(grouperSession);
  
    //################################## SUBJ 1 wheel indirect should be removed, nothing added
    initialFirings = RuleEngine.ruleFirings;
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
    
    stem2testAttributeDef = new AttributeDefSave(grouperSession).assignName("stem2:testAttributeDef").assignCreateParentStemsIfNotExist(true).save();
  
    //count rule firings
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
  
    assertFalse(PrivilegeHelper.hasImmediatePrivilege(stem2testAttributeDef, SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_ADMIN));
    assertFalse(PrivilegeHelper.hasImmediatePrivilege(stem2testAttributeDef, stem2sub3wheel.toSubject(), AttributeDefPrivilege.ATTR_ADMIN));
    
    stem2testAttributeDef.delete();
    
    GrouperSession.stopQuietly(grouperSession);
  
    //################################## SUBJ 2 stem1admin on stem2
    initialFirings = RuleEngine.ruleFirings;
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ2);
    
    stem2testAttributeDef = new AttributeDefSave(grouperSession).assignName("stem2:testAttributeDef").assignCreateParentStemsIfNotExist(true).save();
  
    //count rule firings
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
  
    assertFalse(PrivilegeHelper.hasImmediatePrivilege(stem2testAttributeDef, SubjectTestHelper.SUBJ2, AttributeDefPrivilege.ATTR_ADMIN));
    assertTrue(PrivilegeHelper.hasImmediatePrivilege(stem2testAttributeDef, stem1admins.toSubject(), AttributeDefPrivilege.ATTR_ADMIN));
    assertTrue(stem2testAttributeDef.getPrivilegeDelegate().hasAttrAdmin(SubjectTestHelper.SUBJ2));
    
    stem2testAttributeDef.delete();
    
    GrouperSession.stopQuietly(grouperSession);
  
    //################################## SUBJ 2 stem1admin on stem1
    initialFirings = RuleEngine.ruleFirings;
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ2);
    
    stem1testAttributeDef = new AttributeDefSave(grouperSession).assignName("stem1:testAttributeDef").assignCreateParentStemsIfNotExist(true).save();
  
    //count rule firings
    assertEquals(initialFirings, RuleEngine.ruleFirings);
  
    assertTrue(PrivilegeHelper.hasImmediatePrivilege(stem1testAttributeDef, SubjectTestHelper.SUBJ2, AttributeDefPrivilege.ATTR_ADMIN));
    assertFalse(PrivilegeHelper.hasImmediatePrivilege(stem1testAttributeDef, stem1admins.toSubject(), AttributeDefPrivilege.ATTR_ADMIN));
    assertTrue(stem1testAttributeDef.getPrivilegeDelegate().hasAttrAdmin(SubjectTestHelper.SUBJ2));
    
    stem1testAttributeDef.delete();
    
    GrouperSession.stopQuietly(grouperSession);
  
    //################################## SUBJ 3 stem1admins2 stem2:sub
    initialFirings = RuleEngine.ruleFirings;
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ3);
    
    stem2subTestAttributeDef = new AttributeDefSave(grouperSession).assignName("stem2:sub:testAttributeDef").assignCreateParentStemsIfNotExist(true).save();
  
    //count rule firings
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
  
    assertFalse(PrivilegeHelper.hasImmediatePrivilege(stem2subTestAttributeDef, SubjectTestHelper.SUBJ3, AttributeDefPrivilege.ATTR_ADMIN));
    assertTrue(PrivilegeHelper.hasImmediatePrivilege(stem2subTestAttributeDef, stem1admins2.toSubject(), AttributeDefPrivilege.ATTR_ADMIN));
    assertTrue(stem2subTestAttributeDef.getPrivilegeDelegate().hasAttrAdmin(SubjectTestHelper.SUBJ3));
    
    stem2subTestAttributeDef.delete();
    
    GrouperSession.stopQuietly(grouperSession);
  
    //################################## SUBJ 4 stem2
    initialFirings = RuleEngine.ruleFirings;
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ4);
    
    stem2testAttributeDef = new AttributeDefSave(grouperSession).assignName("stem2:testAttributeDef").assignCreateParentStemsIfNotExist(true).save();
  
    //count rule firings
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
  
    assertTrue(PrivilegeHelper.hasImmediatePrivilege(stem2testAttributeDef, SubjectTestHelper.SUBJ4, AttributeDefPrivilege.ATTR_ADMIN));
    assertTrue(stem2testAttributeDef.getPrivilegeDelegate().hasAttrAdmin(SubjectTestHelper.SUBJ4));
    
    stem2testAttributeDef.delete();
    
    GrouperSession.stopQuietly(grouperSession);
  
    //################################## SUBJ 3 stem1admins2 stem2:sub
    initialFirings = RuleEngine.ruleFirings;
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ3);
    
    stem2subTestAttributeDef = new AttributeDefSave(grouperSession).assignName("stem2:sub:testAttributeDef").assignCreateParentStemsIfNotExist(true).save();
  
    //count rule firings
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
  
    assertFalse(PrivilegeHelper.hasImmediatePrivilege(stem2subTestAttributeDef, SubjectTestHelper.SUBJ3, AttributeDefPrivilege.ATTR_ADMIN));
    assertTrue(PrivilegeHelper.hasImmediatePrivilege(stem2subTestAttributeDef, stem1admins2.toSubject(), AttributeDefPrivilege.ATTR_ADMIN));
    assertTrue(stem2subTestAttributeDef.getPrivilegeDelegate().hasAttrAdmin(SubjectTestHelper.SUBJ3));
    
    stem2subTestAttributeDef.delete();
    
    GrouperSession.stopQuietly(grouperSession);
  
    //################################## SUBJ 5 stem1admins, stem1admins2 stem2:sub5
    initialFirings = RuleEngine.ruleFirings;
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ5);
    
    stem2sub5testAttributeDef = new AttributeDefSave(grouperSession).assignName("stem2:sub5:testAttributeDef").assignCreateParentStemsIfNotExist(true).save();
  
    //count rule firings
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
  
    assertFalse(PrivilegeHelper.hasImmediatePrivilege(stem2sub5testAttributeDef, SubjectTestHelper.SUBJ5, AttributeDefPrivilege.ATTR_ADMIN));
    assertTrue(PrivilegeHelper.hasImmediatePrivilege(stem2sub5testAttributeDef, stem1admins.toSubject(), AttributeDefPrivilege.ATTR_ADMIN));
    assertTrue(PrivilegeHelper.hasImmediatePrivilege(stem2sub5testAttributeDef, stem1admins2.toSubject(), AttributeDefPrivilege.ATTR_ADMIN));
    assertTrue(stem2sub5testAttributeDef.getPrivilegeDelegate().hasAttrAdmin(SubjectTestHelper.SUBJ5));
    
    stem2sub5testAttributeDef.delete();
    
    GrouperSession.stopQuietly(grouperSession);
  
    //################################## SUBJ 6 stem1admins stem2:sub5
    initialFirings = RuleEngine.ruleFirings;
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ6);
    
    stem2sub5testAttributeDef = new AttributeDefSave(grouperSession).assignName("stem2:sub5:testAttributeDef").assignCreateParentStemsIfNotExist(true).save();
  
    //count rule firings
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
  
    assertFalse(PrivilegeHelper.hasImmediatePrivilege(stem2sub5testAttributeDef, SubjectTestHelper.SUBJ5, AttributeDefPrivilege.ATTR_ADMIN));
    assertTrue(PrivilegeHelper.hasImmediatePrivilege(stem2sub5testAttributeDef, stem1admins.toSubject(), AttributeDefPrivilege.ATTR_ADMIN));
    assertFalse(PrivilegeHelper.hasImmediatePrivilege(stem2sub5testAttributeDef, stem1admins2.toSubject(), AttributeDefPrivilege.ATTR_ADMIN));
    assertTrue(stem2sub5testAttributeDef.getPrivilegeDelegate().hasAttrAdmin(SubjectTestHelper.SUBJ5));
    
    stem2sub5testAttributeDef.delete();
    
    GrouperSession.stopQuietly(grouperSession);
  
    
    //################################## SUBJ 4 stem2 NO WHEEL
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.use", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.group", "");
  
    initialFirings = RuleEngine.ruleFirings;
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ4);
    
    stem2testAttributeDef = new AttributeDefSave(grouperSession).assignName("stem2:testAttributeDef").assignCreateParentStemsIfNotExist(true).save();
  
    //count rule firings
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
  
    assertTrue(PrivilegeHelper.hasImmediatePrivilege(stem2testAttributeDef, SubjectTestHelper.SUBJ4, AttributeDefPrivilege.ATTR_ADMIN));
    assertTrue(stem2testAttributeDef.getPrivilegeDelegate().hasAttrAdmin(SubjectTestHelper.SUBJ4));
    
    stem2testAttributeDef.delete();
    
    GrouperSession.stopQuietly(grouperSession);
  
    
    
    
  
  }

  /**
   * 
   */
  public void testReassignStemPrivilegesIfFromGroup() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem2 = new StemSave(grouperSession).assignName("stem2").assignCreateParentStemsIfNotExist(true).save();
    Stem stem2sub = new StemSave(grouperSession).assignName("stem2:sub").assignCreateParentStemsIfNotExist(true).save();
    @SuppressWarnings("unused")
    Stem stem2sub2 = new StemSave(grouperSession).assignName("stem2:sub2").assignCreateParentStemsIfNotExist(true).save();
    Stem stem2sub3 = new StemSave(grouperSession).assignName("stem2:sub3").assignCreateParentStemsIfNotExist(true).save();
    Stem stem2sub4 = new StemSave(grouperSession).assignName("stem2:sub4").assignCreateParentStemsIfNotExist(true).save();
    Stem stem2sub5 = new StemSave(grouperSession).assignName("stem2:sub5").assignCreateParentStemsIfNotExist(true).save();
    Stem stem1 = new StemSave(grouperSession).assignName("stem1").assignCreateParentStemsIfNotExist(true).save();

    Group stem1admins = new GroupSave(grouperSession).assignName("stem1:admins").assignCreateParentStemsIfNotExist(true).save();
    Group stem1admins2 = new GroupSave(grouperSession).assignName("stem1:admins2").assignCreateParentStemsIfNotExist(true).save();
    Group stem2sub3wheel = new GroupSave(grouperSession).assignName("stem2:sub3wheel").assignCreateParentStemsIfNotExist(true).save();

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.use", "true");
    Group wheelGroup = new GroupSave(grouperSession).assignName("etc:sysadmingroup").assignCreateParentStemsIfNotExist(true).save();

    //subject 0 is wheel
    wheelGroup.addMember(SubjectTestHelper.SUBJ0);
    wheelGroup.addMember(stem2sub3wheel.toSubject());

    //subject 1 is indirect wheel
    stem2sub3wheel.addMember(SubjectTestHelper.SUBJ1);
    
    //subject 2 is stem1admin
    stem1admins.addMember(SubjectTestHelper.SUBJ2);
    
    //subject 3 is stem1admins2
    stem1admins2.addMember(SubjectTestHelper.SUBJ3);
    
    //subject4 is not in a group
    stem2.grantPriv(SubjectTestHelper.SUBJ4, NamingPrivilege.CREATE);
    stem2.grantPriv(SubjectTestHelper.SUBJ4, NamingPrivilege.STEM);
    
    //subject5 is in stem1admins and stem1admins2
    stem1admins.addMember(SubjectTestHelper.SUBJ5);
    stem1admins2.addMember(SubjectTestHelper.SUBJ5);

    //subject6 is in stem1admins and NOT stem1admins2
    stem1admins.addMember(SubjectTestHelper.SUBJ6);

    //stem2 is admined by stem1admins
    stem2.grantPriv(stem1admins.toSubject(), NamingPrivilege.CREATE);
    stem2.grantPriv(stem1admins.toSubject(), NamingPrivilege.STEM);

    //stem2sub is admined by stem1admins2
    stem2sub.grantPriv(stem1admins2.toSubject(), NamingPrivilege.CREATE);
    stem2sub.grantPriv(stem1admins2.toSubject(), NamingPrivilege.STEM);
    
    //stem2sub2 is not admined by a group

    //stem1 is admined by stem1admin
    stem1.grantPriv(stem1admins.toSubject(), NamingPrivilege.CREATE, false);
    stem1.grantPriv(stem1admins.toSubject(), NamingPrivilege.STEM, false);

    //stem2sub3 is admined by stem1admins2
    stem2sub3.grantPriv(stem2sub3wheel.toSubject(), NamingPrivilege.CREATE);
    stem2sub3.grantPriv(stem2sub3wheel.toSubject(), NamingPrivilege.STEM);
    
    //stem2sub4 is admined by wheel
    stem2sub4.grantPriv(wheelGroup.toSubject(), NamingPrivilege.CREATE);
    stem2sub4.grantPriv(wheelGroup.toSubject(), NamingPrivilege.STEM);

    //stem2sub5 is admined by two groups
    stem2sub5.grantPriv(stem1admins.toSubject(), NamingPrivilege.CREATE);
    stem2sub5.grantPriv(stem1admins.toSubject(), NamingPrivilege.STEM);
    stem2sub5.grantPriv(stem1admins2.toSubject(), NamingPrivilege.CREATE);
    stem2sub5.grantPriv(stem1admins2.toSubject(), NamingPrivilege.STEM);
    
    //add a rule on stem2 saying if you create a group underneath, then remove admin if in another group which has create on stem
    RuleApi.reassignStemPrivilegesIfFromGroup(SubjectFinder.findRootSubject(), stem2, Stem.Scope.SUB);

    long initialFirings = RuleEngine.ruleFirings;
    GrouperSession.stopQuietly(grouperSession);
    
    Stem stem2testStem = null;
    Stem stem1testStem = null;
    Stem stem2subTestStem = null;
    Stem stem2sub5testStem = null;
    
    //################################## stem1 no rule, SUBJ 0 wheel should not be removed, nothing added
    initialFirings = RuleEngine.ruleFirings;
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    stem1testStem = new StemSave(grouperSession).assignName("stem1:testStem").assignCreateParentStemsIfNotExist(true).save();

    //count rule firings
    assertEquals(initialFirings, RuleEngine.ruleFirings);

    assertTrue(PrivilegeHelper.hasImmediatePrivilege(stem1testStem, SubjectTestHelper.SUBJ0, NamingPrivilege.STEM));


    stem1testStem.delete();

    GrouperSession.stopQuietly(grouperSession);

    //################################## SUBJ 0 wheel should be removed, nothing added
    initialFirings = RuleEngine.ruleFirings;
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    stem2testStem = new StemSave(grouperSession).assignName("stem2:testStem").assignCreateParentStemsIfNotExist(true).save();

    //count rule firings
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);

    assertFalse(PrivilegeHelper.hasImmediatePrivilege(stem2testStem, SubjectTestHelper.SUBJ0, NamingPrivilege.STEM));
    assertFalse(PrivilegeHelper.hasImmediatePrivilege(stem2testStem, wheelGroup.toSubject(), NamingPrivilege.STEM));
    
    stem2testStem.delete();
    
    GrouperSession.stopQuietly(grouperSession);

    //################################## SUBJ 1 wheel indirect should be removed, nothing added
    initialFirings = RuleEngine.ruleFirings;
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
    
    stem2testStem = new StemSave(grouperSession).assignName("stem2:testStem").assignCreateParentStemsIfNotExist(true).save();

    //count rule firings
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);

    assertFalse(PrivilegeHelper.hasImmediatePrivilege(stem2testStem, SubjectTestHelper.SUBJ1, NamingPrivilege.STEM));
    assertFalse(PrivilegeHelper.hasImmediatePrivilege(stem2testStem, stem2sub3wheel.toSubject(), NamingPrivilege.STEM));
    
    stem2testStem.delete();
    
    GrouperSession.stopQuietly(grouperSession);

    //################################## SUBJ 2 stem1admin on stem2
    initialFirings = RuleEngine.ruleFirings;
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ2);
    
    stem2testStem = new StemSave(grouperSession).assignName("stem2:testStem").assignCreateParentStemsIfNotExist(true).save();

    //count rule firings
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);

    assertFalse(PrivilegeHelper.hasImmediatePrivilege(stem2testStem, SubjectTestHelper.SUBJ2, NamingPrivilege.STEM));
    assertTrue(PrivilegeHelper.hasImmediatePrivilege(stem2testStem, stem1admins.toSubject(), NamingPrivilege.STEM));
    assertTrue(stem2testStem.hasStem(SubjectTestHelper.SUBJ2));
    
    stem2testStem.delete();
    
    GrouperSession.stopQuietly(grouperSession);

    //################################## SUBJ 2 stem1admin on stem1
    initialFirings = RuleEngine.ruleFirings;
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ2);
    
    stem1testStem = new StemSave(grouperSession).assignName("stem1:testStem").assignCreateParentStemsIfNotExist(true).save();


    //count rule firings
    assertEquals(initialFirings, RuleEngine.ruleFirings);

    assertTrue(PrivilegeHelper.hasImmediatePrivilege(stem1testStem, SubjectTestHelper.SUBJ2, NamingPrivilege.STEM));
    assertFalse(PrivilegeHelper.hasImmediatePrivilege(stem1testStem, stem1admins.toSubject(), NamingPrivilege.STEM));
    assertTrue(stem1testStem.hasStem(SubjectTestHelper.SUBJ2));
    
    stem1testStem.delete();
    
    GrouperSession.stopQuietly(grouperSession);

    //################################## SUBJ 3 stem1admins2 stem2:sub
    initialFirings = RuleEngine.ruleFirings;
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ3);
    
    stem2subTestStem = new StemSave(grouperSession).assignName("stem2:sub:testStem").assignCreateParentStemsIfNotExist(true).save();

    //count rule firings
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);

    assertFalse(PrivilegeHelper.hasImmediatePrivilege(stem2subTestStem, SubjectTestHelper.SUBJ3, NamingPrivilege.STEM));
    assertTrue(PrivilegeHelper.hasImmediatePrivilege(stem2subTestStem, stem1admins2.toSubject(), NamingPrivilege.STEM));
    assertTrue(stem2subTestStem.hasStem(SubjectTestHelper.SUBJ3));
    
    stem2subTestStem.delete();
    
    GrouperSession.stopQuietly(grouperSession);

    //################################## SUBJ 4 stem2
    initialFirings = RuleEngine.ruleFirings;
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ4);
    
    stem2testStem = new StemSave(grouperSession).assignName("stem2:testStem").assignCreateParentStemsIfNotExist(true).save();

    //count rule firings
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);

    assertTrue(PrivilegeHelper.hasImmediatePrivilege(stem2testStem, SubjectTestHelper.SUBJ4, NamingPrivilege.STEM));
    assertTrue(stem2testStem.hasStem(SubjectTestHelper.SUBJ4));
    
    stem2testStem.delete();
    
    GrouperSession.stopQuietly(grouperSession);

    //################################## SUBJ 3 stem1admins2 stem2:sub
    initialFirings = RuleEngine.ruleFirings;
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ3);
    
    stem2subTestStem = new StemSave(grouperSession).assignName("stem2:sub:testStem").assignCreateParentStemsIfNotExist(true).save();

    //count rule firings
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);

    assertFalse(PrivilegeHelper.hasImmediatePrivilege(stem2subTestStem, SubjectTestHelper.SUBJ3, NamingPrivilege.STEM));
    assertTrue(PrivilegeHelper.hasImmediatePrivilege(stem2subTestStem, stem1admins2.toSubject(), NamingPrivilege.STEM));
    assertTrue(stem2subTestStem.hasStem(SubjectTestHelper.SUBJ3));
    
    stem2subTestStem.delete();
    
    GrouperSession.stopQuietly(grouperSession);

    //################################## SUBJ 5 stem1admins, stem1admins2 stem2:sub5
    initialFirings = RuleEngine.ruleFirings;
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ5);
    
    stem2sub5testStem = new StemSave(grouperSession).assignName("stem2:sub5:testStem").assignCreateParentStemsIfNotExist(true).save();

    //count rule firings
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);

    assertFalse(PrivilegeHelper.hasImmediatePrivilege(stem2sub5testStem, SubjectTestHelper.SUBJ5, NamingPrivilege.STEM));
    assertTrue(PrivilegeHelper.hasImmediatePrivilege(stem2sub5testStem, stem1admins.toSubject(), NamingPrivilege.STEM));
    assertTrue(PrivilegeHelper.hasImmediatePrivilege(stem2sub5testStem, stem1admins2.toSubject(), NamingPrivilege.STEM));
    assertTrue(stem2sub5testStem.hasStem(SubjectTestHelper.SUBJ5));
    
    stem2sub5testStem.delete();
    
    GrouperSession.stopQuietly(grouperSession);

    //################################## SUBJ 6 stem1admins stem2:sub5
    initialFirings = RuleEngine.ruleFirings;
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ6);
    
    stem2sub5testStem = new StemSave(grouperSession).assignName("stem2:sub5:testStem").assignCreateParentStemsIfNotExist(true).save();

    //count rule firings
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);

    assertFalse(PrivilegeHelper.hasImmediatePrivilege(stem2sub5testStem, SubjectTestHelper.SUBJ5, NamingPrivilege.STEM));
    assertTrue(PrivilegeHelper.hasImmediatePrivilege(stem2sub5testStem, stem1admins.toSubject(), NamingPrivilege.STEM));
    assertFalse(PrivilegeHelper.hasImmediatePrivilege(stem2sub5testStem, stem1admins2.toSubject(), NamingPrivilege.STEM));
    assertTrue(stem2sub5testStem.hasStem(SubjectTestHelper.SUBJ5));
    
    stem2sub5testStem.delete();
    
    GrouperSession.stopQuietly(grouperSession);
    
    //################################## SUBJ 4 stem2 NO WHEEL
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.use", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.group", "");

    initialFirings = RuleEngine.ruleFirings;
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ4);
    
    stem2testStem = new StemSave(grouperSession).assignName("stem2:testStem").assignCreateParentStemsIfNotExist(true).save();

    //count rule firings
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);

    assertTrue(PrivilegeHelper.hasImmediatePrivilege(stem2testStem, SubjectTestHelper.SUBJ4, NamingPrivilege.STEM));
    assertTrue(stem2testStem.hasStem(SubjectTestHelper.SUBJ4));
    
    stem2testStem.delete();
    
    GrouperSession.stopQuietly(grouperSession);

  }
  

  /**
   * 
   */
  public void testGroupIntersection() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Subject subject0 = SubjectFinder.findById("test.subject.0", true);
    Subject subject1 = SubjectFinder.findById("test.subject.1", true);
    Subject subject9 = SubjectFinder.findById("test.subject.9", true);

    Group groupA = new GroupSave(grouperSession).assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignName("stem:a").assignCreateParentStemsIfNotExist(true).save();
    groupA.grantPriv(subject9, AccessPrivilege.ADMIN, false);

    Group groupB = new GroupSave(grouperSession).assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignName("stem:b").assignCreateParentStemsIfNotExist(true).save();
    groupB.grantPriv(subject9, AccessPrivilege.READ, false);
    
    Group groupC = new GroupSave(grouperSession).assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignName("stem:c").assignCreateParentStemsIfNotExist(true).save();
    groupC.grantPriv(subject9, AccessPrivilege.READ, false);
    
    groupB.addMember(groupC.toSubject());
    
    RuleApi.groupIntersection(subject9, groupA, groupB);
    
    groupB.addMember(subject0);
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_grouperRules");
    
    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;
    
    //doesnt do anything
    groupB.deleteMember(subject0);
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_grouperRules");
    
    assertEquals(initialFirings, RuleEngine.ruleFirings);

    groupB.addMember(subject0);
    groupA.addMember(subject0);
    
    //count rule firings
    
    groupB.deleteMember(subject0);
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_grouperRules");
    
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);

    //should come out of groupA
    assertFalse(groupA.hasMember(subject0));
    
    
    groupC.addMember(subject0);
    groupA.addMember(subject0);
    
    //count rule firings
    
    groupC.deleteMember(subject0);
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_grouperRules");
    
    assertEquals(initialFirings+2, RuleEngine.ruleFirings);

    //should come out of groupA
    assertFalse(groupA.hasMember(subject0));
    
    
    groupC.addMember(subject0);
    groupB.addMember(subject0);
    groupA.addMember(subject0);
    
    //count rule firings
    
    groupC.deleteMember(subject0);
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_grouperRules");
    
    //no change
    assertEquals(initialFirings+2, RuleEngine.ruleFirings);
    assertTrue(groupA.hasMember(subject0));
    
    groupC.addMember(subject0);
    
    //count rule firings
    
    groupB.deleteMember(subject0);
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_grouperRules");
    
    //no change
    assertEquals(initialFirings+2, RuleEngine.ruleFirings);
    assertTrue(groupA.hasMember(subject0));
    
    

    //lets someone to A
    groupA.addMember(subject1);
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_grouperRules");
    
    //run the daemon
    String status = GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);

    assertTrue(status, status.toLowerCase().contains("success"));

    //should not be in A anymore
    assertFalse(groupA.hasMember(subject1));
    
    
  }

  /**
   * 
   */
  public void testReassignGroupPrivilegesIfFromGroup() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem2 = new StemSave(grouperSession).assignName("stem2").assignCreateParentStemsIfNotExist(true).save();
    Stem stem2sub = new StemSave(grouperSession).assignName("stem2:sub").assignCreateParentStemsIfNotExist(true).save();
    @SuppressWarnings("unused")
    Stem stem2sub2 = new StemSave(grouperSession).assignName("stem2:sub2").assignCreateParentStemsIfNotExist(true).save();
    Stem stem2sub3 = new StemSave(grouperSession).assignName("stem2:sub3").assignCreateParentStemsIfNotExist(true).save();
    Stem stem2sub4 = new StemSave(grouperSession).assignName("stem2:sub4").assignCreateParentStemsIfNotExist(true).save();
    Stem stem2sub5 = new StemSave(grouperSession).assignName("stem2:sub5").assignCreateParentStemsIfNotExist(true).save();
    Stem stem1 = new StemSave(grouperSession).assignName("stem1").assignCreateParentStemsIfNotExist(true).save();

    Group stem1admins = new GroupSave(grouperSession).assignName("stem1:admins").assignCreateParentStemsIfNotExist(true).save();
    Group stem1admins2 = new GroupSave(grouperSession).assignName("stem1:admins2").assignCreateParentStemsIfNotExist(true).save();
    Group stem2sub3wheel = new GroupSave(grouperSession).assignName("stem2:sub3wheel").assignCreateParentStemsIfNotExist(true).save();

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.use", "true");
    Group wheelGroup = new GroupSave(grouperSession).assignName("etc:sysadmingroup").assignCreateParentStemsIfNotExist(true).save();

    //subject 0 is wheel
    wheelGroup.addMember(SubjectTestHelper.SUBJ0);
    wheelGroup.addMember(stem2sub3wheel.toSubject());

    //subject 1 is indirect wheel
    stem2sub3wheel.addMember(SubjectTestHelper.SUBJ1);
    
    //subject 2 is stem1admin
    stem1admins.addMember(SubjectTestHelper.SUBJ2);
    
    //subject 3 is stem1admins2
    stem1admins2.addMember(SubjectTestHelper.SUBJ3);
    
    //subject4 is not in a group
    stem2.grantPriv(SubjectTestHelper.SUBJ4, NamingPrivilege.CREATE);
    
    //subject5 is in stem1admins and stem1admins2
    stem1admins.addMember(SubjectTestHelper.SUBJ5);
    stem1admins2.addMember(SubjectTestHelper.SUBJ5);

    //subject6 is in stem1admins and NOT stem1admins2
    stem1admins.addMember(SubjectTestHelper.SUBJ6);

    //stem2 is admined by stem1admins
    stem2.grantPriv(stem1admins.toSubject(), NamingPrivilege.CREATE);

    //stem2sub is admined by stem1admins2
    stem2sub.grantPriv(stem1admins2.toSubject(), NamingPrivilege.CREATE);
    
    //stem2sub2 is not admined by a group

    //stem1 is admined by stem1admin
    stem1.grantPriv(stem1admins.toSubject(), NamingPrivilege.CREATE, false);

    //stem2sub3 is admined by stem1admins2
    stem2sub3.grantPriv(stem2sub3wheel.toSubject(), NamingPrivilege.CREATE);
    
    //stem2sub4 is admined by wheel
    stem2sub4.grantPriv(wheelGroup.toSubject(), NamingPrivilege.CREATE);

    //stem2sub5 is admined by two groups
    stem2sub5.grantPriv(stem1admins.toSubject(), NamingPrivilege.CREATE);
    stem2sub5.grantPriv(stem1admins2.toSubject(), NamingPrivilege.CREATE);
    

    Group groupA = new GroupSave(grouperSession).assignName("stem1:admins").assignCreateParentStemsIfNotExist(true).save();

    groupA.addMember(SubjectTestHelper.SUBJ0);
    
    
    //add a rule on stem2 saying if you create a group underneath, then remove admin if in another group which has create on stem
    RuleApi.reassignGroupPrivilegesIfFromGroup(SubjectFinder.findRootSubject(), stem2, Stem.Scope.SUB);

    long initialFirings = RuleEngine.ruleFirings;
    GrouperSession.stopQuietly(grouperSession);
    
    Group stem2testGroup = null;
    Group stem1testGroup = null;
    Group stem2subTestGroup = null;
    Group stem2sub5testGroup = null;
    
    //################################## GrouperSystem should be able to call this
    initialFirings = RuleEngine.ruleFirings;
    
    grouperSession = GrouperSession.startRootSession();
    
    stem2testGroup = new GroupSave(grouperSession).assignName("stem2:testGroup").assignCreateParentStemsIfNotExist(true).save();

    //count rule firings
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);

    assertFalse(PrivilegeHelper.hasImmediatePrivilege(stem2testGroup, SubjectFinder.findRootSubject(), AccessPrivilege.ADMIN));
    assertFalse(PrivilegeHelper.hasImmediatePrivilege(stem2testGroup, wheelGroup.toSubject(), AccessPrivilege.ADMIN));
    
    stem2testGroup.delete();
    
    GrouperSession.stopQuietly(grouperSession);
    
    //################################## SUBJ 0 wheel should be removed, nothing added
    initialFirings = RuleEngine.ruleFirings;
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    stem2testGroup = new GroupSave(grouperSession).assignName("stem2:testGroup").assignCreateParentStemsIfNotExist(true).save();

    //count rule firings
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);

    assertFalse(PrivilegeHelper.hasImmediatePrivilege(stem2testGroup, SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN));
    assertFalse(PrivilegeHelper.hasImmediatePrivilege(stem2testGroup, wheelGroup.toSubject(), AccessPrivilege.ADMIN));
    
    stem2testGroup.delete();
    
    GrouperSession.stopQuietly(grouperSession);

    //################################## stem1 no rule, SUBJ 0 wheel should not be removed, nothing added
    initialFirings = RuleEngine.ruleFirings;
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    stem1testGroup = new GroupSave(grouperSession).assignName("stem1:testGroup").assignCreateParentStemsIfNotExist(true).save();

    //count rule firings
    assertEquals(initialFirings, RuleEngine.ruleFirings);

    assertTrue(PrivilegeHelper.hasImmediatePrivilege(stem1testGroup, SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN));

    stem1testGroup.delete();

    GrouperSession.stopQuietly(grouperSession);

    //################################## SUBJ 1 wheel indirect should be removed, nothing added
    initialFirings = RuleEngine.ruleFirings;
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
    
    stem2testGroup = new GroupSave(grouperSession).assignName("stem2:testGroup").assignCreateParentStemsIfNotExist(true).save();

    //count rule firings
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);

    assertFalse(PrivilegeHelper.hasImmediatePrivilege(stem2testGroup, SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN));
    assertFalse(PrivilegeHelper.hasImmediatePrivilege(stem2testGroup, stem2sub3wheel.toSubject(), AccessPrivilege.ADMIN));
    
    stem2testGroup.delete();
    
    GrouperSession.stopQuietly(grouperSession);

    //################################## SUBJ 2 stem1admin on stem2
    initialFirings = RuleEngine.ruleFirings;
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ2);
    
    stem2testGroup = new GroupSave(grouperSession).assignName("stem2:testGroup").assignCreateParentStemsIfNotExist(true).save();

    //count rule firings
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);

    assertFalse(PrivilegeHelper.hasImmediatePrivilege(stem2testGroup, SubjectTestHelper.SUBJ2, AccessPrivilege.ADMIN));
    assertTrue(PrivilegeHelper.hasImmediatePrivilege(stem2testGroup, stem1admins.toSubject(), AccessPrivilege.ADMIN));
    assertTrue(stem2testGroup.hasAdmin(SubjectTestHelper.SUBJ2));
    
    stem2testGroup.delete();
    
    GrouperSession.stopQuietly(grouperSession);

    //################################## SUBJ 2 stem1admin on stem1
    initialFirings = RuleEngine.ruleFirings;
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ2);
    
    stem1testGroup = new GroupSave(grouperSession).assignName("stem1:testGroup").assignCreateParentStemsIfNotExist(true).save();

    //count rule firings
    assertEquals(initialFirings, RuleEngine.ruleFirings);

    assertTrue(PrivilegeHelper.hasImmediatePrivilege(stem1testGroup, SubjectTestHelper.SUBJ2, AccessPrivilege.ADMIN));
    assertFalse(PrivilegeHelper.hasImmediatePrivilege(stem1testGroup, stem1admins.toSubject(), AccessPrivilege.ADMIN));
    assertTrue(stem1testGroup.hasAdmin(SubjectTestHelper.SUBJ2));
    
    stem1testGroup.delete();
    
    GrouperSession.stopQuietly(grouperSession);

    //################################## SUBJ 3 stem1admins2 stem2:sub
    initialFirings = RuleEngine.ruleFirings;
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ3);
    
    stem2subTestGroup = new GroupSave(grouperSession).assignName("stem2:sub:testGroup").assignCreateParentStemsIfNotExist(true).save();

    //count rule firings
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);

    assertFalse(PrivilegeHelper.hasImmediatePrivilege(stem2subTestGroup, SubjectTestHelper.SUBJ3, AccessPrivilege.ADMIN));
    assertTrue(PrivilegeHelper.hasImmediatePrivilege(stem2subTestGroup, stem1admins2.toSubject(), AccessPrivilege.ADMIN));
    assertTrue(stem2subTestGroup.hasAdmin(SubjectTestHelper.SUBJ3));
    
    stem2subTestGroup.delete();
    
    GrouperSession.stopQuietly(grouperSession);

    //################################## SUBJ 4 stem2
    initialFirings = RuleEngine.ruleFirings;
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ4);
    
    stem2testGroup = new GroupSave(grouperSession).assignName("stem2:testGroup").assignCreateParentStemsIfNotExist(true).save();

    //count rule firings
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);

    assertTrue(PrivilegeHelper.hasImmediatePrivilege(stem2testGroup, SubjectTestHelper.SUBJ4, AccessPrivilege.ADMIN));
    assertTrue(stem2testGroup.hasAdmin(SubjectTestHelper.SUBJ4));
    
    stem2testGroup.delete();
    
    GrouperSession.stopQuietly(grouperSession);

    //################################## SUBJ 3 stem1admins2 stem2:sub
    initialFirings = RuleEngine.ruleFirings;
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ3);
    
    stem2subTestGroup = new GroupSave(grouperSession).assignName("stem2:sub:testGroup").assignCreateParentStemsIfNotExist(true).save();

    //count rule firings
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);

    assertFalse(PrivilegeHelper.hasImmediatePrivilege(stem2subTestGroup, SubjectTestHelper.SUBJ3, AccessPrivilege.ADMIN));
    assertTrue(PrivilegeHelper.hasImmediatePrivilege(stem2subTestGroup, stem1admins2.toSubject(), AccessPrivilege.ADMIN));
    assertTrue(stem2subTestGroup.hasAdmin(SubjectTestHelper.SUBJ3));
    
    stem2subTestGroup.delete();
    
    GrouperSession.stopQuietly(grouperSession);

    //################################## SUBJ 5 stem1admins, stem1admins2 stem2:sub5
    initialFirings = RuleEngine.ruleFirings;
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ5);
    
    stem2sub5testGroup = new GroupSave(grouperSession).assignName("stem2:sub5:testGroup").assignCreateParentStemsIfNotExist(true).save();

    //count rule firings
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);

    assertFalse(PrivilegeHelper.hasImmediatePrivilege(stem2sub5testGroup, SubjectTestHelper.SUBJ5, AccessPrivilege.ADMIN));
    assertTrue(PrivilegeHelper.hasImmediatePrivilege(stem2sub5testGroup, stem1admins.toSubject(), AccessPrivilege.ADMIN));
    assertTrue(PrivilegeHelper.hasImmediatePrivilege(stem2sub5testGroup, stem1admins2.toSubject(), AccessPrivilege.ADMIN));
    assertTrue(stem2sub5testGroup.hasAdmin(SubjectTestHelper.SUBJ5));
    
    stem2sub5testGroup.delete();
    
    GrouperSession.stopQuietly(grouperSession);

    //################################## SUBJ 6 stem1admins stem2:sub5
    initialFirings = RuleEngine.ruleFirings;
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ6);
    
    stem2sub5testGroup = new GroupSave(grouperSession).assignName("stem2:sub5:testGroup").assignCreateParentStemsIfNotExist(true).save();

    //count rule firings
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);

    assertFalse(PrivilegeHelper.hasImmediatePrivilege(stem2sub5testGroup, SubjectTestHelper.SUBJ5, AccessPrivilege.ADMIN));
    assertTrue(PrivilegeHelper.hasImmediatePrivilege(stem2sub5testGroup, stem1admins.toSubject(), AccessPrivilege.ADMIN));
    assertFalse(PrivilegeHelper.hasImmediatePrivilege(stem2sub5testGroup, stem1admins2.toSubject(), AccessPrivilege.ADMIN));
    assertTrue(stem2sub5testGroup.hasAdmin(SubjectTestHelper.SUBJ5));
    
    stem2sub5testGroup.delete();
    
    GrouperSession.stopQuietly(grouperSession);

    
    //################################## SUBJ 4 stem2 NO WHEEL
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.use", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.group", "");

    initialFirings = RuleEngine.ruleFirings;
    
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ4);
    
    stem2testGroup = new GroupSave(grouperSession).assignName("stem2:testGroup").assignCreateParentStemsIfNotExist(true).save();

    //count rule firings
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);

    assertTrue(PrivilegeHelper.hasImmediatePrivilege(stem2testGroup, SubjectTestHelper.SUBJ4, AccessPrivilege.ADMIN));
    assertTrue(stem2testGroup.hasAdmin(SubjectTestHelper.SUBJ4));
    
    stem2testGroup.delete();
    
    GrouperSession.stopQuietly(grouperSession);

    
    
    

  }
  

  /**
   * 
   */
  public void testRuleApiEmailTemplate() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group groupA = new GroupSave(grouperSession).assignName("stem:a").assignCreateParentStemsIfNotExist(true).save();
    Group groupB = new GroupSave(grouperSession).assignName("stem:b").assignCreateParentStemsIfNotExist(true).save();
    
    AttributeAssign attributeAssign = groupA
      .getAttributeDelegate().assignAttribute(RuleUtils.ruleAttributeDefName()).getAttributeAssign();
    
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectSourceIdName(), "g:isa");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleActAsSubjectIdName(), "GrouperSystem");
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleCheckTypeName(), 
        RuleCheckType.membershipAdd.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumName(), RuleThenEnum.sendEmail.name());
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg0Name(), "a@b.c, ${safeSubject.emailAddress}"); // ${subjectEmail}
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg1Name(), "template: testTemplateSubject"); //${groupId}
    attributeAssign.getAttributeValueDelegate().assignValue(
        RuleUtils.ruleThenEnumArg2Name(), "template: testTemplateBody"); 
    
    //should be valid
    String isValidString = attributeAssign.getAttributeValueDelegate().retrieveValueString(
        RuleUtils.ruleValidName());
    assertEquals("T", isValidString);
    
    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;
    
    groupB.addMember(SubjectTestHelper.SUBJ0);
  
    assertEquals(initialFirings, RuleEngine.ruleFirings);
    
    groupA.addMember(SubjectTestHelper.SUBJ0);
    
    //count rule firings
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
  
    // GrouperSession.startRootSession();
    // addMember("stem:a", "test.subject.0");
    // addMember("stem:b", "test.subject.0");
    // delMember("stem:b", "test.subject.0");
    // hasMember("stem:a", "test.subject.0");
    
  }

  
  /**
   * 
   */
  public void testGroupIntersectionDate() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group groupA = new GroupSave(grouperSession).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignName("stem:a").assignCreateParentStemsIfNotExist(true).save();
    
    Subject subject9 = SubjectFinder.findById("test.subject.9", true);
    Subject subject0 = SubjectFinder.findById("test.subject.0", true);
    Subject subject1 = SubjectFinder.findById("test.subject.1", true);

    groupA.grantPriv(subject9, AccessPrivilege.ADMIN, false);
  
    Group groupB = new GroupSave(grouperSession).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
      .assignName("stem:b").assignCreateParentStemsIfNotExist(true).save();
    groupB.grantPriv(subject9, AccessPrivilege.READ, false);
    
    RuleApi.groupIntersection(subject9, groupA, groupB, 5);
    
    groupB.addMember(subject0);
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_grouperRules");
    
    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;
    
    //doesnt do anything
    groupB.deleteMember(subject0);
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_grouperRules");
    
    assertEquals(initialFirings, RuleEngine.ruleFirings);
  
    groupB.addMember(subject0);
    groupA.addMember(subject0);
    
    //count rule firings
    
    groupB.deleteMember(subject0);
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_grouperRules");
    
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
  
    //should have a disabled date in group A
    assertTrue(groupA.hasMember(subject0));

    Member member0 = MemberFinder.findBySubject(grouperSession, subject0, true);
    Member member1 = MemberFinder.findBySubject(grouperSession, subject1, true);
    
    Membership membership = groupA.getImmediateMembership(Group.getDefaultList(), member0, true, true);
    
    assertNotNull(membership.getDisabledTime());
    long disabledTime = membership.getDisabledTime().getTime();
    
    assertTrue("More than 4 days: " + new Date(disabledTime), disabledTime > System.currentTimeMillis() + (4 * 24 * 60 * 60 * 1000));
    assertTrue("Less than 6 days: " + new Date(disabledTime), disabledTime < System.currentTimeMillis() + (6 * 24 * 60 * 60 * 1000));

    groupA.addMember(subject1);
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_grouperRules");
    
    membership = groupA.getImmediateMembership(Group.getDefaultList(), member1, true, true);

    assertNull(membership.getDisabledTime());

    //run the daemon
    String status = GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);

    assertTrue(status, status.toLowerCase().contains("success"));

    membership = groupA.getImmediateMembership(Group.getDefaultList(), member1, true, true);

    assertNotNull(membership.getDisabledTime());
    disabledTime = membership.getDisabledTime().getTime();
    
    assertTrue("More than 4 days: " + new Date(disabledTime), disabledTime > System.currentTimeMillis() + (4 * 24 * 60 * 60 * 1000));
    assertTrue("Less than 6 days: " + new Date(disabledTime), disabledTime < System.currentTimeMillis() + (6 * 24 * 60 * 60 * 1000));
  
  }
  
  /**
   * 
   */
  public void testRuleVeto() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group ruleGroup = new GroupSave(grouperSession).assignName("stem:a").assignCreateParentStemsIfNotExist(true).save();
    Group mustBeInGroup = new GroupSave(grouperSession).assignName("stem:b").assignCreateParentStemsIfNotExist(true).save();
    Subject actAsSubject = SubjectFinder.findByIdAndSource("GrouperSystem", "g:isa", true);
    
    RuleApi.vetoMembershipIfNotInGroup(actAsSubject, ruleGroup, mustBeInGroup, 
        "rule.entity.must.be.a.member.of.stem.b", "Entity cannot be a member of stem:a if not a member of stem:b");
    
    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;
    
    Subject subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);

    try {
      ruleGroup.addMember(subject0);
      fail("Should be vetoed");
    } catch (RuleVeto rve) {
      //this is good
      String stack = ExceptionUtils.getFullStackTrace(rve);
      assertTrue(stack, stack.contains("Entity cannot be a member of stem:a if not a member of stem:b"));
    }

    assertEquals(initialFirings+1, RuleEngine.ruleFirings);

    mustBeInGroup.addMember(subject0);
    ruleGroup.addMember(subject0);
    
    assertEquals("Didnt fire since is a member", initialFirings+1, RuleEngine.ruleFirings);

  }
  
  /**
   * 
   */
  public void testRuleVetoSubjectAssignInFolderIfNotInGroup() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group allowedGroup = new GroupSave(grouperSession).assignName("stem:allowed").assignCreateParentStemsIfNotExist(true).save();
    Group restrictedGroup = new GroupSave(grouperSession).assignName("stem2:restricted").assignCreateParentStemsIfNotExist(true).save();
    Group employeeGroup = new GroupSave(grouperSession).assignName("etc:employee").assignCreateParentStemsIfNotExist(true).save();
    
    //Stem rootStem = StemFinder.findRootStem(grouperSession);
    Stem restrictedStem = StemFinder.findByName(grouperSession, "stem2", true);

    
    RuleApi.vetoSubjectAssignInFolderIfNotInGroup(SubjectFinder.findRootSubject(), restrictedStem, employeeGroup, false, "jdbc", Stem.Scope.SUB, "rule.entity.must.be.a.member.of.etc.employee", "Entity cannot be assigned if not a member of etc:employee");

    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;

    Subject subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);

    try {
      restrictedGroup.addMember(subject0);
      fail("Should be vetoed");
    } catch (RuleVeto rve) {
      //this is good
      String stack = ExceptionUtils.getFullStackTrace(rve);
      assertTrue(stack, stack.contains("Entity cannot be assigned if not a member of etc:employee"));
    }
  
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
    
    allowedGroup.addMember(subject0);

    //this doesnt actually fire
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
    
    employeeGroup.addMember(subject0);
    restrictedGroup.addMember(subject0);
    
    assertEquals("Didnt fire since is a member", initialFirings+1, RuleEngine.ruleFirings);

  }
  
  /**
   * disallow a source at root, but then allow in subfolder
   */
  public void testRuleVetoSubjectAssignInFolderIfNotInGroupPenn() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group allowedGroup = new GroupSave(grouperSession).assignName("stem:allowed").assignCreateParentStemsIfNotExist(true).save();
    Group restrictedGroup = new GroupSave(grouperSession).assignName("stem2:restricted").assignCreateParentStemsIfNotExist(true).save();
    
    Stem rootStem = StemFinder.findRootStem(grouperSession);
    Stem allowedStem = StemFinder.findByName(grouperSession, "stem", true);

    RuleApi.vetoSubjectAssignInFolderIfNotInGroup(SubjectFinder.findRootSubject(), rootStem, null, false, "jdbc", Scope.SUB,
        "rule.entity.must.be.a.member.of.etc.employee", "Entity cannot be assigned if not a member of etc:employee");

    
    RuleApi.vetoSubjectAssignInFolderIfNotInGroup(SubjectFinder.findRootSubject(), allowedStem, null, true, "jdbc", Scope.SUB,
        "rule.entity.must.be.a.member.of.etc.employee", "Entity cannot be assigned if not a member of etc:employee");

    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;

    Subject subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);

    try {
      restrictedGroup.addMember(subject0);
      fail("Should be vetoed");
    } catch (RuleVeto rve) {
      //this is good
      String stack = ExceptionUtils.getFullStackTrace(rve);
      assertTrue(stack, stack.contains("Entity cannot be assigned if not a member of etc:employee"));
    }
  
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
    
    allowedGroup.addMember(SubjectTestHelper.SUBJ0);

    //this doesnt actually fire
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
    
  }
  
  /**
   * <pre>
   * root  denies groups SUB
   * a:    allows all ONE, result: allows all, allows grouperSystem
   * b:    denies jdbc ONE, result: denies groups and jdbc, allows grouperSystem
   * a:a   result: will deny groups inherit, allow jdbc, allows grouperSystem
   * b:a   result: will deny groups inherit, allow jdbc, allows grouperSystem
   * b:c   allows groups SUB, result: allow all, allows grouperSystem
   * a:b   denies jdbc SUB, result: deny jdbc, groups, allows grouperSystem
   * b:d   allows amployees in jdbc SUB, result: denies groups and allows employees, allows grouperSystem
   * a:e   allows employees for all SUB, result: allows employees, denies groups, denies grouperSystem
   * a:e:a deny all SUB, result: cant assign jdbc, groups, grouperSystem
   * a:e:b allow all ONE, result, can assign jdbc, groups, grouperSystem
   * </pre>
   */
  public void testRuleVetoSubjectAssignInFolderInherit() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group a_group = new GroupSave(grouperSession).assignName("a:group").assignCreateParentStemsIfNotExist(true).save();
    Group b_group = new GroupSave(grouperSession).assignName("b:group").assignCreateParentStemsIfNotExist(true).save();
    Group a_a_group = new GroupSave(grouperSession).assignName("a:a:group").assignCreateParentStemsIfNotExist(true).save();
    Group b_a_group = new GroupSave(grouperSession).assignName("b:a:group").assignCreateParentStemsIfNotExist(true).save();
    Group b_c_group = new GroupSave(grouperSession).assignName("b:c:group").assignCreateParentStemsIfNotExist(true).save();
    Group a_b_group = new GroupSave(grouperSession).assignName("a:b:group").assignCreateParentStemsIfNotExist(true).save();
    Group b_d_group = new GroupSave(grouperSession).assignName("b:d:group").assignCreateParentStemsIfNotExist(true).save();
    Group a_e_group = new GroupSave(grouperSession).assignName("a:e:group").assignCreateParentStemsIfNotExist(true).save();
    Group a_e_a_group = new GroupSave(grouperSession).assignName("a:e:a:group").assignCreateParentStemsIfNotExist(true).save();
    Group a_e_b_group = new GroupSave(grouperSession).assignName("a:e:b:group").assignCreateParentStemsIfNotExist(true).save();
    
    Group groupEmployee = new GroupSave(grouperSession).assignName("stem:employee").assignCreateParentStemsIfNotExist(true).save();
    Group groupAnother = new GroupSave(grouperSession).assignName("stem:another").assignCreateParentStemsIfNotExist(true).save();

    Stem rootStem = StemFinder.findRootStem(grouperSession);
    Stem a_stem = StemFinder.findByName(grouperSession, "a", true);
    Stem b_stem = StemFinder.findByName(grouperSession, "b", true);
    Stem a_a_stem = StemFinder.findByName(grouperSession, "a:a", true);
    Stem b_a_stem = StemFinder.findByName(grouperSession, "b:a", true);
    Stem b_c_stem = StemFinder.findByName(grouperSession, "b:c", true);
    Stem a_b_stem = StemFinder.findByName(grouperSession, "a:b", true);
    Stem b_d_stem = StemFinder.findByName(grouperSession, "b:d", true);
    Stem a_e_stem = StemFinder.findByName(grouperSession, "a:e", true);
    Stem a_e_a_stem = StemFinder.findByName(grouperSession, "a:e:a", true);
    Stem a_e_b_stem = StemFinder.findByName(grouperSession, "a:e:b", true);

    List<Stem> stems = GrouperUtil.toList(rootStem, a_stem, b_stem, a_a_stem, b_a_stem, b_c_stem, 
        a_b_stem, b_d_stem, a_e_stem, a_e_a_stem, a_e_b_stem);

    Subject subjectEmployee = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Subject subjectGrouperSystem = SubjectFinder.findRootSubject();
    groupEmployee.addMember(subjectEmployee);
    
    Subject subjectNonEmployee = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);

    for (Stem stem : stems) {
      //lets do some searches
      assertTrue(SubjectHelper.inList(SubjectFinder.findAllInStem(stem.getName(), subjectEmployee.getId()), subjectEmployee));
      assertTrue(SubjectHelper.inList(SubjectFinder.findAllInStem(stem.getName(), subjectNonEmployee.getId()), subjectNonEmployee));
      assertTrue(SubjectHelper.inList(SubjectFinder.findAllInStem(stem.getName(), groupAnother.getName()), groupAnother.toSubject()));
      assertTrue(SubjectHelper.inList(SubjectFinder.findAllInStem(stem.getName(), "GrouperSystem"), SubjectFinder.findRootSubject()));

      for (Source source : SourceManager.getInstance().getSources()) {
        //nothing should be protected
        RestrictSourceForGroup restrictSourceForGroup = SubjectFinder.restrictSourceForGroup(stem.getName(), source.getId());
        assertFalse(restrictSourceForGroup.isRestrict());
      }

    }
    
    

    RuleApi.vetoSubjectAssignInFolderIfNotInGroup(SubjectFinder.findRootSubject(), rootStem, null, false, "g:gsa", Scope.SUB,
        "root.cannot.be.group", "Root cannot be group");
    RuleApi.vetoSubjectAssignInFolderIfNotInGroup(SubjectFinder.findRootSubject(), a_stem, null, true, null, Scope.ONE,
        "a.allow.all", "a allow all");
    RuleApi.vetoSubjectAssignInFolderIfNotInGroup(SubjectFinder.findRootSubject(), b_stem, null, false, "jdbc", Scope.ONE,
        "b.deny.jdbc", "b deny jdbc");
    RuleApi.vetoSubjectAssignInFolderIfNotInGroup(SubjectFinder.findRootSubject(), b_c_stem, null, true, "g:gsa", Scope.SUB,
        "b.c.allow.groups.sub", "b:c allow groups sub");
    RuleApi.vetoSubjectAssignInFolderIfNotInGroup(SubjectFinder.findRootSubject(), a_b_stem, null, false, "jdbc", Scope.SUB,
        "a.b.deny.jdbc.sub", "a:b deny jdbc sub");
    RuleApi.vetoSubjectAssignInFolderIfNotInGroup(SubjectFinder.findRootSubject(), b_d_stem, groupEmployee, false, "jdbc", Scope.SUB,
        "b.d.allow.employees.jdbc.sub", "b:d allow employees jdbc sub");
    RuleApi.vetoSubjectAssignInFolderIfNotInGroup(SubjectFinder.findRootSubject(), a_e_stem, groupEmployee, false, null, Scope.SUB,
        "a.e.allow.employees.all.sub", "a:e allow employees all sub");
    RuleApi.vetoSubjectAssignInFolderIfNotInGroup(SubjectFinder.findRootSubject(), a_e_a_stem, null, false, null, Scope.SUB,
        "a.e.a.deny.all.sub", "a:e:a deny all sub");
    RuleApi.vetoSubjectAssignInFolderIfNotInGroup(SubjectFinder.findRootSubject(), a_e_b_stem, null, true, null, Scope.SUB,
        "a.e.b.allow.all.one", "a:e:b allow all one");

    //count rule firings
    long initialFirings = -1;
    
    //* a:    allows all ONE, result: allows all, allows grouperSystem
    a_group.addMember(subjectEmployee);
    a_group.addMember(subjectNonEmployee);
    a_group.addMember(subjectGrouperSystem);
    a_group.addMember(groupAnother.toSubject());
    
    //lets do some searches / etc
    assertTrue(SubjectHelper.inList(SubjectFinder.findAllInStem(a_stem.getName(), subjectEmployee.getId()), subjectEmployee));
    assertTrue(SubjectHelper.inList(SubjectFinder.findAllInStem(a_stem.getName(), subjectNonEmployee.getId()), subjectNonEmployee));
    assertTrue(SubjectHelper.inList(SubjectFinder.findAllInStem(a_stem.getName(), groupAnother.getName()), groupAnother.toSubject()));
    assertTrue(SubjectHelper.inList(SubjectFinder.findAllInStem(a_stem.getName(), "GrouperSystem"), SubjectFinder.findRootSubject()));
    assertFalse(SubjectFinder.restrictSourceForGroup(a_stem.getName(), "jdbc").isRestrict());
    assertFalse(SubjectFinder.restrictSourceForGroup(a_stem.getName(), "g:gsa").isRestrict());
    assertFalse(SubjectFinder.restrictSourceForGroup(a_stem.getName(), "g:isa").isRestrict());
    
    //* b:    denies jdbc ONE, result: denies groups and jdbc, allows grouperSystem
    initialFirings = RuleEngine.ruleFirings;
    try {
      b_group.addMember(subjectEmployee);
      fail();
    } catch (RuntimeException re) {
      //this is good
      String stack = ExceptionUtils.getFullStackTrace(re);
      assertTrue(stack, stack.contains("b deny jdbc"));
    }
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
    
    initialFirings = RuleEngine.ruleFirings;
    try {
      b_group.addMember(subjectNonEmployee);
      fail();
    } catch (RuntimeException re) {
      //this is good
      String stack = ExceptionUtils.getFullStackTrace(re);
      assertTrue(stack, stack.contains("b deny jdbc"));
    }
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
    
    initialFirings = RuleEngine.ruleFirings;
    try {
      b_group.addMember(groupAnother.toSubject());
      fail();
    } catch (RuntimeException re) {
      //this is good
      String stack = ExceptionUtils.getFullStackTrace(re);
      assertTrue(stack, stack.contains("Root cannot be group"));
    }
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
    
    b_group.addMember(subjectGrouperSystem);

    //lets do some searches / etc
    assertFalse(SubjectHelper.inList(SubjectFinder.findAllInStem(b_stem.getName(), subjectEmployee.getId()), subjectEmployee));
    assertFalse(SubjectHelper.inList(SubjectFinder.findAllInStem(b_stem.getName(), subjectNonEmployee.getId()), subjectNonEmployee));
    assertFalse(SubjectHelper.inList(SubjectFinder.findAllInStem(b_stem.getName(), groupAnother.getName()), groupAnother.toSubject()));
    assertTrue(SubjectHelper.inList(SubjectFinder.findAllInStem(b_stem.getName(), "GrouperSystem"), SubjectFinder.findRootSubject()));
    assertTrue(SubjectFinder.restrictSourceForGroup(b_stem.getName(), "jdbc").isRestrict());
    assertTrue(SubjectFinder.restrictSourceForGroup(b_stem.getName(), "g:gsa").isRestrict());
    assertFalse(SubjectFinder.restrictSourceForGroup(b_stem.getName(), "g:isa").isRestrict());
    

    
    //* a:a   result: will deny groups inherit, allow jdbc, allows grouperSystem
    a_a_group.addMember(subjectEmployee);
    a_a_group.addMember(subjectNonEmployee);
    a_a_group.addMember(subjectGrouperSystem);

    initialFirings = RuleEngine.ruleFirings;
    try {
      a_a_group.addMember(groupAnother.toSubject());
      fail();
    } catch (RuntimeException re) {
      //this is good
      String stack = ExceptionUtils.getFullStackTrace(re);
      assertTrue(stack, stack.contains("Root cannot be group"));
    }
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
        
    //lets do some searches / etc
    assertTrue(SubjectHelper.inList(SubjectFinder.findAllInStem(a_a_stem.getName(), subjectEmployee.getId()), subjectEmployee));
    assertTrue(SubjectHelper.inList(SubjectFinder.findAllInStem(a_a_stem.getName(), subjectNonEmployee.getId()), subjectNonEmployee));
    assertFalse(SubjectHelper.inList(SubjectFinder.findAllInStem(a_a_stem.getName(), groupAnother.getName()), groupAnother.toSubject()));
    assertTrue(SubjectHelper.inList(SubjectFinder.findAllInStem(a_a_stem.getName(), "GrouperSystem"), SubjectFinder.findRootSubject()));
    assertFalse(SubjectFinder.restrictSourceForGroup(a_a_stem.getName(), "jdbc").isRestrict());
    assertTrue(SubjectFinder.restrictSourceForGroup(a_a_stem.getName(), "g:gsa").isRestrict());
    assertFalse(SubjectFinder.restrictSourceForGroup(a_a_stem.getName(), "g:isa").isRestrict());
    

    //* b:a   result: will deny groups inherit, allow jdbc, allows grouperSystem
    b_a_group.addMember(subjectEmployee);
    b_a_group.addMember(subjectNonEmployee);
    b_a_group.addMember(subjectGrouperSystem);

    initialFirings = RuleEngine.ruleFirings;
    try {
      b_a_group.addMember(groupAnother.toSubject());
      fail();
    } catch (RuntimeException re) {
      //this is good
      String stack = ExceptionUtils.getFullStackTrace(re);
      assertTrue(stack, stack.contains("Root cannot be group"));
    }
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);

    //lets do some searches / etc
    assertTrue(SubjectHelper.inList(SubjectFinder.findAllInStem(b_a_stem.getName(), subjectEmployee.getId()), subjectEmployee));
    assertTrue(SubjectHelper.inList(SubjectFinder.findAllInStem(b_a_stem.getName(), subjectNonEmployee.getId()), subjectNonEmployee));
    assertFalse(SubjectHelper.inList(SubjectFinder.findAllInStem(b_a_stem.getName(), groupAnother.getName()), groupAnother.toSubject()));
    assertTrue(SubjectHelper.inList(SubjectFinder.findAllInStem(b_a_stem.getName(), "GrouperSystem"), SubjectFinder.findRootSubject()));
    assertFalse(SubjectFinder.restrictSourceForGroup(b_a_stem.getName(), "jdbc").isRestrict());
    assertTrue(SubjectFinder.restrictSourceForGroup(b_a_stem.getName(), "g:gsa").isRestrict());
    assertFalse(SubjectFinder.restrictSourceForGroup(b_a_stem.getName(), "g:isa").isRestrict());
    
    //* b:c   allows groups SUB, result: allow all, allows grouperSystem
    b_c_group.addMember(subjectEmployee);
    b_c_group.addMember(subjectNonEmployee);
    b_c_group.addMember(subjectGrouperSystem);
    b_c_group.addMember(groupAnother.toSubject());

    //lets do some searches / etc
    assertTrue(SubjectHelper.inList(SubjectFinder.findAllInStem(b_c_stem.getName(), subjectEmployee.getId()), subjectEmployee));
    assertTrue(SubjectHelper.inList(SubjectFinder.findAllInStem(b_c_stem.getName(), subjectNonEmployee.getId()), subjectNonEmployee));
    assertTrue(SubjectHelper.inList(SubjectFinder.findAllInStem(b_c_stem.getName(), groupAnother.getName()), groupAnother.toSubject()));
    assertTrue(SubjectHelper.inList(SubjectFinder.findAllInStem(b_c_stem.getName(), "GrouperSystem"), SubjectFinder.findRootSubject()));
    assertFalse(SubjectFinder.restrictSourceForGroup(b_c_stem.getName(), "jdbc").isRestrict());
    assertFalse(SubjectFinder.restrictSourceForGroup(b_c_stem.getName(), "g:gsa").isRestrict());
    assertFalse(SubjectFinder.restrictSourceForGroup(b_c_stem.getName(), "g:isa").isRestrict());
    

    //* a:b   denies jdbc SUB, result: deny jdbc, groups, allows grouperSystem
    initialFirings = RuleEngine.ruleFirings;
    try {
      a_b_group.addMember(subjectEmployee);
      fail();
    } catch (RuntimeException re) {
      //this is good
      String stack = ExceptionUtils.getFullStackTrace(re);
      assertTrue(stack, stack.contains("a:b deny jdbc sub"));
    }
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
    
    initialFirings = RuleEngine.ruleFirings;
    try {
      a_b_group.addMember(subjectNonEmployee);
      fail();
    } catch (RuntimeException re) {
      //this is good
      String stack = ExceptionUtils.getFullStackTrace(re);
      assertTrue(stack, stack.contains("a:b deny jdbc sub"));
    }
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
    
    initialFirings = RuleEngine.ruleFirings;
    try {
      a_b_group.addMember(groupAnother.toSubject());
      fail();
    } catch (RuntimeException re) {
      //this is good
      String stack = ExceptionUtils.getFullStackTrace(re);
      assertTrue(stack, stack.contains("Root cannot be group"));
    }
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
    
    a_b_group.addMember(subjectGrouperSystem);

    //lets do some searches / etc
    assertFalse(SubjectHelper.inList(SubjectFinder.findAllInStem(a_b_stem.getName(), subjectEmployee.getId()), subjectEmployee));
    assertFalse(SubjectHelper.inList(SubjectFinder.findAllInStem(a_b_stem.getName(), subjectNonEmployee.getId()), subjectNonEmployee));
    assertFalse(SubjectHelper.inList(SubjectFinder.findAllInStem(a_b_stem.getName(), groupAnother.getName()), groupAnother.toSubject()));
    assertTrue(SubjectHelper.inList(SubjectFinder.findAllInStem(a_b_stem.getName(), "GrouperSystem"), SubjectFinder.findRootSubject()));
    assertTrue(SubjectFinder.restrictSourceForGroup(a_b_stem.getName(), "jdbc").isRestrict());
    assertTrue(SubjectFinder.restrictSourceForGroup(a_b_stem.getName(), "g:gsa").isRestrict());
    assertFalse(SubjectFinder.restrictSourceForGroup(a_b_stem.getName(), "g:isa").isRestrict());
    

    //* b:d   allows amployees in jdbc SUB, result: denies groups and allows employees, allows grouperSystem
    b_d_group.addMember(subjectEmployee);
    
    initialFirings = RuleEngine.ruleFirings;
    try {
      b_d_group.addMember(subjectNonEmployee);
      fail();
    } catch (RuntimeException re) {
      //this is good
      String stack = ExceptionUtils.getFullStackTrace(re);
      assertTrue(stack, stack.contains("b:d allow employees jdbc sub"));
    }
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
    
    initialFirings = RuleEngine.ruleFirings;
    try {
      b_d_group.addMember(groupAnother.toSubject());
      fail();
    } catch (RuntimeException re) {
      //this is good
      String stack = ExceptionUtils.getFullStackTrace(re);
      assertTrue(stack, stack.contains("Root cannot be group"));
    }
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
    
    b_d_group.addMember(subjectGrouperSystem);

    assertTrue(SubjectHelper.inList(SubjectFinder.findAllInStem(b_d_stem.getName(), subjectEmployee.getId()), subjectEmployee));
    assertTrue(SubjectHelper.inList(SubjectFinder.findAllInStem(b_d_stem.getName(), subjectNonEmployee.getId()), subjectNonEmployee));
    assertFalse(SubjectHelper.inList(SubjectFinder.findAllInStem(b_d_stem.getName(), groupAnother.getName()), groupAnother.toSubject()));
    assertTrue(SubjectHelper.inList(SubjectFinder.findAllInStem(b_d_stem.getName(), "GrouperSystem"), SubjectFinder.findRootSubject()));
    assertTrue(SubjectFinder.restrictSourceForGroup(b_d_stem.getName(), "jdbc").isRestrict());
    assertTrue(SubjectFinder.restrictSourceForGroup(b_d_stem.getName(), "g:gsa").isRestrict());
    assertFalse(SubjectFinder.restrictSourceForGroup(b_d_stem.getName(), "g:isa").isRestrict());

    //* a:e   allows employees for all SUB, result: allows employees, denies groups, denies grouperSystem
    a_e_group.addMember(subjectEmployee);
    
    initialFirings = RuleEngine.ruleFirings;
    try {
      a_e_group.addMember(subjectNonEmployee);
      fail();
    } catch (RuntimeException re) {
      //this is good
      String stack = ExceptionUtils.getFullStackTrace(re);
      assertTrue(stack, stack.contains("a:e allow employees all sub"));
    }
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
    
    initialFirings = RuleEngine.ruleFirings;
    try {
      a_e_group.addMember(groupAnother.toSubject());
      fail();
    } catch (RuntimeException re) {
      //this is good
      String stack = ExceptionUtils.getFullStackTrace(re);
      assertTrue(stack, stack.contains("a:e allow employees all sub"));
    }
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
    
    initialFirings = RuleEngine.ruleFirings;
    try {
      a_e_group.addMember(subjectGrouperSystem);
      fail();
    } catch (RuntimeException re) {
      //this is good
      String stack = ExceptionUtils.getFullStackTrace(re);
      assertTrue(stack, stack.contains("a:e allow employees all sub"));
    }
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);

    assertTrue(SubjectHelper.inList(SubjectFinder.findAllInStem(a_e_stem.getName(), subjectEmployee.getId()), subjectEmployee));
    assertTrue(SubjectHelper.inList(SubjectFinder.findAllInStem(a_e_stem.getName(), subjectNonEmployee.getId()), subjectNonEmployee));
    
    {
      Set<Subject> results = SubjectFinder.findAllInStem(a_e_stem.getName(), groupAnother.getName());
      assertTrue(GrouperUtil.setToString(results), SubjectHelper.inList(results, groupAnother.toSubject()));
    }    
    assertTrue(SubjectHelper.inList(SubjectFinder.findAllInStem(a_e_stem.getName(), "GrouperSystem"), SubjectFinder.findRootSubject()));
    assertTrue(SubjectFinder.restrictSourceForGroup(a_e_stem.getName(), "jdbc").isRestrict());
    assertTrue(SubjectFinder.restrictSourceForGroup(a_e_stem.getName(), "g:gsa").isRestrict());
    assertTrue(SubjectFinder.restrictSourceForGroup(a_e_stem.getName(), "g:isa").isRestrict());

    //* a:e:a deny all SUB, result: cant assign jdbc, groups, grouperSystem
    
    initialFirings = RuleEngine.ruleFirings;
    try {
      a_e_a_group.addMember(subjectEmployee);
      fail();
    } catch (RuntimeException re) {
      //this is good
      String stack = ExceptionUtils.getFullStackTrace(re);
      assertTrue(stack, stack.contains("a:e:a deny all sub"));
    }
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
    
    initialFirings = RuleEngine.ruleFirings;
    try {
      a_e_a_group.addMember(subjectNonEmployee);
      fail();
    } catch (RuntimeException re) {
      //this is good
      String stack = ExceptionUtils.getFullStackTrace(re);
      assertTrue(stack, stack.contains("a:e:a deny all sub"));
    }
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
    
    initialFirings = RuleEngine.ruleFirings;
    try {
      a_e_a_group.addMember(groupAnother.toSubject());
      fail();
    } catch (RuntimeException re) {
      //this is good
      String stack = ExceptionUtils.getFullStackTrace(re);
      assertTrue(stack, stack.contains("a:e:a deny all sub"));
    }
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
    
    initialFirings = RuleEngine.ruleFirings;
    try {
      a_e_a_group.addMember(subjectGrouperSystem);
      fail();
    } catch (RuntimeException re) {
      //this is good
      String stack = ExceptionUtils.getFullStackTrace(re);
      assertTrue(stack, stack.contains("a:e:a deny all"));
    }
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);

    assertFalse(SubjectHelper.inList(SubjectFinder.findAllInStem(a_e_a_stem.getName(), subjectEmployee.getId()), subjectEmployee));
    assertFalse(SubjectHelper.inList(SubjectFinder.findAllInStem(a_e_a_stem.getName(), subjectNonEmployee.getId()), subjectNonEmployee));
    assertFalse(SubjectHelper.inList(SubjectFinder.findAllInStem(a_e_a_stem.getName(), groupAnother.getName()), groupAnother.toSubject()));
    assertFalse(SubjectHelper.inList(SubjectFinder.findAllInStem(a_e_a_stem.getName(), "GrouperSystem"), SubjectFinder.findRootSubject()));
    assertTrue(SubjectFinder.restrictSourceForGroup(a_e_a_stem.getName(), "jdbc").isRestrict());
    assertTrue(SubjectFinder.restrictSourceForGroup(a_e_a_stem.getName(), "g:gsa").isRestrict());
    assertTrue(SubjectFinder.restrictSourceForGroup(a_e_a_stem.getName(), "g:isa").isRestrict());

    //* a:e:b allow all ONE, result, can assign jdbc, groups, grouperSystem
    a_e_b_group.addMember(subjectEmployee);
    a_e_b_group.addMember(subjectNonEmployee);
    a_e_b_group.addMember(subjectGrouperSystem);
    a_e_b_group.addMember(groupAnother.toSubject());

    assertTrue(SubjectHelper.inList(SubjectFinder.findAllInStem(a_e_b_stem.getName(), subjectEmployee.getId()), subjectEmployee));
    assertTrue(SubjectHelper.inList(SubjectFinder.findAllInStem(a_e_b_stem.getName(), subjectNonEmployee.getId()), subjectNonEmployee));
    assertTrue(SubjectHelper.inList(SubjectFinder.findAllInStem(a_e_b_stem.getName(), groupAnother.getName()), groupAnother.toSubject()));
    assertTrue(SubjectHelper.inList(SubjectFinder.findAllInStem(a_e_b_stem.getName(), "GrouperSystem"), SubjectFinder.findRootSubject()));
    assertFalse(SubjectFinder.restrictSourceForGroup(a_e_b_stem.getName(), "jdbc").isRestrict());
    assertFalse(SubjectFinder.restrictSourceForGroup(a_e_b_stem.getName(), "g:gsa").isRestrict());
    assertFalse(SubjectFinder.restrictSourceForGroup(a_e_b_stem.getName(), "g:isa").isRestrict());
  }
  
  /**
   * 
   */
  public void testInheritGroupPrivileges() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem2 = new StemSave(grouperSession).assignName("stem2").assignCreateParentStemsIfNotExist(true).save();

    Group groupA = new GroupSave(grouperSession).assignName("stem1:admins").assignCreateParentStemsIfNotExist(true).save();

    Subject subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    groupA.addMember(subject0);
    
    RuleApi.inheritGroupPrivileges(SubjectFinder.findRootSubject(), stem2, Scope.SUB, groupA.toSubject(), Privilege.getInstances("read, update"));

    long initialFirings = RuleEngine.ruleFirings;
    
    Group groupB = new GroupSave(grouperSession).assignName("stem2:b").assignCreateParentStemsIfNotExist(true).save();

    //count rule firings
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);

    //make sure allowed
    assertTrue(groupB.hasUpdate(subject0));
    assertTrue(groupB.hasRead(subject0));
    
    
    Group groupD = new GroupSave(grouperSession).assignName("stem3:d").assignCreateParentStemsIfNotExist(true).save();

    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
    
    assertFalse(groupD.hasUpdate(subject0));
    assertFalse(groupD.hasRead(subject0));
    
    
    Group groupC = new GroupSave(grouperSession).assignName("stem2:sub:c").assignCreateParentStemsIfNotExist(true).save();

    assertEquals(initialFirings+2, RuleEngine.ruleFirings);

    assertTrue(groupC.hasRead(subject0));
    assertTrue(groupC.hasUpdate(subject0));

  }

  
  
  /**
   * 
   */
  public void testInheritGroupPrivilegesFindManage() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem2 = new StemSave(grouperSession).assignName("stem2").assignCreateParentStemsIfNotExist(true).save();

    Group groupA = new GroupSave(grouperSession).assignName("stem1:admins").assignCreateParentStemsIfNotExist(true).save();

    Subject subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    groupA.addMember(subject0);
    
    RuleApi.inheritGroupPrivileges(SubjectFinder.findRootSubject(), stem2, Scope.SUB, groupA.toSubject(), Privilege.getInstances("read, update"));
    RuleApi.inheritGroupPrivileges(SubjectFinder.findRootSubject(), stem2, Scope.ONE, SubjectTestHelper.SUBJ1, Privilege.getInstances("read, update"));

    RuleApi.inheritFolderPrivileges(SubjectFinder.findRootSubject(), stem2, Scope.SUB, groupA.toSubject(), Privilege.getInstances("stem, create"));
    RuleApi.inheritFolderPrivileges(SubjectFinder.findRootSubject(), stem2, Scope.ONE, SubjectTestHelper.SUBJ1, Privilege.getInstances("create"));

    Stem stem23 = new StemSave(grouperSession).assignName("stem2:stem3").assignCreateParentStemsIfNotExist(true).save();
    
    RuleApi.inheritGroupPrivileges(SubjectFinder.findRootSubject(), stem23, Scope.SUB, SubjectTestHelper.SUBJ2, Privilege.getInstances("read, update"));
    RuleApi.inheritFolderPrivileges(SubjectFinder.findRootSubject(), stem23, Scope.SUB, SubjectTestHelper.SUBJ2, Privilege.getInstances("stem"));
    
    Group groupB = new GroupSave(grouperSession).assignName("stem2:b").assignCreateParentStemsIfNotExist(true).save();
    Group group23B = new GroupSave(grouperSession).assignName("stem2:stem3:b").assignCreateParentStemsIfNotExist(true).save();

//    Set<RuleDefinition> ruleDefinitions = RuleFinder.findGroupPrivilegeInheritRules(group23B.getParentStem());
    Set<RuleDefinition> ruleDefinitions = RuleFinder.findFolderPrivilegeInheritRules(group23B.getParentStem());
    
    for (RuleDefinition ruleDefinition : ruleDefinitions) {
      System.out.println(ruleDefinition);
    }
  }

  /**
   * 
   */
  public void testInheritFolderPrivileges() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem2 = new StemSave(grouperSession).assignName("stem2").assignCreateParentStemsIfNotExist(true).save();
  
    Group groupA = new GroupSave(grouperSession).assignName("stem1:admins").assignCreateParentStemsIfNotExist(true).save();
  
    Subject subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    groupA.addMember(subject0);
    
    RuleApi.inheritFolderPrivileges(SubjectFinder.findRootSubject(), stem2, Scope.SUB, groupA.toSubject(), Privilege.getInstances("stem, create"));

    long initialFirings = RuleEngine.ruleFirings;
    
    
    Stem stemB = new StemSave(grouperSession).assignName("stem2:b").assignCreateParentStemsIfNotExist(true).save();
  
    //count rule firings
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
  
    //make sure allowed
    assertTrue(stemB.hasCreate(subject0));
    assertTrue(stemB.hasStem(subject0));
    
    
    Stem stemD = new StemSave(grouperSession).assignName("stem3:b").assignCreateParentStemsIfNotExist(true).save();
  
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
    
    assertFalse(stemD.hasCreate(subject0));
    assertFalse(stemD.hasStem(subject0));
    
    
    Stem stemC = new StemSave(grouperSession).assignName("stem2:sub:c").assignCreateParentStemsIfNotExist(true).save();
  
    //fires for the sub stem and c stem
    assertEquals(initialFirings+3, RuleEngine.ruleFirings);
  
    assertTrue(stemC.hasCreate(subject0));
    assertTrue(stemC.hasStem(subject0));
  
  
  }

  /**
   * 
   */
  public void testGroupIntersectionFolder() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group groupA = new GroupSave(grouperSession).assignName("stem1:a").assignCreateParentStemsIfNotExist(true).save();
    Group groupB = new GroupSave(grouperSession).assignName("stem2:b").assignCreateParentStemsIfNotExist(true).save();
    Group groupC = new GroupSave(grouperSession).assignName("stem2:sub:c").assignCreateParentStemsIfNotExist(true).save();
    Group groupD = new GroupSave(grouperSession).assignName("stem3:subgroup").assignCreateParentStemsIfNotExist(true).save();
    groupC.addMember(groupD.toSubject());
    
    Stem stem = StemFinder.findByName(grouperSession, "stem2", true);
    
    RuleApi.groupIntersectionWithFolder(SubjectFinder.findRootSubject(), groupA, stem, Scope.SUB);
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_grouperRules");
    
    long initialFirings = RuleEngine.ruleFirings;
    
    Subject subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);

    groupB.addMember(subject0);
  
    //count rule firings
    
    //doesnt do anything
    groupB.deleteMember(subject0);
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_grouperRules");
    
    assertEquals(initialFirings, RuleEngine.ruleFirings);
    
    groupB.addMember(subject0);
    groupA.addMember(subject0);
    
    //count rule firings
    
    groupB.deleteMember(subject0);
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_grouperRules");
    
    //should come out of groupA
    assertFalse(groupA.hasMember(subject0));
  
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
  
    
    groupC.addMember(subject0);
    
    //doesnt do anything
    groupC.deleteMember(subject0);
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_grouperRules");
    
    //count rule firings
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
  
    groupC.addMember(subject0);
    groupA.addMember(subject0);
  
    groupC.deleteMember(subject0);
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_grouperRules");
    
    //should fire from ancestor
    assertFalse(groupA.hasMember(subject0));
  
    assertEquals(initialFirings+2, RuleEngine.ruleFirings);
  
    
    // effective test
    
    groupD.addMember(subject0);
    
    //doesnt do anything
    groupD.deleteMember(subject0);
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_grouperRules");
    
    //count rule firings
    assertEquals(initialFirings+2, RuleEngine.ruleFirings);
  
    groupD.addMember(subject0);
    groupA.addMember(subject0);
  
    groupD.deleteMember(subject0);
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_grouperRules");
    
    //should fire from ancestor
    assertFalse(groupA.hasMember(subject0));
  
    assertEquals(initialFirings+3, RuleEngine.ruleFirings);
    
    groupD.addMember(subject0);
    groupC.addMember(subject0);
    groupA.addMember(subject0);
  
    groupD.deleteMember(subject0);
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_grouperRules");

    // doesn't do anything
    assertTrue(groupA.hasMember(subject0));
  
    assertEquals(initialFirings+3, RuleEngine.ruleFirings);
    
    groupD.addMember(subject0);
  
    groupC.deleteMember(subject0);
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_grouperRules");

    // doesn't do anything
    assertTrue(groupA.hasMember(subject0));
  
    assertEquals(initialFirings+3, RuleEngine.ruleFirings);
  }

  
  /**
   * 
   */
  public void testInheritAttributeDefPrivileges() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem2 = new StemSave(grouperSession).assignName("stem2").assignCreateParentStemsIfNotExist(true).save();
  
    Group groupA = new GroupSave(grouperSession).assignName("stem1:admins").assignCreateParentStemsIfNotExist(true).save();
  
    Subject subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
  
    groupA.addMember(subject0);
    
    RuleApi.inheritAttributeDefPrivileges(SubjectFinder.findRootSubject(), stem2, Scope.SUB, groupA.toSubject(), Privilege.getInstances("attrRead, attrUpdate"));
  
    long initialFirings = RuleEngine.ruleFirings;
     
    
    AttributeDef attributeDefB = new AttributeDefSave(grouperSession).assignName("stem2:b").assignCreateParentStemsIfNotExist(true).save();
  
    //count rule firings
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
  
    //make sure allowed
    assertTrue(attributeDefB.getPrivilegeDelegate().hasAttrUpdate(subject0));
    assertTrue(attributeDefB.getPrivilegeDelegate().hasAttrRead(subject0));
    
    
    AttributeDef attributeDefD = new AttributeDefSave(grouperSession).assignName("stem3:b").assignCreateParentStemsIfNotExist(true).save();
  
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
    
    assertFalse(attributeDefD.getPrivilegeDelegate().hasAttrUpdate(subject0));
    assertFalse(attributeDefD.getPrivilegeDelegate().hasAttrRead(subject0));
    
    
    AttributeDef attributeDefC = new AttributeDefSave(grouperSession).assignName("stem2:sub:c").assignCreateParentStemsIfNotExist(true).save();
  
    assertEquals(initialFirings+2, RuleEngine.ruleFirings);
  
    assertTrue(attributeDefC.getPrivilegeDelegate().hasAttrRead(subject0));
    assertTrue(attributeDefC.getPrivilegeDelegate().hasAttrUpdate(subject0));
  
  }
  
  /**
   * 
   */
  public void testPermissionAssignment() {

    GrouperSession grouperSession = GrouperSession.startRootSession();
    AttributeDef permissionDef = new AttributeDefSave(grouperSession)
      .assignName("stem:permissionDef").assignCreateParentStemsIfNotExist(true)
      .assignAttributeDefType(AttributeDefType.perm)
      .save();
    
    permissionDef.setAssignToEffMembership(true);
    permissionDef.setAssignToGroup(true);
    permissionDef.store();
    
    Group groupEmployee = new GroupSave(grouperSession).assignName("stem:employee").assignCreateParentStemsIfNotExist(true).save();
    Group groupEmployeeSub = new GroupSave(grouperSession).assignName("stem:employeesub").assignCreateParentStemsIfNotExist(true).save();
    groupEmployee.addMember(groupEmployeeSub.toSubject());
    
    //make a role
    Role payrollUser = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollUser").assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();
    Role payrollGuest = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollGuest").assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();

    //assign a user to a role
    Subject subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    payrollUser.addMember(subject0, false);
    Subject subject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Subject subject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    payrollGuest.addMember(subject1, false);
    
    Subject subject3 = SubjectFinder.findByIdAndSource("test.subject.3", "jdbc", true);
    payrollUser.addMember(subject3, false);
    Subject subject4 = SubjectFinder.findByIdAndSource("test.subject.4", "jdbc", true);
    Subject subject5 = SubjectFinder.findByIdAndSource("test.subject.5", "jdbc", true);
    payrollGuest.addMember(subject4, false);
    
    Subject subject6 = SubjectFinder.findByIdAndSource("test.subject.6", "jdbc", true);
    payrollUser.addMember(subject6, false);
    Subject subject7 = SubjectFinder.findByIdAndSource("test.subject.7", "jdbc", true);
    Subject subject8 = SubjectFinder.findByIdAndSource("test.subject.8", "jdbc", true);
    payrollGuest.addMember(subject7, false);
    
    //create a permission, assign to role
    AttributeDefName canLogin = new AttributeDefNameSave(grouperSession, permissionDef).assignName("apps:payroll:permissions:canLogin").assignCreateParentStemsIfNotExist(true).save();
    
    payrollUser.getPermissionRoleDelegate().assignRolePermission(canLogin);
    
    //assign the permission to another user directly, not due to a role
    payrollGuest.getPermissionRoleDelegate().assignSubjectRolePermission(canLogin, subject1);
    payrollGuest.getPermissionRoleDelegate().assignSubjectRolePermission(canLogin, subject4);
    payrollGuest.getPermissionRoleDelegate().assignSubjectRolePermission(canLogin, subject7);
    
    //see that they both have the permission
    Member member0 = MemberFinder.findBySubject(grouperSession, subject0, false);
    Member member1 = MemberFinder.findBySubject(grouperSession, subject1, false);
    Member member3 = MemberFinder.findBySubject(grouperSession, subject3, false);
    Member member4 = MemberFinder.findBySubject(grouperSession, subject4, false);
    Member member6 = MemberFinder.findBySubject(grouperSession, subject6, false);
    Member member7 = MemberFinder.findBySubject(grouperSession, subject7, false);
    
    Set<PermissionEntry> permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member0.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member1.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
    
    RuleApi.permissionGroupIntersection(SubjectFinder.findRootSubject(), permissionDef, groupEmployee);

    groupEmployee.addMember(subject0);
    groupEmployee.addMember(subject1);
    groupEmployee.addMember(subject2);
    groupEmployeeSub.addMember(subject3);
    groupEmployeeSub.addMember(subject4);
    groupEmployeeSub.addMember(subject5);
    groupEmployee.addMember(subject6);
    groupEmployee.addMember(subject7);
    groupEmployee.addMember(subject8);
    groupEmployeeSub.addMember(subject6);
    groupEmployeeSub.addMember(subject7);
    groupEmployeeSub.addMember(subject8);
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_grouperRules");
    
    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;
    
    //doesnt do anything
    groupEmployee.deleteMember(subject2);
    groupEmployeeSub.deleteMember(subject5);
    groupEmployeeSub.deleteMember(subject8);
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_grouperRules");
    
    assertEquals(initialFirings, RuleEngine.ruleFirings);

    groupEmployee.deleteMember(subject0);
    groupEmployeeSub.deleteMember(subject3);
    groupEmployeeSub.deleteMember(subject6);
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_grouperRules");
    
    //should come out of groupA
    assertFalse(payrollUser.hasMember(subject0));
    assertFalse(payrollUser.hasMember(subject3));
    assertTrue(payrollUser.hasMember(subject6));

    assertEquals(initialFirings+2, RuleEngine.ruleFirings);

    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member0.getUuid());
    assertEquals(0, permissions.size());
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member3.getUuid());
    assertEquals(0, permissions.size());
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member6.getUuid());
    assertEquals(1, permissions.size());
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member1.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member4.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member7.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
    
    //take out second user
    groupEmployee.deleteMember(subject1);
    groupEmployeeSub.deleteMember(subject4);
    groupEmployeeSub.deleteMember(subject7);
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_grouperRules");
    
    assertTrue(payrollGuest.hasMember(subject1));
    assertTrue(payrollGuest.hasMember(subject4));
    assertTrue(payrollGuest.hasMember(subject7));

    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member1.getUuid());
    assertEquals(0, permissions.size());
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member4.getUuid());
    assertEquals(0, permissions.size());
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member7.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
  }

  /**
   * 
   */
  public void testPermissionAssignmentIntersectFolder() {
  
    GrouperSession grouperSession = GrouperSession.startRootSession();
    AttributeDef permissionDef = new AttributeDefSave(grouperSession)
      .assignName("stem:permissionDef").assignCreateParentStemsIfNotExist(true)
      .assignAttributeDefType(AttributeDefType.perm)
      .save();
    
    permissionDef.setAssignToEffMembership(true);
    permissionDef.setAssignToGroup(true);
    permissionDef.store();
    
    Group groupProgrammers = new GroupSave(grouperSession).assignName("stem:orgs:itEmployee:programmers").assignCreateParentStemsIfNotExist(true).save();
    Group groupProgrammersSub = new GroupSave(grouperSession).assignName("stem2:programmerssub").assignCreateParentStemsIfNotExist(true).save();
    Group groupSysadmins = new GroupSave(grouperSession).assignName("stem:orgs:itEmployee:sysadmins").assignCreateParentStemsIfNotExist(true).save();
    groupProgrammers.addMember(groupProgrammersSub.toSubject());
    
    Stem itEmployee = StemFinder.findByName(grouperSession, "stem:orgs:itEmployee", true);
    
    //make a role
    Role payrollUser = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollUser").assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();
    Role payrollGuest = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollGuest").assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();
  
    //assign a user to a role
    
    Subject subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Subject subject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Subject subject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    
    Subject subject3 = SubjectFinder.findByIdAndSource("test.subject.3", "jdbc", true);
    Subject subject4 = SubjectFinder.findByIdAndSource("test.subject.4", "jdbc", true);
    Subject subject5 = SubjectFinder.findByIdAndSource("test.subject.5", "jdbc", true);

    payrollUser.addMember(subject0, false);
    payrollGuest.addMember(subject1, false);
    payrollUser.addMember(subject3, false);
    payrollGuest.addMember(subject4, false);
    
    //create a permission, assign to role
    AttributeDefName canLogin = new AttributeDefNameSave(grouperSession, permissionDef).assignName("apps:payroll:permissions:canLogin").assignCreateParentStemsIfNotExist(true).save();
    
    payrollUser.getPermissionRoleDelegate().assignRolePermission(canLogin);
    
    //assign the permission to another user directly, not due to a role
    payrollGuest.getPermissionRoleDelegate().assignSubjectRolePermission(canLogin, subject1);
    payrollGuest.getPermissionRoleDelegate().assignSubjectRolePermission(canLogin, subject4);
    
    //see that they both have the permission
    Member member0 = MemberFinder.findBySubject(grouperSession, subject0, false);
    Member member1 = MemberFinder.findBySubject(grouperSession, subject1, false);
    Member member3 = MemberFinder.findBySubject(grouperSession, subject3, false);
    Member member4 = MemberFinder.findBySubject(grouperSession, subject4, false);
    
    Set<PermissionEntry> permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member0.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member1.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
    
    RuleApi.permissionFolderIntersection(SubjectFinder.findRootSubject(), permissionDef, itEmployee, Stem.Scope.SUB);
    
    groupProgrammers.addMember(subject0, false);
    groupSysadmins.addMember(subject0, false);
    groupProgrammers.addMember(subject1, false);
    groupSysadmins.addMember(subject1, false);
    groupProgrammers.addMember(subject2, false);
    groupSysadmins.addMember(subject2, false);
    groupProgrammersSub.addMember(subject3, false);
    groupSysadmins.addMember(subject3, false);
    groupProgrammersSub.addMember(subject4, false);
    groupSysadmins.addMember(subject4, false);
    groupProgrammersSub.addMember(subject5, false);
    groupSysadmins.addMember(subject5, false);
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_grouperRules");
    
    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;
    
    //doesnt do anything
    groupProgrammers.deleteMember(subject2);
    groupSysadmins.deleteMember(subject2);
    groupProgrammersSub.deleteMember(subject5);
    groupSysadmins.deleteMember(subject5);
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_grouperRules");
    
    assertEquals(initialFirings, RuleEngine.ruleFirings);
  
    groupProgrammers.deleteMember(subject0);
    groupProgrammersSub.deleteMember(subject3);
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_grouperRules");
    
    assertEquals(initialFirings, RuleEngine.ruleFirings);

    groupSysadmins.deleteMember(subject0);
    groupSysadmins.deleteMember(subject3);
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_grouperRules");
    
    //should come out of groupA
    assertEquals(initialFirings+2, RuleEngine.ruleFirings);
    
    assertFalse(payrollUser.hasMember(subject0));
    assertFalse(payrollUser.hasMember(subject3));
  
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member0.getUuid());
    assertEquals(0, permissions.size());
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member1.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member3.getUuid());
    assertEquals(0, permissions.size());
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member4.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
    
    //take out second user
    groupSysadmins.deleteMember(subject1);
    groupSysadmins.deleteMember(subject4);
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_grouperRules");
    
    assertEquals(initialFirings+2, RuleEngine.ruleFirings);

    groupProgrammers.deleteMember(subject1);
    groupProgrammersSub.deleteMember(subject4);
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_grouperRules");
    
    assertEquals(initialFirings+4, RuleEngine.ruleFirings);

    assertTrue(payrollGuest.hasMember(subject1));
    assertTrue(payrollGuest.hasMember(subject4));
  
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member1.getUuid());
    assertEquals(0, permissions.size());
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member4.getUuid());
    assertEquals(0, permissions.size());
  }

  /**
   * 
   */
  public void testPermissionAssignmentDisabledDate() {
  
    GrouperSession grouperSession = GrouperSession.startRootSession();
    AttributeDef permissionDef = new AttributeDefSave(grouperSession)
      .assignName("stem:permissionDef").assignCreateParentStemsIfNotExist(true)
      .assignAttributeDefType(AttributeDefType.perm)
      .save();
    
    permissionDef.setAssignToEffMembership(true);
    permissionDef.setAssignToGroup(true);
    permissionDef.store();
    
    Group groupEmployee = new GroupSave(grouperSession).assignName("stem:employee").assignCreateParentStemsIfNotExist(true).save();
  
    //make a role
    Role payrollUser = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollUser").assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();
    Role payrollGuest = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollGuest").assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();
  
    Subject subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Subject subject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Subject subject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);

    //assign a user to a role
    payrollUser.addMember(subject0, false);
    payrollGuest.addMember(subject1, false);
    
    //create a permission, assign to role
    AttributeDefName canLogin = new AttributeDefNameSave(grouperSession, permissionDef).assignName("apps:payroll:permissions:canLogin").assignCreateParentStemsIfNotExist(true).save();
    
    payrollUser.getPermissionRoleDelegate().assignRolePermission(canLogin);
    
    //assign the permission to another user directly, not due to a role
    payrollGuest.getPermissionRoleDelegate().assignSubjectRolePermission(canLogin, subject1);
    
    //see that they both have the permission
    Member member0 = MemberFinder.findBySubject(grouperSession, subject0, false);
    Member member1 = MemberFinder.findBySubject(grouperSession, subject1, false);
    
    Set<PermissionEntry> permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member0.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member1.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());

    RuleApi.permissionGroupIntersection(SubjectFinder.findRootSubject(), permissionDef, groupEmployee, 7);

    groupEmployee.addMember(subject0);
    groupEmployee.addMember(subject1);
    groupEmployee.addMember(subject2);
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_grouperRules");
    
    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;
    
    //doesnt do anything
    groupEmployee.deleteMember(subject2);
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_grouperRules");
    
    assertEquals(initialFirings, RuleEngine.ruleFirings);
  
    groupEmployee.deleteMember(subject0);
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_grouperRules");
    
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
    
    //should come out of groupA in 7 days
    Membership membership = ((Group)payrollUser).getImmediateMembership(Group.getDefaultList(), member0, true, true);
    
    assertNotNull(membership.getDisabledTime());
    long disabledTime = membership.getDisabledTime().getTime();
    
    assertTrue("More than 6 days: " + new Date(disabledTime), disabledTime > System.currentTimeMillis() + (6 * 24 * 60 * 60 * 1000));
    assertTrue("Less than 8 days: " + new Date(disabledTime), disabledTime < System.currentTimeMillis() + (8 * 24 * 60 * 60 * 1000));

    
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member0.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member1.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
    
    //take out second user
    groupEmployee.deleteMember(subject1);
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_grouperRules");
    
    assertEquals(initialFirings+2, RuleEngine.ruleFirings);

    assertTrue(payrollGuest.hasMember(subject1));

    //should come out of groupA in 7 days
    membership = ((Group)payrollGuest).getImmediateMembership(Group.getDefaultList(), member1, true, true);
    
    assertNull(membership.getDisabledTime());

    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member1.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());

    Timestamp timestamp = permissions.iterator().next().getDisabledTime();

    disabledTime = timestamp.getTime();
    
    assertTrue("More than 6 days: " + new Date(disabledTime), disabledTime > System.currentTimeMillis() + (6 * 24 * 60 * 60 * 1000));
    assertTrue("Less than 8 days: " + new Date(disabledTime), disabledTime < System.currentTimeMillis() + (8 * 24 * 60 * 60 * 1000));

  }

  /**
   * 
   */
  public void testRuleVetoInOrg() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group ruleGroup = new GroupSave(grouperSession).assignName("stem:a").assignCreateParentStemsIfNotExist(true).save();

    Group groupProgrammers = new GroupSave(grouperSession).assignName("stem:orgs:itEmployee:programmers").assignCreateParentStemsIfNotExist(true).save();
    Group groupSysadmins = new GroupSave(grouperSession).assignName("stem:orgs:itEmployee:sysadmins").assignCreateParentStemsIfNotExist(true).save();
    
    Stem mustBeInStem = StemFinder.findByName(grouperSession, "stem:orgs:itEmployee", true);
    
    RuleApi.vetoMembershipIfNotInGroupInFolder(SubjectFinder.findRootSubject(), ruleGroup, mustBeInStem, Stem.Scope.SUB, 
        "rule.entity.must.be.in.IT.employee.to.be.in.group", "Entity cannot be a member of group if not in the IT department org");
    
    Subject subject0 = SubjectFinder.findById("test.subject.0", true);

    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;
    
    try {
      ruleGroup.addMember(subject0);
      fail("Should be vetoed");
    } catch (RuleVeto rve) {
      //this is good
      String stack = ExceptionUtils.getFullStackTrace(rve);
      assertTrue(stack, stack.contains("Entity cannot be a member of group if not in the IT department org"));
    }
  
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
  
    groupProgrammers.addMember(subject0);
    ruleGroup.addMember(subject0);
    
    assertEquals("Didnt fire since is a member", initialFirings+1, RuleEngine.ruleFirings);

    assertTrue(ruleGroup.hasMember(subject0));

    ruleGroup.deleteMember(subject0);
    groupProgrammers.deleteMember(subject0);
    groupSysadmins.addMember(subject0);
 
    ruleGroup.addMember(subject0);

    assertEquals("Didnt fire since is a member", initialFirings+1, RuleEngine.ruleFirings);

    assertTrue(ruleGroup.hasMember(subject0));

  }

  /**
   * 
   */
  public void testRuleVetoPermissions() {

    GrouperSession grouperSession = GrouperSession.startRootSession();
    AttributeDef permissionDef = new AttributeDefSave(grouperSession).assignName("stem:permissionDef").assignCreateParentStemsIfNotExist(true).assignAttributeDefType(AttributeDefType.perm).save();
    
    permissionDef.setAssignToEffMembership(true);
    permissionDef.setAssignToGroup(true);
    permissionDef.store();
    
    Group groupEmployee = new GroupSave(grouperSession).assignName("stem:employee").assignCreateParentStemsIfNotExist(true).save();

    //make a role
    Role payrollUser = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollUser").assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();

    Subject subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    
    //assign a user to a role
    payrollUser.addMember(subject0, false);
    
    //create a permission, assign to role
    AttributeDefName canLogin = new AttributeDefNameSave(grouperSession, permissionDef).assignName("apps:payroll:permissions:canLogin").assignCreateParentStemsIfNotExist(true).save();
       
    RuleApi.vetoPermissionIfNotInGroup(SubjectFinder.findRootSubject(), permissionDef, groupEmployee, "rule.entity.must.be.an.employee", "Entity cannot be assigned these permissions unless they are an employee");

    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;
    

    try {
      //assign the permission to another user directly, not due to a role
      payrollUser.getPermissionRoleDelegate().assignSubjectRolePermission(canLogin, subject0);
    } catch (RuleVeto rve) {
      //this is good
      String stack = ExceptionUtils.getFullStackTrace(rve);
      assertTrue(stack, stack.contains("Entity cannot be assigned these permissions unless they are an employee"));
    }

    assertEquals(initialFirings+1, RuleEngine.ruleFirings);

    //see that not have the permission
    Member member0 = MemberFinder.findBySubject(grouperSession, subject0, false);
    
    Set<PermissionEntry> permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member0.getUuid());
    assertEquals(0, permissions.size());
    
    groupEmployee.addMember(subject0);
    
    payrollUser.getPermissionRoleDelegate().assignSubjectRolePermission(canLogin, subject0);

    assertEquals("Didnt fire since is a member", initialFirings+1, RuleEngine.ruleFirings);

    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member0.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
    
  }

  /**
   * 
   */
  public void testRuleEmailFlattenedRemove() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group groupEmployee = new GroupSave(grouperSession).assignName("stem:employee").assignCreateParentStemsIfNotExist(true).save();
    Group groupProgrammer = new GroupSave(grouperSession).assignName("stem:programmer").assignCreateParentStemsIfNotExist(true).save();
    Group groupResearcher = new GroupSave(grouperSession).assignName("stem:researcher").assignCreateParentStemsIfNotExist(true).save();

    groupEmployee.addMember(groupProgrammer.toSubject());
    groupEmployee.addMember(groupResearcher.toSubject());

    Subject subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    
    //subject0 is an employee by two paths
    groupProgrammer.addMember(subject0, false);
    groupResearcher.addMember(subject0, false);

    RuleApi.emailOnFlattenedMembershipRemove(SubjectFinder.findRootSubject(), groupEmployee, "a@b.c, ${safeSubject.emailAddress}", "You will be removed from group: ${groupDisplayExtension}", "template: testEmailGroupBodyFlattenedRemove");
    
    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;
    long initialEmailCount = GrouperEmail.testingEmailCount;
    
    //doesnt do anything, still in the group by another path
    groupProgrammer.deleteMember(subject0);
  
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_grouperRules");

    assertEquals(initialFirings, RuleEngine.ruleFirings);
    assertEquals(initialEmailCount, GrouperEmail.testingEmailCount);
  
    groupResearcher.deleteMember(subject0);

    //run the change log to change log temp and rules consumer
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_TEMP_TO_CHANGE_LOG);
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_CONSUMER_PREFIX + "grouperRules");

    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
    assertEquals(initialEmailCount+1, GrouperEmail.testingEmailCount);
    
    //should send an email...
    
  }

  /**
   * 
   */
  public void testRuleEmailFlattenedAddFromStem() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group groupEmployee = new GroupSave(grouperSession).assignName("stem:employee").assignCreateParentStemsIfNotExist(true).save();
    Group groupProgrammer = new GroupSave(grouperSession).assignName("stem:programmer").assignCreateParentStemsIfNotExist(true).save();
    Group groupResearcher = new GroupSave(grouperSession).assignName("stem:researcher").assignCreateParentStemsIfNotExist(true).save();
  
    groupEmployee.addMember(groupProgrammer.toSubject());
    groupEmployee.addMember(groupResearcher.toSubject());
  
    Stem stem = StemFinder.findByName(grouperSession, "stem", true);
    
    Subject subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    
    RuleApi.emailOnFlattenedMembershipAddFromStem(SubjectFinder.findRootSubject(), stem, Stem.Scope.SUB, "a@b.c, ${safeSubject.emailAddress}", "template: testEmailGroupSubjectFlattenedAddInFolder", "Hello ${safeSubject.name},\n\nJust letting you know you were removed from group ${groupDisplayExtension} in the central Groups management system.  Please do not respond to this email.\n\nRegards.");
    
    //run at first so the consumer is initted
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_grouperRules");

    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;
    
    long initialEmailCount = GrouperEmail.testingEmailCount;
    
    //subject0 is an employee by two paths
    groupProgrammer.addMember(subject0, false);
    
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_TEMP_TO_CHANGE_LOG);
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_CONSUMER_PREFIX + "grouperRules");
  
    assertEquals(initialFirings+2, RuleEngine.ruleFirings);
    assertEquals(initialEmailCount+2, GrouperEmail.testingEmailCount);

    groupResearcher.addMember(subject0, false);

    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_TEMP_TO_CHANGE_LOG);
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_CONSUMER_PREFIX + "grouperRules");
  
    assertEquals(initialFirings+3, RuleEngine.ruleFirings);
    assertEquals(initialEmailCount+3, GrouperEmail.testingEmailCount);
    
    groupEmployee.addMember(subject0);
  
    //run the change log to change log temp and rules consumer
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_TEMP_TO_CHANGE_LOG);
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_CONSUMER_PREFIX + "grouperRules");
  
    //should not send an email...
    assertEquals(initialFirings+3, RuleEngine.ruleFirings);
    assertEquals(initialEmailCount+3, GrouperEmail.testingEmailCount);
  
    
  }

  /**
   * 
   */
  public void testRuleEmailDisabledDate() {
    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group groupEmployee = new GroupSave(grouperSession).assignName("stem:employee").assignCreateParentStemsIfNotExist(true).save();
    Group groupProgrammer = new GroupSave(grouperSession).assignName("stem:programmer").assignCreateParentStemsIfNotExist(true).save();
  
    groupEmployee.addMember(groupProgrammer.toSubject());

    Subject subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    
    RuleApi.emailOnFlattenedDisabledDate(SubjectFinder.findRootSubject(), groupEmployee, 6, 8, GrouperConfig.getProperty("mail.test.address") + ", ${safeSubject.emailAddress}", "You will be removed from group: ${groupDisplayExtension} on ${ruleElUtils.formatDate(membershipDisabledTimestamp, 'yyyy/MM/dd')}", "Hello ${safeSubject.name},\n\nJust letting you know you will be removed from group ${groupDisplayExtension} on ${ruleElUtils.formatDate(membershipDisabledTimestamp, 'yyyy/MM/dd')} in the central Groups management system.  Please do not respond to this email.\n\nRegards.");
    
    //count rule firings
    long initialEmailCount = GrouperEmail.testingEmailCount;
    
    groupEmployee.addMember(subject0, false);
    
    GrouperLoader.runOnceByJobName(grouperSession, "MAINTENANCE__rules");

    Member member0 = MemberFinder.findBySubject(grouperSession, subject0, false);
    
    Membership membership = groupEmployee.getImmediateMembership(Group.getDefaultList(), member0, true, true);
    
    //set disabled 7 days in the future
    membership.setDisabledTime(new java.sql.Timestamp(System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000)));
    membership.update();
    
    GrouperLoader.runOnceByJobName(grouperSession, "MAINTENANCE__rules");
    
    //should fire
    assertEquals(initialEmailCount + 1, GrouperEmail.testingEmailCount);

    membership.setDisabledTime(new Timestamp(System.currentTimeMillis() + (5 * 24 * 60 * 60 * 1000)));
    membership.update();
    
    GrouperLoader.runOnceByJobName(grouperSession, "MAINTENANCE__rules");
    
    //should not fire
    assertEquals(initialEmailCount + 1, GrouperEmail.testingEmailCount);

    membership.setDisabledTime(new Timestamp(System.currentTimeMillis() + (9 * 24 * 60 * 60 * 1000)));
    membership.update();
    
    GrouperLoader.runOnceByJobName(grouperSession, "MAINTENANCE__rules");
    
    //should not fire
    assertEquals(initialEmailCount + 1, GrouperEmail.testingEmailCount);

    membership.setDisabledTime(new Timestamp(System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000)));
    membership.update();
    
    GrouperLoader.runOnceByJobName(grouperSession, "MAINTENANCE__rules");
    
    //should fire
    assertEquals(initialEmailCount + 2, GrouperEmail.testingEmailCount);

    groupProgrammer.addMember(subject0);

    GrouperLoader.runOnceByJobName(grouperSession, "MAINTENANCE__rules");
    
    //should not fire
    assertEquals(initialEmailCount + 2, GrouperEmail.testingEmailCount);

  }

  /**
   * 
   */
  public void testRuleEmailPermissionsDisabledDate() {

    GrouperSession grouperSession = GrouperSession.startRootSession();

    AttributeDef permissionDef = new AttributeDefSave(grouperSession).assignName("stem:permissionDef").assignCreateParentStemsIfNotExist(true).assignAttributeDefType(AttributeDefType.perm).save();
    
    permissionDef.setAssignToEffMembership(true);
    permissionDef.setAssignToGroup(true);
    permissionDef.store();
    
    RuleApi.emailOnFlattenedPermissionDisabledDate(SubjectFinder.findRootSubject(), permissionDef, 6, 8, GrouperConfig.getProperty("mail.test.address") + ", ${safeSubject.emailAddress}", "You will have this permission unassigned: ${attributeDefNameDisplayExtension} in role ${roleDisplayExtension}, removed on ${ruleElUtils.formatDate(permissionDisabledTimestamp, 'yyyy/MM/dd')}", "Hello ${safeSubject.name},\n\nJust letting you know you will have this permission removed ${attributeDefNameDisplayExtension} in role ${roleDisplayExtension}, removed on ${ruleElUtils.formatDate(permissionDisabledTimestamp, 'yyyy/MM/dd')} in the central Groups / Permissions management system.  Please do not respond to this email.\n\nRegards.");

    //count rule firings
    long initialEmailCount = GrouperEmail.testingEmailCount;

    //make a role
    Role payrollUser = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollUser").assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();
    Role payrollGuest = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollGuest").assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();

    Subject subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);

    //subject 1,2 is just more data in the mix
    Subject subject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Subject subject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    

    payrollUser.addMember(subject1, false);
    payrollGuest.addMember(subject0, false);
    payrollGuest.addMember(subject2, false);
    
    //create a permission, assign to role
    AttributeDefName canLogin = new AttributeDefNameSave(grouperSession, permissionDef).assignName("apps:payroll:permissions:canLogin").assignCreateParentStemsIfNotExist(true).save();
    
    payrollUser.getPermissionRoleDelegate().assignRolePermission(canLogin);

    payrollGuest.getPermissionRoleDelegate().assignSubjectRolePermission(canLogin, subject2);

    try {
      GrouperDAOFactory.getFactory().getPermissionEntry().findPermissionsByAttributeDefDisabledRange(permissionDef.getId(),
          null, null);

      fail("should need either disabled from or to");
    } catch (Exception e) {
      //good
    }

    AttributeAssign attributeAssign = payrollGuest.getPermissionRoleDelegate().assignSubjectRolePermission(canLogin, subject0).getAttributeAssign();

    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);
    
    //should not fire
    assertEquals(initialEmailCount, GrouperEmail.testingEmailCount);

    //set disabled 7 days in the future
    attributeAssign.setDisabledTime(new java.sql.Timestamp(System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000)));
    attributeAssign.saveOrUpdate(true);
    
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);
    
    //should fire
    assertEquals(initialEmailCount + 1, GrouperEmail.testingEmailCount);
  
    attributeAssign.setDisabledTime(new java.sql.Timestamp(System.currentTimeMillis() + (5 * 24 * 60 * 60 * 1000)));
    attributeAssign.saveOrUpdate(true);
    
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);
    
    //should not fire
    assertEquals(initialEmailCount + 1, GrouperEmail.testingEmailCount);
  
    attributeAssign.setDisabledTime(new java.sql.Timestamp(System.currentTimeMillis() + (9 * 24 * 60 * 60 * 1000)));
    attributeAssign.saveOrUpdate(true);
    
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);
    
    //should not fire
    assertEquals(initialEmailCount + 1, GrouperEmail.testingEmailCount);
  
    attributeAssign.setDisabledTime(new java.sql.Timestamp(System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000)));
    attributeAssign.saveOrUpdate(true);
    
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);
    
    //should fire
    assertEquals(initialEmailCount + 2, GrouperEmail.testingEmailCount);
  
    payrollUser.addMember(subject0, false);
  
    GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);
    
    //should not fire
    assertEquals(initialEmailCount + 2, GrouperEmail.testingEmailCount);
  
  }

  /**
   * 
   */
  public void testGroupIntersectionDaemon() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Subject subject0 = SubjectFinder.findById("test.subject.0", true);
    Subject subject1 = SubjectFinder.findById("test.subject.1", true);
    Subject subject2 = SubjectFinder.findById("test.subject.2", true);
    Subject subject9 = SubjectFinder.findById("test.subject.9", true);
    
    Group groupA = new GroupSave(grouperSession).assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignName("stem:a").assignCreateParentStemsIfNotExist(true).save();
    groupA.grantPriv(subject9, AccessPrivilege.ADMIN, false);
  
    Group groupB = new GroupSave(grouperSession).assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignName("stem:b").assignCreateParentStemsIfNotExist(true).save();
    groupB.grantPriv(subject9, AccessPrivilege.READ, false);
    
    Group groupC = new GroupSave(grouperSession).assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignName("stem:c").assignCreateParentStemsIfNotExist(true).save();
    groupC.grantPriv(subject9, AccessPrivilege.READ, false);
    
    groupB.addMember(groupC.toSubject());
    
    RuleApi.groupIntersection(subject9, groupA, groupB);
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_grouperRules");
    
    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;
    
    groupA.addMember(subject0, false);
    groupA.addMember(subject1, false);
    groupA.addMember(subject2, false);
    groupB.addMember(subject1, false);
    groupC.addMember(subject2, false);
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_grouperRules");
    
    //run the daemon
    String status = GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);
    
    assertTrue(status.toLowerCase().contains("success"));
    
    assertEquals(initialFirings + 1, RuleEngine.ruleFirings);

    assertFalse(groupA.hasMember(subject0));
    assertTrue(groupA.hasMember(subject1));
    assertTrue(groupA.hasMember(subject2));
    
  }

  /**
   * 
   */
  public void testGroupIntersectionDateDaemon() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group groupA = new GroupSave(grouperSession).assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignName("stem:a").assignCreateParentStemsIfNotExist(true).save();
    
    Subject subject9 = SubjectFinder.findById("test.subject.9", true);
    Subject subject0 = SubjectFinder.findById("test.subject.0", true);
    Subject subject1 = SubjectFinder.findById("test.subject.1", true);
    Subject subject2 = SubjectFinder.findById("test.subject.2", true);
  
    groupA.grantPriv(subject9, AccessPrivilege.ADMIN, false);

    Group groupB = new GroupSave(grouperSession).assignSaveMode(SaveMode.INSERT_OR_UPDATE).assignName("stem:b").assignCreateParentStemsIfNotExist(true).save();
    groupB.grantPriv(subject9, AccessPrivilege.READ, false);

    RuleApi.groupIntersection(subject9, groupA, groupB, 7);

    groupA.addMember(subject0);
    groupA.addMember(subject1);
    groupB.addMember(subject1);
    groupA.addMember(subject2);
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_grouperRules");
    
    Member member2 = MemberFinder.findBySubject(grouperSession, subject2, false);
    
    Membership membership = groupA.getImmediateMembership(Group.getDefaultList(), member2, true, true);
    
    membership.setDisabledTime(new Timestamp(System.currentTimeMillis() + (3 * 24 * 60 * 60 * 1000)));

    GrouperDAOFactory.getFactory().getMembership().update(membership);
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_grouperRules");
    
    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;

    //run the daemon
    String status = GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);

    assertTrue(status.toLowerCase().contains("success"));

    assertEquals(initialFirings + 1, RuleEngine.ruleFirings);

    //should have a disabled date in group A
    assertTrue(groupA.hasMember(subject0));
    assertTrue(groupA.hasMember(subject1));
    assertTrue(groupA.hasMember(subject2));
  
    Member member0 = MemberFinder.findBySubject(grouperSession, subject0, false);
    Member member1 = MemberFinder.findBySubject(grouperSession, subject1, false);
    
    membership = groupA.getImmediateMembership(Group.getDefaultList(), member0, true, true);
    
    assertNotNull(membership.getDisabledTime());
    long disabledTime = membership.getDisabledTime().getTime();
    
    assertTrue("More than 6 days: " + new Date(disabledTime), disabledTime > System.currentTimeMillis() + (6 * 24 * 60 * 60 * 1000));
    assertTrue("Less than 8 days: " + new Date(disabledTime), disabledTime < System.currentTimeMillis() + (8 * 24 * 60 * 60 * 1000));
  
    //should have member1, with no disabled date
    membership = groupA.getImmediateMembership(Group.getDefaultList(), member1, true, true);
    
    assertNull(membership.getDisabledTime());
    
    //should have member2 with original disabled date
    membership = groupA.getImmediateMembership(Group.getDefaultList(), member2, true, true);
    
    assertNotNull(membership.getDisabledTime());
    
    disabledTime = membership.getDisabledTime().getTime();
    
    assertTrue("More than 2 days: " + new Date(disabledTime), disabledTime > System.currentTimeMillis() + (2 * 24 * 60 * 60 * 1000));
    assertTrue("Less than 4 days: " + new Date(disabledTime), disabledTime < System.currentTimeMillis() + (4 * 24 * 60 * 60 * 1000));
  }

  /**
   * 
   */
  public void testGroupIntersectionFolderDaemon() {

    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group groupA = new GroupSave(grouperSession).assignName("stem1:a").assignCreateParentStemsIfNotExist(true).save();

    Group groupC = new GroupSave(grouperSession).assignName("stem2:sub:c").assignCreateParentStemsIfNotExist(true).save();
    Group groupD = new GroupSave(grouperSession).assignName("stem3:subgroup").assignCreateParentStemsIfNotExist(true).save();
    groupC.addMember(groupD.toSubject());
    
    Stem stem = StemFinder.findByName(grouperSession, "stem2", true);
    
    RuleApi.groupIntersectionWithFolder(SubjectFinder.findRootSubject(), groupA, stem, Scope.SUB);
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_grouperRules");
    
    long initialFirings = RuleEngine.ruleFirings;
    
    Subject subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Subject subject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Subject subject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);

    groupA.addMember(subject0);
    groupA.addMember(subject1);
    groupA.addMember(subject2);
    groupC.addMember(subject1);
    groupD.addMember(subject2);
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_grouperRules");
    
    //run the daemon
    String status = GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);

    assertTrue(status.toLowerCase().contains("success"));
    
    assertEquals(initialFirings + 1, RuleEngine.ruleFirings);
        
    //should come out of groupA
    assertFalse(groupA.hasMember(subject0));
    assertTrue(groupA.hasMember(subject1));
    assertTrue(groupA.hasMember(subject2));
  
  
  }

  
  /**
   * 
   */
  public void testInheritAttributeDefPrivilegesDaemon() {

    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem2 = new StemSave(grouperSession).assignName("stem2").assignCreateParentStemsIfNotExist(true).save();
  
    Group groupA = new GroupSave(grouperSession).assignName("stem1:admins").assignCreateParentStemsIfNotExist(true).save();
  
    AttributeDef attributeDefB = new AttributeDefSave(grouperSession).assignName("stem2:b").assignCreateParentStemsIfNotExist(true).save();
    AttributeDef attributeDefC = new AttributeDefSave(grouperSession).assignName("stem2:c").assignCreateParentStemsIfNotExist(true).save();
    AttributeDef attributeDefD = new AttributeDefSave(grouperSession).assignName("stem2:d").assignCreateParentStemsIfNotExist(true).save();
    AttributeDef attributeDefE = new AttributeDefSave(grouperSession).assignName("stem3:e").assignCreateParentStemsIfNotExist(true).save();

    //almost has it
    attributeDefC.getPrivilegeDelegate().grantPriv(groupA.toSubject(), AttributeDefPrivilege.ATTR_OPTIN, false);
    attributeDefC.getPrivilegeDelegate().grantPriv(groupA.toSubject(), AttributeDefPrivilege.ATTR_READ, false);

    //has it
    attributeDefD.getPrivilegeDelegate().grantPriv(groupA.toSubject(), AttributeDefPrivilege.ATTR_UPDATE, false);
    attributeDefD.getPrivilegeDelegate().grantPriv(groupA.toSubject(), AttributeDefPrivilege.ATTR_READ, false);
    
    RuleApi.inheritAttributeDefPrivileges(SubjectFinder.findRootSubject(), stem2, Scope.SUB, groupA.toSubject(), Privilege.getInstances("attrRead, attrUpdate"));
  
    long initialFirings = RuleEngine.ruleFirings;

    //run the daemon
    String status = GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);

    assertTrue(status, status.toLowerCase().contains("success"));

    //count rule firings
    assertEquals(initialFirings+3, RuleEngine.ruleFirings);
  
    //make sure allowed
    assertTrue(attributeDefB.getPrivilegeDelegate().hasAttrUpdate(groupA.toSubject()));
    assertTrue(attributeDefB.getPrivilegeDelegate().hasAttrRead(groupA.toSubject()));
    
    //make sure allowed
    assertTrue(attributeDefC.getPrivilegeDelegate().hasAttrUpdate(groupA.toSubject()));
    assertTrue(attributeDefC.getPrivilegeDelegate().hasAttrRead(groupA.toSubject()));
    assertTrue(attributeDefC.getPrivilegeDelegate().hasAttrOptin(groupA.toSubject()));
  
    //make sure allowed
    assertTrue(attributeDefD.getPrivilegeDelegate().hasAttrUpdate(groupA.toSubject()));
    assertTrue(attributeDefD.getPrivilegeDelegate().hasAttrRead(groupA.toSubject()));

    assertFalse(attributeDefE.getPrivilegeDelegate().hasAttrUpdate(groupA.toSubject()));
    assertFalse(attributeDefE.getPrivilegeDelegate().hasAttrRead(groupA.toSubject()));

  }

  /**
   * 
   */
  public void testInheritGroupPrivilegesDaemon() {

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem stem2 = new StemSave(grouperSession).assignName("stem2").assignCreateParentStemsIfNotExist(true).save();
  
    Group groupA = new GroupSave(grouperSession).assignName("stem1:admins").assignCreateParentStemsIfNotExist(true).save();

    Group groupB = new GroupSave(grouperSession).assignName("stem2:b").assignCreateParentStemsIfNotExist(true).save();
    Group groupC = new GroupSave(grouperSession).assignName("stem2:c").assignCreateParentStemsIfNotExist(true).save();
    Group groupD = new GroupSave(grouperSession).assignName("stem2:d").assignCreateParentStemsIfNotExist(true).save();
    Group groupE = new GroupSave(grouperSession).assignName("stem3:e").assignCreateParentStemsIfNotExist(true).save();

    groupC.grantPriv(groupA.toSubject(), AccessPrivilege.OPTIN, false);
    groupC.grantPriv(groupA.toSubject(), AccessPrivilege.READ, false);
    
    groupD.grantPriv(groupA.toSubject(), AccessPrivilege.UPDATE, false);
    groupD.grantPriv(groupA.toSubject(), AccessPrivilege.READ, false);

    RuleApi.inheritGroupPrivileges(SubjectFinder.findRootSubject(), stem2, Scope.SUB, groupA.toSubject(), Privilege.getInstances("read, update"));
  
    long initialFirings = RuleEngine.ruleFirings;
    
    //run the daemon
    String status = GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);

    assertTrue(status, status.toLowerCase().contains("success"));

    //count rule firings
    assertEquals(initialFirings+3, RuleEngine.ruleFirings);
  
    //make sure allowed
    assertTrue(groupB.hasUpdate(groupA.toSubject()));
    assertTrue(groupB.hasRead(groupA.toSubject()));
    
    assertTrue(groupC.hasUpdate(groupA.toSubject()));
    assertTrue(groupC.hasOptin(groupA.toSubject()));
    assertTrue(groupC.hasRead(groupA.toSubject()));

    assertTrue(groupD.hasUpdate(groupA.toSubject()));
    assertTrue(groupD.hasRead(groupA.toSubject()));
    
    assertFalse(groupE.hasUpdate(groupA.toSubject()));
    assertFalse(groupE.hasRead(groupA.toSubject()));
      
  }

  /**
   * 
   */
  public void testInheritFolderPrivilegesDaemon() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem2 = new StemSave(grouperSession).assignName("stem2").assignCreateParentStemsIfNotExist(true).save();
  
    Group groupA = new GroupSave(grouperSession).assignName("stem1:admins").assignCreateParentStemsIfNotExist(true).save();
  
    Subject subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    groupA.addMember(subject0);
    
    Stem stemB = new StemSave(grouperSession).assignName("stem2:b").assignCreateParentStemsIfNotExist(true).save();
    Stem stemC = new StemSave(grouperSession).assignName("stem2:c").assignCreateParentStemsIfNotExist(true).save();
    Stem stemD = new StemSave(grouperSession).assignName("stem2:d").assignCreateParentStemsIfNotExist(true).save();
    Stem stemE = new StemSave(grouperSession).assignName("stem3:e").assignCreateParentStemsIfNotExist(true).save();

    //stemC can stem and create
    stemC.grantPriv(groupA.toSubject(), NamingPrivilege.STEM, false);
    stemC.grantPriv(groupA.toSubject(), NamingPrivilege.CREATE, false);
    
    //stemD can stem but not create
    stemD.grantPriv(groupA.toSubject(), NamingPrivilege.STEM, false);
    
    RuleApi.inheritFolderPrivileges(SubjectFinder.findRootSubject(), stem2, Scope.SUB, groupA.toSubject(), Privilege.getInstances("stem, create"));
  
    long initialFirings = RuleEngine.ruleFirings;
    
    //run the daemon
    String status = GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);

    assertTrue(status, status.toLowerCase().contains("success"));

    //count rule firings
    assertEquals(initialFirings+3, RuleEngine.ruleFirings);
  
    //make sure allowed
    assertTrue(stemB.hasCreate(subject0));
    assertTrue(stemB.hasStem(subject0));

    assertTrue(stemC.hasCreate(subject0));
    assertTrue(stemC.hasStem(subject0));

    assertTrue(stemD.hasCreate(subject0));
    assertTrue(stemD.hasStem(subject0));

    assertFalse(stemE.hasCreate(subject0));
    assertFalse(stemE.hasStem(subject0));
    
  
  }

  /**
   * 
   */
  public void testPermissionAssignmentDaemon() {
  
    GrouperSession grouperSession = GrouperSession.startRootSession();
    AttributeDef permissionDef = new AttributeDefSave(grouperSession).assignName("stem:permissionDef").assignCreateParentStemsIfNotExist(true).assignAttributeDefType(AttributeDefType.perm).save();
    
    permissionDef.setAssignToEffMembership(true);
    permissionDef.setAssignToGroup(true);
    permissionDef.store();
    
    Group groupEmployee = new GroupSave(grouperSession).assignName("stem:employee").assignCreateParentStemsIfNotExist(true).save();
    Group groupEmployeeSub = new GroupSave(grouperSession).assignName("stem:employeesub").assignCreateParentStemsIfNotExist(true).save();
    groupEmployee.addMember(groupEmployeeSub.toSubject());
    
    
    //make a role
    Role payrollUser = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollUser").assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();
    Role payrollGuest = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollGuest").assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();
  
    //assign a user to a role
    Subject subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    payrollUser.addMember(subject0, false);
    Subject subject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Subject subject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    payrollGuest.addMember(subject1, false);
    Subject subject3 = SubjectFinder.findByIdAndSource("test.subject.3", "jdbc", true);
    payrollUser.addMember(subject3, false);
    Subject subject4 = SubjectFinder.findByIdAndSource("test.subject.4", "jdbc", true);
    payrollGuest.addMember(subject4, false);
    Subject subject5 = SubjectFinder.findByIdAndSource("test.subject.5", "jdbc", true);
    payrollUser.addMember(subject5, false);
    Subject subject6 = SubjectFinder.findByIdAndSource("test.subject.6", "jdbc", true);
    payrollGuest.addMember(subject6, false);
    
    //create a permission, assign to role
    AttributeDefName canLogin = new AttributeDefNameSave(grouperSession, permissionDef).assignName("apps:payroll:permissions:canLogin").assignCreateParentStemsIfNotExist(true).save();
    
    payrollUser.getPermissionRoleDelegate().assignRolePermission(canLogin);
    
    //assign the permission to another user directly, not due to a role
    payrollGuest.getPermissionRoleDelegate().assignSubjectRolePermission(canLogin, subject1);
    payrollGuest.getPermissionRoleDelegate().assignSubjectRolePermission(canLogin, subject4);
    payrollGuest.getPermissionRoleDelegate().assignSubjectRolePermission(canLogin, subject6);
    
    //see that they both have the permission
    Member member0 = MemberFinder.findBySubject(grouperSession, subject0, false);
    Member member1 = MemberFinder.findBySubject(grouperSession, subject1, false);
    Member member2 = MemberFinder.findBySubject(grouperSession, subject2, true);
    Member member3 = MemberFinder.findBySubject(grouperSession, subject3, true);
    Member member4 = MemberFinder.findBySubject(grouperSession, subject4, true);
    Member member5 = MemberFinder.findBySubject(grouperSession, subject5, true);
    Member member6 = MemberFinder.findBySubject(grouperSession, subject6, true);
    
    Set<PermissionEntry> permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member0.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member1.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());

    RuleApi.permissionGroupIntersection(SubjectFinder.findRootSubject(), permissionDef, groupEmployee);
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_grouperRules");
    
    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;

    groupEmployee.addMember(subject3);
    groupEmployee.addMember(subject4);
    groupEmployeeSub.addMember(subject5);
    groupEmployeeSub.addMember(subject6);
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_grouperRules");
    
    //run the daemon
    String status = GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);

    assertTrue(status.toLowerCase().contains("success"));

    assertEquals(initialFirings + 2, RuleEngine.ruleFirings);
  
    //should come out of groupA
    assertFalse(payrollUser.hasMember(subject0));

    assertTrue(payrollUser.hasMember(subject3));
    assertTrue(payrollUser.hasMember(subject5));

    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member0.getUuid());
    assertEquals(0, permissions.size());
    
    assertTrue(payrollGuest.hasMember(subject1));
    assertTrue(payrollGuest.hasMember(subject4));
    assertTrue(payrollGuest.hasMember(subject6));
  
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member1.getUuid());
    assertEquals(0, permissions.size());
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member2.getUuid());
    assertEquals(0, permissions.size());
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member3.getUuid());
    assertEquals(1, permissions.size());
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member5.getUuid());
    assertEquals(1, permissions.size());
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member4.getUuid());
    assertEquals(1, permissions.size());
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member6.getUuid());
    assertEquals(1, permissions.size());
  }

  /**
   * 
   */
  public void testPermissionAssignmentDisabledDateDaemon() {
  
    GrouperSession grouperSession = GrouperSession.startRootSession();
    AttributeDef permissionDef = new AttributeDefSave(grouperSession).assignName("stem:permissionDef").assignCreateParentStemsIfNotExist(true).assignAttributeDefType(AttributeDefType.perm).save();
    
    permissionDef.setAssignToEffMembership(true);
    permissionDef.setAssignToGroup(true);
    permissionDef.store();
    
    Group groupEmployee = new GroupSave(grouperSession).assignName("stem:employee").assignCreateParentStemsIfNotExist(true).save();
  
    //make a role
    Role payrollUser = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollUser").assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();
    Role payrollGuest = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollGuest").assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();
  
    Subject subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Subject subject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Subject subject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    Subject subject3 = SubjectFinder.findByIdAndSource("test.subject.3", "jdbc", true);
    Subject subject4 = SubjectFinder.findByIdAndSource("test.subject.4", "jdbc", true);
    Subject subject5 = SubjectFinder.findByIdAndSource("test.subject.5", "jdbc", true);
    Subject subject6 = SubjectFinder.findByIdAndSource("test.subject.6", "jdbc", true);

    //assign a user to a role
    payrollUser.addMember(subject0, false);
    payrollGuest.addMember(subject1, false);
    //subject 3 and 4 are on the up and up
    payrollUser.addMember(subject3, false);
    payrollGuest.addMember(subject4, false);
    //subject 5 and 6 have disabled dates, shouldnt be changed
    payrollUser.addMember(subject5, false);
    payrollGuest.addMember(subject6, false);
    
    //create a permission, assign to role
    AttributeDefName canLogin = new AttributeDefNameSave(grouperSession, permissionDef).assignName("apps:payroll:permissions:canLogin").assignCreateParentStemsIfNotExist(true).save();
    
    payrollUser.getPermissionRoleDelegate().assignRolePermission(canLogin);
    
    //assign the permission to another user directly, not due to a role
    payrollGuest.getPermissionRoleDelegate().assignSubjectRolePermission(canLogin, subject1);

    payrollGuest.getPermissionRoleDelegate().assignSubjectRolePermission(canLogin, subject4);
    
    AttributeAssign attributeAssign = payrollGuest.getPermissionRoleDelegate().assignSubjectRolePermission(canLogin, subject6).getAttributeAssign();
    
    attributeAssign.setDisabledTime(new Timestamp(System.currentTimeMillis() + (3 * 24 * 60 * 60 * 1000)));
    attributeAssign.saveOrUpdate(true);
    
    //see that they both have the permission
    Member member0 = MemberFinder.findBySubject(grouperSession, subject0, false);
    Member member1 = MemberFinder.findBySubject(grouperSession, subject1, false);
    Member member2 = MemberFinder.findBySubject(grouperSession, subject2, true);
    Member member3 = MemberFinder.findBySubject(grouperSession, subject3, false);
    Member member4 = MemberFinder.findBySubject(grouperSession, subject4, false);
    Member member5 = MemberFinder.findBySubject(grouperSession, subject5, false);
    Member member6 = MemberFinder.findBySubject(grouperSession, subject6, false);

    Membership membership = ((Group)payrollUser).getImmediateMembership(Group.getDefaultList(), member5, true, true);
    membership.setDisabledTime(new Timestamp(System.currentTimeMillis() + (3 * 24 * 60 * 60 * 1000)));
    GrouperDAOFactory.getFactory().getMembership().update(membership);
    

    Set<PermissionEntry> permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member0.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member1.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
  
    RuleApi.permissionGroupIntersection(SubjectFinder.findRootSubject(), permissionDef, groupEmployee, 7);
  
    groupEmployee.addMember(subject2);
    groupEmployee.addMember(subject3);
    groupEmployee.addMember(subject4);
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_grouperRules");
    
    membership = ((Group)payrollUser).getImmediateMembership(Group.getDefaultList(), member5, true, true);
    
    membership.setDisabledTime(new Timestamp(System.currentTimeMillis() + (3 * 24 * 60 * 60 * 1000)));

    GrouperDAOFactory.getFactory().getMembership().update(membership);
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_grouperRules");
    
    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;

    //run the daemon
    String status = GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);

    assertTrue(status.toLowerCase().contains("success"));
    
    assertEquals(initialFirings+2, RuleEngine.ruleFirings);
    
    //should come out of groupA in 7 days
    membership = ((Group)payrollUser).getImmediateMembership(Group.getDefaultList(), member0, true, true);
    
    assertNotNull(membership.getDisabledTime());
    long disabledTime = membership.getDisabledTime().getTime();
    
    assertTrue("More than 6 days: " + new Date(disabledTime), disabledTime > System.currentTimeMillis() + (6 * 24 * 60 * 60 * 1000));
    assertTrue("Less than 8 days: " + new Date(disabledTime), disabledTime < System.currentTimeMillis() + (8 * 24 * 60 * 60 * 1000));

    membership = ((Group)payrollUser).getImmediateMembership(Group.getDefaultList(), member3, true, true);
    
    assertNull(membership.getDisabledTime());

    //member 5 already had a disabled date
    membership = ((Group)payrollUser).getImmediateMembership(Group.getDefaultList(), member5, true, true);
    
    assertNotNull(membership.getDisabledTime());
    disabledTime = membership.getDisabledTime().getTime();

    assertTrue("More than 2 days: " + new Date(disabledTime), disabledTime > System.currentTimeMillis() + (2 * 24 * 60 * 60 * 1000));
    assertTrue("Less than 4 days: " + new Date(disabledTime), disabledTime < System.currentTimeMillis() + (4 * 24 * 60 * 60 * 1000));
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member3.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member4.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
    
    assertTrue(payrollUser.hasMember(subject0));
    assertTrue(payrollGuest.hasMember(subject1));
    assertTrue(payrollUser.hasMember(subject3));
    assertTrue(payrollGuest.hasMember(subject4));
    assertTrue(payrollUser.hasMember(subject5));
    assertTrue(payrollGuest.hasMember(subject6));
  
    //should not care about guest disabled date
    membership = ((Group)payrollGuest).getImmediateMembership(Group.getDefaultList(), member1, true, true);
    assertNull(membership.getDisabledTime());

    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member0.getUuid());
    assertEquals(1, permissions.size());

    Timestamp timestamp = permissions.iterator().next().getImmediateMshipDisabledTime();
    
    disabledTime = timestamp.getTime();
    
    assertTrue("More than 6 days: " + new Date(disabledTime), disabledTime > System.currentTimeMillis() + (6 * 24 * 60 * 60 * 1000));
    assertTrue("Less than 8 days: " + new Date(disabledTime), disabledTime < System.currentTimeMillis() + (8 * 24 * 60 * 60 * 1000));

    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member1.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
  
    timestamp = permissions.iterator().next().getDisabledTime();
  
    disabledTime = timestamp.getTime();
    
    assertTrue("More than 6 days: " + new Date(disabledTime), disabledTime > System.currentTimeMillis() + (6 * 24 * 60 * 60 * 1000));
    assertTrue("Less than 8 days: " + new Date(disabledTime), disabledTime < System.currentTimeMillis() + (8 * 24 * 60 * 60 * 1000));

    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member2.getUuid());
    assertEquals(0, permissions.size());
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member3.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());

    timestamp = permissions.iterator().next().getDisabledTime();
    
    assertNull(timestamp);

    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member4.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());

    timestamp = permissions.iterator().next().getDisabledTime();
    
    assertNull(timestamp);

    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member5.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());

    timestamp = permissions.iterator().next().getImmediateMshipDisabledTime();
    
    disabledTime = timestamp.getTime();
    
    assertTrue("More than 2 days: " + new Date(disabledTime), disabledTime > System.currentTimeMillis() + (2 * 24 * 60 * 60 * 1000));
    assertTrue("Less than 4 days: " + new Date(disabledTime), disabledTime < System.currentTimeMillis() + (4 * 24 * 60 * 60 * 1000));

    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member6.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
  
    timestamp = permissions.iterator().next().getDisabledTime();
  
    disabledTime = timestamp.getTime();
    
    assertTrue("More than 2 days: " + new Date(disabledTime), disabledTime > System.currentTimeMillis() + (2 * 24 * 60 * 60 * 1000));
    assertTrue("Less than 4 days: " + new Date(disabledTime), disabledTime < System.currentTimeMillis() + (4 * 24 * 60 * 60 * 1000));


  }

  /**
   * 
   */
  public void testPermissionAssignmentIntersectFolderDaemon() {
  
    GrouperSession grouperSession = GrouperSession.startRootSession();
    AttributeDef permissionDef = new AttributeDefSave(grouperSession)
      .assignName("stem:permissionDef").assignCreateParentStemsIfNotExist(true)
      .assignAttributeDefType(AttributeDefType.perm)
      .save();
    
    permissionDef.setAssignToEffMembership(true);
    permissionDef.setAssignToGroup(true);
    permissionDef.store();
    
    Group groupProgrammers = new GroupSave(grouperSession).assignName("stem:orgs:itEmployee:programmers").assignCreateParentStemsIfNotExist(true).save();
    Group groupProgrammersSub = new GroupSave(grouperSession).assignName("stem2:programmerssub").assignCreateParentStemsIfNotExist(true).save();
    Group groupSysadmins = new GroupSave(grouperSession).assignName("stem:orgs:itEmployee:sysadmins").assignCreateParentStemsIfNotExist(true).save();
    groupProgrammers.addMember(groupProgrammersSub.toSubject());
    
    Stem itEmployee = StemFinder.findByName(grouperSession, "stem:orgs:itEmployee", true);
    
    //make a role
    Role payrollUser = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollUser").assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();
    Role payrollGuest = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollGuest").assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();
  
    //assign a user to a role
    
    Subject subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Subject subject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);

    //subject2 is ok
    Subject subject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    Subject subject3 = SubjectFinder.findByIdAndSource("test.subject.3", "jdbc", true);
    groupProgrammers.addMember(subject2, false);
    groupSysadmins.addMember(subject3, false);
    
    Subject subject4 = SubjectFinder.findByIdAndSource("test.subject.4", "jdbc", true);
    Subject subject5 = SubjectFinder.findByIdAndSource("test.subject.5", "jdbc", true);
    groupProgrammersSub.addMember(subject4, false);
    groupProgrammersSub.addMember(subject5, false);

  
    payrollUser.addMember(subject0, false);
    payrollGuest.addMember(subject1, false);
    payrollUser.addMember(subject2, false);
    payrollGuest.addMember(subject3, false);
    payrollUser.addMember(subject4, false);
    payrollGuest.addMember(subject5, false);
    
    //create a permission, assign to role
    AttributeDefName canLogin = new AttributeDefNameSave(grouperSession, permissionDef).assignName("apps:payroll:permissions:canLogin").assignCreateParentStemsIfNotExist(true).save();
    
    payrollUser.getPermissionRoleDelegate().assignRolePermission(canLogin);
    
    //assign the permission to another user directly, not due to a role
    payrollGuest.getPermissionRoleDelegate().assignSubjectRolePermission(canLogin, subject1);
    payrollGuest.getPermissionRoleDelegate().assignSubjectRolePermission(canLogin, subject3);
    payrollGuest.getPermissionRoleDelegate().assignSubjectRolePermission(canLogin, subject5);
    
    //see that they both have the permission
    Member member0 = MemberFinder.findBySubject(grouperSession, subject0, false);
    Member member1 = MemberFinder.findBySubject(grouperSession, subject1, false);
    Member member2 = MemberFinder.findBySubject(grouperSession, subject2, false);
    Member member3 = MemberFinder.findBySubject(grouperSession, subject3, false);
    Member member4 = MemberFinder.findBySubject(grouperSession, subject4, false);
    Member member5 = MemberFinder.findBySubject(grouperSession, subject5, false);
    
    Set<PermissionEntry> permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member0.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member1.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
    
    RuleApi.permissionFolderIntersection(SubjectFinder.findRootSubject(), permissionDef, itEmployee, Stem.Scope.SUB);
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_consumer_grouperRules");
    
    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;

    //run the daemon
    String status = GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);

    assertTrue(status.toLowerCase().contains("success"));
  
    assertEquals(initialFirings + 2, RuleEngine.ruleFirings);
      
    assertFalse(payrollUser.hasMember(subject0));
    assertTrue(payrollUser.hasMember(subject2));
    assertTrue(payrollUser.hasMember(subject4));
  
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member0.getUuid());
    assertEquals(0, permissions.size());
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member1.getUuid());
    assertEquals(0, permissions.size());
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member2.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member3.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());

    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member4.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member5.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
    
    assertTrue(payrollGuest.hasMember(subject1));
    assertTrue(payrollGuest.hasMember(subject3));
    assertTrue(payrollGuest.hasMember(subject5));
  
  }

  /**
   * 
   */
  public void testRuleVetoDaemon() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group ruleGroup = new GroupSave(grouperSession).assignName("stem:a").assignCreateParentStemsIfNotExist(true).save();
    Group mustBeInGroup = new GroupSave(grouperSession).assignName("stem:b").assignCreateParentStemsIfNotExist(true).save();
    Subject actAsSubject = SubjectFinder.findByIdAndSource("GrouperSystem", "g:isa", true);

    Subject subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Subject subject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    
    //add these before the rule
    ruleGroup.addMember(subject0);
    ruleGroup.addMember(subject1);
    mustBeInGroup.addMember(subject1);
    
    RuleApi.vetoMembershipIfNotInGroup(actAsSubject, ruleGroup, mustBeInGroup, 
        "rule.entity.must.be.a.member.of.stem.b", "Entity cannot be a member of stem:a if not a member of stem:b");
    
    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;
  
    //run the daemon
    String status = GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);

    assertTrue(status.toLowerCase().contains("success"));
  
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
  
    assertFalse(ruleGroup.hasMember(subject0));
    assertTrue(ruleGroup.hasMember(subject1));
  
  }

  /**
   * 
   */
  public void testRuleVetoInOrgDaemon() {

    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group ruleGroup = new GroupSave(grouperSession).assignName("stem:a").assignCreateParentStemsIfNotExist(true).save();

    Group groupProgrammers = new GroupSave(grouperSession).assignName("stem:orgs:itEmployee:programmers").assignCreateParentStemsIfNotExist(true).save();
    
    Stem mustBeInStem = StemFinder.findByName(grouperSession, "stem:orgs:itEmployee", true);

    Subject subject0 = SubjectFinder.findById("test.subject.0", true);
    Subject subject1 = SubjectFinder.findById("test.subject.1", true);

    ruleGroup.addMember(subject0);
    ruleGroup.addMember(subject1);
    groupProgrammers.addMember(subject1);

    RuleApi.vetoMembershipIfNotInGroupInFolder(SubjectFinder.findRootSubject(), ruleGroup, mustBeInStem, Stem.Scope.SUB, 
        "rule.entity.must.be.in.IT.employee.to.be.in.group", "Entity cannot be a member of group if not in the IT department org");
    
    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;
    
    //run the daemon
    String status = GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);

    assertTrue(status.toLowerCase().contains("success"));
  
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);

    assertFalse(ruleGroup.hasMember(subject0));
    assertTrue(ruleGroup.hasMember(subject1));

  }

  /**
   * 
   */
  public void testRuleVetoPermissionsDaemon() {

    GrouperSession grouperSession = GrouperSession.startRootSession();
    AttributeDef permissionDef = new AttributeDefSave(grouperSession).assignName("stem:permissionDef").assignCreateParentStemsIfNotExist(true).assignAttributeDefType(AttributeDefType.perm).save();
    
    permissionDef.setAssignToEffMembership(true);
    permissionDef.setAssignToGroup(true);
    permissionDef.store();
    
    Group groupEmployee = new GroupSave(grouperSession).assignName("stem:employee").assignCreateParentStemsIfNotExist(true).save();

    //make a role
    Role payrollUser = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollUser").assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();
    Role payrollGuest = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollGuest").assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();

    Subject subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    Subject subject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Subject subject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    Subject subject3 = SubjectFinder.findByIdAndSource("test.subject.3", "jdbc", true);

    //assign a user to a role
    payrollUser.addMember(subject0, false);
    payrollGuest.addMember(subject1, false);
    payrollUser.addMember(subject2, false);
    payrollGuest.addMember(subject3, false);

    //create a permission, assign to role
    AttributeDefName canLogin = new AttributeDefNameSave(grouperSession, permissionDef).assignName("apps:payroll:permissions:canLogin").assignCreateParentStemsIfNotExist(true).save();

    //assign the permission to another user directly, not due to a role
    payrollUser.getPermissionRoleDelegate().assignRolePermission(canLogin);
    payrollGuest.getPermissionRoleDelegate().assignSubjectRolePermission(canLogin, subject1);
    payrollGuest.getPermissionRoleDelegate().assignSubjectRolePermission(canLogin, subject3);

    //these are legit
    groupEmployee.addMember(subject2);
    groupEmployee.addMember(subject3);
    
    RuleApi.vetoPermissionIfNotInGroup(SubjectFinder.findRootSubject(), permissionDef, groupEmployee, "rule.entity.must.be.an.employee", "Entity cannot be assigned these permissions unless they are an employee");

    //count rule firings
    long initialFirings = RuleEngine.ruleFirings;
    
    //run the daemon
    String status = GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);

    assertTrue(status.toLowerCase().contains("success"));


    assertEquals(initialFirings+2, RuleEngine.ruleFirings);

    //see that not have the permission
    Member member0 = MemberFinder.findBySubject(grouperSession, subject0, false);
    Member member1 = MemberFinder.findBySubject(grouperSession, subject1, false);
    Member member2 = MemberFinder.findBySubject(grouperSession, subject2, false);
    Member member3 = MemberFinder.findBySubject(grouperSession, subject3, false);
    
    Set<PermissionEntry> permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member0.getUuid());
    assertEquals(0, permissions.size());
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member1.getUuid());
    assertEquals(0, permissions.size());
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member2.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member3.getUuid());
    assertEquals(1, permissions.size());
    assertEquals("apps:payroll:permissions:canLogin", permissions.iterator().next().getAttributeDefNameName());
    
    assertFalse(payrollUser.hasMember(subject0));
    assertTrue(payrollGuest.hasMember(subject1));

  }

  /**
   * 
   */
  public void testInheritGroupPrivilegesPattern() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem2 = new StemSave(grouperSession).assignName("stem2").assignCreateParentStemsIfNotExist(true).save();
  
    Group groupA = new GroupSave(grouperSession).assignName("stem1:admins").assignCreateParentStemsIfNotExist(true).save();
  
    Subject subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    groupA.addMember(subject0);
    
    RuleApi.inheritGroupPrivileges(SubjectFinder.findRootSubject(), stem2, Scope.SUB, groupA.toSubject(), Privilege.getInstances("read, update"), "stem2:%someGroup");
  
    long initialFirings = RuleEngine.ruleFirings;
    
    Group groupB = new GroupSave(grouperSession).assignName("stem2:b").assignCreateParentStemsIfNotExist(true).save();
  
    //count rule firings
    assertEquals(initialFirings, RuleEngine.ruleFirings);
  
    //make sure not allowed
    assertFalse(groupB.hasUpdate(subject0));
    assertFalse(groupB.hasRead(subject0));
    
    //allowed
    Group someGroup = new GroupSave(grouperSession).assignName("stem2:b:w_someGroup").assignCreateParentStemsIfNotExist(true).save();
    
    //count rule firings
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
  
    //make sure allowed
    assertTrue(someGroup.hasUpdate(subject0));
    assertTrue(someGroup.hasRead(subject0));
    
    
    Group groupD = new GroupSave(grouperSession).assignName("stem3:d").assignCreateParentStemsIfNotExist(true).save();
  
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
    
    assertFalse(groupD.hasUpdate(subject0));
    assertFalse(groupD.hasRead(subject0));
    
    
    Group groupC = new GroupSave(grouperSession).assignName("stem2:sub:c").assignCreateParentStemsIfNotExist(true).save();
  
    assertEquals(initialFirings+1, RuleEngine.ruleFirings);
  
    assertFalse(groupC.hasRead(subject0));
    assertFalse(groupC.hasUpdate(subject0));
  
  }

  /**
   * 
   */
  public void testInheritGroupPrivilegesPatternDaemon() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem2 = new StemSave(grouperSession).assignName("stem2").assignCreateParentStemsIfNotExist(true).save();
  
    Group groupA = new GroupSave(grouperSession).assignName("stem1:admins").assignCreateParentStemsIfNotExist(true).save();
  
    Subject subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);
    groupA.addMember(subject0);
    
    Group groupB = new GroupSave(grouperSession).assignName("stem2:b").assignCreateParentStemsIfNotExist(true).save();
    //allowed
    Group someGroup = new GroupSave(grouperSession).assignName("stem2:b:w_someGroup").assignCreateParentStemsIfNotExist(true).save();
    
    Group groupD = new GroupSave(grouperSession).assignName("stem3:d").assignCreateParentStemsIfNotExist(true).save();
    
    assertFalse(someGroup.hasUpdate(subject0));
    assertFalse(someGroup.hasRead(subject0));
  
    Group groupC = new GroupSave(grouperSession).assignName("stem2:sub:c").assignCreateParentStemsIfNotExist(true).save();
    

    RuleApi.inheritGroupPrivileges(SubjectFinder.findRootSubject(), stem2, Scope.SUB, groupA.toSubject(), Privilege.getInstances("read, update"), "stem2:%someGroup");
    
    long initialFirings = RuleEngine.ruleFirings;
    
    //run the daemon
    String status = GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);

    assertTrue(status.toLowerCase().contains("success"));
    
    //count rule firings, one for the one group for two privs
    assertEquals(initialFirings+2, RuleEngine.ruleFirings);

    //make sure not allowed
    assertFalse(groupB.hasUpdate(subject0));
    assertFalse(groupB.hasRead(subject0));
  
    //make sure allowed
    assertTrue(someGroup.hasUpdate(subject0));
    assertTrue(someGroup.hasRead(subject0));
    
    assertFalse(groupD.hasUpdate(subject0));
    assertFalse(groupD.hasRead(subject0));
  
    assertFalse(groupC.hasRead(subject0));
    assertFalse(groupC.hasUpdate(subject0));
  
    //run the daemon
    status = GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_RULES);

    assertTrue(status.toLowerCase().contains("success"));
    
    //not much changed
    assertEquals(initialFirings+2, RuleEngine.ruleFirings);

    //make sure not allowed
    assertFalse(groupB.hasUpdate(subject0));
    assertFalse(groupB.hasRead(subject0));
  
    //make sure allowed
    assertTrue(someGroup.hasUpdate(subject0));
    assertTrue(someGroup.hasRead(subject0));
    
    assertFalse(groupD.hasUpdate(subject0));
    assertFalse(groupD.hasRead(subject0));
  
    assertFalse(groupC.hasRead(subject0));
    assertFalse(groupC.hasUpdate(subject0));
  
  }

  /**
   * 
   */
  public void testNoNeedForWheelOrRootPrivileges() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
  
    Group groupA = new GroupSave(grouperSession).assignName("stem1:admins").assignCreateParentStemsIfNotExist(true).save();
    
    assertFalse(SubjectHelper.inList(groupA.getAdmins(), SubjectFinder.findRootSubject()));
    
    Stem stem2 = new StemSave(grouperSession).assignName("stem2").assignCreateParentStemsIfNotExist(true).save();
    
    assertFalse(SubjectHelper.inList(stem2.getStemAdmins(), SubjectFinder.findRootSubject()));
    
    @SuppressWarnings("unused")
    AttributeDef attributeDef = new AttributeDefSave(grouperSession).assignName("stem1:attributeDef").assignCreateParentStemsIfNotExist(true).save();
    
    assertNull(new MembershipFinder().addAttributeDef(attributeDef).assignMembershipType(MembershipType.IMMEDIATE).addPrivilegeTheUserHas(AttributeDefPrivilege.ATTR_ADMIN).addSubject(SubjectFinder.findRootSubject()).findMembership(false));
    
  }

  /**
   * 
   */
  public void testNoNeedForInheritedAdminPrivileges() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
  
    Stem mainStem = new StemSave(grouperSession).assignName("stem").assignCreateParentStemsIfNotExist(true).save();
    mainStem.grantPriv(SubjectTestHelper.SUBJ3, NamingPrivilege.CREATE);

    Group groupAdmins = new GroupSave(grouperSession).assignName("stemX:admins").assignCreateParentStemsIfNotExist(true).save();
    
    groupAdmins.addMember(SubjectTestHelper.SUBJ0);
    groupAdmins.addMember(SubjectTestHelper.SUBJ1);

    // stem2 inherits SUB to groupA read/update
    RuleApi.inheritGroupPrivileges(SubjectFinder.findRootSubject(), mainStem, Scope.SUB, groupAdmins.toSubject(), Privilege.getInstances("admin"));
    RuleApi.inheritFolderPrivileges(SubjectFinder.findRootSubject(), mainStem, Scope.SUB, groupAdmins.toSubject(), Privilege.getInstances("stemAdmin"));
    RuleApi.inheritAttributeDefPrivileges(SubjectFinder.findRootSubject(), mainStem, Scope.SUB, groupAdmins.toSubject(), Privilege.getInstances("attrAdmin"));
    
    RuleApi.runRulesForOwner(mainStem);

    GrouperSession.stopQuietly(grouperSession);

    //NOT INHERITED
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ3);
    
    Stem stem1 = new StemSave(grouperSession).assignName("stem:stem1").assignCreateParentStemsIfNotExist(true).save();
    stem1.grantPriv(SubjectTestHelper.SUBJ3, NamingPrivilege.CREATE);
    Group groupA = new GroupSave(grouperSession).assignName("stem:stem1:admins").assignCreateParentStemsIfNotExist(true).save();
    
    assertTrue(SubjectHelper.inList(groupA.getAdmins(), SubjectTestHelper.SUBJ3));
    
    Stem stem2 = new StemSave(grouperSession).assignName("stem:stem2").assignCreateParentStemsIfNotExist(true).save();
    
    assertTrue(SubjectHelper.inList(stem2.getStemAdmins(), SubjectTestHelper.SUBJ3));
    
    AttributeDef attributeDef = new AttributeDefSave(grouperSession).assignName("stem:stem1:attributeDef").assignCreateParentStemsIfNotExist(true).save();
    
    // doesnt currently work
    assertNotNull(new MembershipFinder().addAttributeDef(attributeDef).addPrivilegeTheUserHas(AttributeDefPrivilege.ATTR_ADMIN).addSubject(SubjectTestHelper.SUBJ3).findMembership(false));


    GrouperSession.stopQuietly(grouperSession);

    //YES INHERITED
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    groupA = new GroupSave(grouperSession).assignName("stem:stem1:admins2").assignCreateParentStemsIfNotExist(true).save();
    
    assertNull(new MembershipFinder().addGroup(groupA).addPrivilegeTheUserHas(AccessPrivilege.ADMIN).addSubject(SubjectTestHelper.SUBJ0).assignMembershipType(MembershipType.IMMEDIATE).findMembership(false));
    
    stem2 = new StemSave(grouperSession).assignName("stem:stem2a").assignCreateParentStemsIfNotExist(true).save();
    
    assertNull(new MembershipFinder().addStem(stem2).addPrivilegeTheUserHas(NamingPrivilege.STEM_ADMIN).addSubject(SubjectTestHelper.SUBJ0).assignMembershipType(MembershipType.IMMEDIATE).findMembership(false));
    
    attributeDef = new AttributeDefSave(grouperSession).assignName("stem:stem1:attributeDef2").assignCreateParentStemsIfNotExist(true).save();
    
    // doesnt currently work
    assertNull(new MembershipFinder().addAttributeDef(attributeDef).addPrivilegeTheUserHas(AttributeDefPrivilege.ATTR_ADMIN).assignMembershipType(MembershipType.IMMEDIATE).addSubject(SubjectTestHelper.SUBJ0).findMembership(false));
    
  }

}
