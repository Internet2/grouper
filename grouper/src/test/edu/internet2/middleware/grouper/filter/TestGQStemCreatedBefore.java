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
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import junit.framework.Assert;
import junit.framework.TestCase;
import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.exception.QueryException;
import edu.internet2.middleware.grouper.helper.DateHelper;
import edu.internet2.middleware.grouper.helper.GroupHelper;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Test {@link StemCreatedBeforeFilter}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestGQStemCreatedBefore.java,v 1.2 2009-03-20 19:56:41 mchyzer Exp $
 */
public class TestGQStemCreatedBefore extends TestCase {

  public static void main(String[] args) {
    TestRunner.run(new TestGQStemCreatedBefore("testStemCreatedBeforeFilterNothing"));
  }
  
  public TestGQStemCreatedBefore(String name) {
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

  public void testStemCreatedBeforeFilterNothing() {
    GrouperUtil.sleep(100);
    Date            when  = new Date();
    GrouperUtil.sleep(100);
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
        s, new StemCreatedBeforeFilter(when, root)
      );
      Assert.assertTrue("groups",  gq.getGroups().size()      == 0);
      Assert.assertTrue("members", gq.getMembers().size()     == 0);
      Assert.assertTrue("mships",  gq.getMemberships().size() == 0);
      for (Stem stem : gq.getStems()) {
        if (stem.getName().startsWith("edu")) {
          throw new RuntimeException("Stem cant be edu");
        }
        if (stem.getName().startsWith("edu")) {
          throw new RuntimeException("Stem cant be edu");
        }
      }
    }
    catch (QueryException eQ) {
      Assert.fail("unable to query: " + eQ.getMessage());
    }
  } // public void testStemCreatedBeforeFilterNothing()

  public void testStemCreatedBeforeFilterSomething() {
    Date            when  = DateHelper.getFutureDate();
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
        s, new StemCreatedBeforeFilter(when, root)
      );
      Assert.assertTrue("groups",  gq.getGroups().size()      == 0);
      Assert.assertTrue("members", gq.getMembers().size()     == 0);
      Assert.assertTrue("mships",  gq.getMemberships().size() == 0);
      Assert.assertTrue("stems",   gq.getStems().size()       >= 2);
      
      //find edu
      boolean foundIt = false;
      for (Stem stem : gq.getStems()) {
        if (StringUtils.equals("edu", stem.getName())) {
          foundIt = true;
        }
      }
      
      assertTrue(foundIt);

      //find com
      foundIt = false;
      for (Stem stem : gq.getStems()) {
        if (StringUtils.equals("com", stem.getName())) {
          foundIt = true;
        }
      }
      
      assertTrue(foundIt);

      
    }
    catch (QueryException eQ) {
      Assert.fail("unable to query: " + eQ.getMessage());
    }
  } // public void testStemCreatedBefore()

  public void testStemCreatedBeforeFilterSomethingScoped() {
    Date            when  = DateHelper.getFutureDate();
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemHelper.findRootStem(s);
    Stem            edu   = StemHelper.addChildStem(root, "edu", "education");
    Group           i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    Group           uofc  = StemHelper.addChildGroup(edu, "uofc", "uchicago");
    GroupHelper.addMember(i2, uofc);
    Stem            com   = StemHelper.addChildStem(root, "com", "commercial");
    StemHelper.addChildGroup(com, "devclue", "devclue");
    try {
      GrouperQuery gq = GrouperQuery.createQuery(
        s, new StemCreatedBeforeFilter(when, edu)
      );
      Assert.assertTrue("groups",   gq.getGroups().size()       == 0);
      Assert.assertTrue("members",  gq.getMembers().size()      == 0);
      Assert.assertTrue("mships",   gq.getMemberships().size()  == 0);
      Assert.assertTrue("stems",    gq.getStems().size()        == 0);
    }
    catch (QueryException eQ) {
      Assert.fail("unable to query: " + eQ.getMessage());
    }

    try {
      Stem test = StemHelper.addChildStem(edu, "test", "test");
      Stem test2 = StemHelper.addChildStem(test, "test2", "test2");

      GrouperQuery gq = GrouperQuery.createQuery(
        s, new StemCreatedBeforeFilter(when, edu)
      );
      Assert.assertTrue("groups",   gq.getGroups().size()       == 0);
      Assert.assertTrue("members",  gq.getMembers().size()      == 0);
      Assert.assertTrue("mships",   gq.getMemberships().size()  == 0);
      Assert.assertTrue("stems",    gq.getStems().size()        == 2);
    }
    catch (QueryException eQ) {
      Assert.fail("unable to query: " + eQ.getMessage());
    }
  } // public void testStemCreatedBeforeSomethingScoped()

}

