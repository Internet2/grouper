/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.Set;

import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiPaging;


/**
 * container for inviting external users
 * @author mchyzer
 *
 */
public class InviteExternalContainer {

  /**
   * search results when looking for a group to add the subject to
   */
  private Set<GuiGroup> guiGroupsSearch;

  /**
   * keep track of the paging on the group search panel
   */
  private GuiPaging guiPaging = null;

  /**
   * search results when looking for a group to add the subject to
   * @return the gui groups
   */
  public Set<GuiGroup> getGuiGroupsSearch() {
    return this.guiGroupsSearch;
  }

  /**
   * keep track of the paging on the group search panel
   * @return the paging object, init if not there...
   */
  public GuiPaging getGuiPaging() {
    if (this.guiPaging == null) {
      this.guiPaging = new GuiPaging();
    }
    return this.guiPaging;
  }

  /**
   * search results when looking for a group to add the subject to
   * @param guiGroupsAddMember1
   */
  public void setGuiGroupsSearch(Set<GuiGroup> guiGroupsAddMember1) {
    this.guiGroupsSearch = guiGroupsAddMember1;
  }

  /**
   * paging for the group search panel
   * @param guiPaging
   */
  public void setGuiPaging(GuiPaging guiPaging) {
    this.guiPaging = guiPaging;
  }

  
  
}
