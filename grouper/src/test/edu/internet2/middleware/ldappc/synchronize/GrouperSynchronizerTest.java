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

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.cfg.ApiConfig;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.ldappc.BaseLdappcTestCase;
import edu.internet2.middleware.ldappc.ConfigManager;
import edu.internet2.middleware.ldappc.InputOptions;
import edu.internet2.middleware.ldappc.LdappcConfigurationException;

public class GrouperSynchronizerTest extends BaseLdappcTestCase {

  private InputOptions options = new InputOptions();

  private ConfigManager configuration;

  public GrouperSynchronizerTest(String name) {
    super(name);
  }

  public void setUp() {
    super.setUp();

    ApiConfig.testConfig.put("stems.updateLastMembershipTime", "true");
    ApiConfig.testConfig.put("groups.updateLastMembershipTime", "true");

    configuration = ConfigManager.load(GrouperUtil.fileFromResourceName(
        BaseLdappcTestCase.TEST_CONFIG).getAbsolutePath());
  }

  public void testStatusUnknown() throws LdappcConfigurationException, NamingException {

    GrouperSynchronizer gs = new GroupEntrySynchronizer(null, null, configuration,
        options, null);

    Group group = StemHelper.addChildGroup(this.edu, "groupA", "Group A");

    // workaround Group.getLastMembershipChange() test environment nuance
    Group g = GroupFinder.findByUuid(grouperSession, group.getUuid(), true);

    assertEquals(
        "Status should be UNKNOWN when InputOptions.getLastModifyTime() is null.",
        GrouperSynchronizer.STATUS_UNKNOWN, gs.determineStatus(g));
  }

  public void testStatusNew() throws LdappcConfigurationException, NamingException {

    options.setLastModifyTime(new Date());

    GrouperSynchronizer gs = new GroupEntrySynchronizer(null, null, configuration,
        options, null);

    Group group = StemHelper.addChildGroup(this.edu, "groupA", "Group A");

    // workaround Group.getLastMembershipChange() test environment nuance
    Group g = GroupFinder.findByUuid(grouperSession, group.getUuid(), true);

    assertEquals(
        "Status should be NEW when InputOptions.getLastModifyTime() is before Group.getCreateTime().",
        GrouperSynchronizer.STATUS_NEW, gs.determineStatus(g));
  }

  public void testStatusNewWithModification() throws LdappcConfigurationException,
      NamingException {

    options.setLastModifyTime(new Date());

    GrouperSynchronizer gs = new GroupEntrySynchronizer(null, null, configuration,
        options, null);

    Group group = StemHelper.addChildGroup(this.edu, "groupA", "Group A");

    group.setDescription("description");

    // workaround Group.getLastMembershipChange() test environment nuance
    Group g = GroupFinder.findByUuid(grouperSession, group.getUuid(), true);

    assertEquals(
        "Status should be NEW when InputOptions.getLastModifyTime() is before Group.getCreateTime().",
        GrouperSynchronizer.STATUS_NEW, gs.determineStatus(g));
  }

  public void testStatusNewWithMemberModification() throws LdappcConfigurationException,
      NamingException {

    options.setLastModifyTime(new Date());

    GrouperSynchronizer gs = new GroupEntrySynchronizer(null, null, configuration,
        options, null);

    Group group = StemHelper.addChildGroup(this.edu, "groupA", "Group A");

    group.addMember(SubjectTestHelper.SUBJ0);

    // workaround Group.getLastMembershipChange() test environment nuance
    Group g = GroupFinder.findByUuid(grouperSession, group.getUuid(), true);

    assertEquals(
        "Status should be NEW when InputOptions.getLastModifyTime() is before Group.getCreateTime().",
        GrouperSynchronizer.STATUS_NEW, gs.determineStatus(g));
  }

  public void testStatusUnchanged() throws LdappcConfigurationException, NamingException {

    GrouperSynchronizer gs = new GroupEntrySynchronizer(null, null, configuration,
        options, null);

    Group group = StemHelper.addChildGroup(this.edu, "groupA", "Group A");

    options.setLastModifyTime(new Date());

    // workaround Group.getLastMembershipChange() test environment nuance
    Group g = GroupFinder.findByUuid(grouperSession, group.getUuid(), true);

    assertEquals(
        "Status should be UNCHANGED when InputOptions.getLastModifyTime() is after Group.getCreateTime().",
        GrouperSynchronizer.STATUS_UNCHANGED, gs.determineStatus(g));
  }

  public void testStatusUnchangedWithModification() throws LdappcConfigurationException,
      NamingException {

    GrouperSynchronizer gs = new GroupEntrySynchronizer(null, null, configuration,
        options, null);

    Group group = StemHelper.addChildGroup(this.edu, "groupA", "Group A");

    group.setDescription("description");

    options.setLastModifyTime(new Date());

    // workaround Group.getLastMembershipChange() test environment nuance
    Group g = GroupFinder.findByUuid(grouperSession, group.getUuid(), true);

    assertEquals(
        "Status should be UNCHANGED when InputOptions.getLastModifyTime() is after Group.getCreateTime() and Group.getModifyTime().",
        GrouperSynchronizer.STATUS_UNCHANGED, gs.determineStatus(g));
  }

  public void testStatusUnchangedWithMemberModification()
      throws LdappcConfigurationException, NamingException {

    GrouperSynchronizer gs = new GroupEntrySynchronizer(null, null, configuration,
        options, null);

    Group group = StemHelper.addChildGroup(this.edu, "groupA", "Group A");

    group.addMember(SubjectTestHelper.SUBJ0);

    options.setLastModifyTime(new Date());

    // workaround Group.getLastMembershipChange() test environment nuance
    Group g = GroupFinder.findByUuid(grouperSession, group.getUuid(), true);

    assertEquals(
        "Status should be UNCHANGED when InputOptions.getLastModifyTime() is after Group.getCreateTime() and Group.getLastMembershipChange().",
        GrouperSynchronizer.STATUS_UNCHANGED, gs.determineStatus(g));
  }

  public void testStatusModified() throws LdappcConfigurationException, NamingException {

    GrouperSynchronizer gs = new GroupEntrySynchronizer(null, null, configuration,
        options, null);

    Group group = StemHelper.addChildGroup(this.edu, "groupA", "Group A");

    options.setLastModifyTime(new Date());

    group.addMember(SubjectTestHelper.SUBJ0);

    // workaround Group.getLastMembershipChange() test environment nuance
    Group g = GroupFinder.findByUuid(grouperSession, group.getUuid(), true);

    assertEquals(
        "Status should be MODIFIED when InputOptions.getLastModifyTime() is after Group.getCreateTime() and before Group.getLastMembershipChange().",
        GrouperSynchronizer.STATUS_MODIFIED, gs.determineStatus(g));
  }
}
