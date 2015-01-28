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

package edu.internet2.middleware.grouper.filter;
import java.util.Set;

import junit.framework.Assert;
import junit.framework.TestCase;
import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.exception.QueryException;
import edu.internet2.middleware.grouper.helper.GroupHelper;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.registry.RegistryReset;


public class TestGQGroupAttributeExact extends TestCase {

  /**
   * Method main.
   * @param args String[]
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    //TestRunner.run(new TestGQGroupAttributeExact("testGroupAttributeExactFilterSomethingScoped"));
    TestRunner.run(TestGQGroupAttributeExact.class);
  }
  /**
   * 
   */
  public TestGQGroupAttributeExact() {
    super();
  }

  public TestGQGroupAttributeExact(String name) {
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

  public void testGroupAttributeExactFilterNothing() {
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
        s, new GroupAttributeExactFilter("name", "nothing", root)
      );
      Assert.assertTrue("groups",  gq.getGroups().size()      == 0);
      Assert.assertTrue("members", gq.getMembers().size()     == 0);
      Assert.assertTrue("mships",  gq.getMemberships().size() == 0);
      Assert.assertTrue("stems",   gq.getStems().size()       == 0);
    }
    catch (QueryException eQ) {
      Assert.fail("unable to query: " + eQ.getMessage());
    }
  }

  public void testGroupAttributeExactFilterSomething() {
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
        s, new GroupAttributeExactFilter("extension", "uofc", root)
      );
      Assert.assertTrue("groups",  gq.getGroups().size()      == 1);
      Assert.assertTrue("members", gq.getMembers().size()     == 0);
      Assert.assertTrue("mships",  gq.getMemberships().size() == 0);
      Assert.assertTrue("stems",   gq.getStems().size()       == 0);
    }
    catch (QueryException eQ) {
      Assert.fail("unable to query: " + eQ.getMessage());
    }
  }

  public void testGroupAttributeExactFilterSomethingScoped() throws Exception {
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemHelper.findRootStem(s);
    Stem            edu   = StemHelper.addChildStem(root, "edu", "education");
    Group           i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    Group           uofc  = StemHelper.addChildGroup(edu, "uofc", "uchicago");
    Group           duke  = StemHelper.addChildGroup(edu, "duke", "Duke University");
    Stem            com   = StemHelper.addChildStem(root, "com", "commercial");
    Group           devclue = StemHelper.addChildGroup(com, "devclue", "devclue");
    GroupHelper.addMember(i2, uofc);

    GroupType custom = GroupType.createType(s, "customType");
    custom.addAttribute(s, "customAttribute", false);

    try {
      GrouperQuery gq = GrouperQuery.createQuery(
        s, new GroupAttributeExactFilter("extension", "uofc", com)
      );
      Assert.assertTrue("groups",  gq.getGroups().size()      == 0);
      Assert.assertTrue("members", gq.getMembers().size()     == 0);
      Assert.assertTrue("mships",  gq.getMemberships().size() == 0);
      Assert.assertTrue("stems",   gq.getStems().size()       == 0);
    }
    catch (QueryException eQ) {
      Assert.fail("unable to query: " + eQ.getMessage());
    }

    {
      GrouperQuery gq = GrouperQuery.createQuery(
        s, new GroupAttributeExactFilter("extension", "uofc", edu)
      );
      Assert.assertEquals("groups",  gq.getGroups().size(), 1);
      Assert.assertTrue("members", gq.getMembers().size()     == 0);
      Assert.assertTrue("mships",  gq.getMemberships().size() == 0);
      Assert.assertTrue("stems",   gq.getStems().size()       == 0);
    }

    Set<Group> selectedGroups = GrouperDAOFactory.getFactory().getGroup().findAllByAnyApproximateAttr("uofc");
    assertEquals(1, selectedGroups.size());
    
    devclue.addType(custom);
    uofc.addType(custom);
    devclue.setAttribute("customAttribute", "String with i2 within");
    uofc.setAttribute("customAttribute", "String with i2 within");

    try {
      GrouperQuery gq = GrouperQuery.createQuery(
        s, new GroupAttributeExactFilter("customAttribute", "i2", com)
      );
      Assert.assertTrue("groups",  gq.getGroups().size()      == 0);
      Assert.assertTrue("members", gq.getMembers().size()     == 0);
      Assert.assertTrue("mships",  gq.getMemberships().size() == 0);
      Assert.assertTrue("stems",   gq.getStems().size()       == 0);
    }
    catch (QueryException eQ) {
      Assert.fail("unable to query: " + eQ.getMessage());
    }

    try {
      GrouperQuery gq = GrouperQuery.createQuery(
        s, new GroupAttributeExactFilter("customAttribute", "String with i2 within", com)
      );
      Assert.assertEquals("groups", 1, gq.getGroups().size());
      Assert.assertTrue("members", gq.getMembers().size()     == 0);
      Assert.assertTrue("mships",  gq.getMemberships().size() == 0);
      Assert.assertTrue("stems",   gq.getStems().size()       == 0);
    }
    catch (QueryException eQ) {
      Assert.fail("unable to query: " + eQ.getMessage());
    }
  }
  
  /**
   * 
   */
  public void testFindByAttributeLegacyAttributeRoot() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem root = StemHelper.findRootStem(grouperSession);
    Group group1 = new GroupSave(grouperSession).assignName("test:group1").assignCreateParentStemsIfNotExist(true).save();
    Group group2 = new GroupSave(grouperSession).assignName("test:group2").assignCreateParentStemsIfNotExist(true).save();
    Group group3 = new GroupSave(grouperSession).assignName("test:group3").assignCreateParentStemsIfNotExist(true).save();

    GroupType type1 = GroupType.createType(grouperSession, "type1");
    GroupType type2 = GroupType.createType(grouperSession, "type2");
    
    AttributeDefName type1Attr1 = type1.addAttribute(grouperSession, "type1Attr1");
    AttributeDefName type1Attr2 = type1.addAttribute(grouperSession, "type1Attr2");
    AttributeDefName type2Attr1 = type2.addAttribute(grouperSession, "type2Attr1");
    AttributeDefName type2Attr2 = type2.addAttribute(grouperSession, "type2Attr2");
    
    group1.addType(type1);
    group1.addType(type2);
    group2.addType(type2);
    
    group1.setAttribute("type1Attr1", "test value 1");
    group1.setAttribute("type1Attr2", "test value 2");
    group1.setAttribute("type2Attr2", "test value 3");
    group2.setAttribute("type2Attr2", "test value 4");
    
    GrouperQuery gq = GrouperQuery.createQuery(grouperSession, new GroupAttributeExactFilter("type1Attr1", "test value 1", root));
    Assert.assertEquals(1, gq.getGroups().size());
    Assert.assertEquals(group1.getName(), gq.getGroups().iterator().next().getName());
    
    gq = GrouperQuery.createQuery(grouperSession, new GroupAttributeExactFilter("type1Attr2", "test value 2", root));
    Assert.assertEquals(1, gq.getGroups().size());
    Assert.assertEquals(group1.getName(), gq.getGroups().iterator().next().getName());
    
    gq = GrouperQuery.createQuery(grouperSession, new GroupAttributeExactFilter("type2Attr2", "test value 3", root));
    Assert.assertEquals(1, gq.getGroups().size());
    Assert.assertEquals(group1.getName(), gq.getGroups().iterator().next().getName());
    
    gq = GrouperQuery.createQuery(grouperSession, new GroupAttributeExactFilter("type2Attr2", "test value 4", root));
    Assert.assertEquals(1, gq.getGroups().size());
    Assert.assertEquals(group2.getName(), gq.getGroups().iterator().next().getName());
    
    group1.setAttribute("type2Attr2", "test value 4");
    
    gq = GrouperQuery.createQuery(grouperSession, new GroupAttributeExactFilter("type2Attr2", "test value 4", root));
    Assert.assertEquals(2, gq.getGroups().size());
  }

  /**
   * 
   */
  public void testSecurity() {
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemHelper.findRootStem(s);
    Stem            edu   = StemHelper.addChildStem(root, "edu", "education");
    Group           i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    Group           uofc  = StemHelper.addChildGroup(edu, "uofc", "uchicago");
    Group           duke  = StemHelper.addChildGroup(edu, "duke", "Duke University");
    Stem            com   = StemHelper.addChildStem(root, "com", "commercial");
    Group           devclue = StemHelper.addChildGroup(com, "devclue", "devclue");
    Group           dev2 = StemHelper.addChildGroup(com, "dev2", "dev2");

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

    devclue.addType(custom);
    uofc.addType(custom);
    dev2.addType(custom);
    devclue.setAttribute("customAttribute", "String with i2 within");
    uofc.setAttribute("customAttribute", "String with i2 within");
    dev2.setAttribute("customAttribute", "String with i2 within");

    devclue.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.GROUP_ATTR_READ);
    custom.getAttributeDefName().getAttributeDef().getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
    custom.internal_getAttributeDefForAttributes().getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
    s.stop();
    s = GrouperSession.start(SubjectTestHelper.SUBJ0);
    GrouperQuery gq = GrouperQuery.createQuery(s, new GroupAttributeExactFilter("customAttribute", "String with i2 within", root));
    Assert.assertEquals(1, gq.getGroups().size());
    Assert.assertEquals(devclue.getName(), gq.getGroups().iterator().next().getName());
    
    s.stop();
    s = GrouperSession.startRootSession();
    devclue.revokePriv(SubjectTestHelper.SUBJ0, AccessPrivilege.GROUP_ATTR_READ);
    s.stop();
    s = GrouperSession.start(SubjectTestHelper.SUBJ0);
    gq = GrouperQuery.createQuery(s, new GroupAttributeExactFilter("customAttribute", "String with i2 within", root));
    Assert.assertEquals(0, gq.getGroups().size());
    
    s.stop();
    s = GrouperSession.startRootSession();
    devclue.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.GROUP_ATTR_READ);
    custom.getAttributeDefName().getAttributeDef().getPrivilegeDelegate().revokePriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, true);
    s.stop();
    s = GrouperSession.start(SubjectTestHelper.SUBJ0);
    gq = GrouperQuery.createQuery(s, new GroupAttributeExactFilter("customAttribute", "String with i2 within", root));
    Assert.assertEquals(0, gq.getGroups().size());
    
    s.stop();
    s = GrouperSession.startRootSession();
    custom.getAttributeDefName().getAttributeDef().getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, true);
    custom.internal_getAttributeDefForAttributes().getPrivilegeDelegate().revokePriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, true);
    s.stop();
    s = GrouperSession.start(SubjectTestHelper.SUBJ0);
    gq = GrouperQuery.createQuery(s, new GroupAttributeExactFilter("customAttribute", "String with i2 within", root));
    Assert.assertEquals(0, gq.getGroups().size());
    
    s.stop();
    s = GrouperSession.startRootSession();
    custom.internal_getAttributeDefForAttributes().getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, true);
    s.stop();
    s = GrouperSession.start(SubjectTestHelper.SUBJ0);
    gq = GrouperQuery.createQuery(s, new GroupAttributeExactFilter("customAttribute", "String with i2 within", root));
    Assert.assertEquals(1, gq.getGroups().size());
    
    gq = GrouperQuery.createQuery(s, new GroupAttributeExactFilter("etc:legacy:attribute:legacyAttribute_customAttribute", "String with i2 within", root));
    Assert.assertEquals(1, gq.getGroups().size());
    
    gq = GrouperQuery.createQuery(s, new GroupAttributeExactFilter("extension", "devclue", root));
    Assert.assertEquals(1, gq.getGroups().size());
    
    gq = GrouperQuery.createQuery(s, new GroupAttributeExactFilter("extension", "devclue", com));
    Assert.assertEquals(1, gq.getGroups().size());
    
    gq = GrouperQuery.createQuery(s, new GroupAttributeExactFilter("extension", "dev", com));
    Assert.assertEquals(0, gq.getGroups().size());
    
    gq = GrouperQuery.createQuery(s, new GroupAttributeExactFilter("extension", "devclux", com));
    Assert.assertEquals(0, gq.getGroups().size());
    
    gq = GrouperQuery.createQuery(s, new GroupAttributeExactFilter("extension", "devclue", edu));
    Assert.assertEquals(0, gq.getGroups().size());
    
    // you really only need view permissions on the group for the normal group attrs to work
    s.stop();
    s = GrouperSession.startRootSession();
    devclue.revokePriv(SubjectTestHelper.SUBJ0, AccessPrivilege.GROUP_ATTR_READ);
    custom.getAttributeDefName().getAttributeDef().getPrivilegeDelegate().revokePriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
    custom.internal_getAttributeDefForAttributes().getPrivilegeDelegate().revokePriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);

    s.stop();
    s = GrouperSession.start(SubjectTestHelper.SUBJ0);
    gq = GrouperQuery.createQuery(s, new GroupAttributeExactFilter("extension", "devclue", root));
    Assert.assertEquals(0, gq.getGroups().size());
    
    gq = GrouperQuery.createQuery(s, new GroupAttributeExactFilter("extension", "devclue", com));
    Assert.assertEquals(0, gq.getGroups().size());
    
    gq = GrouperQuery.createQuery(s, new GroupAttributeExactFilter("extension", "dev", com));
    Assert.assertEquals(0, gq.getGroups().size());
    
    gq = GrouperQuery.createQuery(s, new GroupAttributeExactFilter("extension", "devclux", com));
    Assert.assertEquals(0, gq.getGroups().size());
    
    gq = GrouperQuery.createQuery(s, new GroupAttributeExactFilter("extension", "devclue", edu));
    Assert.assertEquals(0, gq.getGroups().size());
    
    s.stop();
    s = GrouperSession.startRootSession();
    devclue.grantPriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW);
    dev2.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW);

    s.stop();
    s = GrouperSession.start(SubjectTestHelper.SUBJ0);
    gq = GrouperQuery.createQuery(s, new GroupAttributeExactFilter("extension", "devclue", root));
    Assert.assertEquals(1, gq.getGroups().size());
    
    gq = GrouperQuery.createQuery(s, new GroupAttributeExactFilter("extension", "devclue", com));
    Assert.assertEquals(1, gq.getGroups().size());
    
    gq = GrouperQuery.createQuery(s, new GroupAttributeExactFilter("extension", "dev", root));
    Assert.assertEquals(0, gq.getGroups().size());
    
    gq = GrouperQuery.createQuery(s, new GroupAttributeExactFilter("extension", "dev", com));
    Assert.assertEquals(0, gq.getGroups().size());
    
    gq = GrouperQuery.createQuery(s, new GroupAttributeExactFilter("extension", "devclux", com));
    Assert.assertEquals(0, gq.getGroups().size());
    
    gq = GrouperQuery.createQuery(s, new GroupAttributeExactFilter("extension", "devclue", edu));
    Assert.assertEquals(0, gq.getGroups().size());
  }
}

