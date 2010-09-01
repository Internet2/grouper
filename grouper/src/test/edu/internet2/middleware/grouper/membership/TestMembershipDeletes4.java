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

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.R;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * @author Shilen Patel.
 */
public class TestMembershipDeletes4 extends GrouperTest {

  private static final Log LOG = GrouperUtil.getLog(TestMembershipDeletes4.class);

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
    super.setUp();
    r     = R.populateRegistry(1, 5, 1);
    gA    = r.getGroup("a", "a");
    gB    = r.getGroup("a", "b");
    gC    = r.getGroup("a", "c");
    gD    = r.getGroup("a", "d");
    gE    = r.getGroup("a", "e");
    subjA = r.getSubject("a");
    memberA = MemberFinder.findBySubject(r.rs, subjA, true);
  }

  protected void tearDown () {
    LOG.debug("tearDown");
    
    if (r != null) {
      r.rs.stop();
    }
  }

  public void testMembershipDeletes4a() {
    
    // initial data
    gA.addMember(gB.toSubject());
    gB.addMember(gC.toSubject());
    gC.addMember(gA.toSubject());
    gD.addMember(gC.toSubject());
    gC.addMember(subjA);
    gB.grantPriv(gC.toSubject(), AccessPrivilege.OPTIN);
    gD.grantPriv(gC.toSubject(), AccessPrivilege.UPDATE);
    gC.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);

    int beforeCount = GrouperDAOFactory.getFactory().getMembership().findAll(true).size();
    gE.addMember(gD.toSubject());
    
    // Remove gD -> gE
    gE.deleteMember(gD.toSubject());
    int afterCount = GrouperDAOFactory.getFactory().getMembership().findAll(true).size();
    assertEquals("membership changes after add and delete", 0, afterCount - beforeCount);
  }
  
  public void testMembershipDeletes4b() {
    
    // initial data
    gA.addMember(gB.toSubject());
    gB.addMember(gC.toSubject());
    gC.addMember(gA.toSubject());
    gC.addMember(subjA);
    gB.grantPriv(gC.toSubject(), AccessPrivilege.OPTIN);
    gE.addMember(gD.toSubject());
    gD.grantPriv(gC.toSubject(), AccessPrivilege.UPDATE);
    gC.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);

    int beforeCount = GrouperDAOFactory.getFactory().getMembership().findAll(true).size();
    gD.addMember(gC.toSubject());
    
    // Remove gC -> gD
    gD.deleteMember(gC.toSubject());
    int afterCount = GrouperDAOFactory.getFactory().getMembership().findAll(true).size();
    assertEquals("membership changes after add and delete", 0, afterCount - beforeCount);
  }
  
  public void testMembershipDeletes4c() {
    
    // initial data
    gA.addMember(gB.toSubject());
    gB.addMember(gC.toSubject());
    gC.addMember(gA.toSubject());
    gD.addMember(gC.toSubject());
    gC.addMember(subjA);
    gB.grantPriv(gC.toSubject(), AccessPrivilege.OPTIN);
    gE.addMember(gD.toSubject());
    gC.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);

    int beforeCount = GrouperDAOFactory.getFactory().getMembership().findAll(true).size();
    gD.grantPriv(gC.toSubject(), AccessPrivilege.UPDATE);
    
    // Remove gC -> gD (update priv)
    gD.revokePriv(gC.toSubject(), AccessPrivilege.UPDATE);
    int afterCount = GrouperDAOFactory.getFactory().getMembership().findAll(true).size();
    assertEquals("membership changes after add and delete", 0, afterCount - beforeCount);
  }
  
  public void testMembershipDeletes4d() {
    
    // initial data
    gA.addMember(gB.toSubject());
    gB.addMember(gC.toSubject());
    gD.addMember(gC.toSubject());
    gC.addMember(subjA);
    gB.grantPriv(gC.toSubject(), AccessPrivilege.OPTIN);
    gE.addMember(gD.toSubject());
    gD.grantPriv(gC.toSubject(), AccessPrivilege.UPDATE);
    gC.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);

    int beforeCount = GrouperDAOFactory.getFactory().getMembership().findAll(true).size();
    gC.addMember(gA.toSubject());

    // Remove gA -> gC
    gC.deleteMember(gA.toSubject());
    int afterCount = GrouperDAOFactory.getFactory().getMembership().findAll(true).size();
    assertEquals("membership changes after add and delete", 0, afterCount - beforeCount);
  }
  
  public void testMembershipDeletes4e() {
    
    // initial data
    gA.addMember(gB.toSubject());
    gB.addMember(gC.toSubject());
    gC.addMember(gA.toSubject());
    gD.addMember(gC.toSubject());
    gB.grantPriv(gC.toSubject(), AccessPrivilege.OPTIN);
    gE.addMember(gD.toSubject());
    gD.grantPriv(gC.toSubject(), AccessPrivilege.UPDATE);
    gC.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);

    int beforeCount = GrouperDAOFactory.getFactory().getMembership().findAll(true).size();
    gC.addMember(subjA);

    // Remove SA -> gC
    gC.deleteMember(subjA);
    int afterCount = GrouperDAOFactory.getFactory().getMembership().findAll(true).size();
    assertEquals("membership changes after add and delete", 0, afterCount - beforeCount);
  }
  
  public void testMembershipDeletes4f() {
    
    // initial data
    gA.addMember(gB.toSubject());
    gB.addMember(gC.toSubject());
    gD.addMember(gC.toSubject());
    gC.addMember(subjA);
    gB.grantPriv(gC.toSubject(), AccessPrivilege.OPTIN);
    gE.addMember(gD.toSubject());
    gD.grantPriv(gC.toSubject(), AccessPrivilege.UPDATE);
    gC.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);

    int beforeCount = GrouperDAOFactory.getFactory().getMembership().findAll(true).size();
    gC.addMember(gA.toSubject());

    // Remove gA -> gC (update priv)
    gC.deleteMember(gA.toSubject());
    int afterCount = GrouperDAOFactory.getFactory().getMembership().findAll(true).size();
    assertEquals("membership changes after add and delete", 0, afterCount - beforeCount);
  }
  
  public void testMembershipDeletes4g() {
    
    // initial data
    gB.addMember(gC.toSubject());
    gC.addMember(gA.toSubject());
    gD.addMember(gC.toSubject());
    gC.addMember(subjA);
    gB.grantPriv(gC.toSubject(), AccessPrivilege.OPTIN);
    gE.addMember(gD.toSubject());
    gD.grantPriv(gC.toSubject(), AccessPrivilege.UPDATE);
    gC.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);

    int beforeCount = GrouperDAOFactory.getFactory().getMembership().findAll(true).size();
    gA.addMember(gB.toSubject());

    // Remove gB -> gA
    gA.deleteMember(gB.toSubject());
    int afterCount = GrouperDAOFactory.getFactory().getMembership().findAll(true).size();
    assertEquals("membership changes after add and delete", 0, afterCount - beforeCount);
  }
  
  public void testMembershipDeletes4h() {
    
    // initial data
    gA.addMember(gB.toSubject());
    gC.addMember(gA.toSubject());
    gD.addMember(gC.toSubject());
    gC.addMember(subjA);
    gB.grantPriv(gC.toSubject(), AccessPrivilege.OPTIN);
    gE.addMember(gD.toSubject());
    gD.grantPriv(gC.toSubject(), AccessPrivilege.UPDATE);
    gC.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);

    int beforeCount = GrouperDAOFactory.getFactory().getMembership().findAll(true).size();
    gB.addMember(gC.toSubject());

    // Remove gC -> gB
    gB.deleteMember(gC.toSubject());
    int afterCount = GrouperDAOFactory.getFactory().getMembership().findAll(true).size();
    assertEquals("membership changes after add and delete", 0, afterCount - beforeCount);
  }
  
  public void testMembershipDeletes4i() {
    
    // initial data
    gA.addMember(gB.toSubject());
    gB.addMember(gC.toSubject());
    gC.addMember(gA.toSubject());
    gD.addMember(gC.toSubject());
    gC.addMember(subjA);
    gE.addMember(gD.toSubject());
    gD.grantPriv(gC.toSubject(), AccessPrivilege.UPDATE);
    gC.grantPriv(gA.toSubject(), AccessPrivilege.UPDATE);

    int beforeCount = GrouperDAOFactory.getFactory().getMembership().findAll(true).size();
    gB.grantPriv(gC.toSubject(), AccessPrivilege.OPTIN);
    
    // Remove gC -> gB (opt-in)
    gB.revokePriv(gC.toSubject(), AccessPrivilege.OPTIN);
    int afterCount = GrouperDAOFactory.getFactory().getMembership().findAll(true).size();
    assertEquals("membership changes after add and delete", 0, afterCount - beforeCount);
  }
}
