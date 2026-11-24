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
package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.customComposite.GrouperCustomComposite;
import edu.internet2.middleware.grouper.app.membershipRequire.MembershipRequireAdmin;
import edu.internet2.middleware.grouper.app.membershipRequire.MembershipRequireConfiguration;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperConfigHibernate;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.CustomCompositeContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * the custom composites and membership requirements screen.  a custom composite is a filter on a
 * group's members screen; a membership requirement removes, or vetoes, members of a group who are not
 * in another group.  both are config plus UI text, which this screen saves through database config so
 * changes are audited, kept in history, and seen by every node.  only Grouper system administrators
 * can use it, checked on every action here and again in the core when anything is saved
 *
 * @author shilen
 */
public class UiV2CustomComposite extends UiServiceLogicBase {

  /**
   * start the session and check access.  every action calls this first.  besides being a sysadmin,
   * this screen is held to the same controls as the configuration editor, since it changes
   * configuration (and membership requirements remove members): the configuration UI must be on,
   * and an action which changes something must come from an allowed source IP
   * (grouperUi.configurationEditor.sourceIpAddresses)
   * @param changesConfig true for an action which saves or deletes
   * @return the session, to stop in a finally, or null if not allowed, in which case the message
   * has been added and the action should return
   */
  private static GrouperSession startAndCheckAllowed(boolean changesConfig) {
    Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
    GrouperSession grouperSession = GrouperSession.start(loggedInSubject);
    // only the sysadmin check here: whether the configuration UI is on is checked below, which says
    // so on the screen rather than failing the request
    if (!PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
      GrouperSession.stopQuietly(grouperSession);
      throw new RuntimeException("Not allowed!!!!!");
    }
    UiV2Configure uiV2Configure = new UiV2Configure();
    boolean allowed = changesConfig ? uiV2Configure.allowedToViewConfiguration() : uiV2Configure.allowedToViewConfigurationIndex();
    if (!allowed) {
      GrouperSession.stopQuietly(grouperSession);
      return null;
    }
    return grouperSession;
  }

  /**
   * show the list of custom composites and membership requirements
   * @param request
   * @param response
   */
  public void index(HttpServletRequest request, HttpServletResponse response) {
    GrouperSession grouperSession = startAndCheckAllowed(false);
    if (grouperSession == null) {
      return;
    }
    try {
      showIndex();
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * draw the list screen
   */
  private static void showIndex() {
    CustomCompositeContainer customCompositeContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getCustomCompositeContainer();
    customCompositeContainer.setCustomComposites(GrouperCustomComposite.retrieveAll());
    customCompositeContainer.setMembershipRequirements(MembershipRequireAdmin.retrieveAll());
    GuiResponseJs.retrieveGuiResponseJs().addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
        "/WEB-INF/grouperUi2/customComposite/customCompositeIndex.jsp"));
  }

