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
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.exception.QueryException;
import edu.internet2.middleware.grouper.filter.GroupAttributeFilter;
import edu.internet2.middleware.grouper.filter.GrouperQuery;
import edu.internet2.middleware.grouper.helper.GroupHelper;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.registry.RegistryReset;

/**
 * Test {@link GroupAttributeFilter}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestGQGroupAttribute.java,v 1.2 2009-03-20 19:56:41 mchyzer Exp $
 */
public class TestGQGroupAttribute extends TestCase {

  public TestGQGroupAttribute(String name) {
    super(name);
  }

  protected void setUp () {
    RegistryReset.internal_resetRegistryAndAddTestSubjects();
  }

  protected void tearDown () {
    // Nothing 
  }

  // Tests

  public void testGroupAttributeFilterNothing() {
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
        s, new GroupAttributeFilter("extension", "nothing", root)
      );
      Assert.assertTrue("groups",  gq.getGroups().size()      == 0);
      Assert.assertTrue("members", gq.getMembers().size()     == 0);
      Assert.assertTrue("mships",  gq.getMemberships().size() == 0);
      Assert.assertTrue("stems",   gq.getStems().size()       == 0);
    }
    catch (QueryException eQ) {
      Assert.fail("unable to query: " + eQ.getMessage());
    }
  } // public void testGroupAttributeFilterNothing()

  public void testGroupAttributeFilterSomething() {
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
        s, new GroupAttributeFilter("extension", "uofc", root)
      );
      Assert.assertTrue("groups",  gq.getGroups().size()      == 1);
      Assert.assertTrue("members", gq.getMembers().size()     == 0);
      Assert.assertTrue("mships",  gq.getMemberships().size() == 0);
      Assert.assertTrue("stems",   gq.getStems().size()       == 0);
    }
    catch (QueryException eQ) {
      Assert.fail("unable to query: " + eQ.getMessage());
    }
  } // public void testGroupAttributeFilterSomething()

  public void testGroupAttributeFilterSomethingScoped() throws Exception {
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemHelper.findRootStem(s);
    Stem            edu   = StemHelper.addChildStem(root, "edu", "education");
    Group           i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    Group           uofc  = StemHelper.addChildGroup(edu, "uofc", "uchicago");
    Stem            com   = StemHelper.addChildStem(root, "com", "commercial");
    Group           devclue = StemHelper.addChildGroup(com, "devclue", "devclue");
    GroupHelper.addMember(i2, uofc);

    GroupType custom = GroupType.createType(s, "customType");
    custom.addAttribute(s, "customAttribute", AccessPrivilege.VIEW, AccessPrivilege.UPDATE, false);

    try {
      GrouperQuery gq = GrouperQuery.createQuery(
        s, new GroupAttributeFilter("extension", "uofc", com)
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
    devclue.setAttribute("customAttribute", "String with i2 within");
    uofc.setAttribute("customAttribute", "String with i2 within");
    devclue.store();
    uofc.store();

    try {
      GrouperQuery gq = GrouperQuery.createQuery(
        s, new GroupAttributeFilter("customAttribute", "i2", com)
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
        s, new GroupAttributeFilter("customAttribute", "i2", edu)
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
        s, new GroupAttributeFilter("extension", "i2", edu)
      );
      Assert.assertTrue("groups",  gq.getGroups().size()      == 1);
      Assert.assertTrue("members", gq.getMembers().size()     == 1);
      Assert.assertTrue("mships",  gq.getMemberships().size() == 1);
      Assert.assertTrue("stems",   gq.getStems().size()       == 0);
    }
    catch (QueryException eQ) {
      Assert.fail("unable to query: " + eQ.getMessage());
    }
  } // public void testGroupAttributeFilterSomethingDisplayExtensionScoped()

}

