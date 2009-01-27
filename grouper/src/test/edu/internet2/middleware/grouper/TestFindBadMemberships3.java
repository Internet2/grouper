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

import java.util.LinkedHashSet;
import java.util.Set;

import junit.framework.Assert;
import junit.framework.TestCase;
import junit.textui.TestRunner;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.DefaultMemberOf;
import edu.internet2.middleware.grouper.misc.FindBadMemberships;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * @author Shilen Patel.
 */
public class TestFindBadMemberships3 extends TestCase {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new TestFindBadMemberships3("testFindBadMemberships3"));
  }
  
  private static final Log LOG = GrouperUtil.getLog(TestFindBadMemberships3.class);

  public TestFindBadMemberships3(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testFindBadMemberships3() {
    LOG.info("testFindBadMemberships3");
    try {
      R r = R.populateRegistry(3, 10, 2);
      Group gA = r.getGroup("a", "a");
      Group gB = r.getGroup("a", "b");
      Group gC = r.getGroup("a", "c");
      Group gD = r.getGroup("a", "d");
      Group gE = r.getGroup("a", "e");
      Group gF = r.getGroup("a", "f");
      Group gG = r.getGroup("a", "g");
      Group gH = r.getGroup("a", "h");
      Group gI = r.getGroup("a", "i");
      Group gJ = r.getGroup("a", "j");
      Subject subjA = r.getSubject("a");
      Subject subjB = r.getSubject("b");
      Stem nsA = r.getStem("a");
      Stem nsB = r.getStem("b");

      nsA.grantPriv(gE.toSubject(), NamingPrivilege.CREATE);
      nsB.grantPriv(gE.toSubject(), NamingPrivilege.STEM);
      nsB.grantPriv(gE.toSubject(), NamingPrivilege.CREATE);
      gA.addCompositeMember(CompositeType.UNION, gB, gC);
      gA.grantPriv(subjB, AccessPrivilege.UPDATE);
      gB.addMember(gD.toSubject());
      gC.addMember(subjA);
      gE.addCompositeMember(CompositeType.UNION, gF, gA);
      gG.grantPriv(gE.toSubject(), AccessPrivilege.UPDATE);
      gI.addMember(gA.toSubject());
      gH.grantPriv(gI.toSubject(), AccessPrivilege.UPDATE);
      gJ.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);

      Set<Group> goodGroups = new LinkedHashSet<Group>();
      Set<Group> badGroups = new LinkedHashSet<Group>();

      Set<Stem> goodStems = new LinkedHashSet<Stem>();
      Set<Stem> badStems= new LinkedHashSet<Stem>();

      goodGroups.add(gA);
      goodGroups.add(gB);
      goodGroups.add(gC);
      goodGroups.add(gD);
      goodGroups.add(gE);
      goodGroups.add(gF);
      goodGroups.add(gG);
      goodGroups.add(gH);
      goodGroups.add(gI);
      goodGroups.add(gJ);

      goodStems.add(nsA);
      goodStems.add(nsB);

      MembershipTestHelper.checkBadGroupMemberships("All should be good", goodGroups, badGroups);
      MembershipTestHelper.checkBadStemMemberships("All should be good", goodStems, badStems);
      Assert.assertEquals("There should not be any invalid memberships", 0, FindBadMemberships.checkMembershipsWithInvalidOwners());

      // create a membership with an invalid owner
      Membership invalid = MembershipTestHelper.findEffectiveMembership(r.rs, gG, gD.toSubject(), gE, 1, 
            FieldFinder.find("updaters"));
      invalid.setHibernateVersionNumber(-1L);
      invalid.setOwnerGroupId("invalid1");
      invalid.setUuid("invalid1");
      DefaultMemberOf mof = new DefaultMemberOf();
      mof.addSave(invalid);
      GrouperDAOFactory.getFactory().getMembership().update(mof);
      Assert.assertEquals("There should be 1 invalid membership", 1, FindBadMemberships.checkMembershipsWithInvalidOwners());

      // create another membership with an invalid owner
      invalid.setHibernateVersionNumber(-1L);
      invalid.setOwnerStemId("invalid2");
      invalid.setUuid("invalid2");
      mof = new DefaultMemberOf();
      mof.addSave(invalid);
      GrouperDAOFactory.getFactory().getMembership().update(mof);
      Assert.assertEquals("There should be 2 invalid memberships", 2, FindBadMemberships.checkMembershipsWithInvalidOwners());

      // gD -> gH gets deleted
      Membership gDgH = MembershipTestHelper.findEffectiveMembership(r.rs, gH, 
          gD.toSubject(), gA, 2, FieldFinder.find("updaters"));
      mof = new DefaultMemberOf();
      mof.addDelete(gDgH);
      GrouperDAOFactory.getFactory().getMembership().update(mof);
      goodGroups.remove(gH);
      badGroups.add(gH);
      MembershipTestHelper.checkBadGroupMemberships("gD -> gH gets deleted", goodGroups, badGroups);
      MembershipTestHelper.checkBadStemMemberships("gD -> gH gets deleted", goodStems, badStems);

      // gD -> nsA gets deleted (create priv)
      Membership gDnsA = MembershipTestHelper.findEffectiveMembership(r.rs, nsA, 
          gD.toSubject(), gE, 1, FieldFinder.find("creators"));
      mof = new DefaultMemberOf();
      mof.addDelete(gDnsA);
      GrouperDAOFactory.getFactory().getMembership().update(mof);
      goodStems.remove(nsA);
      badStems.add(nsA);
      MembershipTestHelper.checkBadGroupMemberships("gD -> nsA gets deleted", goodGroups, badGroups);
      MembershipTestHelper.checkBadStemMemberships("gD -> nsA gets deleted", goodStems, badStems);

      // gD -> nsB gets deleted (stem priv)
      Membership gDnsB = MembershipTestHelper.findEffectiveMembership(r.rs, nsB, 
          gD.toSubject(), gE, 1, FieldFinder.find("stemmers"));
      mof = new DefaultMemberOf();
      mof.addDelete(gDnsB);
      GrouperDAOFactory.getFactory().getMembership().update(mof);
      goodStems.remove(nsB);
      badStems.add(nsB);
      MembershipTestHelper.checkBadGroupMemberships("gD -> nsB gets deleted", goodGroups, badGroups);
      MembershipTestHelper.checkBadStemMemberships("gD -> nsB gets deleted", goodStems, badStems);


      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  }
}

