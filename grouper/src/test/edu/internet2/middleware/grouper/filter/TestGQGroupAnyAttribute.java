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

package edu.internet2.middleware.grouper.filter;
import junit.framework.Assert;
import junit.framework.TestCase;
import junit.runner.TestRunListener;
import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.QueryException;
import edu.internet2.middleware.grouper.filter.GroupAnyAttributeFilter;
import edu.internet2.middleware.grouper.filter.GrouperQuery;
import edu.internet2.middleware.grouper.helper.GroupHelper;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.registry.RegistryReset;


/**
 * Test {@link GroupAnyAttributeFilter}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestGQGroupAnyAttribute.java,v 1.4 2009-04-14 07:41:24 mchyzer Exp $
 */
public class TestGQGroupAnyAttribute extends TestCase {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(TestGQGroupAnyAttribute.class);
  }
  
  public TestGQGroupAnyAttribute(String name) {
    super(name);
  }

  protected void setUp () {
    RegistryReset.internal_resetRegistryAndAddTestSubjects();
    GrouperTest.initGroupsAndAttributes();

  }

  protected void tearDown () {
    // Nothing 
  }

  // Tests

  public void testGroupAnyAttributeFilterNothing() {
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemHelper.findRootStem(s);
    Stem            edu   = StemHelper.addChildStem(root, "edu", "education");
    Group           i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    Group           uofc  = StemHelper.addChildGroup(edu, "uofc", "uchicago");
    Stem            com   = StemHelper.addChildStem(root, "com", "commercial");
    StemHelper.addChildGroup(com, "devclue", "devclue");
    GroupHelper.addMember(i2, uofc);
    try {
      GrouperQuery gq = GrouperQuery.createQuery(
        s, new GroupAnyAttributeFilter("nothing", root)
      );
      Assert.assertTrue("groups",  gq.getGroups().size()      == 0);
      Assert.assertTrue("members", gq.getMembers().size()     == 0);
      Assert.assertTrue("mships",  gq.getMemberships().size() == 0);
      Assert.assertTrue("stems",   gq.getStems().size()       == 0);
    }
    catch (QueryException eQ) {
      Assert.fail("unable to query: " + eQ.getMessage());
    }
  } // public void testGroupAnyAttributeFilterNothing()

  public void testGroupAnyAttributeFilterSomething() {
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemHelper.findRootStem(s);
    Stem            edu   = StemHelper.addChildStem(root, "edu", "education");
    Group           i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    Group           uofc  = StemHelper.addChildGroup(edu, "uofc", "uchicago");
    Stem            com   = StemHelper.addChildStem(root, "com", "commercial");
    StemHelper.addChildGroup(com, "devclue", "devclue");
    GroupHelper.addMember(i2, uofc);
    try {
      GrouperQuery gq = GrouperQuery.createQuery(
        s, new GroupAnyAttributeFilter("uofc", root)
      );
      Assert.assertTrue("groups",  gq.getGroups().size()      == 1);
      Assert.assertTrue("members", gq.getMembers().size()     == 0);
      Assert.assertTrue("mships",  gq.getMemberships().size() == 0);
      Assert.assertTrue("stems",   gq.getStems().size()       == 0);
    }
    catch (QueryException eQ) {
      Assert.fail("unable to query: " + eQ.getMessage());
    }
  } // public void testGroupAnyAttributeFilterSomething()

  public void testGroupAnyAttributeFilterSomethingScoped() throws Exception {
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemHelper.findRootStem(s);
    Stem            edu   = StemHelper.addChildStem(root, "edu", "education");
    Group           i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    Group           uofc  = StemHelper.addChildGroup(edu, "uofc", "uchicago");
    Group           duke  = StemHelper.addChildGroup(edu, "duke", "Duke University");
    Stem            com   = StemHelper.addChildStem(root, "com", "commercial");
    Group           devclue = StemHelper.addChildGroup(com, "devclue", "devclue");
    Group           dev2 = StemHelper.addChildGroup(com, "dev2", "dev2");
    GroupHelper.addMember(i2, uofc);

    uofc.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
    uofc.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
    i2.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
    i2.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
    duke.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
    duke.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
    devclue.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
    devclue.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
    dev2.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
    dev2.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
    
    GroupType custom = GroupType.createType(s, "customType");
    custom.addAttribute(s, "customAttribute", false);

    try {
      GrouperQuery gq = GrouperQuery.createQuery(
        s, new GroupAnyAttributeFilter("uofc", com)
      );
      Assert.assertTrue("groups",  gq.getGroups().size()      == 0);
      Assert.assertTrue("members", gq.getMembers().size()     == 0);
      Assert.assertTrue("mships",  gq.getMemberships().size() == 0);
      Assert.assertTrue("stems",   gq.getStems().size()       == 0);
    }
    catch (QueryException eQ) {
      Assert.fail("unable to query: " + eQ.getMessage());
    }

    devclue.addType(custom);
    uofc.addType(custom);
    dev2.addType(custom);
    devclue.setAttribute("customAttribute", "String with i2 within");
    uofc.setAttribute("customAttribute", "String with i2 within");

    try {
      GrouperQuery gq = GrouperQuery.createQuery(
        s, new GroupAnyAttributeFilter("i2", com)
      );
      Assert.assertTrue("groups",  gq.getGroups().size()      == 1);
      Assert.assertTrue("members", gq.getMembers().size()     == 0);
      Assert.assertTrue("mships",  gq.getMemberships().size() == 0);
      Assert.assertTrue("stems",   gq.getStems().size()       == 0);
    }
    catch (QueryException eQ) {
      Assert.fail("unable to query: " + eQ.getMessage());
    }

    try {
      GrouperQuery gq = GrouperQuery.createQuery(
        s, new GroupAnyAttributeFilter("i2", edu)
      );
      Assert.assertTrue("groups",  gq.getGroups().size()      == 2);
      Assert.assertTrue("members", gq.getMembers().size()     == 1);
      Assert.assertTrue("mships",  gq.getMemberships().size() == 1);
      Assert.assertTrue("stems",   gq.getStems().size()       == 0);
    }
    catch (QueryException eQ) {
      Assert.fail("unable to query: " + eQ.getMessage());
    }


    // test security
    dev2.setAttribute("customAttribute", "String with i2 within");

    devclue.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.GROUP_ATTR_READ);
    custom.getAttributeDefName().getAttributeDef().getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
    custom.internal_getAttributeDefForAttributes().getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
    s.stop();
    s = GrouperSession.start(SubjectTestHelper.SUBJ0);
    GrouperQuery gq = GrouperQuery.createQuery(s, new GroupAnyAttributeFilter("i2 Within", root));
    Assert.assertEquals(1, gq.getGroups().size());
    Assert.assertEquals(devclue.getName(), gq.getGroups().iterator().next().getName());
    
    s.stop();
    s = GrouperSession.startRootSession();
    devclue.revokePriv(SubjectTestHelper.SUBJ0, AccessPrivilege.GROUP_ATTR_READ);
    s.stop();
    s = GrouperSession.start(SubjectTestHelper.SUBJ0);
    gq = GrouperQuery.createQuery(s, new GroupAnyAttributeFilter("i2 Within", root));
    Assert.assertEquals(0, gq.getGroups().size());
    
    s.stop();
    s = GrouperSession.startRootSession();
    devclue.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.GROUP_ATTR_READ);
    custom.getAttributeDefName().getAttributeDef().getPrivilegeDelegate().revokePriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, true);
    s.stop();
    s = GrouperSession.start(SubjectTestHelper.SUBJ0);
    gq = GrouperQuery.createQuery(s, new GroupAnyAttributeFilter("i2 Within", root));
    Assert.assertEquals(0, gq.getGroups().size());
    
    s.stop();
    s = GrouperSession.startRootSession();
    custom.getAttributeDefName().getAttributeDef().getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, true);
    custom.internal_getAttributeDefForAttributes().getPrivilegeDelegate().revokePriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, true);
    s.stop();
    s = GrouperSession.start(SubjectTestHelper.SUBJ0);
    gq = GrouperQuery.createQuery(s, new GroupAnyAttributeFilter("i2 Within", root));
    Assert.assertEquals(0, gq.getGroups().size());
    
    s.stop();
    s = GrouperSession.startRootSession();
    custom.internal_getAttributeDefForAttributes().getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, true);
    s.stop();
    s = GrouperSession.start(SubjectTestHelper.SUBJ0);
    gq = GrouperQuery.createQuery(s, new GroupAnyAttributeFilter("i2 Within", root));
    Assert.assertEquals(1, gq.getGroups().size());
    
    gq = GrouperQuery.createQuery(s, new GroupAnyAttributeFilter("devclue", root));
    Assert.assertEquals(1, gq.getGroups().size());
    
    gq = GrouperQuery.createQuery(s, new GroupAnyAttributeFilter("devclue", com));
    Assert.assertEquals(1, gq.getGroups().size());
    
    gq = GrouperQuery.createQuery(s, new GroupAnyAttributeFilter("dev", com));
    Assert.assertEquals(1, gq.getGroups().size());
    
    gq = GrouperQuery.createQuery(s, new GroupAnyAttributeFilter("devclux", com));
    Assert.assertEquals(0, gq.getGroups().size());
    
    gq = GrouperQuery.createQuery(s, new GroupAnyAttributeFilter("devclue", edu));
    Assert.assertEquals(0, gq.getGroups().size());
    
    // you really only need view permissions on the group for the normal group attrs to work
    s.stop();
    s = GrouperSession.startRootSession();
    devclue.revokePriv(SubjectTestHelper.SUBJ0, AccessPrivilege.GROUP_ATTR_READ);
    custom.getAttributeDefName().getAttributeDef().getPrivilegeDelegate().revokePriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
    custom.internal_getAttributeDefForAttributes().getPrivilegeDelegate().revokePriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);

    s.stop();
    s = GrouperSession.start(SubjectTestHelper.SUBJ0);
    gq = GrouperQuery.createQuery(s, new GroupAnyAttributeFilter("devclue", root));
    Assert.assertEquals(0, gq.getGroups().size());
    
    gq = GrouperQuery.createQuery(s, new GroupAnyAttributeFilter("devclue", com));
    Assert.assertEquals(0, gq.getGroups().size());
    
    gq = GrouperQuery.createQuery(s, new GroupAnyAttributeFilter("dev", com));
    Assert.assertEquals(0, gq.getGroups().size());
    
    gq = GrouperQuery.createQuery(s, new GroupAnyAttributeFilter("devclux", com));
    Assert.assertEquals(0, gq.getGroups().size());
    
    gq = GrouperQuery.createQuery(s, new GroupAnyAttributeFilter("devclue", edu));
    Assert.assertEquals(0, gq.getGroups().size());
    
    s.stop();
    s = GrouperSession.startRootSession();
    devclue.grantPriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW);
    dev2.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW);

    s.stop();
    s = GrouperSession.start(SubjectTestHelper.SUBJ0);
    gq = GrouperQuery.createQuery(s, new GroupAnyAttributeFilter("devclue", root));
    Assert.assertEquals(1, gq.getGroups().size());
    
    gq = GrouperQuery.createQuery(s, new GroupAnyAttributeFilter("devclue", com));
    Assert.assertEquals(1, gq.getGroups().size());
    
    gq = GrouperQuery.createQuery(s, new GroupAnyAttributeFilter("dev", root));
    Assert.assertEquals(2, gq.getGroups().size());
    
    gq = GrouperQuery.createQuery(s, new GroupAnyAttributeFilter("dev", com));
    Assert.assertEquals(2, gq.getGroups().size());
    
    gq = GrouperQuery.createQuery(s, new GroupAnyAttributeFilter("devclux", com));
    Assert.assertEquals(0, gq.getGroups().size());
    
    gq = GrouperQuery.createQuery(s, new GroupAnyAttributeFilter("devclue", edu));
    Assert.assertEquals(0, gq.getGroups().size());
    
  } // public void testGroupAnyAttributeFilterSomethingDisplayExtensionScoped()

}

