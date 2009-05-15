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
import edu.internet2.middleware.ldappc.Provisioner;
import edu.internet2.middleware.ldappc.ProvisionerOptions;
import edu.internet2.middleware.ldappc.exception.ConfigurationException;

public class GrouperSynchronizerTest extends BaseLdappcTestCase {

  private ProvisionerOptions options = new ProvisionerOptions();

  private ConfigManager configuration;

  public void setUp() {
    super.setUp();

    ApiConfig.testConfig.put("stems.updateLastMembershipTime", "true");
    ApiConfig.testConfig.put("groups.updateLastMembershipTime", "true");

    configuration = new ConfigManager(GrouperUtil.fileFromResourceName(
        BaseLdappcTestCase.LDAPPC_BUSHY_XML).getAbsolutePath());
  }

  public void testStatusUnknown() throws ConfigurationException, NamingException {

    Provisioner provisioner = new Provisioner(configuration, options, null);

    Group group = StemHelper.addChildGroup(this.edu, "groupA", "Group A");

    // workaround Group.getLastMembershipChange() test environment nuance
    Group g = GroupFinder.findByUuid(grouperSession, group.getUuid(), true);

    assertEquals(
        "Status should be UNKNOWN when InputOptions.getLastModifyTime() is null.",
        Provisioner.STATUS_UNKNOWN, provisioner.determineStatus(g));
  }

  public void testStatusNew() throws ConfigurationException, NamingException {

    options.setLastModifyTime(new Date());

    Provisioner provisioner = new Provisioner(configuration, options, null);

    Group group = StemHelper.addChildGroup(this.edu, "groupA", "Group A");

    // workaround Group.getLastMembershipChange() test environment nuance
    Group g = GroupFinder.findByUuid(grouperSession, group.getUuid(), true);

    assertEquals(
        "Status should be NEW when InputOptions.getLastModifyTime() is before Group.getCreateTime().",
        Provisioner.STATUS_NEW, provisioner.determineStatus(g));
  }

  public void testStatusNewWithModification() throws ConfigurationException,
      NamingException {

    options.setLastModifyTime(new Date());

    Provisioner provisioner = new Provisioner(configuration, options, null);

    Group group = StemHelper.addChildGroup(this.edu, "groupA", "Group A");

    group.setDescription("description");

    // workaround Group.getLastMembershipChange() test environment nuance
    Group g = GroupFinder.findByUuid(grouperSession, group.getUuid(), true);

    assertEquals(
        "Status should be NEW when InputOptions.getLastModifyTime() is before Group.getCreateTime().",
        Provisioner.STATUS_NEW, provisioner.determineStatus(g));
  }

  public void testStatusNewWithMemberModification() throws ConfigurationException,
      NamingException {

    options.setLastModifyTime(new Date());

    Provisioner provisioner = new Provisioner(configuration, options, null);

    Group group = StemHelper.addChildGroup(this.edu, "groupA", "Group A");

    group.addMember(SubjectTestHelper.SUBJ0);

    // workaround Group.getLastMembershipChange() test environment nuance
    Group g = GroupFinder.findByUuid(grouperSession, group.getUuid(), true);

    assertEquals(
        "Status should be NEW when InputOptions.getLastModifyTime() is before Group.getCreateTime().",
        Provisioner.STATUS_NEW, provisioner.determineStatus(g));
  }

  public void testStatusUnchanged() throws ConfigurationException, NamingException {

    Provisioner provisioner = new Provisioner(configuration, options, null);

    Group group = StemHelper.addChildGroup(this.edu, "groupA", "Group A");

    options.setLastModifyTime(new Date());

    // workaround Group.getLastMembershipChange() test environment nuance
    Group g = GroupFinder.findByUuid(grouperSession, group.getUuid(), true);

    assertEquals(
        "Status should be UNCHANGED when InputOptions.getLastModifyTime() is after Group.getCreateTime().",
        Provisioner.STATUS_UNCHANGED, provisioner.determineStatus(g));
  }

  public void testStatusUnchangedWithModification() throws ConfigurationException,
      NamingException {

    Provisioner provisioner = new Provisioner(configuration, options, null);

    Group group = StemHelper.addChildGroup(this.edu, "groupA", "Group A");

    group.setDescription("description");

    options.setLastModifyTime(new Date());

    // workaround Group.getLastMembershipChange() test environment nuance
    Group g = GroupFinder.findByUuid(grouperSession, group.getUuid(), true);

    assertEquals(
        "Status should be UNCHANGED when InputOptions.getLastModifyTime() is after Group.getCreateTime() and Group.getModifyTime().",
        Provisioner.STATUS_UNCHANGED, provisioner.determineStatus(g));
  }

  public void testStatusUnchangedWithMemberModification() throws ConfigurationException,
      NamingException {

    Provisioner provisioner = new Provisioner(configuration, options, null);

    Group group = StemHelper.addChildGroup(this.edu, "groupA", "Group A");

    group.addMember(SubjectTestHelper.SUBJ0);

    options.setLastModifyTime(new Date());

    // workaround Group.getLastMembershipChange() test environment nuance
    Group g = GroupFinder.findByUuid(grouperSession, group.getUuid(), true);

    assertEquals(
        "Status should be UNCHANGED when InputOptions.getLastModifyTime() is after Group.getCreateTime() and Group.getLastMembershipChange().",
        Provisioner.STATUS_UNCHANGED, provisioner.determineStatus(g));
  }

  public void testStatusModified() throws ConfigurationException, NamingException {

    Provisioner provisioner = new Provisioner(configuration, options, null);

    Group group = StemHelper.addChildGroup(this.edu, "groupA", "Group A");

    options.setLastModifyTime(new Date());

    group.addMember(SubjectTestHelper.SUBJ0);

    // workaround Group.getLastMembershipChange() test environment nuance
    Group g = GroupFinder.findByUuid(grouperSession, group.getUuid(), true);

    assertEquals(
        "Status should be MODIFIED when InputOptions.getLastModifyTime() is after Group.getCreateTime() and before Group.getLastMembershipChange().",
        Provisioner.STATUS_MODIFIED, provisioner.determineStatus(g));
  }
}
