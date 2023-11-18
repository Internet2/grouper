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
import java.util.Set;

import junit.framework.Assert;
import junit.textui.TestRunner;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.DateHelper;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.R;
import edu.internet2.middleware.grouper.helper.T;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * @author  blair christensen.
 * @version $Id: TestQuery.java,v 1.2 2009-03-20 19:56:41 mchyzer Exp $
 */
public class TestQuery extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    //TestRunner.run(TestQuery.class);
    TestRunner.run(new TestQuery("testGroupModifiedBeforeFilterFindSomething"));
  }
  
  protected void setUp () {
    super.setUp();

  }

  protected void tearDown () {
    super.tearDown();
  }
  
  /**
   * @see GrouperTest#setupConfigs
   */
  @Override
  protected void setupConfigs() {
    super.setupConfigs();
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.use", "false");

  }

  private static final Log LOG = GrouperUtil.getLog(TestQuery.class);

  public TestQuery(String name) {
    super(name);
  }

  public void testStemDisplayExtensionFilterFindNothing() {
    LOG.info("testStemDisplayExtensionFilterFindNothing");
    try {
      R r = R.populateRegistry(0, 0, 0);
      GrouperQuery gq = GrouperQuery.createQuery(
        r.rs, new StemDisplayExtensionFilter("foo", r.root)
      );
      T.amount( "groups"  , 0,  gq.getGroups().size()       );
      T.amount( "members" , 0,  gq.getMembers().size()      );
      T.amount( "mships"  , 0,  gq.getMemberships().size()  );
      T.amount( "stems"   , 0,  gq.getStems().size()        );
      r.rs.stop();
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  } // public void testStemDisplayExtensionFilterFindNothing()

  public void testGroupModifiedAfterFilterFindNothing() {
    LOG.info("testGroupModifiedAfterFilterFindNothing");
    try {
      R r = R.populateRegistry(0, 0, 0);
      GrouperQuery gq = GrouperQuery.createQuery(
        r.rs, new GroupModifiedAfterFilter(new Date(), r.root)
      );
      T.amount( "groups"  , 0,  gq.getGroups().size()       );
      T.amount( "members" , 0,  gq.getMembers().size()      );
      T.amount( "mships"  , 0,  gq.getMemberships().size()  );
      T.amount( "stems"   , 0,  gq.getStems().size()        );
      r.rs.stop();
    }
    catch (Exception e) {
      Assert.fail("unexpected exception: " + e.getMessage());
    }
  } // public void testGroupModifiedAfterFilterFindNothing()

  public void testGroupModifiedAfterFilterFindSomething() {
    LOG.info("testGroupModifiedAfterFilterFindSomething");
    try {
      R     r = R.populateRegistry(2, 1, 0);
      Date  d = new Date( new Date().getTime() - T.DATE_OFFSET );
      Group a = r.getGroup("a", "a");
      Group b = r.getGroup("b", "a");
      a.setDescription("modified");
      a.store();
      b.setDescription("modified");
      b.store();
      GrouperQuery gq = GrouperQuery.createQuery(
        r.rs, new GroupModifiedAfterFilter(d, r.root)
      );
      T.amount( "groups"  , 2,  gq.getGroups().size()       );
      T.amount( "members" , 0,  gq.getMembers().size()      );
      T.amount( "mships"  , 0,  gq.getMemberships().size()  );
      T.amount( "stems"   , 0,  gq.getStems().size()        );
      r.rs.stop();
    }
    catch (Exception e) {
      Assert.fail("unexpected exception: " + e.getMessage());
    }
  } // public void testGroupModifiedAfterFilterFindSomething()

  public void testGroupModifiedAfterFilterFindSomethingScoped() {
    LOG.info("testGroupModifiedAfterFilterFindSomethingScoped");
    try {
      R     r = R.populateRegistry(2, 1, 0);
      Date  d = new Date( new Date().getTime() - T.DATE_OFFSET );
      Group a = r.getGroup("a", "a");
      Group b = r.getGroup("b", "a");
      a.setDescription("modified");
      a.store();
      b.setDescription("modified");
      b.store();
      GrouperQuery gq = GrouperQuery.createQuery(
        r.rs, new GroupModifiedAfterFilter(d, r.getStem("a"))
      );
      T.amount( "groups"  , 1,  gq.getGroups().size()       );
      T.amount( "members" , 0,  gq.getMembers().size()      );
      T.amount( "mships"  , 0,  gq.getMemberships().size()  );
      T.amount( "stems"   , 0,  gq.getStems().size()        );
      r.rs.stop();
    }
    catch (Exception e) {
      Assert.fail("unexpected exception: " + e.getMessage());
    }
  } // public void testGroupModifiedAfterFilterFindSomethingScoped()

  public void testGroupModifiedBeforeFilterFindNothing() {
    LOG.info("testGroupModifiedBeforeFilterFindNothing");
    try {
      R r = R.populateRegistry(0, 0, 0);
      GrouperQuery gq = GrouperQuery.createQuery(
        r.rs, new GroupModifiedBeforeFilter(new Date(), r.root)
      );
      Set<Group> groups = gq.getGroups();
      groups = filterOutBuiltInGroups(groups);
      T.amount( "groups: " + GrouperUtil.toStringForLog(groups)  , 0,  groups.size()       );
      T.amount( "members" , 0,  gq.getMembers().size()      );
      T.amount( "mships"  , 0,  gq.getMemberships().size()  );
      T.amount( "stems"   , 0,  gq.getStems().size()        );
      r.rs.stop();
    }
    catch (Exception e) {
      Assert.fail("unexpected exception: " + e.getMessage());
    }
  } // public void testGroupModifiedBeforeFilterFindNothing()

  public void testGroupModifiedBeforeFilterFindSomething() {
    LOG.info("testGroupModifiedBeforeFilterFindSomething");
    try {
      R     r = R.populateRegistry(2, 1, 0);
      Group a = r.getGroup("a", "a");
      Group b = r.getGroup("b", "a");
      a.setDescription("modified");
      a.store();
      b.setDescription("modified");
      b.store();
      Date  d = new Date( new Date().getTime() + T.DATE_OFFSET );
      GrouperQuery gq = GrouperQuery.createQuery(
        r.rs, new GroupModifiedBeforeFilter(d, r.root)
      );
      Set<Group> groups = gq.getGroups();
      groups = filterOutBuiltInGroups(groups);
      T.amount( "groups"  , 2,  groups.size()       );
      T.amount( "members" , 0,  gq.getMembers().size()      );
      T.amount( "mships"  , 0,  gq.getMemberships().size()  );
      T.amount( "stems"   , 0,  gq.getStems().size()        );
      r.rs.stop();
    }
    catch (Exception e) {
      Assert.fail("unexpected exception: " + e.getMessage());
    }
  } // public void testGroupModifiedBeforeFilterFindSomething()

  public void testGroupModifiedBeforeFilterFindSomethingScoped() {
    LOG.info("testGroupModifiedBeforeFilterFindSomethingScoped");
    try {
      R     r = R.populateRegistry(2, 1, 0);
      Group a = r.getGroup("a", "a");
      Group b = r.getGroup("b", "a");
      a.setDescription("modified");
      a.store();
      b.setDescription("modified");
      b.store();
      Date  d = new Date( new Date().getTime() + T.DATE_OFFSET );
      GrouperQuery gq = GrouperQuery.createQuery(
        r.rs, new GroupModifiedBeforeFilter(d, r.getStem("a"))
      );
      T.amount( "groups"  , 1,  gq.getGroups().size()       );
      T.amount( "members" , 0,  gq.getMembers().size()      );
      T.amount( "mships"  , 0,  gq.getMemberships().size()  );
      T.amount( "stems"   , 0,  gq.getStems().size()        );
      r.rs.stop();
    }
    catch (Exception e) {
      Assert.fail("unexpected exception: " + e.getMessage());
    }
  } // public void testGroupModifiedBeforeFilterFindSomethingScoped()

  public void testMembershipCreatedAfterFilter() {
    LOG.info("testMembershipCreatedAfterFilter");
    try {
      R       r       = R.populateRegistry(2, 1, 2);
      Stem    nsA     = r.getStem("a");
      Group   a       = r.getGroup("a", "a");
      Group   b       = r.getGroup("b", "a");
      Subject subjA   = r.getSubject("a");
      Subject subjB   = r.getSubject("b");
  
      GrouperUtil.sleep(100);
      Date            past  = new Date();
      GrouperUtil.sleep(100);
      a.addMember(subjA); 
      b.addMember(subjB);
      Date    future  = DateHelper.getFutureDate();
  
      // Find nothing
      GrouperQuery gq = GrouperQuery.createQuery(
        r.rs, new MembershipCreatedAfterFilter(future, StemFinder.findRootStem(r.rs))
      );
      T.amount( "nothing - groups"  , 0 , gq.getGroups().size()       );
      T.amount( "nothing - members" , 0 , gq.getMembers().size()      );
      T.amount( "nothing - mships"  , 0 , gq.getMemberships().size()  );
      T.amount( "nothing - stems"   , 0 , gq.getStems().size()        );
  
      // Find something
      gq = GrouperQuery.createQuery(
        r.rs, new MembershipCreatedAfterFilter(past, StemFinder.findRootStem(r.rs))
      );
      T.amount( "something - groups"  , 0 , gq.getGroups().size()       );
      T.amount( "something - members" , 2 , gq.getMembers().size()      );
      T.amount( "something - mships"  , 2 , gq.getMemberships().size()  );
      T.amount( "something - stems"   , 0 , gq.getStems().size()        );
  
      // Find something - scoped
      gq = GrouperQuery.createQuery(
        r.rs, new MembershipCreatedAfterFilter(past, nsA)
      );
      T.amount( "scoped - groups"   , 0 , gq.getGroups().size()       );
      T.amount( "scoped - members"  , 1 , gq.getMembers().size()      );
      T.amount( "scoped - mships"   , 1 , gq.getMemberships().size()  );
      T.amount( "scoped - stems"    , 0 , gq.getStems().size()        );
  
      r.rs.stop();
    }
    catch (Exception e) {
      Assert.fail("unexpected exception: " + e.getMessage());
    }
  } // public void testMembershipCreatedAfterFilter()

  public void testMembershipCreatedBeforeFilter() {
    LOG.info("testMembershipCreatedBeforeFilter");
    try {
      R       r       = R.populateRegistry(2, 1, 2);
      Stem    nsA     = r.getStem("a");
      Group   a       = r.getGroup("a", "a");
      Group   b       = r.getGroup("b", "a");
      Subject subjA   = r.getSubject("a");
      Subject subjB   = r.getSubject("b");
  
      GrouperUtil.sleep(100);
      Date            past  = new Date();
      GrouperUtil.sleep(100);
      a.addMember(subjA); 
      b.addMember(subjB);
      Date    future  = DateHelper.getFutureDate();
  
      // Find nothing
      GrouperQuery gq = GrouperQuery.createQuery(
        r.rs, new MembershipCreatedBeforeFilter(past, StemFinder.findRootStem(r.rs))
      );
      T.amount( "nothing - groups"  , 0 , gq.getGroups().size()       );
      T.amount( "nothing - members" , 0 , gq.getMembers().size()      );
      T.amount( "nothing - mships"  , 0 , gq.getMemberships().size()  );
      T.amount( "nothing - stems"   , 0 , gq.getStems().size()        );
  
      // Find something
      gq = GrouperQuery.createQuery(
        r.rs, new MembershipCreatedBeforeFilter(future, StemFinder.findRootStem(r.rs))
      );
      T.amount( "something - groups"  , 0 , gq.getGroups().size()       );
      T.amount( "something - members" , 2 , gq.getMembers().size()      );
      T.amount( "something - mships"  , 2 , gq.getMemberships().size()  );
      T.amount( "something - stems"   , 0 , gq.getStems().size()        );
  
      // Find something - scoped
      gq = GrouperQuery.createQuery(
        r.rs, new MembershipCreatedBeforeFilter(future, nsA)
      );
      T.amount( "scoped - groups"   , 0 , gq.getGroups().size()       );
      T.amount( "scoped - members"  , 1 , gq.getMembers().size()      );
      T.amount( "scoped - mships"   , 1 , gq.getMemberships().size()  );
      T.amount( "scoped - stems"    , 0 , gq.getStems().size()        );
  
      r.rs.stop();
    }
    catch (Exception e) {
      Assert.fail("unexpected exception: " + e.getMessage());
    }
  } // public void testMembershipCreatedBeforeFilter()

  public void testStemDisplayExtensionFilterFindSomething() {
    LOG.info("testStemDisplayExtensionFilterFindSomething");
    try {
      R r = R.populateRegistry(2, 0, 0);
      GrouperQuery gq = GrouperQuery.createQuery(
        r.rs, new StemDisplayExtensionFilter("stem", r.root)
      );
      T.amount( "groups"  , 0,  gq.getGroups().size()       );
      T.amount( "members" , 0,  gq.getMembers().size()      );
      T.amount( "mships"  , 0,  gq.getMemberships().size()  );
      T.amount( "stems"   , 2,  gq.getStems().size()        );
      r.rs.stop();
    }
    catch (Exception e) {
      Assert.fail("unexpected exception: " + e.getMessage());
    }
  } // public void testStemDisplayExtensionFilterFindSomething()

  public void testStemDisplayExtensionFilterFindSomethingScoped() {
    LOG.info("testStemDisplayExtensionFilterFindSomethingScoped");
    try {
      R r = R.populateRegistry(2, 0, 0);
      GrouperQuery gq = GrouperQuery.createQuery(
        r.rs, new StemDisplayExtensionFilter("stem a", r.ns)
      );
      T.amount( "groups"  , 0,  gq.getGroups().size()       );
      T.amount( "members" , 0,  gq.getMembers().size()      );
      T.amount( "mships"  , 0,  gq.getMemberships().size()  );
      T.amount( "stems"   , 1,  gq.getStems().size()        );
      r.rs.stop();
    }
    catch (Exception e) {
      Assert.fail("unexpected exception: " + e.getMessage());
    }
  } // public void testStemDisplayExtensionFilterFindSomethingScoped()

  public void testStemDisplayNameFilterFindNothing() {
    LOG.info("testStemDisplayNameFilterFindNothing");
    try {
      R r = R.populateRegistry(0, 0, 0);
      GrouperQuery gq = GrouperQuery.createQuery(
        r.rs, new StemDisplayNameFilter("foo", r.root)
      );
      T.amount( "groups"  , 0,  gq.getGroups().size()       );
      T.amount( "members" , 0,  gq.getMembers().size()      );
      T.amount( "mships"  , 0,  gq.getMemberships().size()  );
      T.amount( "stems"   , 0,  gq.getStems().size()        );
      r.rs.stop();
    }
    catch (Exception e) {
      Assert.fail("unexpected exception: " + e.getMessage());
    }
  } // public void testStemDisplayNameFilterFindNothing()

  public void testStemDisplayNameFilterFindSomething() {
    LOG.info("testStemDisplayNameFilterFindSomething");
    try {
      R r = R.populateRegistry(2, 0, 0);
      GrouperQuery gq = GrouperQuery.createQuery(
        r.rs, new StemDisplayNameFilter("stem", r.root)
      );
      T.amount( "groups"  , 0,  gq.getGroups().size()       );
      T.amount( "members" , 0,  gq.getMembers().size()      );
      T.amount( "mships"  , 0,  gq.getMemberships().size()  );
      T.amount( "stems"   , 2,  gq.getStems().size()        );
      r.rs.stop();
    }
    catch (Exception e) {
      Assert.fail("unexpected exception: " + e.getMessage());
    }
  } // public void testStemDisplayNameFilterFindSomething()

  public void testStemDisplayNameFilterFindSomethingScoped() {
    LOG.info("testStemDisplayNameFilterFindSomethingScoped");
    try {
      R r = R.populateRegistry(2, 0, 0);
      GrouperQuery gq = GrouperQuery.createQuery(
        r.rs, new StemDisplayNameFilter("stem a", r.ns)
      );
      T.amount( "groups"  , 0,  gq.getGroups().size()       );
      T.amount( "members" , 0,  gq.getMembers().size()      );
      T.amount( "mships"  , 0,  gq.getMemberships().size()  );
      T.amount( "stems"   , 1,  gq.getStems().size()        );
      r.rs.stop();
    }
    catch (Exception e) {
      Assert.fail("unexpected exception: " + e.getMessage());
    }
  } // public void testStemDisplayNameFilterFindSomethingScoped()

  public void testStemExtensionFilterFindNothing() {
    LOG.info("testStemExtensionFilterFindNothing");
    try {
      R r = R.populateRegistry(0, 0, 0);
      GrouperQuery gq = GrouperQuery.createQuery(
        r.rs, new StemExtensionFilter("foo", r.root)
      );
      T.amount( "groups"  , 0,  gq.getGroups().size()       );
      T.amount( "members" , 0,  gq.getMembers().size()      );
      T.amount( "mships"  , 0,  gq.getMemberships().size()  );
      T.amount( "stems"   , 0,  gq.getStems().size()        );
      r.rs.stop();
    }
    catch (Exception e) {
      Assert.fail("unexpected exception: " + e.getMessage());
    }
  } // public void testStemExtensionFilterFindNothing()

  public void testStemExtensionFilterFindSomething() {
    LOG.info("testStemExtensionFilterFindSomething");
    try {
      R r = R.populateRegistry(2, 0, 0);
      GrouperQuery gq = GrouperQuery.createQuery(
        r.rs, new StemExtensionFilter("a", r.root)
      );
      T.amount( "groups"  , 0,  gq.getGroups().size()       );
      T.amount( "members" , 0,  gq.getMembers().size()      );
      T.amount( "mships"  , 0,  gq.getMemberships().size()  );
      assertTrue(gq.getStems().size() >= 1);
      
      //find a
      boolean foundIt = false;
      for (Stem stem : gq.getStems()) {
        if (StringUtils.equals("a", stem.getExtension())) {
          foundIt = true;
        }
      }
      
      assertTrue(foundIt);

      foundIt = false;
      for (Stem stem : gq.getStems()) {
        if (StringUtils.equals("b", stem.getExtension())) {
          foundIt = true;
        }
      }
      
      assertFalse(foundIt);

      r.rs.stop();
    }
    catch (Exception e) {
      Assert.fail("unexpected exception: " + e.getMessage());
    }
  } // public void testStemExtensionFilterFindSomething()

  public void testStemExtensionFilterFindSomethingScoped() {
    LOG.info("testStemExtensionFilterFindSomethingScoped");
    try {
      R r = R.populateRegistry(2, 0, 0);
      GrouperQuery gq = GrouperQuery.createQuery(
        r.rs, new StemExtensionFilter("a", r.ns)
      );
      T.amount( "groups"  , 0,  gq.getGroups().size()       );
      T.amount( "members" , 0,  gq.getMembers().size()      );
      T.amount( "mships"  , 0,  gq.getMemberships().size()  );
      T.amount( "stems"   , 1,  gq.getStems().size()        );
      r.rs.stop();
    }
    catch (Exception e) {
      Assert.fail("unexpected exception: " + e.getMessage());
    }
  } // public void testStemExtensionFilterFindSomethingScoped()

  public void testStemNameFilterFindSomething() {
    LOG.info("testStemNameFilterFindSomething");
    try {
      R r = R.populateRegistry(2, 0, 0);
      GrouperQuery gq = GrouperQuery.createQuery(
        r.rs, new StemNameFilter("i2:a", r.root)
      );
      T.amount( "groups"  , 0,  gq.getGroups().size()       );
      T.amount( "members" , 0,  gq.getMembers().size()      );
      T.amount( "mships"  , 0,  gq.getMemberships().size()  );
      T.amount( "stems"   , 1,  gq.getStems().size()        );
      r.rs.stop();
    }
    catch (Exception e) {
      Assert.fail("unexpected exception: " + e.getMessage());
    }
  } // public void testStemNameFilterFindSomething()

  public void testStemNameFilterFindSomethingScoped() {
    LOG.info("testStemNameFilterFindSomethingScoped");
    try {
      R r = R.populateRegistry(2, 0, 0);
      GrouperQuery gq = GrouperQuery.createQuery(
        r.rs, new StemNameFilter("i2:a", r.ns)
      );
      T.amount( "groups"  , 0,  gq.getGroups().size()       );
      T.amount( "members" , 0,  gq.getMembers().size()      );
      T.amount( "mships"  , 0,  gq.getMemberships().size()  );
      T.amount( "stems"   , 1,  gq.getStems().size()        );
      r.rs.stop();
    }
    catch (Exception e) {
      Assert.fail("unexpected exception: " + e.getMessage());
    }
  } // public void testStemNameFilterFindSomethingScoped()

  public void testStemNameFilterFindNothing() {
    LOG.info("testStemNameFilterFindNothing");
    try {
      R r = R.populateRegistry(0, 0, 0);
      GrouperQuery gq = GrouperQuery.createQuery(
        r.rs, new StemNameFilter("foo", r.root)
      );
      T.amount( "groups"  , 0,  gq.getGroups().size()       );
      T.amount( "members" , 0,  gq.getMembers().size()      );
      T.amount( "mships"  , 0,  gq.getMemberships().size()  );
      T.amount( "stems"   , 0,  gq.getStems().size()        );
      r.rs.stop();
    }
    catch (Exception e) {
      Assert.fail("unexpected exception: " + e.getMessage());
    }
  } // public void testStemNameFilterFindNothing()

}

