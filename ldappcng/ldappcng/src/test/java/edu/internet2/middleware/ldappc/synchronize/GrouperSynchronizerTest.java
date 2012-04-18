/*******************************************************************************
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
 ******************************************************************************/
/*
 * Copyright 2006-2007 The University Of Chicago Copyright 2006-2007 University
 * Corporation for Advanced Internet Development, Inc. Copyright 2006-2007 EDUCAUSE
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

package edu.internet2.middleware.ldappc.synchronize;

import java.util.Date;

import javax.naming.NamingException;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.cfg.ApiConfig;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.ldappc.BaseLdappcTestCase;
import edu.internet2.middleware.ldappc.Ldappc;
import edu.internet2.middleware.ldappc.exception.ConfigurationException;

public class GrouperSynchronizerTest extends BaseLdappcTestCase {

  public static void main(String[] args) {
    //TestRunner.run(GrouperSynchronizerTest.class);
    TestRunner.run(new GrouperSynchronizerTest("testStatusUnknown"));
  }
  
  public GrouperSynchronizerTest(String name) {
    super(name);
  }

  public void setUp() {
    super.setUp();

    ApiConfig.testConfig.put("stems.updateLastMembershipTime", "true");
    ApiConfig.testConfig.put("groups.updateLastMembershipTime", "true");
    
    try {
      if (this.useEmbedded()) {
        this.setUpLdapContext();
      }
      setUpLdappc(pathToConfig, pathToProperties);
    } catch (Exception e) {
      e.printStackTrace();
      fail("An error occurred : " + e);
    }
  }

  public void testStatusUnknown() throws ConfigurationException, NamingException {

    Group group = StemHelper.addChildGroup(this.edu, "groupA", "Group A");

    // workaround Group.getLastMembershipChange() test environment nuance
    Group g = GroupFinder.findByUuid(grouperSession, group.getUuid(), true);

    assertEquals(
        "Status should be UNKNOWN when InputOptions.getLastModifyTime() is null.",
        Ldappc.STATUS_UNKNOWN, ldappc.determineStatus(g));
  }

  public void testStatusNew() throws ConfigurationException, NamingException {

    ldappc.getOptions().setLastModifyTime(new Date());

    Group group = StemHelper.addChildGroup(this.edu, "groupA", "Group A");

    // workaround Group.getLastMembershipChange() test environment nuance
    Group g = GroupFinder.findByUuid(grouperSession, group.getUuid(), true);

    assertEquals(
        "Status should be NEW when InputOptions.getLastModifyTime() is before Group.getCreateTime().",
        Ldappc.STATUS_NEW, ldappc.determineStatus(g));
  }

  public void testStatusNewWithModification() throws ConfigurationException,
      NamingException {

    ldappc.getOptions().setLastModifyTime(new Date());

    Group group = StemHelper.addChildGroup(this.edu, "groupA", "Group A");

    group.setDescription("description");

    // workaround Group.getLastMembershipChange() test environment nuance
    Group g = GroupFinder.findByUuid(grouperSession, group.getUuid(), true);

    assertEquals(
        "Status should be NEW when InputOptions.getLastModifyTime() is before Group.getCreateTime().",
        Ldappc.STATUS_NEW, ldappc.determineStatus(g));
  }

  public void testStatusNewWithMemberModification() throws ConfigurationException,
      NamingException {

    ldappc.getOptions().setLastModifyTime(new Date());

    Group group = StemHelper.addChildGroup(this.edu, "groupA", "Group A");

    group.addMember(SubjectTestHelper.SUBJ0);

    // workaround Group.getLastMembershipChange() test environment nuance
    Group g = GroupFinder.findByUuid(grouperSession, group.getUuid(), true);

    assertEquals(
        "Status should be NEW when InputOptions.getLastModifyTime() is before Group.getCreateTime().",
        Ldappc.STATUS_NEW, ldappc.determineStatus(g));
  }

  public void testStatusUnchanged() throws ConfigurationException, NamingException {

    Group group = StemHelper.addChildGroup(this.edu, "groupA", "Group A");

    ldappc.getOptions().setLastModifyTime(new Date());

    // workaround Group.getLastMembershipChange() test environment nuance
    Group g = GroupFinder.findByUuid(grouperSession, group.getUuid(), true);

    assertEquals(
        "Status should be UNCHANGED when InputOptions.getLastModifyTime() is after Group.getCreateTime().",
        Ldappc.STATUS_UNCHANGED, ldappc.determineStatus(g));
  }

  public void testStatusUnchangedWithModification() throws ConfigurationException,
      NamingException {

    Group group = StemHelper.addChildGroup(this.edu, "groupA", "Group A");

    group.setDescription("description");

    ldappc.getOptions().setLastModifyTime(new Date());

    // workaround Group.getLastMembershipChange() test environment nuance
    Group g = GroupFinder.findByUuid(grouperSession, group.getUuid(), true);

    assertEquals(
        "Status should be UNCHANGED when InputOptions.getLastModifyTime() is after Group.getCreateTime() and Group.getModifyTime().",
        Ldappc.STATUS_UNCHANGED, ldappc.determineStatus(g));
  }

  public void testStatusUnchangedWithMemberModification() throws ConfigurationException,
      NamingException {

    Group group = StemHelper.addChildGroup(this.edu, "groupA", "Group A");

    group.addMember(SubjectTestHelper.SUBJ0);

    ldappc.getOptions().setLastModifyTime(new Date());

    // workaround Group.getLastMembershipChange() test environment nuance
    Group g = GroupFinder.findByUuid(grouperSession, group.getUuid(), true);

    assertEquals(
        "Status should be UNCHANGED when InputOptions.getLastModifyTime() is after Group.getCreateTime() and Group.getLastMembershipChange().",
        Ldappc.STATUS_UNCHANGED, ldappc.determineStatus(g));
  }

  public void testStatusModified() throws ConfigurationException, NamingException {

    Group group = StemHelper.addChildGroup(this.edu, "groupA", "Group A");

    ldappc.getOptions().setLastModifyTime(new Date());

    group.addMember(SubjectTestHelper.SUBJ0);

    // workaround Group.getLastMembershipChange() test environment nuance
    Group g = GroupFinder.findByUuid(grouperSession, group.getUuid(), true,
        new QueryOptions().secondLevelCache(false));

    assertEquals(
        "Status should be MODIFIED when InputOptions.getLastModifyTime() is after Group.getCreateTime() and before Group.getLastMembershipChange().",
        Ldappc.STATUS_MODIFIED, ldappc.determineStatus(g));
  }
}
