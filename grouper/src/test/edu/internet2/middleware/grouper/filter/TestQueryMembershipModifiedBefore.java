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
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.R;
import edu.internet2.middleware.grouper.helper.T;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * @author shilen
 * 
 * @version $Id: TestQueryMembershipModifiedBefore.java,v 1.5 2009-12-10 08:54:15 mchyzer Exp $
 */
public class TestQueryMembershipModifiedBefore extends GrouperTest {

  public static void main(String[] args) {
    TestRunner.run(new TestQueryMembershipModifiedBefore("testFindNothing"));
    //TestRunner.run(TestQueryMembershipModifiedBefore.class);
  }

  private static final Log LOG = GrouperUtil
      .getLog(TestQueryMembershipModifiedBefore.class);

  /**
   * @param name
   */
  public TestQueryMembershipModifiedBefore(String name) {
    super(name);
  }

  /**
   * 
   */
  public void testFindSomething() {
    try {

      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.use", "false");
      RegistryReset.internal_resetRegistryAndAddTestSubjects();
      GrouperTest.initGroupsAndAttributes();


      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("stems.updateLastMembershipTime", "true");
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.updateLastMembershipTime", "true");

      Date pre = new Date();
      GrouperUtil.sleep(100);

      R r = R.populateRegistry(2, 2, 1);
      Group gA = r.getGroup("a", "a");
      Group gB = r.getGroup("b", "a");
      Group gC = r.getGroup("a", "b");
      Group gD = r.getGroup("b", "b");
      Subject subjA = r.getSubject("a");

      gC.revokePriv(AccessPrivilege.READ);
      gC.revokePriv(AccessPrivilege.VIEW);

      GrouperUtil.sleep(100);
      Date post = new Date();
      GrouperUtil.sleep(100);

      gA.addMember(subjA);
      gB.grantPriv(subjA, AccessPrivilege.UPDATE);

      GrouperQuery gq = GrouperQuery.createQuery(r.rs,
          new GroupMembershipModifiedBeforeFilter(pre, r.root));
      T.amount("groups", 0, gq.getGroups().size());
      T.amount("members", 0, gq.getMembers().size());
      T.amount("mships", 0, gq.getMemberships().size());
      T.amount("stems", 0, gq.getStems().size());

      gq = GrouperQuery.createQuery(r.rs, new GroupMembershipModifiedBeforeFilter(post,
          r.root));
      T.amount("groups", 2, gq.getGroups().size());
      T.amount("members", 0, gq.getMembers().size());
      T.amount("mships", 0, gq.getMemberships().size());
      T.amount("stems", 0, gq.getStems().size());

      GrouperSession subjASession = GrouperSession.start(subjA);

      gq = GrouperQuery.createQuery(subjASession,
          new GroupMembershipModifiedBeforeFilter(post, r.root));
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

      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("stems.updateLastMembershipTime", "true");
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.updateLastMembershipTime", "true");

      Date pre = new Date();
      GrouperUtil.sleep(100);

      R r = R.populateRegistry(2, 2, 1);
      Group gA = r.getGroup("a", "a");
      Group gB = r.getGroup("b", "a");
      Group gC = r.getGroup("a", "b");
      Group gD = r.getGroup("b", "b");
      Subject subjA = r.getSubject("a");

      gC.revokePriv(AccessPrivilege.READ);
      gC.revokePriv(AccessPrivilege.VIEW);

      GrouperUtil.sleep(100);
      Date post = new Date();
      GrouperUtil.sleep(100);

      gA.addMember(subjA);
      gB.grantPriv(subjA, AccessPrivilege.UPDATE);

      GrouperQuery gq = GrouperQuery.createQuery(r.rs,
          new GroupMembershipModifiedBeforeFilter(pre, StemFinder.findByName(r.rs,
              "i2:a", true)));
      T.amount("groups", 0, gq.getGroups().size());
      T.amount("members", 0, gq.getMembers().size());
      T.amount("mships", 0, gq.getMemberships().size());
      T.amount("stems", 0, gq.getStems().size());

      gq = GrouperQuery.createQuery(r.rs, new GroupMembershipModifiedBeforeFilter(post,
          StemFinder.findByName(r.rs, "i2:a", true)));
      T.amount("groups", 1, gq.getGroups().size());
      T.amount("members", 0, gq.getMembers().size());
      T.amount("mships", 0, gq.getMemberships().size());
      T.amount("stems", 0, gq.getStems().size());

      GrouperSession subjASession = GrouperSession.start(subjA);

      gq = GrouperQuery.createQuery(subjASession,
          new GroupMembershipModifiedBeforeFilter(post, StemFinder.findByName(r.rs,
              "i2:a", true)));
      T.amount("groups", 0, gq.getGroups().size());
      T.amount("members", 0, gq.getMembers().size());
      T.amount("mships", 0, gq.getMemberships().size());
      T.amount("stems", 0, gq.getStems().size());

      gq = GrouperQuery.createQuery(subjASession,
          new GroupMembershipModifiedBeforeFilter(post, StemFinder.findByName(r.rs,
              "i2:b", true)));
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

      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.use", "false");
      RegistryReset.internal_resetRegistryAndAddTestSubjects();
      GrouperTest.initGroupsAndAttributes();


      R r = R.populateRegistry(0, 0, 0);
      //do 5 seconds ago in case the auto-create kicked in...
      GrouperQuery gq = GrouperQuery.createQuery(r.rs,
          new GroupMembershipModifiedBeforeFilter(new Date(System.currentTimeMillis()-10000), r.root));
      T.amount("groups", 0, gq.getGroups().size());
      T.amount("members", 0, gq.getMembers().size());
      T.amount("mships", 0, gq.getMemberships().size());
      T.amount("stems", 0, gq.getStems().size());
      
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.updateLastMembershipTime", "false");

      Stem top = r.root.addChildStem("top", "top");
      top.addChildGroup("child", "child");
      
      // verify that nulls don't get returned.
      gq = GrouperQuery.createQuery(r.rs,
          new GroupMembershipModifiedBeforeFilter(new Date(), r.root));
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
