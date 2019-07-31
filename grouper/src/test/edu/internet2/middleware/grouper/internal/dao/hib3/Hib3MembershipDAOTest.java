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
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.subject.Source;
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

  /**
   * (see GRP-2106) effective memberships from multiple sources should not be double counted.
   * For related tests, see also {@link edu.internet2.middleware.grouper.MembershipFinderTest#testMembershipSizeWithMultipleEffective()}
   * @throws Exception
   */
  public void testRetrieveMembersDistinct() throws Exception {
    //lets add some members
    GrouperSession grouperSession = SessionHelper.getRootSession();
    Group group1 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:testGroup1").save();
    Group group2 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:testGroup2").save();
    Group group3 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:testGroup3").save();
    Group group4 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:testGroup4").save();
    group1.addMember(SubjectTestHelper.SUBJ0);
    group1.addMember(SubjectTestHelper.SUBJ1);
    group1.addMember(SubjectTestHelper.SUBJ2);
    group2.addMember(SubjectTestHelper.SUBJ1);
    group2.addMember(SubjectTestHelper.SUBJ2);
    group2.addMember(SubjectTestHelper.SUBJ3);
    group3.addMember(group1.toSubject());
    group3.addMember(group2.toSubject());
    group4.addMember(SubjectTestHelper.SUBJ1);

    Field grouperMemberField = FieldFinder.find("members", true);
    Set<Source> nonGroupSourcesCache = SubjectHelper.nonGroupSources();

    /* SUBJ1 amd SUBJ2 have 2 effective memberships in group3, should not double count the unique members */
    QueryOptions queryOptions = new QueryOptions().retrieveResults(true).retrieveCount(true);
    Set<Member> members = GrouperDAOFactory.getFactory().getMembership().findAllMembersByGroupOwnerAndFieldAndType(group3.getId(), grouperMemberField, null, queryOptions, true);
    assertEquals(6, members.size());
    assertEquals(6, queryOptions.getCount().intValue());

    /* same as above, but just indirect members */
    queryOptions = new QueryOptions().retrieveResults(true).retrieveCount(true);
    members = GrouperDAOFactory.getFactory().getMembership().findAllMembersByGroupOwnerAndFieldAndType(group3.getId(), grouperMemberField, "effective", queryOptions, true);
    assertEquals(4, members.size());
    assertEquals(4, queryOptions.getCount().intValue());


    /* similar as above, but a different method that can specify the sources */
    queryOptions = new QueryOptions().retrieveResults(true).retrieveCount(true);
    members = new Hib3MembershipDAO().findAllMembersByGroupOwnerAndField(group3.getId(), grouperMemberField, nonGroupSourcesCache, queryOptions, true);
    assertEquals(4, members.size());
    assertEquals(4, queryOptions.getCount().intValue());

    /* effective group3 memberships not in group4 includes SUBJ2 twice; don't double count it */
    queryOptions = new QueryOptions().retrieveResults(true).retrieveCount(true);
    members = new Hib3MembershipDAO().findAllMembersInOneGroupNotOtherAndType(group3.getId(), group4.getId(), "effective", null, queryOptions, true, false);
    assertEquals(3, members.size());
    assertEquals(3, queryOptions.getCount().intValue());

    /* For an attribute privilege, do not double count effective permissions from multiple paths (SUBJ1 and SUBJ2) */
    AttributeDef attributeDef = new AttributeDefSave(grouperSession).assignName("test:testAttributeDef").assignCreateParentStemsIfNotExist(true).save();
    attributeDef.getPrivilegeDelegate().grantPriv(group3.toSubject(), AttributeDefPrivilege.ATTR_READ, false);
    queryOptions = new QueryOptions().retrieveResults(true).retrieveCount(true);
    members = new Hib3MembershipDAO().findAllMembersByAttrDefOwnerAndFieldAndType(attributeDef.getId(), FieldFinder.find("attrReaders", true), "effective", queryOptions, true);
    assertEquals(6, members.size());
    assertEquals(6, queryOptions.getCount().intValue());
  }

  
}
