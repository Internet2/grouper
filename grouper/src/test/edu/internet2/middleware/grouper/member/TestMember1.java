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

package edu.internet2.middleware.grouper.member;
import java.util.Iterator;
import java.util.Set;

import junit.textui.TestRunner;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * Test {@link Member}.
 * <p />
 * @author  blair christensen.
 * @version $Id$
 */
public class TestMember1 extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new TestMember1("testStemGroupPrivs"));
  }
  
  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(TestMember1.class);

  /**
   * 
   * @param name
   */
  public TestMember1(String name) {
    super(name);
  }

  /** allowed prefixes for this test */
  private static Set<String> allowedPrefixes = GrouperUtil.toSet("stem", "stem2", "parent");
  
  /**
   * filter groups for this test
   * @param groups
   * @return the same list for chaining
   */
  public static Set<Group> filterGroups(Set<Group> groups) {
    if (groups == null) {
      return null;
    }
    Iterator<Group> iterator = groups.iterator();
    OUTER: while (iterator.hasNext()) {
      Group group = iterator.next();
      for (String prefix : allowedPrefixes) {
        if (group.getName().startsWith(prefix+":")) {
          continue OUTER;
        }
      }
      //another group, dont worry about it
      iterator.remove();
    }
    return groups;
  }
  
  /**
   * filter stems for this test
   * @param stems
   * @return the same list for chaining
   */
  public static Set<Stem> filterStems(Set<Stem> stems) {
    if (stems == null) {
      return null;
    }
    Iterator<Stem> iterator = stems.iterator();
    OUTER: while (iterator.hasNext()) {
      Stem stem = iterator.next();
      for (String prefix : allowedPrefixes) {
        if (stem.getName().startsWith(prefix)) {
          continue OUTER;
        }
      }
      //another group, dont worry about it
      iterator.remove();
    }
    return stems;
  }
  
  // Tests

  /**
   * 
   */
  public void testStemGroupPrivs() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Subject subject0 = SubjectTestHelper.SUBJ0;
    Member member0 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, true);
    Subject subject1 = SubjectTestHelper.SUBJ1;
    Member member1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
    Subject subject2 = SubjectTestHelper.SUBJ2;
    Member member2 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ2, true);
    Subject subject3 = SubjectTestHelper.SUBJ3;
    Member member3 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ3, true);
    Subject subject4 = SubjectTestHelper.SUBJ4;
    Member member4 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ4, true);
    Subject subject5 = SubjectTestHelper.SUBJ5;
    Member member5 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ5, true);
    Subject subject6 = SubjectTestHelper.SUBJ6;
    Member member6 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ6, true);
    Subject subject7 = SubjectTestHelper.SUBJ7;
    Member member7 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ7, true);
    
    Stem parent = new StemSave(grouperSession).assignName("parent").save();
    Stem stem = new StemSave(grouperSession).assignName("stem").save();
    @SuppressWarnings("unused")
    Stem stem2 = new StemSave(grouperSession).assignName("stem2").save();
    Stem child = new StemSave(grouperSession).assignName("parent:child").save();
    
    Group parentGroup0 = new GroupSave(grouperSession).assignName("parent:parentGroup0").save();
    Group parentGroup1 = new GroupSave(grouperSession).assignName("parent:parentGroup1").save();
    Group parentGroup2 = new GroupSave(grouperSession).assignName("parent:parentGroup2").save();
    Group parentGroup3 = new GroupSave(grouperSession).assignName("parent:parentGroup3").save();
    Group parentGroup4 = new GroupSave(grouperSession).assignName("parent:parentGroup4").save();
    Group parentGroup5 = new GroupSave(grouperSession).assignName("parent:parentGroup5").save();
    Group parentGroup6 = new GroupSave(grouperSession).assignName("parent:parentGroup6").save();
    Group parentGroup7 = new GroupSave(grouperSession).assignName("parent:parentGroup7").save();

    Group stemGroup0 = new GroupSave(grouperSession).assignName("stem:stemGroup0").save();
    Group stemGroup1 = new GroupSave(grouperSession).assignName("stem:stemGroup1").save();
    Group stemGroup2 = new GroupSave(grouperSession).assignName("stem:stemGroup2").save();
    Group stemGroup3 = new GroupSave(grouperSession).assignName("stem:stemGroup3").save();
    Group stemGroup4 = new GroupSave(grouperSession).assignName("stem:stemGroup4").save();
    Group stemGroup5 = new GroupSave(grouperSession).assignName("stem:stemGroup5").save();
    Group stemGroup6 = new GroupSave(grouperSession).assignName("stem:stemGroup6").save();
    Group stemGroup7 = new GroupSave(grouperSession).assignName("stem:stemGroup7").save();
    
    @SuppressWarnings("unused")
    Group stem2Group0 = new GroupSave(grouperSession).assignName("stem2:stemGroup0").save();
    @SuppressWarnings("unused")
    Group stem2Group1 = new GroupSave(grouperSession).assignName("stem2:stemGroup1").save();
    @SuppressWarnings("unused")
    Group stem2Group2 = new GroupSave(grouperSession).assignName("stem2:stemGroup2").save();
    @SuppressWarnings("unused")
    Group stem2Group3 = new GroupSave(grouperSession).assignName("stem2:stemGroup3").save();
    @SuppressWarnings("unused")
    Group stem2Group4 = new GroupSave(grouperSession).assignName("stem2:stemGroup4").save();
    @SuppressWarnings("unused")
    Group stem2Group5 = new GroupSave(grouperSession).assignName("stem2:stemGroup5").save();
    @SuppressWarnings("unused")
    Group stem2Group6 = new GroupSave(grouperSession).assignName("stem2:stemGroup6").save();
    @SuppressWarnings("unused")
    Group stem2Group7 = new GroupSave(grouperSession).assignName("stem2:stemGroup7").save();
    
    Group childGroup0 = new GroupSave(grouperSession).assignName("parent:child:childGroup0").save();
    Group childGroup1 = new GroupSave(grouperSession).assignName("parent:child:childGroup1").save();
    Group childGroup2 = new GroupSave(grouperSession).assignName("parent:child:childGroup2").save();
    Group childGroup3 = new GroupSave(grouperSession).assignName("parent:child:childGroup3").save();
    Group childGroup4 = new GroupSave(grouperSession).assignName("parent:child:childGroup4").save();
    Group childGroup5 = new GroupSave(grouperSession).assignName("parent:child:childGroup5").save();
    Group childGroup6 = new GroupSave(grouperSession).assignName("parent:child:childGroup6").save();
    Group childGroup7 = new GroupSave(grouperSession).assignName("parent:child:childGroup7").save();
    
    parentGroup0.grantPriv(subject0, AccessPrivilege.ADMIN, false);
    parentGroup1.grantPriv(subject1, AccessPrivilege.OPTIN, false);
    parentGroup2.grantPriv(subject2, AccessPrivilege.OPTOUT, false);
    parentGroup3.grantPriv(subject3, AccessPrivilege.READ, false);
    parentGroup4.grantPriv(subject4, AccessPrivilege.UPDATE, false);
    parentGroup5.grantPriv(subject5, AccessPrivilege.VIEW, false);
    parentGroup6.grantPriv(subject6, AccessPrivilege.GROUP_ATTR_READ, false);
    parentGroup7.grantPriv(subject7, AccessPrivilege.GROUP_ATTR_UPDATE, false);
    
    stemGroup0.grantPriv(subject0, AccessPrivilege.ADMIN, false);
    stemGroup1.grantPriv(subject1, AccessPrivilege.OPTIN, false);
    stemGroup2.grantPriv(subject2, AccessPrivilege.OPTOUT, false);
    stemGroup3.grantPriv(subject3, AccessPrivilege.READ, false);
    stemGroup4.grantPriv(subject4, AccessPrivilege.UPDATE, false);
    stemGroup5.grantPriv(subject5, AccessPrivilege.VIEW, false);
    stemGroup6.grantPriv(subject6, AccessPrivilege.GROUP_ATTR_READ, false);
    stemGroup7.grantPriv(subject7, AccessPrivilege.GROUP_ATTR_UPDATE, false);

    childGroup0.grantPriv(subject0, AccessPrivilege.ADMIN, false);
    childGroup1.grantPriv(subject1, AccessPrivilege.OPTIN, false);
    childGroup2.grantPriv(subject2, AccessPrivilege.OPTOUT, false);
    childGroup3.grantPriv(subject3, AccessPrivilege.READ, false);
    childGroup4.grantPriv(subject4, AccessPrivilege.UPDATE, false);
    childGroup5.grantPriv(subject5, AccessPrivilege.VIEW, false);
    childGroup6.grantPriv(subject6, AccessPrivilege.GROUP_ATTR_READ, false);
    childGroup7.grantPriv(subject7, AccessPrivilege.GROUP_ATTR_UPDATE, false);

    assertEquals(3, filterGroups(member0.hasAdmin()).size());
    assertEquals(0, filterGroups(member0.hasOptin()).size());
    assertEquals(0, filterGroups(member0.hasOptout()).size());
    assertEquals(0, filterGroups(member0.hasRead()).size());
    assertEquals(0, filterGroups(member0.hasUpdate()).size());
    assertEquals(0, filterGroups(member0.hasView()).size());
    assertEquals(0, filterGroups(member0.hasGroupAttrRead()).size());
    assertEquals(0, filterGroups(member0.hasGroupAttrUpdate()).size());
    assertTrue(member0.hasAdmin(parentGroup0));
    assertTrue(member0.hasAdmin(stemGroup0));
    assertTrue(member0.hasAdmin(childGroup0));

    assertEquals(0, filterGroups(member1.hasAdmin()).size());
    assertEquals(3, filterGroups(member1.hasOptin()).size());
    assertEquals(0, filterGroups(member1.hasOptout()).size());
    assertEquals(0, filterGroups(member1.hasRead()).size());
    assertEquals(0, filterGroups(member1.hasUpdate()).size());
    assertEquals(0, filterGroups(member1.hasView()).size());
    assertEquals(0, filterGroups(member1.hasGroupAttrRead()).size());
    assertEquals(0, filterGroups(member1.hasGroupAttrUpdate()).size());
    assertTrue(member1.hasOptin(parentGroup1));
    assertTrue(member1.hasOptin(stemGroup1));
    assertTrue(member1.hasOptin(childGroup1));
    
    assertEquals(0, filterGroups(member2.hasAdmin()).size());
    assertEquals(0, filterGroups(member2.hasOptin()).size());
    assertEquals(3, filterGroups(member2.hasOptout()).size());
    assertEquals(0, filterGroups(member2.hasRead()).size());
    assertEquals(0, filterGroups(member2.hasUpdate()).size());
    assertEquals(0, filterGroups(member2.hasView()).size());
    assertEquals(0, filterGroups(member2.hasGroupAttrRead()).size());
    assertEquals(0, filterGroups(member2.hasGroupAttrUpdate()).size());
    assertTrue(member2.hasOptout(parentGroup2));
    assertTrue(member2.hasOptout(stemGroup2));
    assertTrue(member2.hasOptout(childGroup2));

    assertEquals(0, filterGroups(member3.hasAdmin()).size());
    assertEquals(0, filterGroups(member3.hasOptin()).size());
    assertEquals(0, filterGroups(member3.hasOptout()).size());
    assertEquals(3, filterGroups(member3.hasRead()).size());
    assertEquals(0, filterGroups(member3.hasUpdate()).size());
    assertEquals(0, filterGroups(member3.hasView()).size());
    assertEquals(0, filterGroups(member3.hasGroupAttrRead()).size());
    assertEquals(0, filterGroups(member3.hasGroupAttrUpdate()).size());
    assertTrue(member3.hasRead(parentGroup3));
    assertTrue(member3.hasRead(stemGroup3));
    assertTrue(member3.hasRead(childGroup3));

    assertEquals(0, filterGroups(member4.hasAdmin()).size());
    assertEquals(0, filterGroups(member4.hasOptin()).size());
    assertEquals(0, filterGroups(member4.hasOptout()).size());
    assertEquals(0, filterGroups(member4.hasRead()).size());
    assertEquals(3, filterGroups(member4.hasUpdate()).size());
    assertEquals(0, filterGroups(member4.hasView()).size());
    assertEquals(0, filterGroups(member4.hasGroupAttrRead()).size());
    assertEquals(0, filterGroups(member4.hasGroupAttrUpdate()).size());
    assertTrue(member4.hasUpdate(parentGroup4));
    assertTrue(member4.hasUpdate(stemGroup4));
    assertTrue(member4.hasUpdate(childGroup4));

    assertEquals(0, filterGroups(member5.hasAdmin()).size());
    assertEquals(0, filterGroups(member5.hasOptin()).size());
    assertEquals(0, filterGroups(member5.hasOptout()).size());
    assertEquals(0, filterGroups(member5.hasRead()).size());
    assertEquals(0, filterGroups(member5.hasUpdate()).size());
    assertEquals(3, filterGroups(member5.hasView()).size());
    assertEquals(0, filterGroups(member5.hasGroupAttrRead()).size());
    assertEquals(0, filterGroups(member5.hasGroupAttrUpdate()).size());
    assertTrue(member5.hasView(parentGroup5));
    assertTrue(member5.hasView(stemGroup5));
    assertTrue(member5.hasView(childGroup5));
    
    assertEquals(0, filterGroups(member6.hasAdmin()).size());
    assertEquals(0, filterGroups(member6.hasOptin()).size());
    assertEquals(0, filterGroups(member6.hasOptout()).size());
    assertEquals(0, filterGroups(member6.hasRead()).size());
    assertEquals(0, filterGroups(member6.hasUpdate()).size());
    assertEquals(0, filterGroups(member6.hasView()).size());
    assertEquals(3, filterGroups(member6.hasGroupAttrRead()).size());
    assertEquals(0, filterGroups(member6.hasGroupAttrUpdate()).size());
    assertTrue(member6.hasGroupAttrRead(parentGroup6));
    assertTrue(member6.hasGroupAttrRead(stemGroup6));
    assertTrue(member6.hasGroupAttrRead(childGroup6));
    
    assertEquals(0, filterGroups(member7.hasAdmin()).size());
    assertEquals(0, filterGroups(member7.hasOptin()).size());
    assertEquals(0, filterGroups(member7.hasOptout()).size());
    assertEquals(0, filterGroups(member7.hasRead()).size());
    assertEquals(0, filterGroups(member7.hasUpdate()).size());
    assertEquals(0, filterGroups(member7.hasView()).size());
    assertEquals(0, filterGroups(member7.hasGroupAttrRead()).size());
    assertEquals(3, filterGroups(member7.hasGroupAttrUpdate()).size());
    assertTrue(member7.hasGroupAttrUpdate(parentGroup7));
    assertTrue(member7.hasGroupAttrUpdate(stemGroup7));
    assertTrue(member7.hasGroupAttrUpdate(childGroup7));

    assertEquals(3, filterStems(member0.hasAdminInStem()).size());
    assertEquals(0, filterStems(member0.hasOptinInStem()).size());
    assertEquals(0, filterStems(member0.hasOptoutInStem()).size());
    assertEquals(0, filterStems(member0.hasReadInStem()).size());
    assertEquals(0, filterStems(member0.hasUpdateInStem()).size());
    assertEquals(0, filterStems(member0.hasViewInStem()).size());
    assertEquals(0, filterStems(member0.hasGroupAttrReadInStem()).size());
    assertEquals(0, filterStems(member0.hasGroupAttrUpdateInStem()).size());
    assertTrue(member0.hasAdminInStem().contains(parent));
    assertTrue(member0.hasAdminInStem().contains(child));
    assertTrue(member0.hasAdminInStem().contains(stem));
    
    assertEquals(0, filterStems(member1.hasAdminInStem()).size());
    assertEquals(3, filterStems(member1.hasOptinInStem()).size());
    assertEquals(0, filterStems(member1.hasOptoutInStem()).size());
    assertEquals(0, filterStems(member1.hasReadInStem()).size());
    assertEquals(0, filterStems(member1.hasUpdateInStem()).size());
    assertEquals(0, filterStems(member1.hasViewInStem()).size());
    assertEquals(0, filterStems(member1.hasGroupAttrReadInStem()).size());
    assertEquals(0, filterStems(member1.hasGroupAttrUpdateInStem()).size());
    assertTrue(member1.hasOptinInStem().contains(parent));
    assertTrue(member1.hasOptinInStem().contains(child));
    assertTrue(member1.hasOptinInStem().contains(stem));
    
    assertEquals(0, filterStems(member2.hasAdminInStem()).size());
    assertEquals(0, filterStems(member2.hasOptinInStem()).size());
    assertEquals(3, filterStems(member2.hasOptoutInStem()).size());
    assertEquals(0, filterStems(member2.hasReadInStem()).size());
    assertEquals(0, filterStems(member2.hasUpdateInStem()).size());
    assertEquals(0, filterStems(member2.hasViewInStem()).size());
    assertEquals(0, filterStems(member2.hasGroupAttrReadInStem()).size());
    assertEquals(0, filterStems(member2.hasGroupAttrUpdateInStem()).size());
    assertTrue(member2.hasOptoutInStem().contains(parent));
    assertTrue(member2.hasOptoutInStem().contains(child));
    assertTrue(member2.hasOptoutInStem().contains(stem));
    
    assertEquals(0, filterStems(member3.hasAdminInStem()).size());
    assertEquals(0, filterStems(member3.hasOptinInStem()).size());
    assertEquals(0, filterStems(member3.hasOptoutInStem()).size());
    assertEquals(3, filterStems(member3.hasReadInStem()).size());
    assertEquals(0, filterStems(member3.hasUpdateInStem()).size());
    assertEquals(0, filterStems(member3.hasViewInStem()).size());
    assertEquals(0, filterStems(member3.hasGroupAttrReadInStem()).size());
    assertEquals(0, filterStems(member3.hasGroupAttrUpdateInStem()).size());
    assertTrue(member3.hasReadInStem().contains(parent));
    assertTrue(member3.hasReadInStem().contains(child));
    assertTrue(member3.hasReadInStem().contains(stem));
    
    assertEquals(0, filterStems(member4.hasAdminInStem()).size());
    assertEquals(0, filterStems(member4.hasOptinInStem()).size());
    assertEquals(0, filterStems(member4.hasOptoutInStem()).size());
    assertEquals(0, filterStems(member4.hasReadInStem()).size());
    assertEquals(3, filterStems(member4.hasUpdateInStem()).size());
    assertEquals(0, filterStems(member4.hasViewInStem()).size());
    assertEquals(0, filterStems(member4.hasGroupAttrReadInStem()).size());
    assertEquals(0, filterStems(member4.hasGroupAttrUpdateInStem()).size());
    assertTrue(member4.hasUpdateInStem().contains(parent));
    assertTrue(member4.hasUpdateInStem().contains(child));
    assertTrue(member4.hasUpdateInStem().contains(stem));
    
    assertEquals(0, filterStems(member5.hasAdminInStem()).size());
    assertEquals(0, filterStems(member5.hasOptinInStem()).size());
    assertEquals(0, filterStems(member5.hasOptoutInStem()).size());
    assertEquals(0, filterStems(member5.hasReadInStem()).size());
    assertEquals(0, filterStems(member5.hasUpdateInStem()).size());
    assertEquals(3, filterStems(member5.hasViewInStem()).size());
    assertEquals(0, filterStems(member5.hasGroupAttrReadInStem()).size());
    assertEquals(0, filterStems(member5.hasGroupAttrUpdateInStem()).size());
    assertTrue(member5.hasViewInStem().contains(parent));
    assertTrue(member5.hasViewInStem().contains(child));
    assertTrue(member5.hasViewInStem().contains(stem));
    
    assertEquals(0, filterStems(member6.hasAdminInStem()).size());
    assertEquals(0, filterStems(member6.hasOptinInStem()).size());
    assertEquals(0, filterStems(member6.hasOptoutInStem()).size());
    assertEquals(0, filterStems(member6.hasReadInStem()).size());
    assertEquals(0, filterStems(member6.hasUpdateInStem()).size());
    assertEquals(0, filterStems(member6.hasViewInStem()).size());
    assertEquals(3, filterStems(member6.hasGroupAttrReadInStem()).size());
    assertEquals(0, filterStems(member6.hasGroupAttrUpdateInStem()).size());
    assertTrue(member6.hasGroupAttrReadInStem().contains(parent));
    assertTrue(member6.hasGroupAttrReadInStem().contains(child));
    assertTrue(member6.hasGroupAttrReadInStem().contains(stem));
    
    assertEquals(0, filterStems(member7.hasAdminInStem()).size());
    assertEquals(0, filterStems(member7.hasOptinInStem()).size());
    assertEquals(0, filterStems(member7.hasOptoutInStem()).size());
    assertEquals(0, filterStems(member7.hasReadInStem()).size());
    assertEquals(0, filterStems(member7.hasUpdateInStem()).size());
    assertEquals(0, filterStems(member7.hasViewInStem()).size());
    assertEquals(0, filterStems(member7.hasGroupAttrReadInStem()).size());
    assertEquals(3, filterStems(member7.hasGroupAttrUpdateInStem()).size());
    assertTrue(member7.hasGroupAttrUpdateInStem().contains(parent));
    assertTrue(member7.hasGroupAttrUpdateInStem().contains(child));
    assertTrue(member7.hasGroupAttrUpdateInStem().contains(stem));
    
  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "false");
  }

}

