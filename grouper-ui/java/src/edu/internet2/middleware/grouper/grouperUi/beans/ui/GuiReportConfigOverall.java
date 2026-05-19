package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.app.reports.GrouperReportConfigurationBean;
import edu.internet2.middleware.grouper.app.reports.GrouperReportInstance;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiStem;

/**
 * report config wrapper for the overall (admin) view that includes information about the
 * owning group or folder
 */
public class GuiReportConfigOverall {
  
  /**
   * underlying gui report config (config bean + most recent instance)
   */
  private GuiReportConfig guiReportConfig;
  
  /**
   * owning group, if this report config is on a group; null otherwise
   */
  private Group ownerGroup;
  
  /**
   * owning stem (folder), if this report config is on a folder; null otherwise
   */
  private Stem ownerStem;
  
  public GuiReportConfigOverall(GuiReportConfig guiReportConfig, Group ownerGroup, Stem ownerStem) {
    this.guiReportConfig = guiReportConfig;
    this.ownerGroup = ownerGroup;
    this.ownerStem = ownerStem;
  }
  
  /**
   * @return underlying gui report config
   */
  public GuiReportConfig getGuiReportConfig() {
    return guiReportConfig;
  }
  
  /**
   * @return underlying report config bean (for jsp convenience)
   */
  public GrouperReportConfigurationBean getReportConfigBean() {
    return guiReportConfig.getReportConfigBean();
  }
  
  /**
   * @return most recent report instance (for jsp convenience), or null if never run
   */
  public GrouperReportInstance getMostRecentReportInstance() {
    return guiReportConfig.getMostRecentReportInstance();
  }
  
  /**
   * @return formatted last run time, or null if never run
   */
  public String getLastRunTime() {
    return guiReportConfig.getLastRunTime();
  }
  
  /**
   * @return user friendly cron description
   */
  public String getUserFriendlyCron() {
    return guiReportConfig.getUserFriendlyCron();
  }
  
  /**
   * @return owning group, if any
   */
  public Group getOwnerGroup() {
    return ownerGroup;
  }

  /**
   * @return owning stem (folder), if any
   */
  public Stem getOwnerStem() {
    return ownerStem;
  }

  /**
   * cached GuiGroup wrapper around the owner group, for shortLinkWithIcon rendering
   */
  private GuiGroup guiOwnerGroup;

  /**
   * @return GuiGroup wrapper around the owner group, or null if owner is a folder
   */
  public GuiGroup getGuiOwnerGroup() {
    if (this.guiOwnerGroup == null && this.ownerGroup != null) {
      this.guiOwnerGroup = new GuiGroup(this.ownerGroup);
    }
    return this.guiOwnerGroup;
  }

  /**
   * cached GuiStem wrapper around the owner stem, for shortLinkWithIcon rendering
   */
  private GuiStem guiOwnerStem;

  /**
   * @return GuiStem wrapper around the owner stem, or null if owner is a group
   */
  public GuiStem getGuiOwnerStem() {
    if (this.guiOwnerStem == null && this.ownerStem != null) {
      this.guiOwnerStem = new GuiStem(this.ownerStem);
    }
    return this.guiOwnerStem;
  }
  
  /**
   * @return true if this report is configured on a group
   */
  public boolean isGroupOwner() {
    return ownerGroup != null;
  }
  
  /**
   * @return true if this report is configured on a folder
   */
  public boolean isStemOwner() {
    return ownerStem != null;
  }
  
  /**
   * @return id of the owning group or stem
   */
  public String getOwnerId() {
    if (ownerGroup != null) {
      return ownerGroup.getId();
    }
    if (ownerStem != null) {
      return ownerStem.getId();
    }
    return null;
  }
  
  /**
   * @return display name of the owning group or stem
   */
  public String getOwnerDisplayName() {
    if (ownerGroup != null) {
      return ownerGroup.getDisplayName();
    }
    if (ownerStem != null) {
      return ownerStem.getDisplayName();
    }
    return null;
  }
  
  /**
   * @return system name of the owning group or stem
   */
  public String getOwnerName() {
    if (ownerGroup != null) {
      return ownerGroup.getName();
    }
    if (ownerStem != null) {
      return ownerStem.getName();
    }
    return null;
  }
  
}
