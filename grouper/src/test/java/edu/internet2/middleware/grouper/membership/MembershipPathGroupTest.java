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
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.membership;

import java.util.ArrayList;
import java.util.List;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 *
 */
public class MembershipPathGroupTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new MembershipPathGroupTest("testMembershipPathGroup"));
  }
  
  /**
   * 
   */
  public MembershipPathGroupTest() {
    
  }
  
  /**
   * @param name
   */
  public MembershipPathGroupTest(String name) {
    super(name);
  }

  /**
   * 
   */
  public void testCompositePrivilege() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();

    Group g0 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:mpaths2:privGroup").save();
    Group g1 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:mpaths2:endGroup").save();
    Group g2 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:mpaths2:factor1").save();
    Group g3 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:mpaths2:factor2").save();
    Subject s0 = SubjectFinder.findById("test.subject.0", true);

    
    g0.grantPriv(g1.toSubject(), AccessPrivilege.OPTIN);
    g1.addCompositeMember(CompositeType.INTERSECTION, g2, g3);
    g2.addMember(s0);
    g3.addMember(s0);

    MembershipPathGroup membershipPathGroup = MembershipPathGroup.analyzePrivileges(g0, s0);

    List<MembershipPath> membershipPaths = new ArrayList<MembershipPath>(membershipPathGroup.getMembershipPaths());

    assertEquals(2, membershipPaths.size());

    assertTrue(membershipPaths.get(0).isPathAllowed());
    assertEquals(3, membershipPaths.get(0).getMembershipPathNodes().size());
    assertEquals("test:mpaths2:factor1", membershipPaths.get(0).getMembershipPathNodes().get(0).getOwnerGroup().getName());
    assertEquals("test:mpaths2:endGroup", membershipPaths.get(0).getMembershipPathNodes().get(1).getOwnerGroup().getName());
    assertEquals("test:mpaths2:factor2", membershipPaths.get(0).getMembershipPathNodes().get(1).getOtherFactor().getName());
    assertEquals("test:mpaths2:privGroup", membershipPaths.get(0).getMembershipPathNodes().get(2).getOwnerGroup().getName());
    assertEquals(1, membershipPaths.get(0).getFields().size());
    assertEquals("optins", membershipPaths.get(0).getFields().iterator().next().getName());

    assertTrue(membershipPaths.get(1).isPathAllowed());
    assertEquals(3, membershipPaths.get(1).getMembershipPathNodes().size());
    assertEquals("test:mpaths2:factor2", membershipPaths.get(1).getMembershipPathNodes().get(0).getOwnerGroup().getName());
    assertEquals("test:mpaths2:endGroup", membershipPaths.get(1).getMembershipPathNodes().get(1).getOwnerGroup().getName());
    assertEquals("test:mpaths2:factor1", membershipPaths.get(1).getMembershipPathNodes().get(1).getOtherFactor().getName());
    assertEquals("test:mpaths2:privGroup", membershipPaths.get(1).getMembershipPathNodes().get(2).getOwnerGroup().getName());
    assertEquals(1, membershipPaths.get(1).getFields().size());
    assertEquals("optins", membershipPaths.get(1).getFields().iterator().next().getName());

    membershipPathGroup = MembershipPathGroup.analyze(g1, s0, Group.getDefaultList());

    membershipPaths = new ArrayList<MembershipPath>(membershipPathGroup.getMembershipPaths());
    
    String membershipPathGroupString = membershipPathGroup.toString();
    
    assertEquals(membershipPathGroupString, 2, membershipPaths.size());

    assertTrue(membershipPathGroupString, membershipPaths.get(0).isPathAllowed());
    assertEquals(membershipPathGroupString, 2, membershipPaths.get(0).getMembershipPathNodes().size());
    assertEquals(membershipPathGroupString, "test:mpaths2:factor1", membershipPaths.get(0).getMembershipPathNodes().get(0).getOwnerGroup().getName());
    assertEquals(membershipPathGroupString, "test:mpaths2:endGroup", membershipPaths.get(0).getMembershipPathNodes().get(1).getOwnerGroup().getName());
    assertEquals(membershipPathGroupString, "test:mpaths2:factor2", membershipPaths.get(0).getMembershipPathNodes().get(1).getOtherFactor().getName());
    assertEquals(membershipPathGroupString, 1, membershipPaths.get(0).getFields().size());
    assertEquals(membershipPathGroupString, "members", membershipPaths.get(0).getFields().iterator().next().getName());

    assertTrue(membershipPathGroupString, membershipPaths.get(1).isPathAllowed());
    assertEquals(membershipPathGroupString, 2, membershipPaths.get(1).getMembershipPathNodes().size());
    assertEquals(membershipPathGroupString, "test:mpaths2:factor2", membershipPaths.get(1).getMembershipPathNodes().get(0).getOwnerGroup().getName());
    assertEquals(membershipPathGroupString, "test:mpaths2:endGroup", membershipPaths.get(1).getMembershipPathNodes().get(1).getOwnerGroup().getName());
    assertEquals(membershipPathGroupString, "test:mpaths2:factor1", membershipPaths.get(1).getMembershipPathNodes().get(1).getOtherFactor().getName());
    assertEquals(membershipPathGroupString, 1, membershipPaths.get(1).getFields().size());
    assertEquals(membershipPathGroupString, "members", membershipPaths.get(1).getFields().iterator().next().getName());

    
