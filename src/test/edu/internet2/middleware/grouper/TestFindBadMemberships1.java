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

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.DefaultMemberOf;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.subject.Subject;

/**
 * @author Shilen Patel.
 */
public class TestFindBadMemberships1 extends TestCase {

  private static final Log LOG = LogFactory.getLog(TestFindBadMemberships1.class);

  public TestFindBadMemberships1(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
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
      goodGroups.remove(gE);
      badGroups.add(gE);
      MembershipTestHelper.checkBadGroupMemberships("SB -> gE gets duplicated", goodGroups, badGroups);

      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  }
}

