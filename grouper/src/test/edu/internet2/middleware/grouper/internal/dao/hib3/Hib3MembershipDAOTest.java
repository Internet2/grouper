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
 * @author mchyzer
 * $Id: Hib3MembershipDAOTest.java,v 1.5 2009-08-18 23:11:39 shilen Exp $
 */
package edu.internet2.middleware.grouper.internal.dao.hib3;

import java.util.Set;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;

/**
 *
 */
public class Hib3MembershipDAOTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new Hib3MembershipDAOTest("testRetrieveMembershipsByMember"));
  }
  
  /**
   * @param name
   */
  public Hib3MembershipDAOTest(String name) {
    super(name);
  }

  /**
   * @throws Exception 
   * 
   */
  public void testRetrieveMembershipsByMember() throws Exception {
    //lets add some members
    GrouperSession grouperSession = SessionHelper.getRootSession();
    Stem root = StemHelper.findRootStem(grouperSession);
    Stem edu = StemHelper.addChildStem(root, "edu", "education");
    Group i2 = StemHelper.addChildGroup(edu, "i2", "internet2");
    Group i3 = StemHelper.addChildGroup(edu, "i3", "internet2");
    Group i4 = StemHelper.addChildGroup(edu, "i4", "internet2");

    i2.addMember(SubjectTestHelper.SUBJ0);
    i2.addMember(SubjectTestHelper.SUBJ1);
    i2.addMember(SubjectTestHelper.SUBJ2);
    i2.addMember(SubjectTestHelper.SUBJ3);
    i2.addMember(SubjectFinder.findRootSubject());

    i3.addMember(SubjectTestHelper.SUBJ0);
    i3.addMember(SubjectTestHelper.SUBJ1);
    i3.addMember(SubjectTestHelper.SUBJ2);
    i3.addMember(SubjectTestHelper.SUBJ3);
    i3.addMember(SubjectFinder.findRootSubject());

    i4.addMember(SubjectTestHelper.SUBJ0);
    i4.addMember(SubjectTestHelper.SUBJ1);
    i4.addMember(SubjectTestHelper.SUBJ2);
    i4.addMember(SubjectTestHelper.SUBJ3);
    i4.addMember(SubjectFinder.findRootSubject());

    
    //get all memberships by member, 
    Set<Membership> allMemberships = new Hib3MembershipDAO().findAllByGroupOwnerAndField(i2.getUuid(), Group.getDefaultList(), true);
    
    assertTrue(Integer.toString(allMemberships.size()),  2 < allMemberships.size());
    
    //lets get all members
    Set<Member> members = i2.getMembers();
    
    Set<Membership> byMembersLarge = new Hib3MembershipDAO().findAllByGroupOwnerAndFieldAndMembers(i2.getUuid(), Group.getDefaultList(),
        members, true);
    
    assertEquals(allMemberships.size(), byMembersLarge.size());
    
    int originalBatchSize = Hib3MembershipDAO.batchSize;
    Hib3MembershipDAO.batchSize = 2;
    try {
      Set<Membership> byMembersSmall = new Hib3MembershipDAO().findAllByGroupOwnerAndFieldAndMembers(i2.getUuid(), Group.getDefaultList(),
          members, true);
      
      assertEquals(allMemberships.size(), byMembersSmall.size());
      
    } finally {
      Hib3MembershipDAO.batchSize = originalBatchSize;
    }
  }
  
}
