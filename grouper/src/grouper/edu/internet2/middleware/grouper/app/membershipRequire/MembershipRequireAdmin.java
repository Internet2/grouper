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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.customComposite.GrouperConfigSourceType;
import edu.internet2.middleware.grouper.app.customComposite.GrouperCustomComposite;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperConfigHibernate;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.config.GrouperUiApiTextConfig;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.provider.SourceManager;

/**
 * a membership requirement as an administrator sees and edits it, for the custom composites screen.
 * unlike {@link MembershipRequireConfigBean}, which only holds requirements which are complete and
 * valid, this holds whatever is configured, so a broken one can be seen and fixed.  a requirement is
 * config in grouper.properties plus the veto message text:
 * <pre>
 * grouper.membershipRequirement.someConfigId.uiKey = requireActiveNetId
 * grouper.membershipRequirement.someConfigId.attributeName = etc:attribute:membershipRequirement:requireActiveNetId
 * grouper.membershipRequirement.someConfigId.requireGroupName = ref:accounts:activeNetIds
 * grouper.membershipRequirement.someConfigId.hookEnable = true
 * grouper.membershipRequirement.someConfigId.sourceIdsToIgnore =
 * grouper.text.en.us.properties: veto.membershipVeto.customComposite.requireActiveNetId = Users must be ...
 * </pre>
 * the attribute marks where the requirement applies: assigned to a group, or to a folder for every
 * group under it.  the attribute itself and its assignments are Grouper objects, audited as such,
 * not config
 *
 * @author shilen
 */
public class MembershipRequireAdmin {

  /** prefix of every requirement key */
  public static final String CONFIG_PREFIX = "grouper.membershipRequirement.";

  /** prefix of the veto message text key, followed by the uiKey */
  public static final String VETO_TEXT_PREFIX = "veto.membershipVeto.customComposite.";

  /** matches a key of one requirement, group 1 is the config id */
  private static final Pattern CONFIG_KEY_PATTERN = Pattern.compile(
      "^grouper\\.membershipRequirement\\.([^.]+)\\.(uiKey|attributeName|requireGroupName|hookEnable|sourceIdsToIgnore)$");

  /** a config id is part of config keys, so keep it to something which cannot be misread in one */
  public static final Pattern CONFIG_ID_PATTERN = Pattern.compile("^[A-Za-z][A-Za-z0-9_]*$");

  /**
   * how the attribute which marks where the requirement applies is chosen
   */
  public static enum AttributeOption {

    /** an attribute which already exists */
    existing,

    /** a new attribute name in an attribute definition which already exists */
    newNameInExistingDef,

    /** a new attribute definition and a new attribute name in it */
    newNameAndDef;
  }

  /** config id */
  private String configId;

  /** uiKey, part of the text key of the veto message */
  private String uiKey;

  /** attribute name */
  private String attributeName;

  /** require group name */
  private String requireGroupName;

  /** hookEnable as configured, blank means true */
  private String hookEnable;

  /** sourceIdsToIgnore as configured, blank means the global setting */
  private String sourceIdsToIgnore;

  /** veto message */
  private String vetoMessage;

  /**
   * the config id of the groupScreen.attribute entry which puts this requirement's attribute on the
   * group create and edit screens, or null if there is none
   */
  private String groupScreenConfigId;

  /** label of the attribute on the group screen */
  private String groupScreenLabel;

  /** description of the attribute on the group screen */
  private String groupScreenDescription;

  /** order of the attribute on the group screen */
  private String groupScreenIndex;

  /** prefix of the keys which put an attribute on the group create and edit screens */
  public static final String GROUP_SCREEN_PREFIX = "groupScreen.attribute.";

  /** matches the attribute name key of a group screen attribute, group 1 is its config id */
  private static final Pattern GROUP_SCREEN_ATTRIBUTE_NAME_PATTERN = Pattern.compile(
      "^groupScreen\\.attribute\\.([^.]+)\\.attributeName$");

  /** the keys of a group screen attribute after its config id, attributeName last */
  private static final List<String> GROUP_SCREEN_SUFFIXES = GrouperUtil.toList("label", "description", "index", "attributeName");

  /**
   * if the attribute is on the group create and edit screens, as a checkbox, so whoever can assign it
   * can apply the requirement to a group there
   * @return true if shown
   */
  public boolean isShownOnGroupScreen() {
    return this.groupScreenConfigId != null;
  }

  /**
   * label of the attribute on the group screen
   * @return the label
   */
  public String getGroupScreenLabel() {
    return this.groupScreenLabel;
  }

  /**
   * description of the attribute on the group screen
   * @return the description
   */
  public String getGroupScreenDescription() {
    return this.groupScreenDescription;
  }

  /**
   * order of the attribute on the group screen
   * @return the index
   */
  public String getGroupScreenIndex() {
    return this.groupScreenIndex;
  }

