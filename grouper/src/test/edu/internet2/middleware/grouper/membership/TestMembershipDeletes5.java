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

import junit.framework.TestCase;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.R;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * @author Shilen Patel.
 */
public class TestMembershipDeletes5 extends GrouperTest {

  private static final Log LOG = GrouperUtil.getLog(TestMembershipDeletes5.class);

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

  protected void setUp() {
    super.setUp();

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
  }

  protected void tearDown () {
    super.tearDown();
    
    if (r != null) {
      r.rs.stop();
    }
  }

  public void testMembershipDeletes5a() {
    
    // initial data
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

    int beforeCount = GrouperDAOFactory.getFactory().getMembership().findAll(true).size();
    gA.addMember(gB.toSubject());

    // Remove gB -> gA
    gA.deleteMember(gB.toSubject());
    int afterCount = GrouperDAOFactory.getFactory().getMembership().findAll(true).size();
    assertEquals("membership changes after add and delete", 0, afterCount - beforeCount);
  }

  public void testMembershipDeletes5b() {
    
    // initial data
    gA.addMember(gB.toSubject());
    gB.addMember(gA.toSubject());
    gA.addMember(subjA);
    gA.addMember(subjC);
    gB.addMember(subjA);
    gB.addMember(subjB);
    gC.addMember(gA.toSubject());
    gD.addMember(gB.toSubject());
    gB.addMember(gB.toSubject(), fieldCustom1);
    gB.addMember(gA.toSubject(), fieldCustom2);

    int beforeCount = GrouperDAOFactory.getFactory().getMembership().findAll(true).size();
    gA.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);

    // Remove gA -> gA (update priv)
    gA.revokePriv(gA.toSubject(), AccessPrivilege.UPDATE);
    int afterCount = GrouperDAOFactory.getFactory().getMembership().findAll(true).size();
    assertEquals("membership changes after add and delete", 0, afterCount - beforeCount);
  }
  
  public void testMembershipDeletes5c() {
    
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
    gB.addMember(gA.toSubject(), fieldCustom2);

    int beforeCount = GrouperDAOFactory.getFactory().getMembership().findAll(true).size();
    gB.addMember(gB.toSubject(), fieldCustom1);

    // Remove gB -> gB (custom field 1)
    gB.deleteMember(gB.toSubject(), fieldCustom1);
    int afterCount = GrouperDAOFactory.getFactory().getMembership().findAll(true).size();
    assertEquals("membership changes after add and delete", 0, afterCount - beforeCount);
  }
  
  public void testMembershipDeletes5d() {
    
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

    int beforeCount = GrouperDAOFactory.getFactory().getMembership().findAll(true).size();
    gB.addMember(gA.toSubject(), fieldCustom2);

    // Remove gA -> gB (custom field 2)
    gB.deleteMember(gA.toSubject(), fieldCustom2);
    int afterCount = GrouperDAOFactory.getFactory().getMembership().findAll(true).size();
    assertEquals("membership changes after add and delete", 0, afterCount - beforeCount);
  }
  
  public void testMembershipDeletes5e() {
    
    // initial data
    gA.addMember(gB.toSubject());
    gB.addMember(gA.toSubject());
    gA.addMember(subjA);
    gA.addMember(subjC);
    gB.addMember(subjA);
    gB.addMember(subjB);
    gA.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);
    gC.addMember(gA.toSubject());
    gB.addMember(gB.toSubject(), fieldCustom1);
    gB.addMember(gA.toSubject(), fieldCustom2);

    int beforeCount = GrouperDAOFactory.getFactory().getMembership().findAll(true).size();
    gD.addMember(gB.toSubject());    

    // Remove gB -> gD
    gD.deleteMember(gB.toSubject());
    int afterCount = GrouperDAOFactory.getFactory().getMembership().findAll(true).size();
    assertEquals("membership changes after add and delete", 0, afterCount - beforeCount);
  }
  
  public void testMembershipDeletes5f() {
    
    // initial data
    gA.addMember(gB.toSubject());
    gB.addMember(gA.toSubject());
    gA.addMember(subjA);
    gA.addMember(subjC);
    gB.addMember(subjB);
    gA.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);
    gC.addMember(gA.toSubject());
    gD.addMember(gB.toSubject());
    gB.addMember(gB.toSubject(), fieldCustom1);
    gB.addMember(gA.toSubject(), fieldCustom2);

    int beforeCount = GrouperDAOFactory.getFactory().getMembership().findAll(true).size();
    gB.addMember(subjA);

    // Remove SA -> gB
    gB.deleteMember(subjA);
    int afterCount = GrouperDAOFactory.getFactory().getMembership().findAll(true).size();
    assertEquals("membership changes after add and delete", 0, afterCount - beforeCount);
  }
}

