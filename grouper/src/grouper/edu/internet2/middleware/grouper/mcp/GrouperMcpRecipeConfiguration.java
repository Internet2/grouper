/*******************************************************************************
 * Copyright 2024 Internet2
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
package edu.internet2.middleware.grouper.mcp;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleBase;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * Configuration module for MCP recipes, which gives the UI its add/edit/delete wizard for
 * {@link GrouperMcpRecipe}.  The runtime read path does not go through this class: MCP reads
 * recipes straight from config through GrouperMcpRecipe, which is cheap because config is
 * already cached on every node.  This class exists for editing and validation.
 *
 * @author mchyzer
 */
public class GrouperMcpRecipeConfiguration extends GrouperConfigurationModuleBase {

  @Override
  public String getConfigIdElementIdHandle() {
    return "#mcpRecipeConfigId";
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
    return "grouperMcpRecipe." + this.getConfigId() + ".";
  }

  @Override
  public String getConfigIdRegex() {
    return "^(grouperMcpRecipe)\\.([^.]+)\\.(.*)$";
  }

  @Override
  protected String getConfigurationTypePrefix() {
    return "grouperMcpRecipe";
  }

  @Override
  public String getConfigIdThatIdentifiesThisConfig() {
    return "mcpRecipeConfigId";
  }

  /**
   * list of configured MCP recipes
   * @return the recipe configurations
   */
  public static List<GrouperMcpRecipeConfiguration> retrieveAllMcpRecipeConfigurations() {
    Set<String> classNames = new HashSet<String>();
    classNames.add(GrouperMcpRecipeConfiguration.class.getName());
    return (List<GrouperMcpRecipeConfiguration>) (Object) retrieveAllConfigurations(classNames);
  }

  /**
   * the recipe screens write through this class, so each write drops this node's cache.  without
   * it an administrator saves a recipe and then sees the old one for the rest of the TTL
   */
  @Override
  public void insertConfig(boolean fromUi, StringBuilder message, List<String> errorsToDisplay,
      Map<String, String> validationErrorsToDisplay, List<String> actionsPerformed) {
    super.insertConfig(fromUi, message, errorsToDisplay, validationErrorsToDisplay, actionsPerformed);
    if (errorsToDisplay.size() == 0 && validationErrorsToDisplay.size() == 0) {
      GrouperMcpRecipe.stampAttribution(this.getConfigId());
    }
    GrouperMcpRecipe.clearCache();
  }

  @Override
  public void editConfig(boolean fromUi, StringBuilder message, List<String> errorsToDisplay,
      Map<String, String> validationErrorsToDisplay, List<String> actionsPerformed) {
    super.editConfig(fromUi, message, errorsToDisplay, validationErrorsToDisplay, actionsPerformed);
    if (errorsToDisplay.size() == 0 && validationErrorsToDisplay.size() == 0) {
      GrouperMcpRecipe.stampAttribution(this.getConfigId());
    }
    GrouperMcpRecipe.clearCache();
  }

  @Override
  public void deleteConfig(boolean fromUi) {
    super.deleteConfig(fromUi);
    GrouperMcpRecipe.clearCache();
  }

  /**
   * the fields which decide who a recipe reaches and what it can influence.  a recipe's content
   * owner, who holds groupNameCanEdit but administers nothing, sees these on the edit screen but
   * cannot change them.  keep this in step with the groupNameCanEdit documentation in
   * grouper.base.properties, which tells a deployer exactly this list
   */
  private static final Set<String> ADMIN_ONLY_SUFFIXES = new HashSet<String>(Arrays.asList(
      "enabled", "groupNameCanUse", "groupNameCanEdit", "toolNames", "priority"));

  /**
   * one edit screen serves both audiences, so what a person may change is decided by this call
   * rather than by which screen they were sent to.  a recipe administrator keeps everything.  a
   * content owner gets the wording only, with the rest marked read only so they can still see
   * who the recipe reaches and which tools it is attached to.
   *
   * <p>This is enforcement, not presentation: populateConfigurationValuesFromUi skips read only
   * attributes, so a content owner posting a value for one of these has it ignored rather than
   * saved.  The attribute keeps the value it was built with from config, so the subsequent save
   * writes back what was already there.  Call this before populating from the request.</p>
   *
   * <p>Deliberately an explicit call from the screens rather than an override of
   * retrieveAttributes.  That method is shared with server side validation - MCP's update path
   * reaches it through GrouperMcpRecipe.validateFieldValues - and validation must not change
   * with who is asking, nor pay for a group lookup on a request which is not rendering a
   * form.</p>
   *
   * @param loggedInSubject the subject the screen is being rendered for or submitted by
   */
  public void markAdminOnlyFieldsReadOnly(Subject loggedInSubject) {

    if (GrouperMcpRecipe.canAdminInUi(loggedInSubject)) {
      return;
    }

    Map<String, GrouperConfigurationModuleAttribute> attributes = this.retrieveAttributes();

    for (String suffix : ADMIN_ONLY_SUFFIXES) {
      GrouperConfigurationModuleAttribute attribute = attributes.get(suffix);
      if (attribute != null) {
        attribute.setReadOnly(true);
      }
    }
  }

