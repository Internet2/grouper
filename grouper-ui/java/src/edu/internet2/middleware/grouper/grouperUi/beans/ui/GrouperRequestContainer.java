package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import javax.servlet.http.HttpServletRequest;

import edu.internet2.middleware.grouper.ui.GrouperUiFilter;

/**
 * request container for grouper in the j2ee request object, under 
 * the attribute name "grouperRequestContainer"
 * @author mchyzer
 *
 */
public class GrouperRequestContainer {

  /**
   * current gui audit entry  being displayed
   */
  public GuiAuditEntry guiAuditEntry = null;

  /**
   * current gui audit entry  being displayed
   * @return audit
   */
  public GuiAuditEntry getGuiAuditEntry() {
    return this.guiAuditEntry;
  }

  /**
   * current gui audit entry  being displayed
   * @param guiAuditEntry1
   */
  public void setGuiAuditEntry(GuiAuditEntry guiAuditEntry1) {
    this.guiAuditEntry = guiAuditEntry1;
  }

  /**
   * retrieveFromSession, cannot be null
   * @return the app state in request scope
   */
  public static GrouperRequestContainer retrieveFromRequestOrCreate() {
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    String attributeName = "grouperRequestContainer";
    GrouperRequestContainer grouperRequestContainer = 
      (GrouperRequestContainer)httpServletRequest.getAttribute(attributeName);
    if (grouperRequestContainer == null) {
      grouperRequestContainer = new GrouperRequestContainer();
      httpServletRequest.setAttribute(attributeName, grouperRequestContainer);
    }
    return grouperRequestContainer;
  }

  /**
   * container for index screen and general components
   */
  private IndexContainer indexContainer = null;

  /**
   * container for index screen and general components, lazy load, create if null
   * @return the index container
   */
  public IndexContainer getIndexContainer() {
    if (this.indexContainer == null) {
      this.indexContainer = new IndexContainer();
    }
    return this.indexContainer;
  }

  /**
   * container for index screen and general components
   * @param indexContainer1
   */
  public void setIndexContainer(IndexContainer indexContainer1) {
    this.indexContainer = indexContainer1;
  }

  
}
