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

import javax.naming.ldap.LdapContext;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.ldappc.GrouperProvisionerConfiguration;
import edu.internet2.middleware.ldappc.GrouperProvisionerOptions;
import edu.internet2.middleware.ldappc.util.SubjectCache;

/**
 * This defines the common functionality needed by all Grouper synchronizers.
 */
public abstract class GrouperSynchronizer extends Synchronizer {

  /**
   * Indicates the group is new since the last modification date.
   */
  public static final int STATUS_NEW = 0;

  /**
   * Indicates the group has been modified since the last modification date.
   */
  public static final int STATUS_MODIFIED = 1;

  /**
   * Indicates the group has not been modified since the last modification date.
   */
  public static final int STATUS_UNCHANGED = 2;

  /**
   * Indicates a last modification date was not provided so the group's status is unknown.
   */
  public static final int STATUS_UNKNOWN = 3;

  /**
   * Grouper configuration for provisioning.
   */
  private GrouperProvisionerConfiguration configuration;

  /**
   * Grouper options for provisioning.
   */
  private GrouperProvisionerOptions options;

  /**
   * Constructs a <code>GroupSynchronizer</code>.
   * 
   * @param context
   *          Ldap context to be used for synchronizing
   * @param configuration
   *          Grouper provisioning configuration
   * @param options
   *          Grouper provisioning options
   * @param subjectCache
   *          Subject cache to speed subject retrieval
   */
  public GrouperSynchronizer(LdapContext context,
      GrouperProvisionerConfiguration configuration, GrouperProvisionerOptions options,
      SubjectCache subjectCache) {
    super(context, subjectCache);
    setConfiguration(configuration);
    setOptions(options);
  }

  /**
   * Get the Grouper provisioner configuration.
   * 
   * @return Grouper provisioner configuration
   */
  public GrouperProvisionerConfiguration getConfiguration() {
    return configuration;
  }

  /**
   * Set the Grouper provisioner configuration.
   * 
   * @param configuration
   *          Grouper provisioner configuration
   */
  protected void setConfiguration(GrouperProvisionerConfiguration configuration) {
    this.configuration = configuration;
  }

  /**
   * Get the Grouper provisioner options.
   * 
   * @return Grouper provisioner options
   */
  public GrouperProvisionerOptions getOptions() {
    return options;
  }

  /**
   * Set the Grouper provisioner options.
   * 
   * @param options
   *          Grouper provisioner options
   */
  protected void setOptions(GrouperProvisionerOptions options) {
    this.options = options;
  }

  /**
   * Determines the status of the group based on the lastModifyTime provided in the
   * GrouperOptions.
   * 
   * @param group
   *          Group
   * @return Status of the group, either {@link #STATUS_NEW}, {@link #STATUS_MODIFIED},
   *         {@link #STATUS_UNCHANGED} or {@link #STATUS_UNKNOWN}.
   */
  protected int determineStatus(Group group) {

    Date lastModifyTime = options.getLastModifyTime();
    if (lastModifyTime == null) {
      return STATUS_UNKNOWN;
    }

    Date groupCreateTime = group.getCreateTime();
    if (groupCreateTime == null) {
      return STATUS_UNKNOWN;
    }

    if (lastModifyTime.before(groupCreateTime)) {
      return STATUS_NEW;
    }

    Date groupModifyTime = group.getModifyTime();
    if (groupModifyTime != null && lastModifyTime.before(groupModifyTime)) {
      return STATUS_MODIFIED;
    }

    // some weirdness occurs with getLastMembershipChange()
    if (group.getLastMembershipChange() != null) {
      Date memberModifyTime = new Date(group.getLastMembershipChange().getTime());
      if (lastModifyTime.before(memberModifyTime)) {
        return STATUS_MODIFIED;
      }
    }

    return STATUS_UNCHANGED;
  }
}