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
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.RevokePrivilegeAlreadyRevokedException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.MembershipTestHelper;
import edu.internet2.middleware.grouper.helper.R;
import edu.internet2.middleware.grouper.helper.T;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * @author Shilen Patel.
 */
public class TestMembership11 extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new TestMembership11("testNestedComposites"));
  }
  
  private static final Log LOG = GrouperUtil.getLog(TestMembership11.class);

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
  Subject all;

  Field fieldMembers;
  Field fieldUpdaters;

  /**
   * @param name
   */
  public TestMembership11(String name) {
    super(name);
  }

  /**
   * 
   */
  public void testNestedComposites() {
    LOG.info("testNestedComposites");
    try {
      //sleep so if auto added members in config check, doesnt mess things up here
      GrouperUtil.sleep(50);
//      before   = DateHelper.getPastDate();
      before   = new Date();

      r     = R.populateRegistry(1, 14, 5);
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
      all   = SubjectFinder.findAllSubject();

      fieldMembers = Group.getDefaultList();
      fieldUpdaters = FieldFinder.find(Field.FIELD_NAME_UPDATERS, true);

      // Test 1
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

      verifyMemberships();
      deleteMemberships();

      // Test 2
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
      gA.addCompositeMember(CompositeType.INTERSECTION, gB, gC);
      
      verifyMemberships();
      deleteMemberships();

      // Test 3
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
      gA.addCompositeMember(CompositeType.INTERSECTION, gB, gC);
      gB.addMember(gD.toSubject());
      
      verifyMemberships();
      deleteMemberships();

      // Test 4
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
      gA.addCompositeMember(CompositeType.INTERSECTION, gB, gC);
      gB.addMember(gD.toSubject());
      gC.addMember(subjB);
      
      verifyMemberships();
      deleteMemberships();

      // Test 5
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
      gA.addCompositeMember(CompositeType.INTERSECTION, gB, gC);
      gB.addMember(gD.toSubject());
      gC.addMember(subjB);
      gC.addMember(subjC);
      
      verifyMemberships();
      deleteMemberships();

      // Test 6
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
      gA.addCompositeMember(CompositeType.INTERSECTION, gB, gC);
      gB.addMember(gD.toSubject());
      gC.addMember(subjB);
      gC.addMember(subjC);
      gC.addMember(subjD);
      
      verifyMemberships();
      deleteMemberships();

      // Test 7
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
      gA.addCompositeMember(CompositeType.INTERSECTION, gB, gC);
      gB.addMember(gD.toSubject());
      gC.addMember(subjB);
      gC.addMember(subjC);
      gC.addMember(subjD);
      gD.addMember(gE.toSubject());
      
      verifyMemberships();
      deleteMemberships();

      // Test 8
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
      gA.addCompositeMember(CompositeType.INTERSECTION, gB, gC);
      gB.addMember(gD.toSubject());
      gC.addMember(subjB);
      gC.addMember(subjC);
      gC.addMember(subjD);
      gD.addMember(gE.toSubject());
      gE.addCompositeMember(CompositeType.UNION, gF, gG);
      
      verifyMemberships();
      deleteMemberships();

      // Test 9
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
      gA.addCompositeMember(CompositeType.INTERSECTION, gB, gC);
      gB.addMember(gD.toSubject());
      gC.addMember(subjB);
      gC.addMember(subjC);
      gC.addMember(subjD);
      gD.addMember(gE.toSubject());
      gE.addCompositeMember(CompositeType.UNION, gF, gG);
      gF.addMember(subjD);
      
      verifyMemberships();
      deleteMemberships();

      // Test 10
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
      gA.addCompositeMember(CompositeType.INTERSECTION, gB, gC);
      gB.addMember(gD.toSubject());
      gC.addMember(subjB);
      gC.addMember(subjC);
      gC.addMember(subjD);
      gD.addMember(gE.toSubject());
      gE.addCompositeMember(CompositeType.UNION, gF, gG);
      gF.addMember(subjD);
      gG.addMember(subjE);
      
      verifyMemberships();
      deleteMemberships();

      // Test 11
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
      
      verifyMemberships();
      deleteMemberships();

      // Test 12
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
      
      verifyMemberships();
      deleteMemberships();

      // Test 13
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
      
      verifyMemberships();
      deleteMemberships();

      // Test 14
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
      
      verifyMemberships();
      deleteMemberships();

      // Test 15
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
      
      verifyMemberships();
      deleteMemberships();

      // Test 16
      gI.addMember(all);
      gJ.addCompositeMember(CompositeType.INTERSECTION, gK, gL);
      gK.addMember(subjA);
      gK.addMember(subjB);
      gL.addMember(gM.toSubject());
      gM.addMember(subjA);
      gM.addMember(gN.toSubject());
      gN.addMember(subjA);
      gN.addMember(subjB);
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
      
      verifyMemberships();
      deleteMemberships();

      // Test 17
      gJ.addCompositeMember(CompositeType.INTERSECTION, gK, gL);
      gK.addMember(subjA);
      gK.addMember(subjB);
      gL.addMember(gM.toSubject());
      gM.addMember(subjA);
      gM.addMember(gN.toSubject());
      gN.addMember(subjA);
      gN.addMember(subjB);
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
      
      verifyMemberships();
      deleteMemberships();

      // Test 18
      gK.addMember(subjA);
      gK.addMember(subjB);
      gL.addMember(gM.toSubject());
      gM.addMember(subjA);
      gM.addMember(gN.toSubject());
      gN.addMember(subjA);
      gN.addMember(subjB);
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
      
      verifyMemberships();
      deleteMemberships();

      // Test 19
      gK.addMember(subjB);
      gL.addMember(gM.toSubject());
      gM.addMember(subjA);
      gM.addMember(gN.toSubject());
      gN.addMember(subjA);
      gN.addMember(subjB);
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
      
      verifyMemberships();
      deleteMemberships();

      // Test 20
      gL.addMember(gM.toSubject());
      gM.addMember(subjA);
      gM.addMember(gN.toSubject());
      gN.addMember(subjA);
      gN.addMember(subjB);
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
      
      verifyMemberships();
      deleteMemberships();

      // Test 21
      gM.addMember(subjA);
      gM.addMember(gN.toSubject());
      gN.addMember(subjA);
      gN.addMember(subjB);
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
      
      verifyMemberships();
      deleteMemberships();

      // Test 22
      gM.addMember(gN.toSubject());
      gN.addMember(subjA);
      gN.addMember(subjB);
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
      
      verifyMemberships();
      deleteMemberships();

      // Test 23
      gN.addMember(subjA);
      gN.addMember(subjB);
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
      
      verifyMemberships();
      deleteMemberships();

      // Test 24
      gN.addMember(subjB);
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
      
      verifyMemberships();

      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  }

  /**
   * @throws Exception
   */
  public  void verifyMemberships() throws Exception {

    // gA should have two members only
    T.amount("Verify number of memberships for gA", 2, gA.getCompositeMemberships().size());
    Assert.assertTrue("Verify SC -> gA", gA.hasMember(subjC));
    Assert.assertTrue("Verify SD -> gA", gA.hasMember(subjD));

    // gE should have five members only
    T.amount("Verify number of memberships for gE", 4, gE.getCompositeMemberships().size());
    Assert.assertTrue("Verify SC -> gE", gE.hasMember(subjC));
    Assert.assertTrue("Verify SD -> gE", gE.hasMember(subjD));
    Assert.assertTrue("Verify SE -> gE", gE.hasMember(subjE));
    Assert.assertTrue("Verify all -> gE", gE.hasMember(all));

    // gH should have two members only
    T.amount("Verify number of memberships for gH", 2, gH.getCompositeMemberships().size());
    Assert.assertTrue("Verify SC -> gH", gH.hasMember(subjC));
    Assert.assertTrue("Verify all -> gH", gH.hasMember(all));

    // gJ should have two members only
    T.amount("Verify number of memberships for gJ", 2, gJ.getCompositeMemberships().size());
    Assert.assertTrue("Verify SA -> gJ", gJ.hasMember(subjB));
    Assert.assertTrue("Verify SB -> gJ", gJ.hasMember(subjB));

    // gH should have 2 effective update privileges
    T.amount("Verify number of effective update privileges for gH", 2, gH.getEffectiveMemberships(fieldUpdaters).size());
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "all -> gH", gH, all, gH, 1, gH, gH.toSubject(), null, 0, fieldUpdaters);
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "SC -> gH", gH, subjC, gH, 1, gH, gH.toSubject(), null, 0, fieldUpdaters);

    // gG should have 2 effective privileges
    T.amount("Verify number of effective privileges for gG", 2, gG.getEffectiveMemberships().size());
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "all -> gG", gG, all, gH, 1, gG, gH.toSubject(), null, 0, fieldMembers);
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "SC -> gG", gG, subjC, gH, 1, gG, gH.toSubject(), null, 0, fieldMembers);

    // gD should have 5 effective privileges
    T.amount("Verify number of effective privileges for gD", 4, gD.getEffectiveMemberships().size());
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "all -> gD", gD, all, gE, 1, gD, gE.toSubject(), null, 0, fieldMembers);
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "SC -> gD", gD, subjC, gE, 1, gD, gE.toSubject(), null, 0, fieldMembers);
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "SD -> gD", gD, subjD, gE, 1, gD, gE.toSubject(), null, 0, fieldMembers);
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "SE -> gD", gD, subjE, gE, 1, gD, gE.toSubject(), null, 0, fieldMembers);

    // gB should have 6 effective privileges
    T.amount("Verify number of effective privileges for gB", 5, gB.getEffectiveMemberships().size());
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "gE -> gB", gB, gE.toSubject(), gD, 1, gB, gD.toSubject(), null, 0, fieldMembers);
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "all -> gB", gB, all, gE, 2, gB, gE.toSubject(), gD, 1, fieldMembers);
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "SC -> gB", gB, subjC, gE, 2, gB, gE.toSubject(), gD, 1, fieldMembers);
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "SD -> gB", gB, subjD, gE, 2, gB, gE.toSubject(), gD, 1, fieldMembers);
    MembershipTestHelper.verifyEffectiveMembership(r.rs, "SE -> gB", gB, subjE, gE, 2, gB, gE.toSubject(), gD, 1, fieldMembers);

    // verify the total number of list memberships
    Set<Membership> listMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldMembers);
    T.amount("Number of list memberships", 46, listMemberships.size());

    // verify the total number of update privileges
    Set<Membership> updateMemberships = MembershipFinder.internal_findAllByCreatedAfter(r.rs, before, fieldUpdaters);
    T.amount("Number of update privileges", 3, updateMemberships.size());
  }

  /**
   * @throws Exception
   */
  public void deleteMemberships() throws Exception {
    gA.deleteCompositeMember();
    gB.deleteMember(gD.toSubject());
    gC.deleteMember(subjB);
    gC.deleteMember(subjC);
    gC.deleteMember(subjD);
    gD.deleteMember(gE.toSubject());
    gE.deleteCompositeMember();
    gF.deleteMember(subjD);
    gG.deleteMember(subjE);
    gG.deleteMember(gH.toSubject());
    
    gH.revokePriv(gH.toSubject(), AccessPrivilege.UPDATE);

    //try this again
    try {
      gH.revokePriv(gH.toSubject(), AccessPrivilege.UPDATE);
      fail("Should throw already revoked exception");
    } catch (RevokePrivilegeAlreadyRevokedException rpare) {
      //good
    }

    assertFalse(gH.revokePriv(gH.toSubject(), AccessPrivilege.UPDATE, false));

    
    gH.deleteCompositeMember();
    gI.deleteMember(subjA);
    gI.deleteMember(subjB);
    gI.deleteMember(subjC);
    gI.deleteMember(all);
    gJ.deleteCompositeMember();
    gK.deleteMember(subjA);
    gK.deleteMember(subjB);
    gL.deleteMember(gM.toSubject());
    gM.deleteMember(subjA);
    gM.deleteMember(gN.toSubject());
    gN.deleteMember(subjA);
    gN.deleteMember(subjB);
  }

}