//    test.subject.0 (fields: optins):  -> test:mpaths2:factor1 -> test:mpaths2:endGroup (composite intersection in test:mpaths2:factor1 and in test:mpaths2:factor2) -> test:mpaths2:privGroup
//    test.subject.0 (fields: optins):  -> test:mpaths2:factor2 -> test:mpaths2:endGroup (composite intersection in test:mpaths2:factor1 and in test:mpaths2:factor2) -> test:mpaths2:privGroup


  }
  
  /**
   * 
   */
  public void testMembershipPathGroup() {
    
    //########################## Non composite
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Subject memberSubject = SubjectFinder.findById("test.subject.0", true);
    Subject sessionSubject = SubjectFinder.findById("test.subject.1", true);
    Subject sessionSubject2 = SubjectFinder.findById("test.subject.2", true);
    Subject sessionSubject3 = SubjectFinder.findById("test.subject.3", true);
    Subject sessionSubject4 = SubjectFinder.findById("test.subject.4", true);
    Member memberMember = MemberFinder.findBySubject(grouperSession, memberSubject, true);
    
    Group endGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
        .assignName("test:mpaths:overallGroup").save();
    
    Group privGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:mpaths:privGroup").save();    
    
    Stem privStem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:mpaths").save();

    AttributeDef privAttributeDef = new AttributeDefSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignAttributeDefType(AttributeDefType.attr).assignName("test:mpaths:privAttrDef").assignToStem(true).save();

    endGroup.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
    endGroup.grantPriv(sessionSubject2, AccessPrivilege.READ, false);
    endGroup.grantPriv(sessionSubject3, AccessPrivilege.ADMIN, false);
    endGroup.grantPriv(sessionSubject4, AccessPrivilege.READ, false);

    privGroup.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
    privGroup.grantPriv(SubjectFinder.findAllSubject(), AccessPrivilege.OPTIN, false);
    privGroup.grantPriv(sessionSubject2, AccessPrivilege.ADMIN, false);
    privGroup.grantPriv(sessionSubject3, AccessPrivilege.ADMIN, false);
    privGroup.grantPriv(sessionSubject4, AccessPrivilege.ADMIN, false);

    privGroup.grantPriv(endGroup.toSubject(), AccessPrivilege.READ, false);
    privGroup.grantPriv(endGroup.toSubject(), AccessPrivilege.UPDATE, false);
    privGroup.grantPriv(memberSubject, AccessPrivilege.VIEW, false);

    privStem.grantPriv(SubjectFinder.findAllSubject(), NamingPrivilege.STEM_ATTR_READ, false);
    privStem.grantPriv(sessionSubject2, NamingPrivilege.STEM, false);
    privStem.grantPriv(sessionSubject3, NamingPrivilege.STEM, false);
    privStem.grantPriv(sessionSubject4, NamingPrivilege.STEM, false);
    
    privAttributeDef.getPrivilegeDelegate().grantPriv(SubjectFinder.findAllSubject(), AttributeDefPrivilege.ATTR_OPTIN, false);
    privAttributeDef.getPrivilegeDelegate().grantPriv(sessionSubject2, AttributeDefPrivilege.ATTR_READ, false);
    privAttributeDef.getPrivilegeDelegate().grantPriv(sessionSubject3, AttributeDefPrivilege.ATTR_ADMIN, false);
    privAttributeDef.getPrivilegeDelegate().grantPriv(sessionSubject4, AttributeDefPrivilege.ATTR_READ, false);
    
    privStem.grantPriv(endGroup.toSubject(), NamingPrivilege.STEM, false);
    privStem.grantPriv(endGroup.toSubject(), NamingPrivilege.CREATE, false);
    privStem.grantPriv(memberSubject, NamingPrivilege.STEM_ATTR_READ, false);

    privAttributeDef.getPrivilegeDelegate().grantPriv(endGroup.toSubject(), Privilege.getInstance("attrRead"), false);
    privAttributeDef.getPrivilegeDelegate().grantPriv(endGroup.toSubject(), Privilege.getInstance("attrUpdate"), false);
    privAttributeDef.getPrivilegeDelegate().grantPriv(memberSubject, Privilege.getInstance("attrView"), false);

    endGroup.addMember(memberSubject, false);
    
    //one hop membership
    {
      Group intermediateGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:mpaths:intermediateGroup").save();
  
      intermediateGroup.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
      intermediateGroup.grantPriv(sessionSubject3, AccessPrivilege.ADMIN, false);
      intermediateGroup.grantPriv(sessionSubject4, AccessPrivilege.READ, false);

      endGroup.addMember(intermediateGroup.toSubject(), false);
      intermediateGroup.addMember(memberSubject, false);

      privGroup.grantPriv(intermediateGroup.toSubject(), AccessPrivilege.ADMIN, false);
      privStem.grantPriv(intermediateGroup.toSubject(), NamingPrivilege.STEM_ATTR_UPDATE, false);
      privAttributeDef.getPrivilegeDelegate().grantPriv(intermediateGroup.toSubject(), Privilege.getInstance("attrAdmin"), false);
    }
    
    //two hop membership
    {
      Group intermediateGroup2a_member = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:mpaths:intermediateGroup2a_member").save();
      intermediateGroup2a_member.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
      Group intermediateGroup2b_owner = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:mpaths:intermediateGroup2b_owner").save();
      intermediateGroup2b_owner.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);

      endGroup.addMember(intermediateGroup2b_owner.toSubject(), false);
      intermediateGroup2b_owner.addMember(intermediateGroup2a_member.toSubject(), false);
      intermediateGroup2a_member.addMember(memberSubject, false);
    }

    Group overallComposite = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
        .assignName("test:mpaths:overallComposite").save();
    overallComposite.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
    overallComposite.grantPriv(sessionSubject4, AccessPrivilege.READ, false);
    overallComposite.grantPriv(endGroup.toSubject(), AccessPrivilege.OPTOUT, false);
    
    {
      Group appGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:mpaths:appGroup").save();
      appGroup.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
      appGroup.grantPriv(sessionSubject4, AccessPrivilege.READ, false);
  
      Group employeeGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:mpaths:employeeGroup").save();
      employeeGroup.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
      employeeGroup.grantPriv(sessionSubject4, AccessPrivilege.READ, false);
  
      if (!overallComposite.hasComposite()) {
        overallComposite.addCompositeMember( CompositeType.INTERSECTION, appGroup, employeeGroup );
      }
  
      appGroup.addMember(memberSubject, false);
      employeeGroup.addMember(memberSubject, false);
      
      endGroup.addMember(overallComposite.toSubject(), false);
    }
    
    Group overallCompositeDepth2owner = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
        .assignName("test:mpaths:overallCompositeDepth2owner").save();
    overallCompositeDepth2owner.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);

    {
      Group overallCompositeDepth2 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:mpaths:overallCompositeDepth2").save();
      overallCompositeDepth2.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);

      Group appGroupDepth2 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:mpaths:appGroupDepth2").save();
      appGroupDepth2.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);

      Group employeeGroupDepth2 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:mpaths:employeeGroupDepth2").save();
      employeeGroupDepth2.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);

      overallCompositeDepth2owner.addMember(overallCompositeDepth2.toSubject(), false);
      
      if (!overallCompositeDepth2.hasComposite()) {
        overallCompositeDepth2.addCompositeMember( CompositeType.INTERSECTION, appGroupDepth2, employeeGroupDepth2 );
      }
  
      appGroupDepth2.addMember(memberSubject, false);
      employeeGroupDepth2.addMember(memberSubject, false);
      
      endGroup.addMember(overallCompositeDepth2owner.toSubject(), false);
    }
    
    Group overallEffectiveComposite = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
        .assignName("test:mpaths:overallEffectiveComposite").save();
    overallEffectiveComposite.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);

    {
      Group appEffectiveGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:mpaths:appEffectiveGroup").save();
      appEffectiveGroup.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
      Group appImmediateGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:mpaths:appImmediateGroup").save();
      appImmediateGroup.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);

      Group employeeEffectiveGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:mpaths:employeeEffectiveGroup").save();
      employeeEffectiveGroup.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
      Group employeeImmediateGroup = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
          .assignName("test:mpaths:employeeImmediateGroup").save();
      employeeImmediateGroup.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);

      if (!overallEffectiveComposite.hasComposite()) {
        overallEffectiveComposite.addCompositeMember( CompositeType.INTERSECTION, appEffectiveGroup, employeeEffectiveGroup );
      }

      appEffectiveGroup.addMember(appImmediateGroup.toSubject(), false);
      employeeEffectiveGroup.addMember(employeeImmediateGroup.toSubject(), false);

      appImmediateGroup.addMember(memberSubject, false);
      employeeImmediateGroup.addMember(memberSubject, false);

      endGroup.addMember(overallEffectiveComposite.toSubject(), false);
    }
    
    MembershipPathGroup membershipPathGroup = MembershipPathGroup.analyze(endGroup, memberMember, Group.getDefaultList());
    
    //grouper system
    List<MembershipPath> membershipPaths = new ArrayList<MembershipPath>(membershipPathGroup.getMembershipPaths());
    
    assertEquals(9, GrouperUtil.length(membershipPaths));
    assertTrue(membershipPaths.get(0).isPathAllowed());
    assertEquals(1, membershipPaths.get(0).getMembershipPathNodes().size());
    assertEquals("test:mpaths:overallGroup", membershipPaths.get(0).getMembershipPathNodes().get(0).getOwnerGroup().getName());
    
    assertEquals(2, membershipPaths.get(1).getMembershipPathNodes().size());
    assertTrue(membershipPaths.get(1).isPathAllowed());
    assertEquals("test:mpaths:intermediateGroup", membershipPaths.get(1).getMembershipPathNodes().get(0).getOwnerGroup().getName());
    assertEquals("test:mpaths:overallGroup", membershipPaths.get(1).getMembershipPathNodes().get(1).getOwnerGroup().getName());

    // privs
    membershipPathGroup = MembershipPathGroup.analyzePrivileges(privGroup, memberMember);
    
    //grouper system
    membershipPaths = new ArrayList<MembershipPath>(membershipPathGroup.getMembershipPaths());
    
    assertEquals(11, GrouperUtil.length(membershipPaths));
    
    assertTrue(membershipPaths.get(0).isPathAllowed());
    assertEquals(1, membershipPaths.get(0).getMembershipPathNodes().size());
    assertEquals("test:mpaths:privGroup", membershipPaths.get(0).getMembershipPathNodes().get(0).getOwnerGroup().getName());
    assertEquals(1, membershipPaths.get(0).getFields().size());
    assertEquals("viewers", membershipPaths.get(0).getFields().iterator().next().getName());

    assertTrue(membershipPaths.get(1).isPathAllowed());
    assertEquals(2, membershipPaths.get(1).getMembershipPathNodes().size());
    assertEquals("test:mpaths:intermediateGroup", membershipPaths.get(1).getMembershipPathNodes().get(0).getOwnerGroup().getName());
    assertEquals("test:mpaths:privGroup", membershipPaths.get(1).getMembershipPathNodes().get(1).getOwnerGroup().getName());
    assertEquals(1, membershipPaths.get(1).getFields().size());
    assertEquals("admins", membershipPaths.get(1).getFields().iterator().next().getName());

    assertTrue(membershipPaths.get(2).isPathAllowed());
    assertEquals(2, membershipPaths.get(2).getMembershipPathNodes().size());
    assertEquals("test:mpaths:overallGroup", membershipPaths.get(2).getMembershipPathNodes().get(0).getOwnerGroup().getName());
    assertEquals("test:mpaths:privGroup", membershipPaths.get(2).getMembershipPathNodes().get(1).getOwnerGroup().getName());
    assertEquals(2, membershipPaths.get(2).getFields().size());
    assertTrue(membershipPaths.get(2).getFields().contains(AccessPrivilege.READ.getField()));
    assertTrue(membershipPaths.get(2).getFields().contains(AccessPrivilege.UPDATE.getField()));
    
    assertEquals(3, membershipPaths.get(3).getMembershipPathNodes().size());
    assertTrue(membershipPaths.get(3).isPathAllowed());
    assertEquals("test:mpaths:intermediateGroup", membershipPaths.get(3).getMembershipPathNodes().get(0).getOwnerGroup().getName());
    assertEquals("test:mpaths:overallGroup", membershipPaths.get(3).getMembershipPathNodes().get(1).getOwnerGroup().getName());
    assertEquals("test:mpaths:privGroup", membershipPaths.get(3).getMembershipPathNodes().get(2).getOwnerGroup().getName());
    assertEquals(2, membershipPaths.get(3).getFields().size());
    assertTrue(membershipPaths.get(3).getFields().contains(AccessPrivilege.READ.getField()));
    assertTrue(membershipPaths.get(3).getFields().contains(AccessPrivilege.UPDATE.getField()));

    
    //System.out.prin tln(membershipPathGroup.toString());

    //########################## noncomposite as test.subject.1
    //System.out.prin tln("\n\n########################## noncomposite as test.subject.1\n");

    GrouperSession.stopQuietly(grouperSession);

    grouperSession = GrouperSession.start(sessionSubject);

    membershipPathGroup = MembershipPathGroup.analyze(endGroup, memberMember, Group.getDefaultList());
    membershipPaths = new ArrayList<MembershipPath>(membershipPathGroup.getMembershipPaths());
    
    assertEquals(0, GrouperUtil.length(membershipPaths));