  /**
   * the three last edited fields are written by Grouper and cannot be typed into, so there is
   * nothing to show until Grouper has written them.  on the add screen that is always, which is
   * where three empty read only rows were most obviously wrong, and it is also true of a recipe
   * which has only ever been edited in a properties file or in the database.  hide them until
   * they hold something.
   * @param suffix the attribute suffix
   * @return false to hide, null to leave the decision to the configuration metadata
   */
  @Override
  public Boolean showAttributeOverride(String suffix) {

    if (Strings.CS.equals("lastEditedBy", suffix)
        || Strings.CS.equals("lastEditedByName", suffix)
        || Strings.CS.equals("lastEditedOn", suffix)) {
      return StringUtils.isNotBlank(this.retrieveAttributeValueFromConfig(suffix, false));
    }

    return null;
  }

  @Override
  public void validatePreSave(boolean isInsert, List<String> errorsToDisplay,
      Map<String, String> validationErrorsToDisplay) {

    super.validatePreSave(isInsert, errorsToDisplay, validationErrorsToDisplay);
    if (errorsToDisplay.size() > 0 || validationErrorsToDisplay.size() > 0) {
      return;
    }

    // the name is what an AI client asks for a recipe by, so two recipes cannot share one
    GrouperConfigurationModuleAttribute nameAttribute = this.retrieveAttributes().get("name");

    if (nameAttribute != null && StringUtils.isNotBlank(nameAttribute.getValueOrExpressionEvaluation())) {

      String name = nameAttribute.getValueOrExpressionEvaluation();

      // including the disabled ones: a name taken by a recipe which is turned off is still
      // taken, or re-enabling it would leave two recipes a client cannot tell apart.
      // read past the cache: it is node local, so a name taken on another node inside the TTL
      // would otherwise look free here
      Map<String, GrouperMcpRecipe> existingRecipes = GrouperMcpRecipe.retrieveAllRecipes(true, false);
      GrouperMcpRecipe existingRecipe = existingRecipes.get(name);

      if (existingRecipe != null && !Strings.CS.equals(this.getConfigId(), existingRecipe.getConfigId())) {
        String errorMessage = GrouperTextContainer.retrieveFromRequest().getText()
            .get("mcpRecipeNameAlreadyUsedError");
        errorMessage = Strings.CS.replace(errorMessage, "##recipeName##", name);
        errorsToDisplay.add(errorMessage);
      }
    }

    // a group which does not resolve means nobody can use or edit the recipe, which is a
    // silent failure at runtime, so catch it here where somebody is watching
    validateGroup("groupNameCanUse", validationErrorsToDisplay);
    validateGroup("groupNameCanEdit", validationErrorsToDisplay);

    // a recipe pointing at a tool which does not exist adds its pointer to nothing at all
    GrouperConfigurationModuleAttribute toolNamesAttribute = this.retrieveAttributes().get("toolNames");

    if (toolNamesAttribute != null
        && StringUtils.isNotBlank(toolNamesAttribute.getValueOrExpressionEvaluation())) {

      for (String toolName : GrouperUtil.splitTrimToList(
          toolNamesAttribute.getValueOrExpressionEvaluation(), ",")) {

        if (!GrouperMcpToolNames.isToolName(toolName)) {
          String errorMessage = GrouperTextContainer.retrieveFromRequest().getText()
              .get("mcpRecipeToolNotFoundError");
          errorMessage = Strings.CS.replace(errorMessage, "##toolName##",
              GrouperUtil.xmlEscape(toolName));
          validationErrorsToDisplay.put(toolNamesAttribute.getHtmlForElementIdHandle(), errorMessage);
          return;
        }
      }
    }
  }

  /**
   * add a validation error if a group name is set but does not resolve
   * @param attributeName the config attribute holding the group name
   * @param validationErrorsToDisplay errors keyed by element id
   */
  private void validateGroup(String attributeName, Map<String, String> validationErrorsToDisplay) {

    GrouperConfigurationModuleAttribute attribute = this.retrieveAttributes().get(attributeName);

    if (attribute == null || StringUtils.isBlank(attribute.getValueOrExpressionEvaluation())) {
      return;
    }

    String groupName = attribute.getValueOrExpressionEvaluation();

    GrouperSession rootSession = GrouperSession.startRootSession();

    try {
      Group group = GroupFinder.findByName(rootSession, groupName, false);

      if (group == null) {
        String errorMessage = GrouperTextContainer.retrieveFromRequest().getText()
            .get("mcpRecipeGroupNotFoundError");
        errorMessage = Strings.CS.replace(errorMessage, "##groupName##",
            GrouperUtil.xmlEscape(groupName));
        validationErrorsToDisplay.put(attribute.getHtmlForElementIdHandle(), errorMessage);
      }
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

}
