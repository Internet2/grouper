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
package edu.internet2.middleware.grouper.membership;

import junit.textui.TestRunner;

import java.util.HashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;

/**
 * @author shilen
 */
public class TestSelfMembershipRead extends GrouperTest {

  private GrouperSession grouperSession = null;

  public static void main(String[] args) {
    TestRunner.run(new TestSelfMembershipRead("testMembershipFinder"));
  }
  
  /**
   * @param name
   */
  public TestSelfMembershipRead(String name) {
    super(name);
  }

  protected void tearDown () {
    super.tearDown();
  }
  
  protected void setUp() {
    super.setUp();

    try {
      this.grouperSession = GrouperSession.startRootSession();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.membership.allowSelfRead", "true");
  }
  
  /**
   *
   */
  public void testGetGroups() throws Exception {
    Group testGroup1 = new GroupSave(this.grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:testGroup1").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:testGroup2").save();
    Group testGroup3 = new GroupSave(this.grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:testGroup3").save();
    Group testGroup4 = new GroupSave(this.grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:testGroup4").save();
    Group testGroup5 = new GroupSave(this.grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:testGroup5").save();
    Group testGroup6 = new GroupSave(this.grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:testGroup6").save();

    // group 1 - just member
    testGroup1.addMember(SubjectTestHelper.SUBJ0);
    testGroup1.addMember(SubjectTestHelper.SUBJ1);
    
    // group 2 - member and read
    testGroup2.addMember(SubjectTestHelper.SUBJ0);
    testGroup2.addMember(SubjectTestHelper.SUBJ1);
    testGroup2.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ);
    
    // group 3 - member and view
    testGroup3.addMember(SubjectTestHelper.SUBJ0);
    testGroup3.addMember(SubjectTestHelper.SUBJ1);
    testGroup3.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW);

    // group 4 - member and view
    testGroup4.addMember(SubjectTestHelper.SUBJ0);
    testGroup4.addMember(SubjectTestHelper.SUBJ1);
    testGroup4.grantPriv(SubjectFinder.findById("GrouperAll", true), AccessPrivilege.VIEW);
    
    // group 5 - member and optin
    testGroup5.addMember(SubjectTestHelper.SUBJ0);
    testGroup5.addMember(SubjectTestHelper.SUBJ1);
    testGroup5.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.OPTIN);
    
    // group 6 - just view
    testGroup6.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW);
    
    Member member0 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0, false);
    Member member1 = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, false);
    
    {
      // test default list as root
      Set<Group> groups = member0.getGroups();
      assertEquals(5, groups.size());
      assertTrue(groups.contains(testGroup1));
      assertTrue(groups.contains(testGroup2));
      assertTrue(groups.contains(testGroup3));
      assertTrue(groups.contains(testGroup4));
      assertTrue(groups.contains(testGroup5));
    }
    
    {
      // test view as root
      Set<Group> groups = member0.getGroups(FieldFinder.find("viewers", true));
      assertEquals(2, groups.size());
      assertTrue(groups.contains(testGroup3));
      assertTrue(groups.contains(testGroup6));
    }
    
    GrouperSession.start(SubjectTestHelper.SUBJ0);

    {
      // test default list as test.subject.0
      Set<Group> groups = member0.getGroups();
      assertEquals(4, groups.size());
      assertTrue(groups.contains(testGroup2));
      assertTrue(groups.contains(testGroup3));
      assertTrue(groups.contains(testGroup4));
      assertTrue(groups.contains(testGroup5));
    }
    
    {
      // test default list as test.subject.0 (for subj1's memberships)
      Set<Group> groups = member1.getGroups();
      assertEquals(1, groups.size());
      assertTrue(groups.contains(testGroup2));
    }
    
    {
      // test view as test.subject.0
      Set<Group> groups = member0.getGroups(FieldFinder.find("viewers", true));
      assertEquals(0, groups.size());
    }
    
    GrouperSession.start(SubjectTestHelper.SUBJ1);

    {
      // test default list as test.subject.1
      Set<Group> groups = member0.getGroups();
      assertEquals(0, groups.size());
    }
    
