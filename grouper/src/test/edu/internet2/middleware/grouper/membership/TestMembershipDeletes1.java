/**
 * Copyright 2012 Internet2
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

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.helper.DateHelper;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.R;
import edu.internet2.middleware.grouper.helper.T;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * @author Shilen Patel.
 */
public class TestMembershipDeletes1 extends GrouperTest {

  private static final Log LOG = GrouperUtil.getLog(TestMembershipDeletes1.class);

  Date before;
  R       r;
  Group   gA;
  Group   gB;
  Group   gC;
  Group   gD;
  Group   gE;
  Group   gF;
  Group   gG;
  Group   gH;
  Group   gI;
  Group   gJ;
  Group   gK;
  Group   gL;
  Group   gM;
  Group   gN;
  Subject subjA;
  Subject subjB;
  Subject subjC;
  Subject subjD;
  Subject subjE;
  Subject subjFDel;
  Member  memberA;
  Member  memberD;
  Subject all;

  Field fieldMembers;
  Field fieldUpdaters;

  public TestMembershipDeletes1(String name) {
    super(name);
  }

  public void testMembershipDeletes1() {
    LOG.info("testMembershipDeletes1");
    try {
      GrouperUtil.sleep(100);
      before  = new Date();
      GrouperUtil.sleep(100);

      r     = R.populateRegistry(1, 14, 6);
      gA    = r.getGroup("a", "a");
      gB    = r.getGroup("a", "b");
      gC    = r.getGroup("a", "c");
      gD    = r.getGroup("a", "d");
      gE    = r.getGroup("a", "e");
      gF    = r.getGroup("a", "f");
      gG    = r.getGroup("a", "g");
      gH    = r.getGroup("a", "h");
      gI    = r.getGroup("a", "i");
      gJ    = r.getGroup("a", "j");
      gK    = r.getGroup("a", "k");
      gL    = r.getGroup("a", "l");
      gM    = r.getGroup("a", "m");
      gN    = r.getGroup("a", "n");
      subjA = r.getSubject("a");
      subjB = r.getSubject("b");
      subjC = r.getSubject("c");
      subjD = r.getSubject("d");
      subjE = r.getSubject("e");
      subjFDel = r.getSubject("f");
      all   = SubjectFinder.findAllSubject();
      memberA = MemberFinder.findBySubject(r.rs, subjA, true);
      memberD = MemberFinder.findBySubject(r.rs, subjD, true);

      fieldMembers = Group.getDefaultList();
      fieldUpdaters = FieldFinder.find(Field.FIELD_NAME_UPDATERS, true);

      // initial data
      gA.addCompositeMember(CompositeType.INTERSECTION, gB, gC);
      gB.addMember(gD.toSubject());
      gC.addMember(subjB);
      gC.addMember(subjC);
      gC.addMember(subjD);
      gD.addMember(gE.toSubject());
      gE.addCompositeMember(CompositeType.UNION, gF, gG);
      gF.addMember(subjD);
      gG.addMember(subjE);
      gG.addMember(gH.toSubject());
      gH.grantPriv(gH.toSubject(), AccessPrivilege.UPDATE);
      gH.addCompositeMember(CompositeType.COMPLEMENT, gI, gJ);
      gI.addMember(subjA);
      gI.addMember(subjB);
      gI.addMember(subjC);
      gI.addMember(all);
      gJ.addCompositeMember(CompositeType.INTERSECTION, gK, gL);
      gK.addMember(subjA);
      gK.addMember(subjB);
      gL.addMember(gM.toSubject());
      gM.addMember(subjA);
      gM.addMember(gN.toSubject());
      gN.addMember(subjA);
      gN.addMember(subjB);

      // Remove SA -> gM (causes 2 membership deletes)
      int beforeCount = GrouperDAOFactory.getFactory().getMembership().findAll(true).size();
      gM.deleteMember(subjA);
      int afterCount = GrouperDAOFactory.getFactory().getMembership().findAll(true).size();
      assertEquals("membership changes", -2, afterCount - beforeCount);
      gM.addMember(subjA);
      
      // Remove gM -> gL (causes 13 membership adds and 7 membership deletes)
      beforeCount = GrouperDAOFactory.getFactory().getMembership().findAll(true).size();
      gL.deleteMember(gM.toSubject());
      afterCount = GrouperDAOFactory.getFactory().getMembership().findAll(true).size();
      assertEquals("membership changes", 6, afterCount - beforeCount);
      gL.addMember(gM.toSubject());

      // Add and remove SD -> gG (remove causes 1 membership delete)
      gG.addMember(subjD);
      beforeCount = GrouperDAOFactory.getFactory().getMembership().findAll(true).size();
      gG.deleteMember(subjD);
      afterCount = GrouperDAOFactory.getFactory().getMembership().findAll(true).size();
      assertEquals("membership changes", -1, afterCount - beforeCount);
      
      // add and remove SF -> gI
      gI.addMember(subjFDel);
      gI.deleteMember(subjFDel);
      T.amount("Number of list memberships", 46, MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldMembers).size());
      T.amount("Number of update privileges", 3, MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldUpdaters).size());

      // remove composite gJ
      gJ.deleteCompositeMember();
      T.amount("Number of list memberships", 55, MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldMembers).size());
      T.amount("Number of update privileges", 5, MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldUpdaters).size());
      gJ.addCompositeMember(CompositeType.INTERSECTION, gK, gL);

      // remove composite gH
      gH.deleteCompositeMember();
      T.amount("Number of list memberships", 35, MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldMembers).size());
      T.amount("Number of update privileges", 1, MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldUpdaters).size());
      gH.addCompositeMember(CompositeType.COMPLEMENT, gI, gJ);

      // remove composite gE
      gE.deleteCompositeMember();
      T.amount("Number of list memberships", 32, MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldMembers).size());
      T.amount("Number of update privileges", 3, MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldUpdaters).size());
      gE.addCompositeMember(CompositeType.UNION, gF, gG);

      // remove composite gA
      gA.deleteCompositeMember();
      T.amount("Number of list memberships", 44, MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldMembers).size());
      T.amount("Number of update privileges", 3, MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldUpdaters).size());
      gA.addCompositeMember(CompositeType.INTERSECTION, gB, gC);

      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  }
}