  /**
   * where the group screen keys come from.  if any is in a config file, the group screen setting is
   * changed in the file
   * @return the source
   */
  public GrouperConfigSourceType getGroupScreenSource() {
    if (this.groupScreenConfigId == null) {
      return GrouperConfigSourceType.none;
    }
    return groupScreenSource(this.groupScreenConfigId);
  }

  /**
   * if the group screen setting is turned off for every attribute (groupScreen.attribute.enabled)
   * @return true if off
   */
  public boolean isGroupScreenDisabledGlobally() {
    return !GrouperConfig.retrieveConfig().propertyValueBoolean(GROUP_SCREEN_PREFIX + "enabled", true);
  }

  /**
   * the config id of the group screen entry for an attribute.  set up by hand, it can have any config
   * id, so look for the one whose attribute name matches
   * @param attributeName the attribute
   * @return the config id, or null if the attribute is not on the group screen
   */
  private static String retrieveGroupScreenConfigId(String attributeName) {
    if (StringUtils.isBlank(attributeName)) {
      return null;
    }
    Map<String, String> keys = GrouperConfig.retrieveConfig().propertiesMap(GROUP_SCREEN_ATTRIBUTE_NAME_PATTERN);
    for (String key : new TreeSet<String>(GrouperUtil.nonNull(keys).keySet())) {
      if (Strings.CS.equals(attributeName, StringUtils.trim(keys.get(key)))) {
        Matcher matcher = GROUP_SCREEN_ATTRIBUTE_NAME_PATTERN.matcher(key);
        if (matcher.matches()) {
          return matcher.group(1);
        }
      }
    }
    return null;
  }

  /**
   * config id
   * @return config id
   */
  public String getConfigId() {
    return this.configId;
  }

  /**
   * uiKey, part of the text key of the veto message
   * @return uiKey
   */
  public String getUiKey() {
    return this.uiKey;
  }

  /**
   * attribute name
   * @return attribute name
   */
  public String getAttributeName() {
    return this.attributeName;
  }

  /**
   * require group name
   * @return group name
   */
  public String getRequireGroupName() {
    return this.requireGroupName;
  }

  /**
   * if new members are vetoed when they are not in the require group.  this is only the add
   * membership hook: the full sync and change log still remove members who do not qualify
   * @return true if vetoing
   */
  public boolean isHookEnable() {
    return GrouperUtil.booleanValue(this.hookEnable, true);
  }

  /**
   * if vetoing is turned off for every requirement (grouper.membershipRequirement.hookEnable), in
   * which case the hook does nothing whatever this requirement's own setting says
   * @return true if off for all
   */
  public boolean isHookDisabledGlobally() {
    return !GrouperConfig.retrieveConfig().propertyValueBoolean(CONFIG_PREFIX + "hookEnable", true);
  }

  /**
   * sourceIdsToIgnore as configured on this requirement
   * @return the sources, comma separated, or null
   */
  public String getSourceIdsToIgnore() {
    return this.sourceIdsToIgnore;
  }

  /**
   * the sources which are actually exempt: this requirement's own list, or the global one when it
   * has none.  blank does not mean nobody is exempt
   * @return the sources, comma separated, or null
   */
  public String getSourceIdsToIgnoreEffective() {
    if (!StringUtils.isBlank(this.sourceIdsToIgnore)) {
      return this.sourceIdsToIgnore;
    }
    return StringUtils.trimToNull(GrouperConfig.retrieveConfig().propertyValueString(CONFIG_PREFIX + "sourceIdsToIgnore"));
  }

  /**
   * veto message
   * @return veto message
   */
  public String getVetoMessage() {
    return this.vetoMessage;
  }

  /**
   * where the config keys come from.  if any is in a config file (or the base file) the whole
   * requirement is
   * @return the source
   */
  public GrouperConfigSourceType getConfigSource() {
    GrouperConfigSourceType result = GrouperConfigSourceType.none;
    for (String suffix : GrouperUtil.toList("uiKey", "attributeName", "requireGroupName", "hookEnable", "sourceIdsToIgnore")) {
      GrouperConfigSourceType source = GrouperConfigSourceType.sourceOf(GrouperConfig.retrieveConfig(),
          CONFIG_PREFIX + this.configId + "." + suffix);
      if (!source.isEditable()) {
        return source;
      }
      if (source == GrouperConfigSourceType.database) {
        result = source;
      }
    }
    return result;
  }

  /**
   * if the config can be changed or deleted on the screen
   * @return true if editable
   */
  public boolean isEditable() {
    return this.getConfigSource().isEditable();
  }

  /**
   * where the veto message comes from
   * @return the source
   */
  public GrouperConfigSourceType getVetoMessageSource() {
    return StringUtils.isBlank(this.uiKey) ? GrouperConfigSourceType.none
        : GrouperConfigSourceType.sourceOf(GrouperUiApiTextConfig.retrieveTextConfigEnUs(), VETO_TEXT_PREFIX + this.uiKey);
  }

