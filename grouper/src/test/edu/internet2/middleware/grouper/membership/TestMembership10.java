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

package edu.internet2.middleware.grouper.membership;
import java.util.Date;
import java.util.Set;

import junit.framework.Assert;
import junit.textui.TestRunner;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.R;
import edu.internet2.middleware.grouper.helper.T;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * @author Shilen Patel.
 */
public class TestMembership10 extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new TestMembership10("testComplementComposite"));
  }
  
  private static final Log LOG = GrouperUtil.getLog(TestMembership10.class);

  Date before;
  R       r;
  Group   gA;
  Group   gB;
  Group   gC;
  Group   gD;
  Group   gE;
  Group   gF;
  Group   gG;
  Subject subjA;
  Subject subjB;
  Subject subjC;
  Stem    nsA;

  Field fieldMembers;
  Field fieldUpdaters;
  Field fieldCreators;

  public TestMembership10(String name) {
    super(name);
  }

  public void testComplementComposite() {
    LOG.info("testComplementComposite");
    try {
      GrouperUtil.sleep(100);
      before  = new Date();
      GrouperUtil.sleep(100);

      r     = R.populateRegistry(2, 7, 3);
      gA    = r.getGroup("a", "a");
      gB    = r.getGroup("a", "b");
      gC    = r.getGroup("a", "c");
      gD    = r.getGroup("a", "d");
      gE    = r.getGroup("a", "e");
      gF    = r.getGroup("a", "f");
      gG    = r.getGroup("a", "g");
      subjA = r.getSubject("a");
      subjB = r.getSubject("b");
      subjC = r.getSubject("c");
      nsA   = r.getStem("a");

      fieldMembers = Group.getDefaultList();
      fieldUpdaters = FieldFinder.find(Field.FIELD_NAME_UPDATERS, true);
      fieldCreators = FieldFinder.find(Field.FIELD_NAME_CREATORS, true);

      Set<Membership> listMemberships;
      Set<Membership> updateMemberships;
      Set<Membership> createMemberships;

      // Test 1
      gB.addMember(gD.toSubject());
      gD.addMember(subjA);
      gD.addMember(subjB);
      gC.addMember(gE.toSubject());
      gE.addMember(subjA);
      gE.addMember(subjC);
      gA.addCompositeMember(CompositeType.COMPLEMENT , gB, gC);
      gF.addMember(gA.toSubject());
      gG.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);
      nsA.grantPriv(gA.toSubject(), NamingPrivilege.CREATE);

      verifyMemberships();

      // clear out memberships
      gB.deleteMember(gD.toSubject());
      gD.deleteMember(subjA);
      gD.deleteMember(subjB);
      gC.deleteMember(gE.toSubject());
      gE.deleteMember(subjA);
      gE.deleteMember(subjC);
      gA.deleteCompositeMember();
      gF.deleteMember(gA.toSubject());
      gG.revokePriv(gA.toSubject(), AccessPrivilege.UPDATE);
      nsA.revokePriv(gA.toSubject(), NamingPrivilege.CREATE);

      listMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldMembers);
      T.amount("Number of list memberships", 0, listMemberships.size());

      updateMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldUpdaters);
      T.amount("Number of update privileges", 0, updateMemberships.size());

      createMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldCreators);
      T.amount("Number of create privileges", 0, createMemberships.size());

      // Test 2
      gD.addMember(subjA);
      gD.addMember(subjB);
      gC.addMember(gE.toSubject());
      gE.addMember(subjA);
      gE.addMember(subjC);
      gA.addCompositeMember(CompositeType.COMPLEMENT , gB, gC);
      gF.addMember(gA.toSubject());
      gG.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);
      nsA.grantPriv(gA.toSubject(), NamingPrivilege.CREATE);
      gB.addMember(gD.toSubject());

      verifyMemberships();

      // clear out memberships
      gB.deleteMember(gD.toSubject());
      gD.deleteMember(subjA);
      gD.deleteMember(subjB);
      gC.deleteMember(gE.toSubject());
      gE.deleteMember(subjA);
      gE.deleteMember(subjC);
      gA.deleteCompositeMember();
      gF.deleteMember(gA.toSubject());
      gG.revokePriv(gA.toSubject(), AccessPrivilege.UPDATE);
      nsA.revokePriv(gA.toSubject(), NamingPrivilege.CREATE);

      listMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldMembers);
      T.amount("Number of list memberships", 0, listMemberships.size());

      updateMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldUpdaters);
      T.amount("Number of update privileges", 0, updateMemberships.size());

      createMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldCreators);
      T.amount("Number of create privileges", 0, createMemberships.size());

      // Test 3
      gD.addMember(subjB);
      gC.addMember(gE.toSubject());
      gE.addMember(subjA);
      gE.addMember(subjC);
      gA.addCompositeMember(CompositeType.COMPLEMENT , gB, gC);
      gF.addMember(gA.toSubject());
      gG.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);
      nsA.grantPriv(gA.toSubject(), NamingPrivilege.CREATE);
      gB.addMember(gD.toSubject());
      gD.addMember(subjA);

      verifyMemberships();

      // clear out memberships
      gB.deleteMember(gD.toSubject());
      gD.deleteMember(subjA);
      gD.deleteMember(subjB);
      gC.deleteMember(gE.toSubject());
      gE.deleteMember(subjA);
      gE.deleteMember(subjC);
      gA.deleteCompositeMember();
      gF.deleteMember(gA.toSubject());
      gG.revokePriv(gA.toSubject(), AccessPrivilege.UPDATE);
      nsA.revokePriv(gA.toSubject(), NamingPrivilege.CREATE);

      listMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldMembers);
      T.amount("Number of list memberships", 0, listMemberships.size());

      updateMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldUpdaters);
      T.amount("Number of update privileges", 0, updateMemberships.size());

      createMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldCreators);
      T.amount("Number of create privileges", 0, createMemberships.size());

      // Test 4
      gC.addMember(gE.toSubject());
      gE.addMember(subjA);
      gE.addMember(subjC);
      gA.addCompositeMember(CompositeType.COMPLEMENT , gB, gC);
      gF.addMember(gA.toSubject());
      gG.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);
      nsA.grantPriv(gA.toSubject(), NamingPrivilege.CREATE);
      gB.addMember(gD.toSubject());
      gD.addMember(subjA);
      gD.addMember(subjB);

      verifyMemberships();

      // clear out memberships
      gB.deleteMember(gD.toSubject());
      gD.deleteMember(subjA);
      gD.deleteMember(subjB);
      gC.deleteMember(gE.toSubject());
      gE.deleteMember(subjA);
      gE.deleteMember(subjC);
      gA.deleteCompositeMember();
      gF.deleteMember(gA.toSubject());
      gG.revokePriv(gA.toSubject(), AccessPrivilege.UPDATE);
      nsA.revokePriv(gA.toSubject(), NamingPrivilege.CREATE);

      listMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldMembers);
      T.amount("Number of list memberships", 0, listMemberships.size());

      updateMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldUpdaters);
      T.amount("Number of update privileges", 0, updateMemberships.size());

      createMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldCreators);
      T.amount("Number of create privileges", 0, createMemberships.size());

      // Test 5
      gE.addMember(subjA);
      gE.addMember(subjC);
      gA.addCompositeMember(CompositeType.COMPLEMENT , gB, gC);
      gF.addMember(gA.toSubject());
      gG.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);
      nsA.grantPriv(gA.toSubject(), NamingPrivilege.CREATE);
      gB.addMember(gD.toSubject());
      gD.addMember(subjA);
      gD.addMember(subjB);
      gC.addMember(gE.toSubject());

      verifyMemberships();

      // clear out memberships
      gB.deleteMember(gD.toSubject());
      gD.deleteMember(subjA);
      gD.deleteMember(subjB);
      gC.deleteMember(gE.toSubject());
      gE.deleteMember(subjA);
      gE.deleteMember(subjC);
      gA.deleteCompositeMember();
      gF.deleteMember(gA.toSubject());
      gG.revokePriv(gA.toSubject(), AccessPrivilege.UPDATE);
      nsA.revokePriv(gA.toSubject(), NamingPrivilege.CREATE);

      listMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldMembers);
      T.amount("Number of list memberships", 0, listMemberships.size());

      updateMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldUpdaters);
      T.amount("Number of update privileges", 0, updateMemberships.size());

      createMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldCreators);
      T.amount("Number of create privileges", 0, createMemberships.size());

      // Test 6
      gE.addMember(subjC);
      gA.addCompositeMember(CompositeType.COMPLEMENT , gB, gC);
      gF.addMember(gA.toSubject());
      gG.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);
      nsA.grantPriv(gA.toSubject(), NamingPrivilege.CREATE);
      gB.addMember(gD.toSubject());
      gD.addMember(subjA);
      gD.addMember(subjB);
      gC.addMember(gE.toSubject());
      gE.addMember(subjA);

      verifyMemberships();

      // clear out memberships
      gB.deleteMember(gD.toSubject());
      gD.deleteMember(subjA);
      gD.deleteMember(subjB);
      gC.deleteMember(gE.toSubject());
      gE.deleteMember(subjA);
      gE.deleteMember(subjC);
      gA.deleteCompositeMember();
      gF.deleteMember(gA.toSubject());
      gG.revokePriv(gA.toSubject(), AccessPrivilege.UPDATE);
      nsA.revokePriv(gA.toSubject(), NamingPrivilege.CREATE);

      listMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldMembers);
      T.amount("Number of list memberships", 0, listMemberships.size());

      updateMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldUpdaters);
      T.amount("Number of update privileges", 0, updateMemberships.size());

      createMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldCreators);
      T.amount("Number of create privileges", 0, createMemberships.size());

      // Test 7
      gA.addCompositeMember(CompositeType.COMPLEMENT , gB, gC);
      gF.addMember(gA.toSubject());
      gG.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);
      nsA.grantPriv(gA.toSubject(), NamingPrivilege.CREATE);
      gB.addMember(gD.toSubject());
      gD.addMember(subjA);
      gD.addMember(subjB);
      gC.addMember(gE.toSubject());
      gE.addMember(subjA);
      gE.addMember(subjC);

      verifyMemberships();

      // clear out memberships
      gB.deleteMember(gD.toSubject());
      gD.deleteMember(subjA);
      gD.deleteMember(subjB);
      gC.deleteMember(gE.toSubject());
      gE.deleteMember(subjA);
      gE.deleteMember(subjC);
      gA.deleteCompositeMember();
      gF.deleteMember(gA.toSubject());
      gG.revokePriv(gA.toSubject(), AccessPrivilege.UPDATE);
      nsA.revokePriv(gA.toSubject(), NamingPrivilege.CREATE);

      listMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldMembers);
      T.amount("Number of list memberships", 0, listMemberships.size());

      updateMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldUpdaters);
      T.amount("Number of update privileges", 0, updateMemberships.size());

      createMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldCreators);
      T.amount("Number of create privileges", 0, createMemberships.size());

      // Test 8
      gF.addMember(gA.toSubject());
      gG.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);
      nsA.grantPriv(gA.toSubject(), NamingPrivilege.CREATE);
      gB.addMember(gD.toSubject());
      gD.addMember(subjA);
      gD.addMember(subjB);
      gC.addMember(gE.toSubject());
      gE.addMember(subjA);
      gE.addMember(subjC);
      gA.addCompositeMember(CompositeType.COMPLEMENT , gB, gC);

      verifyMemberships();

      // clear out memberships
      gB.deleteMember(gD.toSubject());
      gD.deleteMember(subjA);
      gD.deleteMember(subjB);
      gC.deleteMember(gE.toSubject());
      gE.deleteMember(subjA);
      gE.deleteMember(subjC);
      gA.deleteCompositeMember();
      gF.deleteMember(gA.toSubject());
      gG.revokePriv(gA.toSubject(), AccessPrivilege.UPDATE);
      nsA.revokePriv(gA.toSubject(), NamingPrivilege.CREATE);

      listMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldMembers);
      T.amount("Number of list memberships", 0, listMemberships.size());

      updateMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldUpdaters);
      T.amount("Number of update privileges", 0, updateMemberships.size());

      createMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldCreators);
      T.amount("Number of create privileges", 0, createMemberships.size());

      // Test 9
      gG.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);
      nsA.grantPriv(gA.toSubject(), NamingPrivilege.CREATE);
      gB.addMember(gD.toSubject());
      gD.addMember(subjA);
      gD.addMember(subjB);
      gC.addMember(gE.toSubject());
      gE.addMember(subjA);
      gE.addMember(subjC);
      gA.addCompositeMember(CompositeType.COMPLEMENT , gB, gC);
      gF.addMember(gA.toSubject());

      verifyMemberships();

      // clear out memberships
      gB.deleteMember(gD.toSubject());
      gD.deleteMember(subjA);
      gD.deleteMember(subjB);
      gC.deleteMember(gE.toSubject());
      gE.deleteMember(subjA);
      gE.deleteMember(subjC);
      gA.deleteCompositeMember();
      gF.deleteMember(gA.toSubject());
      gG.revokePriv(gA.toSubject(), AccessPrivilege.UPDATE);
      nsA.revokePriv(gA.toSubject(), NamingPrivilege.CREATE);

      listMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldMembers);
      T.amount("Number of list memberships", 0, listMemberships.size());

      updateMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldUpdaters);
      T.amount("Number of update privileges", 0, updateMemberships.size());

      createMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldCreators);
      T.amount("Number of create privileges", 0, createMemberships.size());

      // Test 10
      nsA.grantPriv(gA.toSubject(), NamingPrivilege.CREATE);
      gB.addMember(gD.toSubject());
      gD.addMember(subjA);
      gD.addMember(subjB);
      gC.addMember(gE.toSubject());
      gE.addMember(subjA);
      gE.addMember(subjC);
      gA.addCompositeMember(CompositeType.COMPLEMENT , gB, gC);
      gF.addMember(gA.toSubject());
      gG.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);

      verifyMemberships();

      // clear out memberships
      gB.deleteMember(gD.toSubject());
      gD.deleteMember(subjA);
      gD.deleteMember(subjB);
      gC.deleteMember(gE.toSubject());
      gE.deleteMember(subjA);
      gE.deleteMember(subjC);
      gA.deleteCompositeMember();
      gF.deleteMember(gA.toSubject());
      gG.revokePriv(gA.toSubject(), AccessPrivilege.UPDATE);
      nsA.revokePriv(gA.toSubject(), NamingPrivilege.CREATE);

      listMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldMembers);
      T.amount("Number of list memberships", 0, listMemberships.size());

      updateMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldUpdaters);
      T.amount("Number of update privileges", 0, updateMemberships.size());

      createMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldCreators);
      T.amount("Number of create privileges", 0, createMemberships.size());


      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  }


  public  void verifyMemberships() throws Exception {

    // gA should have one member only
    T.amount("Verify number of memberships for gA", 1, gA.getCompositeMemberships().size());
    Assert.assertTrue("Verify SB -> gA", gA.hasMember(subjB));

    // verify the total number of list memberships
    Set<Membership> listMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldMembers);
    T.amount("Number of list memberships", 13, listMemberships.size());

    // verify the total number of update privileges
    Set<Membership> updateMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldUpdaters);
    T.amount("Number of update privileges", 2, updateMemberships.size());

    // verify the total number of create privileges
    Set<Membership> createMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldCreators);
    T.amount("Number of create privileges", 2, createMemberships.size());
  }

}

