/*
  Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2006 The University Of Chicago

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
import  edu.internet2.middleware.subject.*;
import  java.util.*;
import  junit.framework.*;
import  org.apache.commons.logging.*;

/**
 * Test {@link Member}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestMember.java,v 1.4 2006-08-30 19:31:02 blair Exp $
 */
public class TestMember extends TestCase {

  // Private Class Constants
  private static final Log LOG = LogFactory.getLog(TestMember.class);


  public TestMember(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.resetRegistryAndAddTestSubjects();
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

  public void testSetSubjectIdNotRoot() {
    LOG.info("testSetSubjectIdNotRoot");
    try {
      GrouperSession nrs = SessionHelper.getSession(SubjectTestHelper.SUBJ0_ID);
      Member  m     = MemberFinder.findBySubject(nrs, SubjectTestHelper.SUBJ1);
      String  orig  = m.getSubjectId();
      try {
        m.setSubjectId(orig.toUpperCase());
        Assert.fail("set");
      }
      catch (InsufficientPrivilegeException eIP) {
        Assert.assertTrue(eIP.getMessage(), true);
        Assert.assertTrue("unchanged", m.getSubjectId().equals(orig));
      }
      nrs.stop();
    }
    catch (Exception e) {
      Assert.fail("could not get: " + e.getMessage());
    }
  } // public void testSetSubjectIdNotRoot

  public void testSetSubjectIdRoot() {
    LOG.info("testSetSubjectIdRoot");
    try {
      GrouperSession s = SessionHelper.getRootSession();
      Member  m     = MemberFinder.findBySubject(s, SubjectTestHelper.SUBJ1);
      String  orig  = m.getSubjectId();
      try {
        m.setSubjectId(orig.toUpperCase());
        Assert.assertTrue("set", true);
        Assert.assertFalse("changed", m.getSubjectId().equals(orig));
        Assert.assertTrue("val", m.getSubjectId().equals(orig.toUpperCase()));
      }
      catch (InsufficientPrivilegeException eIP) {
        Assert.fail("not set: " + eIP.getMessage());
      }
      s.stop();
    }
    catch (Exception e) {
      Assert.fail("could not get: " + e.getMessage());
    }
  } // public void testSetSubjectIdRoot

  public void testSetRootSubjectId() {
    LOG.info("testSetRootSubjectId");
    try {
      GrouperSession s = SessionHelper.getRootSession();
      Member  m     = MemberFinder.findBySubject(s, s.getSubject());
      String  orig  = m.getSubjectId();
      try {
        m.setSubjectId(orig.toUpperCase());
        Assert.fail("set root subjectid");
      }
      catch (InsufficientPrivilegeException eIP) {
        Assert.assertTrue("could not set root subject id", true);
        Assert.assertTrue("original value", m.getSubjectId().equals(orig));
      }
      s.stop();
    }
    catch (Exception e) {
      Assert.fail("could not get: " + e.getMessage());
    }
  } // public void testSetRootSubjectId()

  public void testSetAllSubjectId() {
    LOG.info("testSetAllSubjectId");
    try {
      GrouperSession s = SessionHelper.getRootSession();
      Member  m     = MemberFinder.findBySubject(
        s, SubjectFinder.findAllSubject()
      );
      String  orig  = m.getSubjectId();
      try {
        m.setSubjectId(orig.toUpperCase());
        Assert.fail("set all subjectid");
      }
      catch (InsufficientPrivilegeException eIP) {
        Assert.assertTrue("could not set all subject id", true);
        Assert.assertTrue("original value", m.getSubjectId().equals(orig));
      }
      s.stop();
    }
    catch (Exception e) {
      Assert.fail("could not get: " + e.getMessage());
    }
  } // public void testSetAllSubjectId()

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
      Member          m     = MemberFinder.findBySubject(s, subj);

      Field f = FieldFinder.find("members");

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

  public void testGetAndHasPrivs() {
    LOG.info("testGetAndHasPrivs");
    try {
      Subject         subj  = SubjectTestHelper.SUBJ0;
      Subject         all   = SubjectTestHelper.SUBJA;
      GrouperSession  s     = SessionHelper.getRootSession();
      Stem            root  = StemFinder.findRootStem(s);
      Stem            edu   = root.addChildStem("edu", "edu");
      Group           i2    = edu.addChildGroup("i2", "i2");
      Group           uofc  = edu.addChildGroup("uofc", "uofc");
      GroupHelper.addMember(uofc, subj, "members");
      GroupHelper.addMember(i2, uofc.toSubject(), "members");
      Member          m     = MemberFinder.findBySubject(s, subj);
      PrivHelper.grantPriv(s, root, subj, NamingPrivilege.CREATE);
      PrivHelper.grantPriv(s, edu,  all,  NamingPrivilege.STEM); 
      PrivHelper.grantPriv(s, i2,   all,  AccessPrivilege.OPTIN);
      PrivHelper.grantPriv(s, uofc, subj, AccessPrivilege.UPDATE);

      FieldFinder.find("members");

      // Get naming privs
      Assert.assertTrue("getprivs/root  == 1",  m.getPrivs(root).size() == 1);
      Assert.assertTrue("getprivs/edu   == 1",  m.getPrivs(edu).size()  == 1);

      // Get access privs
      Assert.assertTrue("getprivs/i2    == 1",  m.getPrivs(i2).size()   == 3);
      Assert.assertTrue("getprivs/uofc  == 1",  m.getPrivs(uofc).size() == 3);;

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
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  } // public void testGetAndHasPrivs()

}

