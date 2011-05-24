package edu.internet2.middleware.grouper.grouperUi.beans.api;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * container for permission entry to show a row on the permissions screen
 * @author mchyzer
 *
 */
@SuppressWarnings("serial")
public class GuiPermissionEntryContainer implements Serializable {
  
  /** permission entries, should be one per action */
  private List<GuiPermissionEntry> guiPermissionEntries;

  /**
   * long screen label
   */
  private String screenLabelLong = null;
  
  /**
   * short screen label
   */
  private String screenLabelShort = null;
  
  /** gui attribute assign metadata on permissions */
  private Set<GuiAttributeAssign> guiAttributeAssigns;

  /**
   * gui attribute assign metadata on permissions
   * @param theGuiAttributeAssigns
   */
  public void setGuiAttributeAssigns(Set<GuiAttributeAssign> theGuiAttributeAssigns) {
    this.guiAttributeAssigns = theGuiAttributeAssigns;
  }

  /**
   * gui attribute assign metadata on permissions
   * @return attribute assigns
   */
  public Set<GuiAttributeAssign> getGuiAttributeAssigns() {
    return this.guiAttributeAssigns;
  }

  /**
   * 
   * @return long label if different than the short one
   */
  public String getScreenLabelLongIfDifferent() {
    this.initScreenLabels();
    if (this.isNeedsTooltip()) {
      return this.screenLabelLong;
    }
    return null;
  }
  
  /**
   * get long screen label (tooltip)
   * @return tooltip
   */
  public String getScreenLabelLong() {
    this.initScreenLabels();
    return this.screenLabelLong;
  }

  /**
   * get short screen label 
   * @return short screen label
   */
  public String getScreenLabelShort() {
    this.initScreenLabels();
    return this.screenLabelShort;
  }

  /**
   * get short screen label 
   * @return short screen label
   */
  public boolean isNeedsTooltip() {
    this.initScreenLabels();
    return !StringUtils.equals(this.screenLabelLong, this.screenLabelShort);
  }

  /**
   * init screen labels
   */
  private void initScreenLabels() {
    if (this.screenLabelLong == null && this.screenLabelShort == null) {
      
      if (GrouperUtil.length(this.guiPermissionEntries) > 0) {
        GuiPermissionEntry guiPermissionEntry = this.guiPermissionEntries.get(0);
        this.screenLabelLong = guiPermissionEntry.getScreenLabelLong();
        this.screenLabelShort = guiPermissionEntry.getScreenLabelShort();
      }
    }
  }

  /**
   * permission entries
   * @return permission entries
   */
  public List<GuiPermissionEntry> getGuiPermissionEntries() {
    return this.guiPermissionEntries;
  }

  /**
   * permission entries
   * @param permissionEntries1
   */
  public void setGuiPermissionEntries(List<GuiPermissionEntry> permissionEntries1) {
    this.clear();
    this.guiPermissionEntries = permissionEntries1;
  }

  /**
   * clear everything out
   */
  private void clear() {
    this.screenLabelLong = null;
    this.screenLabelShort = null;
  }
  
}
