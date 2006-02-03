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

package test.edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  java.util.*;
import  junit.framework.*;
import  org.apache.commons.logging.*;


/**
 * Test use of the ADMIN {@link AccessPrivilege}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestAccessPrivilege.java,v 1.2 2006-02-03 19:38:53 blair Exp $
 */
public class TestAccessPrivilege extends TestCase {

  // Private Class Constants
  private static final Log LOG = LogFactory.getLog(TestAccessPrivilege.class);


  // Private Class Variables
  private static Group          a;
  private static Stem           edu;
  private static Group          i2;
  private static Member         m;
  private static GrouperSession nrs;
  private static Stem           root;
  private static GrouperSession s;
  private static Subject        subj0;
  private static Subject        subj1;
  private static Group          uofc;


  public TestAccessPrivilege(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    RegistryReset.resetRegistryAndAddTestSubjects();
    s     = SessionHelper.getRootSession();
    nrs   = SessionHelper.getSession(SubjectHelper.SUBJ0_ID);
    root  = StemHelper.findRootStem(s);
    edu   = StemHelper.addChildStem(root, "edu", "educational");
    i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    uofc  = StemHelper.addChildGroup(edu, "uofc", "uchicago");
    subj0 = SubjectHelper.SUBJ0;
    subj1 = SubjectHelper.SUBJ1;
    GroupHelper.addMember(uofc, subj0, "members");
    PrivHelper.grantPriv(s, uofc, subj0, AccessPrivilege.UPDATE);
    PrivHelper.grantPriv(s, i2, uofc.toSubject(), AccessPrivilege.OPTIN);
    m = Helper.getMemberBySubject(nrs, subj1);
  }

  protected void tearDown () {
    LOG.debug("tearDown");
    // Nothing 
  }

  // Tests

