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
package edu.internet2.middleware.grouper.membership;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;

/**
 * @author shilen
 * $Id$
 */
public class TestInvalidGrouperAllMemberships extends GrouperTest {
  
  /** top level stem */
  private Stem edu;

  /** root session */
  private GrouperSession grouperSession;
  
  /** root stem */
  private Stem root;
  
  /** group */
  private Group group;
  
  /**
   * @param name
   */
  public TestInvalidGrouperAllMemberships(String name) {
    super(name);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    
    grouperSession     = SessionHelper.getRootSession();
    root  = StemHelper.findRootStem(grouperSession);
    edu   = StemHelper.addChildStem(root, "edu", "education");
    group = edu.addChildGroup("group", "group");
  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    super.tearDown();
  }
  
  /**
   * 
   */
  public void testAdminPrivilege() {
    try {
      group.grantPriv(SubjectFinder.findAllSubject(), AccessPrivilege.ADMIN);
      fail("unexpected");
    } catch (Exception e) {
      // good
    }
    
    assertFalse(group.hasAdmin(SubjectFinder.findAllSubject()));
  }
  
  /**
   * 
   */
  public void testUpdatePrivilege() {
    try {
      group.grantPriv(SubjectFinder.findAllSubject(), AccessPrivilege.UPDATE);
      fail("unexpected");
    } catch (Exception e) {
      // good
    }
    
    assertFalse(group.hasUpdate(SubjectFinder.findAllSubject()));
  }
  
  /**
   * 
   */
  public void testGroupAttrUpdatePrivilege() {
    try {
      group.grantPriv(SubjectFinder.findAllSubject(), AccessPrivilege.GROUP_ATTR_UPDATE);
      fail("unexpected");
    } catch (Exception e) {
      // good
    }
    
    assertFalse(group.hasGroupAttrUpdate(SubjectFinder.findAllSubject()));
  }
  
  /**
   * 
   */
  public void testGroupAttrReadPrivilege() {
    group.grantPriv(SubjectFinder.findAllSubject(), AccessPrivilege.GROUP_ATTR_READ);
    assertTrue(group.hasGroupAttrRead(SubjectFinder.findAllSubject()));
  }
  
  /**
   * 
   */
  public void testMember() {
    try {
      group.addMember(SubjectFinder.findAllSubject());
      fail("unexpected");
    } catch (Exception e) {
      // good
    }
    
    assertFalse(group.hasMember(SubjectFinder.findAllSubject()));
  }
  
  /**
   * 
   */
  public void testMemberChange() {
    Group groupAsMember = edu.addChildGroup("groupAsMember", "groupAsMember");
    Member memberRoot = MemberFinder.findBySubject(grouperSession, SubjectFinder.findRootSubject(), true);
    Member memberAll = MemberFinder.findBySubject(grouperSession, SubjectFinder.findAllSubject(), true);
    
    group.addMember(groupAsMember.toSubject());
    Membership ms = MembershipFinder.findImmediateMembership(grouperSession, group, groupAsMember.toSubject(), true);
   
    // this should work
    ms.setMember(memberRoot);
    ms.setMemberUuid(memberRoot.getId());
    GrouperDAOFactory.getFactory().getMembership().update(ms);
    
    try {
      ms.setMember(memberAll);
      ms.setMemberUuid(memberAll.getId());
      GrouperDAOFactory.getFactory().getMembership().update(ms);
      fail("unexpected");
    } catch (Exception e) {
      // good
    }
  }
}