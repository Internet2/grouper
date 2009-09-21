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

package edu.internet2.middleware.grouper.member;
import java.util.Iterator;

import junit.framework.Assert;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.GrantPrivilegeException;
import edu.internet2.middleware.grouper.exception.GroupAddException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.exception.SessionException;
import edu.internet2.middleware.grouper.exception.StemAddException;
import edu.internet2.middleware.grouper.helper.GroupHelper;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.R;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.helper.T;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;

/**
 * Test {@link Member}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestMember.java,v 1.2 2009-09-21 06:14:27 mchyzer Exp $
 */
public class TestMember extends GrouperTest {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(TestMember.class);

  /**
   * 
   * @param name
   */
  public TestMember(String name) {
    super(name);
  }

  /**
   * 
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.internal_resetRegistryAndAddTestSubjects();
  }

  protected void tearDown () {
    LOG.debug("tearDown");
  }


  // Tests

  public void testGetSource() {
    LOG.info("testGetSource");
    GrouperSession s = SessionHelper.getRootSession();
    try {
      // I'm not sure what to test on source retrieval
      Member  m   = s.getMember();
      Source  src = m.getSubjectSource();
      Assert.assertNotNull("src !null", src);
      Assert.assertTrue("src instanceof Source", src instanceof Source);
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  } // public void testGetSource()

  public void testGetMembershipsAndGroups() {
    LOG.info("testGetMembershipsAndGroups");
    try {
      Subject         subj  = SubjectTestHelper.SUBJ0;
      GrouperSession  s     = SessionHelper.getRootSession();
      Stem            root  = StemFinder.findRootStem(s);
      Stem            edu   = root.addChildStem("edu", "edu");
      Group           i2    = edu.addChildGroup("i2", "i2");
      Group           uofc  = edu.addChildGroup("uofc", "uofc");
      GroupHelper.addMember(uofc, subj, "members");
      GroupHelper.addMember(i2, uofc.toSubject(), "members");
      Member          m     = MemberFinder.findBySubject(s, subj, true);

      Field f = FieldFinder.find("members", true);

      // Get mships (by field and without)
      Assert.assertTrue(
        "getMship/!field", m.getMemberships().size() == 2
      );
      Assert.assertTrue(
        "getMship/field", m.getMemberships(f).size() == 2
      );

      // Get effective mships (by field and without)

      // Get immediate mships (by field and without)
      Assert.assertTrue(
        "getImmMship/!field", m.getImmediateMemberships().size() == 1
      );
      Iterator iterIMNF = m.getImmediateMemberships().iterator();
      while (iterIMNF.hasNext()) {
        Membership ms = (Membership) iterIMNF.next();
        Assert.assertTrue("IMNF owner", ms.getGroup().equals(uofc));
        Assert.assertTrue("IMNF member", ms.getMember().equals(m));
        Assert.assertTrue("IMNF field", ms.getList().equals(f));
        Assert.assertTrue("IMNF depth", ms.getDepth() == 0);
      } 
      Assert.assertTrue(
        "getImmMship/field", m.getImmediateMemberships(f).size() == 1
      );
      Iterator iterIMF = m.getImmediateMemberships(f).iterator();
      while (iterIMF.hasNext()) {
        Membership ms = (Membership) iterIMF.next();
        Assert.assertTrue("IMF owner", ms.getGroup().equals(uofc));
        Assert.assertTrue("IMF member", ms.getMember().equals(m));
        Assert.assertTrue("IMF field", ms.getList().equals(f));
        Assert.assertTrue("IMF depth", ms.getDepth() == 0);
      } 

      // Get effective mships (by field and without)
      Assert.assertTrue(
        "getEffMship/!field", m.getEffectiveMemberships().size() == 1
      );
      Iterator iterEMNF = m.getEffectiveMemberships().iterator();
      while (iterEMNF.hasNext()) {
        Membership ms = (Membership) iterEMNF.next();
        Assert.assertTrue("EMNF owner", ms.getGroup().equals(i2));
        Assert.assertTrue("EMNF member", ms.getMember().equals(m));
        Assert.assertTrue("EMNF field", ms.getList().equals(f));
        Assert.assertTrue("EMNF depth", ms.getDepth() == 1);
        Assert.assertTrue("EMNF via", ms.getViaGroup().equals(uofc));
      }
      Assert.assertTrue(
        "getEffMship/field", m.getEffectiveMemberships(f).size() == 1
      );
      Iterator iterEMF = m.getEffectiveMemberships(f).iterator();
      while (iterEMF.hasNext()) {
        Membership ms = (Membership) iterEMF.next();
        Assert.assertTrue("EMF owner", ms.getGroup().equals(i2));
        Assert.assertTrue("EMF member", ms.getMember().equals(m));
        Assert.assertTrue("EMF field", ms.getList().equals(f));
        Assert.assertTrue("EMF depth", ms.getDepth() == 1);
        Assert.assertTrue("EMF via", ms.getViaGroup().equals(uofc));
      }

      // Get groups
      Assert.assertTrue("groups == 2", m.getGroups().size() == 2);
      Iterator iterG = m.getGroups().iterator();
      while (iterG.hasNext()) {
        Group g = (Group) iterG.next();
        if      (g.equals(i2)) {  
          Assert.assertTrue("imm group: i2", true);
        }
        else if (g.equals(uofc)) {
          Assert.assertTrue("imm group: uofc", true);
        }
        else {
          Assert.fail("unknown imm group: " + g.getName());
        }
      }
      // Get immediate groups
      Assert.assertTrue("imm groups == 1", m.getImmediateGroups().size() == 1);
      Iterator iterIG = m.getImmediateGroups().iterator();
      while (iterIG.hasNext()) {
        Group g = (Group) iterIG.next();
        if (g.equals(uofc)) {
          Assert.assertTrue("imm group: uofc", true);
        }
        else {
          Assert.fail("unknown imm group: " + g.getName());
        }
      }
      // Get effective groups
      Assert.assertTrue("eff groups == 1", m.getEffectiveGroups().size() == 1);
      Iterator iterEG = m.getEffectiveGroups().iterator();
      while (iterEG.hasNext()) {
        Group g = (Group) iterEG.next();
        if (g.equals(i2)) {
          Assert.assertTrue("eff group: i2", true);
        }
        else {
          Assert.fail("unknown eff group: " + g.getName());
        }
      }

      // Is member
      Assert.assertTrue("isMem/i2/!field",    m.isMember(i2));
      Assert.assertTrue("isMem/i2/field",     m.isMember(i2, f));
      Assert.assertTrue("isMem/uofc/!field",  m.isMember(uofc));
      Assert.assertTrue("isMem/uofc/field",   m.isMember(uofc, f));
      // Is immediate member
      Assert.assertTrue("isImm/i2/!field",    !m.isImmediateMember(i2));
      Assert.assertTrue("isImm/i2/field",     !m.isImmediateMember(i2, f));
      Assert.assertTrue("isImm/uofc/!field",  m.isImmediateMember(uofc));
      Assert.assertTrue("isImm/uofc/field",   m.isImmediateMember(uofc, f));
      // Is effective member
      Assert.assertTrue("isEff/i2/!field",    m.isEffectiveMember(i2));
      Assert.assertTrue("isEff/i2/field",     m.isEffectiveMember(i2, f));
      Assert.assertTrue("isEff/uofc/!field",  !m.isEffectiveMember(uofc));
      Assert.assertTrue("isEff/uofc/field",   !m.isEffectiveMember(uofc, f));

      s.stop();
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  } // public void testGetMembershipsAndGroups()

  public void testGetAndHasPrivs()
    throws  GrantPrivilegeException,
            GroupAddException,
            InsufficientPrivilegeException,
            MemberNotFoundException,
            SchemaException,
            SessionException,
            StemAddException
  {
    LOG.info("testGetAndHasPrivs");

    Subject         subj  = SubjectTestHelper.SUBJ0;
    Subject         all   = SubjectTestHelper.SUBJA;
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemFinder.findRootStem(s);
    Stem            edu   = root.addChildStem("edu", "edu");
    Group           i2    = edu.addChildGroup("i2", "i2");
    Group           uofc  = edu.addChildGroup("uofc", "uofc");
    GroupHelper.addMember(uofc, subj, "members");
    GroupHelper.addMember(i2, uofc.toSubject(), "members");
    Member          m     = MemberFinder.findBySubject(s, subj, true);
    root.grantPriv( subj, NamingPrivilege.CREATE );
    edu.grantPriv( all, NamingPrivilege.STEM );
    i2.grantPriv( all, AccessPrivilege.OPTIN );
    uofc.grantPriv( subj, AccessPrivilege.UPDATE );

    // Get naming privs
    assertEquals( "getPrivs/root", 1, m.getPrivs(root).size() );
    assertEquals( "getprivs/edu", 1, m.getPrivs(edu).size() ); 

    // Get access privs
    assertEquals( "getprivs/i2", 3, m.getPrivs(i2).size() );
    assertEquals( "getprivs/uofc", 3, m.getPrivs(uofc).size() );

    // Has naming privs
    Assert.assertTrue("hasCreate == 1",   m.hasCreate().size() == 1);
    Assert.assertTrue("hasCreate: root",  m.hasCreate(root));
    Assert.assertTrue("!hasCreate: edu",  !m.hasCreate(edu));

    Assert.assertTrue("hasStem == 1",     m.hasStem().size() == 1);
    Assert.assertTrue("!hasStem: root",   !m.hasStem(root));
    Assert.assertTrue("hasStem: edu",     m.hasStem(edu));

    // Has access privs
    Assert.assertTrue("hasAdmin == 0",    m.hasAdmin().size() == 0);
    Assert.assertTrue("!hasAdmin: i2",    !m.hasAdmin(i2));
    Assert.assertTrue("!hasAdmin: uofc",  !m.hasAdmin(uofc));

    Assert.assertTrue("hasOptin == 1",    m.hasOptin().size() == 1);
    Assert.assertTrue("hasOptin: i2",     m.hasOptin(i2));
    Assert.assertTrue("!hasOptin: uofc",  !m.hasOptin(uofc));

    Assert.assertTrue("hasOptout == 0",   m.hasOptout().size() == 0);
    Assert.assertTrue("!hasOptout: i2",   !m.hasOptout(i2));
    Assert.assertTrue("!hasOptout: uofc", !m.hasOptout(uofc));

    Assert.assertTrue("hasRead == 2",     m.hasRead().size() == 2);
    Assert.assertTrue("hasRead: i2",      m.hasRead(i2));
    Assert.assertTrue("hasRead: uofc",    m.hasRead(uofc));

    Assert.assertTrue("hasUpdate == 1",   m.hasUpdate().size() == 1);
    Assert.assertTrue("!hasUpdate: i2",   !m.hasUpdate(i2));
    Assert.assertTrue("hasUpdate: uofc",  m.hasUpdate(uofc));

    Assert.assertTrue("hasView == 2",     m.hasView().size() == 2);
    Assert.assertTrue("hasView: i2",      m.hasView(i2));
    Assert.assertTrue("hasView: uofc",    m.hasView(uofc));

    s.stop();
  }

  public void testFailCanAdminWhenNoPriv() {
    LOG.info("testFailCanAdminWhenNoPriv");
    try {
      R       r   = R.populateRegistry(1, 1, 1);
      Group   a   = r.getGroup("a", "a");
      Member  m   = MemberFinder.findBySubject(r.rs, r.getSubject("a"), true);
      Assert.assertFalse("OK: cannot admin", m.canAdmin(a));
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailCanAdminWhenNoPriv()

  public void testFailCanAdminWhenNull() {
    LOG.info("testFailCanAdminWhenNull");
    try {
      R       r   = R.populateRegistry(0, 0, 0);
      Member  m   = r.rs.getMember();
      try {
        m.canAdmin(null);
        Assert.fail("FAIL: expected exception");
      }
      catch (IllegalArgumentException eIA) {
        Assert.assertTrue("OK: threw expected exception", true);
      }
      finally {
        r.rs.stop();
      }
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailCanAdminWhenNull()

  public void testFailCanCreateWhenNoPriv() {
    LOG.info("testFailCanCreateWhenNoPriv");
    try {
      R       r   = R.populateRegistry(1, 0, 1);
      Stem    a   = r.getStem("a");
      Member  m   = MemberFinder.findBySubject(r.rs, r.getSubject("a"), true);
      Assert.assertFalse("OK: cannot create", m.canCreate(a));
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailCanCreateWhenNoPriv()

  /**
   * 
   */
  public void testFailCanCreateWhenNull() {
    LOG.info("testFailCanCreateWhenNull");
    try {
      R       r   = R.populateRegistry(0, 0, 0);
      Member  m   = r.rs.getMember();
      try {
        m.canCreate(null);
        Assert.fail("FAIL: expected exception");
      }
      catch (IllegalArgumentException eIA) {
        Assert.assertTrue("OK: threw expected exception", true);
      }
      finally {
        r.rs.stop();
      }
    }
    catch (Exception e) {
      T.e(e);
    }
  }

