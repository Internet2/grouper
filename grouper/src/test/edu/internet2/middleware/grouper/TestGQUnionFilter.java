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

import  edu.internet2.middleware.grouper.*;
import  edu.internet2.middleware.subject.*;
import  edu.internet2.middleware.subject.provider.*;
import  java.util.*;
import  junit.framework.*;

/**
 * Test {@link UnionFilter}.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestGQUnionFilter.java,v 1.1.2.1 2006-04-10 19:07:20 blair Exp $
 */
public class TestGQUnionFilter extends TestCase {

  public TestGQUnionFilter(String name) {
    super(name);
  }

  protected void setUp () {
    RegistryReset.resetRegistryAndAddTestSubjects();
  }

  protected void tearDown () {
    // Nothing 
  }

  // Tests

  public void testUnionFilterNothing() {
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemHelper.findRootStem(s);
    Stem            edu   = StemHelper.addChildStem(root, "edu", "education");
    Group           i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    Group           uofc  = StemHelper.addChildGroup(edu, "uofc", "uchicago");
    Stem            com   = StemHelper.addChildStem(root, "com", "commercial");
    Group           dc    = StemHelper.addChildGroup(com, "devclue", "devclue");
    GroupHelper.addMember(i2, uofc);
    try {
      GrouperQuery gq = GrouperQuery.createQuery(
        s, 
        new UnionFilter(
          new NullFilter(), new NullFilter()
        )
      );
      Assert.assertTrue("groups",  gq.getGroups().size()      == 0);
      Assert.assertTrue("members", gq.getMembers().size()     == 0);
      Assert.assertTrue("mships",  gq.getMemberships().size() == 0);
      Assert.assertTrue("stems",   gq.getStems().size()       == 0);
    }
    catch (QueryException eQ) {
      Assert.fail("unable to query: " + eQ.getMessage());
    }
  } // public void testUnionFilterNothing()

  public void testUnionFilterSomething() {
    Date            when  = DateHelper.getPastDate();
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemHelper.findRootStem(s);
    Stem            edu   = StemHelper.addChildStem(root, "edu", "education");
    Group           i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    Group           uofc  = StemHelper.addChildGroup(edu, "uofc", "uchicago");
    Stem            com   = StemHelper.addChildStem(root, "com", "commercial");
    Group           dc    = StemHelper.addChildGroup(com, "devclue", "devclue");
    GroupHelper.addMember(i2, uofc);
    try {
      GrouperQuery gq = GrouperQuery.createQuery(
        s, 
        new UnionFilter(
          new StemCreatedAfterFilter(when, root),
          new GroupCreatedAfterFilter(when, root) 
        )
      );
      Assert.assertTrue("groups",  gq.getGroups().size()      == 3);
      Assert.assertTrue("members", gq.getMembers().size()     == 1);
      Assert.assertTrue("mships",  gq.getMemberships().size() == 1);
      Assert.assertTrue("stems",   gq.getStems().size()       == 2);
    }
    catch (QueryException eQ) {
      Assert.fail("unable to query: " + eQ.getMessage());
    }
  } // public void testAnd()

  public void testUnionFilterSomethingScoped() {
    Date            when  = DateHelper.getPastDate();
    GrouperSession  s     = SessionHelper.getRootSession();
    Stem            root  = StemHelper.findRootStem(s);
    Stem            edu   = StemHelper.addChildStem(root, "edu", "education");
    Group           i2    = StemHelper.addChildGroup(edu, "i2", "internet2");
    Group           uofc  = StemHelper.addChildGroup(edu, "uofc", "uchicago");
    GroupHelper.addMember(i2, uofc);
    Stem            com   = StemHelper.addChildStem(root, "com", "commercial");
    Group           dc    = StemHelper.addChildGroup(com, "devclue", "devclue");
    try {
      GrouperQuery gq = GrouperQuery.createQuery(
        s, 
        new UnionFilter(
          new GroupCreatedAfterFilter(when, root),
          new GroupCreatedAfterFilter(when, edu) 
        )
      );
      Assert.assertTrue("groups",   gq.getGroups().size()       == 3);
      Assert.assertTrue("members",  gq.getMembers().size()      == 1);
      Assert.assertTrue("mships",   gq.getMemberships().size()  == 1);
      Assert.assertTrue("stems",    gq.getStems().size()        == 0);
    }
    catch (QueryException eQ) {
      Assert.fail("unable to query: " + eQ.getMessage());
    }
  } // public void testAndSomethingScoped()

}