  /**
   * the attribute name, if it exists
   * @return the attribute name or null
   */
  public AttributeDefName retrieveAttributeDefName() {
    return StringUtils.isBlank(this.attributeName) ? null : AttributeDefNameFinder.findByName(this.attributeName, false);
  }

  /**
   * what stops this requirement.  the engine skips a requirement with any of these, with only an
   * error in the log, so without this a broken requirement looks fine and does nothing
   * @return text of each problem, empty if none
   */
  public List<String> retrieveProblems() {
    List<String> problems = new ArrayList<String>();
    if (StringUtils.isBlank(this.uiKey)) {
      problems.add(GrouperCustomComposite.text("membershipRequirementProblemUiKeyMissing"));
    }
    if (StringUtils.isBlank(this.attributeName)) {
      problems.add(GrouperCustomComposite.text("membershipRequirementProblemAttributeMissing"));
    } else if (this.retrieveAttributeDefName() == null) {
      problems.add(GrouperCustomComposite.text("membershipRequirementProblemAttributeNotFound"));
    }
    if (StringUtils.isBlank(this.requireGroupName)) {
      problems.add(GrouperCustomComposite.text("membershipRequirementProblemGroupMissing"));
    } else if (GroupFinder.findByName(GrouperSession.staticGrouperSession(), this.requireGroupName, false) == null) {
      problems.add(GrouperCustomComposite.text("membershipRequirementProblemGroupNotFound"));
    }
    return problems;
  }

  /**
   * what is missing but does not stop the requirement: a requirement with no veto message still removes
   * and vetoes members, and a vetoed user sees a default message
   * @return text of each warning, empty if none
   */
  public List<String> retrieveWarnings() {
    List<String> warnings = new ArrayList<String>();
    if (!StringUtils.isBlank(this.uiKey) && StringUtils.isBlank(this.vetoMessage)) {
      warnings.add(GrouperCustomComposite.text("membershipRequirementWarningVetoMessageMissing"));
    }
    return warnings;
  }

  /**
   * every config id which has at least one requirement key
   * @return the config ids, sorted
   */
  public static Set<String> retrieveConfigIds() {
    Set<String> result = new TreeSet<String>();
    Map<String, String> keys = GrouperConfig.retrieveConfig().propertiesMap(CONFIG_KEY_PATTERN);
    for (String key : GrouperUtil.nonNull(keys).keySet()) {
      Matcher matcher = CONFIG_KEY_PATTERN.matcher(key);
      if (matcher.matches()) {
        result.add(matcher.group(1));
      }
    }
    return result;
  }

  /**
   * a requirement as configured
   * @param configId the config id
   * @return the requirement, or null if none of its keys are set
   */
  public static MembershipRequireAdmin retrieve(String configId) {
    if (StringUtils.isBlank(configId) || !retrieveConfigIds().contains(configId)) {
      return null;
    }
    MembershipRequireAdmin result = new MembershipRequireAdmin();
    result.configId = configId;
    result.uiKey = configValue(configId, "uiKey");
    result.attributeName = configValue(configId, "attributeName");
    result.requireGroupName = configValue(configId, "requireGroupName");
    result.hookEnable = configValue(configId, "hookEnable");
    result.sourceIdsToIgnore = configValue(configId, "sourceIdsToIgnore");
    if (result.uiKey != null) {
      result.vetoMessage = GrouperUiApiTextConfig.retrieveTextConfigEnUs().propertyValueString(VETO_TEXT_PREFIX + result.uiKey);
    }
    result.groupScreenConfigId = retrieveGroupScreenConfigId(result.attributeName);
    if (result.groupScreenConfigId != null) {
      String groupScreenKeyPrefix = GROUP_SCREEN_PREFIX + result.groupScreenConfigId + ".";
      result.groupScreenLabel = StringUtils.trimToNull(GrouperConfig.retrieveConfig().propertyValueString(groupScreenKeyPrefix + "label"));
      result.groupScreenDescription = StringUtils.trimToNull(GrouperConfig.retrieveConfig().propertyValueString(groupScreenKeyPrefix + "description"));
      result.groupScreenIndex = StringUtils.trimToNull(GrouperConfig.retrieveConfig().propertyValueString(groupScreenKeyPrefix + "index"));
    }
    return result;
  }

  /**
   * every requirement as configured
   * @return the requirements, sorted by config id
   */
  public static List<MembershipRequireAdmin> retrieveAll() {
    List<MembershipRequireAdmin> result = new ArrayList<MembershipRequireAdmin>();
    for (String configId : retrieveConfigIds()) {
      MembershipRequireAdmin membershipRequireAdmin = retrieve(configId);
      if (membershipRequireAdmin != null) {
        result.add(membershipRequireAdmin);
      }
    }
    return result;
  }

