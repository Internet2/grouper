package edu.internet2.middleware.grouper.app.loader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.abac.GrouperAbac;
import edu.internet2.middleware.grouper.abac.GrouperJexlScriptAnalysis;
import edu.internet2.middleware.grouper.abac.GrouperLoaderJexlScriptFullSync;
import edu.internet2.middleware.grouper.app.serviceLifecycle.GrouperRecentMemberships;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.dataField.GrouperDataEngine;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * the checks made before a loader config is imported onto a group (GrouperLoaderImportExport
 * .importLoaderConfig).  an import is a save by the importing user, not a trusted restore: the
 * config is written as root and a scripted group's script is then run by the daemon as root, so
 * without these checks a group admin could import a script which uses groups they cannot read and
 * have those members copied into their own group.
 *
 * <p>JEXL_SCRIPT: the user must be allowed to edit scripted groups on the group
 * (PrivilegeHelper.canEditAbacLoader, not the abac-or-recent-memberships check the import screen
 * opens with), and the script must pass the checks the UI's loader screen makes before it saves
 * (see checkJexlScript: this is a patch for a release, so they are the checks that release's UI
 * makes).</p>
 *
 * <p>RECENT_MEMBERSHIPS: the user must be allowed to edit recent memberships loaders on the group
 * (PrivilegeHelper.canEditRecentMembershipsLoader), the group the memberships come from must exist
 * and the user must be able to READ it, and the time span must be a positive number, as on the
 * UI's loader screen.</p>
 *
 * <p>for both, only the attributes the UI saves for that type may be in the import, each at most
 * once; anything else refuses the whole import, so a mistake or an attempt is visible.  values the
 * daemon sets itself (the scripted group's last sync time) are accepted and not written.</p>
 *
 * <p>SQL and LDAP: not checked here, the caller checks them.  the UI only lets loader editors
 * (wheel, or the uiV2.loader.edit.if.in.group group, which is UI config) import those, in
 * UiV2GrouperLoader.validatePrivilegesForImport.  any attribute names are accepted for them, as
 * before.</p>
 *
 * <p>the checks run as the user of the current grouper session, before anything is written.</p>
 */
public class GrouperLoaderImportAuthorization {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperLoaderImportAuthorization.class);

  /** loader type of a scripted group */
  public static final String LOADER_TYPE_JEXL_SCRIPT = "JEXL_SCRIPT";

  /** loader type of a recent memberships group */
  public static final String LOADER_TYPE_RECENT_MEMBERSHIPS = "RECENT_MEMBERSHIPS";

  // ---------------------------------------------------------------------------------------------
  // attribute names

  /**
   * @return the scripted group script attribute name
   */
  private static String jexlScriptAttributeName() {
    return GrouperAbac.jexlScriptStemName() + ":" + GrouperAbac.GROUPER_JEXL_SCRIPT_JEXL_SCRIPT;
  }

  /**
   * @return the scripted group subject source ids attribute name
   */
  private static String jexlSourceIdsAttributeName() {
    return GrouperAbac.jexlScriptStemName() + ":" + GrouperAbac.GROUPER_JEXL_SCRIPT_SUBJECT_SOURCE_IDS;
  }

  /**
   * @return the scripted group last sync attribute name, which the daemon sets
   */
  private static String jexlLastSyncAttributeName() {
    return GrouperAbac.jexlScriptStemName() + ":" + GrouperAbac.GROUPER_JEXL_SCRIPT_JEXL_LAST_GROUP_SYNC;
  }

  /**
   * @return recent memberships attribute name of the time span in micros
   */
  private static String recentMicrosAttributeName() {
    return GrouperRecentMemberships.recentMembershipsStemName() + ":" + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_ATTR_MICROS;
  }

  /**
   * @return recent memberships attribute name of the group uuid the memberships come from
   */
  private static String recentGroupUuidFromAttributeName() {
    return GrouperRecentMemberships.recentMembershipsStemName() + ":" + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_ATTR_GROUP_UUID_FROM;
  }

  /**
   * @return recent memberships attribute name of whether current members are included
   */
  private static String recentIncludeCurrentAttributeName() {
    return GrouperRecentMemberships.recentMembershipsStemName() + ":" + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_ATTR_INCLUDE_CURRENT;
  }

  /**
   * @param loaderType
   * @return the attribute names an import of this type may write, or null if any (SQL, LDAP)
   */
  private static Set<String> attributeNamesToWrite(String loaderType) {
    Set<String> result = new HashSet<String>();
    if (StringUtils.equals(loaderType, LOADER_TYPE_JEXL_SCRIPT)) {
      result.add(jexlScriptAttributeName());
      result.add(jexlSourceIdsAttributeName());
      return result;
    }
    if (StringUtils.equals(loaderType, LOADER_TYPE_RECENT_MEMBERSHIPS)) {
      result.add(recentMicrosAttributeName());
      result.add(recentGroupUuidFromAttributeName());
      result.add(recentIncludeCurrentAttributeName());
      return result;
    }
    return null;
  }

  /**
   * @param loaderType
   * @return the attribute names an import of this type may contain but which are not written,
   * since the daemon sets them
   */
  private static Set<String> attributeNamesToIgnore(String loaderType) {
    Set<String> result = new HashSet<String>();
    if (StringUtils.equals(loaderType, LOADER_TYPE_JEXL_SCRIPT)) {
      result.add(jexlLastSyncAttributeName());
    }
    return result;
  }

  /**
   * whether the import should write this attribute.  call only after checkImport found nothing
   * @param loaderType
   * @param attributeName
   * @return true to write it, false to skip it
   */
  public static boolean isAttributeToWrite(String loaderType, String attributeName) {
    Set<String> toWrite = attributeNamesToWrite(loaderType);
    // SQL and LDAP: checked by the caller, written as before
    if (toWrite == null) {
      return true;
    }
    return toWrite.contains(attributeName);
  }

  // ---------------------------------------------------------------------------------------------
  // the check

  /**
   * check an import before anything is written
   * @param group the group it is imported onto
   * @param loaderType the loaderType in the json
   * @param jsonNode the parsed json
   * @param subject the importing user (the subject of the current grouper session)
   * @return the errors, escaped for html, or an empty list if it can be imported
   */
  public static List<String> checkImport(Group group, String loaderType, JsonNode jsonNode, Subject subject) {

    List<String> errors = new ArrayList<String>();

    boolean isJexl = StringUtils.equals(loaderType, LOADER_TYPE_JEXL_SCRIPT);
    boolean isRecent = StringUtils.equals(loaderType, LOADER_TYPE_RECENT_MEMBERSHIPS);

    // SQL and LDAP are checked by the caller, and an unknown type is refused by the import
    if (!isJexl && !isRecent) {
      return errors;
    }

    if (group == null || subject == null) {
      errors.add("Not allowed to import this loader config");
      return errors;
    }

    // who may do it first, so someone who may not learns nothing about the content
    if (isJexl && !canEditAbacLoader(subject, group)) {
      errors.add("Not allowed to configure a scripted (JEXL) loader on this group");
      return errors;
    }
    if (isRecent && !canEditRecentMembershipsLoader(subject, group)) {
      errors.add("Not allowed to configure a recent memberships loader on this group");
      return errors;
    }

    Map<String, String> attributes = attributesByName(loaderType, jsonNode, errors);
    if (errors.size() > 0) {
      return errors;
    }

    if (isJexl) {
      checkJexlScript(group, attributes, subject, errors);
    } else {
      checkRecentMemberships(attributes, subject, errors);
    }
    return errors;
  }

  /**
   * the attributes in the import, by name.  refuses a missing name, a name not allowed for the
   * type, and a name given twice (the check would see one value and the import write the other)
   * @param loaderType
   * @param jsonNode
   * @param errors add errors here
   * @return the attribute values by name
   */
  private static Map<String, String> attributesByName(String loaderType, JsonNode jsonNode, List<String> errors) {

    Map<String, String> result = new LinkedHashMap<String, String>();
    Set<String> toWrite = attributeNamesToWrite(loaderType);
    Set<String> toIgnore = attributeNamesToIgnore(loaderType);

    ArrayNode attributesArray = null;
    try {
      attributesArray = GrouperUtil.jsonJacksonGetArrayNode(jsonNode, "attributes");
    } catch (RuntimeException re) {
      errors.add("Invalid json");
      return result;
    }

    for (int index = 0; attributesArray != null && index < attributesArray.size(); index++) {
      JsonNode singleAttributeNode = attributesArray.get(index);
      String attributeName = GrouperUtil.jsonJacksonGetString(singleAttributeNode, "attributeName");
      String attributeValue = GrouperUtil.jsonJacksonGetString(singleAttributeNode, "attributeValue");
      if (StringUtils.isBlank(attributeName)) {
        errors.add("Invalid json: an attribute has no attributeName");
        continue;
      }
      if (!toWrite.contains(attributeName) && !toIgnore.contains(attributeName)) {
        errors.add("Attribute not allowed in a " + loaderType + " loader import: '" + GrouperUtil.xmlEscape(attributeName) + "'");
        continue;
      }
      if (result.containsKey(attributeName)) {
        errors.add("Attribute is in the import more than once: '" + GrouperUtil.xmlEscape(attributeName) + "'");
        continue;
      }
      result.put(attributeName, attributeValue);
    }
    return result;
  }

  // ---------------------------------------------------------------------------------------------
  // scripted groups

  /**
   * the checks this release's UI loader screen makes before it saves a script
   * (UiV2GrouperLoader.editGrouperLoaderSave): a script is given, GrouperAbac.validScript finds
   * nothing wrong, the analysis in save mode gives no error and no warning (this is where READ on
   * every group the script uses, and update on the privacy realm of every data field, is checked),
   * and it selects no more members than the user is allowed
   * @param group
   * @param attributes
   * @param subject
   * @param errors add errors here
   */
  private static void checkJexlScript(Group group, Map<String, String> attributes, Subject subject, List<String> errors) {

    String script = attributes.get(jexlScriptAttributeName());
    if (StringUtils.isBlank(script)) {
      errors.add("The script is required");
      return;
    }

    // the sources the daemon will use for this group (GrouperLoaderJexlScriptFullSync), which
    // drop any not in the available list
    Set<String> effectiveSourceIds = GrouperAbac.effectiveSubjectSourceIds(attributes.get(jexlSourceIdsAttributeName()));

    GrouperDataEngine grouperDataEngine = new GrouperDataEngine();
    grouperDataEngine.loadFieldsAndRows(GrouperConfig.retrieveConfig());

    String invalidMessage = GrouperAbac.validScript(script, grouperDataEngine);
    if (!StringUtils.isBlank(invalidMessage)) {
      errors.add("The script is not valid: " + GrouperUtil.xmlEscape(invalidMessage));
      return;
    }

    GrouperJexlScriptAnalysis grouperJexlScriptAnalysis = null;
    try {
      grouperJexlScriptAnalysis = GrouperLoaderJexlScriptFullSync.analyzeJexlScriptHtml(
          grouperDataEngine, script, null, subject, false, effectiveSourceIds, false);
    } catch (RuntimeException re) {
      // e.g. a script which does not parse, or a data field which does not exist
      LOG.warn("Error analyzing imported script on group: " + group.getName(), re);
      errors.add("The script could not be analyzed: " + GrouperUtil.xmlEscape(re.getMessage()));
      return;
    }
    if (grouperJexlScriptAnalysis == null) {
      errors.add("The script could not be analyzed");
      return;
    }

    // e.g. a group in the script the user cannot read, or a data field whose privacy realm they
    // cannot update.  in save mode a warning refuses the save too, as on the UI's loader screen
    if (StringUtils.isNotBlank(grouperJexlScriptAnalysis.getErrorMessage())) {
      errors.add("The script is not allowed: " + GrouperUtil.xmlEscape(grouperJexlScriptAnalysis.getErrorMessage()));
      return;
    }
    if (StringUtils.isNotBlank(grouperJexlScriptAnalysis.getWarningMessage())) {
      errors.add("The script is not allowed: " + GrouperUtil.xmlEscape(grouperJexlScriptAnalysis.getWarningMessage()));
      return;
    }

    // the configured max ABAC membership size for the importing user, so people cannot configure
    // oversized scripted groups that would overwhelm Grouper
    if (GrouperUtil.length(grouperJexlScriptAnalysis.getGrouperJexlScriptParts()) > 0) {
      Integer maxAbacMembershipSize = GrouperAbac.maxAbacMembershipSizeForSubject(subject);
      if (maxAbacMembershipSize != null) {
        int populationCount = grouperJexlScriptAnalysis.getGrouperJexlScriptParts().get(0).getPopulationCount();
        if (populationCount > maxAbacMembershipSize.intValue()) {
          errors.add("The script selects " + populationCount + " members, more than the "
              + maxAbacMembershipSize.intValue() + " you are allowed");
          return;
        }
      }
    }
  }

  /**
   * @param subject
   * @param group
   * @return true if the subject may make the group a scripted group
   */
  private static boolean canEditAbacLoader(Subject subject, Group group) {
    return PrivilegeHelper.canEditAbacLoader(subject, group);
  }

  // ---------------------------------------------------------------------------------------------
  // recent memberships

  /**
   * the checks the UI makes before it saves a recent memberships loader
   * @param attributes
   * @param subject
   * @param errors add errors here
   */
  private static void checkRecentMemberships(Map<String, String> attributes, final Subject subject, List<String> errors) {

    final String groupUuidFrom = attributes.get(recentGroupUuidFromAttributeName());
    if (StringUtils.isBlank(groupUuidFrom)) {
      errors.add("The group the recent memberships come from is required");
      return;
    }

    // looked up as root so a group the user cannot see gives the same answer as one which is not
    // there, then READ is required, as on the UI's loader screen
    boolean canRead = (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        Group groupFrom = GroupFinder.findByUuid(grouperSession, groupUuidFrom, false);
        return groupFrom != null && groupFrom.canHavePrivilege(subject, AccessPrivilege.READ.getName(), false);
      }
    });
    if (!canRead) {
      errors.add("The group the recent memberships come from was not found or you cannot read it");
      return;
    }

    String micros = attributes.get(recentMicrosAttributeName());
    boolean microsValid = false;
    try {
      microsValid = !StringUtils.isBlank(micros) && Long.parseLong(StringUtils.trim(micros)) > 0;
    } catch (NumberFormatException nfe) {
      // not a number
    }
    if (!microsValid) {
      errors.add("The recent memberships time span must be a positive number");
      return;
    }

    String includeCurrent = attributes.get(recentIncludeCurrentAttributeName());
    if (!StringUtils.isBlank(includeCurrent)) {
      try {
        GrouperUtil.booleanValue(includeCurrent);
      } catch (RuntimeException re) {
        errors.add("The recent memberships include current setting must be true or false");
        return;
      }
    }
  }

  /**
   * @param subject
   * @param group
   * @return true if the subject may make the group a recent memberships group
   */
  private static boolean canEditRecentMembershipsLoader(Subject subject, Group group) {
    return PrivilegeHelper.canEditRecentMembershipsLoader(subject, group);
  }

}