  public void testFailCanOptinWhenNoPriv() {
    LOG.info("testFailCanOptinWhenNoPriv");
    try {
      R       r   = R.populateRegistry(1, 1, 1);
      Group   a   = r.getGroup("a", "a");
      Member  m   = MemberFinder.findBySubject(r.rs, r.getSubject("a"), true);
      Assert.assertFalse("OK: cannot optin", m.canOptin(a));
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailCanOptinWhenNoPriv()

  public void testFailCanOptinWhenNull() {
    LOG.info("testFailCanOptinWhenNull");
    try {
      R       r   = R.populateRegistry(0, 0, 0);
      Member  m   = r.rs.getMember();
      try {
        m.canOptin(null);
        Assert.fail("FAIL: expected exception");
      }
      catch (IllegalArgumentException eIA) {
        Assert.assertTrue("OK: threw expected exception", true);
      }
      finally {
        r.rs.stop();
      }
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailCanOptinWhenNull()

  public void testFailCanOptoutWhenNoPriv() {
    LOG.info("testFailCanOptoutWhenNoPriv");
    try {
      R       r   = R.populateRegistry(1, 1, 1);
      Group   a   = r.getGroup("a", "a");
      Member  m   = MemberFinder.findBySubject(r.rs, r.getSubject("a"), true);
      Assert.assertFalse("OK: cannot optout", m.canOptout(a));
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailCanOptoutWhenNoPriv()

  public void testFailCanOptoutWhenNull() {
    LOG.info("testFailCanOptoutWhenNull");
    try {
      R       r   = R.populateRegistry(0, 0, 0);
      Member  m   = r.rs.getMember();
      try {
        m.canOptout(null);
        Assert.fail("FAIL: expected exception");
      }
      catch (IllegalArgumentException eIA) {
        Assert.assertTrue("OK: threw expected exception", true);
      }
      finally {
        r.rs.stop();
      }
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailCanOptoutWhenNull()

  public void testFailCanReadWhenNoPriv() {
    LOG.info("testFailCanReadWhenNoPriv");
    try {
      R       r   = R.populateRegistry(1, 1, 1);
      Group   a   = r.getGroup("a", "a");
      a.revokePriv(AccessPrivilege.READ); // Revoke READ from all subjects
      Member  m   = MemberFinder.findBySubject(r.rs, r.getSubject("a"), true);
      Assert.assertFalse("OK: cannot read", m.canRead(a));
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailCanReadWhenNoPriv()

  public void testFailCanReadWhenNull() {
    LOG.info("testFailCanReadWhenNull");
    try {
      R       r   = R.populateRegistry(0, 0, 0);
      Member  m   = r.rs.getMember();
      try {
        m.canAttrRead(null);
        Assert.fail("FAIL: expected exception");
      }
      catch (IllegalArgumentException eIA) {
        Assert.assertTrue("OK: threw expected exception", true);
      }
      finally {
        r.rs.stop();
      }
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailCanReadWhenNull()

  public void testFailCanStemWhenNoPriv() {
    LOG.info("testFailCanStemWhenNoPriv");
    try {
      R       r   = R.populateRegistry(1, 0, 1);
      Stem    a   = r.getStem("a");
      Member  m   = MemberFinder.findBySubject(r.rs, r.getSubject("a"), true);
      Assert.assertFalse("OK: cannot stem", m.canStem(a));
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailCanStemWhenNoPriv()

  public void testFailCanStemWhenNull() {
    LOG.info("testFailCanStemWhenNull");
    try {
      R       r   = R.populateRegistry(0, 0, 0);
      Member  m   = r.rs.getMember();
      try {
        m.canStem(null);
        Assert.fail("FAIL: expected exception");
      }
      catch (IllegalArgumentException eIA) {
        Assert.assertTrue("OK: threw expected exception", true);
      }
      finally {
        r.rs.stop();
      }
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailCanStemWhenNull()

  public void testFailCanUpdateWhenNoPriv() {
    LOG.info("testFailCanUpdateWhenNoPriv");
    try {
      R       r   = R.populateRegistry(1, 1, 1);
      Group   a   = r.getGroup("a", "a");
      Member  m   = MemberFinder.findBySubject(r.rs, r.getSubject("a"), true);
      Assert.assertFalse("OK: cannot update", m.canUpdate(a));
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailCanUpdateWhenNoPriv()

  public void testFailCanUpdateWhenNull() {
    LOG.info("testFailCanUpdateWhenNull");
    try {
      R       r   = R.populateRegistry(0, 0, 0);
      Member  m   = r.rs.getMember();
      try {
        m.canAttrUpdate(null);
        Assert.fail("FAIL: expected exception");
      }
      catch (IllegalArgumentException eIA) {
        Assert.assertTrue("OK: threw expected exception", true);
      }
      finally {
        r.rs.stop();
      }
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailCanUpdateWhenNull()

  public void testFailCanViewWhenNoPriv() {
    LOG.info("testFailCanViewWhenNoPriv");
    try {
      R       r   = R.populateRegistry(1, 1, 1);
      Group   a   = r.getGroup("a", "a");
      a.revokePriv(AccessPrivilege.VIEW); // Revoke VIEW from all subjects
      a.revokePriv(AccessPrivilege.READ); // Revoke READ from all subjects
      Member  m   = MemberFinder.findBySubject(r.rs, r.getSubject("a"), true);
      Assert.assertFalse("OK: cannot view", m.canView(a));
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailCanViewWhenNoPriv()

  public void testFailCanViewWhenNull() {
    LOG.info("testFailCanViewWhenNull");
    try {
      R       r   = R.populateRegistry(0, 0, 0);
      Member  m   = r.rs.getMember();
      try {
        m.canAttrView(null);
        Assert.fail("FAIL: expected exception");
      }
      catch (IllegalArgumentException eIA) {
        Assert.assertTrue("OK: threw expected exception", true);
      }
      finally {
        r.rs.stop();
      }
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testFailCanViewWhenNull()

  /**
   * 
   * @throws Exception
   */
  public void testFindBySubjectId() throws Exception {
    GrouperDAOFactory.getFactory().getMember().findBySubject("GrouperSystem", true);
    GrouperDAOFactory.getFactory().getMember().findBySubject("GrouperSystem", "g:isa", true);
    try {
      GrouperDAOFactory.getFactory().getMember().findBySubject("sflkjlksjflksjdlksd", true);
      fail("Shouldnt find this");
    } catch (MemberNotFoundException snfe) {
      //good
    }
  }

  public void testPassCanAdminWhenRoot() {
    LOG.info("testPassCanAdminWhenRoot");
    try {
      R       r   = R.populateRegistry(1, 1, 0);
      Group   a   = r.getGroup("a", "a");
      Member  m   = r.rs.getMember();
      Assert.assertTrue("OK: can admin", m.canAdmin(a));
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testPassCanAdminWhenRoot()

  public void testPassCanCreateWhenRoot() {
    LOG.info("testPassCanCreateWhenRoot");
    try {
      R       r   = R.populateRegistry(1, 0, 0);
      Stem    a   = r.getStem("a");
      Member  m   = r.rs.getMember();
      Assert.assertTrue("OK: can create", m.canCreate(a));
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testPassCanCreateWhenRoot()

  public void testPassCanOptinWhenRoot() {
    LOG.info("testPassCanOptinWhenRoot");
    try {
      R       r   = R.populateRegistry(1, 1, 0);
      Group   a   = r.getGroup("a", "a");
      Member  m   = r.rs.getMember();
      Assert.assertTrue("OK: can g", m.canOptin(a));
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testPassCanOptinWhenRoot()

  public void testPassCanOptoutWhenRoot() {
    LOG.info("testPassCanOptoutWhenRoot");
    try {
      R       r   = R.populateRegistry(1, 1, 0);
      Group   a   = r.getGroup("a", "a");
      Member  m   = r.rs.getMember();
      Assert.assertTrue("OK: can optout", m.canOptout(a));
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testPassCanOptoutWhenRoot()

  public void testPassCanReadWhenRoot() {
    LOG.info("testPassCanReadWhenRoot");
    try {
      R       r   = R.populateRegistry(1, 1, 0);
      Group   a   = r.getGroup("a", "a");
      Member  m   = r.rs.getMember();
      Assert.assertTrue("OK: can read", m.canRead(a));
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testPassCanReadWhenRoot()

  public void testPassCanStemWhenRoot() {
    LOG.info("testPassCanStemWhenRoot");
    try {
      R       r   = R.populateRegistry(1, 0, 0);
      Stem    a   = r.getStem("a");
      Member  m   = r.rs.getMember();
      Assert.assertTrue("OK: can stem", m.canStem(a));
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testPassCanStemWhenRoot()

  public void testPassCanUpdateWhenRoot() {
    LOG.info("testPassCanUpdateWhenRoot");
    try {
      R       r   = R.populateRegistry(1, 1, 0);
      Group   a   = r.getGroup("a", "a");
      Member  m   = r.rs.getMember();
      Assert.assertTrue("OK: can update", m.canUpdate(a));
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testPassCanUpdateWhenRoot()

  public void testPassCanViewWhenRoot() {
    LOG.info("testPassCanViewWhenRoot");
    try {
      R       r   = R.populateRegistry(1, 1, 0);
      Group   a   = r.getGroup("a", "a");
      Member  m   = r.rs.getMember();
      Assert.assertTrue("OK: can view", m.canView(a));
      r.rs.stop();
    }
    catch (Exception e) {
      T.e(e);
    }
  } // public void testPassCanViewWhenRoot()

  public void testSetAllSubjectId() {
    LOG.info("testSetAllSubjectId");
    try {
      R         r     = R.populateRegistry(0, 0, 0);
      Subject   subj  = SubjectFinder.findAllSubject();
      Member    m     = MemberFinder.findBySubject(r.rs, subj, true);
      String    orig  = m.getSubjectId();
      try {
        m.setSubjectId( orig.toUpperCase() );
        m.store();
        fail("unexpectedly changed subjectId on GrouperAll");
      }
      catch (InsufficientPrivilegeException eIP) {
        assertTrue(true);
        T.string("subjectId", orig, m.getSubjectId());
      }
      finally {
        r.rs.stop();
      }
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testSetAllSubjectId()

  public void testSetRootSubjectId() {
    LOG.info("testSetRootSubjectId");
    try {
      R         r     = R.populateRegistry(0, 0, 0);
      Subject   subj  = SubjectFinder.findRootSubject();
      Member    m     = MemberFinder.findBySubject(r.rs, subj, true);
      String    orig  = m.getSubjectId();
      try {
        m.setSubjectId( orig.toUpperCase() );
        m.store();
        fail("unexpectedly changed subjectId on GrouperSystem");
      }
      catch (InsufficientPrivilegeException eIP) {
        assertTrue(true);
        T.string("subjectId", orig, m.getSubjectId());
      }
      finally {
        r.rs.stop();
      }
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testSetRootSubjectId()

  public void testSetSubjectIdNotRoot() {
    LOG.info("testSetSubjectIdNotRoot");
    try {
      R         r     = R.populateRegistry(0, 0, 2);
      Subject   subjA = r.getSubject("a");
      Subject   subjB = r.getSubject("b");
      r.rs.stop();
  
      GrouperSession  nrs   = GrouperSession.start(subjA);
      Member          m     = MemberFinder.findBySubject(nrs, subjB, true);
      String          orig  = m.getSubjectId();
      try {
        m.setSubjectId( orig.toUpperCase() );
        m.store();
        fail("unexpectedly changed subjectId when not root-like");
      }
      catch (InsufficientPrivilegeException eIP) {
        assertTrue(true);
        T.string("subjectId", orig, m.getSubjectId());
      }
      finally {
        nrs.stop();
      }
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testSetSubjectIdNotRoot

  public void testSetSubjectIdRoot() {
    LOG.info("testSetSubjectIdRoot");
    try {
      R         r     = R.populateRegistry(0, 0, 1);
      Subject   subjA = r.getSubject("a");
      Member    m     = MemberFinder.findBySubject(r.rs, subjA, true);
      String    orig  = m.getSubjectId();
      try {
        m.setSubjectId( orig.toUpperCase() );
        m.store();
        assertTrue(true);
        T.string("subjectId", orig.toUpperCase(), m.getSubjectId());
      }
      catch (InsufficientPrivilegeException eIP) {
        fail("did not change subjectId: " + eIP.getMessage());
      }
      finally {
        r.rs.stop();
      }
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testSetSubjectIdRoot

  public void testSetSubjectSourceIdFailAsNonRoot() {
    LOG.info("testSetSubjectSourceIdFailAsNonRoot");
    try {
      R         r     = R.populateRegistry(0, 0, 2);
      Subject   subjA = r.getSubject("a");
      Subject   subjB = r.getSubject("b");
      r.rs.stop();
  
      GrouperSession  nrs   = GrouperSession.start(subjA);
      Member          m     = MemberFinder.findBySubject(nrs, subjB, true);
      String          orig  = m.getSubjectSourceId();
      try {
        m.setSubjectSourceId( orig.toUpperCase() );
        m.store();
        fail("unexpectedly changed subject source id when not root-like");
      }
      catch (InsufficientPrivilegeException eIP) {
        assertTrue(true);
        T.string("subject source id", orig, m.getSubjectSourceId());
      }
      finally {
        nrs.stop();
      }
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testSetSubjectSourceIdFailAsNonRoot

  public void testSetSubjectSourceIdFailNullValue() {
    LOG.info("testSetSubjectSourceIdFailNullValue");
    try {
      R       r     = R.populateRegistry(0, 0, 1);
      Subject subjA = r.getSubject("a");
      Member  m     = MemberFinder.findBySubject(r.rs, subjA, true);
      try {
        m.setSubjectSourceId(null);
        m.store();
        fail("unexpectedly changed subject source id when value null");
      }
      catch (IllegalArgumentException eIA) {
        assertTrue(true);
      }
      finally {
        r.rs.stop();
      }
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testSetSubjectSourceIdFailNullValue

  public void testSetSubjectSourceIdFailOnGrouperAll() {
    LOG.info("testSetSubjectSourceIdFailOnGrouperAll");
    try {
      R         r     = R.populateRegistry(0, 0, 0);
      Subject   subj  = SubjectFinder.findAllSubject();
      Member    m     = MemberFinder.findBySubject(r.rs, subj, true);
      String    orig  = m.getSubjectSourceId();
      try {
        m.setSubjectSourceId( orig.toUpperCase() );
        m.store();
        fail("unexpectedly changed subject source id on GrouperAll");
      }
      catch (InsufficientPrivilegeException eIP) {
        assertTrue(true);
        T.string("subject source id", orig, m.getSubjectSourceId());
      }
      finally {
        r.rs.stop();
      }
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testSetSubjectSourceIdFailOnGrouperAll()

  public void testSetSubjectSourceIdFailOnGrouperSystem() {
    LOG.info("testSetSubjectSourceIdFailOnGrouperSystem");
    try {
      R         r     = R.populateRegistry(0, 0, 0);
      Subject   subj  = SubjectFinder.findRootSubject();
      Member    m     = MemberFinder.findBySubject(r.rs, subj, true);
      String    orig  = m.getSubjectSourceId();
      try {
        m.setSubjectSourceId( orig.toUpperCase() );
        m.store();
        fail("unexpectedly changed subject source id on GrouperSystem");
      }
      catch (InsufficientPrivilegeException eIP) {
        assertTrue(true);
        T.string("subject source id", orig, m.getSubjectSourceId());
      }
      finally {
        r.rs.stop();
      }
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testSetSubjectSourceIdFailOnGrouperSystem()

  public void testSetSubjectSourceIdOk() {
    LOG.info("testSetSubjectSourceIdOk");
    try {
      R         r     = R.populateRegistry(0, 0, 1);
      Subject   subjA = r.getSubject("a");
      Member    m     = MemberFinder.findBySubject(r.rs, subjA, true);
      String    orig  = m.getSubjectSourceId();
      try {
        m.setSubjectSourceId( orig.toUpperCase() );
        m.store();
        assertTrue(true);
        T.string("subject source id", orig.toUpperCase(), m.getSubjectSourceId());
      }
      catch (InsufficientPrivilegeException eIP) {
        fail("did not change subject source id: " + eIP.getMessage());
      }
      finally {
        r.rs.stop();
      }
    }
    catch (Exception e) {
      unexpectedException(e);
    }
  } // public void testSetSubjectSourceIdOk() 

}

