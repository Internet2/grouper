/*
 * Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004-2007 The University Of Chicago
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package edu.internet2.middleware.grouper.filter;

import java.util.Date;

import junit.framework.Assert;
import junit.textui.TestRunner;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.cfg.ApiConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.R;
import edu.internet2.middleware.grouper.helper.T;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * @author shilen
 * 
 * @version $Id: TestQueryMembershipModifiedAfter.java,v 1.5 2009-11-15 05:17:17 mchyzer Exp $
 */
public class TestQueryMembershipModifiedAfter extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    //TestRunner.run(new TestQueryMembershipModifiedAfter("testFindNothing"));
    TestRunner.run(TestQueryMembershipModifiedAfter.class);
  }
  
  private static final Log LOG = GrouperUtil
      .getLog(TestQueryMembershipModifiedAfter.class);

  /**
   * @param name
   */
  public TestQueryMembershipModifiedAfter(String name) {
    super(name);
  }

  /**
   * 
   */
  public void testFindSomething() {
    try {

      ApiConfig.testConfig.put("stems.updateLastMembershipTime", "true");
      ApiConfig.testConfig.put("groups.updateLastMembershipTime", "true");

      R r = R.populateRegistry(2, 2, 1);
      Group gA = r.getGroup("a", "a");
      Group gB = r.getGroup("b", "a");
      Group gC = r.getGroup("a", "b");
      Group gD = r.getGroup("b", "b");
      Subject subjA = r.getSubject("a");

      gA.revokePriv(AccessPrivilege.READ);
      gA.revokePriv(AccessPrivilege.VIEW);

      GrouperUtil.sleep(100);
      Date pre = new Date();
      GrouperUtil.sleep(100);

      gA.addMember(subjA);
      gB.grantPriv(subjA, AccessPrivilege.UPDATE);

      GrouperUtil.sleep(100);
      Date post = new Date();

      GrouperQuery gq = GrouperQuery.createQuery(r.rs,
          new GroupMembershipModifiedAfterFilter(post, r.root));
      T.amount("groups", 0, gq.getGroups().size());
      T.amount("members", 0, gq.getMembers().size());
      T.amount("mships", 0, gq.getMemberships().size());
      T.amount("stems", 0, gq.getStems().size());

      gq = GrouperQuery.createQuery(r.rs, new GroupMembershipModifiedAfterFilter(pre,
          r.root));
      T.amount("groups", 2, gq.getGroups().size());
      T.amount("members", 1, gq.getMembers().size());
      T.amount("mships", 1, gq.getMemberships().size());
      T.amount("stems", 0, gq.getStems().size());

      GrouperSession subjASession = GrouperSession.start(subjA);

      gq = GrouperQuery.createQuery(subjASession, new GroupMembershipModifiedAfterFilter(
          pre, r.root));
      T.amount("groups", 1, gq.getGroups().size());
      T.amount("members", 0, gq.getMembers().size());
      T.amount("mships", 0, gq.getMemberships().size());
      T.amount("stems", 0, gq.getStems().size());

      subjASession.stop();

      r.rs.stop();
    } catch (Exception e) {
      Assert.fail("unexpected exception: " + e.getMessage());
    }
  }

  /**
   * 
   */
  public void testFindSomethingScoped() {
    try {

      ApiConfig.testConfig.put("stems.updateLastMembershipTime", "true");
      ApiConfig.testConfig.put("groups.updateLastMembershipTime", "true");

      R r = R.populateRegistry(2, 2, 1);
      Group gA = r.getGroup("a", "a");
      Group gB = r.getGroup("b", "a");
      Group gC = r.getGroup("a", "b");
      Group gD = r.getGroup("b", "b");
      Subject subjA = r.getSubject("a");

      gA.revokePriv(AccessPrivilege.READ);
      gA.revokePriv(AccessPrivilege.VIEW);

      GrouperUtil.sleep(100);
      Date pre = new Date();
      GrouperUtil.sleep(100);

      gA.addMember(subjA);
      gB.grantPriv(subjA, AccessPrivilege.UPDATE);

      GrouperUtil.sleep(100);
      Date post = new Date();

      GrouperQuery gq = GrouperQuery.createQuery(r.rs,
          new GroupMembershipModifiedAfterFilter(post, StemFinder.findByName(r.rs,
              "i2:a", true)));
      T.amount("groups", 0, gq.getGroups().size());
      T.amount("members", 0, gq.getMembers().size());
      T.amount("mships", 0, gq.getMemberships().size());
      T.amount("stems", 0, gq.getStems().size());

      gq = GrouperQuery.createQuery(r.rs, new GroupMembershipModifiedAfterFilter(pre,
          StemFinder.findByName(r.rs, "i2:a", true)));
      T.amount("groups", 1, gq.getGroups().size());
      T.amount("members", 1, gq.getMembers().size());
      T.amount("mships", 1, gq.getMemberships().size());
      T.amount("stems", 0, gq.getStems().size());

      GrouperSession subjASession = GrouperSession.start(subjA);

      gq = GrouperQuery.createQuery(subjASession, new GroupMembershipModifiedAfterFilter(
          pre, StemFinder.findByName(r.rs, "i2:a", true)));
      T.amount("groups", 0, gq.getGroups().size());
      T.amount("members", 0, gq.getMembers().size());
      T.amount("mships", 0, gq.getMemberships().size());
      T.amount("stems", 0, gq.getStems().size());

      gq = GrouperQuery.createQuery(subjASession, new GroupMembershipModifiedAfterFilter(
          pre, StemFinder.findByName(r.rs, "i2:b", true)));
      T.amount("groups", 1, gq.getGroups().size());
      T.amount("members", 0, gq.getMembers().size());
      T.amount("mships", 0, gq.getMemberships().size());
      T.amount("stems", 0, gq.getStems().size());

      subjASession.stop();

      r.rs.stop();
    } catch (Exception e) {
      Assert.fail("unexpected exception: " + e.getMessage());
    }
  }

  /**
   * 
   */
  public void testFindNothing() {
    try {
      R r = R.populateRegistry(0, 0, 0);
      GrouperQuery gq = GrouperQuery.createQuery(r.rs,
          new GroupMembershipModifiedAfterFilter(new Date(), r.root));
      T.amount("groups", 0, gq.getGroups().size());
      T.amount("members", 0, gq.getMembers().size());
      T.amount("mships", 0, gq.getMemberships().size());
      T.amount("stems", 0, gq.getStems().size());
      
      ApiConfig.testConfig.put("groups.updateLastMembershipTime", "false");

      GrouperUtil.sleep(100);
      long now = System.currentTimeMillis();
      GrouperUtil.sleep(100);
      
      Stem top = r.root.addChildStem("top", "top");
      top.addChildGroup("child", "child");
      
      // verify that nulls don't get returned.
      gq = GrouperQuery.createQuery(r.rs,
          new GroupMembershipModifiedAfterFilter(new Date(now), r.root));
      T.amount("groups", 0, gq.getGroups().size());
      T.amount("members", 0, gq.getMembers().size());
      T.amount("mships", 0, gq.getMemberships().size());
      T.amount("stems", 0, gq.getStems().size());

      r.rs.stop();
    } catch (Exception e) {
      Assert.fail("unexpected exception: " + e.getMessage());
    }
  }

}
