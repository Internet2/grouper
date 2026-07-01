package edu.internet2.middleware.grouper.app.dropbox;

import java.util.Set;

/**
 * Bundles Dropbox provisioner configuration into a single object so API command
 * method signatures don't need an individual parameter for each setting.  Mirrors
 * the TrueFoundrySettings / ScimSettings pattern.
 */
public class DropboxSettings {

  /** Grouper folder name whose child groups are admin-role groups (blank = off) */
  private String adminRoleFolderName;

  /** whether admin-role management is enabled */
  private boolean manageAdminRoles;

  /** whether to wipe member data on team removal */
  private boolean wipeDataOnRemove;

  /** whether to keep the account (convert to Basic) on team removal */
  private boolean keepAccountOnRemove;

  /** group names to ignore */
  private Set<String> ignoreGroupNames;

  /** member emails to ignore */
  private Set<String> ignoreUserEmails;

  /**
   * Load settings from a DropboxProvisionerConfiguration.
   * @param config the provisioner configuration
   */
  public void loadFromConfiguration(DropboxProvisionerConfiguration config) {
    this.adminRoleFolderName = config.getDropboxAdminRoleFolderName();
    this.manageAdminRoles = config.isManageAdminRoles();
    this.wipeDataOnRemove = config.isDropboxWipeDataOnRemove();
    this.keepAccountOnRemove = config.isDropboxKeepAccountOnRemove();
    this.ignoreGroupNames = DropboxApiCommands.parseIgnoreSet(config.getDropboxIgnoreGroupNames());
    this.ignoreUserEmails = DropboxApiCommands.parseIgnoreSet(config.getDropboxIgnoreUserEmails());
  }

  public String getAdminRoleFolderName() {
    return adminRoleFolderName;
  }

  public void setAdminRoleFolderName(String adminRoleFolderName) {
    this.adminRoleFolderName = adminRoleFolderName;
  }

  public boolean isManageAdminRoles() {
    return manageAdminRoles;
  }

  public void setManageAdminRoles(boolean manageAdminRoles) {
    this.manageAdminRoles = manageAdminRoles;
  }

  public boolean isWipeDataOnRemove() {
    return wipeDataOnRemove;
  }

  public void setWipeDataOnRemove(boolean wipeDataOnRemove) {
    this.wipeDataOnRemove = wipeDataOnRemove;
  }

  public boolean isKeepAccountOnRemove() {
    return keepAccountOnRemove;
  }

  public void setKeepAccountOnRemove(boolean keepAccountOnRemove) {
    this.keepAccountOnRemove = keepAccountOnRemove;
  }

  public Set<String> getIgnoreGroupNames() {
    return ignoreGroupNames;
  }

  public void setIgnoreGroupNames(Set<String> ignoreGroupNames) {
    this.ignoreGroupNames = ignoreGroupNames;
  }

  public Set<String> getIgnoreUserEmails() {
    return ignoreUserEmails;
  }

  public void setIgnoreUserEmails(Set<String> ignoreUserEmails) {
    this.ignoreUserEmails = ignoreUserEmails;
  }

}
