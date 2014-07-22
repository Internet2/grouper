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
 * @author mchyzer
 * $Id: TestPrivileges.java,v 1.2 2009-03-21 13:35:50 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.privs;

import java.util.Set;

import junit.framework.Assert;
import junit.textui.TestRunner;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GroupAddException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import edu.internet2.middleware.grouper.exception.SessionException;
import edu.internet2.middleware.grouper.exception.StemAddException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.PrivHelper;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.helper.T;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;



/**
 *
 */
public class TestPrivileges extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    //TestRunner.run(TestPrivileges.class);
    TestRunner.run(new TestPrivileges("testHasAccessPrivButCannotReadPriv"));
  }
  
  /**
   * @param name
   */
  public TestPrivileges(String name) {
    super(name);
  }

  /** */
  private static final Log LOG = GrouperUtil.getLog(TestPrivileges.class);

  /**
   * Gary Brown, 20051216, <39C7E27B4A5BE4494B39A93F@cse-gwb.cse.bris.ac.uk>>
   * status  fixed
   */
  public void testGrantAllAccessPriv() {
    LOG.info("testGrantAllAccessPriv");
    try {   
      Subject         all   = SubjectFinder.findAllSubject();
      GrouperSession  s     = SessionHelper.getRootSession();
      Stem            root  = StemFinder.findRootStem(s);
      Stem            ns    = root.addChildStem("ns", "ns");
      Group           a     = ns.addChildGroup("a", "a");
      Group           b     = ns.addChildGroup("b", "b");
      PrivHelper.grantPriv(s, a, all, AccessPrivilege.ADMIN);
      PrivHelper.grantPriv(s, b, all, AccessPrivilege.UPDATE);
      s.stop();
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  } // public void testGrantAllAccessPriv()

  // @source  Gary Brown, 20051216, <39C7E27B4A5BE4494B39A93F@cse-gwb.cse.bris.ac.uk>>
  // @status  fixed
  public void testGrantAllNamingPriv() {
    LOG.info("testGrantAllNamingPriv");
    try {   
      Subject         all   = SubjectFinder.findAllSubject();
      GrouperSession  s     = SessionHelper.getRootSession();
      Stem            root  = StemFinder.findRootStem(s);
      Stem            a     = root.addChildStem("a", "a");
      Stem            b     = root.addChildStem("b", "b");
      PrivHelper.grantPriv(s, a, all, NamingPrivilege.CREATE);
      PrivHelper.grantPriv(s, b, all, NamingPrivilege.STEM);
      s.stop();
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  } // public void testGrantAllNamingPriv()

  // Tests
  
  // @source  Gary Brown, 20051202, <C76C2307ED5A17415027C3D2@cse-gwb.cse.bris.ac.uk>
  // @status  fixed
  public void testGrantStemToGroup() {
    try {
      // Setup
      GrouperSession  s     = GrouperSession.startRootSession();
      Stem            root  = StemFinder.findRootStem(s);
      Stem            edu   = root.addChildStem("edu", "educational");
      Group           i2    = edu.addChildGroup("i2", "internet2");
      Subject         subj0 = SubjectFinder.findById("test.subject.0", true);
      i2.addMember(subj0);
      Assert.assertTrue("i2 has mem subj0", i2.hasMember(subj0));
      Assert.assertTrue("i2 has imm mem subj0", i2.hasImmediateMember(subj0));
      // Test
      Stem            ns    = StemFinder.findByName(s, edu.getName(), true);
      Assert.assertNotNull("ns !null", ns);
      Group           g     = GroupFinder.findByName(s, i2.getName(), true);
      Assert.assertNotNull("g !null", g);
  
      // Without the pre-granting of CREATE, the later granting of STEM
      // is fine.
      ns.grantPriv(
        SubjectFinder.findById(g.getUuid(), true),
         Privilege.getInstance("create")
       );
      Assert.assertTrue("g (ns) has CREATE", g.toMember().hasCreate(ns));
      Assert.assertTrue("g (m) has CREATE",  ns.hasCreate(g.toSubject()));
  
      ns.grantPriv(
        SubjectFinder.findById(g.getUuid(), true),
        Privilege.getInstance("stem")
      );
      Assert.assertTrue("g (ns) has STEM", g.toMember().hasStem(ns));
      Assert.assertTrue("g (m) has STEM",  ns.hasStem(g.toSubject()));
  
      s.stop();
    }
    catch (Exception e) {
      Assert.fail("exception: " + e.getMessage());
    }
  } // public void testGrantStemToGroup()

  // @source  Gary Brown, 20051216, <39C7E27B4A5BE4494B39A93F@cse-gwb.cse.bris.ac.uk>>
  // @status  fixed
  public void testHasAccessPrivButCannotReadPriv() 
    throws  GroupAddException,
            GroupNotFoundException,
            InsufficientPrivilegeException,
            MemberNotFoundException,
            SessionException,
            StemAddException
  {

    Subject         subj  = SubjectTestHelper.SUBJ0;
    GrouperSession  nrs   = GrouperSession.start(subj);
    Member          m     = MemberFinder.findBySubject(nrs, subj, true);

    int baseLineViewSize = m.hasView().size();
    int baseLineReadSize = m.hasRead().size();
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "true");
    
    LOG.info("testHasAccessPrivButCannotReadPriv");
    // Setup
    Subject         all   = SubjectFinder.findAllSubject();
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemFinder.findRootStem(s);
    Stem            edu   = root.addChildStem("edu", "edu");
    Group           a     = edu.addChildGroup("a", "a");
    Group           b     = edu.addChildGroup("b", "b");
    Group           c     = edu.addChildGroup("c", "c");
    edu.addChildGroup("d", "d");
    PrivHelper.grantPriv(s, a, all, AccessPrivilege.OPTIN);
    PrivHelper.grantPriv(s, b, all, AccessPrivilege.OPTOUT);
    PrivHelper.grantPriv(s, c, all, AccessPrivilege.UPDATE);
    s.stop();
  
    // Test
    nrs   = GrouperSession.start(subj);
    m     = MemberFinder.findBySubject(nrs, subj, true);
    Group           A     = GroupFinder.findByName(nrs, "edu:a", true);
    Group           B     = GroupFinder.findByName(nrs, "edu:b", true);
    Group           C     = GroupFinder.findByName(nrs, "edu:c", true);
    Group           D     = GroupFinder.findByName(nrs, "edu:d", true);
  
    Set view    = m.hasView();
    
    assertEquals(
      "VIEW/4 ("    + view.size()   + ")", 4+baseLineViewSize, view.size() 
    );
    Assert.assertTrue("VIEW/A/HAS",   A.hasView(subj)   );
    Assert.assertTrue("VIEW/B/HAS",   B.hasView(subj)   );
    Assert.assertTrue("VIEW/C/HAS",   C.hasView(subj)   );
    Assert.assertTrue("VIEW/D/HAS",   D.hasView(subj)   );
    Assert.assertTrue("VIEW/A/IS" ,   m.hasView(A)      );
    Assert.assertTrue("VIEW/B/IS" ,   m.hasView(B)      );
    Assert.assertTrue("VIEW/C/IS" ,   m.hasView(C)      );
    Assert.assertTrue("VIEW/D/IS" ,   m.hasView(D)      );
  
    Set read    = m.hasRead();
    Assert.assertEquals(
      "READ/4 ("    + read.size()   + ")", 4 + baseLineReadSize, read.size()
    );
    Assert.assertTrue("READ/A/HAS",   A.hasRead(subj)   );
    Assert.assertTrue("READ/B/HAS",   B.hasRead(subj)   );
    Assert.assertTrue("READ/C/HAS",   C.hasRead(subj)   );
    Assert.assertTrue("READ/D/HAS",   D.hasRead(subj)   );
    Assert.assertTrue("READ/A/IS" ,   m.hasRead(A)      );
    Assert.assertTrue("READ/B/IS" ,   m.hasRead(B)      );
    Assert.assertTrue("READ/C/IS" ,   m.hasRead(C)      );
    Assert.assertTrue("READ/D/IS" ,   m.hasRead(D)      );
  
    Set optin   = m.hasOptin();
    Assert.assertTrue(
      "OPTIN/1 ("   + optin.size()  + ")", optin.size()   == 1
    );
    Assert.assertTrue("OPTIN/A/HAS",  A.hasOptin(subj)  );
    Assert.assertTrue("OPTIN/B/HAS",  !B.hasOptin(subj) );
    Assert.assertTrue("OPTIN/C/HAS",  !C.hasOptin(subj) );
    Assert.assertTrue("OPTIN/D/HAS",  !D.hasOptin(subj) );
    Assert.assertTrue("OPTIN/A/IS" ,  m.hasOptin(A)     );
    Assert.assertTrue("OPTIN/B/IS" ,  !m.hasOptin(B)    );
    Assert.assertTrue("OPTIN/C/IS" ,  !m.hasOptin(C)    );
    Assert.assertTrue("OPTIN/D/IS" ,  !m.hasOptin(D)    );
  
    Set optout  = m.hasOptout();
    Assert.assertTrue(
      "OPTOUT/1 ("  + optout.size() + ")", optout.size()  == 1
    );
    Assert.assertTrue("OPTOUT/A/HAS", !A.hasOptout(subj));
    Assert.assertTrue("OPTOUT/B/HAS", B.hasOptout(subj) );
    Assert.assertTrue("OPTOUT/C/HAS", !C.hasOptout(subj));
    Assert.assertTrue("OPTOUT/D/HAS", !D.hasOptout(subj));
    Assert.assertTrue("OPTOUT/A/IS" , !m.hasOptout(A)   );
    Assert.assertTrue("OPTOUT/B/IS" , m.hasOptout(B)    );
    Assert.assertTrue("OPTOUT/C/IS" , !m.hasOptout(C)   );
    Assert.assertTrue("OPTOUT/D/IS" , !m.hasOptout(D)   );
  
    Set update  = m.hasUpdate();
    Assert.assertTrue(
      "UPDATE/1 ("  + update.size() + ")", update.size()  == 1
    );
    Assert.assertTrue("UPDATE/A/HAS", !A.hasUpdate(subj));
    Assert.assertTrue("UPDATE/B/HAS", !B.hasUpdate(subj));
    Assert.assertTrue("UPDATE/C/HAS", C.hasUpdate(subj) );
    Assert.assertTrue("UPDATE/D/HAS", !D.hasUpdate(subj));
    Assert.assertTrue("UPDATE/A/IS" , !m.hasUpdate(A)   );
    Assert.assertTrue("UPDATE/B/IS" , !m.hasUpdate(B)   );
    Assert.assertTrue("UPDATE/C/IS" , m.hasUpdate(C)    );
    Assert.assertTrue("UPDATE/D/IS" , !m.hasUpdate(D)   );
  
    Set admin   = m.hasAdmin();
    Assert.assertTrue(
      "ADMIN/0 ("   + admin.size()  + ")", admin.size()   == 0
    );
    Assert.assertTrue("ADMIN/A/HAS",  !A.hasAdmin(subj) );
    Assert.assertTrue("ADMIN/B/HAS",  !B.hasAdmin(subj) );
    Assert.assertTrue("ADMIN/C/HAS",  !C.hasAdmin(subj) );
    Assert.assertTrue("ADMIN/D/HAS",  !D.hasAdmin(subj) );
    Assert.assertTrue("ADMIN/A/IS" ,  !m.hasAdmin(A)    );
    Assert.assertTrue("ADMIN/B/IS" ,  !m.hasAdmin(B)    );
    Assert.assertTrue("ADMIN/C/IS" ,  !m.hasAdmin(C)    );
    Assert.assertTrue("ADMIN/D/IS" ,  !m.hasAdmin(D)    );
  
    nrs.stop();
  
  }

  // @source  Gary Brown, 20051216, <39C7E27B4A5BE4494B39A93F@cse-gwb.cse.bris.ac.uk>>
  // @status  fixed
  public void testHasNamingPrivButCannotReadPriv() 
    throws  InsufficientPrivilegeException,
            MemberNotFoundException,
            SessionException,
            StemAddException,
            StemNotFoundException
  {
    LOG.info("testHasNamingPrivButCannotReadPriv");
  
    // Setup
    Subject         all   = SubjectFinder.findAllSubject();
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemFinder.findRootStem(s);
    Stem            edu   = root.addChildStem("edu", "edu");
    Stem            a     = edu.addChildStem("a", "a");
    Stem            b     = edu.addChildStem("b", "b");
    edu.addChildStem("c", "c");
    PrivHelper.grantPriv(s, a, all, NamingPrivilege.CREATE);
    PrivHelper.grantPriv(s, b, all, NamingPrivilege.STEM);
    s.stop();
  
    // Test
    Subject         subj  = SubjectTestHelper.SUBJ0;
    GrouperSession  nrs   = GrouperSession.start(subj);
    Member          m     = MemberFinder.findBySubject(nrs, subj, true);
    Stem            A     = StemFinder.findByName(nrs, "edu:a", true);
    Stem            B     = StemFinder.findByName(nrs, "edu:b", true);
    Stem            C     = StemFinder.findByName(nrs, "edu:c", true);
  
    Set create  = m.hasCreate();
    T.amount( "CREATE", 1, create.size() );
    Assert.assertTrue("CREATE/A/HAS", A.hasCreate(subj) );
    Assert.assertTrue("CREATE/B/HAS", !B.hasCreate(subj));
    Assert.assertTrue("CREATE/C/HAS", !C.hasCreate(subj));
    Assert.assertTrue("CREATE/A/IS" , m.hasCreate(A)    );
    Assert.assertTrue("CREATE/B/IS" , !m.hasCreate(B)   );
    Assert.assertTrue("CREATE/C/IS" , !m.hasCreate(C)   );
  
    Set stem    = m.hasStem();
    T.amount( "STEM", 1, stem.size() );
    Assert.assertTrue("STEM/A/HAS"  , !A.hasStem(subj)  );
    Assert.assertTrue("STEM/B/HAS"  , B.hasStem(subj)   );
    Assert.assertTrue("STEM/C/HAS"  , !C.hasStem(subj)  );
    Assert.assertTrue("STEM/A/IS"   , !m.hasStem(A)     );
    Assert.assertTrue("STEM/B/IS"   , m.hasStem(B)      );
    Assert.assertTrue("STEM/C/IS"   , !m.hasStem(C)     );
  
    nrs.stop();
  }
}
