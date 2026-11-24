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
package edu.internet2.middleware.grouper.app.customComposite;

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

import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperConfigHibernate;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.config.GrouperUiApiTextConfig;

/**
 * a custom composite: a filter on the members screen of a group (and of groups in a folder) which
 * intersects the members with, or takes away from them, the members of another group.  each is
 * three keys in grouper.properties sharing an index, plus a label in the UI text under the uiKey:
 * <pre>
 * grouper.membership.customComposite.uiKey.0 = customCompositeMinusEmployees
 * grouper.membership.customComposite.compositeType.0 = complement
 * grouper.membership.customComposite.groupName.0 = ref:activeEmployees
 * grouper.text.en.us.properties: customCompositeMinusEmployees = Members who are not employees
 * </pre>
 * the indexes do not have to be consecutive.  a deleted custom composite leaves a gap rather than
 * renumbering the others, so an index a user already picked on an open page still means the same
 * filter, and an entry set in a config file never has to move.  the one exception: a new one gets
 * the index after the highest in use, so if the highest is deleted, the next one added reuses its
 * index.  an index with only some of its three keys is incomplete and is not offered
 *
 * @author shilen
 */
public class GrouperCustomComposite {

  /** config file the composite keys are in */
  public static final String GROUPER_PROPERTIES = "grouper.properties";

  /** config file the labels are in */
  public static final String TEXT_PROPERTIES = "grouper.text.en.us.properties";

  /** prefix of every custom composite key */
  public static final String CONFIG_PREFIX = "grouper.membership.customComposite.";

  /**
   * matches one of the three keys of a custom composite, group 2 is the index.  at most nine digits,
   * so an index always fits in an int: a typo with a longer number does not match, rather than
   * failing every members screen which reads these, and the config check reports it as misspelled
   */
  public static final Pattern KEY_PATTERN = Pattern.compile(
      "^grouper\\.membership\\.customComposite\\.(uiKey|compositeType|groupName)\\.(\\d{1,9})$");

  /** matches the uiKey of a membership requirement, which shares the text namespace */
  private static final Pattern MEMBERSHIP_REQUIREMENT_UI_KEY_PATTERN = Pattern.compile(
      "^grouper\\.membershipRequirement\\.([^.]+)\\.uiKey$");

  /**
   * a uiKey is used as a text key, so it is held to something which cannot be read as anything else
   * in a properties file and cannot reach into another part of the text namespace with a dot
   */
  public static final Pattern UI_KEY_PATTERN = Pattern.compile("^[A-Za-z][A-Za-z0-9_]*$");

  /** the index in config */
  private int index;

  /** the uiKey, which is also the text key of the label */
  private String uiKey;

  /** complement or intersection */
  private String compositeType;

  /** the other group */
  private String groupName;

  /** the label shown in the members filter */
  private String label;

  /**
   * the index in config
   * @return the index
   */
  public int getIndex() {
    return this.index;
  }

  /**
   * the uiKey, which is also the text key of the label
   * @return the uiKey
   */
  public String getUiKey() {
    return this.uiKey;
  }

  /**
   * complement or intersection
   * @return the type
   */
  public String getCompositeType() {
    return this.compositeType;
  }

  /**
   * the other group
   * @return the group name
   */
  public String getGroupName() {
    return this.groupName;
  }

  /**
   * the label shown in the members filter
   * @return the label
   */
  public String getLabel() {
    return this.label;
  }

  /**
   * if all three keys are set.  only a complete one is offered on the members screen
   * @return true if complete
   */
  public boolean isComplete() {
    return !StringUtils.isBlank(this.uiKey) && !StringUtils.isBlank(this.compositeType)
        && !StringUtils.isBlank(this.groupName);
  }

