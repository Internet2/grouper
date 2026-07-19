package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.internet2.middleware.grouper.app.externalSystem.GrouperExternalSystem;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.subject.Subject;

/**
 * 
 */
public class ExternalSystemContainer {
  
  /**
   * grouper external system user is currently viewing/editing/adding
   */
  private GuiGrouperExternalSystem guiGrouperExternalSystem;
  
  /**
   * all external systems
   */
  private List<GuiGrouperExternalSystem> guiGrouperExternalSystems = new ArrayList<GuiGrouperExternalSystem>();
  
  
  /**
   * html for external system
   */
  private String html;

  /**
   * references (usages) of the external system currently being viewed, i.e.
   * everywhere in Grouper this external system is used.  Shown in the References
   * section of the view details screen.
   */
  private List<GuiGrouperExternalSystemUsage> guiGrouperExternalSystemUsages = new ArrayList<GuiGrouperExternalSystemUsage>();

  /**
   * true if the references list was truncated because some type had more than
   * the max references shown per type
   */
  private boolean externalSystemUsagesTruncated = false;

  /**
   * max references shown per type (used in the truncation info note)
   */
  private int externalSystemUsagesMaxPerType;

  /**
   * @return references (usages) of the external system currently being viewed
   */
  public List<GuiGrouperExternalSystemUsage> getGuiGrouperExternalSystemUsages() {
    return guiGrouperExternalSystemUsages;
  }

  /**
   * @param guiGrouperExternalSystemUsages1 references (usages) of the external system
   */
  public void setGuiGrouperExternalSystemUsages(List<GuiGrouperExternalSystemUsage> guiGrouperExternalSystemUsages1) {
    this.guiGrouperExternalSystemUsages = guiGrouperExternalSystemUsages1;
  }

  /**
   * @return true if the references list was truncated
   */
  public boolean isExternalSystemUsagesTruncated() {
    return externalSystemUsagesTruncated;
  }

  /**
   * @param externalSystemUsagesTruncated1 true if the references list was truncated
   */
  public void setExternalSystemUsagesTruncated(boolean externalSystemUsagesTruncated1) {
    this.externalSystemUsagesTruncated = externalSystemUsagesTruncated1;
  }

  /**
   * @return max references shown per type
   */
  public int getExternalSystemUsagesMaxPerType() {
    return externalSystemUsagesMaxPerType;
  }

  /**
   * @param externalSystemUsagesMaxPerType1 max references shown per type
   */
  public void setExternalSystemUsagesMaxPerType(int externalSystemUsagesMaxPerType1) {
    this.externalSystemUsagesMaxPerType = externalSystemUsagesMaxPerType1;
  }

  
  public List<GuiGrouperExternalSystem> getGuiGrouperExternalSystems() {
    return guiGrouperExternalSystems;
  }

  
  public void setGuiGrouperExternalSystems(
      List<GuiGrouperExternalSystem> guiGrouperExternalSystems) {
    this.guiGrouperExternalSystems = guiGrouperExternalSystems;
  }
  
  
  public GuiGrouperExternalSystem getGuiGrouperExternalSystem() {
    return guiGrouperExternalSystem;
  }


  
  public void setGuiGrouperExternalSystem(
      GuiGrouperExternalSystem guiGrouperExternalSystem) {
    this.guiGrouperExternalSystem = guiGrouperExternalSystem;
  }
  
  
  public String getHtml() {
    return html;
  }
  
  public void setHtml(String html) {
    this.html = html;
  }


  /**
   * @return true if can view external systems
   */
  public boolean isCanViewExternalSystems() {
    
    Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    if (PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
      return true;
    }
    return false;
  }
  
  public List<GrouperExternalSystem> getAllExternalSystems() {
    return GrouperExternalSystem.retrieveAllGrouperExternalSystems();
  }

  public List<GrouperExternalSystem> getAllExternalSystemTypes() {
    return GrouperExternalSystem.retrieveAllModuleConfigurationTypes();
  }

  public List<GrouperExternalSystem> getAllExternalSystemTypesAdd() {
    List<GrouperExternalSystem> retrieveAllModuleConfigurationTypes = new ArrayList<GrouperExternalSystem>(GrouperExternalSystem.retrieveAllModuleConfigurationTypes());
    Iterator<GrouperExternalSystem> iterator = retrieveAllModuleConfigurationTypes.iterator();
    while (iterator.hasNext()) {
      GrouperExternalSystem grouperExternalSystem = iterator.next();
      if (!grouperExternalSystem.isCanAdd()) {
        iterator.remove();
      }
    }
    return retrieveAllModuleConfigurationTypes;
  }

}