  /**
   * one config value of a requirement
   * @param configId the config id
   * @param suffix e.g. uiKey
   * @return the value or null
   */
  private static String configValue(String configId, String suffix) {
    return StringUtils.trimToNull(GrouperConfig.retrieveConfig().propertyValueString(CONFIG_PREFIX + configId + "." + suffix));
  }

  /**
   * a definition can mark where a requirement applies if it is a marker which can be assigned to
   * groups and folders and is not multi-assignable or multi-valued, like the ones this screen creates.  the group
   * edit screen also skips a multi-assignable or multi-valued definition
   * @param attributeDef the definition
   * @return true if suitable
   */
  public static boolean isSuitableAttributeDef(AttributeDef attributeDef) {
    return attributeDef != null && attributeDef.getValueType() == AttributeDefValueType.marker
        && attributeDef.isAssignToGroup() && attributeDef.isAssignToStem()
        && !attributeDef.isMultiAssignable() && !attributeDef.isMultiValued();
  }

  /**
   * if a name of an attribute or definition to create has a folder and a last part
   * @param name the full name
   * @return true if so
   */
  private static boolean isValidNewName(String name) {
    // a colon after the first character means there is a folder
    if (StringUtils.isBlank(name) || name.indexOf(':') <= 0) {
      return false;
    }
    return !StringUtils.isBlank(GrouperUtil.extensionFromName(name));
  }

  /**
   * check a comma separated list of subject source ids
   * @param sourceIds the list
   * @return true if blank or every id is a source
   */
  public static boolean isValidSourceIds(String sourceIds) {
    Set<String> validSourceIds = new TreeSet<String>();
    for (Source source : SourceManager.getInstance().getSources()) {
      validSourceIds.add(source.getId());
    }
    for (String sourceId : GrouperUtil.nonNull(GrouperUtil.splitTrimToSet(sourceIds, ","))) {
      if (!validSourceIds.contains(sourceId)) {
        return false;
      }
    }
    return true;
  }