  /**
   * where the three keys come from.  if any of them is in a config file (or the base file) the whole
   * composite is, since editing two of its keys in the database and leaving the third in the file
   * would split it across two places
   * @return the source
   */
  public GrouperConfigSourceType getConfigSource() {
    GrouperConfigSourceType result = GrouperConfigSourceType.none;
    for (String key : this.configKeys()) {
      GrouperConfigSourceType source = GrouperConfigSourceType.sourceOf(GrouperConfig.retrieveConfig(), key);
      if (!source.isEditable()) {
        return source;
      }
      if (source == GrouperConfigSourceType.database || result == GrouperConfigSourceType.none) {
        result = source;
      }
    }
    return result;
  }

  /**
   * where the label comes from
   * @return the source
   */
  public GrouperConfigSourceType getLabelSource() {
    if (StringUtils.isBlank(this.uiKey)) {
      return GrouperConfigSourceType.none;
    }
    return GrouperConfigSourceType.sourceOf(GrouperUiApiTextConfig.retrieveTextConfigEnUs(), this.uiKey);
  }

  /**
   * if this can be changed or deleted on the screen
   * @return true if editable
   */
  public boolean isEditable() {
    return this.getConfigSource().isEditable();
  }

  /**
   * the three config keys, uiKey first
   * @return the keys
   */
  private List<String> configKeys() {
    return GrouperUtil.toList(CONFIG_PREFIX + "uiKey." + this.index,
        CONFIG_PREFIX + "compositeType." + this.index, CONFIG_PREFIX + "groupName." + this.index);
  }

  /**
   * every index which has at least one of the three keys, in order
   * @return the indexes
   */
  public static Set<Integer> retrieveIndexes() {
    Set<Integer> result = new TreeSet<Integer>();
    Map<String, String> keys = GrouperConfig.retrieveConfig().propertiesMap(KEY_PATTERN);
    for (String key : GrouperUtil.nonNull(keys).keySet()) {
      Matcher matcher = KEY_PATTERN.matcher(key);
      if (matcher.matches()) {
        result.add(GrouperUtil.intValue(matcher.group(2)));
      }
    }
    return result;
  }

  /**
   * the custom composite at an index
   * @param index the index
   * @return the composite, or null if none of its keys are set
   */
  public static GrouperCustomComposite retrieve(int index) {
    GrouperCustomComposite result = new GrouperCustomComposite();
    result.index = index;
    result.uiKey = StringUtils.trimToNull(GrouperConfig.retrieveConfig().propertyValueString(CONFIG_PREFIX + "uiKey." + index));
    // the members screen reads the type ignoring case, so Intersection or INTERSECTION works there.
    // lower case it here so the edit screen shows it as the type it is, and does not save it as the
    // first choice instead
    result.compositeType = StringUtils.lowerCase(StringUtils.trimToNull(GrouperConfig.retrieveConfig().propertyValueString(CONFIG_PREFIX + "compositeType." + index)));
    result.groupName = StringUtils.trimToNull(GrouperConfig.retrieveConfig().propertyValueString(CONFIG_PREFIX + "groupName." + index));
    if (result.uiKey == null && result.compositeType == null && result.groupName == null) {
      return null;
    }
    if (result.uiKey != null) {
      result.label = GrouperUiApiTextConfig.retrieveTextConfigEnUs().propertyValueString(result.uiKey);
    }
    return result;
  }

  /**
   * an index sent from a screen
   * @param indexString the index
   * @return the index, or null if it is not one to nine digits, so a long number cannot overflow
   */
  public static Integer parseIndex(String indexString) {
    if (StringUtils.isBlank(indexString) || !StringUtils.isNumeric(indexString) || indexString.length() > 9) {
      return null;
    }
    return GrouperUtil.intValue(indexString);
  }

  /**
   * every custom composite, complete or not, in index order
   * @return the composites
   */
  public static List<GrouperCustomComposite> retrieveAll() {
    List<GrouperCustomComposite> result = new ArrayList<GrouperCustomComposite>();
    for (Integer index : retrieveIndexes()) {
      GrouperCustomComposite grouperCustomComposite = retrieve(index);
      if (grouperCustomComposite != null) {
        result.add(grouperCustomComposite);
      }
    }
    return result;
  }