  /**
   * show the form to add a custom composite
   * @param request
   * @param response
   */
  public void addCustomComposite(HttpServletRequest request, HttpServletResponse response) {
    GrouperSession grouperSession = startAndCheckAllowed(false);
    if (grouperSession == null) {
      return;
    }
    try {
      GuiResponseJs.retrieveGuiResponseJs().addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/customComposite/customCompositeEdit.jsp"));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * show the form to edit a custom composite
   * @param request
   * @param response
   */
  public void editCustomComposite(HttpServletRequest request, HttpServletResponse response) {
    GrouperSession grouperSession = startAndCheckAllowed(false);
    if (grouperSession == null) {
      return;
    }
    try {
      GrouperCustomComposite grouperCustomComposite = retrieveCustomComposite(request);
      if (grouperCustomComposite == null) {
        GuiResponseJs.retrieveGuiResponseJs().addAction(GuiScreenAction.newMessage(GuiMessageType.error,
            TextContainer.retrieveFromRequest().getText().get("customCompositeErrorNotFound")));
        showIndex();
        return;
      }
      CustomCompositeContainer customCompositeContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getCustomCompositeContainer();
      customCompositeContainer.setCustomComposite(grouperCustomComposite);
      GuiResponseJs.retrieveGuiResponseJs().addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/customComposite/customCompositeEdit.jsp"));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * the custom composite named on the request
   * @param request the request, with customCompositeIndex
   * @return the composite or null
   */
  private static GrouperCustomComposite retrieveCustomComposite(HttpServletRequest request) {
    Integer index = GrouperCustomComposite.parseIndex(request.getParameter("customCompositeIndex"));
    return index == null ? null : GrouperCustomComposite.retrieve(index);
  }

  /**
   * add or change a custom composite
   * @param request
   * @param response
   */
  public void saveCustomComposite(HttpServletRequest request, HttpServletResponse response) {
    GrouperSession grouperSession = startAndCheckAllowed(true);
    if (grouperSession == null) {
      return;
    }
    try {
      String index = request.getParameter("customCompositeIndex");
      Integer theIndex = GrouperCustomComposite.parseIndex(index);
      if (!StringUtils.isBlank(index) && theIndex == null) {
        throw new RuntimeException("Invalid customCompositeIndex");
      }

      Map<String, String> errors = GrouperCustomComposite.save(theIndex,
          request.getParameter("customCompositeUiKey"), request.getParameter("customCompositeLabel"),
          request.getParameter("customCompositeCompositeType"), retrieveGroupName(request, "config_groupName"));

      // the group picker is the generic form element, whose error goes next to its own element
      if (errors.containsKey("groupName")) {
        errors.put("#config_groupName_spanid", errors.remove("groupName"));
      }
      if (addErrors(errors, "customComposite")) {
        return;
      }

      GuiResponseJs.retrieveGuiResponseJs().addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2CustomComposite.index')"));
      GuiResponseJs.retrieveGuiResponseJs().addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("customCompositeSaveSuccess")));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * delete a custom composite
   * @param request
   * @param response
   */
  public void deleteCustomComposite(HttpServletRequest request, HttpServletResponse response) {
    GrouperSession grouperSession = startAndCheckAllowed(true);
    if (grouperSession == null) {
      return;
    }
    try {
      Integer index = GrouperCustomComposite.parseIndex(request.getParameter("customCompositeIndex"));
      if (index == null) {
        throw new RuntimeException("customCompositeIndex is required");
      }
      String error = GrouperCustomComposite.delete(index);
      if (error != null) {
        GuiResponseJs.retrieveGuiResponseJs().addAction(GuiScreenAction.newMessage(GuiMessageType.error, error));
        return;
      }
      showIndex();
      GuiResponseJs.retrieveGuiResponseJs().addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("customCompositeDeleteSuccess")));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * show the form to add a membership requirement
   * @param request
   * @param response
   */
  public void addMembershipRequirement(HttpServletRequest request, HttpServletResponse response) {
    GrouperSession grouperSession = startAndCheckAllowed(false);
    if (grouperSession == null) {
      return;
    }
    try {
      CustomCompositeContainer customCompositeContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getCustomCompositeContainer();

      // as on other configuration screens, the form posts back to itself once the config id is entered,
      // which draws the rest: the configuration module needs a config id to build its fields
      String configId = StringUtils.trimToNull(request.getParameter("membershipRequirementConfigId"));
      if (configId != null) {
        if (!MembershipRequireAdmin.CONFIG_ID_PATTERN.matcher(configId).matches()) {
          GuiResponseJs.retrieveGuiResponseJs().addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
              "#membershipRequirementConfigId", TextContainer.retrieveFromRequest().getText().get("membershipRequirementErrorConfigIdInvalid")));
        } else if (GrouperConfigHibernate.containsPasswordRelatedWords(configId)) {
          GuiResponseJs.retrieveGuiResponseJs().addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
              "#membershipRequirementConfigId", TextContainer.retrieveFromRequest().getText().get("customCompositeErrorPasswordRelatedWords")));
        } else {
          MembershipRequireConfiguration membershipRequireConfiguration = new MembershipRequireConfiguration();
          membershipRequireConfiguration.setConfigId(configId);
          membershipRequireConfiguration.populateConfigurationValuesFromUi(request);
          // veto new additions starts on, until it is changed
          if (membershipRequireConfiguration.retrieveValue("hookEnable") == null) {
            membershipRequireConfiguration.assignValue("hookEnable", "true");
          }
          customCompositeContainer.setMembershipRequireConfiguration(membershipRequireConfiguration);
        }
      }

      Map<String, String> form = retrieveMembershipRequirementForm(request);
      // what was typed, so a config id which was refused stays in the field to fix
      form.put("configId", configId);
      if (form.get("attributeOption") == null) {
        form.put("attributeOption", MembershipRequireAdmin.AttributeOption.newNameAndDef.name());
      }
      customCompositeContainer.setMembershipRequirementForm(form);

      GuiResponseJs.retrieveGuiResponseJs().addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/customComposite/membershipRequirementEdit.jsp"));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * show the form to edit a membership requirement
   * @param request
   * @param response
   */
  public void editMembershipRequirement(HttpServletRequest request, HttpServletResponse response) {
    GrouperSession grouperSession = startAndCheckAllowed(false);
    if (grouperSession == null) {
      return;
    }
    try {
      if (!assignMembershipRequirement(request)) {
        return;
      }
      CustomCompositeContainer customCompositeContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getCustomCompositeContainer();
      MembershipRequireAdmin membershipRequireAdmin = customCompositeContainer.getMembershipRequirement();

      // the module's fields come from config.  the ones which cannot be changed are read only
      MembershipRequireConfiguration membershipRequireConfiguration = new MembershipRequireConfiguration();
      membershipRequireConfiguration.setConfigId(membershipRequireAdmin.getConfigId());
      membershipRequireConfiguration.markFieldsReadOnly(membershipRequireAdmin);
      customCompositeContainer.setMembershipRequireConfiguration(membershipRequireConfiguration);

      Map<String, String> form = new HashMap<String, String>();
      form.put("vetoMessage", membershipRequireAdmin.getVetoMessage());
      form.put("attributeOption", MembershipRequireAdmin.AttributeOption.existing.name());
      form.put("showOnGroupScreen", membershipRequireAdmin.isShownOnGroupScreen() ? "true" : null);
      form.put("groupScreenLabel", membershipRequireAdmin.getGroupScreenLabel());
      form.put("groupScreenDescription", membershipRequireAdmin.getGroupScreenDescription());
      form.put("groupScreenIndex", membershipRequireAdmin.getGroupScreenIndex());
      customCompositeContainer.setMembershipRequirementForm(form);

      GuiResponseJs.retrieveGuiResponseJs().addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/customComposite/membershipRequirementEdit.jsp"));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * the requirement form's own fields from the request, the ones which are not in the configuration
   * module
   * @param request the request
   * @return field name to value, blank ones left out
   */
  private static Map<String, String> retrieveMembershipRequirementForm(HttpServletRequest request) {
    Map<String, String> form = new HashMap<String, String>();
    for (String field : GrouperUtil.toList("vetoMessage", "attributeOption", "attributeDefName",
        "showOnGroupScreen", "groupScreenLabel", "groupScreenDescription", "groupScreenIndex")) {
      String value = StringUtils.trimToNull(request.getParameter("membershipRequirement" + StringUtils.capitalize(field)));
      if (value != null) {
        form.put(field, value);
      }
    }
    return form;
  }

  /**
   * put the membership requirement named on the request in the container
   * @param request the request, with membershipRequirementConfigId
   * @return true if found, false if not, in which case the list is shown with an error
   */
  private static boolean assignMembershipRequirement(HttpServletRequest request) {
    MembershipRequireAdmin membershipRequireAdmin = MembershipRequireAdmin.retrieve(request.getParameter("membershipRequirementConfigId"));
    if (membershipRequireAdmin == null) {
      GuiResponseJs.retrieveGuiResponseJs().addAction(GuiScreenAction.newMessage(GuiMessageType.error,
          TextContainer.retrieveFromRequest().getText().get("membershipRequirementErrorNotFound")));
      showIndex();
      return false;
    }
    GrouperRequestContainer.retrieveFromRequestOrCreate().getCustomCompositeContainer().setMembershipRequirement(membershipRequireAdmin);
    return true;
  }

  /**
   * add or change a membership requirement
   * @param request
   * @param response
   */
  public void saveMembershipRequirement(HttpServletRequest request, HttpServletResponse response) {
    GrouperSession grouperSession = startAndCheckAllowed(true);
    if (grouperSession == null) {
      return;
    }
    try {
      boolean isNew = GrouperUtil.booleanValue(request.getParameter("membershipRequirementIsNew"), false);
      String configId = StringUtils.trimToNull(request.getParameter("membershipRequirementConfigId"));

      // the requirement's grouper.properties keys come from the configuration module's generic form
      // fields.  a read only one (set in a config file, or the uiKey and attribute once set) keeps its
      // configured value rather than what was posted.  with no valid config id there is no module, and
      // the save reports the config id.  the pattern is only for a new one: an existing requirement can
      // have any config id, e.g. one added in the configuration editor
      String uiKey = null;
      String attributeName = null;
      String requireGroupName = null;
      String hookEnable = null;
      String sourceIdsToIgnore = null;
      MembershipRequireAdmin existingMembershipRequirement = (isNew || configId == null) ? null : MembershipRequireAdmin.retrieve(configId);
      if (configId != null && (isNew ? MembershipRequireAdmin.CONFIG_ID_PATTERN.matcher(configId).matches()
          : existingMembershipRequirement != null)) {
        MembershipRequireConfiguration membershipRequireConfiguration = new MembershipRequireConfiguration();
        membershipRequireConfiguration.setConfigId(configId);
        membershipRequireConfiguration.markFieldsReadOnly(existingMembershipRequirement);
        membershipRequireConfiguration.populateConfigurationValuesFromUi(request);
        uiKey = membershipRequireConfiguration.retrieveValue("uiKey");
        attributeName = membershipRequireConfiguration.retrieveValue("attributeName");
        requireGroupName = membershipRequireConfiguration.retrieveValue("requireGroupName");
        hookEnable = membershipRequireConfiguration.retrieveValue("hookEnable");
        sourceIdsToIgnore = membershipRequireConfiguration.retrieveValue("sourceIdsToIgnore");
      }

      Map<String, String> form = retrieveMembershipRequirementForm(request);
      MembershipRequireAdmin.AttributeOption attributeOption = GrouperUtil.enumValueOfIgnoreCase(
          MembershipRequireAdmin.AttributeOption.class, form.get("attributeOption"), false);

      Map<String, String> errors = MembershipRequireAdmin.save(isNew, configId, uiKey,
          form.get("vetoMessage"), requireGroupName, GrouperUtil.booleanValue(hookEnable, true),
          sourceIdsToIgnore, attributeOption, attributeName, form.get("attributeDefName"),
          GrouperUtil.booleanValue(form.get("showOnGroupScreen"), false),
          form.get("groupScreenLabel"), form.get("groupScreenDescription"), form.get("groupScreenIndex"));

      // errors on the module's fields go next to the generic form's element for that field
      Map<String, String> errorsByElement = new LinkedHashMap<String, String>();
      for (String field : errors.keySet()) {
        boolean moduleField = GrouperUtil.toList("uiKey", "attributeName", "requireGroupName", "hookEnable", "sourceIdsToIgnore").contains(field);
        errorsByElement.put(moduleField ? "#config_" + field + "_spanid" : field, errors.get(field));
      }
      if (addErrors(errorsByElement, "membershipRequirement")) {
        return;
      }

      GuiResponseJs.retrieveGuiResponseJs().addAction(GuiScreenAction.newScript("guiV2link('operation=UiV2CustomComposite.index')"));
      GuiResponseJs.retrieveGuiResponseJs().addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("membershipRequirementSaveSuccess")));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * delete a membership requirement's config and texts.  the attribute and its assignments stay
   * @param request
   * @param response
   */
  public void deleteMembershipRequirement(HttpServletRequest request, HttpServletResponse response) {
    GrouperSession grouperSession = startAndCheckAllowed(true);
    if (grouperSession == null) {
      return;
    }
    try {
      String error = MembershipRequireAdmin.delete(request.getParameter("membershipRequirementConfigId"));
      if (error != null) {
        GuiResponseJs.retrieveGuiResponseJs().addAction(GuiScreenAction.newMessage(GuiMessageType.error, error));
        return;
      }
      showIndex();
      GuiResponseJs.retrieveGuiResponseJs().addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("membershipRequirementDeleteSuccess")));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * the group chosen in a group picker, as a name.  a picker sends the group id when one was picked
   * from the list, or what was typed otherwise
   * @param request the request
   * @param idBase the start of the picker's field names, which end in Name and NameDisplay
   * @return the group name, or what was typed if it is not a group, so the save reports it
   */
  private static String retrieveGroupName(HttpServletRequest request, String idBase) {
    String value = StringUtils.trimToNull(request.getParameter(idBase + "Name"));
    if (value == null) {
      value = StringUtils.trimToNull(request.getParameter(idBase + "NameDisplay"));
    }
    if (value == null) {
      return null;
    }
    Group group = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), value, false);
    if (group == null) {
      group = GroupFinder.findByName(GrouperSession.staticGrouperSession(), value, false);
    }
    return group == null ? value : group.getName();
  }

  /**
   * show errors from a save
   * @param errors field name to message, blank field name for the whole form
   * @param idPrefix the prefix of the form's field ids, e.g. customComposite for #customCompositeLabel
   * @return true if there were any
   */
  private static boolean addErrors(Map<String, String> errors, String idPrefix) {
    if (GrouperUtil.length(errors) == 0) {
      return false;
    }
    for (String field : errors.keySet()) {
      if (StringUtils.isBlank(field)) {
        GuiResponseJs.retrieveGuiResponseJs().addAction(GuiScreenAction.newMessageAppend(GuiMessageType.error, errors.get(field)));
      } else if (field.startsWith("#")) {
        // already an element on the screen, e.g. a configuration module field (#config_uiKey_spanid)
        GuiResponseJs.retrieveGuiResponseJs().addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            field, errors.get(field)));
      } else {
        GuiResponseJs.retrieveGuiResponseJs().addAction(GuiScreenAction.newValidationMessage(GuiMessageType.error,
            "#" + idPrefix + StringUtils.capitalize(field), errors.get(field)));
      }
    }
    return true;
  }
}
