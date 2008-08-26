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

import  edu.internet2.middleware.grouper.internal.dto.MembershipDTO;
import  edu.internet2.middleware.subject.*;
import  java.util.*;
import  junit.framework.*;
import  org.apache.commons.logging.*;

/**
 * @author Shilen Patel.
 */
public class TestFindBadMemberships0 extends TestCase {

  private static final Log LOG = LogFactory.getLog(TestFindBadMemberships0.class);

  public TestFindBadMemberships0(String name) {
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
      MembershipDTO gEgI = 
        (MembershipDTO)MembershipTestHelper.findEffectiveMembership(r.rs, gI, gE.toSubject(), gD, 4, Group.getDefaultList()).getDTO();
      MembershipDTO gCgI = 
        (MembershipDTO)MembershipTestHelper.findEffectiveMembership(r.rs, gI, gC.toSubject(), gH, 1, Group.getDefaultList()).getDTO();
      String gEgIParent = gEgI.getParentUuid();
      DefaultMemberOf mof = new DefaultMemberOf();
      mof.addDelete(gEgI);
      GrouperDAOFactory.getFactory().getMembership().update(mof);
      goodGroups.remove(gI);
      badGroups.add(gI);
      MembershipTestHelper.checkBadGroupMemberships("gE -> gI deleted", goodGroups, badGroups);

      // gE -> gI gets added back with the wrong parent uuid.
      gEgI.setId(null);
      gEgI.setParentUuid(gCgI.getUuid());
      mof = new DefaultMemberOf();
      mof.addSave(gEgI);
      GrouperDAOFactory.getFactory().getMembership().update(mof);
      MembershipTestHelper.checkBadGroupMemberships("gE -> gI added back with wrong parent", goodGroups, badGroups);

      // gE -> gI gets the wrong viaUuid
      gEgI =
        (MembershipDTO)MembershipTestHelper.findEffectiveMembership(r.rs, gI, gE.toSubject(), gD, 4, Group.getDefaultList()).getDTO();
      mof = new DefaultMemberOf();
      mof.addDelete(gEgI);
      GrouperDAOFactory.getFactory().getMembership().update(mof);
      gEgI.setId(null);
      gEgI.setParentUuid(gEgIParent);
      gEgI.setViaUuid(gC.getUuid());
      mof = new DefaultMemberOf();
      mof.addSave(gEgI);
      GrouperDAOFactory.getFactory().getMembership().update(mof);
      MembershipTestHelper.checkBadGroupMemberships("gE -> gI gets the wrong viaUuid", goodGroups, badGroups);

      // gE -> gI gets the wrong memberUuid
      gEgI =
        (MembershipDTO)MembershipTestHelper.findEffectiveMembership(r.rs, gI, gE.toSubject(), gC, 4, Group.getDefaultList()).getDTO();
      mof = new DefaultMemberOf();
      mof.addDelete(gEgI);
      GrouperDAOFactory.getFactory().getMembership().update(mof);
      gEgI.setId(null);
      gEgI.setViaUuid(gD.getUuid());
      gEgI.setMemberUuid(gD.toMember().getUuid());
      mof = new DefaultMemberOf();
      mof.addSave(gEgI);
      GrouperDAOFactory.getFactory().getMembership().update(mof);
      MembershipTestHelper.checkBadGroupMemberships("gE -> gI gets the wrong memberUuid", goodGroups, badGroups);

      // gE -> gI gets corrected again
      gEgI = 
        (MembershipDTO)MembershipTestHelper.findEffectiveMembership(r.rs, gI, gD.toSubject(), gD, 4, Group.getDefaultList()).getDTO();
      mof = new DefaultMemberOf();
      mof.addDelete(gEgI);
      GrouperDAOFactory.getFactory().getMembership().update(mof);
      gEgI.setId(null);
      gEgI.setMemberUuid(gE.toMember().getUuid());
      mof = new DefaultMemberOf();
      mof.addSave(gEgI);
      GrouperDAOFactory.getFactory().getMembership().update(mof);
      badGroups.remove(gI);
      goodGroups.add(gI);
      MembershipTestHelper.checkBadGroupMemberships("gE -> gI gets corrected again", goodGroups, badGroups);

      // gE -> gI gets duplicate membership
      mof = new DefaultMemberOf();
      gEgI.setId(null);
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
}