  /**
   * the uiKeys of every custom composite and membership requirement.  both use their uiKey as a text
   * key, so they share one namespace
   * @return uiKey to how many entries use it
   */
  public static Map<String, Integer> retrieveUiKeysInUse() {
    Map<String, Integer> result = new LinkedHashMap<String, Integer>();
    for (GrouperCustomComposite grouperCustomComposite : retrieveAll()) {
      addUiKey(result, grouperCustomComposite.getUiKey());
    }
    Map<String, String> requirementUiKeys = GrouperConfig.retrieveConfig().propertiesMap(MEMBERSHIP_REQUIREMENT_UI_KEY_PATTERN);
    for (String uiKeyValue : GrouperUtil.nonNull(requirementUiKeys).values()) {
      addUiKey(result, StringUtils.trimToNull(uiKeyValue));
    }
    return result;
  }

  /**
   * count a uiKey
   * @param uiKeys the counts
   * @param uiKey the key, skipped if null
   */
  private static void addUiKey(Map<String, Integer> uiKeys, String uiKey) {
    if (uiKey == null) {
      return;
    }
    Integer count = uiKeys.get(uiKey);
    uiKeys.put(uiKey, count == null ? 1 : count + 1);
  }

  /**
   * check a new uiKey.  it is (part of) a text key, so it has to be safe to use as one, and a text key
   * written for it must not already exist: it would overwrite that UI text
   * @param uiKey the new uiKey
   * @param uiKeyIsTextKey true if the uiKey itself is written as a text key, e.g. a custom composite's
   * label.  a membership requirement only writes its veto message
   * @param otherTextKeys other text keys which will be written for it, e.g. the veto message
   * @return the text key of the error, or null if it is fine
   */
  public static String validateNewUiKey(String uiKey, boolean uiKeyIsTextKey, String... otherTextKeys) {
    if (StringUtils.isBlank(uiKey)) {
      return "customCompositeErrorUiKeyRequired";
    }
    if (!UI_KEY_PATTERN.matcher(uiKey).matches()) {
      return "customCompositeErrorUiKeyInvalid";
    }
    // the database config engine encrypts and masks the value of a key with one of these words, so
    // the label (and veto message) would be stored as if it were a secret
    if (GrouperConfigHibernate.containsPasswordRelatedWords(uiKey)) {
      return "customCompositeErrorPasswordRelatedWords";
    }
    if (retrieveUiKeysInUse().containsKey(uiKey)) {
      return "customCompositeErrorUiKeyInUse";
    }
    if (uiKeyIsTextKey && GrouperUiApiTextConfig.retrieveTextConfigEnUs().containsKey(uiKey)) {
      return "customCompositeErrorUiKeyTextExists";
    }
    for (String otherTextKey : otherTextKeys) {
      if (GrouperUiApiTextConfig.retrieveTextConfigEnUs().containsKey(otherTextKey)) {
        return "customCompositeErrorOtherTextExists";
      }
    }
    return null;
  }

  /**
   * the next index for a new custom composite, one past the highest in use
   * @return the index
   */
  public static int nextIndex() {
    int result = 0;
    for (Integer index : retrieveIndexes()) {
      result = Math.max(result, index + 1);
    }
    return result;
  }

