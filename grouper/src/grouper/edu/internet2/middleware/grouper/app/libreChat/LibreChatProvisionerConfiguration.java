package edu.internet2.middleware.grouper.app.libreChat;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Runtime settings of the LibreChat provisioner (provisioner.&lt;configId&gt;.*)
 *
 * GRP-7594
 */
public class LibreChatProvisionerConfiguration extends GrouperProvisioningConfiguration {

  /** default prefix of idOnTheSource for groups this provisioner manages */
  public static final String DEFAULT_GROUP_ID_ON_THE_SOURCE_PREFIX = "grouper:";

  /** web service external system config id */
  private String libreChatExternalSystemConfigId;

  /** groups whose idOnTheSource starts with this are managed by this provisioner; nothing else is touched */
  private String libreChatGroupIdOnTheSourcePrefix;

  /** true to remove all members instead of deleting a group (keeps the group's sharing grants) */
  private boolean libreChatEmptyGroupsInsteadOfDelete;

  public String getLibreChatExternalSystemConfigId() {
    return this.libreChatExternalSystemConfigId;
  }

  public void setLibreChatExternalSystemConfigId(String libreChatExternalSystemConfigId) {
    this.libreChatExternalSystemConfigId = libreChatExternalSystemConfigId;
  }

  public String getLibreChatGroupIdOnTheSourcePrefix() {
    return this.libreChatGroupIdOnTheSourcePrefix;
  }

  public void setLibreChatGroupIdOnTheSourcePrefix(String libreChatGroupIdOnTheSourcePrefix) {
    this.libreChatGroupIdOnTheSourcePrefix = libreChatGroupIdOnTheSourcePrefix;
  }

  public boolean isLibreChatEmptyGroupsInsteadOfDelete() {
    return this.libreChatEmptyGroupsInsteadOfDelete;
  }

  public void setLibreChatEmptyGroupsInsteadOfDelete(boolean libreChatEmptyGroupsInsteadOfDelete) {
    this.libreChatEmptyGroupsInsteadOfDelete = libreChatEmptyGroupsInsteadOfDelete;
  }

  @Override
  public void configureSpecificSettings() {
    this.libreChatExternalSystemConfigId = this.retrieveConfigString("libreChatExternalSystemConfigId", true);
    this.libreChatGroupIdOnTheSourcePrefix = GrouperUtil.defaultIfBlank(
        this.retrieveConfigString("libreChatGroupIdOnTheSourcePrefix", false), DEFAULT_GROUP_ID_ON_THE_SOURCE_PREFIX);
    this.libreChatEmptyGroupsInsteadOfDelete = GrouperUtil.booleanValue(
        this.retrieveConfigBoolean("libreChatEmptyGroupsInsteadOfDelete", false), false);
  }

}
