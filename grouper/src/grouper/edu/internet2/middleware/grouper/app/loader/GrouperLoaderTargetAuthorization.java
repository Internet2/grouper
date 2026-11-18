package edu.internet2.middleware.grouper.app.loader;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;

import com.fasterxml.jackson.databind.JsonNode;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.app.loader.db.GrouperLoaderDb;
import edu.internet2.middleware.grouper.app.loader.ldap.LoaderLdapUtils;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * <pre>
 * Which groups a loader saved by a delegated loader editor may write.
 *
 * Members of the loader editors group (uiV2.loader.edit.if.in.group) who are not wheel or root
 * can save SQL and LDAP loaders.  The loader daemon runs as GrouperSystem, and a group list
 * loader (SQL_GROUP_LIST, LDAP_GROUP_LIST, LDAP_GROUPS_FROM_ATTRIBUTES) creates or edits every
 * group its query or search returns, sets those groups' members and grants the privileges the
 * group query names.  Nothing tied those groups to anything the editor manages, so an editor
 * could write the wheel group.  Rule (user decision 2026-11-13): for an editor who is not wheel
 * or root, every group the loader writes or reads must sit under a folder the editor
 * administers (STEM_ADMIN on the group's parent folder or any folder above it):
 *
 * - at save (loader screen): for SQL_GROUP_LIST the query and the group query are run and every
 *   group_name they return is checked; for every SQL and LDAP loader the and-groups are checked;
 *   for the group list types the groups-like value is checked (its fixed part before the first
 *   % or _ must name a folder under one the editor administers).  LDAP group names are only known
 *   when the loader runs, so they are checked at run time.  A refused save throws a HookVeto
 *   (the screen shows its reason) and nothing is saved.
 * - the save records who saved the loader (member id, attribute
 *   etc:attribute:loaderEditor:loaderLastEditorMemberId on the loader group, created on first
 *   use).  A save by wheel or root removes the record.
 * - at run time (GrouperLoaderType.syncGroupList, before anything is written): if the loader
 *   group has a recorded editor who is not wheel or root now, the same check runs on the groups
 *   the loader actually got (the query result, the group query result, the and-groups and the
 *   groups-like value).  If any fails the job stops with an error and nothing is written.  The
 *   result is checked, not a second run of the query, so a query that answers differently the
 *   second time does not get past the check.
 *
 * More limits on an editor who is not wheel or root (2026-11-13), at save on the loader screen
 * and at config import (GrouperLoaderImportExport.importLoaderConfig, which also records the
 * editor):
 *
 * - the user needs READ on every and-group (grouperLoaderAndGroups, grouperLoaderLdapAndGroups),
 *   as RECENT_MEMBERSHIPS and JEXL_SCRIPT saves already require; the root loader otherwise copies
 *   a private group's members.  Loader diagnostics are refused for such a user too, before they
 *   look up the and-groups as root.
 * - the LDAP results transformation class (loaded and created as root) may not be set or changed
 *   (a value a system administrator stored on that loader may be kept).
 * - a SQL loader may not use the Grouper database (database grouper, or another profile with the
 *   same JDBC URL): it would read any Grouper table.
 *
 * Loaders saved before this check (no recorded editor) and loaders saved by wheel or root run as
 * before.  Sites should review existing group list loaders (see the issue write-up).
 *
 * Kept in its own small class so the same file works in every release (2.6 on, Java 8 syntax, no
 * commons-lang import): UiV2GrouperLoader (save and diagnostics), GrouperLoaderImportExport and
 * GrouperLoaderType each only add one call.
 * </pre>
 */
public class GrouperLoaderTargetAuthorization {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperLoaderTargetAuthorization.class);

  /** reason key of the veto; there is no externalized text, so the UI shows the reason */
  public static final String VETO_REASON_KEY = "veto.grouperLoader.targets.folderAdminOnly";

  /** extension of the folder (under the attribute root folder) that holds the editor attribute */
  public static final String EDITOR_FOLDER_EXTENSION = "loaderEditor";

  /** extension of the attribute definition */
  public static final String EDITOR_ATTRIBUTE_DEF_EXTENSION = "loaderEditorDef";

  /** extension of the attribute name whose value is the member id of the last non-wheel editor */
  public static final String EDITOR_ATTRIBUTE_NAME_EXTENSION = "loaderLastEditorMemberId";

  /** group_name column of the loader query and group query (same as GrouperLoaderResultset.GROUP_NAME_COL) */
  public static final String GROUP_NAME_COLUMN = "GROUP_NAME";

  /** at most this many names in a message */
  public static final int MAX_NAMES_IN_MESSAGE = 10;

  /** start of a refused save message */
  public static final String MESSAGE_SAVE_PREFIX = "Only a Grouper system administrator can save a loader that writes or reads "
      + "groups outside the folders you administer.  You need admin on a folder above each of these: ";

  /** message when the query cannot be run at save */
  public static final String MESSAGE_SAVE_QUERY_FAILED = "The loader query or group query could not be run to check which groups it names, "
      + "so the loader was not saved.  Fix the query or ask a Grouper system administrator.";

  /** start of a refused run message */
  public static final String MESSAGE_RUN_PREFIX = "Loader stopped, nothing was changed: it was last saved by a user who is not a "
      + "Grouper system administrator, and it names groups outside the folders that user administers.  "
      + "A system administrator must review and save the loader.  Groups: ";

  /** label used for a groups-like value that names no folder */
  public static final String GROUPS_LIKE_LABEL = "groups like ";

  /**
   * no instances
   */
  private GrouperLoaderTargetAuthorization() {
  }

  // ---------------------------------------------------------------- names

  /**
   * @return name of the folder that holds the editor attribute, e.g. etc:attribute:loaderEditor
   */
  public static String editorFolderName() {
    return GrouperCheckConfig.attributeRootStemName() + ":" + EDITOR_FOLDER_EXTENSION;
  }

  /**
   * @return name of the editor attribute, e.g. etc:attribute:loaderEditor:loaderLastEditorMemberId
   */
  public static String editorAttributeDefNameName() {
    return editorFolderName() + ":" + EDITOR_ATTRIBUTE_NAME_EXTENSION;
  }

  /**
   * folders above a group or folder name, nearest first: a:b:c -> a:b, a
   * @param name group or folder name
   * @return the folder names (empty for a top level name)
   */
  public static List<String> ancestorFolderNames(String name) {
    List<String> result = new ArrayList<String>();
    if (name == null) {
      return result;
    }
    String current = name.trim();
    int index = current.lastIndexOf(':');
    while (index > 0) {
      current = current.substring(0, index);
      result.add(current);
      index = current.lastIndexOf(':');
    }
    return result;
  }

  /**
   * the folder a groups-like value is limited to: the part before the first SQL LIKE wildcard
   * (% or _), cut at its last colon.  _ counts as a wildcard (it is one in SQL LIKE), so
   * a:my_groups:% is limited to folder a only.
   * @param groupsLike e.g. school:dept:%
   * @return e.g. school:dept, or null if the value names no folder
   */
  public static String groupsLikeFolderName(String groupsLike) {
    if (groupsLike == null) {
      return null;
    }
    String value = groupsLike.trim();
    int wildcard = -1;
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      if (c == '%' || c == '_') {
        wildcard = i;
        break;
      }
    }
    String fixedPart = wildcard == -1 ? value : value.substring(0, wildcard);
    int colon = fixedPart.lastIndexOf(':');
    if (colon <= 0) {
      return null;
    }
    return fixedPart.substring(0, colon);
  }

  /**
   * @param value comma separated, may be null
   * @return the trimmed non blank parts
   */
  public static List<String> splitCommas(String value) {
    List<String> result = new ArrayList<String>();
    if (isBlank(value)) {
      return result;
    }
    for (String part : GrouperUtil.splitTrim(value, ",")) {
      if (!isBlank(part)) {
        result.add(part);
      }
    }
    return result;
  }

  /**
   * @param value
   * @return true if null or whitespace
   */
  private static boolean isBlank(String value) {
    return value == null || value.trim().length() == 0;
  }

  // ---------------------------------------------------------------- privilege checks

  /**
   * see if a subject is wheel or root, checked as root so it does not depend on the current session
   * @param subject
   * @return true if wheel or root
   */
  public static boolean isWheelOrRoot(final Subject subject) {
    if (subject == null) {
      return false;
    }
    if (PrivilegeHelper.isSystemSubject(subject)) {
      return true;
    }
    return (Boolean) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        return PrivilegeHelper.isWheelOrRoot(subject);
      }
    });
  }

  /**
   * the targets the subject may not use: group names (written groups and and-groups) that are not
   * under a folder the subject administers, and the groups-like value if it does not name such a
   * folder.  Checked as root; the subject's own privileges are looked up.  Does not consider
   * wheel (callers do).
   * @param subject the editor
   * @param groupNames groups the loader writes or reads, may be null
   * @param groupsLike groups-like value, may be blank
   * @return the names not allowed, sorted, empty if all allowed
   */
  public static Set<String> targetsNotAdministered(final Subject subject, final Collection<String> groupNames,
      final String groupsLike) {

    return (Set<String>) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        Set<String> notAllowed = new TreeSet<String>();
        // folder name -> subject administers it; most loaders write many groups in few folders
        Map<String, Boolean> folderCache = new HashMap<String, Boolean>();

        if (groupNames != null) {
          for (String groupName : groupNames) {
            if (isBlank(groupName)) {
              continue;
            }
            if (!administersAFolderAbove(grouperSession, subject, groupName.trim(), false, folderCache)) {
              notAllowed.add(groupName.trim());
            }
          }
        }

        if (!isBlank(groupsLike)) {
          String folderName = groupsLikeFolderName(groupsLike);
          // the folder itself counts: groups like a:b:% only reaches groups under a:b
          if (folderName == null || !administersAFolderAbove(grouperSession, subject, folderName, true, folderCache)) {
            notAllowed.add(GROUPS_LIKE_LABEL + groupsLike.trim());
          }
        }
        return notAllowed;
      }
    });
  }

  /**
   * see if the subject has STEM_ADMIN on a folder above the name (or on the folder itself when
   * includeSelf).  Folders that do not exist yet are skipped (the loader would create them under
   * the nearest existing one).  Call as root.
   * @param grouperSession root session
   * @param subject
   * @param name group or folder name
   * @param includeSelf true if name is a folder that counts itself
   * @param folderCache folder name to answer
   * @return true if allowed
   */
  private static boolean administersAFolderAbove(GrouperSession grouperSession, Subject subject, String name,
      boolean includeSelf, Map<String, Boolean> folderCache) {
    List<String> folderNames = new ArrayList<String>();
    if (includeSelf) {
      folderNames.add(name);
    }
    folderNames.addAll(ancestorFolderNames(name));
    for (String folderName : folderNames) {
      Boolean answer = folderCache.get(folderName);
      if (answer == null) {
        Stem stem = StemFinder.findByName(grouperSession, folderName, false);
        answer = stem != null && stem.hasStemAdmin(subject);
        folderCache.put(folderName, answer);
      }
      if (answer) {
        return true;
      }
    }
    return false;
  }

  /**
   * @param prefix message start
   * @param names not allowed names
   * @return message with at most MAX_NAMES_IN_MESSAGE names
   */
  private static String message(String prefix, Set<String> names) {
    StringBuilder result = new StringBuilder(prefix);
    int count = 0;
    Iterator<String> iterator = names.iterator();
    while (iterator.hasNext() && count < MAX_NAMES_IN_MESSAGE) {
      if (count > 0) {
        result.append(", ");
      }
      result.append(iterator.next());
      count++;
    }
    if (names.size() > MAX_NAMES_IN_MESSAGE) {
      result.append(", and ").append(names.size() - MAX_NAMES_IN_MESSAGE).append(" more");
    }
    return result.toString();
  }

  // ---------------------------------------------------------------- save time

  /**
   * run a loader query (as the loader would, against the loader database) and return the values
   * of its group_name column
   * @param dbName loader database name, e.g. grouper
   * @param query
   * @return the group names (empty if the query has no group_name column)
   * @throws Exception if the query cannot be run
   */
  public static Set<String> groupNamesFromQuery(String dbName, String query) throws Exception {
    Set<String> result = new TreeSet<String>();
    if (isBlank(query)) {
      return result;
    }
    GrouperLoaderDb grouperLoaderDb = GrouperLoaderConfig.retrieveDbProfile(isBlank(dbName) ? "grouper" : dbName.trim());
    Connection connection = null;
    Statement statement = null;
    ResultSet resultSet = null;
    try {
      connection = grouperLoaderDb.connection();
      statement = connection.createStatement();
      resultSet = statement.executeQuery(query);
      ResultSetMetaData metaData = resultSet.getMetaData();
      int groupNameColumn = -1;
      for (int i = 1; i <= metaData.getColumnCount(); i++) {
        if (GROUP_NAME_COLUMN.equalsIgnoreCase(metaData.getColumnLabel(i))
            || GROUP_NAME_COLUMN.equalsIgnoreCase(metaData.getColumnName(i))) {
          groupNameColumn = i;
          break;
        }
      }
      if (groupNameColumn == -1) {
        return result;
      }
      while (resultSet.next()) {
        String groupName = resultSet.getString(groupNameColumn);
        if (!isBlank(groupName)) {
          result.add(groupName.trim());
        }
      }
    } finally {
      GrouperUtil.closeQuietly(resultSet);
      GrouperUtil.closeQuietly(statement);
      GrouperUtil.closeQuietly(connection);
    }
    return result;
  }

  /**
   * @param editLoaderType SQL or LDAP (the loader screen's source type)
   * @param sqlType e.g. SQL_GROUP_LIST
   * @param ldapType e.g. LDAP_GROUP_LIST
   * @return true if the loader being saved is a group list type
   */
  public static boolean isGroupListType(String editLoaderType, String sqlType, String ldapType) {
    if ("SQL".equals(editLoaderType)) {
      return "SQL_GROUP_LIST".equals(sqlType);
    }
    if ("LDAP".equals(editLoaderType)) {
      return "LDAP_GROUP_LIST".equals(ldapType) || "LDAP_GROUPS_FROM_ATTRIBUTES".equals(ldapType);
    }
    return false;
  }

  /**
   * the folder rule of a save (the groups the loader writes or reads).  The loader screen and the
   * import use the full check, checkUiSave with the group and the transformation class, which
   * also calls this.
   * @param subject logged in user
   * @param saveIsLoader true if the user saves a loader, false if the user removes it
   * @param editLoaderType SQL, LDAP, JEXL_SCRIPT, RECENT_MEMBERSHIPS, ...
   * @param sqlType SQL_SIMPLE or SQL_GROUP_LIST (when SQL)
   * @param sqlDatabaseName loader database (when SQL)
   * @param sqlQuery query (when SQL)
   * @param sqlGroupQuery group query (when SQL)
   * @param ldapType LDAP_SIMPLE, LDAP_GROUP_LIST, LDAP_GROUPS_FROM_ATTRIBUTES (when LDAP)
   * @param groupsLike groups-like value
   * @param andGroups comma separated and-group names
   * @return null if allowed, else the reason
   */
  public static String checkUiSave(Subject subject, boolean saveIsLoader, String editLoaderType, String sqlType,
      String sqlDatabaseName, String sqlQuery, String sqlGroupQuery, String ldapType, String groupsLike, String andGroups) {

    // removing a loader only stops it; only SQL and LDAP loaders write groups chosen by the editor
    if (!saveIsLoader || !("SQL".equals(editLoaderType) || "LDAP".equals(editLoaderType))) {
      return null;
    }
    if (isWheelOrRoot(subject)) {
      return null;
    }

    boolean groupList = isGroupListType(editLoaderType, sqlType, ldapType);

    List<String> names = new ArrayList<String>(splitCommas(andGroups));

    if (groupList && "SQL".equals(editLoaderType)) {
      try {
        names.addAll(groupNamesFromQuery(sqlDatabaseName, sqlQuery));
        names.addAll(groupNamesFromQuery(sqlDatabaseName, sqlGroupQuery));
      } catch (Exception e) {
        // the database error may hold data, so it is logged, not shown
        LOG.warn("Could not run loader query to check its groups, editor: " + subject.getSourceId() + "::" + subject.getId(), e);
        return MESSAGE_SAVE_QUERY_FAILED;
      }
    }

    Set<String> notAllowed = targetsNotAdministered(subject, names, groupList ? groupsLike : null);
    if (notAllowed.isEmpty()) {
      return null;
    }
    return message(MESSAGE_SAVE_PREFIX, notAllowed);
  }

  /**
   * check a save on the loader screen and throw a HookVeto (the UI shows its reason) if refused;
   * if allowed and a SQL or LDAP loader is saved, record who saved it (wheel or root: remove the
   * record).  UiV2GrouperLoader.editGrouperLoaderSave calls this with the submitted fields.
   * @param subject logged in user
   * @param group the loader group
   * @param saveIsLoader true if the user saves a loader, false if the user removes it
   * @param editLoaderType SQL, LDAP, ...
   * @param sqlType
   * @param sqlDatabaseName
   * @param sqlQuery
   * @param sqlGroupQuery
   * @param ldapType
   * @param groupsLike
   * @param andGroups
   */
  public static void assertUiSaveAllowedAndRecordEditor(Subject subject, Group group, boolean saveIsLoader, String editLoaderType,
      String sqlType, String sqlDatabaseName, String sqlQuery, String sqlGroupQuery, String ldapType, String groupsLike, String andGroups) {
    String error = checkUiSave(subject, saveIsLoader, editLoaderType, sqlType, sqlDatabaseName, sqlQuery, sqlGroupQuery,
        ldapType, groupsLike, andGroups);
    if (error != null) {
      throw new HookVeto(VETO_REASON_KEY, error);
    }
    if (saveIsLoader && ("SQL".equals(editLoaderType) || "LDAP".equals(editLoaderType))) {
      recordEditor(group, subject);
    }
  }

  // ---------------------------------------------------------------- more limits on delegated editors (2026-11-13)

  /** message when a non-wheel editor sets or changes the LDAP results transformation class */
  public static final String MESSAGE_TRANSFORMATION_CLASS = "Only a Grouper system administrator can set or change the "
      + "LDAP results transformation class of a loader.";

  /** message when a non-wheel editor saves or imports a SQL loader on the Grouper database */
  public static final String MESSAGE_GROUPER_DATABASE = "Only a Grouper system administrator can save a SQL loader that "
      + "queries the Grouper database.";

  /** start of the message when a non-wheel editor names and-groups they cannot read (same text if a group does not exist) */
  public static final String MESSAGE_AND_GROUPS_PREFIX = "You need READ on each of the groups to also require "
      + "(or the group does not exist): ";

  /** start of the message when diagnostics are refused because of and-groups */
  public static final String MESSAGE_DIAGNOSTICS_PREFIX = "Diagnostics not run.  " + MESSAGE_AND_GROUPS_PREFIX;

  /** message when an import names a loader attribute more than once */
  public static final String MESSAGE_IMPORT_DUPLICATE = "The loader config names a loader attribute more than once, "
      + "so it was not imported.";

  /** message when an import has no user to check */
  public static final String MESSAGE_IMPORT_NO_SUBJECT = "No logged in user to check the loader import against, "
      + "so it was not imported.";

  /** loader attribute: SQL loader type (SQL_SIMPLE, SQL_GROUP_LIST) */
  public static final String ATTR_SQL_TYPE = "grouperLoaderType";

  /** loader attribute: SQL loader database */
  public static final String ATTR_SQL_DB_NAME = "grouperLoaderDbName";

  /** loader attribute: SQL loader query */
  public static final String ATTR_SQL_QUERY = "grouperLoaderQuery";

  /** loader attribute: SQL loader group query */
  public static final String ATTR_SQL_GROUP_QUERY = "grouperLoaderGroupQuery";

  /** loader attribute: SQL loader groups like */
  public static final String ATTR_SQL_GROUPS_LIKE = "grouperLoaderGroupsLike";

  /** loader attribute: SQL loader and-groups */
  public static final String ATTR_SQL_AND_GROUPS = "grouperLoaderAndGroups";

  /** loader attribute: LDAP loader type */
  public static final String ATTR_LDAP_TYPE = "grouperLoaderLdapType";

  /** loader attribute: LDAP loader groups like */
  public static final String ATTR_LDAP_GROUPS_LIKE = "grouperLoaderLdapGroupsLike";

  /** loader attribute: LDAP loader and-groups */
  public static final String ATTR_LDAP_AND_GROUPS = "grouperLoaderLdapAndGroups";

  /** loader attribute: LDAP results transformation class */
  public static final String ATTR_LDAP_RESULTS_TRANSFORMATION_CLASS = "grouperLoaderLdapResultsTransformationClass";

  /**
   * see if a loader database is the Grouper database: the name grouper, or another database
   * profile with the same JDBC URL.  A database that cannot be looked up counts as the Grouper
   * database (fail closed).
   * @param dbName loader database name, blank means grouper
   * @return true if it is (or may be) the Grouper database
   */
  public static boolean isGrouperDatabase(String dbName) {
    if (isBlank(dbName) || "grouper".equalsIgnoreCase(dbName.trim())) {
      return true;
    }
    try {
      String url = GrouperLoaderConfig.retrieveDbProfile(dbName.trim()).getUrl();
      String grouperUrl = GrouperLoaderConfig.retrieveDbProfile("grouper").getUrl();
      return isBlank(url) || (grouperUrl != null && grouperUrl.trim().equalsIgnoreCase(url.trim()));
    } catch (RuntimeException re) {
      LOG.warn("Could not look up loader database: " + dbName, re);
      return true;
    }
  }

  /**
   * the and-groups the subject may not read: names that do not resolve to a group, or groups
   * the subject has no READ on.  Checked as root; does not consider wheel (callers do).
   * @param subject
   * @param andGroups comma separated group names, may be blank
   * @return the names not allowed, sorted, empty if all allowed
   */
  public static Set<String> andGroupsNotReadable(final Subject subject, final String andGroups) {
    final List<String> names = splitCommas(andGroups);
    if (names.isEmpty()) {
      return new TreeSet<String>();
    }
    return (Set<String>) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        Set<String> notAllowed = new TreeSet<String>();
        for (String name : names) {
          Group andGroup = GroupFinder.findByName(grouperSession, name, false);
          // same answer for a missing group and one the subject cannot read, so this is no existence test
          if (andGroup == null || !andGroup.hasRead(subject)) {
            notAllowed.add(name);
          }
        }
        return notAllowed;
      }
    });
  }

  /**
   * @param group loader group
   * @return the LDAP results transformation class stored on the group's LDAP loader, or null
   */
  public static String storedLdapResultsTransformationClass(final Group group) {
    if (group == null) {
      return null;
    }
    return (String) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        return ldapAttributeValue(group, LoaderLdapUtils.grouperLoaderLdapResultsTransformationClassName());
      }
    });
  }

  /**
   * read a value of the group's LDAP loader assignment.  Call as root.
   * @param group
   * @param attributeDefNameName
   * @return the value or null
   */
  private static String ldapAttributeValue(Group group, String attributeDefNameName) {
    AttributeDefName ldapMarker = AttributeDefNameFinder.findByName(LoaderLdapUtils.grouperLoaderLdapName(), false);
    if (ldapMarker == null) {
      return null;
    }
    AttributeAssign attributeAssign = group.getAttributeDelegate().retrieveAssignment(AttributeDef.ACTION_DEFAULT, ldapMarker, false, false);
    if (attributeAssign == null) {
      return null;
    }
    return attributeAssign.getAttributeValueDelegate().retrieveValueString(attributeDefNameName);
  }

  /**
   * full check of a save on the loader screen (and of an import) by a user who may not be wheel:
   * the LDAP results transformation class may not be set or changed (it is loaded and created as
   * root), the SQL database may not be the Grouper database, the user needs READ on every
   * and-group, and then the folder rule of checkUiSave.
   * @param subject logged in user
   * @param group the loader group (to compare the stored transformation class), may be null
   * @param saveIsLoader true if the user saves a loader, false if the user removes it
   * @param editLoaderType SQL, LDAP, JEXL_SCRIPT, RECENT_MEMBERSHIPS, ...
   * @param sqlType
   * @param sqlDatabaseName
   * @param sqlQuery
   * @param sqlGroupQuery
   * @param ldapType
   * @param groupsLike
   * @param andGroups
   * @param ldapResultsTransformationClass submitted LDAP results transformation class
   * @return null if allowed, else the reason
   */
  public static String checkUiSave(Subject subject, Group group, boolean saveIsLoader, String editLoaderType, String sqlType,
      String sqlDatabaseName, String sqlQuery, String sqlGroupQuery, String ldapType, String groupsLike, String andGroups,
      String ldapResultsTransformationClass) {

    if (!saveIsLoader || !("SQL".equals(editLoaderType) || "LDAP".equals(editLoaderType))) {
      return null;
    }
    if (isWheelOrRoot(subject)) {
      return null;
    }

    // the transformation class is created as root when the loader or diagnostics run; a value a
    // system administrator already stored on this loader may be kept
    if ("LDAP".equals(editLoaderType) && !isBlank(ldapResultsTransformationClass)) {
      String stored = storedLdapResultsTransformationClass(group);
      if (stored == null || !stored.trim().equals(ldapResultsTransformationClass.trim())) {
        return MESSAGE_TRANSFORMATION_CLASS;
      }
    }

    // checked before any query runs, so the check itself does not query the Grouper database
    if ("SQL".equals(editLoaderType) && isGrouperDatabase(sqlDatabaseName)) {
      return MESSAGE_GROUPER_DATABASE;
    }

    // a missing or unknown type is checked as a group list: the loader works out SQL_GROUP_LIST
    // itself from a blank type and a group_name column, so only an explicit simple type skips the
    // group list checks (fail closed)
    if ("SQL".equals(editLoaderType)) {
      sqlType = sqlType != null && "SQL_SIMPLE".equalsIgnoreCase(sqlType.trim()) ? "SQL_SIMPLE" : "SQL_GROUP_LIST";
    }
    if ("LDAP".equals(editLoaderType)) {
      ldapType = ldapType != null && "LDAP_SIMPLE".equalsIgnoreCase(ldapType.trim()) ? "LDAP_SIMPLE" : "LDAP_GROUP_LIST";
    }

    Set<String> notReadable = andGroupsNotReadable(subject, andGroups);
    if (!notReadable.isEmpty()) {
      return message(MESSAGE_AND_GROUPS_PREFIX, notReadable);
    }

    return checkUiSave(subject, saveIsLoader, editLoaderType, sqlType, sqlDatabaseName, sqlQuery, sqlGroupQuery,
        ldapType, groupsLike, andGroups);
  }

  /**
   * the full check (checkUiSave with the group and transformation class), throw a HookVeto if
   * refused, else record the editor.  UiV2GrouperLoader.editGrouperLoaderSave calls this.
   * @param subject logged in user
   * @param group the loader group
   * @param saveIsLoader
   * @param editLoaderType
   * @param sqlType
   * @param sqlDatabaseName
   * @param sqlQuery
   * @param sqlGroupQuery
   * @param ldapType
   * @param groupsLike
   * @param andGroups
   * @param ldapResultsTransformationClass
   */
  public static void assertUiSaveAllowedAndRecordEditor(Subject subject, Group group, boolean saveIsLoader, String editLoaderType,
      String sqlType, String sqlDatabaseName, String sqlQuery, String sqlGroupQuery, String ldapType, String groupsLike, String andGroups,
      String ldapResultsTransformationClass) {
    String error = checkUiSave(subject, group, saveIsLoader, editLoaderType, sqlType, sqlDatabaseName, sqlQuery, sqlGroupQuery,
        ldapType, groupsLike, andGroups, ldapResultsTransformationClass);
    if (error != null) {
      throw new HookVeto(VETO_REASON_KEY, error);
    }
    if (saveIsLoader && ("SQL".equals(editLoaderType) || "LDAP".equals(editLoaderType))) {
      recordEditor(group, subject);
    }
  }

  /** the loader attribute keys the import check reads (none ends with another) */
  private static final String[] IMPORT_ATTRIBUTE_KEYS = new String[] {ATTR_SQL_TYPE, ATTR_SQL_DB_NAME, ATTR_SQL_QUERY,
      ATTR_SQL_GROUP_QUERY, ATTR_SQL_GROUPS_LIKE, ATTR_SQL_AND_GROUPS, ATTR_LDAP_TYPE, ATTR_LDAP_GROUPS_LIKE,
      ATTR_LDAP_AND_GROUPS, ATTR_LDAP_RESULTS_TRANSFORMATION_CLASS};

  /**
   * the attribute key of an exported loader attribute name: the part after the last colon, and if
   * that ends with a known loader attribute key, that key (the SQL attributes carry the legacy
   * prefix, whatever it is configured to: etc:legacy:attribute:legacyAttribute_grouperLoaderQuery
   * -> grouperLoaderQuery)
   * @param attributeName
   * @return the key or null
   */
  public static String importAttributeKey(String attributeName) {
    if (isBlank(attributeName)) {
      return null;
    }
    String key = attributeName.trim();
    int colon = key.lastIndexOf(':');
    if (colon >= 0) {
      key = key.substring(colon + 1);
    }
    for (String knownKey : IMPORT_ATTRIBUTE_KEYS) {
      if (key.endsWith(knownKey)) {
        return knownKey;
      }
    }
    return key;
  }

  /**
   * check a loader config import (GrouperLoaderImportExport.importLoaderConfig) by the user of the
   * current grouper session with the same rules as a save on the loader screen, and if allowed
   * record the editor for the run-time check.  Only SQL and LDAP loaders are checked here (scripted
   * and recent memberships imports are checked elsewhere); json that cannot be read is left to the
   * import, which refuses it.
   * @param json exported loader config
   * @param group group to import onto
   * @return the errors, empty if allowed
   */
  public static List<String> checkImportAndRecordEditor(String json, Group group) {
    List<String> errors = new ArrayList<String>();
    JsonNode jsonNode = null;
    try {
      jsonNode = GrouperUtil.jsonJacksonNode(json);
    } catch (RuntimeException re) {
      return errors;
    }
    String loaderType = jsonNode == null ? null : GrouperUtil.jsonJacksonGetString(jsonNode, "loaderType");
    if (!"SQL".equals(loaderType) && !"LDAP".equals(loaderType)) {
      return errors;
    }
    GrouperSession grouperSession = GrouperSession.staticGrouperSession(false);
    Subject subject = grouperSession == null ? null : grouperSession.getSubject();
    if (subject == null) {
      errors.add(MESSAGE_IMPORT_NO_SUBJECT);
      return errors;
    }
    if (isWheelOrRoot(subject)) {
      // a wheel import has no record, like a wheel save
      if (!hasSqlOrLdapLoaderValues(group)) {
        recordEditor(group, subject);
      }
      return errors;
    }

    // attribute key -> value; a key named twice is refused, since which one is stored is not clear
    Map<String, String> values = new HashMap<String, String>();
    JsonNode attributesNode = jsonNode.get("attributes");
    if (attributesNode != null && attributesNode.isArray()) {
      for (int index = 0; index < attributesNode.size(); index++) {
        JsonNode attributeNode = attributesNode.get(index);
        String key = importAttributeKey(GrouperUtil.jsonJacksonGetString(attributeNode, "attributeName"));
        if (key == null) {
          continue;
        }
        if (values.containsKey(key)) {
          errors.add(MESSAGE_IMPORT_DUPLICATE);
          return errors;
        }
        values.put(key, GrouperUtil.jsonJacksonGetString(attributeNode, "attributeValue"));
      }
    }

    String error = null;
    if ("SQL".equals(loaderType)) {
      error = checkUiSave(subject, group, true, "SQL", values.get(ATTR_SQL_TYPE), values.get(ATTR_SQL_DB_NAME),
          values.get(ATTR_SQL_QUERY), values.get(ATTR_SQL_GROUP_QUERY), null, values.get(ATTR_SQL_GROUPS_LIKE),
          values.get(ATTR_SQL_AND_GROUPS), null);
    } else {
      // the group has no loader yet (the import refuses one that has), so any transformation class is refused
      error = checkUiSave(subject, null, true, "LDAP", null, null, null, null, values.get(ATTR_LDAP_TYPE),
          values.get(ATTR_LDAP_GROUPS_LIKE), values.get(ATTR_LDAP_AND_GROUPS), values.get(ATTR_LDAP_RESULTS_TRANSFORMATION_CLASS));
    }
    if (error != null) {
      errors.add(error);
      return errors;
    }
    // a group that already has a loader is refused by the import itself; do not change its record
    if (!hasSqlOrLdapLoaderValues(group)) {
      recordEditor(group, subject);
    }
    return errors;
  }

  /**
   * @param group
   * @return true if the group already has SQL or LDAP loader settings
   */
  private static boolean hasSqlOrLdapLoaderValues(final Group group) {
    if (group == null) {
      return false;
    }
    return (Boolean) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        return !isBlank(legacyAttributeValueOrNull(group, GrouperLoader.GROUPER_LOADER_TYPE))
            || !isBlank(legacyAttributeValueOrNull(group, GrouperLoader.GROUPER_LOADER_QUERY))
            || !isBlank(ldapAttributeValue(group, LoaderLdapUtils.grouperLoaderLdapTypeName()));
      }
    });
  }

  /**
   * value of a legacy loader attribute on the group, or null when the group does not have the
   * loader group type at all (Group.getAttributeValue throws AttributeNotFoundException in that
   * case even when told not to throw, e.g. a group being imported into for the first time)
   * @param group
   * @param attributeName
   * @return the value or null
   */
  @SuppressWarnings("deprecation")
  private static String legacyAttributeValueOrNull(Group group, String attributeName) {
    try {
      return group.getAttributeValue(attributeName, false, false);
    } catch (edu.internet2.middleware.grouper.exception.AttributeNotFoundException anfe) {
      return null;
    }
  }

  /**
   * check loader diagnostics (UiV2GrouperLoader.loaderDiagnosticsRun) before they look up the
   * loader's and-groups as root: a user who is not wheel or root needs READ on each of them,
   * otherwise the diagnostics tell whether a hidden group exists
   * @param subject logged in user
   * @param group the loader group
   * @return null if allowed, else the reason
   */
  public static String checkDiagnostics(final Subject subject, final Group group) {
    if (group == null || isWheelOrRoot(subject)) {
      return null;
    }
    String andGroups = (String) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        StringBuilder result = new StringBuilder();
        String sqlAndGroups = GrouperLoaderType.attributeValueOrDefaultOrNull(group, GrouperLoader.GROUPER_LOADER_AND_GROUPS);
        if (!isBlank(sqlAndGroups)) {
          result.append(sqlAndGroups);
        }
        String ldapAndGroups = ldapAttributeValue(group, LoaderLdapUtils.grouperLoaderLdapAndGroupsName());
        if (!isBlank(ldapAndGroups)) {
          result.append(result.length() > 0 ? "," : "").append(ldapAndGroups);
        }
        return result.toString();
      }
    });
    Set<String> notReadable = andGroupsNotReadable(subject, andGroups);
    if (notReadable.isEmpty()) {
      return null;
    }
    return message(MESSAGE_DIAGNOSTICS_PREFIX, notReadable);
  }

  // ---------------------------------------------------------------- editor record

  /**
   * find the editor attribute, as root
   * @param createIfMissing true to create the folder, definition and name
   * @return the attribute name or null if missing and not created
   */
  public static AttributeDefName editorAttributeDefName(final boolean createIfMissing) {
    return (AttributeDefName) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(editorAttributeDefNameName(), false);
        if (attributeDefName != null || !createIfMissing) {
          return attributeDefName;
        }
        Stem folder = new StemSave(grouperSession).assignName(editorFolderName())
            .assignCreateParentStemsIfNotExist(true).save();
        String attributeDefName2 = editorFolderName() + ":" + EDITOR_ATTRIBUTE_DEF_EXTENSION;
        AttributeDef attributeDef = AttributeDefFinder.findByName(attributeDefName2, false);
        if (attributeDef == null) {
          // single valued string on groups; no default privileges, so only wheel and root see or change it
          attributeDef = folder.addChildAttributeDef(EDITOR_ATTRIBUTE_DEF_EXTENSION, AttributeDefType.attr);
          attributeDef.setAssignToGroup(true);
          attributeDef.setMultiAssignable(false);
          attributeDef.setMultiValued(false);
          attributeDef.setValueType(AttributeDefValueType.string);
          attributeDef.store();
        }
        return folder.addChildAttributeDefName(attributeDef, EDITOR_ATTRIBUTE_NAME_EXTENSION, EDITOR_ATTRIBUTE_NAME_EXTENSION);
      }
    });
  }

  /**
   * record who saved the loader: the member id of a non-wheel editor, or no record for wheel/root
   * @param group loader group
   * @param subject editor
   */
  public static void recordEditor(final Group group, final Subject subject) {
    if (group == null || subject == null) {
      return;
    }
    final boolean wheelOrRoot = isWheelOrRoot(subject);
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        if (wheelOrRoot) {
          AttributeDefName attributeDefName = editorAttributeDefName(false);
          if (attributeDefName != null && group.getAttributeDelegate().hasAttribute(attributeDefName)) {
            group.getAttributeDelegate().removeAttribute(attributeDefName);
          }
          return null;
        }
        editorAttributeDefName(true);
        Member member = MemberFinder.findBySubject(grouperSession, subject, true);
        group.getAttributeValueDelegate().assignValue(editorAttributeDefNameName(), member.getUuid());
        return null;
      }
    });
  }

  /**
   * @param group loader group
   * @return member id of the recorded (non-wheel) editor or null
   */
  public static String recordedEditorMemberId(final Group group) {
    if (group == null) {
      return null;
    }
    return (String) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        if (editorAttributeDefName(false) == null) {
          return null;
        }
        return group.getAttributeValueDelegate().retrieveValueString(editorAttributeDefNameName());
      }
    });
  }

  // ---------------------------------------------------------------- run time

  /**
   * check a group list run before anything is written
   * @param loaderGroupUuid id of the loader group (Hib3GrouperLoaderLog.getGroupUuid())
   * @param groupNames group names from the query result
   * @param groupNamesFromGroupQuery group names from the group query, may be null
   * @param andGroups and-groups, may be null
   * @param groupsLike groups-like value, may be blank
   * @return null if allowed, else the reason
   */
  public static String checkRun(final String loaderGroupUuid, final Collection<String> groupNames,
      final Collection<String> groupNamesFromGroupQuery, final List<Group> andGroups, final String groupsLike) {
    if (isBlank(loaderGroupUuid)) {
      return null;
    }
    return (String) GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        Group loaderGroup = GroupFinder.findByUuid(grouperSession, loaderGroupUuid, false);
        String editorMemberId = recordedEditorMemberId(loaderGroup);

        // no record: saved before this check, or by wheel or root
        if (isBlank(editorMemberId)) {
          return null;
        }
        Member member = MemberFinder.findByUuid(grouperSession, editorMemberId, false);
        Subject subject = null;
        try {
          subject = member == null ? null : member.getSubject();
        } catch (RuntimeException re) {
          // subject no longer resolvable: nobody can vouch for the targets
          subject = null;
        }

        List<String> names = new ArrayList<String>();
        if (groupNames != null) {
          names.addAll(groupNames);
        }
        if (groupNamesFromGroupQuery != null) {
          names.addAll(groupNamesFromGroupQuery);
        }
        if (andGroups != null) {
          for (Group andGroup : andGroups) {
            if (andGroup != null) {
              names.add(andGroup.getName());
            }
          }
        }

        if (subject == null) {
          Set<String> all = new TreeSet<String>(names);
          if (!isBlank(groupsLike)) {
            all.add(GROUPS_LIKE_LABEL + groupsLike.trim());
          }
          if (all.isEmpty()) {
            return null;
          }
          return message(MESSAGE_RUN_PREFIX, all);
        }
        if (isWheelOrRoot(subject)) {
          return null;
        }
        Set<String> notAllowed = targetsNotAdministered(subject, names, groupsLike);
        if (notAllowed.isEmpty()) {
          return null;
        }
        return message(MESSAGE_RUN_PREFIX, notAllowed);
      }
    });
  }

  /**
   * check a group list run and throw (the job ends in error, nothing is written) if refused.
   * GrouperLoaderType.syncGroupList calls this first.
   * @param loaderGroupUuid
   * @param groupNames
   * @param groupNamesFromGroupQuery
   * @param andGroups
   * @param groupsLike
   */
  public static void assertRunAllowed(String loaderGroupUuid, Collection<String> groupNames,
      Collection<String> groupNamesFromGroupQuery, List<Group> andGroups, String groupsLike) {
    String error = checkRun(loaderGroupUuid, groupNames, groupNamesFromGroupQuery, andGroups, groupsLike);
    if (error != null) {
      throw new RuntimeException(error);
    }
  }

}
