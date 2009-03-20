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

import java.util.LinkedHashSet;
import java.util.Set;

import junit.framework.Assert;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.MembershipTestHelper;
import edu.internet2.middleware.grouper.helper.R;
import edu.internet2.middleware.grouper.helper.T;
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
public class TestFindBadMemberships extends GrouperTest {

  private static final Log LOG = GrouperUtil.getLog(TestFindBadMemberships.class);

  public TestFindBadMemberships(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testFindBadMemberships0() {
    LOG.info("testFindBadMemberships0");
    try {
      R r = R.populateRegistry(1, 12, 1);
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
      Group gK = r.getGroup("a", "k");
      Group gL = r.getGroup("a", "l");
      Subject subjA = r.getSubject("a");

      gA.addMember(subjA);
      gB.addMember(gA.toSubject());
      gB.addMember(gD.toSubject());
      gD.addMember(gE.toSubject());
      gD.addMember(gF.toSubject());
      gF.addMember(gA.toSubject());
      gI.addMember(gH.toSubject());
      gJ.addMember(gH.toSubject());
      gG.addMember(gC.toSubject());
      gH.addMember(gC.toSubject());
      gC.addMember(gK.toSubject());
      gL.addMember(gB.toSubject());
      gC.addMember(gB.toSubject());

      Set<Group> goodGroups = new LinkedHashSet<Group>();
      Set<Group> badGroups = new LinkedHashSet<Group>();

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
      goodGroups.add(gK);
      goodGroups.add(gL);

      MembershipTestHelper.checkBadGroupMemberships("All should be good", goodGroups, badGroups);

      // gE -> gI gets deleted
      Membership gEgI = MembershipTestHelper.findEffectiveMembership(r.rs, gI, 
          gE.toSubject(), gD, 4, Group.getDefaultList());
      Membership gCgI = MembershipTestHelper.findEffectiveMembership(r.rs, gI, 
          gC.toSubject(), gH, 1, Group.getDefaultList());
      String gEgIParent = gEgI.getParentUuid();
      DefaultMemberOf mof = new DefaultMemberOf();
      mof.addDelete(gEgI);
      GrouperDAOFactory.getFactory().getMembership().update(mof);
      goodGroups.remove(gI);
      badGroups.add(gI);
      MembershipTestHelper.checkBadGroupMemberships("gE -> gI deleted", goodGroups, badGroups);

      // gE -> gI gets added back with the wrong parent uuid.
      gEgI.setHibernateVersionNumber(-1L);
      gEgI.setParentUuid(gCgI.getUuid());
      mof = new DefaultMemberOf();
      mof.addSave(gEgI);
      GrouperDAOFactory.getFactory().getMembership().update(mof);
      MembershipTestHelper.checkBadGroupMemberships("gE -> gI added back with wrong parent", goodGroups, badGroups);

      // gE -> gI gets the wrong viaUuid
      gEgI = MembershipTestHelper.findEffectiveMembership(r.rs, gI, 
          gE.toSubject(), gD, 4, Group.getDefaultList());
      mof = new DefaultMemberOf();
      mof.addDelete(gEgI);
      GrouperDAOFactory.getFactory().getMembership().update(mof);
      gEgI.setHibernateVersionNumber(-1L);
      gEgI.setParentUuid(gEgIParent);
      gEgI.setViaGroupId(gC.getUuid());
      mof = new DefaultMemberOf();
      mof.addSave(gEgI);
      GrouperDAOFactory.getFactory().getMembership().update(mof);
      MembershipTestHelper.checkBadGroupMemberships("gE -> gI gets the wrong viaUuid", goodGroups, badGroups);

      // gE -> gI gets the wrong memberUuid
      gEgI = MembershipTestHelper.findEffectiveMembership(r.rs, gI, 
          gE.toSubject(), gC, 4, Group.getDefaultList());
      mof = new DefaultMemberOf();
      mof.addDelete(gEgI);
      GrouperDAOFactory.getFactory().getMembership().update(mof);
      gEgI.setHibernateVersionNumber(-1L);
      gEgI.setViaGroupId(gD.getUuid());
      gEgI.setMemberUuid(gD.toMember().getUuid());
      mof = new DefaultMemberOf();
      mof.addSave(gEgI);
      GrouperDAOFactory.getFactory().getMembership().update(mof);
      MembershipTestHelper.checkBadGroupMemberships("gE -> gI gets the wrong memberUuid", goodGroups, badGroups);

      // gE -> gI gets corrected again
      gEgI = MembershipTestHelper.findEffectiveMembership(r.rs, gI, 
          gD.toSubject(), gD, 4, Group.getDefaultList());
      mof = new DefaultMemberOf();
      mof.addDelete(gEgI);
      GrouperDAOFactory.getFactory().getMembership().update(mof);
      gEgI.setHibernateVersionNumber(-1L);
      gEgI.setMemberUuid(gE.toMember().getUuid());
      mof = new DefaultMemberOf();
      mof.addSave(gEgI);
      GrouperDAOFactory.getFactory().getMembership().update(mof);
      badGroups.remove(gI);
      goodGroups.add(gI);
      MembershipTestHelper.checkBadGroupMemberships("gE -> gI gets corrected again", goodGroups, badGroups);

      // gE -> gI gets duplicate membership
      mof = new DefaultMemberOf();
      gEgI.setHibernateVersionNumber(-1L);
      gEgI.setUuid("testUuid");
      mof.addSave(gEgI);
      GrouperDAOFactory.getFactory().getMembership().update(mof);
      goodGroups.remove(gI);
      badGroups.add(gI);
      MembershipTestHelper.checkBadGroupMemberships("gE -> gI gets duplicate membership", goodGroups, badGroups);

      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  }

  public void testFindBadMemberships1() {
    LOG.info("testFindBadMemberships1");
    try {
      R r = R.populateRegistry(1, 22, 4);
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
      Group gK = r.getGroup("a", "k");
      Group gL = r.getGroup("a", "l");
      Group gM = r.getGroup("a", "m");
      Group gN = r.getGroup("a", "n");
      Group gO = r.getGroup("a", "o");
      Group gP = r.getGroup("a", "p");
      Group gQ = r.getGroup("a", "q");
      Group gR = r.getGroup("a", "r");
      Group gS = r.getGroup("a", "s");
      Group gT = r.getGroup("a", "t");
      Subject subjA = r.getSubject("a");
      Subject subjB = r.getSubject("b");
      Subject subjC = r.getSubject("c");
      Subject subjD = r.getSubject("d");
  
      gA.addMember(subjB);
      gT.addMember(gP.toSubject());
      gD.addCompositeMember(CompositeType.UNION, gB, gC);
      gP.addCompositeMember(CompositeType.UNION, gD, gO);
      gB.addMember(gA.toSubject());
      gN.addMember(gG.toSubject());
      gG.addMember(gD.toSubject());
      gF.addMember(gH.toSubject());
      gH.addMember(subjA);
      gI.addCompositeMember(CompositeType.UNION, gJ, gK);
      gJ.addMember(gE.toSubject());
      gK.addMember(subjC);
      gL.addMember(gI.toSubject());
      gM.addMember(gJ.toSubject());
      gE.addCompositeMember(CompositeType.UNION, gF, gG);
      gQ.addMember(subjD);
      gH.addMember(gQ.toSubject());
      gR.addMember(gS.toSubject());
      gF.addMember(gR.toSubject());
      gR.addMember(gQ.toSubject());
  
      Set<Group> goodGroups = new LinkedHashSet<Group>();
      Set<Group> badGroups = new LinkedHashSet<Group>();
  
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
      goodGroups.add(gK);
      goodGroups.add(gL);
      goodGroups.add(gM);
      goodGroups.add(gN);
      goodGroups.add(gO);
      goodGroups.add(gP);
      goodGroups.add(gQ);
      goodGroups.add(gR);
      goodGroups.add(gS);
      goodGroups.add(gT);
  
      MembershipTestHelper.checkBadGroupMemberships("All should be good", goodGroups, badGroups);
  
      // SB -> gL gets deleted
      Membership SBgL = MembershipTestHelper.findEffectiveMembership(r.rs, gL, 
          subjB, gI, 1, Group.getDefaultList());
      DefaultMemberOf mof = new DefaultMemberOf();
      mof.addDelete(SBgL);
      GrouperDAOFactory.getFactory().getMembership().update(mof);
      goodGroups.remove(gL);
      badGroups.add(gL);
      MembershipTestHelper.checkBadGroupMemberships("SB -> gL gets deleted", goodGroups, badGroups);
  
      // gQ -> gI gets deleted
      Membership gQgI = MembershipTestHelper.findCompositeMembership(r.rs, 
          gI, gQ.toSubject());
      mof = new DefaultMemberOf();
      mof.addDelete(gQgI);
      GrouperDAOFactory.getFactory().getMembership().update(mof);
      goodGroups.remove(gI);
      badGroups.add(gI);
      MembershipTestHelper.checkBadGroupMemberships("gQ -> gI gets deleted", goodGroups, badGroups);
  
      // SB -> gE gets duplicated
      Membership SBgE = MembershipTestHelper.findCompositeMembership(r.rs, 
          gE, subjB);
      mof = new DefaultMemberOf();
      SBgE.setHibernateVersionNumber(-1L);
      SBgE.setUuid("testuuid");
      mof.addSave(SBgE);
      GrouperDAOFactory.getFactory().getMembership().update(mof);
      goodGroups.clear();
      badGroups.add(gE);
      MembershipTestHelper.checkBadGroupMemberships("SB -> gE gets duplicated", goodGroups, badGroups);
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  }

  public void testFindBadMemberships2() {
    LOG.info("testFindBadMemberships2");
    try {
      R r = R.populateRegistry(1, 11, 4);
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
      Group gK = r.getGroup("a", "k");
      Subject subjA = r.getSubject("a");
      Subject subjB = r.getSubject("b");
      Subject subjC = r.getSubject("c");
      Subject subjD = r.getSubject("d");
  
      gB.addMember(subjB);
      gB.addMember(gD.toSubject());
      gB.addMember(gE.toSubject());
      gB.grantPriv(gC.toSubject(), AccessPrivilege.UPDATE);
      gD.addMember(subjD);
      gD.addMember(gF.toSubject());
      gE.grantPriv(subjC, AccessPrivilege.UPDATE);
      gE.grantPriv(gG.toSubject(), AccessPrivilege.UPDATE);
      gH.addMember(gB.toSubject());
      gI.grantPriv(gH.toSubject(), AccessPrivilege.UPDATE);
      gJ.addMember(gA.toSubject());
      gK.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);
      gA.grantPriv(subjA, AccessPrivilege.UPDATE);
      gA.grantPriv(gB.toSubject(), AccessPrivilege.UPDATE);
  
      Set<Group> goodGroups = new LinkedHashSet<Group>();
      Set<Group> badGroups = new LinkedHashSet<Group>();
  
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
      goodGroups.add(gK);
  
      MembershipTestHelper.checkBadGroupMemberships("All should be good", goodGroups, badGroups);
  
      // gF -> gA gets deleted
      Membership gFgA = MembershipTestHelper.findEffectiveMembership(r.rs, gA, 
          gF.toSubject(), gD, 2, FieldFinder.find("updaters", true));
      DefaultMemberOf mof = new DefaultMemberOf();
      mof.addDelete(gFgA);
      GrouperDAOFactory.getFactory().getMembership().update(mof);
      goodGroups.remove(gA);
      badGroups.add(gA);
      MembershipTestHelper.checkBadGroupMemberships("gF -> gA gets deleted", goodGroups, badGroups);
  
      // SD -> gB gets deleted
      Membership SDgB = MembershipTestHelper.findEffectiveMembership(r.rs, gB, 
          subjD, gD, 1, Group.getDefaultList());
      mof = new DefaultMemberOf();
      mof.addDelete(SDgB);
      GrouperDAOFactory.getFactory().getMembership().update(mof);
      goodGroups.remove(gB);
      goodGroups.remove(gH);
      badGroups.add(gB);
      badGroups.add(gH);
      MembershipTestHelper.checkBadGroupMemberships("SD -> gB gets deleted", goodGroups, badGroups);
  
  
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
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
  
      // gD -> gH gets deleted
      Membership gDgH = MembershipTestHelper.findEffectiveMembership(r.rs, gH, 
          gD.toSubject(), gA, 2, FieldFinder.find("updaters", true));
      DefaultMemberOf mof = new DefaultMemberOf();
      mof.addDelete(gDgH);
      GrouperDAOFactory.getFactory().getMembership().update(mof);
      goodGroups.remove(gH);
      badGroups.add(gH);
      MembershipTestHelper.checkBadGroupMemberships("gD -> gH gets deleted", goodGroups, badGroups);
      MembershipTestHelper.checkBadStemMemberships("gD -> gH gets deleted", goodStems, badStems);
  
      // gD -> nsA gets deleted (create priv)
      Membership gDnsA = MembershipTestHelper.findEffectiveMembership(r.rs, nsA, 
          gD.toSubject(), gE, 1, FieldFinder.find("creators", true));
      mof = new DefaultMemberOf();
      mof.addDelete(gDnsA);
      GrouperDAOFactory.getFactory().getMembership().update(mof);
      goodStems.remove(nsA);
      badStems.add(nsA);
      MembershipTestHelper.checkBadGroupMemberships("gD -> nsA gets deleted", goodGroups, badGroups);
      MembershipTestHelper.checkBadStemMemberships("gD -> nsA gets deleted", goodStems, badStems);
  
      // gD -> nsB gets deleted (stem priv)
      Membership gDnsB = MembershipTestHelper.findEffectiveMembership(r.rs, nsB, 
          gD.toSubject(), gE, 1, FieldFinder.find("stemmers", true));
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

