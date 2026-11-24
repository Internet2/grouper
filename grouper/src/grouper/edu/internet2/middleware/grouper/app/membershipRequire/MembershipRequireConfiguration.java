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
package edu.internet2.middleware.grouper.app.membershipRequire;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleBase;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemFormElement;

/**
 * the grouper.properties keys of one membership requirement,
 * grouper.membershipRequirement.(configId).(uiKey|attributeName|requireGroupName|hookEnable|sourceIdsToIgnore),
 * as a configuration module, so the screen draws them with the generic form from their metadata in
 * grouper.base.properties and saves them like other configuration screens.  the rest of a requirement
 * is not in this file (the veto message text, the attribute, the group screen entry), so
 * this is not used on its own: {@link MembershipRequireAdmin#save} checks everything, does the rest,
 * and uses this for the keys.  the runtime does not use this class: the engine reads config directly
 *
 * @author shilen
 */
public class MembershipRequireConfiguration extends GrouperConfigurationModuleBase {

  @Override
  public String getConfigIdElementIdHandle() {
    return "#membershipRequirementConfigId";
  }

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_PROPERTIES;
  }

  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId!");
    }
    return MembershipRequireAdmin.CONFIG_PREFIX + this.getConfigId() + ".";
  }

  /**
   * two parts after the prefix, so the settings for every requirement
   * (grouper.membershipRequirement.hookEnable etc) are not taken for a requirement
   */
  @Override
  public String getConfigIdRegex() {
    return "^(grouper\\.membershipRequirement)\\.([^.]+)\\.(.*)$";
  }

  @Override
  protected String getConfigurationTypePrefix() {
    return "membershipRequirement";
  }

  /**
   * the config id of the example keys in grouper.base.properties
   */
  @Override
  public String getConfigIdThatIdentifiesThisConfig() {
    return "someConfigId";
  }

  /**
   * expression language is evaluated with static class access, and nothing here needs it
   */
  @Override
  public boolean isExpressionLanguageAllowed() {
    return false;
  }

  /**
   * the required group is picked with a group picker, as on the rules screens
   */
  @Override
  public Map<String, GrouperConfigurationModuleAttribute> retrieveAttributes() {
    Map<String, GrouperConfigurationModuleAttribute> attributes = super.retrieveAttributes();
    GrouperConfigurationModuleAttribute requireGroupName = attributes.get("requireGroupName");
    if (requireGroupName != null) {
      requireGroupName.setFormElement(ConfigItemFormElement.GROUPCOMBOBOX);
    }
    return attributes;
  }

  /**
   * the group picker posts the group as config_requireGroupNameName (or what was typed as
   * ...NameDisplay) rather than config_requireGroupName, which the base reads, so read it here as the
   * rules screens do.  the save turns a group id into the name
   */
  @Override
  public void populateConfigurationValuesFromUi(HttpServletRequest request) {
    super.populateConfigurationValuesFromUi(request);
    GrouperConfigurationModuleAttribute requireGroupName = this.retrieveAttributes().get("requireGroupName");
    if (requireGroupName == null || requireGroupName.isReadOnly()) {
      return;
    }
    String value = request.getParameter("config_requireGroupNameName");
    if (StringUtils.isBlank(value)) {
      value = request.getParameter("config_requireGroupNameNameDisplay");
    }
    this.assignValue("requireGroupName", value);
  }

  /**
   * a field's value, as configured or as set
   * @param suffix e.g. uiKey
   * @return the value or null
   */
  public String retrieveValue(String suffix) {
    GrouperConfigurationModuleAttribute attribute = this.retrieveAttributes().get(suffix);
    return attribute == null ? null : StringUtils.trimToNull(attribute.getValue());
  }

  /**
   * set a field's value
   * @param suffix e.g. uiKey
   * @param value the value, blank to remove it on save
   */
  public void assignValue(String suffix, String value) {
    GrouperConfigurationModuleAttribute attribute = this.retrieveAttributes().get(suffix);
    if (attribute == null) {
      throw new RuntimeException("Not a membership requirement field: '" + suffix + "'");
    }
    attribute.setExpressionLanguage(false);
    attribute.setValue(StringUtils.trimToNull(value));
  }

  /**
   * set the fields to the requirement's configured values, blank where a key is not set (the base
   * fills an unset key with the example value from grouper.base.properties, which would show as if it
   * were configured, and be saved).  then mark the fields which cannot be changed read only, before
   * the form values are read, so a posted value for one is skipped rather than read: everything when
   * the requirement is set in a config file, and the uiKey and attribute once they are set
   * @param existing the requirement as configured, null when adding
   */
  public void markFieldsReadOnly(MembershipRequireAdmin existing) {
    if (existing == null) {
      return;
    }
    this.assignValue("uiKey", existing.getUiKey());
    this.assignValue("attributeName", existing.getAttributeName());
    this.assignValue("requireGroupName", existing.getRequireGroupName());
    this.assignValue("hookEnable", existing.isHookEnable() ? "true" : "false");
    this.assignValue("sourceIdsToIgnore", existing.getSourceIdsToIgnore());
    for (String suffix : this.retrieveAttributes().keySet()) {
      GrouperConfigurationModuleAttribute attribute = this.retrieveAttributes().get(suffix);
      if (!existing.isEditable()
          || ("uiKey".equals(suffix) && existing.getUiKey() != null)
          || ("attributeName".equals(suffix) && existing.getAttributeName() != null)) {
        attribute.setReadOnly(true);
      }
    }
  }
}
