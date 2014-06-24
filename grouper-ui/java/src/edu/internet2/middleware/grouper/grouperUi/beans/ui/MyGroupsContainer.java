/*******************************************************************************
 * Copyright 2014 Internet2
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *   http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.Set;

import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiMembershipSubjectContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiPaging;

/**
 * my groups container beans and objects
 * @author mchyzer
 *
 */
public class MyGroupsContainer {

  /**
   * paging for my groups
   */
  private GuiPaging myGroupsGuiPaging = null;
  
  /**
   * gui groups the user manages
   * @return gui groups
   */
  public Set<GuiGroup> getGuiGroupsUserManages() {
    return this.guiGroupsUserManages;
  }

  /**
   * gui groups the user manages
   * @param guiGroupsUserManages1
   */
  public void setGuiGroupsUserManages(Set<GuiGroup> guiGroupsUserManages1) {
    this.guiGroupsUserManages = guiGroupsUserManages1;
  }

  /**
   * for my groups, this is a list of groups the user manages
   */
  private Set<GuiGroup> guiGroupsUserManages;

  /**
   * groups the user is in
   */
  private Set<GuiMembershipSubjectContainer> guiMembershipSubjectContainers;

  /**
   * paging for my groups
   * @return paging
   */
  public GuiPaging getMyGroupsGuiPaging() {
    if (this.myGroupsGuiPaging == null) {
      this.myGroupsGuiPaging = new GuiPaging();
    }
    return this.myGroupsGuiPaging;
  }

  /**
   * paging for my stems
   * @param myGroupsGuiPaging1
   */
  public void setMyGroupsGuiPaging(GuiPaging myGroupsGuiPaging1) {
    this.myGroupsGuiPaging = myGroupsGuiPaging1;
  }

  /**
   * groups the user is in
   * @return subjects and memberships
   */
  public Set<GuiMembershipSubjectContainer> getGuiMembershipSubjectContainers() {
    return this.guiMembershipSubjectContainers;
  }

  /**
   * groups the user is in
   * @param guiMembershipSubjectContainers
   */
  public void setGuiMembershipSubjectContainers(
      Set<GuiMembershipSubjectContainer> guiMembershipSubjectContainers) {
    this.guiMembershipSubjectContainers = guiMembershipSubjectContainers;
  }

}