  public void testGetPrivs() {
    LOG.info("testGetPrivs");
    String impl = "edu.internet2.middleware.grouper.GrouperAccessAdapter";

    Set privs = i2.getPrivs(subj0);
    Assert.assertTrue(
      "subj0 has 3 privs on i2 (" + privs.size() + ")",
      privs.size() == 3
    );
    Iterator iter = privs.iterator();
    while (iter.hasNext()) {
      AccessPrivilege ap = (AccessPrivilege) iter.next();
      if      (ap.getName().equals(AccessPrivilege.OPTIN.getName())) {
        Assert.assertTrue(
          "i2/subj0 group optin: " + ap.getGroup().getName(),
          ap.getGroup().equals(i2)
        );
        Assert.assertTrue(
          "i2/subj0 impl optin: " + ap.getImplementationName(),
          ap.getImplementationName().equals(impl)
        );
        Assert.assertTrue(
          "i2/subj0 priv optin: " + ap.getName(),
          ap.getName().equals(AccessPrivilege.OPTIN.getName())
        );
        Assert.assertTrue(
          "i2/subj0 owner optin: " + ap.getOwner().getId(),
          SubjectHelper.eq(ap.getOwner(), uofc.toSubject())
        );
        Assert.assertTrue(
          "i2/subj0 subj optin: " + ap.getSubject().getId(),
          SubjectHelper.eq(ap.getSubject(), subj0)
        );
        Assert.assertTrue(
          "i2/subj0 isRevokable optin: " + ap.isRevokable(),
          ap.isRevokable() == false
        );
      }
      else if (ap.getName().equals(AccessPrivilege.READ.getName())) {
        Assert.assertTrue(
          "i2/subj0 group read: " + ap.getGroup().getName(),
          ap.getGroup().equals(i2)
        );
        Assert.assertTrue(
          "i2/subj0 impl read: " + ap.getImplementationName(),
          ap.getImplementationName().equals(impl)
        );
        Assert.assertTrue(
          "i2/subj0 priv read: " + ap.getName(),
          ap.getName().equals(AccessPrivilege.READ.getName())
        );
        Assert.assertTrue(
          "i2/subj0 owner read: " + ap.getOwner().getId(),
          SubjectHelper.eq(ap.getOwner(), SubjectHelper.SUBJA)
        );
        Assert.assertTrue(
          "i2/subj0 subj read: " + ap.getSubject().getId(),
          SubjectHelper.eq(ap.getSubject(), subj0)
        );
        Assert.assertTrue(
          "i2/subj0 isRevokable read: " + ap.isRevokable(),
          ap.isRevokable() == false
        );
      }
      else if (ap.getName().equals(AccessPrivilege.VIEW.getName())) {
        Assert.assertTrue(
          "i2/subj0 group view: " + ap.getGroup().getName(),
          ap.getGroup().equals(i2)
        );
        Assert.assertTrue(
          "i2/subj0 impl view: " + ap.getImplementationName(),
          ap.getImplementationName().equals(impl)
        );
        Assert.assertTrue(
          "i2/subj0 priv view: " + ap.getName(),
          ap.getName().equals(AccessPrivilege.VIEW.getName())
        );
        Assert.assertTrue(
          "i2/subj0 owner view: " + ap.getOwner().getId(),
          SubjectHelper.eq(ap.getOwner(), SubjectHelper.SUBJA)
        );
        Assert.assertTrue(
          "i2/subj0 subj view: " + ap.getSubject().getId(),
          SubjectHelper.eq(ap.getSubject(), subj0)
        );
        Assert.assertTrue(
          "i2/subj0 isRevokable view: " + ap.isRevokable(),
          ap.isRevokable() == false
        );
      }
      else {
        Assert.fail("unexpected priv: " + ap);
      }
    }

    privs = uofc.getPrivs(subj0);
    Assert.assertTrue(
      "subj0 has 3 privs on uofc (" + privs.size() + ")",
      privs.size() == 3
    );
    iter = privs.iterator();
    while (iter.hasNext()) {
      AccessPrivilege ap = (AccessPrivilege) iter.next();
      if      (ap.getName().equals(AccessPrivilege.UPDATE.getName())) {
        Assert.assertTrue(
          "uofc/subj0 group update: " + ap.getGroup().getName(),
          ap.getGroup().equals(uofc)
        );
        Assert.assertTrue(
          "uofc/subj0 impl update: " + ap.getImplementationName(),
          ap.getImplementationName().equals(impl)
        );
        Assert.assertTrue(
          "uofc/subj0 priv update: " + ap.getName(),
          ap.getName().equals(AccessPrivilege.UPDATE.getName())
        );
        Assert.assertTrue(
          "uofc/subj0 owner update: " + ap.getOwner().getId(),
          SubjectHelper.eq(ap.getOwner(), subj0)
        );
        Assert.assertTrue(
          "uofc/subj0 subj update: " + ap.getSubject().getId(),
          SubjectHelper.eq(ap.getSubject(), subj0)
        );
        Assert.assertTrue(
          "uofc/subj0 isRevokable update: " + ap.isRevokable(),
          ap.isRevokable() == true
        );
      }
      else if (ap.getName().equals(AccessPrivilege.READ.getName())) {
        Assert.assertTrue(
          "uofc/subj0 group read: " + ap.getGroup().getName(),
          ap.getGroup().equals(uofc)
        );
        Assert.assertTrue(
          "uofc/subj0 impl read: " + ap.getImplementationName(),
          ap.getImplementationName().equals(impl)
        );
        Assert.assertTrue(
          "uofc/subj0 priv read: " + ap.getName(),
          ap.getName().equals(AccessPrivilege.READ.getName())
        );
        Assert.assertTrue(
          "uofc/subj0 owner read: " + ap.getOwner().getId(),
          SubjectHelper.eq(ap.getOwner(), SubjectHelper.SUBJA)
        );
        Assert.assertTrue(
          "uofc/subj0 subj read: " + ap.getSubject().getId(),
          SubjectHelper.eq(ap.getSubject(), subj0)
        );
        Assert.assertTrue(
          "uofc/subj0 isRevokable read: " + ap.isRevokable(),
          ap.isRevokable() == false
        );
      }
      else if (ap.getName().equals(AccessPrivilege.VIEW.getName())) {
        Assert.assertTrue(
          "uofc/subj0 group view: " + ap.getGroup().getName(),
          ap.getGroup().equals(uofc)
        );
        Assert.assertTrue(
          "uofc/subj0 impl view: " + ap.getImplementationName(),
          ap.getImplementationName().equals(impl)
        );
        Assert.assertTrue(
          "uofc/subj0 priv view: " + ap.getName(),
          ap.getName().equals(AccessPrivilege.VIEW.getName())
        );
        Assert.assertTrue(
          "uofc/subj0 owner view: " + ap.getOwner().getId(),
          SubjectHelper.eq(ap.getOwner(), SubjectHelper.SUBJA)
        );
        Assert.assertTrue(
          "uofc/subj0 subj view: " + ap.getSubject().getId(),
          SubjectHelper.eq(ap.getSubject(), subj0)
        );
        Assert.assertTrue(
          "uofc/subj0 isRevokable view: " + ap.isRevokable(),
          ap.isRevokable() == false
        );
      }
      else {
        Assert.fail("unexpected priv: " + ap);
      }
    }
  } // public void testGetPrivs()

}

