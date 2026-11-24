/*******************************************************************************
 * Copyright 2026 Internet2
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.app.customComposite.GrouperCustomComposite;
import edu.internet2.middleware.grouper.app.membershipRequire.MembershipRequireAdmin;
import edu.internet2.middleware.grouper.app.membershipRequire.MembershipRequireConfiguration;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;

/**
 * data for the custom composites and membership requirements screen
 *
 * @author shilen
 */
public class CustomCompositeContainer {

  /**
   * only Grouper system administrators can see or change custom composites and membership
   * requirements, and only when the configuration UI is on, as for the configuration editor.  the
   * screen checks this on every action, not just to show the link
   * @return true if allowed
   */
  public boolean isCanView() {
    if (!GrouperUiConfig.retrieveConfig().propertyValueBoolean("grouperUi.configuration.enabled", true)) {
      return false;
    }
    return PrivilegeHelper.isWheelOrRoot(GrouperUiFilter.retrieveSubjectLoggedIn());
  }

  /** every custom composite */
  private List<GrouperCustomComposite> customComposites;

  /**
   * every custom composite
   * @return the list
   */
  public List<GrouperCustomComposite> getCustomComposites() {
    return this.customComposites;
  }

  /**
   * every custom composite
   * @param customComposites1 the list
   */
  public void setCustomComposites(List<GrouperCustomComposite> customComposites1) {
    this.customComposites = customComposites1;
  }

  /** the custom composite being edited, null when adding */
  private GrouperCustomComposite customComposite;

  /**
   * the custom composite being edited, null when adding
   * @return the composite
   */
  public GrouperCustomComposite getCustomComposite() {
    return this.customComposite;
  }

  /**
   * the custom composite being edited, null when adding
   * @param customComposite1 the composite
   */
  public void setCustomComposite(GrouperCustomComposite customComposite1) {
    this.customComposite = customComposite1;
  }

  /** every membership requirement */
  private List<MembershipRequireAdmin> membershipRequirements;

  /**
   * every membership requirement
   * @return the list
   */
  public List<MembershipRequireAdmin> getMembershipRequirements() {
    return this.membershipRequirements;
  }

  /**
   * every membership requirement
   * @param membershipRequirements1 the list
   */
  public void setMembershipRequirements(List<MembershipRequireAdmin> membershipRequirements1) {
    this.membershipRequirements = membershipRequirements1;
  }

  /** the membership requirement being viewed or edited, null when adding */
  private MembershipRequireAdmin membershipRequirement;

  /**
   * the membership requirement being viewed or edited, null when adding
   * @return the requirement
   */
  public MembershipRequireAdmin getMembershipRequirement() {
    return this.membershipRequirement;
  }

  /**
   * the membership requirement being viewed or edited, null when adding
   * @param membershipRequirement1 the requirement
   */
  public void setMembershipRequirement(MembershipRequireAdmin membershipRequirement1) {
    this.membershipRequirement = membershipRequirement1;
  }

  /** the requirement's grouper.properties keys as a configuration module, for the generic form fields */
  private MembershipRequireConfiguration membershipRequireConfiguration;

  /**
   * the requirement's grouper.properties keys as a configuration module, for the generic form
   * fields.  null on the add screen until a config id is entered
   * @return the module
   */
  public MembershipRequireConfiguration getMembershipRequireConfiguration() {
    return this.membershipRequireConfiguration;
  }

  /**
   * the requirement's grouper.properties keys as a configuration module
   * @param membershipRequireConfiguration1 the module
   */
  public void setMembershipRequireConfiguration(MembershipRequireConfiguration membershipRequireConfiguration1) {
    this.membershipRequireConfiguration = membershipRequireConfiguration1;
  }

  /**
   * the requirement form's own fields, which are not in the module: vetoMessage,
   * attributeOption, attributeDefName, showOnGroupScreen, groupScreenLabel, groupScreenDescription,
   * groupScreenIndex
   */
  private Map<String, String> membershipRequirementForm = new HashMap<String, String>();

  /**
   * the requirement form's own fields, which are not in the module
   * @return field name to value
   */
  public Map<String, String> getMembershipRequirementForm() {
    return this.membershipRequirementForm;
  }

  /**
   * the requirement form's own fields, which are not in the module
   * @param membershipRequirementForm1 field name to value
   */
  public void setMembershipRequirementForm(Map<String, String> membershipRequirementForm1) {
    this.membershipRequirementForm = membershipRequirementForm1;
  }
}
