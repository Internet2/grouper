/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.Set;

import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiPaging;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;


/**
 * container for inviting external users
 * @author mchyzer
 *
 */
public class InviteExternalContainer {

  /**
   * external id for messages
   */
  private String externalId;
  
  /**
   * external id for messages
   * @return the idWithProblem
   */
  public String getExternalId() {
    return this.externalId;
  }

  
  /**
   * external id for messages
   * @param idWithProblem1 the idWithProblem to set
   */
  public void setExternalId(String idWithProblem1) {
    this.externalId = idWithProblem1;
  }

  /**
   * if we should allow invite by identifier (if the inviter knows the identifier, and 
   * no attributes on the external subject are mandatory by the application)
   * @return true if allow invite by identifier
   */
  public boolean isAllowInviteByIdentifier() {
    return GrouperUiConfig.retrieveConfig().propertyValueBoolean("inviteExternalMembers.allowInviteByIdentifier", false);
  }
  
  /**
   * search results when looking for a group to add the subject to
   */
  private Set<GuiGroup> guiGroupsSearch;

  /**
   * keep track of the paging on the group search panel
   */
  private GuiPaging guiPaging = null;

  /**
   * gui groups in addition to the one in the combobox
   */
  private Set<GuiGroup> inviteExtraGuiGroups;
  
  /**
   * gui groups in addition to the one in the combobox
   * @return the inviteExtraGuiGroups
   */
  public Set<GuiGroup> getInviteExtraGuiGroups() {
    return this.inviteExtraGuiGroups;
  }
  
  /**
   * gui groups in addition to the one in the combobox
   * @param inviteExtraGuiGroups1 the inviteExtraGuiGroups to set
   */
  public void setInviteExtraGuiGroups(Set<GuiGroup> inviteExtraGuiGroups1) {
    this.inviteExtraGuiGroups = inviteExtraGuiGroups1;
  }

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
