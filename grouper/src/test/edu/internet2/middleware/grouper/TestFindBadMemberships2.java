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
public class TestFindBadMemberships2 extends TestCase {

  private static final Log LOG = LogFactory.getLog(TestFindBadMemberships2.class);

  public TestFindBadMemberships2(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
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
      MembershipDTO gFgA =
        (MembershipDTO)MembershipTestHelper.findEffectiveMembership(r.rs, gA, gF.toSubject(), gD, 2, FieldFinder.find("updaters")).getDTO();
      DefaultMemberOf mof = new DefaultMemberOf();
      mof.addDelete(gFgA);
      GrouperDAOFactory.getFactory().getMembership().update(mof);
      goodGroups.remove(gA);
      badGroups.add(gA);
      MembershipTestHelper.checkBadGroupMemberships("gF -> gA gets deleted", goodGroups, badGroups);

      // SD -> gB gets deleted
      MembershipDTO SDgB =
        (MembershipDTO)MembershipTestHelper.findEffectiveMembership(r.rs, gB, subjD, gD, 1, Group.getDefaultList()).getDTO();
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
}

