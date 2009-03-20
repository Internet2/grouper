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

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
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
public class TestMembershipDeletes5 extends TestCase {

  private static final Log LOG = GrouperUtil.getLog(TestMembershipDeletes5.class);

  Date before;
  R       r;
  Group   gA;
  Group   gB;
  Group   gC;
  Group   gD;
  Subject subjA;
  Subject subjB;
  Subject subjC;
  Member  memberA;

  Field fieldMembers;
  Field fieldUpdaters;
  Field fieldCustom1;
  Field fieldCustom2;

  public TestMembershipDeletes5(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.reset();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }

  public void testMembershipDeletes5() {
    LOG.info("testMembershipDeletes5");
    try {
      before   = DateHelper.getPastDate();

      r     = R.populateRegistry(1, 4, 3);
      gA    = r.getGroup("a", "a");
      gB    = r.getGroup("a", "b");
      gC    = r.getGroup("a", "c");
      gD    = r.getGroup("a", "d");
      subjA = r.getSubject("a");
      subjB = r.getSubject("b");
      subjC = r.getSubject("c");
      memberA = MemberFinder.findBySubject(r.rs, subjA, true);

      GroupType customType  = GroupType.createType(r.rs, "customType");
      gB.addType(customType);
      fieldCustom1 = customType.addList(r.rs, "customField1", AccessPrivilege.READ, AccessPrivilege.UPDATE);
      fieldCustom2 = customType.addList(r.rs, "customField2", AccessPrivilege.READ, AccessPrivilege.UPDATE);
      fieldMembers = Group.getDefaultList();
      fieldUpdaters = FieldFinder.find("updaters", true);

      // initial data
      gA.addMember(gB.toSubject());
      gB.addMember(gA.toSubject());
      gA.addMember(subjA);
      gA.addMember(subjC);
      gB.addMember(subjA);
      gB.addMember(subjB);
      gA.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);
      gC.addMember(gA.toSubject());
      gD.addMember(gB.toSubject());
      gB.addMember(gB.toSubject(), fieldCustom1);
      gB.addMember(gA.toSubject(), fieldCustom2);


      // Remove gB -> gA
      MemberOf mof = new DefaultMemberOf();
      Membership ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        gA.getUuid(), gB.toMember().getUuid(), Group.getDefaultList(), Membership.IMMEDIATE, true);
      mof.deleteImmediate(r.rs, gA, ms, gB.toMember());
      assertEquals("mof deletes", 12, mof.getDeletes().size());
      assertEquals("mof saves", 0, mof.getSaves().size());

      // Remove gA -> gA (update priv)
      mof = new DefaultMemberOf();
      ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        gA.getUuid(), gA.toMember().getUuid(), fieldUpdaters, Membership.IMMEDIATE, true);
      mof.deleteImmediate(r.rs, gA, ms, gA.toMember());
      assertEquals("mof deletes", 6, mof.getDeletes().size());
      assertEquals("mof saves", 0, mof.getSaves().size());

      // Remove gB -> gB (custom field 1)
      mof = new DefaultMemberOf();
      ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        gB.getUuid(), gB.toMember().getUuid(), fieldCustom1, Membership.IMMEDIATE, true);
      mof.deleteImmediate(r.rs, gB, ms, gB.toMember());
      assertEquals("mof deletes", 6, mof.getDeletes().size());
      assertEquals("mof saves", 0, mof.getSaves().size());

      // Remove gA -> gB (custom field 2)
      mof = new DefaultMemberOf();
      ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        gB.getUuid(), gA.toMember().getUuid(), fieldCustom2, Membership.IMMEDIATE, true);
      mof.deleteImmediate(r.rs, gB, ms, gA.toMember());
      assertEquals("mof deletes", 6, mof.getDeletes().size());
      assertEquals("mof saves", 0, mof.getSaves().size());

      // Remove gB -> gD
      mof = new DefaultMemberOf();
      ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        gD.getUuid(), gB.toMember().getUuid(), Group.getDefaultList(), Membership.IMMEDIATE, true);
      mof.deleteImmediate(r.rs, gD, ms, gB.toMember());
      assertEquals("mof deletes", 6, mof.getDeletes().size());
      assertEquals("mof saves", 0, mof.getSaves().size());

      // Remove SA -> gB
      mof = new DefaultMemberOf();
      ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        gB.getUuid(), memberA.getUuid(), Group.getDefaultList(), Membership.IMMEDIATE, true);
      mof.deleteImmediate(r.rs, gB, ms, memberA);
      assertEquals("mof deletes", 7, mof.getDeletes().size());
      assertEquals("mof saves", 0, mof.getSaves().size());

      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  }
}

