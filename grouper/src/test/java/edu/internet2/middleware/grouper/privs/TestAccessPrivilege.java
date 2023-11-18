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

package edu.internet2.middleware.grouper.privs;
import java.util.Iterator;
import java.util.Set;

import junit.framework.Assert;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.helper.GroupHelper;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.PrivHelper;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * Test use of the ADMIN {@link AccessPrivilege}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestAccessPrivilege.java,v 1.1 2009-03-20 19:56:41 mchyzer Exp $
 */
public class TestAccessPrivilege extends GrouperTest {

  // Private Class Constants
  private static final Log LOG = GrouperUtil.getLog(TestAccessPrivilege.class);
  
  private static Stem           edu;
  private static Group          i2;
  private static Stem           root;
  private static GrouperSession s;
  private static Subject        subj0;
  private static Group          uofc;


  public TestAccessPrivilege(String name) {
    super(name);
  }

  protected void setUp () {
    LOG.debug("setUp");
    super.setUp();
    RegistryReset.internal_resetRegistryAndAddTestSubjects();
    GrouperTest.initGroupsAndAttributes();

    s     = SessionHelper.getRootSession();
    root  = StemHelper.findRootStem(s);
    edu   = StemHelper.addChildStem(root, "edu", "educational");
    i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    uofc  = StemHelper.addChildGroup(edu, "uofc", "uchicago");
    subj0 = SubjectTestHelper.SUBJ0;
    GroupHelper.addMember(uofc, subj0, "members");
    PrivHelper.grantPriv(s, uofc, subj0, AccessPrivilege.UPDATE);
    PrivHelper.grantPriv(s, i2, uofc.toSubject(), AccessPrivilege.OPTIN);
  }

  protected void tearDown () {
    LOG.debug("tearDown"); 
    super.tearDown();
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
          SubjectHelper.eq(ap.getOwner(), SubjectTestHelper.SUBJA)
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
          SubjectHelper.eq(ap.getOwner(), SubjectTestHelper.SUBJA)
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
          SubjectHelper.eq(ap.getOwner(), SubjectTestHelper.SUBJA)
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
          SubjectHelper.eq(ap.getOwner(), SubjectTestHelper.SUBJA)
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

