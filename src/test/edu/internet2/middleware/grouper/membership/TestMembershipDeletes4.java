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

import junit.framework.TestCase;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.helper.DateHelper;
import edu.internet2.middleware.grouper.helper.R;
import edu.internet2.middleware.grouper.helper.T;
import edu.internet2.middleware.grouper.misc.DefaultMemberOf;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.MemberOf;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * @author Shilen Patel.
 */
public class TestMembershipDeletes4 extends TestCase {

  private static final Log LOG = GrouperUtil.getLog(TestMembershipDeletes4.class);

  Date before;
  R       r;
  Group   gA;
  Group   gB;
  Group   gC;
  Group   gD;
  Group   gE;
  Subject subjA;
  Subject subjB;
  Stem    nsA;
  Member  memberA;

  public TestMembershipDeletes4(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testMembershipDeletes4() {
    LOG.info("testMembershipDeletes4");
    try {
      before   = DateHelper.getPastDate();

      r     = R.populateRegistry(1, 5, 1);
      gA    = r.getGroup("a", "a");
      gB    = r.getGroup("a", "b");
      gC    = r.getGroup("a", "c");
      gD    = r.getGroup("a", "d");
      gE    = r.getGroup("a", "e");
      subjA = r.getSubject("a");
      memberA = MemberFinder.findBySubject(r.rs, subjA, true);

      // initial data
      gA.addMember(gB.toSubject());
      gB.addMember(gC.toSubject());
      gC.addMember(gA.toSubject());
      gD.addMember(gC.toSubject());
      gC.addMember(subjA);
      gB.grantPriv(gC.toSubject(), AccessPrivilege.OPTIN);
      gE.addMember(gD.toSubject());
      gD.grantPriv(gC.toSubject(), AccessPrivilege.UPDATE);
      gC.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);

      // Remove gD -> gE
      MemberOf mof = new DefaultMemberOf();
      Membership ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        gE.getUuid(), gD.toMember().getUuid(), Group.getDefaultList(), Membership.IMMEDIATE, true);
      mof.deleteImmediate(r.rs, gE, ms, gD.toMember());
      assertEquals("mof deletes", 5, mof.getDeletes().size());
      assertEquals("mof saves", 0, mof.getSaves().size());

      // Remove gC -> gD
      mof = new DefaultMemberOf();
      ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        gD.getUuid(), gC.toMember().getUuid(), Group.getDefaultList(), Membership.IMMEDIATE, true);
      mof.deleteImmediate(r.rs, gD, ms, gC.toMember());
      assertEquals("mof deletes", 8, mof.getDeletes().size());
      assertEquals("mof saves", 0, mof.getSaves().size());

      // Remove gC -> gD (update priv)
      mof = new DefaultMemberOf();
      ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        gD.getUuid(), gC.toMember().getUuid(), FieldFinder.find("updaters", true), Membership.IMMEDIATE, true);
      mof.deleteImmediate(r.rs, gD, ms, gC.toMember());
      assertEquals("mof deletes", 4, mof.getDeletes().size());
      assertEquals("mof saves", 0, mof.getSaves().size());

      // Remove gA -> gC
      mof = new DefaultMemberOf();
      ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        gC.getUuid(), gA.toMember().getUuid(), Group.getDefaultList(), Membership.IMMEDIATE, true);
      mof.deleteImmediate(r.rs, gC, ms, gA.toMember());
      assertEquals("mof deletes", 11, mof.getDeletes().size());
      assertEquals("mof saves", 0, mof.getSaves().size());

      // Remove SA -> gC
      mof = new DefaultMemberOf();
      ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        gC.getUuid(), memberA.getUuid(), Group.getDefaultList(), Membership.IMMEDIATE, true);
      mof.deleteImmediate(r.rs, gC, ms, memberA);
      assertEquals("mof deletes", 8, mof.getDeletes().size());
      assertEquals("mof saves", 0, mof.getSaves().size());

      // Remove gA -> gC (update priv)
      mof = new DefaultMemberOf();
      ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        gC.getUuid(), gA.toMember().getUuid(), FieldFinder.find("updaters", true), Membership.IMMEDIATE, true);
      mof.deleteImmediate(r.rs, gC, ms, gA.toMember());
      assertEquals("mof deletes", 4, mof.getDeletes().size());
      assertEquals("mof saves", 0, mof.getSaves().size());

      // Remove gB -> gA
      mof = new DefaultMemberOf();
      ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        gA.getUuid(), gB.toMember().getUuid(), Group.getDefaultList(), Membership.IMMEDIATE, true);
      mof.deleteImmediate(r.rs, gA, ms, gB.toMember());
      assertEquals("mof deletes", 11, mof.getDeletes().size());
      assertEquals("mof saves", 0, mof.getSaves().size());

      // Remove gC -> gB
      mof = new DefaultMemberOf();
      ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        gB.getUuid(), gC.toMember().getUuid(), Group.getDefaultList(), Membership.IMMEDIATE, true);
      mof.deleteImmediate(r.rs, gB, ms, gC.toMember());
      assertEquals("mof deletes", 7, mof.getDeletes().size());
      assertEquals("mof saves", 0, mof.getSaves().size());

      // Remove gC -> gB (opt-in)
      mof = new DefaultMemberOf();
      ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        gB.getUuid(), gC.toMember().getUuid(), FieldFinder.find("optins", true), Membership.IMMEDIATE, true);
      mof.deleteImmediate(r.rs, gB, ms, gC.toMember());
      assertEquals("mof deletes", 4, mof.getDeletes().size());
      assertEquals("mof saves", 0, mof.getSaves().size());



      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  }
}