  /**
   * add or change a requirement.  the config id, uiKey and attribute are fixed once created.  everything
   * is checked first, including the requirement's keys against their metadata.  then the attribute is
   * created, the veto message written, the keys saved through {@link MembershipRequireConfiguration}, and
   * the group screen entry written
   * @param isNew true to add, false to change
   * @param configId the config id
   * @param uiKey the uiKey, for a new one
   * @param vetoMessage the message shown when a membership is vetoed
   * @param requireGroupName the group members must be in
   * @param hookEnable whether to veto new memberships
   * @param sourceIdsToIgnore comma separated exempt sources, blank for the global setting
   * @param attributeOption how the attribute is chosen, ignored when changing one which has an attribute
   * @param attributeName the attribute name, existing or to create, ignored when changing one which has
   * an attribute
   * @param attributeDefName the definition to create the name in, existing or to create, unless the
   * attribute name already exists
   * @param showOnGroupScreen put the attribute on the group create and edit screens as a checkbox
   * (groupScreen.attribute.*), so it can be applied to a group there
   * @param groupScreenLabel the checkbox's label, required if shown
   * @param groupScreenDescription the checkbox's description
   * @param groupScreenIndex the checkbox's order among the group screen attributes, blank for the end
   * @return field name to error message, empty if saved.  field names are the parameter names, blank
   * for the whole form
   */
  public static Map<String, String> save(boolean isNew, String configId, String uiKey, String vetoMessage,
      String requireGroupName, boolean hookEnable, String sourceIdsToIgnore, AttributeOption attributeOption,
      String attributeName, String attributeDefName, boolean showOnGroupScreen, String groupScreenLabel,
      String groupScreenDescription, String groupScreenIndex) {

    // the database config engine checks this too, but check before anything is looked at or created
    GrouperDbConfig.seeIfAllowed();

    Map<String, String> errors = new LinkedHashMap<String, String>();

    configId = StringUtils.trimToNull(configId);
    uiKey = StringUtils.trimToNull(uiKey);
    vetoMessage = StringUtils.trimToNull(vetoMessage);
    requireGroupName = StringUtils.trimToNull(requireGroupName);
    sourceIdsToIgnore = StringUtils.trimToNull(sourceIdsToIgnore);
    attributeName = StringUtils.trimToNull(attributeName);
    attributeDefName = StringUtils.trimToNull(attributeDefName);
    groupScreenLabel = StringUtils.trimToNull(groupScreenLabel);
    groupScreenDescription = StringUtils.trimToNull(groupScreenDescription);
    groupScreenIndex = StringUtils.trimToNull(groupScreenIndex);

    MembershipRequireAdmin existing = retrieve(configId);

    if (isNew) {
      if (configId == null || !CONFIG_ID_PATTERN.matcher(configId).matches()) {
        errors.put("configId", GrouperCustomComposite.text("membershipRequirementErrorConfigIdInvalid"));
      } else if (GrouperConfigHibernate.containsPasswordRelatedWords(configId)) {
        // the config id is in every key, and the database config engine encrypts and masks the
        // value of a key with one of these words
        errors.put("configId", GrouperCustomComposite.text("customCompositeErrorPasswordRelatedWords"));
      } else if (existing != null) {
        errors.put("configId", GrouperCustomComposite.text("membershipRequirementErrorConfigIdInUse"));
      }
    } else {
      if (existing == null) {
        errors.put("", GrouperCustomComposite.text("membershipRequirementErrorNotFound"));
        return errors;
      }
      if (!existing.isEditable()) {
        errors.put("", GrouperCustomComposite.text("membershipRequirementErrorReadOnly"));
        return errors;
      }
    }

    boolean newUiKey = existing == null || existing.getUiKey() == null;
    if (newUiKey) {
      String uiKeyError = uiKey == null ? "customCompositeErrorUiKeyRequired"
          : GrouperCustomComposite.validateNewUiKey(uiKey, false, VETO_TEXT_PREFIX + uiKey);
      if (uiKeyError != null) {
        errors.put("uiKey", GrouperCustomComposite.text(uiKeyError));
      }
    } else {
      uiKey = existing.getUiKey();
    }

    // a veto message set in a config file stays as it is
    boolean vetoMessageEditable = newUiKey || existing.getVetoMessageSource().isEditable();
    if (vetoMessageEditable && vetoMessage == null) {
      errors.put("vetoMessage", GrouperCustomComposite.text("membershipRequirementErrorVetoMessageRequired"));
    }

    // the group field can hold a group id (from the picker) or a name: save the name
    if (requireGroupName != null) {
      Group requireGroupById = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), requireGroupName, false);
      if (requireGroupById != null) {
        requireGroupName = requireGroupById.getName();
      }
    }
    if (requireGroupName == null || GroupFinder.findByName(GrouperSession.staticGrouperSession(), requireGroupName, false) == null) {
      errors.put("requireGroupName", GrouperCustomComposite.text("customCompositeErrorGroupNotFound"));
    }

    if (!isValidSourceIds(sourceIdsToIgnore)) {
      errors.put("sourceIdsToIgnore", GrouperCustomComposite.text("membershipRequirementErrorSourceIdsInvalid"));
    }

    // the attribute is fixed once a requirement has one, like the config id: changing it would leave
    // the old attribute's assignments behind and its group screen entry to sort out.  for another
    // attribute, add a new requirement.  one with no attribute yet (incomplete config) can pick one
    boolean attributeFixed = existing != null && existing.getAttributeName() != null;
    if (attributeFixed) {
      attributeOption = AttributeOption.existing;
      attributeName = existing.getAttributeName();
    }

    // the attribute: check everything before creating anything, so a form with an error elsewhere
    // does not leave a new definition behind
    AttributeDef attributeDefToUse = null;
    if (attributeOption == null) {
      errors.put("attributeOption", GrouperCustomComposite.text("membershipRequirementErrorAttributeOptionRequired"));
    } else if (attributeName == null) {
      errors.put("attributeName", GrouperCustomComposite.text("membershipRequirementErrorAttributeNameRequired"));
    } else if (attributeOption == AttributeOption.existing) {
      AttributeDefName existingName = AttributeDefNameFinder.findByName(attributeName, false);
      // the attribute a requirement already uses is kept as it is, even if it was set up some other
      // way before this screen, e.g. assignable to groups only, or since deleted: the problems list
      // reports that, and refusing here would block every edit on a field which cannot be changed
      if (existingName == null && !attributeFixed) {
        errors.put("attributeName", GrouperCustomComposite.text("membershipRequirementErrorAttributeNotFound"));
      } else if (!attributeFixed && existingName != null && !isSuitableAttributeDef(existingName.getAttributeDef())) {
        errors.put("attributeName", GrouperCustomComposite.text("membershipRequirementErrorAttributeDefNotSuitable"));
      }
    } else {
      // a name to create needs a folder and a last part, e.g. not the form's default of the folder
      // followed by a colon, which would fail partway through creating it
      if (!isValidNewName(attributeName)) {
        errors.put("attributeName", GrouperCustomComposite.text("membershipRequirementErrorNameInvalid"));
      } else if (AttributeDefNameFinder.findByName(attributeName, false) != null) {
        errors.put("attributeName", GrouperCustomComposite.text("membershipRequirementErrorAttributeExists"));
      }
      if (attributeDefName == null) {
        errors.put("attributeDefName", GrouperCustomComposite.text("membershipRequirementErrorAttributeDefNameRequired"));
      } else if (attributeOption == AttributeOption.newNameAndDef && !isValidNewName(attributeDefName)) {
        errors.put("attributeDefName", GrouperCustomComposite.text("membershipRequirementErrorNameInvalid"));
      } else {
        attributeDefToUse = AttributeDefFinder.findByName(attributeDefName, false);
        if (attributeOption == AttributeOption.newNameInExistingDef) {
          if (attributeDefToUse == null) {
            errors.put("attributeDefName", GrouperCustomComposite.text("membershipRequirementErrorAttributeDefNotFound"));
          } else if (!isSuitableAttributeDef(attributeDefToUse)) {
            errors.put("attributeDefName", GrouperCustomComposite.text("membershipRequirementErrorAttributeDefNotSuitable"));
          }
        } else if (attributeDefToUse != null) {
          errors.put("attributeDefName", GrouperCustomComposite.text("membershipRequirementErrorAttributeDefExists"));
        }
      }
    }

    // the group screen entry belongs to the attribute, not the requirement: another requirement can use
    // the same attribute and so the same entry.  use the entry the attribute already has, under
    // whatever config id, otherwise a new one under this requirement's config id
    String groupScreenConfigId = attributeName == null ? null : retrieveGroupScreenConfigId(attributeName);
    // an entry set in a config file is changed in the file
    boolean groupScreenEditable = groupScreenConfigId == null || groupScreenSource(groupScreenConfigId).isEditable();
    // when adding (or picking the attribute for an incomplete requirement) the form could not show an
    // entry the attribute already has, so ticking the box only confirms it is shown and it is left as
    // it is, whether it is in the database or a config file: writing the form's blank fields would
    // change the checkbox for every requirement on the attribute.  its label and order are changed
    // when editing a requirement on the attribute
    boolean groupScreenKeepExisting = groupScreenConfigId != null && !attributeFixed;
    if (groupScreenEditable && showOnGroupScreen && !groupScreenKeepExisting) {
      if (groupScreenLabel == null) {
        errors.put("groupScreenLabel", GrouperCustomComposite.text("membershipRequirementErrorGroupScreenLabelRequired"));
      }
      if (groupScreenIndex != null && (!StringUtils.isNumeric(groupScreenIndex) || groupScreenIndex.length() > 6)) {
        errors.put("groupScreenIndex", GrouperCustomComposite.text("membershipRequirementErrorGroupScreenIndexInvalid"));
      }
      // an attribute a requirement already uses skipped the definition check above.  the group screen
      // silently leaves out one which is multi-assignable or multi-valued, or not assignable to groups,
      // and shows one with a value (e.g. a string) as a text field rather than a checkbox, so only a
      // marker is put there.  one being created here is set up correctly
      AttributeDefName groupScreenAttributeDefName = attributeName == null ? null : AttributeDefNameFinder.findByName(attributeName, false);
      if (groupScreenAttributeDefName != null) {
        AttributeDef groupScreenAttributeDef = groupScreenAttributeDefName.getAttributeDef();
        if (groupScreenAttributeDef.getValueType() != AttributeDefValueType.marker
            || groupScreenAttributeDef.isMultiAssignable() || groupScreenAttributeDef.isMultiValued()
            || !groupScreenAttributeDef.isAssignToGroup()) {
          errors.put("showOnGroupScreen", GrouperCustomComposite.text("membershipRequirementErrorGroupScreenAttributeNotSupported"));
        }
      } else if (attributeOption == AttributeOption.existing) {
        // e.g. a requirement's attribute which has since been deleted: an entry for it would show
        // nothing and log a warning on every group screen.  one being created here is created first
        errors.put("showOnGroupScreen", GrouperCustomComposite.text("membershipRequirementErrorAttributeNotFound"));
      }
      // a new entry uses this config id, which must not already be another attribute's
      if (groupScreenConfigId == null && configId != null) {
        for (String suffix : GROUP_SCREEN_SUFFIXES) {
          if (GrouperConfig.retrieveConfig().containsKey(GROUP_SCREEN_PREFIX + configId + "." + suffix)) {
            errors.put("showOnGroupScreen", GrouperCustomComposite.text("membershipRequirementErrorGroupScreenConfigIdInUse"));
            break;
          }
        }
      }
    }

    // the keys in grouper.properties are saved as a configuration module, which also checks them
    // against their metadata in grouper.base.properties (types, the group exists).  checked here,
    // before anything is created or written
    MembershipRequireConfiguration membershipRequireConfiguration = null;
    if (configId != null && !errors.containsKey("configId")) {
      membershipRequireConfiguration = new MembershipRequireConfiguration();
      membershipRequireConfiguration.setConfigId(configId);
      membershipRequireConfiguration.assignValue("uiKey", uiKey);
      membershipRequireConfiguration.assignValue("attributeName", attributeName);
      membershipRequireConfiguration.assignValue("requireGroupName", requireGroupName);
      membershipRequireConfiguration.assignValue("hookEnable", hookEnable ? "true" : "false");
      membershipRequireConfiguration.assignValue("sourceIdsToIgnore", sourceIdsToIgnore);
      List<String> moduleErrors = new ArrayList<String>();
      Map<String, String> moduleValidationErrors = new LinkedHashMap<String, String>();
      membershipRequireConfiguration.validatePreSave(isNew, moduleErrors, moduleValidationErrors);
      addModuleErrors(errors, moduleErrors, moduleValidationErrors, membershipRequireConfiguration.getConfigIdElementIdHandle());
    }

    if (errors.size() > 0) {
      return errors;
    }

    // create the attribute.  these are Grouper objects, audited as the logged in administrator
    if (attributeOption == AttributeOption.newNameAndDef) {
      attributeDefToUse = new AttributeDefSave(GrouperSession.staticGrouperSession()).assignName(attributeDefName)
          .assignCreateParentStemsIfNotExist(true).assignValueType(AttributeDefValueType.marker)
          .assignAttributeDefType(AttributeDefType.attr).assignToGroup(true).assignToStem(true)
          .assignMultiAssignable(false).assignMultiValued(false).save();
    }
    if (attributeOption != AttributeOption.existing) {
      new AttributeDefNameSave(GrouperSession.staticGrouperSession(), attributeDefToUse).assignName(attributeName)
          .assignCreateParentStemsIfNotExist(true).save();
    }

    if (vetoMessageEditable) {
      GrouperCustomComposite.storeConfig(GrouperCustomComposite.TEXT_PROPERTIES, VETO_TEXT_PREFIX + uiKey, vetoMessage);
    }

    // the requirement's keys, through the configuration module, which saves them to database
    // config and removes a blank one (e.g. sourceIdsToIgnore when cleared)
    List<String> moduleErrors = new ArrayList<String>();
    Map<String, String> moduleValidationErrors = new LinkedHashMap<String, String>();
    if (isNew) {
      membershipRequireConfiguration.insertConfig(true, new StringBuilder(), moduleErrors, moduleValidationErrors, new ArrayList<String>());
    } else {
      membershipRequireConfiguration.editConfig(true, new StringBuilder(), moduleErrors, moduleValidationErrors, new ArrayList<String>());
    }
    addModuleErrors(errors, moduleErrors, moduleValidationErrors, membershipRequireConfiguration.getConfigIdElementIdHandle());
    if (errors.size() > 0) {
      return errors;
    }

    // the group screen entry, attribute name last so a partial save leaves it not shown.  unticking
    // removes it only when editing a requirement which already had this attribute (the form showed the
    // box ticked), and no other requirement uses the attribute: one set up by hand for an attribute
    // picked when adding is left alone, and so is one another requirement relies on
    if (groupScreenEditable && !groupScreenKeepExisting) {
      if (showOnGroupScreen) {
        String theGroupScreenConfigId = groupScreenConfigId == null ? configId : groupScreenConfigId;
        String groupScreenKeyPrefix = GROUP_SCREEN_PREFIX + theGroupScreenConfigId + ".";
        storeOrDeleteConfig(groupScreenKeyPrefix + "label", groupScreenLabel);
        storeOrDeleteConfig(groupScreenKeyPrefix + "description", groupScreenDescription);
        storeOrDeleteConfig(groupScreenKeyPrefix + "index", groupScreenIndex);
        storeOrDeleteConfig(groupScreenKeyPrefix + "attributeName", attributeName);
      } else if (attributeFixed && groupScreenConfigId != null
          && !isAttributeUsedByAnotherRequirement(attributeName, configId)) {
        deleteGroupScreenConfig(groupScreenConfigId);
      }
    }

    // this node uses the saved requirement straight away.  other nodes pick it up when their
    // caches expire, within five minutes
    MembershipRequireEngine.clearCaches();

    return errors;
  }

  /**
   * if a requirement other than this one uses an attribute.  the engine allows several requirements
   * on one attribute, and they share its group screen entry
   * @param attributeName the attribute
   * @param configId this requirement, which is not counted
   * @return true if another requirement uses it
   */
  private static boolean isAttributeUsedByAnotherRequirement(String attributeName, String configId) {
    if (StringUtils.isBlank(attributeName)) {
      return false;
    }
    Map<String, String> keys = GrouperConfig.retrieveConfig().propertiesMap(CONFIG_KEY_PATTERN);
    for (String key : GrouperUtil.nonNull(keys).keySet()) {
      Matcher matcher = CONFIG_KEY_PATTERN.matcher(key);
      if (!matcher.matches() || !"attributeName".equals(matcher.group(2))) {
        continue;
      }
      if (!Strings.CS.equals(configId, matcher.group(1)) && Strings.CS.equals(attributeName, StringUtils.trim(keys.get(key)))) {
        return true;
      }
    }
    return false;
  }

  /**
   * where the keys of a group screen entry come from
   * @param groupScreenConfigId the entry's config id
   * @return the source, not editable if any key is in a config file
   */
  private static GrouperConfigSourceType groupScreenSource(String groupScreenConfigId) {
    GrouperConfigSourceType result = GrouperConfigSourceType.none;
    for (String suffix : GROUP_SCREEN_SUFFIXES) {
      GrouperConfigSourceType source = GrouperConfigSourceType.sourceOf(GrouperConfig.retrieveConfig(),
          GROUP_SCREEN_PREFIX + groupScreenConfigId + "." + suffix);
      if (!source.isEditable()) {
        return source;
      }
      if (source == GrouperConfigSourceType.database) {
        result = source;
      }
    }
    return result;
  }

  /**
   * remove a group screen entry from database config, attribute name first so a partial delete leaves
   * it not shown
   * @param groupScreenConfigId the entry's config id
   */
  private static void deleteGroupScreenConfig(String groupScreenConfigId) {
    storeOrDeleteConfig(GROUP_SCREEN_PREFIX + groupScreenConfigId + ".attributeName", null);
    for (String suffix : GrouperUtil.toList("label", "description", "index")) {
      storeOrDeleteConfig(GROUP_SCREEN_PREFIX + groupScreenConfigId + "." + suffix, null);
    }
  }

  /**
   * add the configuration module's errors to the save's.  its field messages are keyed by the
   * element on the screen (#config_suffix_spanid), and are kept that way.  ones about the config id
   * repeat this class's own check of it, so are left out when that already found a problem
   * @param errors the save's errors, field name or element to message, blank for the whole form
   * @param moduleErrors the module's errors for the whole form
   * @param moduleValidationErrors the module's field errors
   * @param configIdElementIdHandle the module's config id element
   */
  private static void addModuleErrors(Map<String, String> errors, List<String> moduleErrors,
      Map<String, String> moduleValidationErrors, String configIdElementIdHandle) {
    for (String elementIdHandle : moduleValidationErrors.keySet()) {
      if (Strings.CS.equals(configIdElementIdHandle, elementIdHandle) && errors.containsKey("configId")) {
        continue;
      }
      errors.put(elementIdHandle, moduleValidationErrors.get(elementIdHandle));
    }
    if (moduleErrors.size() > 0) {
      errors.put("", GrouperUtil.join(moduleErrors.iterator(), " "));
    }
  }

  /**
   * store one grouper.properties value, or remove it from the database if blank
   * @param key the key
   * @param value the value
   */
  private static void storeOrDeleteConfig(String key, String value) {
    if (StringUtils.isBlank(value)) {
      if (GrouperConfigSourceType.sourceOf(GrouperConfig.retrieveConfig(), key) == GrouperConfigSourceType.database) {
        GrouperCustomComposite.deleteConfig(GrouperCustomComposite.GROUPER_PROPERTIES, key);
      }
      return;
    }
    GrouperCustomComposite.storeConfig(GrouperCustomComposite.GROUPER_PROPERTIES, key, value);
  }

  /**
   * delete a requirement's config and texts, and its group screen entry.  the attribute and its
   * assignments are left: deleting them is a separate decision, made on the attribute's own screens.
   * the keys are removed by the configuration module: a delete which stops partway leaves the
   * requirement incomplete, so the engine skips it
   * @param configId the config id
   * @return error message, or null if deleted
   */
  public static String delete(String configId) {

    GrouperDbConfig.seeIfAllowed();

    MembershipRequireAdmin existing = retrieve(configId);
    if (existing == null) {
      return GrouperCustomComposite.text("membershipRequirementErrorNotFound");
    }
    if (!existing.isEditable()) {
      return GrouperCustomComposite.text("membershipRequirementErrorReadOnly");
    }

    // off the group screen first, so nobody applies a requirement which is going away.  an entry set
    // in a config file stays, and is changed in the file, and so does one for an attribute another
    // requirement still uses
    if (existing.groupScreenConfigId != null && existing.getGroupScreenSource().isEditable()
        && !isAttributeUsedByAnotherRequirement(existing.getAttributeName(), configId)) {
      deleteGroupScreenConfig(existing.groupScreenConfigId);
    }

    // the requirement's keys, through the configuration module, which removes them from database config
    MembershipRequireConfiguration membershipRequireConfiguration = new MembershipRequireConfiguration();
    membershipRequireConfiguration.setConfigId(configId);
    membershipRequireConfiguration.deleteConfig(true);

    // the veto message stays if it is in a config file, or if another entry still uses the same uiKey
    if (existing.getUiKey() != null && !GrouperCustomComposite.retrieveUiKeysInUse().containsKey(existing.getUiKey())
        && existing.getVetoMessageSource() == GrouperConfigSourceType.database) {
      GrouperCustomComposite.deleteConfig(GrouperCustomComposite.TEXT_PROPERTIES, VETO_TEXT_PREFIX + existing.getUiKey());
    }

    MembershipRequireEngine.clearCaches();
    return null;
  }
}
