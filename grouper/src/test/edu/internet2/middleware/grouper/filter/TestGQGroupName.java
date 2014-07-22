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
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;
import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.exception.QueryException;
import edu.internet2.middleware.grouper.helper.GroupHelper;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.QuerySort;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Test {@link GroupNameFilter}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestGQGroupName.java,v 1.4 2009-03-27 23:28:53 shilen Exp $
 */
public class TestGQGroupName extends TestCase {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new TestGQGroupName("testGroupNameFilterSomethingNamePaged"));
  }
  
  public TestGQGroupName(String name) {
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

  public void testGroupNameFilterNothing() {
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
        s, new GroupNameFilter("nothing", root)
      );
      Assert.assertTrue("groups",  gq.getGroups().size()      == 0);
      Assert.assertTrue("members", gq.getMembers().size()     == 0);
      Assert.assertTrue("mships",  gq.getMemberships().size() == 0);
      Assert.assertTrue("stems",   gq.getStems().size()       == 0);
    }
    catch (QueryException eQ) {
      Assert.fail("unable to query: " + eQ.getMessage());
    }
  } // public void testGroupNameFilterNothing()

  public void testGroupNameFilterSomethingDisplayExtension() {
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
        s, new GroupNameFilter("uchicago", root)
      );
      Assert.assertTrue("groups",  gq.getGroups().size()      == 1);
      Assert.assertTrue("members", gq.getMembers().size()     == 0);
      Assert.assertTrue("mships",  gq.getMemberships().size() == 0);
      Assert.assertTrue("stems",   gq.getStems().size()       == 0);
    }
    catch (QueryException eQ) {
      Assert.fail("unable to query: " + eQ.getMessage());
    }
  } // public void testGroupNameFilterSomethingDisplayExtension()

  public void testGroupNameFilterSomethingDisplayName() {
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
        s, new GroupNameFilter("internet2", root)
      );
      Assert.assertTrue("groups",  gq.getGroups().size()      == 1);
      Assert.assertTrue("members", gq.getMembers().size()     == 1);
      Assert.assertTrue("mships",  gq.getMemberships().size() == 1);
      Assert.assertTrue("stems",   gq.getStems().size()       == 0);
    }
    catch (QueryException eQ) {
      Assert.fail("unable to query: " + eQ.getMessage());
    }
  } // public void testGroupNameFilterSomethingDisplayName()

  public void testGroupNameFilterSomethingExtension() {
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
        s, new GroupNameFilter("UOFC", root)
      );
      Assert.assertTrue("groups",  gq.getGroups().size()      == 1);
      Assert.assertTrue("members", gq.getMembers().size()     == 0);
      Assert.assertTrue("mships",  gq.getMemberships().size() == 0);
      Assert.assertTrue("stems",   gq.getStems().size()       == 0);
    }
    catch (QueryException eQ) {
      Assert.fail("unable to query: " + eQ.getMessage());
    }
  } // public void testGroupNameFilterSomethingExtension()

  public void testGroupNameFilterSomethingName() {
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
        s, new GroupNameFilter("edu:i2", root)
      );
      Assert.assertTrue("groups",  gq.getGroups().size()      == 1);
      Assert.assertTrue("members", gq.getMembers().size()     == 1);
      Assert.assertTrue("mships",  gq.getMemberships().size() == 1);
      Assert.assertTrue("stems",   gq.getStems().size()       == 0);
    }
    catch (QueryException eQ) {
      Assert.fail("unable to query: " + eQ.getMessage());
    }
  } // public void testGroupNameFilterSomethingName()

  public void testGroupNameFilterSomethingDisplayExtensionScoped() {
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
        s, new GroupNameFilter("uchicago", com)
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
        s, new GroupNameFilter("uchicago", edu)
      ); 
      Assert.assertTrue("groups",  gq.getGroups().size()      == 1);
      Assert.assertTrue("members", gq.getMembers().size()     == 0);
      Assert.assertTrue("mships",  gq.getMemberships().size() == 0);
      Assert.assertTrue("stems",   gq.getStems().size()       == 0);
    }
    catch (QueryException eQ) {
      Assert.fail("unable to query: " + eQ.getMessage());
    }
  } // public void testGroupNameFilterSomethingDisplayExtensionScoped()

  public void testGroupNameFilterSomethingDisplayNameScoped() {
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
        s, new GroupNameFilter("internet2", com)
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
        s, new GroupNameFilter("education", edu)
      );
      Assert.assertTrue("groups",  gq.getGroups().size()      == 2);
      Assert.assertTrue("members", gq.getMembers().size()     == 1);
      Assert.assertTrue("mships",  gq.getMemberships().size() == 1);
      Assert.assertTrue("stems",   gq.getStems().size()       == 0);
    }
    catch (QueryException eQ) {
      Assert.fail("unable to query: " + eQ.getMessage());
    }

    try {
      GrouperQuery gq = GrouperQuery.createQuery(
        s, new GroupNameFilter("education:internet2", edu)
      );
      Assert.assertTrue("groups",  gq.getGroups().size()      == 1);
      Assert.assertTrue("members", gq.getMembers().size()     == 1);
      Assert.assertTrue("mships",  gq.getMemberships().size() == 1);
      Assert.assertTrue("stems",   gq.getStems().size()       == 0);
    }
    catch (QueryException eQ) {
      Assert.fail("unable to query: " + eQ.getMessage());
    }
  } // public void testGroupNameFilterSomethingDisplayNameScoped()

  public void testGroupNameFilterSomethingExtensionScoped() {
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
        s, new GroupNameFilter("UOFC", com)
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
        s, new GroupNameFilter("UOFC", edu)
      );
      Assert.assertTrue("groups",  gq.getGroups().size()      == 1);
      Assert.assertTrue("members", gq.getMembers().size()     == 0);
      Assert.assertTrue("mships",  gq.getMemberships().size() == 0);
      Assert.assertTrue("stems",   gq.getStems().size()       == 0);
    }
    catch (QueryException eQ) {
      Assert.fail("unable to query: " + eQ.getMessage());
    }
  } // public void testGroupNameFilterSomethingExtensionScoped()

  public void testGroupNameFilterSomethingNameScoped() throws Exception {
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemHelper.findRootStem(s);
    Stem            edu   = StemHelper.addChildStem(root, "edu", "education");
    Group           i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    Group           uofc  = StemHelper.addChildGroup(edu, "uofc", "uchicago");
    Stem            com   = StemHelper.addChildStem(root, "com", "commercial");
    StemHelper.addChildGroup(com, "devclue", "devclue");
    GroupHelper.addMember(i2, uofc);

    GroupType custom = GroupType.createType(s, "customType");
    custom.addAttribute(s, "customAttribute", false);

    try {
      GrouperQuery gq = GrouperQuery.createQuery(
        s, new GroupNameFilter("edu:i2", com)
      );
      Assert.assertTrue("groups",  gq.getGroups().size()      == 0);
      Assert.assertTrue("members", gq.getMembers().size()     == 0);
      Assert.assertTrue("mships",  gq.getMemberships().size() == 0);
      Assert.assertTrue("stems",   gq.getStems().size()       == 0);
    }
    catch (QueryException eQ) {
      Assert.fail("unable to query: " + eQ.getMessage());
    }

    uofc.addType(custom);
    uofc.setAttribute("customAttribute", "edu:i2");

    try {
      GrouperQuery gq = GrouperQuery.createQuery(
        s, new GroupNameFilter("edu:i", edu)
      );
      Assert.assertTrue("groups",  gq.getGroups().size()      == 1);
      Assert.assertTrue("members", gq.getMembers().size()     == 1);
      Assert.assertTrue("mships",  gq.getMemberships().size() == 1);
      Assert.assertTrue("stems",   gq.getStems().size()       == 0);
    }
    catch (QueryException eQ) {
      Assert.fail("unable to query: " + eQ.getMessage());
    }
  } // public void testGroupNameFilterSomethingNameScoped()
  
  /**
   * @throws Exception
   */
  public void testGroupNameFilterAlternateName() throws Exception {
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemHelper.findRootStem(s);
    Stem            edu   = StemHelper.addChildStem(root, "edu", "education");
    Group           i2    = StemHelper.addChildGroup(edu, "i2", "Internet2");
    Group           uofc  = StemHelper.addChildGroup(edu, "UofC", "UChicago");
    Stem            com   = StemHelper.addChildStem(root, "com", "commercial");
    Group           dev   = StemHelper.addChildGroup(com, "devclue", "devclue");
   
    try {
      GrouperQuery gq = GrouperQuery.createQuery(
        s, new GroupNameFilter("edu:i2 alt", root)
      );
      Assert.assertTrue("groups",  gq.getGroups().size()      == 0);
      Assert.assertTrue("members", gq.getMembers().size()     == 0);
      Assert.assertTrue("mships",  gq.getMemberships().size() == 0);
      Assert.assertTrue("stems",   gq.getStems().size()       == 0);
    }
    catch (QueryException eQ) {
      Assert.fail("unable to query: " + eQ.getMessage());
    }
    
    i2.addAlternateName("EDU:i2 alt name");
    i2.store();

    try {
      GrouperQuery gq = GrouperQuery.createQuery(
          s, new GroupNameFilter("edu:i2 alt", root)
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
  
  /**
   * @throws Exception
   */
  public void testGroupNameFilterAlternateNameScoped() throws Exception {
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemHelper.findRootStem(s);
    Stem            edu   = StemHelper.addChildStem(root, "edu", "education");
    Stem            edu2  = StemHelper.addChildStem(root, "2edu", "education");
    Stem            edu3  = StemHelper.addChildStem(root, "ed", "education");
    Group           i2    = StemHelper.addChildGroup(edu, "i2", "Internet2");
    Group           uofc  = StemHelper.addChildGroup(edu, "UofC", "UChicago");
    Group           uofc2  = StemHelper.addChildGroup(edu2, "UofC", "UChicago");
    Group           uofc3  = StemHelper.addChildGroup(edu3, "UofC", "UChicago");
    Stem            com   = StemHelper.addChildStem(root, "com", "commercial");
    Group           dev   = StemHelper.addChildGroup(com, "devclue", "devclue");
   
    dev.addAlternateName("EDU:i2 alt name");
    dev.store();
    
    try {
      GrouperQuery gq = GrouperQuery.createQuery(
        s, new GroupNameFilter("edu:i2 Alt", edu)
      );
      Assert.assertTrue("groups",  gq.getGroups().size()      == 0);
      Assert.assertTrue("members", gq.getMembers().size()     == 0);
      Assert.assertTrue("mships",  gq.getMemberships().size() == 0);
      Assert.assertTrue("stems",   gq.getStems().size()       == 0);
    }
    catch (QueryException eQ) {
      Assert.fail("unable to query: " + eQ.getMessage());
    }
    
    i2.addAlternateName("EDU:i2 alt name2");
    i2.store();
    
    uofc.addAlternateName("something:edu:i2 ALT name");
    uofc.store();
    
    // uofc2 matches the search criteria but is not in scope
    uofc2.addAlternateName("something:edu:i2 ALT name2");
    uofc2.store();
    
    // uofc3 matches the search criteria but is not in scope
    uofc3.addAlternateName("something:edu:i2 ALT name3");
    uofc3.store();

    try {
      GrouperQuery gq = GrouperQuery.createQuery(
          s, new GroupNameFilter("edu:i2 Alt", edu)
        );
      Assert.assertTrue("groups",  gq.getGroups().size()      == 2);
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
  public void testGroupNameFilterSomethingNamePaged() {
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemHelper.findRootStem(s);
    Stem            edu   = StemHelper.addChildStem(root, "edu", "education");
    Group           i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    Group           uofc  = StemHelper.addChildGroup(edu, "uofc", "uchicago");
    Group           uofc2  = StemHelper.addChildGroup(edu, "uofc6", "uchicago2");
    Group           uofc3  = StemHelper.addChildGroup(edu, "uofc5", "uchicago3");
    Group           uofc4  = StemHelper.addChildGroup(edu, "uofc4", "uchicago4");
    Group           uofc5  = StemHelper.addChildGroup(edu, "uofc3", "uchicago5");
    Group           uofc6  = StemHelper.addChildGroup(edu, "uofc2", "uchicago6");
    Stem            com   = StemHelper.addChildStem(root, "com", "commercial");
    
    for (Group group : GrouperDAOFactory.getFactory().getGroup().getAllGroupsSecure("edu", s, s.getSubject(), AccessPrivilege.VIEW_PRIVILEGES, new QueryOptions().sort(QuerySort.asc("displayName")), null)) {
      //System.out.println(group.getName());
      group.getName();
    }
    
    for (Group group : GrouperDAOFactory.getFactory().getGroup().findAllByApproximateName("edu")) {
      //System.out.println(group.getName());
      group.getName();
    }
    
    uofc.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
    uofc.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
    
    uofc2.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
    uofc2.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
    uofc2.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW, false);
    
    uofc3.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
    uofc3.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
    uofc3.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW, false);
    
    uofc4.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
    uofc4.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
    uofc4.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW, false);
    
    uofc5.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
    uofc5.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
    
    uofc6.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
    uofc6.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);

    StemHelper.addChildGroup(com, "devclue", "devclue");
    GroupHelper.addMember(i2, uofc);
    try {
      GrouperSession.stopQuietly(s);
      s = GrouperSession.start(SubjectTestHelper.SUBJ0);
      
      GrouperQuery gq = GrouperQuery.createQuery(
        s, new GroupNameFilter("edu:uofc", root, "displayName", true, 1, 2, null));

      List<Group> groups = new ArrayList<Group>(gq.getGroups());
      assertEquals(2, GrouperUtil.length(groups));
      assertEquals("uchicago2", groups.get(0).getDisplayExtension());
      assertEquals("uchicago3", groups.get(1).getDisplayExtension());

      gq = GrouperQuery.createQuery(
          s, new GroupNameFilter("edu:uofc", root, "name", true, 2, 2, null));

      groups = new ArrayList<Group>(gq.getGroups());
      assertEquals(1, GrouperUtil.length(groups));
      assertEquals("uofc6", groups.get(0).getExtension());
    } catch (QueryException eQ) {
      Assert.fail("unable to query: " + eQ.getMessage());
    } finally {
      GrouperSession.stopQuietly(s);
    }
  } // public void testGroupNameFilterSomethingName()

  /**
   * 
   */
  public void testGroupsInStemFilterSomethingNamePaged() {
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemHelper.findRootStem(s);
    Stem            edu   = StemHelper.addChildStem(root, "edu", "education");
    Group           i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    Group           uofc  = StemHelper.addChildGroup(edu, "uofc", "uchicago");
    Group           uofc2  = StemHelper.addChildGroup(edu, "uofc6", "uchicago2");
    Group           uofc3  = StemHelper.addChildGroup(edu, "uofc5", "uchicago3");
    Group           uofc4  = StemHelper.addChildGroup(edu, "uofc4", "uchicago4");
    Group           uofc5  = StemHelper.addChildGroup(edu, "uofc3", "uchicago5");
    Group           uofc6  = StemHelper.addChildGroup(edu, "uofc2", "uchicago6");
    Stem            com   = StemHelper.addChildStem(root, "com", "commercial");
    
    i2.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
    i2.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
    
    uofc.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
    uofc.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
    
    uofc2.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
    uofc2.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
    uofc2.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW, false);
    
    uofc3.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
    uofc3.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
    uofc3.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW, false);
    
    uofc4.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
    uofc4.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
    uofc4.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW, false);
    
    uofc5.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
    uofc5.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
    
    uofc6.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
    uofc6.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
  
    StemHelper.addChildGroup(com, "devclue", "devclue");
    GroupHelper.addMember(i2, uofc);
    try {
      GrouperSession.stopQuietly(s);
      s = GrouperSession.start(SubjectTestHelper.SUBJ0);
      
      GrouperQuery gq = GrouperQuery.createQuery(
        s, new GroupsInStemFilter("edu", Scope.SUB, false, "displayName", true, 1, 2, null));
  
      List<Group> groups = new ArrayList<Group>(gq.getGroups());
      assertEquals(2, GrouperUtil.length(groups));
      assertEquals("uchicago2", groups.get(0).getDisplayExtension());
      assertEquals("uchicago3", groups.get(1).getDisplayExtension());
  
      gq = GrouperQuery.createQuery(
          s, new GroupNameFilter("edu:uofc", root, "name", true, 2, 2, null));
  
      groups = new ArrayList<Group>(gq.getGroups());
      assertEquals(1, GrouperUtil.length(groups));
      assertEquals("uofc6", groups.get(0).getExtension());
    } catch (QueryException eQ) {
      Assert.fail("unable to query: " + eQ.getMessage());
    } finally {
      GrouperSession.stopQuietly(s);
    }
  } // public void testGroupNameFilterSomethingName()

}