  /**
   * add or change a custom composite.  the uiKey is fixed once created, since it is also the text
   * key of the label.  the label is written first and the uiKey last, so a save which stops partway
   * leaves an incomplete composite, which is not offered, rather than one with no label
   * @param index the index to change, or null to add a new one
   * @param uiKey the uiKey for a new one, ignored when changing one which has a uiKey
   * @param label the label
   * @param compositeType complement or intersection
   * @param groupName the other group
   * @return field name (uiKey, label, compositeType, groupName, or blank for the whole form) to
   * error message, empty if saved
   */
  public static Map<String, String> save(Integer index, String uiKey, String label, String compositeType, String groupName) {

    // the database config engine checks this too, but check before anything is looked at
    GrouperDbConfig.seeIfAllowed();

    Map<String, String> errors = new LinkedHashMap<String, String>();

    uiKey = StringUtils.trimToNull(uiKey);
    label = StringUtils.trimToNull(label);
    compositeType = StringUtils.trimToNull(compositeType);
    groupName = StringUtils.trimToNull(groupName);

    GrouperCustomComposite existing = index == null ? null : retrieve(index);

    if (index != null && existing == null) {
      errors.put("", text("customCompositeErrorNotFound"));
      return errors;
    }

    if (existing != null && !existing.isEditable()) {
      errors.put("", text("customCompositeErrorReadOnly"));
      return errors;
    }

    boolean newUiKey = existing == null || existing.getUiKey() == null;

    if (newUiKey) {
      String uiKeyError = validateNewUiKey(uiKey, true);
      if (uiKeyError != null) {
        errors.put("uiKey", text(uiKeyError));
      }
    } else {
      uiKey = existing.getUiKey();
    }

    // a label set in a config file stays as it is
    boolean labelEditable = newUiKey || existing.getLabelSource().isEditable();
    if (labelEditable && label == null) {
      errors.put("label", text("customCompositeErrorLabelRequired"));
    }

    if (!Strings.CS.equals("complement", compositeType) && !Strings.CS.equals("intersection", compositeType)) {
      errors.put("compositeType", text("customCompositeErrorCompositeTypeInvalid"));
    }

    if (groupName == null || GroupFinder.findByName(GrouperSession.staticGrouperSession(), groupName, false) == null) {
      errors.put("groupName", text("customCompositeErrorGroupNotFound"));
    }

    if (errors.size() > 0) {
      return errors;
    }

    int theIndex = index == null ? nextIndex() : index;

    if (labelEditable) {
      storeConfig(TEXT_PROPERTIES, uiKey, label);
    }
    storeConfig(GROUPER_PROPERTIES, CONFIG_PREFIX + "compositeType." + theIndex, compositeType);
    storeConfig(GROUPER_PROPERTIES, CONFIG_PREFIX + "groupName." + theIndex, groupName);
    if (newUiKey) {
      storeConfig(GROUPER_PROPERTIES, CONFIG_PREFIX + "uiKey." + theIndex, uiKey);
    }

    return errors;
  }

  /**
   * delete a custom composite.  only its own three keys and label go: the other indexes are left as
   * they are.  the uiKey goes first, so a delete which stops partway leaves an incomplete composite,
   * which is not offered
   * @param index the index
   * @return error message, or null if deleted
   */
  public static String delete(int index) {

    GrouperDbConfig.seeIfAllowed();

    GrouperCustomComposite existing = retrieve(index);
    if (existing == null) {
      return text("customCompositeErrorNotFound");
    }
    if (!existing.isEditable()) {
      return text("customCompositeErrorReadOnly");
    }

    for (String key : existing.configKeys()) {
      deleteConfig(GROUPER_PROPERTIES, key);
    }

    // the label stays if it is in a config file, or if another entry still uses the same uiKey
    if (existing.getUiKey() != null && existing.getLabelSource() == GrouperConfigSourceType.database
        && !retrieveUiKeysInUse().containsKey(existing.getUiKey())) {
      deleteConfig(TEXT_PROPERTIES, existing.getUiKey());
    }
    return null;
  }

  /**
   * store a value in database config, which is audited, kept in history, and seen by other nodes
   * @param configFileName e.g. grouper.properties
   * @param key the key
   * @param value the value
   */
  public static void storeConfig(String configFileName, String key, String value) {
    new GrouperDbConfig().configFileName(configFileName).propertyName(key).value(value).store();
  }

  /**
   * delete a value from database config, if it is there
   * @param configFileName e.g. grouper.properties
   * @param key the key
   */
  public static void deleteConfig(String configFileName, String key) {
    new GrouperDbConfig().configFileName(configFileName).propertyName(key).delete();
  }

  /**
   * externalized text
   * @param key the text key
   * @return the text
   */
  public static String text(String key) {
    return GrouperTextContainer.retrieveFromRequest().getText().get(key);
  }
}