//    assertFalse(membershipPaths.get(0).isPathAllowed());
//    assertEquals(1, membershipPaths.get(0).getMembershipPathNodes().size());
//    assertEquals("test:mpaths:overallGroup", membershipPaths.get(0).getMembershipPathNodes().get(0).getOwnerGroup().getName());
    
//    assertEquals(2, membershipPaths.get(1).getMembershipPathNodes().size());
//    assertFalse(membershipPaths.get(1).isPathAllowed());
//    assertEquals("test:mpaths:intermediateGroup", membershipPaths.get(1).getMembershipPathNodes().get(0).getOwnerGroup().getName());
//    assertEquals("test:mpaths:overallGroup", membershipPaths.get(1).getMembershipPathNodes().get(1).getOwnerGroup().getName());

    //System.out.prin tln(membershipPathGroup.toString());


    //########################## noncomposite as test.subject.2
    //System.out.prin tln("\n\n########################## noncomposite as test.subject.2\n");

    GrouperSession.stopQuietly(grouperSession);

    grouperSession = GrouperSession.start(sessionSubject2);

    membershipPathGroup = MembershipPathGroup.analyze(endGroup, memberMember, Group.getDefaultList());
    membershipPaths = new ArrayList<MembershipPath>(membershipPathGroup.getMembershipPaths());

    assertEquals(3, GrouperUtil.length(membershipPaths));
    assertTrue(membershipPaths.get(0).isPathAllowed());
    assertEquals(1, membershipPaths.get(0).getMembershipPathNodes().size());
    assertEquals("test:mpaths:overallGroup", membershipPaths.get(0).getMembershipPathNodes().get(0).getOwnerGroup().getName());
    
    assertEquals(2, membershipPaths.get(1).getMembershipPathNodes().size());
    assertFalse(membershipPaths.get(1).isPathAllowed());
    assertEquals("test:mpaths:intermediateGroup", membershipPaths.get(1).getMembershipPathNodes().get(0).getOwnerGroup().getName());
    assertEquals("test:mpaths:overallGroup", membershipPaths.get(1).getMembershipPathNodes().get(1).getOwnerGroup().getName());

    //System.out.prin tln(membershipPathGroup.toString());


    //########################## noncomposite as test.subject.3
    //System.out.prin tln("\n\n########################## noncomposite as test.subject.3\n");

    GrouperSession.stopQuietly(grouperSession);

    grouperSession = GrouperSession.start(sessionSubject3);

    membershipPathGroup = MembershipPathGroup.analyze(endGroup, memberMember, Group.getDefaultList());
    membershipPaths = new ArrayList<MembershipPath>(membershipPathGroup.getMembershipPaths());
    
    assertEquals(3, GrouperUtil.length(membershipPaths));
    assertTrue(membershipPaths.get(0).isPathAllowed());
    assertEquals(1, membershipPaths.get(0).getMembershipPathNodes().size());
    assertEquals("test:mpaths:overallGroup", membershipPaths.get(0).getMembershipPathNodes().get(0).getOwnerGroup().getName());
    
    assertEquals(2, membershipPaths.get(1).getMembershipPathNodes().size());
    assertTrue(membershipPaths.get(1).isPathAllowed());
    assertEquals("test:mpaths:intermediateGroup", membershipPaths.get(1).getMembershipPathNodes().get(0).getOwnerGroup().getName());
    assertEquals("test:mpaths:overallGroup", membershipPaths.get(1).getMembershipPathNodes().get(1).getOwnerGroup().getName());

    //System.out.prin tln(membershipPathGroup.toString());


    //########################## noncomposite as test.subject.4
    //System.out.prin tln("\n\n########################## noncomposite as test.subject.4\n");

    GrouperSession.stopQuietly(grouperSession);

    grouperSession = GrouperSession.start(sessionSubject4);

    membershipPathGroup = MembershipPathGroup.analyze(endGroup, memberMember, Group.getDefaultList());
    membershipPaths = new ArrayList<MembershipPath>(membershipPathGroup.getMembershipPaths());
    
    //System.out.prin tln(membershipPathGroup.toString());


    //########################## composite
    GrouperSession.stopQuietly(grouperSession);

    grouperSession = GrouperSession.startRootSession();

    //System.out.prin tln("\n\n########################## composite\n");

    membershipPathGroup = MembershipPathGroup.analyze(overallComposite, memberMember, Group.getDefaultList());
    membershipPaths = new ArrayList<MembershipPath>(membershipPathGroup.getMembershipPaths());
    
    //System.out.prin tln(membershipPathGroup.toString());

    
  }
}