    {
      // test view as test.subject.1
      Set<Group> groups = member0.getGroups(FieldFinder.find("viewers", true));
      assertEquals(0, groups.size());
    }
  }
  
  /**
   *
   */
  public void testMembershipFinder() throws Exception {
    Group testGroup1 = new GroupSave(this.grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:testGroup1").save();
    Group testGroup2 = new GroupSave(this.grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:testGroup2").save();
    Group testGroup3 = new GroupSave(this.grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:testGroup3").save();
    Group testGroup4 = new GroupSave(this.grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:testGroup4").save();
    Group testGroup5 = new GroupSave(this.grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:testGroup5").save();
    Group testGroup6 = new GroupSave(this.grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:testGroup6").save();

    // group 1 - just member
    testGroup1.addMember(SubjectTestHelper.SUBJ0);
    testGroup1.addMember(SubjectTestHelper.SUBJ1);

    // group 2 - member and read
    testGroup2.addMember(SubjectTestHelper.SUBJ0);
    testGroup2.addMember(SubjectTestHelper.SUBJ1);
    testGroup2.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ);

    // group 3 - member and view
    testGroup3.addMember(SubjectTestHelper.SUBJ0);
    testGroup3.addMember(SubjectTestHelper.SUBJ1);
    testGroup3.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW);

    // group 4 - member and view
    testGroup4.addMember(SubjectTestHelper.SUBJ0);
    testGroup4.addMember(SubjectTestHelper.SUBJ1);
    testGroup4.grantPriv(SubjectFinder.findById("GrouperAll", true), AccessPrivilege.VIEW);

    // group 5 - member and optin
    testGroup5.addMember(SubjectTestHelper.SUBJ0);
    testGroup5.addMember(SubjectTestHelper.SUBJ1);
    testGroup5.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.OPTIN);

    // group 6 - just view
    testGroup6.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW);

    {
      // test default list as root
      MembershipFinder finder = new MembershipFinder();
      finder.addSubject(SubjectTestHelper.SUBJ0);
      Set<Object[]> results = finder.findMembershipsMembers();
      assertEquals(5, results.size());
    }

    {
      // test view as root
      MembershipFinder finder = new MembershipFinder();
      finder.addSubject(SubjectTestHelper.SUBJ0);
      finder.addField("viewers");
      Set<Object[]> results = finder.findMembershipsMembers();
      assertEquals(2, results.size());
    }

    GrouperSession.start(SubjectTestHelper.SUBJ0);

    {
      // test default list as test.subject.0
      MembershipFinder finder = new MembershipFinder();
      finder.addSubject(SubjectTestHelper.SUBJ0);
      Set<Object[]> results = finder.findMembershipsMembers();
      assertEquals(4, results.size());
      Set<Group> resultGroups = new HashSet<Group>();
      
      for (Object[] result : results) {
        Membership currMembership = (Membership)result[0];
        Group currGroup = (Group)result[1];
        Member currMember = (Member)result[2];
        
        assertEquals("members", currMembership.getField().getName());
        assertEquals("test.subject.0", currMember.getSubjectId());
        resultGroups.add(currGroup);
      }
      
      assertTrue(resultGroups.contains(testGroup2));
      assertTrue(resultGroups.contains(testGroup3));
      assertTrue(resultGroups.contains(testGroup4));
      assertTrue(resultGroups.contains(testGroup5));
    }
    
    {
      // test default list as test.subject.0 (for subj1's memberships)
      MembershipFinder finder = new MembershipFinder();
      finder.addSubject(SubjectTestHelper.SUBJ1);
      Set<Object[]> results = finder.findMembershipsMembers();
      assertEquals(1, results.size());
      Set<Group> resultGroups = new HashSet<Group>();
      
      for (Object[] result : results) {
        Membership currMembership = (Membership)result[0];
        Group currGroup = (Group)result[1];
        Member currMember = (Member)result[2];
        
        assertEquals("members", currMembership.getField().getName());
        assertEquals("test.subject.1", currMember.getSubjectId());
        resultGroups.add(currGroup);
      }
      
      assertTrue(resultGroups.contains(testGroup2));
    }
    
    {
      // test default list as test.subject.0 - specify field type list
      MembershipFinder finder = new MembershipFinder();
      finder.addSubject(SubjectTestHelper.SUBJ0);
      finder.assignFieldType(FieldType.LIST);
      Set<Object[]> results = finder.findMembershipsMembers();
      assertEquals(4, results.size());
      Set<Group> resultGroups = new HashSet<Group>();
      
      for (Object[] result : results) {
        Membership currMembership = (Membership)result[0];
        Group currGroup = (Group)result[1];
        Member currMember = (Member)result[2];
        
        assertEquals("members", currMembership.getField().getName());
        assertEquals("test.subject.0", currMember.getSubjectId());
        resultGroups.add(currGroup);
      }
      
      assertTrue(resultGroups.contains(testGroup2));
      assertTrue(resultGroups.contains(testGroup3));
      assertTrue(resultGroups.contains(testGroup4));
      assertTrue(resultGroups.contains(testGroup5));
    }
    
    {
      // test default list as test.subject.0 - specify field members
      MembershipFinder finder = new MembershipFinder();
      finder.addSubject(SubjectTestHelper.SUBJ0);
      finder.assignField(Group.getDefaultList());
      Set<Object[]> results = finder.findMembershipsMembers();
      assertEquals(4, results.size());
      Set<Group> resultGroups = new HashSet<Group>();
      
      for (Object[] result : results) {
        Membership currMembership = (Membership)result[0];
        Group currGroup = (Group)result[1];
        Member currMember = (Member)result[2];
        
        assertEquals("members", currMembership.getField().getName());
        assertEquals("test.subject.0", currMember.getSubjectId());
        resultGroups.add(currGroup);
      }
      
      assertTrue(resultGroups.contains(testGroup2));
      assertTrue(resultGroups.contains(testGroup3));
      assertTrue(resultGroups.contains(testGroup4));
      assertTrue(resultGroups.contains(testGroup5));
    }
    
    {
      // test default list as test.subject.0 - specify field type access
      MembershipFinder finder = new MembershipFinder();
      finder.addSubject(SubjectTestHelper.SUBJ0);
      finder.assignFieldType(FieldType.ACCESS);
      Set<Object[]> results = finder.findMembershipsMembers();
      assertEquals(0, results.size());
    }
    
    {
      // test default list as test.subject.0 - specify admin privilege needed
      MembershipFinder finder = new MembershipFinder();
      finder.addSubject(SubjectTestHelper.SUBJ0);
      finder.addPrivilegeTheUserHas(AccessPrivilege.ADMIN);
      Set<Object[]> results = finder.findMembershipsMembers();
      assertEquals(0, results.size());
    }
    
    {
      // test default list as test.subject.0 - specify group
      MembershipFinder finder = new MembershipFinder();
      finder.addSubject(SubjectTestHelper.SUBJ0);
      finder.addGroup(testGroup3);
      Set<Object[]> results = finder.findMembershipsMembers();
      assertEquals(1, results.size());
    }
    
    {
      // test default list as test.subject.0 - specify multiple members - expected not to work
      MembershipFinder finder = new MembershipFinder();
      finder.addSubject(SubjectTestHelper.SUBJ0);
      finder.addSubject(SubjectTestHelper.SUBJ2);
      Set<Object[]> results = finder.findMembershipsMembers();
      assertEquals(1, results.size());
      Set<Group> resultGroups = new HashSet<Group>();
      
      for (Object[] result : results) {
        Membership currMembership = (Membership)result[0];
        Group currGroup = (Group)result[1];
        Member currMember = (Member)result[2];
        
        assertEquals("members", currMembership.getField().getName());
        assertEquals("test.subject.0", currMember.getSubjectId());
        resultGroups.add(currGroup);
      }
      
      assertTrue(resultGroups.contains(testGroup2));
    }

    {
      // test view as test.subject.0
      MembershipFinder finder = new MembershipFinder();
      finder.addSubject(SubjectTestHelper.SUBJ0);
      finder.addField("viewers");
      Set<Object[]> results = finder.findMembershipsMembers();
      assertEquals(0, results.size());
    }

    GrouperSession.start(SubjectTestHelper.SUBJ1);

    {
      // test default list as test.subject.1
      MembershipFinder finder = new MembershipFinder();
      finder.addSubject(SubjectTestHelper.SUBJ0);
      Set<Object[]> results = finder.findMembershipsMembers();
      assertEquals(0, results.size());
    }

    {
      // test view as test.subject.1
      MembershipFinder finder = new MembershipFinder();
      finder.addSubject(SubjectTestHelper.SUBJ0);
      finder.addField("viewers");
      Set<Object[]> results = finder.findMembershipsMembers();
      assertEquals(0, results.size());
    }
  }
}